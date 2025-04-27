package com.mili.journalapp.ui.theme

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mili.journalapp.data.NoteDatabase
import com.mili.journalapp.data.Note
import com.mili.journalapp.databinding.ActivityAddEditNoteBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddEditNoteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditNoteBinding
    private val noteDao by lazy { NoteDatabase.getDatabase(this).noteDao() }
    private var noteId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        noteId = intent.getIntExtra("note_id", 0)
        val noteContent = intent.getStringExtra("note_content") ?: ""
        binding.editTextNote.setText(noteContent)

        binding.btnSave.setOnClickListener {
            val content = binding.editTextNote.text.toString().trim()
            if (content.isEmpty()) {
                Toast.makeText(this, "Cannot save empty note", Toast.LENGTH_SHORT).show()
            } else {
                saveNote(content)
            }
        }
    }

    private fun saveNote(content: String) {
        CoroutineScope(Dispatchers.IO).launch {
            if (noteId == 0) {
                noteDao.insertNote(Note(content = content, timestamp = System.currentTimeMillis()))
            } else {
                noteDao.updateNote(Note(id = noteId, content = content, timestamp = System.currentTimeMillis()))
            }
            runOnUiThread { finish() }
        }
    }
}
