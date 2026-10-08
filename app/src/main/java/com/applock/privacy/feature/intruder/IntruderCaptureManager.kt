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
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager ?: return

        try {
            var frontCameraId: String? = null
            for (id in cameraManager.cameraIdList) {
                val characteristics = cameraManager.getCameraCharacteristics(id)
                val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
                if (facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    frontCameraId = id
                    break
                }
            }

            if (frontCameraId == null) {
                frontCameraId = cameraManager.cameraIdList.firstOrNull() ?: return
            }

            val handlerThread = HandlerThread("IntruderCaptureThread").apply { start() }
            val handler = Handler(handlerThread.looper)

            val imageReader = ImageReader.newInstance(640, 480, ImageFormat.JPEG, 2)

            imageReader.setOnImageAvailableListener({ reader ->
                val image = reader.acquireLatestImage() ?: return@setOnImageAvailableListener
                val buffer = image.planes[0].buffer
                val bytes = ByteArray(buffer.remaining())
                buffer.get(bytes)
                image.close()

                try {
                    val dir = File(context.filesDir, "intruders")
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
                        AppPreferencesDataSource(context).saveIntruderLog(log)
                    }
                } catch (_: Exception) {}

                try {
                    reader.close()
                    handlerThread.quitSafely()
                } catch (_: Exception) {}
            }, handler)

            cameraManager.openCamera(frontCameraId, object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
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

                                        // Close camera after delay to ensure capture completed
                                        handler.postDelayed({
                                            try {
                                                camera.close()
                                            } catch (_: Exception) {}
                                        }, 1500)
                                    } catch (_: Exception) {
                                        try { camera.close() } catch (_: Exception) {}
                                    }
                                }

                                override fun onConfigureFailed(session: CameraCaptureSession) {
                                    try { camera.close() } catch (_: Exception) {}
                                }
                            },
                            handler
                        )
                    } catch (_: Exception) {
                        try { camera.close() } catch (_: Exception) {}
                    }
                }

                override fun onDisconnected(camera: CameraDevice) {
                    try { camera.close() } catch (_: Exception) {}
                }

                override fun onError(camera: CameraDevice, error: Int) {
                    try { camera.close() } catch (_: Exception) {}
                }
            }, handler)
        } catch (_: Exception) {}
    }
}
