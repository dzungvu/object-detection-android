package com.luke.objectdetection.utils

import android.graphics.Bitmap
import android.util.Base64
import java.io.ByteArrayOutputStream

object Utils {
    fun bitmapToBase64(bitmap: Bitmap
    ): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }
}