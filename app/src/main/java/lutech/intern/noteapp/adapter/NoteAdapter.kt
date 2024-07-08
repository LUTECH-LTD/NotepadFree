package lutech.intern.noteapp.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import lutech.intern.noteapp.R
import lutech.intern.noteapp.common.PreferencesManager
import lutech.intern.noteapp.constant.Constants
import lutech.intern.noteapp.constant.SortNoteMode
import lutech.intern.noteapp.data.entity.Category
import lutech.intern.noteapp.data.entity.Note
import lutech.intern.noteapp.data.entity.NoteWithCategories
import lutech.intern.noteapp.databinding.ItemNoteBinding
import lutech.intern.noteapp.ui.main.MainActivity
import lutech.intern.noteapp.utils.DateTimeUtils
import lutech.intern.noteapp.utils.DrawableUtils

class NoteAdapter(private val context: Context) :
    RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {
    private val noteWithCategories = mutableListOf<NoteWithCategories>()
    private val selectedNotes = mutableListOf<Note>()
    private var isSelectedMode = false
    private var listener: OnItemClickListener? = null

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(noteWithCategories: List<NoteWithCategories>) {
        this.noteWithCategories.clear()
        this.noteWithCategories.addAll(noteWithCategories)
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
            Log.e(Constants.TAG, "onBind: $selectedNotes")
            binding.titleTextView.text = getDisplayNoteTitle(noteWithCategories.note)
            binding.dateTextView.text = getDisplayNoteDateTime(noteWithCategories.note)
            binding.categoryNameTextView.text =
                getDisplayNoteCategories(noteWithCategories.categories)

            if (selectedNotes.contains(noteWithCategories.note)) {
                binding.main.setBackgroundResource(R.drawable.bg_gradient_delete)
            } else {
                binding.main.background = getDisplayNoteBackground(noteWithCategories.note)
            }

            binding.main.setOnClickListener {
                listener?.onItemClickListener(note = noteWithCategories.note)
            }

            binding.main.setOnLongClickListener {
                listener?.onItemLongClickListener(note = noteWithCategories.note)
                true
            }
        }
    }

    private fun getDisplayNoteTitle(note: Note): String {
        return note.title.ifEmpty {
            context.getString(R.string.untitled)
        }
    }

    private fun getDisplayNoteDateTime(note: Note): String {
        return when (PreferencesManager.getSortMode()) {
            SortNoteMode.CREATION_DATE_NEWEST.toString(),
            SortNoteMode.CREATION_DATE_OLDEST.toString() -> {
                context.getString(R.string.created).plus(DateTimeUtils.getFormattedDateTime(note.dateCreate))
            }

            else -> {
                context.getString(R.string.last_edit).plus(DateTimeUtils.getFormattedDateTime(note.lastUpdate))
            }
        }
    }

    private fun getDisplayNoteCategories(categories: List<Category>): String {
        val maxCategoriesToShow = 4
        val categoryNames = categories.take(maxCategoriesToShow).joinToString(", ") { it.name }
        val remainingCategoriesCount = categories.size - maxCategoriesToShow

        return if (remainingCategoriesCount > 0) {
            "$categoryNames (+$remainingCategoriesCount)"
        } else {
            categoryNames
        }
    }

    private fun getDisplayNoteBackground(note: Note): GradientDrawable {
        return DrawableUtils.createGradientDrawable(context, note.color)
    }

    fun toggleSelection(note: Note) {
        if (selectedNotes.contains(note)) {
            selectedNotes.remove(note)
        } else {
            selectedNotes.add(note)
        }
        notifyDataSetChanged()
    }

    fun getSelectedNotesCount(): Int {
        return selectedNotes.size
    }

    fun getSelectedNotes(): List<Note> {
        return selectedNotes
    }

    fun getNoteWithCategories(): List<NoteWithCategories> {
        return noteWithCategories
    }

    fun clearSelectedNotes() {
        selectedNotes.clear()
        notifyDataSetChanged()
    }

    fun selectAllNotes() {
        selectedNotes.clear()
        noteWithCategories.forEach { noteWithCategory ->
            selectedNotes.add(noteWithCategory.note)
        }
        notifyDataSetChanged()
    }

    fun setSelectedMode(isSelectedMode: Boolean) {
        this.isSelectedMode = isSelectedMode
        notifyDataSetChanged()
    }

    fun isSelectedMode(): Boolean {
        return isSelectedMode
    }

    interface OnItemClickListener {
        fun onItemClickListener(note: Note)

        fun onItemLongClickListener(note: Note)
    }
}