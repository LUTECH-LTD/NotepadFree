package lutech.intern.noteapp.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import lutech.intern.noteapp.R
import lutech.intern.noteapp.data.entity.Note
import lutech.intern.noteapp.data.entity.NoteWithCategories
import lutech.intern.noteapp.databinding.ItemNoteBinding
import lutech.intern.noteapp.utils.DateTimeUtils

class NoteAdapter : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {
    private val noteWithCategories = mutableListOf<NoteWithCategories>()

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(noteWithCategories: List<NoteWithCategories>) {
        this.noteWithCategories.clear()
        this.noteWithCategories.addAll(noteWithCategories)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = ItemNoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoteViewHolder(binding)
    }

    override fun getItemCount() = noteWithCategories.size

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.onBind(noteWithCategories[position])
    }

    inner class NoteViewHolder(private val binding: ItemNoteBinding) : ViewHolder(binding.root) {
        fun onBind(noteWithCategories: NoteWithCategories) {
            binding.titleTextView.text = noteWithCategories.note.title ?: itemView.context.getString(R.string.untitled)
            binding.lastUpdateTextView.text = "Last edit: ${DateTimeUtils.getFormattedDateTime(noteWithCategories.note.lastUpdate!!)}"

            val categories = noteWithCategories.categories
            if(categories.isEmpty()) {
                binding.categoryNameTextView.text = null
            } else {
                binding.categoryNameTextView.text = categories.joinToString(", ") { it.name }
            }
        }
    }
}