package com.fhylabs.ipcamera

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Power
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun CameraScreen(
    previewView: PreviewView,
    serverPort: Int,
    serverRunning: Boolean,
    onToggleServer: () -> Unit,
    onSelectResolution: (width: Int, height: Int) -> Unit,
    onSelectAspectRatio: (ratioFloat: Float) -> Unit
) {
    val context = LocalContext.current
    val ipAddress = getLocalIpAddress()
    val url = "http://$ipAddress:$serverPort/"
    var serverState by remember { mutableStateOf(serverRunning) }

    var selectedRatio by remember { mutableStateOf("1:1") }
    var selectedResolution by remember { mutableStateOf("360p") }

    val ratioOptions = listOf("1:1", "2:1", "4:3", "16:9")
    val resolutionOptions = listOf(
        "360p" to Pair(480, 480),
        "480p" to Pair(640, 480),
        "720p" to Pair(1280, 720),
        "1080p" to Pair(1920, 1080)
    )

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color(0x66000000))
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                IconButton(
                    onClick = {
                        onToggleServer()
                        serverState = !serverState
                    },
                    modifier = Modifier.size(44.dp)
                        .background(
                            if (serverState) Color(0xFFE53935) else Color(0xFF43A047),
                            shape = androidx.compose.foundation.shape.CircleShape
                        )
                ) { Icon(Icons.Filled.Power, contentDescription = null, tint = Color.White) }

                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("URL", url))
                        Toast.makeText(context, "URL copied", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(44.dp)
                        .background(Color(0xFF1E88E5), shape = androidx.compose.foundation.shape.CircleShape)
                ) { Icon(Icons.Filled.ContentCopy, contentDescription = null, tint = Color.White) }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ratioOptions.forEach { ratio ->
                    Row(
                        Modifier.selectable(
                            selected = (selectedRatio == ratio),
                            onClick = {
                                selectedRatio = ratio
                                val floatRatio = when(ratio){
                                    "1:1" -> 1f
                                    "2:1" -> 2f
                                    "4:3" -> 4f/3f
                                    "16:9" -> 16f/9f
                                    else -> 1f
                                }
                                onSelectAspectRatio(floatRatio)
                            },
                            role = Role.RadioButton
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = selectedRatio == ratio, onClick = null, colors = RadioButtonDefaults.colors(selectedColor = Color.Cyan))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(ratio, color = Color.White, fontSize = 12.sp)
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                resolutionOptions.forEach { (label, size) ->
                    Row(
                        Modifier.selectable(
                            selected = (selectedResolution == label),
                            onClick = {
                                selectedResolution = label
                                onSelectResolution(size.first, size.second)
                            },
                            role = Role.RadioButton
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = selectedResolution == label, onClick = null, colors = RadioButtonDefaults.colors(selectedColor = Color.Cyan))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(label, color = Color.White, fontSize = 12.sp)
                    }
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Top) {
                Text(
                    text = "Server Status: ${if (serverState) "Running" else "Stopped"}",
                    color = if (serverState) Color.Green else Color.Red,
                    fontSize = 12.sp
                )
                SelectionContainer { Text(text = "URL: $url", color = Color.White, fontSize = 12.sp) }
            }
        }
    }
}
