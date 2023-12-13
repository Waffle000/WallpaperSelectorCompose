package com.waffle.backgroundselector

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import com.yalantis.ucrop.UCrop
import java.io.File

@Composable
fun CropImageScreen(imageUri: Uri, onImageCropped: (Uri) -> Unit) {
    val context = LocalContext.current
    var resultUri by remember { mutableStateOf<Uri?>(null) }
    val imageUris = remember { mutableStateListOf<Uri>() }
    imageUris.addAll(loadImageUris(context))

    val cropActivityResultLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val resultUri = UCrop.getOutput(result.data!!)
            resultUri?.let { onImageCropped(it) }
            val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            resultUri?.let { context.contentResolver.takePersistableUriPermission(it, takeFlags) }
            resultUri?.let { imageUris.add(it) }
            saveImageUris(context, imageUris)
        }
    }
    val destinationUri = Uri.fromFile(File(context.cacheDir, "cropped.jpg"))
    val options = UCrop.Options().apply {
        setCompressionQuality(80)
    }

    val ratio = getScreenDimensions(context)
    val uCropIntent = UCrop.of(imageUri, destinationUri)
        .withAspectRatio(ratio.first, ratio.second)
        .withOptions(options)
        .getIntent(context)
    cropActivityResultLauncher.launch(uCropIntent)

}

fun getScreenDimensions(context: Context): Pair<Float, Float> {
    val metrics = DisplayMetrics()
    val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    windowManager.defaultDisplay.getMetrics(metrics)

    val width = metrics.widthPixels.toFloat()
    val height = metrics.heightPixels.toFloat()

    return Pair(width, height)
}
