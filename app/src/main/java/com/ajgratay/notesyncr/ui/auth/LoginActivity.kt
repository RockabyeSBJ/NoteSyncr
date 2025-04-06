package com.ajgratay.notesyncr.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ajgratay.notesyncr.ui.notes.NotesActivity
import com.ajgratay.notesyncr.ui.theme.NoteSyncrTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : ComponentActivity() {

    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            NoteSyncrTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LoginScreen(
                        onSignInSuccess = { navigateToMain() },
                        onSignUpClick = { navigateToSignUp() }
                    )
                }
            }
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, NotesActivity::class.java))
        finish()
    }
    
    private fun navigateToSignUp() {
        // TODO: Navigate to SignUpActivity
        // For now, we'll just show a toast
        // In a real app, you would use navigation component or startActivity
    }
} 