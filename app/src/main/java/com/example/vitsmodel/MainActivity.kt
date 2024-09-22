package com.example.vitsmodel

import VitsModelInference
import ai.onnxruntime.OrtException
import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.example.myapplication.ExoPlayerMedia
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private lateinit var exoPlayer: ExoPlayerMedia
    private lateinit var textView: TextView
    private  var scope= CoroutineScope(Dispatchers.IO)

    @OptIn(UnstableApi::class) @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        textView= findViewById(R.id.textView)
    }

    var count=0
    fun onClick(v: View){
        textView.text = ""
        scope.launch {
            var result= runVitsModel()
            if(result!=null){

                withContext(Dispatchers.Main){

                    exoPlayer= ExoPlayerMedia(this@MainActivity)
                    try {
                        textView.text = result
//                        Toast.makeText(this@MainActivity,result,Toast.LENGTH_SHORT).show()
                        exoPlayer?.playMedia(result)
//                        exoPlayer?.player?.setMediaSource(afd.fileDescriptor, afd.startOffset, afd.length)
                    } catch (e: IOException) {
                        e.printStackTrace() // طباعة الخطأ للتdebugging
                    }// للتحضير للتشغيل

                }
            }

        }
        Toast.makeText(this@MainActivity,"${++count}", Toast.LENGTH_SHORT).show()
    }
    override fun onDestroy() {
        super.onDestroy()
        try {
            if(exoPlayer!=null){
                exoPlayer?.stop()
            }
        }finally {

        }
    }
    private suspend  fun runVitsModel():String?{
        val pathassetManager =assetFilePath( this,"vits_model.onnx")
        if(pathassetManager?.isNullOrEmpty()==true){
            Log.e("pathassetManager","path is null")
            return null
        }
        val model = VitsModelInference(pathassetManager)
        val inputTokens = intArrayOf(1, 2, 3, 4, 5)
        try {

            val result: FloatArray = model.runInference(inputTokens)
            val byteArray = ByteArray(result.size * 2) // 16 بت تعني كل عينة تحتاج بايتين
            for (i in result.indices) {
                // تحويل القيم إلى نطاق 16 بت
                val intSample = (result[i] * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                // كتابة العينات كـ بايتات
                byteArray[i * 2] = (intSample and 0xFF).toByte()         // بايت منخفض
                byteArray[i * 2 + 1] = (intSample shr 8 and 0xFF).toByte() // بايت عالي
            }
            var audioFile = File.createTempFile("output_audio", ".wav", this.cacheDir)
            audioFile.deleteOnExit();
            val fos = FileOutputStream(audioFile)
            fos.write(byteArray)
            fos.close()
            Log.d("audioFile", audioFile?.absoluteFile.toString());
            return audioFile?.absoluteFile?.toString();

        } catch (e: OrtException) {
            e.printStackTrace()
        } finally {
            model.close() // تأكد من إغلاق النموذج بعد الاستخدام
        }
        return null
    }
    @Throws(IOException::class)
    fun assetFilePath(context: Context, assetName: String?): String? {
        val file = File(context.filesDir, assetName)
        if (file.exists() && file.length() > 0) {
            return file.absolutePath
        }

        return copyAssetToFile(context,assetName,file);
    }
    private fun copyAssetToFile(context: Context, assetName: String?, file: File): String? {
        if (assetName == null) {
            throw IllegalArgumentException("Asset name cannot be null")
        }

        // تحقق من وجود الملف في assets
        val assetManager = context.assets
        val assetList = assetManager.list("") // الحصول على قائمة الملفات
        if (assetList?.contains(assetName) == false) {
            throw FileNotFoundException("Asset file $assetName not found")
        }

        // نسخ الملف
        context.assets.open(assetName).use { inputStream ->
            FileOutputStream(file).use { outputStream ->
                val buffer = ByteArray(4 * 1024)
                var read: Int
                while (inputStream.read(buffer).also { read = it } != -1) {
                    outputStream.write(buffer, 0, read)
                }
                outputStream.flush()
            }
        }

        return file.absolutePath
    }

}