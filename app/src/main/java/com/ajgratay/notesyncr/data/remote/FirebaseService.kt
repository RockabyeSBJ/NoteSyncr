package com.ajgratay.notesyncr.data.remote

import com.ajgratay.notesyncr.data.model.Note
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseService @Inject constructor() {
    private val firestore: FirebaseFirestore = Firebase.firestore
    private val notesCollection = firestore.collection("notes")

    fun observeNotes(userId: String): Flow<List<Note>> = callbackFlow {
        val subscription = notesCollection
            .whereEqualTo("userId", userId)
            .orderBy("updatedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Handle error
                    return@addSnapshotListener
                }

                val notes = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Note::class.java)?.copy(id = doc.id.toLong())
                } ?: emptyList()

                trySend(notes)
            }

        awaitClose { subscription.remove() }
    }

    suspend fun syncNote(note: Note): Result<Note> = try {
        val noteData = note.copy(isSynced = true)
        val docRef = if (note.id == 0L) {
            notesCollection.document()
        } else {
            notesCollection.document(note.id.toString())
        }

        docRef.set(noteData).await()
        Result.success(noteData.copy(id = docRef.id.toLong()))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteNote(noteId: Long): Result<Unit> = try {
        notesCollection.document(noteId.toString()).delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun syncNotes(notes: List<Note>): Result<List<Note>> = try {
        val batch = firestore.batch()
        val syncedNotes = notes.map { note ->
            val docRef = if (note.id == 0L) {
                notesCollection.document()
            } else {
                notesCollection.document(note.id.toString())
            }
            val noteData = note.copy(isSynced = true)
            batch.set(docRef, noteData)
            noteData.copy(id = docRef.id.toLong())
        }
        batch.commit().await()
        Result.success(syncedNotes)
    } catch (e: Exception) {
        Result.failure(e)
    }
} 