package com.lnight.cameraxproject

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.camera.core.CameraSelector
import androidx.camera.video.*
import androidx.camera.view.PreviewView
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.util.Consumer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import android.Manifest
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.text.style.TextAlign
import com.google.accompanist.permissions.isGranted
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.Executor

@RequiresApi(Build.VERSION_CODES.P)
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val permissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
    )

    var recording: Recording? by remember { mutableStateOf(null) }
    val previewView: PreviewView = remember { PreviewView(context) }
    var videoCapture: VideoCapture<Recorder>? by remember { mutableStateOf(null) }
    var recordingStarted by rememberSaveable { mutableStateOf(false) }

    var audioEnabled by rememberSaveable { mutableStateOf(false) }
    var cameraSelector by rememberSaveable {
        mutableStateOf(0)
    }

    LaunchedEffect(true) {
        permissionState.launchMultiplePermissionRequest()
    }

    LaunchedEffect(previewView) {
        videoCapture = context.createVideoCapture(
            lifecycleOwner = lifecycleOwner,
            cameraSelector = if(cameraSelector == 0) CameraSelector.DEFAULT_BACK_CAMERA else CameraSelector.DEFAULT_FRONT_CAMERA,
            previewView = previewView
        )
    }

    when {
        permissionState.permissions.first().status.isGranted -> {

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            AndroidView(
                factory = { previewView },
                modifier = Modifier.fillMaxSize()
            )
            IconButton(
                onClick = {
                    if (!recordingStarted) {
                        videoCapture?.let { videoCapture ->
                            recordingStarted = true
                            val mediaDir = context.externalCacheDirs.firstOrNull()?.let {
                                File(it, context.getString(R.string.app_name)).apply { mkdirs() }
                            }

                            recording = startRecordingVideo(
                                context = context,
                                filenameFormat = "yyyy-MM-dd-HH-mm-ss-SSS",
                                videoCapture = videoCapture,
                                outputDirectory = if (mediaDir != null && mediaDir.exists()) mediaDir else context.filesDir,
                                executor = context.mainExecutor,
                                audioEnabled = audioEnabled
                            ) { event ->
                                if (event is VideoRecordEvent.Finalize) {
                                    val uri = event.outputResults.outputUri
                                    if (uri != Uri.EMPTY) {
                                        val uriEncoded = URLEncoder.encode(
                                            uri.toString(),
                                            StandardCharsets.UTF_8.toString()
                                        )
                                        try {
                                            navController.navigate("${Screen.PlayVideoScreen.route}/$uriEncoded")
                                        } catch (e: IllegalStateException) {
                                            e.printStackTrace()
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        recordingStarted = false
                        recording?.stop()
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
            ) {
                Icon(
                    painter = painterResource(if (recordingStarted) R.drawable.ic_stop else R.drawable.ic_record),
                    contentDescription = if (recordingStarted) "stop record" else "start record",
                    modifier = Modifier.size(100.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
            if (!recordingStarted) {
                IconButton(
                    onClick = {
                        audioEnabled = !audioEnabled
                    },
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(bottom = 32.dp, start = 32.dp)
                ) {
                    Icon(
                        painter = painterResource(if (audioEnabled) R.drawable.ic_mic_on else R.drawable.ic_mic_off),
                        contentDescription = if (audioEnabled) "turn on microphone" else "turn off microphone",
                        modifier = Modifier.size(84.dp),
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
            if (!recordingStarted) {
                IconButton(
                    onClick = {
                        cameraSelector =
                            if (cameraSelector == 1) 0
                            else 1
                        lifecycleOwner.lifecycleScope.launch {
                            videoCapture = context.createVideoCapture(
                                lifecycleOwner = lifecycleOwner,
                                cameraSelector = if(cameraSelector == 0) CameraSelector.DEFAULT_BACK_CAMERA else CameraSelector.DEFAULT_FRONT_CAMERA,
                                previewView = previewView
                            )
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 32.dp, end = 16.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_switch_camera),
                        contentDescription = "switch camera",
                        modifier = Modifier.size(90.dp),
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
        }
        permissionState.shouldShowRationale || !permissionState.permissions.first().status.isGranted -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "You have to grant all permissions before using the app",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = { context.openAppDetails() },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(text = "Give permission")
                }
            }
        }
    }
}

@SuppressLint("MissingPermission")
fun startRecordingVideo(
    context: Context,
    filenameFormat: String,
    videoCapture: VideoCapture<Recorder>,
    outputDirectory: File,
    executor: Executor,
    audioEnabled: Boolean,
    consumer: Consumer<VideoRecordEvent>
): Recording {
    val videoFile = File(
        outputDirectory,
        SimpleDateFormat(filenameFormat, Locale.US).format(System.currentTimeMillis()) + ".mp4"
    )

    val outputOptions = FileOutputOptions.Builder(videoFile).build()

    return videoCapture.output
        .prepareRecording(context, outputOptions)
        .apply { if (audioEnabled) withAudioEnabled() }
        .start(executor, consumer)
}