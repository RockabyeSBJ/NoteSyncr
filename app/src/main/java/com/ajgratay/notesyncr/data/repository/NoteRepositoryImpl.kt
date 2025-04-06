package com.ajgratay.notesyncr.data.repository

import com.ajgratay.notesyncr.data.local.NoteDao
import com.ajgratay.notesyncr.data.model.Note
import com.ajgratay.notesyncr.data.remote.FirebaseService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepositoryImpl @Inject constructor(
    private val noteDao: NoteDao,
    private val firebaseService: FirebaseService
) : NoteRepository {

    override fun getAllNotes(userId: String): Flow<List<Note>> {
        return noteDao.getAllNotes(userId)
    }

    override suspend fun getNoteById(noteId: Long): Note? {
        return noteDao.getNoteById(noteId)
    }

    override suspend fun insertNote(note: Note): Long {
        val localId = noteDao.insertNote(note)
        val noteWithId = note.copy(id = localId)
        
        // Sync with Firebase in the background
        firebaseService.syncNote(noteWithId)
        
        return localId
    }

    override suspend fun updateNote(note: Note) {
        noteDao.updateNote(note)
        
        // Sync with Firebase in the background
        firebaseService.syncNote(note)
    }

    override suspend fun deleteNote(note: Note) {
        noteDao.deleteNote(note)
        
        // Delete from Firebase in the background
        firebaseService.deleteNote(note.id)
    }

    override suspend fun getUnsyncedNotes(userId: String): List<Note> {
        return noteDao.getUnsyncedNotes(userId)
    }

    suspend fun syncWithRemote(userId: String) {
        val unsyncedNotes = getUnsyncedNotes(userId)
        if (unsyncedNotes.isNotEmpty()) {
            firebaseService.syncNotes(unsyncedNotes)
        }
    }
} 