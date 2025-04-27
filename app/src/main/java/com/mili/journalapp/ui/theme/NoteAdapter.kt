package com.mili.journalapp.ui.theme

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mili.journalapp.data.Note
import com.mili.journalapp.databinding.ItemNoteBinding

class NoteAdapter(private val listener: NoteClickListener) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    private var notes = listOf<Note>()
    private val selectedNotes = mutableSetOf<Note>()

    interface NoteClickListener {
        fun onNoteClick(note: Note)
    }

    fun submitList(list: List<Note>) {
        notes = list
        notifyDataSetChanged()
    }

    fun getSelectedNotes(): List<Note> = selectedNotes.toList()

    fun getNoteAt(position: Int): Note = notes[position]

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = ItemNoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(notes[position])
    }

    override fun getItemCount() = notes.size

    inner class NoteViewHolder(private val binding: ItemNoteBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(note: Note) {
            binding.textViewContent.text = note.content
            binding.textViewTimestamp.text = android.text.format.DateFormat.format("yyyy-MM-dd hh:mm a", note.timestamp)

            binding.root.setBackgroundColor(
                if (selectedNotes.contains(note)) 0xFFE0E0E0.toInt() else 0xFFFFFFFF.toInt()
            )

            binding.root.setOnClickListener {
                if (selectedNotes.isNotEmpty()) {
                    toggleSelection(note)
                } else {
                    listener.onNoteClick(note)
                }
            }

            binding.root.setOnLongClickListener {
                toggleSelection(note)
                true
            }
        }

        private fun toggleSelection(note: Note) {
            if (selectedNotes.contains(note)) {
                selectedNotes.remove(note)
            } else {
                selectedNotes.add(note)
            }
            notifyItemChanged(notes.indexOf(note))
        }
    }
}
