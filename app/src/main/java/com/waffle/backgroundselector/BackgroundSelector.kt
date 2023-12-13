@file:OptIn(ExperimentalMaterial3Api::class)

package com.waffle.backgroundselector

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.yalantis.ucrop.UCrop
import java.io.File
import java.security.Permissions

@Composable
fun BackgroundSelector(onBackClicked: () -> Unit, onSearchClicked: () -> Unit, imageList: List<Uri>,
                       refreshGallery: () -> List<Uri>) {

    val context = LocalContext.current
    var selectedColor by remember { mutableStateOf<Uri?>(null) }
    selectedColor = loadImageUri(context)
    val imageUris = remember { mutableStateListOf<Uri>() }
    imageUris.addAll(loadImageUris(context))

    val cropActivityResultLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val resultUri = result.data?.let { UCrop.getOutput(it) }
            resultUri?.let { imageUris.add(it) }
            saveImageUris(context, imageUris)
        }
    }
    val launcher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            uri?.let {
                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                it.let { context.contentResolver.takePersistableUriPermission(it, takeFlags) }
                val destinationUri = Uri.fromFile(File(context.cacheDir, "Image_${System.currentTimeMillis()}"))
                val options = UCrop.Options().apply {
                    setCompressionQuality(80)
                }
                val ratio = getScreenDimensions(context)
                val uCropIntent = UCrop.of(it, destinationUri)
                    .withAspectRatio(ratio.first, ratio.second)
                    .withOptions(options)
                    .getIntent(context)
                cropActivityResultLauncher.launch(uCropIntent)
            }
        }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            launcher.launch(arrayOf("image/*"))

        } else {
            Log.d("ExampleScreen","PERMISSION DENIED")
        }
    }

    var images by remember { mutableStateOf(imageList) }

    LaunchedEffect(Unit) {
        images = refreshGallery()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Wallpaper Selector",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onBackClicked() }) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { onSearchClicked() }) {
                        Icon(Icons.Filled.Search, contentDescription = "Search", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(Color.Blue)
            )
        }
    ) { PaddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            selectedColor?.let { getBitmapFromUri(it, context) }
                ?.let { Image(bitmap = it, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop) }
            Column(
                modifier = Modifier.padding(paddingValues = PaddingValues)
            ) {

                TextMenu(text = "Select from Gallery", onClickMenu = {
                    permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                })
                TextMenu(text = "Set a Color", onClickMenu = { launcher.launch(arrayOf("image/*")) })
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(8.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    items(imageUris) { uri ->
                        ImageItem(image = uri, selectedImage = selectedColor, context = context){ selected ->
                            selectedColor = selected
                            saveImageUri(context, selected)
                        }
                    }
                }
            }
        }

    }
}


@Composable
fun TextMenu(text: String, onClickMenu: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp)
            .clickable(onClick = { onClickMenu() })
    ) {
        Icon(
            imageVector = Icons.Default.List,
            contentDescription = "Icon On Left Text",
            tint = Color.Black
        )
        Text(text = text, Modifier.padding(start = 16.dp), fontWeight = FontWeight.Normal)
    }
}

@Composable
fun ImageItem(image: Uri, selectedImage: Uri?, context: Context, onImageSelected: (Uri) -> Unit) {
    val isSelected = selectedImage == image
    Card(
        modifier = Modifier
            .padding(4.dp)
            .size(140.dp, 180.dp)
            .clickable { onImageSelected(image) },
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                bitmap = getBitmapFromUri(image, context),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            if (isSelected) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "contentDescription",
                        tint = Color.White,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}
@Composable
fun ColorItem(color: Color, selectedColor: Color, onColorSelected: (Color) -> Unit) {
    val isSelected = selectedColor == color
    Card(
        modifier = Modifier
            .padding(4.dp)
            .size(140.dp)
            .clickable { onColorSelected(color) },
        colors = CardDefaults.cardColors(
            containerColor = color
        ),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "contentDescription",
                        tint = Color.White,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

fun Color.toHex(): String = "#${this.value.toULong().toString(16).substring(2).uppercase()}"

fun saveImageUris(context: Context, imageUris: List<Uri>) {
    val sharedPreferences = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()
    val gson = Gson()
    val stringUris = imageUris.map { it.toString() }
    val json = gson.toJson(stringUris)
    editor.putString("imageUris", json)
    editor.apply()
}

fun loadImageUris(context: Context): List<Uri> {
    val sharedPreferences = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
    val gson = Gson()
    val json = sharedPreferences.getString("imageUris", null)
    val type = object : TypeToken<List<String>>() {}.type
    val stringUris: List<String> = gson.fromJson(json, type) ?: emptyList()
    return stringUris.map { Uri.parse(it) }
}

fun saveImageUri(context: Context, imageUri: Uri) {
    val sharedPreferences = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()
    val gson = Gson()
    val stringUri = imageUri.toString()
    editor.putString("imageUri", stringUri)
    editor.apply()
}

fun loadImageUri(context: Context): Uri? {
    val sharedPreferences = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
    val stringUri = sharedPreferences.getString("imageUri", null)
    return stringUri?.let { Uri.parse(it) }
}

private fun getBitmapFromUri(uri: Uri, context: Context): ImageBitmap {
    val parcelFileDescriptor = context.contentResolver.openFileDescriptor(uri, "r")
    val fileDescriptor = parcelFileDescriptor?.fileDescriptor
    val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
    parcelFileDescriptor?.close()
    return image.asImageBitmap()
}