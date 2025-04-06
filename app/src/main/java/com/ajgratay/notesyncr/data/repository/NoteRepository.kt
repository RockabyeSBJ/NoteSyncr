package com.ajgratay.notesyncr.data.repository

import com.ajgratay.notesyncr.data.model.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun getAllNotes(userId: String): Flow<List<Note>>
    suspend fun getNoteById(noteId: Long): Note?
    suspend fun insertNote(note: Note): Long
    suspend fun updateNote(note: Note)
    suspend fun deleteNote(note: Note)
    suspend fun getUnsyncedNotes(userId: String): List<Note>
} 