package com.mrklie.yangtao.ar

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.media.ImageReader
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.core.SharedCamera
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import com.mrklie.yangtao.R
import com.quickbirdstudios.yuv2mat.Yuv
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import java.util.*


class ArActivity : AppCompatActivity() {
    private lateinit var arFragment: ArFragment
    private lateinit var arPreviewRaw: ImageView
    private lateinit var arPreviewProcessed: ImageView

    private lateinit var scanButton: FloatingActionButton

    private lateinit var andyRenderable: ModelRenderable
    private lateinit var cameraManager: CameraManager

    private var arSession: Session? = null
    private var sharedCamera: SharedCamera? = null
    private var cameraDevice: CameraDevice? = null;
    private var cameraId: String? = null
    private var cpuImageReader: ImageReader? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.mrklie.yangtao.R.layout.activity_ar)

        arFragment = supportFragmentManager.findFragmentById(R.id.ux_fragment) as ArFragment
        arPreviewRaw = findViewById(R.id.ar_preview_raw)
        arPreviewProcessed = findViewById(R.id.ar_preview_processed)

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

    private fun scanImage() {
        val frame = arSession!!.update()
        val image = frame.acquireCameraImage()

        image.use {
            val width = image.width
            val height = image.height

            val mat_raw = Yuv.rgb(image)

            // Core.flip(mat.t(), mat, 0)
            Core.flip(mat_raw.t(), mat_raw, 1)

            val mat_processed = mat_raw.clone()

            // Do the processing
            Imgproc.cvtColor(mat_processed, mat_processed, Imgproc.COLOR_BGR2GRAY);
            Imgproc.threshold(mat_processed, mat_processed, 120.0, 255.0, Imgproc.THRESH_BINARY);

            // We rotated the image, therefore height and width are swapped
            val bitmap_raw = Bitmap.createBitmap(height, width, Bitmap.Config.ARGB_8888 )
            Utils.matToBitmap(mat_raw, bitmap_raw)

            val bitmap_processed = Bitmap.createBitmap(height, width, Bitmap.Config.ARGB_8888 )
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



