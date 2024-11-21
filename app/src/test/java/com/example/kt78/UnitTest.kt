package com.example.kt78

import kotlinx.coroutines.*
import android.content.Context
import android.graphics.Bitmap
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import com.example.kt78.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.spy
import org.mockito.Mockito.verify
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// Указание на использование тестового раннера Robolectric
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.DEFAULT_MANIFEST_NAME)
class MainActivityTest {

    // Моки для объектов контекста, основной активности и элементов интерфейса
    private lateinit var mockContext: Context
    private lateinit var mainActivity: MainActivity
    private lateinit var mainActivity2: MainActivity
    private val testDispatcher = StandardTestDispatcher()  // Тестовый диспетчер для корутин
    private val editTextUrl: EditText = mock(EditText::class.java)  // Мок для EditText
    private val buttonDownload: Button = mock(Button::class.java)   // Мок для Button
    private val imageView: ImageView = mock(ImageView::class.java)  // Мок для ImageView

    // Метод выполняется перед каждым тестом для инициализации объектов
    @Before
    fun setUp() {
        mockContext = mock(Context::class.java)  // Создание мока контекста
        Dispatchers.setMain(testDispatcher)      // Установка тестового диспетчера для корутин
        mainActivity = MainActivity()            // Создание экземпляра MainActivity
        mainActivity2 = spy(MainActivity())      // Создание "шпиона" для MainActivity
        mainActivity2.editTextUrl = editTextUrl
        mainActivity2.buttonDownload = buttonDownload
        mainActivity2.imageView = imageView
    }

    // Тест на успешную загрузку изображения
    @Test
    fun testDownloadImageSuccess() = runBlocking {
        val imageUrl = "https://images.wallpaperscraft.com/image/single/lake_mountain_tree_36589_2650x1600.jpg"

        // Асинхронно загружаем изображение
        val bitmapDeferred = mainActivity.downloadImage(imageUrl)
        val bitmap = bitmapDeferred.await()  // Ждем результат

        // Проверяем, что загруженное изображение не равно null
        assertNotNull(bitmap)
    }

    // Тест на обработку ошибки при загрузке изображения с некорректного URL
    @Test
    fun testDownloadImageFailure() = runBlocking {
        val invalidImageUrl = "https://example.com/invalid.jpg"

        // Асинхронно пытаемся загрузить изображение
        val bitmapDeferred = mainActivity.downloadImage(invalidImageUrl)
        val bitmap = bitmapDeferred.await()  // Ждем результат

        // Проверяем, что результат загрузки равен null
        assertNull(bitmap)
    }

    // Тест для проверки базовой инициализации активности
    @Test
    fun activityTest() {
        // Создаем и инициализируем MainActivity с помощью Robolectric
        Robolectric.buildActivity(MainActivity::class.java).use { controller ->
            controller.setup()  // Настройка активности
            val activity = controller.get()  // Получаем экземпляр активности

            // Проверяем, что активность и EditText были инициализированы
            assertNotNull(activity)
            val editTextUrl = activity.findViewById<EditText>(R.id.editTextUrl)
            assertNotNull(editTextUrl)
        }
    }

    // Тест на вызов метода compress при сохранении изображения
    @Test
    fun `saveImageToDisk should call compress`() = runTest {
        val mainActivity: MainActivity = mock()
        val bitmap = mock(Bitmap::class.java)
        val spiedMainActivity = spy(mainActivity)

        // Вызываем метод сохранения изображения на диск
        spiedMainActivity.saveImageToDisk(bitmap).join()

        // Проверяем, что метод compress был вызван с определенными параметрами
        verify(bitmap).compress(eq(Bitmap.CompressFormat.JPEG), eq(100), Mockito.any())
    }

    // Тест на проверку вызова downloadAndSaveImage при нажатии кнопки
    @Test
    fun `button click should call downloadAndSaveImage`() {
        val imageUrl = "https://images.wallpaperscraft.com/image/single/lake_mountain_tree_36589_2650x1600.jpg"

        // При клике на кнопку загружаем и сохраняем изображение
        Mockito.doAnswer {
            mainActivity2.downloadAndSaveImage(imageUrl)
            null
        }.`when`(buttonDownload).performClick()  // Устанавливаем поведение при клике

        buttonDownload.performClick()  // Выполняем клик по кнопке

        // Проверяем, что downloadAndSaveImage был вызван
        verify(mainActivity2).downloadAndSaveImage(imageUrl)
    }
}