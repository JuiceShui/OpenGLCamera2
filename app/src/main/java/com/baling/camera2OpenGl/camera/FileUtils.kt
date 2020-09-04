package com.baling.camera2OpenGl.camera

import android.content.Context
import java.io.File

class FileUtils {
    companion object {

        fun getFileDir(context: Context): File? {
            var filesDir = context.getExternalFilesDir(null)
            if (filesDir == null) {
                filesDir = context.filesDir
            }
            return filesDir
        }

        fun getMediaFileDir(context: Context?): File? {
            val fileDir = getFileDir(
                context!!
            )!!
            val wavFileDir = File(fileDir, "media")
            if (!wavFileDir.exists()) {
                wavFileDir.mkdirs()
            }
            return wavFileDir
        }
    }
}