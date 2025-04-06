package com.ajgratay.notesyncr.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ajgratay.notesyncr.auth.AuthManager
import com.ajgratay.notesyncr.data.repository.NoteRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class NoteSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val noteRepository: NoteRepository,
    private val authManager: AuthManager
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val userId = authManager.currentUser?.uid ?: return Result.failure()
        
        return try {
            (noteRepository as? com.ajgratay.notesyncr.data.repository.NoteRepositoryImpl)?.let {
                it.syncWithRemote(userId)
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
} 