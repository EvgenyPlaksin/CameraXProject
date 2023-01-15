package com.lnight.cameraxproject

sealed class Screen(val route: String) {
    object CameraScreen: Screen("camera_screen")
    object PlayVideoScreen: Screen("play_video_screen")
}