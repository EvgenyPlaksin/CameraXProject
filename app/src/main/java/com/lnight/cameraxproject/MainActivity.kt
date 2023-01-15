package com.lnight.cameraxproject

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.lnight.cameraxproject.ui.theme.CameraXProjectTheme

class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CameraXProjectTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = Screen.CameraScreen.route) {
                        composable(Screen.CameraScreen.route) {
                            CameraScreen(
                                navController = navController
                            )
                        }
                        composable(Screen.PlayVideoScreen.route + "/{videoUri}",  arguments = listOf(navArgument(name = "videoUri") {
                            type = NavType.StringType
                        })) {
                            val uri = it.arguments?.getString("videoUri")
                            PlayVideoScreen(
                                navController = navController,
                                uri = uri ?: ""
                            )
                        }
                    }
                }
            }
        }
    }
}