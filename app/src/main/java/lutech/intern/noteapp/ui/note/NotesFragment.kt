package lutech.intern.noteapp.ui.note

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.github.dhaval2404.colorpicker.MaterialColorPickerDialog
import com.github.dhaval2404.colorpicker.model.ColorShape
import com.github.dhaval2404.colorpicker.model.ColorSwatch
import lutech.intern.noteapp.R
import lutech.intern.noteapp.adapter.NoteAdapter
import lutech.intern.noteapp.common.PreferencesManager
import lutech.intern.noteapp.constant.Constants
import lutech.intern.noteapp.constant.SortNoteMode
import lutech.intern.noteapp.data.entity.Note
import lutech.intern.noteapp.data.entity.NoteWithCategories
import lutech.intern.noteapp.data.entity.relations.NoteCategoryCrossRef
import lutech.intern.noteapp.databinding.FragmentNotesBinding
import lutech.intern.noteapp.event.ClearNotesSelectedEvent
import lutech.intern.noteapp.event.ColorizeNoteEvent
import lutech.intern.noteapp.event.DeleteNoteEvent
import lutech.intern.noteapp.event.LoadNotesEvent
import lutech.intern.noteapp.event.SearchNoteEvent
import lutech.intern.noteapp.event.SelectedAllNotesEvent
import lutech.intern.noteapp.ui.editor.NoteEditorActivity
import lutech.intern.noteapp.ui.main.MainActivity
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.Collections

class NotesFragment : Fragment() {
    private val binding by lazy { FragmentNotesBinding.inflate(layoutInflater) }
    private val notesViewModel: NotesViewModel by viewModels()
    private val noteAdapter by lazy { NoteAdapter(requireContext()) }
    private var notes: List<NoteWithCategories> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        handleEvent()
        observeDataViewModel()
    }

    private fun initViews() {
        initRecyclerView()
    }

    private fun initRecyclerView() {
        binding.notesRecyclerView.adapter = noteAdapter
    }

    private fun observeDataViewModel() {
        notesViewModel.noteWithCategories.observe(viewLifecycleOwner) { list ->
            notes = list
            reloadData()
        }
    }

    private fun filterAndSortNotes(noteWithCategories: List<NoteWithCategories>): List<NoteWithCategories> {
        val menuItemId = (activity as MainActivity).navMenuItemIdSelected ?: return noteWithCategories

        // Filter list by category
        val filteredList = when (menuItemId) {
            R.id.menu_notes -> noteWithCategories
            R.id.menu_uncategorized -> noteWithCategories.filter { it.categories.isEmpty() }
            else -> noteWithCategories.filter { itemList ->
                itemList.categories.any {
                    it.categoryId.toInt() == menuItemId
                }
            }
        }

        // Sort list after filter list
        return when (PreferencesManager.getSortMode()) {
            SortNoteMode.EDIT_DATE_NEWEST.name -> filteredList.sortedByDescending { it.note.lastUpdate }
            SortNoteMode.EDIT_DATE_OLDEST.name -> filteredList.sortedBy { it.note.lastUpdate }
            SortNoteMode.CREATION_DATE_NEWEST.name -> filteredList.sortedByDescending { it.note.dateCreate }
            SortNoteMode.CREATION_DATE_OLDEST.name -> filteredList.sortedBy { it.note.dateCreate }
            SortNoteMode.TITLE_A_Z.name -> filteredList.sortedBy { it.note.title.lowercase() }
            SortNoteMode.TITLE_Z_A.name -> filteredList.sortedByDescending { it.note.title.lowercase() }
            SortNoteMode.COLOR.name -> filteredList.sortedBy { it.note.color }
            else -> filteredList
        }
    }

    private fun reloadData() {
        noteAdapter.submitList(filterAndSortNotes(notes))
        (activity as MainActivity).currentSearchQuery?.let {
            EventBus.getDefault().post(SearchNoteEvent(it))
        }
    }

    private fun handleEvent() {
        binding.addButton.setOnClickListener {
            notesViewModel.insert(Note()) { lastNoteInsert ->
                val categoryId = (activity as MainActivity).navMenuItemIdSelected
                categoryId?.let { id ->
                    if (id != R.id.menu_uncategorized && id != R.id.menu_notes) {
                        notesViewModel.insertNoteCategoryCrossRef(
                            NoteCategoryCrossRef(
                                lastNoteInsert.noteId,
                                id.toLong()
                            )
                        )
                    }
                }
                launcherNoteEditorActivity(lastNoteInsert)
            }
        }

        noteAdapter.setOnItemClickListener(object : NoteAdapter.OnItemClickListener {
            override fun onItemClickListener(note: Note) {
                if(noteAdapter.isSelectedMode()) {
                    noteAdapter.toggleSelection(note)
                    (activity as MainActivity).openActionMode()
                    (activity as MainActivity).setTitleActionMode(noteAdapter.getSelectedNotesCount().toString())
                } else {
                    launcherNoteEditorActivity(note)
                }
            }

            override fun onItemLongClickListener(note: Note) {
                if(!noteAdapter.isSelectedMode()) {
                    binding.addButton.visibility = View.GONE
                    noteAdapter.toggleSelection(note)
                    noteAdapter.setSelectedMode(true)
                    (activity as MainActivity).openActionMode()
                    (activity as MainActivity).setTitleActionMode(noteAdapter.getSelectedNotesCount().toString())
                }
            }
        })
    }

    private fun launcherNoteEditorActivity(note: Note) {
        val intent = Intent(requireContext(), NoteEditorActivity::class.java)
        intent.putExtra(Constants.EXTRA_NOTE, note)
        startActivity(intent)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onLoadNotesEvent(event: LoadNotesEvent) {
        reloadData()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDeleteNoteEvent(event: DeleteNoteEvent) {
        val list = noteAdapter.getSelectedNotes()
        if(list.isNotEmpty()) {
            val builder = android.app.AlertDialog.Builder(requireContext())
            builder.apply {
                setMessage("Delete the selected notes?")
                setPositiveButton(R.string.ok) { _, _ ->
                    list.forEach {
                        notesViewModel.deleteNote(it)
                    }
                    Toast.makeText(requireContext(), "Delete notes (${list.size})", Toast.LENGTH_SHORT).show()
                    (activity as MainActivity).finishActionMode()
                }
                setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                    dialog.dismiss()
                }
            }

            val dialog = builder.create()
            dialog.show()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onClearSelectedNotes(event: ClearNotesSelectedEvent) {
        noteAdapter.clearSelectedNotes()
        noteAdapter.setSelectedMode(false)
        binding.addButton.visibility = View.VISIBLE
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSelectedAllNotes(event: SelectedAllNotesEvent) {
        if(noteAdapter.getSelectedNotes().size < noteAdapter.getNoteWithCategories().size) {
            noteAdapter.selectAllNotes()
        } else {
            noteAdapter.clearSelectedNotes()
        }
        noteAdapter.setSelectedMode(true)
        (activity as MainActivity).openActionMode()
        (activity as MainActivity).setTitleActionMode(noteAdapter.getSelectedNotesCount().toString())
        binding.addButton.visibility = View.GONE
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSearchNoteEvent(event: SearchNoteEvent) {
        Log.e(Constants.TAG, "onSearchNoteEvent: ", )
        val listSearch = filterAndSortNotes(notes).filter {
            it.note.title.lowercase().contains(event.query!!.lowercase(), true)
        }
        noteAdapter.submitList(listSearch)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onColorizeEvent(event: ColorizeNoteEvent) {
        val selectedNotes = noteAdapter.getSelectedNotes()
        if(selectedNotes.isNotEmpty()) {
            MaterialColorPickerDialog
                .Builder(requireContext())
                .setTitle("Select color")
                .setColorShape(ColorShape.SQAURE)
                .setColorSwatch(ColorSwatch._200)
                .setColors(resources.getStringArray(R.array.themeColorHex))
                .setColorListener { color, colorHex ->
                    selectedNotes.forEach { note ->
                        val noteUpdate = Note(
                            noteId = note.noteId,
                            title = note.title,
                            content = note.content,
                            color = colorHex,
                            dateCreate = note.dateCreate,
                        )
                        notesViewModel.updateNote(noteUpdate)
                    }
                    (activity as MainActivity).finishActionMode()
                }
                .show()
        }
    }


    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onResume() {
        super.onResume()
        reloadData()
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    companion object {
        fun newInstance() = NotesFragment()
    }
}
