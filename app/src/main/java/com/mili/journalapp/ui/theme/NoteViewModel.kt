package com.mili.journalapp.ui.theme

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.mili.journalapp.data.Note
import com.mili.journalapp.data.NoteDatabase

class NoteViewModel(application: Application) : AndroidViewModel(application) {

    private val noteDao = NoteDatabase.getDatabase(application).noteDao()

    val allNotes: LiveData<List<Note>> = noteDao.getAllNotes()
}
