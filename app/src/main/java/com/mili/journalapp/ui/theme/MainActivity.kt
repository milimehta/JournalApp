package com.mili.journalapp.ui.theme

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.mili.journalapp.R
import com.mili.journalapp.data.Note
import com.mili.journalapp.data.NoteDatabase
import com.mili.journalapp.databinding.ActivityMainBinding
import com.mili.journalapp.util.FileUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), NoteAdapter.NoteClickListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: NoteAdapter
    private val viewModel: NoteViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        adapter = NoteAdapter(this)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        viewModel.allNotes.observe(this) {
            adapter.submitList(it)
        }

        binding.fab.setOnClickListener {
            startActivity(Intent(this, AddEditNoteActivity::class.java))
        }

        swipeToDelete()
    }

    private fun swipeToDelete() {
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder) = false

            override fun onSwiped(vh: RecyclerView.ViewHolder, direction: Int) {
                val position = vh.adapterPosition
                val note = adapter.getNoteAt(position)

                CoroutineScope(Dispatchers.IO).launch {
                    NoteDatabase.getDatabase(this@MainActivity).noteDao().deleteNote(note)
                }

                Snackbar.make(binding.root, "Note deleted", Snackbar.LENGTH_LONG)
                    .setAction("UNDO") {
                        CoroutineScope(Dispatchers.IO).launch {
                            NoteDatabase.getDatabase(this@MainActivity).noteDao().insertNote(note)
                        }
                    }.show()
            }
        })

        itemTouchHelper.attachToRecyclerView(binding.recyclerView)
    }

    override fun onNoteClick(note: Note) {
        val intent = Intent(this, AddEditNoteActivity::class.java)
        intent.putExtra("note_id", note.id)
        intent.putExtra("note_content", note.content)
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_export -> {
                exportSelectedNotes()
                true
            }
            R.id.action_share -> {
                shareSelectedNotes()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun exportSelectedNotes() {
        val selectedNotes = adapter.getSelectedNotes()
        if (selectedNotes.isEmpty()) {
            Toast.makeText(this, "No notes selected", Toast.LENGTH_SHORT).show()
            return
        }
        selectedNotes.forEach { note ->
            FileUtils.saveNoteToFile(this, note.content, note.timestamp)
        }
        Toast.makeText(this, "Notes exported!", Toast.LENGTH_SHORT).show()
    }

    private fun shareSelectedNotes() {
        val selectedNotes = adapter.getSelectedNotes()
        if (selectedNotes.isEmpty()) {
            Toast.makeText(this, "No notes selected", Toast.LENGTH_SHORT).show()
            return
        }
        val uris = selectedNotes.mapNotNull {
            FileUtils.saveNoteToFile(this, it.content, it.timestamp)?.let { file ->
                androidx.core.content.FileProvider.getUriForFile(
                    this,
                    "${packageName}.provider",
                    file
                )
            }
        }

        val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = "text/plain"
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(intent, "Share Notes"))
    }
}
