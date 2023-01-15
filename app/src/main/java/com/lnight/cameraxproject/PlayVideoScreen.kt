package com.lnight.cameraxproject

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
fun PlayVideoScreen(
    uri: String,
    navController: NavController
) {
    val context = LocalContext.current

    var lifecycle by remember {
        mutableStateOf(Lifecycle.Event.ON_CREATE)
    }
    var onNavigateUp by remember {
        mutableStateOf(false)
    }
    var orientation by remember { mutableStateOf(Configuration.ORIENTATION_PORTRAIT) }

    val player = remember(context) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(uri))
            prepare()
        }
    }

    val configuration = LocalConfiguration.current
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(key1 = lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            lifecycle = event
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(configuration) {
        snapshotFlow { configuration.orientation }
            .collect { orientation = it }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        IconButton(
            onClick = {
                navController.navigateUp()
                onNavigateUp = true
            },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                modifier = Modifier.size(30.dp),
                tint = MaterialTheme.colorScheme.secondary
            )
        }
        AndroidView(
            factory = { context ->
                PlayerView(context).also {
                    it.player = player
                    it.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
                    it.setFullscreenButtonClickListener { isFullScreen ->
                        with(context) {
                            if (isFullScreen) {
                                hideSystemUi()
                            } else {
                                showSystemUi()
                            }
                        }
                    }
                }
            },
            update = {
                when (lifecycle) {
                    Lifecycle.Event.ON_PAUSE -> {
                        it.onPause()
                        it.player?.pause()
                    }
                    Lifecycle.Event.ON_RESUME -> {
                        it.onResume()
                    }
                    else -> Unit
                }
                if (onNavigateUp) {
                    it.player?.pause()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .aspectRatio(if(orientation == Configuration.ORIENTATION_PORTRAIT) 16 / 9f else 17 / 7.8f)
        )
        BackHandler {
            onNavigateUp = true
            navController.navigateUp()
        }

    }
}