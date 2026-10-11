package com.applock.privacy.feature.intruder

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import android.util.Size
import androidx.core.content.ContextCompat
import com.applock.privacy.data.local.AppPreferencesDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object IntruderCaptureManager {

    @SuppressLint("MissingPermission")
    fun captureSilently(
        context: Context,
        packageName: String,
        appName: String,
        failedAttempts: Int
    ) {
        val appContext = context.applicationContext
        if (ContextCompat.checkSelfPermission(appContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        val cameraManager = appContext.getSystemService(Context.CAMERA_SERVICE) as? CameraManager ?: return

        try {
            var frontCameraId: String? = null
            var frontCharacteristics: CameraCharacteristics? = null

            for (id in cameraManager.cameraIdList) {
                val characteristics = cameraManager.getCameraCharacteristics(id)
                val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
                if (facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    frontCameraId = id
                    frontCharacteristics = characteristics
                    break
                }
            }

            if (frontCameraId == null) {
                frontCameraId = cameraManager.cameraIdList.firstOrNull() ?: return
                frontCharacteristics = cameraManager.getCameraCharacteristics(frontCameraId)
            }

            // Determine valid JPEG resolution from device's supported stream configuration map
            val map = frontCharacteristics?.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            val sizes = map?.getOutputSizes(ImageFormat.JPEG) ?: emptyArray()
            val chosenSize: Size = sizes.firstOrNull { it.width in 480..1280 }
                ?: sizes.firstOrNull()
                ?: Size(640, 480)

            val handlerThread = HandlerThread("IntruderCaptureThread").apply { start() }
            val handler = Handler(handlerThread.looper)
            val imageReader = ImageReader.newInstance(chosenSize.width, chosenSize.height, ImageFormat.JPEG, 2)

            var isCleanedUp = false
            var activeCamera: CameraDevice? = null

            fun cleanup() {
                if (isCleanedUp) return
                isCleanedUp = true
                try { activeCamera?.close() } catch (_: Throwable) {}
                try { imageReader.close() } catch (_: Throwable) {}
                try { handlerThread.quitSafely() } catch (_: Throwable) {}
            }

            // Safety timeout: Guarantee camera and background thread close within 3 seconds
            handler.postDelayed({ cleanup() }, 3000L)

            imageReader.setOnImageAvailableListener({ reader ->
                try {
                    val image = reader.acquireLatestImage() ?: return@setOnImageAvailableListener
                    val buffer = image.planes[0].buffer
                    val bytes = ByteArray(buffer.remaining())
                    buffer.get(bytes)
                    image.close()

                    val dir = File(appContext.filesDir, "intruders")
                    if (!dir.exists()) dir.mkdirs()

                    val file = File(dir, "intruder_${System.currentTimeMillis()}.jpg")
                    FileOutputStream(file).use { it.write(bytes) }

                    // Save log record
                    val log = IntruderLog(
                        id = UUID.randomUUID().toString(),
                        timestamp = System.currentTimeMillis(),
                        packageName = packageName,
                        appName = appName,
                        imagePath = file.absolutePath,
                        failedAttempts = failedAttempts
                    )

                    CoroutineScope(Dispatchers.IO).launch {
                        AppPreferencesDataSource(appContext).saveIntruderLog(log)
                    }
                } catch (_: Throwable) {}

                // Post cleanup slightly delayed so camera finishes cleanly
                handler.postDelayed({ cleanup() }, 200L)
            }, handler)

            cameraManager.openCamera(frontCameraId, object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    activeCamera = camera
                    try {
                        val surface = imageReader.surface
                        camera.createCaptureSession(
                            listOf(surface),
                            object : CameraCaptureSession.StateCallback() {
                                override fun onConfigured(session: CameraCaptureSession) {
                                    try {
                                        val builder = camera.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE).apply {
                                            addTarget(surface)
                                            set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
                                        }
                                        session.capture(builder.build(), null, handler)
                                    } catch (_: Throwable) {
                                        cleanup()
                                    }
                                }

                                override fun onConfigureFailed(session: CameraCaptureSession) {
                                    cleanup()
                                }
                            },
                            handler
                        )
                    } catch (_: Throwable) {
                        cleanup()
                    }
                }

                override fun onDisconnected(camera: CameraDevice) {
                    cleanup()
                }

                override fun onError(camera: CameraDevice, error: Int) {
                    cleanup()
                }
            }, handler)
        } catch (_: Throwable) {}
    }
}
