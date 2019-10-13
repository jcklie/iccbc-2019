package com.mrklie.yangtao.ar

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.DisplayMetrics
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.ar.core.*
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import com.mrklie.yangtao.R
import com.mrklie.yangtao.hanzidetail.HanziDetailActivity
import com.mrklie.yangtao.persistence.AppDatabase
import com.quickbirdstudios.yuv2mat.Yuv
import kotlinx.android.synthetic.main.activity_ar.*
import org.jetbrains.anko.doAsync
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.core.Core.subtract
import org.opencv.core.Point
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc.*
import java.io.File
import java.lang.Exception
import java.util.*
import kotlin.math.max
import kotlin.math.min


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
    private var allowVertical: Boolean = false

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
        allowVertical = SP.getBoolean("allowVertical", false)
    }

    private fun initializeSession() {
        // Register scene update to show focus view once tracking is done
        // arFragment.arSceneView.scene.addOnUpdateListener { frameTime ->
        //    handleFocusView()
        //}

        arSession = Session(this, EnumSet.of(Session.Feature.SHARED_CAMERA))
        val arConfig = Config(arSession)

        // Autofocus
        if (arConfig.focusMode == Config.FocusMode.FIXED) {
            arConfig.focusMode = Config.FocusMode.AUTO
        }
        arConfig.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE

        if (allowVertical) {
            arConfig.planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL
        }

        arSession!!.configure(arConfig)
        arFragment.arSceneView!!.setupSession(arSession)
    }

    private fun handleFocusView() {
        for (plane in arSession!!.getAllTrackables(Plane::class.java)) {
            if (plane.trackingState == TrackingState.TRACKING ) {
                focusView.visibility = View.INVISIBLE
                return
            }
        }

        // If there are no planes to track, then hide the view
        focusView.visibility = View.VISIBLE
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

            saveDebugImage(mat_raw, "1_raw")

            // Cut it to the focus size
            // The aspect ratio of the display (which shows the focus rectangle) and
            // the camera are generally not the same. The image is displayed by scaling the
            // longer side to the same size and then center the image. Therefore, we use the
            // longest side with the known focus percentage to compute how many pixels the
            // focus should be.
            val roi = Rect(Point((height - size) / 2, (width - size) / 2), Size(size, size))
            var mat_processed = Mat(mat_raw, roi)

            saveDebugImage(mat_processed, "2_cropped")

            // Convert to rgb and threshold
            cvtColor(mat_processed, mat_processed, COLOR_BGR2GRAY)
            saveDebugImage(mat_processed, "3_gray")
            threshold(mat_processed, mat_processed, 120.0, 255.0, THRESH_BINARY)
            saveDebugImage(mat_processed, "4_binarized")

            // Erode / dilate to remove noise specks and close areas
            val morph_size = 1.0
            val morphKernel = getStructuringElement( MORPH_RECT, Size( 2*morph_size + 1, 2*morph_size+1 ),  Point( morph_size, morph_size ) )

            morphologyEx(mat_processed, mat_processed, MORPH_OPEN, morphKernel)
            morphologyEx(mat_processed, mat_processed, MORPH_CLOSE, morphKernel)
            saveDebugImage(mat_processed, "5_morphed")

            // Detect contours
            val contours = ArrayList<MatOfPoint>()
            val hierarchy = Mat()

            // We need to invert the image, as Opencv wants white as foreground and black as background
            val mat_inverted = Mat(size.toInt(), size.toInt(), mat_processed.type(), Scalar(255.0,255.0,255.0))
            subtract(mat_inverted, mat_processed, mat_inverted);
            findContours(mat_inverted, contours, hierarchy, RETR_LIST, CHAIN_APPROX_SIMPLE )

            var min_x = size
            var min_y = size
            var max_x = 0.0
            var max_y = 0.0

            // Find the bounding box around all contours
            for (contour in contours) {
                if (contourArea(contour) < 10 ) {
                    continue
                }

                val areaPoints = MatOfPoint2f(*contour.toArray())
                val rect = minAreaRect(areaPoints)

                val vertices = arrayOfNulls<Point>(4)
                rect.points(vertices)

                for (j in vertices.indices){
                    val vertex = vertices[j]!!
                    min_x = min(vertex.x, min_x)
                    min_y = min(vertex.y, min_y)
                    max_x = max(vertex.x, max_x)
                    max_y = max(vertex.y, max_y)
                    // line(mat_processed, vertices[j], vertices[(j+1)%4], Scalar(0.0,255.0,0.0))
                }
            }

            if ( (max_x - min_x > 0) && (max_y - min_y > 0)) {
                val center = Rect(Point(min_x, min_y), Point(max_x, max_y))
                try {
                    mat_processed = Mat(mat_processed, center)
                } catch (e: Exception) {

                }
            }

            val new_width = mat_processed.width()
            val new_height = mat_processed.height()

            // We rotated the image, therefore height and width are swapped
            val bitmap_raw = Bitmap.createBitmap(height, width, Bitmap.Config.ARGB_8888 )
            Utils.matToBitmap(mat_raw, bitmap_raw)

            val bitmap_processed = Bitmap.createBitmap(new_width, new_height, Bitmap.Config.ARGB_8888 )
            Utils.matToBitmap(mat_processed, bitmap_processed)

            // We resize the image again to match the classifiers preferencs
            resize( mat_processed, mat_processed, Size(64.0, 64.0) )
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
                    placeScanDialog(hit.createAnchor(), predictions, trackable.type)
                    // placeObject(hit.createAnchor(), prediction[0].toString() )
                }
            }
        }
    }

    private fun saveDebugImage(mat: Mat, name: String) {
        if (showDebug) {
            val file = File(applicationContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "${name}.jpg")
            Imgcodecs.imwrite(file.absolutePath, mat)
            println("Saved to $file")
        }
    }

    private fun placeScanDialog(anchor: Anchor, predictions: List<String>, planeType: Plane.Type) {
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
                anchorNode.localScale = Vector3(0.2f, 0.2f, 0.2f)

                TransformableNode(arFragment.transformationSystem).apply {
                    setParent(anchorNode)
                    if (planeType == Plane.Type.VERTICAL) {
                        val firstRotation = Quaternion.axisAngle(Vector3(0f, 0f, 1f), -90f)
                        val secondRotation = Quaternion.axisAngle(Vector3(0f, 1f, 0f), -90f)
                        localRotation = Quaternion.multiply(firstRotation, secondRotation)
                    }

                    translationController.isEnabled = false
                    this.renderable = renderable
                }

                // Set listeners
                yesButton.setOnClickListener {
                    placeHanzi(anchor, spinner.selectedItem.toString(), planeType)
                    anchorNode.setParent(null)
                }

                noButton.setOnClickListener {
                    anchorNode.setParent(null)
                }

                // Place the node
                arFragment.arSceneView.scene.addChild(anchorNode)
            }
    }

    private fun placeHanzi(anchor: Anchor, hanzi: String, planeType: Plane.Type) {
        val modelName = "models/hanzi${classifier.labelToIndex(hanzi)}.sfb"
        ModelRenderable.builder()
            .setSource(this, Uri.parse(modelName))
            .setRegistryId(hanzi)
            .build()
            .thenAccept {
                addHanziToScene(anchor, it, planeType)
                addHanziMenuToScene(anchor, hanzi, planeType)
                focusView.visibility = View.INVISIBLE

                doAsync {
                    AppDatabase.getDatabase(applicationContext).hanziDao().markScanned(hanzi)
                }
            }
            .exceptionally {
                Toast.makeText(this, "Could not place model", Toast.LENGTH_SHORT).show()
                return@exceptionally null
            }
    }

    private fun addHanziToScene(anchor: Anchor, model: ModelRenderable, planeType: Plane.Type) {
        val anchorNode = AnchorNode(anchor)
        anchorNode.localScale = Vector3(0.1f, 0.1f, 0.1f)

        TransformableNode(arFragment.transformationSystem).apply {
            setParent(anchorNode)
            localRotation = if (planeType == Plane.Type.VERTICAL) {
                Quaternion.axisAngle(Vector3(0f, 1f, 0f), 90f)
                // val secondRotation = Quaternion.axisAngle(Vector3(0f, 1f, 0f), 0f)
                // Quaternion.multiply(firstRotation, secondRotation)
            } else {
                val firstRotation = Quaternion.axisAngle(Vector3(1f, 0f, 0f), -90f)
                val secondRotation = Quaternion.axisAngle(Vector3(0f, 0f, 1f), 180f)
                Quaternion.multiply(firstRotation, secondRotation)
            }

            translationController.isEnabled = false
            renderable = model
            select()
        }

        arFragment.arSceneView.scene.addChild(anchorNode)
    }

    private fun addHanziMenuToScene(anchor: Anchor, hanzi: String, planeType: Plane.Type) {
        ViewRenderable.builder()
            .setView(this, R.layout.scan_menu)
            .build()
            .thenAccept {viewRenderable ->
                viewRenderable.isShadowReceiver = false

                val view = viewRenderable.view

                val infoButton = view.findViewById<ImageButton>(R.id.scan_menu_info)
                val cancelButton = view.findViewById<ImageButton>(R.id.scan_menu_delete)

                // Create the node
                val pose = if (planeType == Plane.Type.VERTICAL) {
                    Pose.makeTranslation(floatArrayOf(0.15f, 0.0f, 0.0f))
                } else {
                    Pose.makeTranslation(floatArrayOf(0.0f, 0.15f, 0.0f))
                }

                val newAnchor = arSession!!.createAnchor(anchor.pose.compose(pose))
                val anchorNode = AnchorNode(newAnchor)
                anchorNode.localScale = Vector3(0.05f, 0.05f, 0.05f)

                TransformableNode(arFragment.transformationSystem).apply {
                    setParent(anchorNode)
                    if (planeType == Plane.Type.VERTICAL) {
                        val firstRotation = Quaternion.axisAngle(Vector3(0f, 0f, 1f), -90f)
                        val secondRotation = Quaternion.axisAngle(Vector3(0f, 1f, 0f), -90f)
                        localRotation = Quaternion.multiply(firstRotation, secondRotation)
                    }

                    translationController.isEnabled = false
                    renderable = viewRenderable
                }

                // Set listeners
                infoButton.setOnClickListener {
                    startActivity(HanziDetailActivity.newIntent(applicationContext, hanzi))
                    anchorNode.setParent(null)
                    anchor.detach()
                    focusView.visibility = View.VISIBLE
                }

                cancelButton.setOnClickListener {
                    anchorNode.setParent(null)
                    anchor.detach()
                    focusView.visibility = View.VISIBLE
                }

                // Place the node
                arFragment.arSceneView.scene.addChild(anchorNode)
            }
    }

    companion object {
        private val TAG = ArActivity::class.qualifiedName
        private val FOCUS_VIEW_PERCENTAGE = 0.25

        fun newIntent(context: Context): Intent {
            return Intent(context, ArActivity::class.java)
        }
    }

}



