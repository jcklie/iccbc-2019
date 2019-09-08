package com.mrklie.yangtao.ar

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.ar.core.*
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.HitTestResult
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import com.mrklie.yangtao.R
import com.quickbirdstudios.yuv2mat.Yuv
import kotlinx.android.synthetic.main.activity_ar.*
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.core.Point
import org.opencv.imgproc.Imgproc.*
import java.util.*
import kotlin.math.max


class ArActivity : AppCompatActivity() {
    private lateinit var arFragment: ArFragment
    private lateinit var arPreviewRaw: ImageView
    private lateinit var arPreviewProcessed: ImageView
    private lateinit var focusView: View
    private lateinit var scanButton: FloatingActionButton

    private lateinit var cameraManager: CameraManager

    private lateinit var classifier: HanziClassifier

    private var displayWidth: Int? = 0
    private var displayHeight: Int? = 0
    private var showDebug: Boolean = false

    private var arSession: Session? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ar)

        arFragment = supportFragmentManager.findFragmentById(R.id.ux_fragment) as ArFragment
        arPreviewRaw = findViewById(R.id.ar_preview_raw)
        arPreviewProcessed = findViewById(R.id.ar_preview_processed)
        focusView = findViewById(R.id.ar_focus)

        scanButton = findViewById(R.id.ar_scan_button)
        scanButton.setOnClickListener { view ->
            scanImage()
        }

        cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager

        val displayMetrics = DisplayMetrics()
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics)
        displayWidth = displayMetrics.widthPixels
        displayHeight = displayMetrics.heightPixels

        initializeSession()

        // Shared Camera
        // createCameraCaptureSession()

        resizeFocusView()

        classifier = HanziClassifier(applicationContext, "model.tflite", "labels.txt")

        val SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        showDebug = SP.getBoolean("showDebug", false)
    }

    private fun initializeSession() {
        // Register scene update to show focus view once tracking is done
        arFragment.arSceneView.scene.addOnUpdateListener { frameTime ->
            // handleFocusView()

            //focusView.visibility = View.VISIBLE
        }

        arSession = Session(this, EnumSet.of(Session.Feature.SHARED_CAMERA))
        val arConfig = Config(arSession)

        // Autofocus
        if (arConfig.focusMode == Config.FocusMode.FIXED) {
            arConfig.focusMode = Config.FocusMode.AUTO
        }
        arConfig.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE

        arSession!!.configure(arConfig)
        arFragment.arSceneView!!.setupSession(arSession)
    }

    private fun handleFocusView() {
        // https://github.com/google-ar/sceneform-android-sdk/issues/68#issuecomment-394813418
        for (plane in arSession!!.getAllTrackables(Plane::class.java)) {
            if (plane.trackingState == TrackingState.TRACKING) {

            }
        }

        // If there are no planes to track, then hide the view
        focusView.visibility = View.INVISIBLE
    }

    private fun resizeFocusView() {
        val longestSide = max(displayWidth!!, displayHeight!!)
        val focusSize = (longestSide * FOCUS_VIEW_PERCENTAGE).toInt()

        val params = focusView.layoutParams
        params.width = focusSize
        params.height = focusSize

        focusView.visibility = View.VISIBLE
    }

    private fun scanImage() {
        val frame = arSession!!.update()
        val image = frame.acquireCameraImage()

        // arSession!!.getSupportedCameraConfigs()[2].imageSize

        image.use {
            val width = image.width
            val height = image.height
            val longestSide = max(width, image.height)
            val size = longestSide * FOCUS_VIEW_PERCENTAGE

            val mat_raw = Yuv.rgb(image)

            // Rotate it
            // Core.flip(mat.t(), mat, 0)
            Core.flip(mat_raw.t(), mat_raw, 1)

            // Cut it to the focus size
            // The aspect ratio of the display (which shows the focus rectangle) and
            // the camera are generally not the same. The image is displayed by scaling the
            // longer side to the same size and then center the image. Therefore, we use the
            // longest side with the known focus percentage to compute how many pixels the
            // focus should be.
            val roi = Rect(Point((height - size) / 2, (width - size) / 2), Size(size, size))
            val mat_processed = Mat(mat_raw, roi)

            // Convert to rgb and threshold
            cvtColor(mat_processed, mat_processed, COLOR_BGR2GRAY)
            threshold(mat_processed, mat_processed, 120.0, 255.0, THRESH_BINARY)

            // Erode / dilate to remove noise specks and close areas
            val morph_size = 2.0
            val morphKernel = getStructuringElement( MORPH_RECT, Size( 2*morph_size + 1, 2*morph_size+1 ),  Point( morph_size, morph_size ) )

            morphologyEx(mat_processed, mat_processed, MORPH_OPEN, morphKernel)
            morphologyEx(mat_processed, mat_processed, MORPH_CLOSE, morphKernel)

            // Detect contours
            val contours = ArrayList<MatOfPoint>()
            val hierarchy = Mat()
            findContours(mat_processed, contours, hierarchy, RETR_LIST, CHAIN_APPROX_SIMPLE )

            // Draw contours
            for (contour in contours) {
                if (contourArea(contour) < 10 ) {
                    continue
                }

                val areaPoints = MatOfPoint2f(*contour.toArray())
                val rect = minAreaRect(areaPoints)

                val vertices = arrayOfNulls<Point>(4)
                rect.points(vertices)

                for (j in 0 until 4){
                    line(mat_processed, vertices[j], vertices[(j+1)%4], Scalar(0.0,255.0,0.0))
                }
            }

            // We rotated the image, therefore height and width are swapped
            val bitmap_raw = Bitmap.createBitmap(height, width, Bitmap.Config.ARGB_8888 )
            Utils.matToBitmap(mat_raw, bitmap_raw)

            val bitmap_processed = Bitmap.createBitmap(size.toInt(), size.toInt(), Bitmap.Config.ARGB_8888 )
            Utils.matToBitmap(mat_processed, bitmap_processed)

            val predictions = classifier.predict(mat_processed)

            // Display the previews
            if (showDebug) {
                arPreviewRaw.setImageBitmap(bitmap_raw)
                arPreviewRaw.bringToFront()

                arPreviewProcessed.setImageBitmap(bitmap_processed)
                arPreviewProcessed.bringToFront()
                ar_debug_prediction.text = predictions.joinToString("")
            }

            val hits = frame.hitTest((displayWidth!! / 2).toFloat(), (displayHeight!! / 2).toFloat())

            for (hit in hits) {
                val trackable = hit.trackable
                if (trackable is Plane && trackable.isPoseInPolygon(hit.hitPose)) {
                    placeScanDialog(hit.createAnchor(), predictions)
                    // placeObject(hit.createAnchor(), prediction[0].toString() )
                }
            }
        }
    }

    private fun placeScanDialog(anchor: Anchor, predictions: List<String>) {
        ViewRenderable.builder()
            .setView(this, R.layout.scan_dialog)
            .build()
            .thenAccept {renderable ->
                renderable.isShadowReceiver = false

                val view = renderable.view
                val spinner = view.findViewById<Spinner>(R.id.scan_dialog_spinner)
                val dataAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, predictions)
                spinner.adapter = dataAdapter

                val yesButton = view.findViewById<Button>(R.id.scan_dialog_yes)
                val noButton = view.findViewById<Button>(R.id.scan_dialog_no)

                // Create the node
                val anchorNode = AnchorNode(anchor)
                anchorNode.renderable = renderable
                anchorNode.localScale = Vector3(0.1f, 0.1f, 0.1f)

                // Set listeners
                yesButton.setOnClickListener {
                    placeObject(anchor, spinner.selectedItem.toString())
                    anchorNode.setParent(null)
                }

                noButton.setOnClickListener {
                    anchorNode.setParent(null)
                }

                // Place the node
                arFragment.arSceneView.scene.addChild(anchorNode)
            }
    }

    private fun placeObject(anchor: Anchor, hanzi: String) {
        val modelName = "models/hanzi${classifier.labelToIndex(hanzi)}.sfb"
        ModelRenderable.builder()
            .setSource(this, Uri.parse(modelName))
            .setRegistryId(hanzi)
            .build()
            .thenAccept {
                addHanziToScene(anchor, it)
            }
            .exceptionally {
                Toast.makeText(this, "Could not place model", Toast.LENGTH_SHORT).show()
                return@exceptionally null
            }
    }

    private fun addHanziToScene(anchor: Anchor, model: ModelRenderable) {
        val anchorNode = AnchorNode(anchor)
        TransformableNode(arFragment.transformationSystem).apply {
            setParent(anchorNode)
            val firstRotation = Quaternion.axisAngle(Vector3(1f, 0f, 0f), -90f)
            val secondRotation = Quaternion.axisAngle(Vector3(0f, 0f, 1f), 180f)
            localRotation = Quaternion.multiply(firstRotation, secondRotation)
            translationController.isEnabled = false
            renderable = model
            select()

            setOnTapListener {hitTestResult: HitTestResult, motionEvent: MotionEvent ->
                anchorNode.setParent(null)
                setParent(null)
                anchor.detach()
            }
        }

        arFragment.arSceneView.scene.addChild(anchorNode)
    }

    companion object {
        private val TAG = ArActivity::class.qualifiedName
        private val FOCUS_VIEW_PERCENTAGE = 0.25

        fun newIntent(context: Context): Intent {
            return Intent(context, ArActivity::class.java)
        }
    }

}



