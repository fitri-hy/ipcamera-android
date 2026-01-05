package com.fhylabs.ipcamera

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Size
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.fhylabs.ipcamera.ui.theme.IpcameraTheme
import java.util.concurrent.atomic.AtomicReference

class MainActivity : ComponentActivity() {

    private lateinit var previewView: PreviewView
    private var server: MJPEGServer? = null
    private val serverPort = 8080
    private val latestFrame = AtomicReference<Bitmap?>()

    private var selectedResolution = Size(480, 480)
    private var selectedAspectRatio: Float = 1f

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                startCamera()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        enableEdgeToEdge()

        previewView = PreviewView(this)

        requestCameraPermissionIfNeeded()

        setContent {
            IpcameraTheme {
                CameraScreen(
                    previewView = previewView,
                    serverPort = serverPort,
                    serverRunning = server != null,
                    onToggleServer = {
                        if (server == null) {
                            server = MJPEGServer(serverPort, latestFrame)
                            server?.startServer()
                        } else {
                            server?.stopServer()
                            server = null
                        }
                    },
                    onSelectResolution = { width, height ->
                        selectedResolution = Size(width, height)
                        restartCamera()
                    },
                    onSelectAspectRatio = { ratio ->
                        selectedAspectRatio = ratio
                        restartCamera()
                    }
                )
            }
        }
    }

    private fun requestCameraPermissionIfNeeded() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            cameraProvider.unbindAll()

            val preview = Preview.Builder()
                .setTargetResolution(selectedResolution)
                .build()
                .also { it.setSurfaceProvider(previewView.surfaceProvider) }

            val analyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setTargetResolution(selectedResolution)
                .build()
                .also { analysis ->
                    analysis.setAnalyzer(ContextCompat.getMainExecutor(this)) { imageProxy ->
                        val bitmap = imageProxy.toBitmap()?.cropToRatio(selectedAspectRatio)
                        latestFrame.set(bitmap)
                        imageProxy.close()
                    }
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, analyzer)

        }, ContextCompat.getMainExecutor(this))
    }

    private fun restartCamera() {
        startCamera()
    }

    override fun onDestroy() {
        super.onDestroy()
        server?.stopServer()
    }
}

fun Bitmap.cropToRatio(ratio: Float): Bitmap {
    val w = width
    val h = height
    val currentRatio = w.toFloat() / h.toFloat()

    return if (currentRatio > ratio) {
        val newW = (h * ratio).toInt()
        val xOffset = (w - newW) / 2
        Bitmap.createBitmap(this, xOffset, 0, newW, h)
    } else {
        val newH = (w / ratio).toInt()
        val yOffset = (h - newH) / 2
        Bitmap.createBitmap(this, 0, yOffset, w, newH)
    }
}

fun getLocalIpAddress(): String {
    return try {
        val interfaces = java.net.NetworkInterface.getNetworkInterfaces()
        for (intf in interfaces) {
            val addrs = intf.inetAddresses
            for (addr in addrs) {
                if (!addr.isLoopbackAddress && addr.hostAddress.indexOf(':') < 0) {
                    return addr.hostAddress
                }
            }
        }
        "127.0.0.1"
    } catch (ex: Exception) {
        ex.printStackTrace()
        "127.0.0.1"
    }
}
