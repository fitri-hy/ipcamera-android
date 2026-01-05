package com.fhylabs.ipcamera

import android.graphics.Bitmap
import fi.iki.elonen.NanoHTTPD
import java.io.InputStream
import java.io.ByteArrayOutputStream
import java.util.concurrent.atomic.AtomicReference

class MJPEGServer(
    private val port: Int,
    private val latestFrame: AtomicReference<Bitmap?>
) : NanoHTTPD(port) {

    private val boundary = "MJPEGBOUNDARY"

    fun startServer() {
        start(SOCKET_READ_TIMEOUT, false)
        println("MJPEG Server started on port $port")
    }

    override fun serve(session: IHTTPSession?): Response {
        val uri = session?.uri ?: "/"

        return when (uri) {
            "/" -> {
                val html = """
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <title>IP Camera</title>
                        <style>
                            html, body {
                                margin: 0;
                                padding: 0;
                                background-color: black;
                                height: 100%;
                                width: 100%;
                                display: flex;
                                justify-content: center;
                                align-items: center;
                            }
                            #camera {
                                max-width: 100%;
                                max-height: 100%;
                                object-fit: contain;
                                background-color: black;
                            }
                        </style>
                    </head>
                    <body>
                        <img id="camera" src="/stream" />
                    </body>
                    </html>
                """.trimIndent()
                newFixedLengthResponse(Response.Status.OK, "text/html", html)
            }

            "/stream" -> {
                val response = newChunkedResponse(
                    Response.Status.OK,
                    "multipart/x-mixed-replace; boundary=$boundary",
                    MJPEGInputStream(latestFrame)
                )
                response.addHeader("Connection", "close")
                response.addHeader("Cache-Control", "no-cache")
                response.addHeader("Pragma", "no-cache")
                response
            }

            else -> newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "404 Not Found")
        }
    }

    fun stopServer() {
        stop()
        println("MJPEG Server stopped")
    }
}

class MJPEGInputStream(private val latestFrame: AtomicReference<Bitmap?>) : InputStream() {
    private var buffer: ByteArray = ByteArray(0)
    private var pos = 0

    override fun read(): Int {
        if (pos >= buffer.size) {
            generateNextFrame()
            pos = 0
        }
        return if (buffer.isNotEmpty()) buffer[pos++].toInt() and 0xFF else -1
    }

    private fun generateNextFrame() {
        val bitmap = latestFrame.get()
        if (bitmap == null) {
            buffer = ByteArray(0)
            return
        }

        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
        val jpeg = stream.toByteArray()
        val header = "--MJPEGBOUNDARY\r\nContent-Type: image/jpeg\r\nContent-Length: ${jpeg.size}\r\n\r\n".toByteArray()
        buffer = header + jpeg + "\r\n".toByteArray()
    }
}
