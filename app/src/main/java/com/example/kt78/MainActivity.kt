package com.example.kt78

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                ImageDownloaderApp()
            }
        }
    }

    @Composable
    fun ImageDownloaderApp() {
        var imageUrl by remember { mutableStateOf(TextFieldValue()) }
        var bitmap by remember { mutableStateOf<Bitmap?>(null) }
        val context = LocalContext.current

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // URL Input
            OutlinedTextField(
                value = imageUrl,
                onValueChange = { imageUrl = it },
                label = { Text("Введите URL изображения") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(9.dp))

            // Download Button
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Button(
                    onClick = {
                        lifecycleScope.launch {
                            bitmap = downloadAndSaveImage(imageUrl.text, context)
                            if (bitmap == null) {
                                Toast.makeText(context, "Ошибка загрузки изображения", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Изображение загружено и сохранено", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Загрузить изображение")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Image Display
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap!!.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text(
                            text = "Здесь будет отображено изображение",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dynamic List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(10) { index ->
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "Элемент списка $index",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }

    private suspend fun downloadAndSaveImage(imageUrl: String, context: android.content.Context): Bitmap? {
        val bitmap = withContext(Dispatchers.IO) {
            downloadImage(imageUrl)
        }

        if (bitmap != null) {
            withContext(Dispatchers.IO) {
                saveImageToDisk(bitmap, context)
            }
        }
        return bitmap
    }

    private fun downloadImage(imageUrl: String): Bitmap? = runCatching {
        val connection = URL(imageUrl).openConnection()
        connection.doInput = true
        val input = connection.getInputStream()
        BitmapFactory.decodeStream(input)
    }.getOrNull()

    private fun saveImageToDisk(bitmap: Bitmap, context: android.content.Context) {
        runCatching {
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "downloaded_image.jpg")
            FileOutputStream(file).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream.flush()
            }
        }.onFailure {
            Toast.makeText(context, "Ошибка сохранения изображения", Toast.LENGTH_SHORT).show()
        }
    }
}
