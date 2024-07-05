package lutech.intern.noteapp.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import lutech.intern.noteapp.R
import lutech.intern.noteapp.adapter.CategoryAdapter.OnItemClickListener
import lutech.intern.noteapp.constant.SortOption
import lutech.intern.noteapp.data.entity.Category
import lutech.intern.noteapp.data.entity.Note
import lutech.intern.noteapp.data.entity.NoteWithCategories
import lutech.intern.noteapp.databinding.ItemNoteBinding
import lutech.intern.noteapp.utils.DateTimeUtils
import lutech.intern.noteapp.utils.DrawableUtils

class NoteAdapter : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {
    private val noteWithCategories = mutableListOf<NoteWithCategories>()
    private var listener: OnItemClickListener? = null
    private var sortOption: String? = null

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(noteWithCategories: List<NoteWithCategories>, sortOption: String?) {
        this.noteWithCategories.clear()
        this.noteWithCategories.addAll(noteWithCategories)
        this.sortOption = sortOption
        notifyDataSetChanged()
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
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
            binding.titleTextView.text =
                if (noteWithCategories.note.title == null || noteWithCategories.note.title == "") {
                    itemView.context.getString(R.string.untitled)
                } else {
                    noteWithCategories.note.title
                }

            sortOption?.let {
                if (sortOption != SortOption.CREATION_DATE_NEWEST.toString() && sortOption != SortOption.CREATION_DATE_OLDEST.toString()) {
                    binding.lastUpdateTextView.text =
                        "Last edit: ${DateTimeUtils.getFormattedDateTime(noteWithCategories.note.lastUpdate!!)}"
                } else {
                    binding.lastUpdateTextView.text =
                        "Created: ${DateTimeUtils.getFormattedDateTime(noteWithCategories.note.dateCreate!!)}"
                }
            } ?: run {
                binding.lastUpdateTextView.text =
                    "Last edit: ${DateTimeUtils.getFormattedDateTime(noteWithCategories.note.lastUpdate!!)}"
            }


            val categories = noteWithCategories.categories
            if (categories.isEmpty()) {
                binding.categoryNameTextView.text = null
            } else {
                val maxCategoriesToShow = 4
                val categoryNames =
                    categories.take(maxCategoriesToShow).joinToString(", ") { it.name }
                val remainingCategoriesCount = categories.size - maxCategoriesToShow

                val displayText = if (remainingCategoriesCount > 0) {
                    "$categoryNames (+$remainingCategoriesCount)"
                } else {
                    categoryNames
                }

                binding.categoryNameTextView.text = displayText
            }

            binding.main.background = DrawableUtils.createGradientDrawable(
                itemView.context,
                noteWithCategories.note.color
            )

            binding.root.setOnClickListener {
                listener?.onItemClickListener(note = noteWithCategories.note)
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClickListener(note: Note)
    }
}