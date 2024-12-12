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
import androidx.compose.material3.DrawerState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.compose.*
import androidx.work.*
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
        val navController = rememberNavController()
        val items = listOf("home", "settings")
        var selectedItem by remember { mutableStateOf("home") }

        // DrawerState для управления открытием/закрытием Drawer
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
                            navController.navigate("home")
                            selectedItem = "home"
                            // Закрыть Drawer
                            coroutineScope.launch {
                                drawerState.close()
                            }
                        }) {
                            Text("Главная")
                        }

                        // Drawer Item for Settings Screen
                        TextButton(onClick = {
                            navController.navigate("settings")
                            selectedItem = "settings"
                            // Закрыть Drawer
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
                        title = { Text("Приложение") },
                        navigationIcon = {
                            IconButton(onClick = {
                                // Открыть Drawer при нажатии на иконку меню
                                coroutineScope.launch {
                                    drawerState.open()
                                }
                            }) {
                                Icon(Icons.Default.Menu, contentDescription = "Меню")
                            }
                        }
                    )
                },
                bottomBar = {
                    NavigationBar {
                        items.forEach { screen ->
                            NavigationBarItem(
                                icon = {
                                    Icon(
                                        if (screen == "home") Icons.Default.Menu else Icons.Default.Settings,
                                        contentDescription = null
                                    )
                                },
                                label = { Text(if (screen == "home") "Главная" else "Настройки") },
                                selected = selectedItem == screen,
                                onClick = {
                                    navController.navigate(screen) {
                                        // Use popUpTo to avoid stack overflow
                                        popUpTo = navController.graph.startDestinationId
                                        launchSingleTop = true
                                    }
                                    selectedItem = screen
                                }
                            )
                        }
                    }
                },
                content = { padding ->
                    Box(modifier = Modifier.padding(padding)) {
                        NavHost(navController, startDestination = "home") {
                            composable("home") { HomeScreen() }
                            composable("settings") { SettingsScreen() }
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
                        // Запускаем фоновую задачу через WorkManager для загрузки изображения
                        val workRequest = OneTimeWorkRequestBuilder<ImageDownloadWorker>()
                            .setInputData(workDataOf("image_url" to imageUrl.text))
                            .build()
                        WorkManager.getInstance(context).enqueue(workRequest)

                        Toast.makeText(context, "Задача загрузки изображения запущена", Toast.LENGTH_SHORT).show()
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

    // WorkManager задача для загрузки изображения
    class ImageDownloadWorker(appContext: android.content.Context, workerParams: WorkerParameters) :
        Worker(appContext, workerParams) {

        override fun doWork(): Result {
            val imageUrl = inputData.getString("image_url") ?: return Result.failure()

            return try {
                val bitmap = downloadImage(imageUrl)
                saveImageToDisk(bitmap)
                Result.success()
            } catch (e: Exception) {
                Result.failure()
            }
        }

        private fun downloadImage(imageUrl: String): Bitmap? = runCatching {
            val connection = URL(imageUrl).openConnection()
            connection.doInput = true
            val input = connection.getInputStream()
            BitmapFactory.decodeStream(input)
        }.getOrNull()

        private fun saveImageToDisk(bitmap: Bitmap?) {
            if (bitmap != null) {
                val context = applicationContext
                val file = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "downloaded_image.jpg")
                FileOutputStream(file).use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                    outputStream.flush()
                }
            }
        }
    }
}
