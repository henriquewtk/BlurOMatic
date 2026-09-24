package com.example.bluromatic.data

import android.content.Context
import android.net.Uri
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.bluromatic.KEY_BLUR_LEVEL
import com.example.bluromatic.KEY_IMAGE_URI
import com.example.bluromatic.getImageUri
import com.example.bluromatic.workers.BlurWorker
import com.example.bluromatic.workers.CleanupWorker
import com.example.bluromatic.workers.SaveImageToFileWorker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.work.Constraints

class WorkManagerBluromaticRepository(
    context: Context
) : BluromaticRepository {

    override val outputWorkInfo: Flow<WorkInfo?> = MutableStateFlow(null)

    private var imageUri: Uri = context.getImageUri()

    private val workManager = WorkManager.getInstance(context)

    override fun applyBlur(blurLevel: Int) {

        // Primeiro limpa os arquivos temporários
        var continuation = workManager.beginWith(
            OneTimeWorkRequest.from(CleanupWorker::class.java)
        )

        // Cria o Worker responsável pelo desfoque
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()

        val blurBuilder = OneTimeWorkRequestBuilder<BlurWorker>()
            .setConstraints(constraints)

        // Envia os dados para o BlurWorker
        blurBuilder.setInputData(
            createInputDataForWorkRequest(
                blurLevel,
                imageUri
            )
        )

        // Adiciona o BlurWorker à sequência
        continuation = continuation.then(
            blurBuilder.build()
        )

        // Cria o Worker responsável por salvar a imagem
        val save = OneTimeWorkRequestBuilder<SaveImageToFileWorker>()
            .build()

        // Adiciona o SaveImageToFileWorker à sequência
        continuation = continuation.then(save)

        // Inicia a sequência
        continuation.enqueue()
    }

    override fun cancelWork() {
        workManager.cancelAllWork()
    }

    private fun createInputDataForWorkRequest(
        blurLevel: Int,
        imageUri: Uri
    ): Data {

        val builder = Data.Builder()

        builder
            .putString(KEY_IMAGE_URI, imageUri.toString())
            .putInt(KEY_BLUR_LEVEL, blurLevel)

        return builder.build()
    }
}