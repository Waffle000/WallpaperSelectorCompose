@file:OptIn(ExperimentalMaterial3Api::class)

package com.waffle.backgroundselector

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BackgroundSelector(onBackClicked: () -> Unit, onSearchClicked: () -> Unit) {
    val colors = listOf(
        Color.Red, Color.Green, Color.Blue, Color.Yellow,
        Color.Magenta, Color.Cyan, Color.Gray, Color.LightGray, Color.Black
    )
    var selectedColor by remember { mutableStateOf(Color.White) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Wallpaper Selector", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { onBackClicked() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
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
        Column (
            modifier = Modifier.padding(paddingValues = PaddingValues)
        ) {
            TextMenu(text = "Select from Gallery")
            TextMenu(text = "Set a Color")
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(8.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                items(colors) { color ->
                    ColorItem(color, selectedColor) { selected ->
                        selectedColor = selected
                    }
                }
            }

            // Bottom area with text description for the selected color
            Box(modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "You have selected the color: ${selectedColor.toHex()}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun TextMenu(text: String) {
    Row(
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp)
    ) {
        Icon(imageVector = Icons.Default.List, contentDescription = "Icon On Left Text", tint = Color.LightGray)
        Text(text = text, Modifier.padding(start = 16.dp), fontWeight = FontWeight.Normal)
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
        Box(modifier = Modifier.fillMaxSize()) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.Center)
                        .background(Color.Black),
                    tint = Color.White,
                )
            }
        }
    }
}

fun Color.toHex(): String = "#${this.value.toULong().toString(16).substring(2).uppercase()}"

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    BackgroundSelector(
        onBackClicked = { /* Handle back press */ },
        onSearchClicked = { /* Handle search action */ }
    )
}