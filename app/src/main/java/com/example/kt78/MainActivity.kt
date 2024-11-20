package com.example.kt78

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL

class MainActivity : AppCompatActivity() {

    lateinit var editTextUrl:EditText
    lateinit var buttonDownload:Button
    lateinit var imageView:ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var editTextUrl = findViewById<EditText>(R.id.editTextUrl)
        var buttonDownload = findViewById<Button>(R.id.buttonDownload)
        var imageView = findViewById<ImageView>(R.id.imageView)

        buttonDownload.setOnClickListener {
            val imageUrl = editTextUrl.text.toString()
            lifecycleScope.launch {
                val bitmap = downloadAndSaveImage(imageUrl)
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap)
                    showToast("Изображение загружено и сохранено")
                } else {
                    showToast("Ошибка загрузки изображения")
                }
            }
        }
    }

    suspend fun downloadAndSaveImage(imageUrl: String): Bitmap? {
        // Переводим выполнение в фоновый поток
        val bitmap = withContext(Dispatchers.IO) {
            downloadImage(imageUrl) // Загружаем изображение
        }


        // Если изображение было успешно загружено, сохраняем его
        if (bitmap != null) {
            withContext(Dispatchers.IO) {
                saveImageToDisk(bitmap)
            }
        }

        return bitmap // Возвращаем результат (bitmap или null)
    }

    fun downloadImage(imageUrl: String): Bitmap? = runCatching {
        val connection = URL(imageUrl).openConnection()
        connection.doInput = true
        val input = connection.getInputStream()
        BitmapFactory.decodeStream(input)
    }.getOrNull()


    fun saveImageToDisk(bitmap: Bitmap) {

        runCatching {
            val file = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "downloaded_image.jpg")
            FileOutputStream(file).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                // принудительная запись данных
                outputStream.flush()
            }
        }.onFailure { showToast("Ошибка сохранения изображения") }
    }

    fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}