package com.example.bluromatic.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.bluromatic.DELAY_TIME_MILLIS
import com.example.bluromatic.OUTPUT_PATH
import com.example.bluromatic.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File

private const val TAG = "CleanupWorker"

class CleanupWorker(
    ctx: Context,
    params: WorkerParameters
) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {

        makeStatusNotification(
            applicationContext.resources.getString(R.string.cleaning_up_files),
            applicationContext
        )

        return withContext(Dispatchers.IO) {

            try {

                val outputDirectory = File(
                    applicationContext.filesDir,
                    OUTPUT_PATH
                )

                if (outputDirectory.exists()) {
                    outputDirectory.deleteRecursively()
                }

                delay(DELAY_TIME_MILLIS)

                Result.success()

            } catch (exception: Exception) {

                Log.e(TAG, "Error cleaning files", exception)

                Result.failure()
            }
        }
    }
}