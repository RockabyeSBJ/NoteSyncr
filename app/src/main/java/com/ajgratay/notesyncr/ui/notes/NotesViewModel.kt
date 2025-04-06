package com.ajgratay.notesyncr.ui.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ajgratay.notesyncr.auth.AuthManager
import com.ajgratay.notesyncr.data.model.Note
import com.ajgratay.notesyncr.data.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

data class NotesState(
    val notes: List<Note> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAddingNote: Boolean = false,
    val isEditingNote: Boolean = false,
    val currentNote: Note? = null
)

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val authManager: AuthManager
) : ViewModel() {

    private val _state = MutableStateFlow(NotesState())
    val state: StateFlow<NotesState> = _state.asStateFlow()

    init {
        loadNotes()
    }

    private fun loadNotes() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            try {
                val userId = authManager.currentUser?.uid ?: return@launch
                noteRepository.getAllNotes(userId).collect { notes ->
                    _state.update { 
                        it.copy(
                            notes = notes,
                            isLoading = false
                        ) 
                    }
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load notes"
                    ) 
                }
            }
        }
    }

    fun addNote(title: String, content: String) {
        viewModelScope.launch {
            try {
                val userId = authManager.currentUser?.uid ?: return@launch
                val note = Note(
                    title = title,
                    content = content,
                    userId = userId,
                    createdAt = Date(),
                    updatedAt = Date()
                )
                noteRepository.insertNote(note)
                _state.update { it.copy(isAddingNote = false) }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        error = e.message ?: "Failed to add note"
                    ) 
                }
            }
        }
    }

    fun updateNote(note: Note, title: String, content: String) {
        viewModelScope.launch {
            try {
                val updatedNote = note.copy(
                    title = title,
                    content = content,
                    updatedAt = Date()
                )
                noteRepository.updateNote(updatedNote)
                _state.update { it.copy(isEditingNote = false, currentNote = null) }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        error = e.message ?: "Failed to update note"
                    ) 
                }
            }
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            try {
                noteRepository.deleteNote(note)
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        error = e.message ?: "Failed to delete note"
                    ) 
                }
            }
        }
    }

    fun showAddNoteDialog() {
        _state.update { it.copy(isAddingNote = true) }
    }

    fun hideAddNoteDialog() {
        _state.update { it.copy(isAddingNote = false) }
    }

    fun showEditNoteDialog(note: Note) {
        _state.update { it.copy(isEditingNote = true, currentNote = note) }
    }

    fun hideEditNoteDialog() {
        _state.update { it.copy(isEditingNote = false, currentNote = null) }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
} 