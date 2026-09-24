package com.example.bluromatic.workers

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.bluromatic.DELAY_TIME_MILLIS
import com.example.bluromatic.KEY_IMAGE_URI
import com.example.bluromatic.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val TAG = "SaveImageToFileWorker"

class SaveImageToFileWorker(
    ctx: Context,
    params: WorkerParameters
) : CoroutineWorker(ctx, params) {

    private val title = "Blurred Image"

    private val dateFormatter = SimpleDateFormat(
        "yyyy.MM.dd 'at' HH:mm:ss z",
        Locale.getDefault()
    )

    override suspend fun doWork(): Result {

        makeStatusNotification(
            applicationContext.resources.getString(R.string.saving_image),
            applicationContext
        )

        return withContext(Dispatchers.IO) {

            try {

                val resourceUri = inputData.getString(KEY_IMAGE_URI)

                require(!resourceUri.isNullOrEmpty())

                val bitmap = BitmapFactory.decodeStream(
                    applicationContext.contentResolver
                        .openInputStream(Uri.parse(resourceUri))
                )

                val resolver = applicationContext.contentResolver

                val imageCollection =
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI

                val imageDetails = android.content.ContentValues().apply {
                    put(
                        MediaStore.Images.Media.DISPLAY_NAME,
                        "$title ${dateFormatter.format(Date())}.jpg"
                    )

                    put(
                        MediaStore.Images.Media.MIME_TYPE,
                        "image/jpeg"
                    )
                }

                val imageUri = resolver.insert(
                    imageCollection,
                    imageDetails
                )

                imageUri?.let {

                    resolver.openOutputStream(it)?.use { outputStream ->
                        bitmap.compress(
                            android.graphics.Bitmap.CompressFormat.JPEG,
                            100,
                            outputStream
                        )
                    }
                }

                delay(DELAY_TIME_MILLIS)

                Result.success(
                    workDataOf(
                        KEY_IMAGE_URI to imageUri.toString()
                    )
                )

            } catch (exception: Exception) {

                Log.e(TAG, "Error saving image", exception)

                Result.failure()
            }
        }
    }
}