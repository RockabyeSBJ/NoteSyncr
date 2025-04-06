package com.ajgratay.notesyncr.ui.auth

import android.content.Intent
import android.util.Patterns
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ajgratay.notesyncr.auth.AuthManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val isSignedIn: Boolean = false,
    val error: String? = null,
    val resetPasswordSent: Boolean = false
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authManager: AuthManager
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    private var activityResultLauncher: ActivityResultLauncher<Intent>? = null

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        _state.value = _state.value.copy(
            isSignedIn = authManager.isUserSignedIn()
        )
    }

    fun setActivityResultLauncher(launcher: ActivityResultLauncher<Intent>) {
        activityResultLauncher = launcher
    }

    fun updateEmail(email: String) {
        _state.update { 
            it.copy(
                email = email, 
                emailError = validateEmail(email),
                error = null
            ) 
        }
    }

    fun updatePassword(password: String) {
        _state.update { 
            it.copy(
                password = password, 
                passwordError = validatePassword(password),
                error = null
            ) 
        }
    }

    private fun validateEmail(email: String): String? {
        return when {
            email.isEmpty() -> null
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Please enter a valid email address"
            else -> null
        }
    }

    private fun validatePassword(password: String): String? {
        return when {
            password.isEmpty() -> null
            password.length < 6 -> "Password must be at least 6 characters"
            else -> null
        }
    }

    fun signInWithEmail() {
        // Validate inputs before proceeding
        val emailError = validateEmail(_state.value.email)
        val passwordError = validatePassword(_state.value.password)
        
        if (emailError != null || passwordError != null) {
            _state.update { 
                it.copy(
                    emailError = emailError,
                    passwordError = passwordError
                ) 
            }
            return
        }
        
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                error = null
            )
            
            try {
                authManager.signInWithEmail(_state.value.email, _state.value.password)
                    .onSuccess {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            isSignedIn = true
                        )
                    }
                    .onFailure { e ->
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = e.message ?: "Authentication failed"
                        )
                    }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Authentication failed"
                )
            }
        }
    }

    fun signInWithGoogle() {
        val launcher = activityResultLauncher
        if (launcher == null) {
            _state.value = _state.value.copy(
                error = "ActivityResultLauncher not initialized"
            )
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                error = null
            )
            
            try {
                authManager.signInWithGoogle(launcher)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to start Google Sign-In"
                )
            }
        }
    }

    fun handleGoogleSignInResult(data: Intent?) {
        viewModelScope.launch {
            try {
                authManager.handleGoogleSignInResult(data)
                    .onSuccess {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            isSignedIn = true
                        )
                    }
                    .onFailure { e ->
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = e.message ?: "Authentication failed"
                        )
                    }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Authentication failed"
                )
            }
        }
    }
    
    fun resetPassword(email: String) {
        if (email.isBlank()) {
            _state.update { it.copy(error = "Please enter your email address") }
            return
        }
        
        if (validateEmail(email) != null) {
            _state.update { it.copy(error = "Please enter a valid email address") }
            return
        }
        
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                error = null
            )
            
            try {
                authManager.resetPassword(email)
                    .onSuccess {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            resetPasswordSent = true,
                            error = "Password reset email sent. Please check your inbox."
                        )
                    }
                    .onFailure { e ->
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = e.message ?: "Failed to send password reset email"
                        )
                    }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to send password reset email"
                )
            }
        }
    }
} 