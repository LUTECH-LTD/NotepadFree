package lutech.intern.noteapp.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import lutech.intern.noteapp.R
import lutech.intern.noteapp.data.entity.Note
import lutech.intern.noteapp.databinding.ItemNoteBinding
import lutech.intern.noteapp.utils.DateTimeUtils

class NoteAdapter : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {
    private val notes = mutableListOf<Note>()

    @SuppressLint("NotifyDataSetChanged")
    fun submitNotes(notes: List<Note>) {
        this.notes.clear()
        this.notes.addAll(notes)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = ItemNoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoteViewHolder(binding)
    }

    override fun getItemCount() = notes.size

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.onBind(notes[position])
    }

    inner class NoteViewHolder(private val binding: ItemNoteBinding) : ViewHolder(binding.root) {
        fun onBind(note: Note) {
            binding.titleTextView.text = note.title ?: itemView.context.getString(R.string.untitled)
            binding.lastUpdateTextView.text = "Last edit: ${DateTimeUtils.getFormattedDateTime(note.lastUpdate)}"
        }
    }
}