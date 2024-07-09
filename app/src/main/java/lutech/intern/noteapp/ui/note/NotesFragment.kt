package lutech.intern.noteapp.ui.note

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.github.dhaval2404.colorpicker.MaterialColorPickerDialog
import com.github.dhaval2404.colorpicker.model.ColorShape
import com.github.dhaval2404.colorpicker.model.ColorSwatch
import lutech.intern.noteapp.R
import lutech.intern.noteapp.adapter.CategorySelectionAdapter
import lutech.intern.noteapp.adapter.NoteAdapter
import lutech.intern.noteapp.common.PreferencesManager
import lutech.intern.noteapp.constant.Constants
import lutech.intern.noteapp.constant.SortNoteMode
import lutech.intern.noteapp.data.entity.Category
import lutech.intern.noteapp.data.entity.Note
import lutech.intern.noteapp.data.entity.NoteWithCategories
import lutech.intern.noteapp.data.entity.relations.NoteCategoryCrossRef
import lutech.intern.noteapp.databinding.DialogSelectCategoryBinding
import lutech.intern.noteapp.databinding.FragmentNotesBinding
import lutech.intern.noteapp.event.Event
import lutech.intern.noteapp.ui.editor.NoteEditorActivity
import lutech.intern.noteapp.ui.main.MainActivity
import lutech.intern.noteapp.utils.FileManager
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class NotesFragment : Fragment() {
    private val binding by lazy { FragmentNotesBinding.inflate(layoutInflater) }
    private val notesViewModel: NotesViewModel by viewModels()
    private val noteAdapter by lazy { NoteAdapter(requireContext()) }
    private var noteWithCategories: List<NoteWithCategories> = emptyList()
    private val categorySelectionAdapter by lazy { CategorySelectionAdapter() }
    private lateinit var mainActivity: MainActivity

    private val openDocumentTreeLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    // Export file vào máy
                    if (!noteAdapter.isSelectedMode()) {
                        noteAdapter.getNoteWithCategories().forEach {
                            FileManager(requireContext()).exportFileToFolder(
                                uri,
                                it.note.title,
                                it.note.content
                            )
                        }
                    } else {
                        if (noteAdapter.getSelectedNotes().isNotEmpty()) {
                            noteAdapter.getSelectedNotes().forEach {
                                FileManager(requireContext()).exportFileToFolder(
                                    uri,
                                    it.title,
                                    it.content
                                )
                            }
                        }
                    }
                    mainActivity.finishActionMode()
                }
            }
        }

    private val openDocumentLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    val note = FileManager(requireContext()).importFileFromFolder(uri)
                    note?.let {
                        notesViewModel.insertNote(note) {}
                    }
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        mainActivity = (activity as MainActivity)
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
        binding.rcvNotes.adapter = noteAdapter
    }

    private fun observeDataViewModel() {
        notesViewModel.noteWithCategories.observe(viewLifecycleOwner) { list ->
            noteWithCategories = list
            reloadData()
        }

        notesViewModel.categories.observe(viewLifecycleOwner) { list ->
            categorySelectionAdapter.submitList(list)
        }
    }

    private fun filterAndSortNotes(noteWithCategories: List<NoteWithCategories>): List<NoteWithCategories> {
        val menuItemId =
            (activity as MainActivity).navMenuItemIdSelected ?: return noteWithCategories

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
        noteAdapter.submitList(filterAndSortNotes(noteWithCategories))
        mainActivity.currentSearchQuery?.let {
            EventBus.getDefault().post(Event.SearchNotesEvent(it))
        }
    }

    private fun handleEvent() {
        binding.btnAdd.setOnClickListener {
            notesViewModel.insertNote(Note()) { lastNoteInsert ->
                mainActivity.navMenuItemIdSelected?.let { categoryId ->
                    if (categoryId != R.id.menu_uncategorized && categoryId != R.id.menu_notes) {
                        notesViewModel.insertNoteCategoryCrossRef(
                            NoteCategoryCrossRef(
                                lastNoteInsert.noteId,
                                categoryId.toLong()
                            )
                        )
                    }
                }
                launcherNoteEditorActivity(lastNoteInsert)
            }
        }

        noteAdapter.setOnItemClickListener(object : NoteAdapter.OnItemClickListener {
            override fun onItemClickListener(note: Note) {
                if (noteAdapter.isSelectedMode()) {
                    noteAdapter.toggleSelection(note)
                    mainActivity.setTitleActionMode(noteAdapter.getSelectedNotesCount().toString())
                } else {
                    launcherNoteEditorActivity(note)
                }
            }

            override fun onItemLongClickListener(note: Note) {
                if (!noteAdapter.isSelectedMode()) {
                    noteAdapter.toggleSelection(note)
                    noteAdapter.setSelectedMode(true)
                    mainActivity.openActionMode()
                    mainActivity.setTitleActionMode(noteAdapter.getSelectedNotesCount().toString())
                    binding.btnAdd.visibility = View.GONE
                }
            }
        })
    }

    private fun launcherNoteEditorActivity(note: Note) {
        val intent = Intent(requireContext(), NoteEditorActivity::class.java)
        intent.putExtra(Constants.EXTRA_NOTE_ID, note.noteId)
        startActivity(intent)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: Event) {
        when (event) {
            is Event.LoadNotesEvent -> {
                reloadData()
            }

            is Event.SearchNotesEvent -> {
                searchNotes(noteWithCategories, event.query ?: "")
            }

            is Event.SelectAllNotesEvent -> {
                selectAllNotes()
            }

            is Event.DeleteNotesEvent -> {
                showDeleteNotesDialog()
            }

            is Event.ClearSelectedNotesEvent -> {
                clearSelectedNotes()
            }

            is Event.ChangeCategoryNotesEvent -> {
                showPickerCategoryDialog()
            }

            is Event.ChangeColorNotesEvent -> {
                showColorPickerDialog()
            }

            is Event.ExportNotesEvent -> {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                openDocumentTreeLauncher.launch(intent)
            }

            is Event.ImportNotesEvent -> {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "text/plain"
                }
                openDocumentLauncher.launch(intent)
            }
        }
    }

    private fun searchNotes(notes: List<NoteWithCategories>, query: String) {
        val searchNotes = filterAndSortNotes(notes).filter {
            noteAdapter.setSearchKeyword(query)
            it.note.title.lowercase().contains(query.lowercase())
        }
        noteAdapter.submitList(searchNotes)
    }

    private fun selectAllNotes() {
        if (noteAdapter.getSelectedNotes().size < noteAdapter.getNoteWithCategories().size) {
            noteAdapter.selectAllNotes()
        } else {
            noteAdapter.clearSelectedNotes()
        }
        noteAdapter.setSelectedMode(true)
        mainActivity.openActionMode()
        mainActivity.setTitleActionMode(noteAdapter.getSelectedNotesCount().toString())
        binding.btnAdd.visibility = View.GONE
    }

    private fun showDeleteNotesDialog() {
        val selectedCategories = noteAdapter.getSelectedNotes()
        if (selectedCategories.isNotEmpty()) {
            val builder = AlertDialog.Builder(requireContext())
            builder.apply {
                setMessage("Delete the selected notes?")
                setPositiveButton(R.string.ok) { _, _ ->
                    deleteNotes(selectedCategories)
                }
                setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                    dialog.dismiss()
                }
            }

            val dialog = builder.create()
            dialog.show()
        }
    }

    private fun deleteNotes(list: List<Note>) {
        list.forEach { notesViewModel.deleteNote(it) }
        Toast.makeText(requireContext(), "Delete notes (${list.size})", Toast.LENGTH_SHORT).show()
        mainActivity.finishActionMode()
    }

    private fun clearSelectedNotes() {
        noteAdapter.clearSelectedNotes()
        noteAdapter.setSelectedMode(false)
        binding.btnAdd.visibility = View.VISIBLE
    }

    private fun showColorPickerDialog() {
        val selectedNotes = noteAdapter.getSelectedNotes()
        if (selectedNotes.isNotEmpty()) {
            MaterialColorPickerDialog
                .Builder(requireContext())
                .setTitle("Select color")
                .setColorShape(ColorShape.SQAURE)
                .setColorSwatch(ColorSwatch._200)
                .setColors(resources.getStringArray(R.array.themeColorHex))
                .setColorListener { _, colorHex ->
                    updateColorNotes(selectedNotes, colorHex)
                }
                .show()
        }
    }

    private fun updateColorNotes(selectedNotes: List<Note>, colorHex: String) {
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
        mainActivity.finishActionMode()
    }

    private fun showPickerCategoryDialog() {
        val selectedCategories = categorySelectionAdapter.getSelectedCategories()
        val selectedNotes = noteAdapter.getSelectedNotes()
        if (selectedNotes.isNotEmpty()) {
            if (categorySelectionAdapter.getCategories().isEmpty()) {
                val builder = AlertDialog.Builder(requireContext())
                builder.apply {
                    setMessage("Categories can be added in the app's menu. To open the menu use menu in the top left corner off the note list screen.")
                    setPositiveButton(R.string.ok, null)
                }

                val dialog = builder.create()
                dialog.show()
                return
            }

            val dialogBinding = DialogSelectCategoryBinding.inflate(layoutInflater)
            dialogBinding.categoriesRecyclerView.adapter = categorySelectionAdapter
            val builder = AlertDialog.Builder(requireContext())
            builder.apply {
                setView(dialogBinding.root)
                setPositiveButton(R.string.ok) { _, _ ->
                    updateCategoryNotes(selectedCategories, selectedNotes)
                }
            }

            val dialog = builder.create()
            dialog.show()

            dialog.setOnDismissListener {
                categorySelectionAdapter.clearSelectedCategories()
            }
        }
    }

    private fun updateCategoryNotes(selectedCategories: List<Category>, selectedNotes: List<Note>) {
        if (selectedCategories.isNotEmpty()) {
            selectedNotes.forEach { note ->
                selectedCategories.forEach { category ->
                    notesViewModel.insertNoteCategoryCrossRef(
                        NoteCategoryCrossRef(
                            noteId = note.noteId,
                            categoryId = category.categoryId
                        )
                    )
                }
            }
        }
        mainActivity.finishActionMode()
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
