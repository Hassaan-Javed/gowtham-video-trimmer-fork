package com.hi.library.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


object FileUtilKt {



    @JvmStatic
    fun getActualFileUri(context: Context, uri: Uri): Uri {
        val inputStream = context.contentResolver.openInputStream(uri)
        if (inputStream == null) throw IOException("Unable to open input stream from URI")

        val fileName = "video_" + System.currentTimeMillis() + ".mp4"
        val tempFile = File(context.cacheDir, fileName)

        try {
            FileOutputStream(tempFile).use { outputStream ->
                val buffer = ByteArray(4096)
                var bytesRead: Int
                while ((inputStream.read(buffer).also { bytesRead = it }) != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                }
            }
        } finally {
            inputStream.close()
        }

        return Uri.fromFile(tempFile)
    }


    @JvmStatic
    fun createVideoUri(context: Context): Uri? {
        val videoFile = File(
            context.cacheDir,
            "captured_video_" + System.currentTimeMillis() + ".mp4"
        )
        return FileProvider.getUriForFile(
            context,
            context.packageName + ".fileprovider",
            videoFile
        )
    }


}