package com.mrklie.yangtao.ar

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode


class ArActivity : AppCompatActivity() {

    private lateinit var arFragment: ArFragment
    private lateinit var andyRenderable: ModelRenderable

    private var arSession: Session? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.mrklie.yangtao.R.layout.activity_ar)
        arFragment = supportFragmentManager.findFragmentById(com.mrklie.yangtao.R.id.ux_fragment) as ArFragment

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

    override fun onResume() {
        super.onResume()

        //Check if ARSession is null. If it is, instantiate it
        if(arSession == null) {
            arSession = Session(this@ArActivity)
            arSession?.setupAutofocus()
        }
    }

    private fun Session.setupAutofocus() {
        //Create the config
        val arConfig = Config(this)

        //Check if the configuration is set to fixed
        if (arConfig.focusMode == Config.FocusMode.FIXED)
            arConfig.focusMode = Config.FocusMode.AUTO

        //Sceneform requires that the ARCore session is configured to the UpdateMode LATEST_CAMERA_IMAGE.
        //This is probably not required for just auto focus. I was updating the camera configuration as well
        arConfig.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE

        //Reconfigure the session
        configure(arConfig)

        //Setup the session with ARSceneView
        arFragment.arSceneView!!.setupSession(this)
    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, ArActivity::class.java)
        }
    }
}



