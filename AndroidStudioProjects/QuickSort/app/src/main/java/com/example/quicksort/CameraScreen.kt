package com.example.quicksort

import android.Manifest
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(
    onBackClick: () -> Unit,
    onImageCaptured: (Uri) -> Unit,
    aiViewModel: AiViewModel
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    // 갤러리 선택
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onImageCaptured(it) }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 상단 앱바
        TopAppBar(
            title = { Text("사진 촬영") },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF4CAF50),
                titleContentColor = Color.White,
                navigationIconContentColor = Color.White
            )
        )

        if (hasCameraPermission) {
            CameraPreview(
                onImageCaptured = onImageCaptured,
                onGalleryClick = { galleryLauncher.launch("image/*") }
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("카메라 권한이 필요합니다")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { launcher.launch(Manifest.permission.CAMERA) }) {
                        Text("권한 요청")
                    }
                }
            }
        }
    }
}

@Composable
fun CameraPreview(
    onImageCaptured: (Uri) -> Unit,
    onGalleryClick: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val previewView = remember { PreviewView(context) }
    val imageCapture = remember { ImageCapture.Builder().build() }
    val cameraSelector = remember { CameraSelector.DEFAULT_BACK_CAMERA }

    LaunchedEffect(previewView) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(context))
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 카메라 프리뷰
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        // 하단 컨트롤
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 갤러리 버튼
            IconButton(
                onClick = onGalleryClick,
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = "갤러리",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            // 촬영 버튼
            FloatingActionButton(
                onClick = {
                    captureImage(context, imageCapture, onImageCaptured)
                },
                shape = CircleShape,
                containerColor = Color.White,
                modifier = Modifier.size(80.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Camera,
                    contentDescription = "촬영",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(40.dp)
                )
            }

            // 오른쪽 여백 (대칭을 위해)
            Spacer(modifier = Modifier.size(64.dp))
        }
    }
}

private fun captureImage(
    context: Context,
    imageCapture: ImageCapture,
    onImageCaptured: (Uri) -> Unit
) {
    val outputDirectory = context.getExternalFilesDir(null)
    val photoFile = File(
        outputDirectory,
        SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
            .format(System.currentTimeMillis()) + ".jpg"
    )

    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                val savedUri = Uri.fromFile(photoFile)
                onImageCaptured(savedUri)
                Toast.makeText(context, "사진이 촬영되었습니다", Toast.LENGTH_SHORT).show()
            }

            override fun onError(exception: ImageCaptureException) {
                Toast.makeText(context, "촬영 실패: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        }
    )
}
