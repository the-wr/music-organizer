package com.wr.musicorganizer.utils

import android.content.Context
import com.google.gson.GsonBuilder
import io.reactivex.Completable
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.util.*


object FileUtils {
    const val MAX_TRIES = 5
    const val RETRY_DELAY_MS = 1000L

    fun save(context: Context, obj: Any, filename: String) = Completable.create {
        try {
            val gson = GsonBuilder().setPrettyPrinting().create()
            val json = gson.toJson(obj)
            val byteArray = json.toByteArray()

            val tempFilename = "temp_$filename"
            var tries = 0
            var success = false

            while (!success && tries < MAX_TRIES) {
                try {
                    // 1. Write everything to temp file and make sure it's fully and correctly written
                    val streamOut = context.openFileOutput(tempFilename, Context.MODE_PRIVATE)
                    streamOut.write(byteArray)
                    streamOut.close()

                    val tempFile = RandomAccessFile(File(context.filesDir, tempFilename), "r")
                    val length = tempFile.length().toInt()

                    if (length != byteArray.size) {
                        tries++
                        Thread.sleep(RETRY_DELAY_MS)
                        continue
                    }

                    val byteArrayIn = ByteArray(length)
                    tempFile.readFully(byteArrayIn)
                    tempFile.close()

                    if (!Arrays.equals(byteArray, byteArrayIn)) {
                        tries++
                        Thread.sleep(RETRY_DELAY_MS)
                        continue
                    }

                    // 2. Delete original file if it exists
                    val originalFile = File(filename)
                    if (originalFile.exists()) {
                        originalFile.delete()
                    }

                    // 3. Rename temp file to original file
                    success = File(context.filesDir, tempFilename).renameTo(File(context.filesDir, filename))
                    if (!success) {
                        tries++
                        Thread.sleep(RETRY_DELAY_MS)
                    }
                } catch (e: Throwable) {
                    tries++
                    Thread.sleep(RETRY_DELAY_MS)
                }
            }

            if (success) {
                it.onComplete()
            } else {
                it.onError(Exception("Failed to reliably save file after $tries attempts"))
            }
        } catch (e: Throwable) {
            it.onError(e)
        }
    }

    fun copy(src: File, dst: File) {
        val inStream = FileInputStream(src)
        val outStream = FileOutputStream(dst)
        val inChannel = inStream.channel
        val outChannel = outStream.channel
        val written = inChannel.transferTo(0, inChannel.size(), outChannel)
        inStream.close()
        outStream.close()
    }
}