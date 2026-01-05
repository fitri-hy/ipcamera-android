<p align="center">
  <img src="logo.png" alt="IP Camera Logo" width="120"/>
</p>

# IP Camera Android App

A lightweight **IP Camera app** built with **Jetpack Compose** and **CameraX**, streaming live video over MJPEG to any web browser or compatible viewer.

## Features

* 📹 **Live Camera Preview** (Back Camera)
* 🌐 **MJPEG Streaming** on `http://<IP>:8080/`
* 🖥️ **Full-screen browser view** with responsive ratio (1:1, 16:9, 4:3, 2:1)
* 🎨 **Customizable UI**: Start button green, Copy URL button blue
* 🔒 **Automatic camera permission handling**
* 🔄 **Dynamic resolution & aspect ratio selection**
* ⚡ **Screen always on** while app is active

## Installation

1. Clone the repository:

   ```bash
   git clone https://github.com/fitri-hy/ipcamera-android.git
   ```
2. Open in **Android Studio** (Arctic Fox or newer).
3. Build and run on an Android device with a camera.
4. Grant camera permission when prompted.

---

## Usage

1. Launch the app → Camera preview starts automatically.
2. Tap **Power** button to start/stop MJPEG server.
3. Tap **Copy URL** to copy the MJPEG URL:

   ```
   http://<device-ip>:8080/
   ```
4. Open the URL in any browser → Tab title shows **“IP Camera”**, background is black, and video fits selected aspect ratio.
5. Select **resolution** and **aspect ratio** in-app for custom streaming.

## Requirements

* Android 7.0 (API 24) or higher
* Camera permission
* Local network access for streaming

## Libraries & Tools

* [Jetpack Compose](https://developer.android.com/jetpack/compose)
* [CameraX](https://developer.android.com/training/camerax)
* [NanoHTTPD](https://github.com/NanoHttpd/nanohttpd) for MJPEG streaming
