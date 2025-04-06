package com.ajgratay.notesyncr.di

import android.content.Context
import androidx.room.Room
import com.ajgratay.notesyncr.data.local.NoteDao
import com.ajgratay.notesyncr.data.local.NoteDatabase
import com.ajgratay.notesyncr.data.remote.FirebaseService
import com.ajgratay.notesyncr.data.repository.NoteRepository
import com.ajgratay.notesyncr.data.repository.NoteRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideNoteDatabase(
        @ApplicationContext context: Context
    ): NoteDatabase {
        return Room.databaseBuilder(
            context,
            NoteDatabase::class.java,
            "note_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideNoteDao(database: NoteDatabase): NoteDao {
        return database.noteDao()
    }

    @Provides
    @Singleton
    fun provideNoteRepository(
        noteDao: NoteDao,
        firebaseService: FirebaseService
    ): NoteRepository {
        return NoteRepositoryImpl(noteDao, firebaseService)
    }
} 