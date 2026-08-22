package com.geecee.escapelauncher.core.common

import android.content.Context
import java.io.IOException
import java.io.InputStream

/**
 * Loads text from a file in Assets
 *
 * @param context Context
 * @param fileName Name of the file text will be loaded from
 *
 * @return Returns a String? with the text from the file
 */
fun loadTextFromAssets(context: Context, fileName: String): String? {
    var inputStream: InputStream? = null
    var fileContent: String? = null
    try {
        inputStream = context.assets.open(fileName)
        fileContent = inputStream.bufferedReader().use { it.readText() }
    } catch (e: IOException) {
        e.printStackTrace()
    } finally {
        inputStream?.close()
    }
    return fileContent
}