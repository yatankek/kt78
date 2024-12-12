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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
                MainScaffold()
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MainScaffold() {
        var currentScreen by remember { mutableStateOf("Главная") }
        val context = LocalContext.current

        // Define Drawer State and Scope
        val drawerState = rememberDrawerState(DrawerValue.Closed)
        val coroutineScope = rememberCoroutineScope()

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet {
                    Column {
                        Text("Заголовок меню", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(16.dp))

                        // Drawer Item for Home Screen
                        TextButton(onClick = {
                            currentScreen = "Главная"
                            coroutineScope.launch {
                                drawerState.close()
                            }
                        }) {
                            Text("Главная")
                        }

                        // Drawer Item for Settings Screen
                        TextButton(onClick = {
                            currentScreen = "Настройки"
                            coroutineScope.launch {
                                drawerState.close()
                            }
                        }) {
                            Text("Настройки")
                        }
                    }
                }
            }
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(currentScreen) },
                        navigationIcon = {
                            IconButton(onClick = {
                                coroutineScope.launch {
                                    if (drawerState.isOpen) drawerState.close() else drawerState.open()
                                }
                            }) {
                                Icon(Icons.Default.Menu, contentDescription = "Меню")
                            }
                        }
                    )
                },
                bottomBar = {
                    BottomAppBar {
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Menu, contentDescription = null) },
                            label = { Text("Главная") },
                            selected = currentScreen == "Главная",
                            onClick = { currentScreen = "Главная" }
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                            label = { Text("Настройки") },
                            selected = currentScreen == "Настройки",
                            onClick = { currentScreen = "Настройки" }
                        )
                    }
                },
                content = { padding ->
                    Box(modifier = Modifier.padding(padding)) {
                        when (currentScreen) {
                            "Главная" -> HomeScreen()
                            "Настройки" -> SettingsScreen()
                        }
                    }
                }
            )
        }
    }

    @Composable
    fun HomeScreen() {
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

    @Composable
    fun SettingsScreen() {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Здесь будут настройки", style = MaterialTheme.typography.headlineSmall)
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
