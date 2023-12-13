package com.waffle.backgroundselector

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.waffle.backgroundselector.ui.theme.BackgroundSelectorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var currentScreen by remember { mutableStateOf(Screen.Gallery) }
            var imageList by remember { mutableStateOf(listOf<Uri>()) }
            var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

            when (currentScreen) {
                Screen.Gallery -> BackgroundSelector(onBackClicked = {}, onSearchClicked = {}, imageList, refreshGallery = {
                    // Add logic to refresh the image list
                    imageList
                })
                Screen.Crop -> selectedImageUri?.let { uri ->
                    CropImageScreen(imageUri = uri, onImageCropped = { croppedUri ->
                        // Add croppedUri to the image list and switch back to gallery
                        imageList = imageList + listOf(croppedUri)
                        currentScreen = Screen.Gallery
                    })
                }
            }
        }
    }
}


enum class Screen {
    Gallery, Crop
}