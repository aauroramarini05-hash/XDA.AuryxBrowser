package com.xdustatom.auryxbrowser.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.net.URL

object FaviconUtils {
    
    suspend fun fetchFavicon(pageUrl: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val url = URL(pageUrl)
            val faviconUrl = "${url.protocol}://${url.host}/favicon.ico"
            val connection = URL(faviconUrl).openConnection()
            connection.connectTimeout = 3000
            connection.readTimeout = 3000
            val inputStream = connection.getInputStream()
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            null
        }
    }
    
    fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
    }
    
    fun base64ToBitmap(base64: String): Bitmap? {
        return try {
            val bytes = Base64.decode(base64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (e: Exception) {
            null
        }
    }
}
