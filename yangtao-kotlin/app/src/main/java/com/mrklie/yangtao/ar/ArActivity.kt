package com.mrklie.yangtao.ar

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.ar.core.*
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import com.mrklie.yangtao.R
import com.quickbirdstudios.yuv2mat.Yuv
import com.quickbirdstudios.yuv2mat.YuvImage
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.core.Point
import org.opencv.imgproc.Imgproc
import org.opencv.imgproc.Imgproc.*
import java.util.*
import kotlin.math.min


class ArActivity : AppCompatActivity() {
    private lateinit var arFragment: ArFragment
    private lateinit var arPreviewRaw: ImageView
    private lateinit var arPreviewProcessed: ImageView
    private lateinit var focusView: View

    private lateinit var scanButton: FloatingActionButton

    private lateinit var andyRenderable: ModelRenderable
    private lateinit var cameraManager: CameraManager

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

        ModelRenderable.builder()
            .setSource(this, Uri.parse("发.sfb"))
            .build()
            .thenAccept({ renderable -> andyRenderable = renderable })
            .exceptionally {
                val toast =
                    Toast.makeText(this, "Unable to load andy renderable", Toast.LENGTH_LONG)
                toast.setGravity(Gravity.CENTER, 0, 0)
                toast.show()
                null
            }

        createTapListener()

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

        // Shared Camera
        // createCameraCaptureSession()

        resizeFocusView()

    }

    override fun onResume() {
        super.onResume()

        if (!OpenCVLoader.initDebug()) {
            Toast.makeText(this, "Could not load OpenCV", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createTapListener() {
        arFragment.setOnTapArPlaneListener { hitResult, _, _ ->
            // Create the Anchor.
            val anchor = hitResult.createAnchor()
            val anchorNode = AnchorNode(anchor)
            anchorNode.setParent(arFragment.arSceneView.scene)

            // Create the transformable andy and add it to the anchor.
            TransformableNode(arFragment.transformationSystem).apply {
                setParent(anchorNode)
                renderable = andyRenderable

                localRotation = Quaternion.axisAngle(Vector3(1f, 0f, 0f), 90f)
                translationController.isEnabled = false
                select()
            }
        }
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
        val displayMetrics = DisplayMetrics()
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics)
        val displayWidth = displayMetrics.widthPixels
        val displayHeight = displayMetrics.heightPixels
        val displayLen = min(displayWidth, displayHeight)

        val params = focusView.layoutParams
        params.width = (displayLen * 0.8).toInt()
        params.height = (displayLen * 0.8).toInt()

        focusView.visibility = View.VISIBLE
    }

    private fun scanImage() {
        val frame = arSession!!.update()
        val image = frame.acquireCameraImage()

        // arSession!!.getSupportedCameraConfigs()[2].imageSize

        image.use {
            val width = image.width
            val height = image.height
            val longest_side = min(width, height).toDouble()
            val border = longest_side * 0.0
            val size = longest_side - border
            val roi = Rect(Point(height - longest_side + border, width - longest_side + border), Size(size, size))

            val mat_raw = Yuv.rgb(image)

            // Core.flip(mat.t(), mat, 0)
            Core.flip(mat_raw.t(), mat_raw, 1)

            val mat_processed = Mat(mat_raw, roi)

            // Do the processing
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

            // Display the previews
            arPreviewRaw.setImageBitmap(bitmap_raw)
            arPreviewRaw.bringToFront()

            arPreviewProcessed.setImageBitmap(bitmap_processed)
            arPreviewProcessed.bringToFront()
        }
    }

    companion object {
        private val TAG = ArActivity::class.qualifiedName

        fun newIntent(context: Context): Intent {
            return Intent(context, ArActivity::class.java)
        }
    }

}



