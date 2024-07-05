package lutech.intern.noteapp.ui.note

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import lutech.intern.noteapp.R
import lutech.intern.noteapp.adapter.NoteAdapter
import lutech.intern.noteapp.constant.Constants
import lutech.intern.noteapp.constant.SortOption
import lutech.intern.noteapp.data.entity.Note
import lutech.intern.noteapp.data.entity.NoteWithCategories
import lutech.intern.noteapp.data.entity.relations.NoteCategoryCrossRef
import lutech.intern.noteapp.databinding.FragmentNotesBinding
import lutech.intern.noteapp.event.SearchEvent
import lutech.intern.noteapp.event.SortEvent
import lutech.intern.noteapp.ui.editor.NoteEditorActivity
import lutech.intern.noteapp.ui.main.MainActivity
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class NotesFragment : Fragment() {
    private val binding by lazy { FragmentNotesBinding.inflate(layoutInflater) }
    private val notesViewModel: NotesViewModel by viewModels()
    private val noteAdapter by lazy { NoteAdapter() }
    private var listData: List<NoteWithCategories> = emptyList()

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
            (activity as MainActivity).getSelectedMenuItemId()?.let { idMenu ->
                val listFilter = when (idMenu) {
                    R.id.menu_notes -> list
                    R.id.menu_uncategorized -> list.filter { it.categories.isEmpty() }
                    else -> list.filter { itemList ->
                        itemList.categories.any {
                            it.categoryId.toInt() == idMenu
                        }
                    }
                }
                listData = listFilter
                noteAdapter.submitList(listFilter, null)
            }
        }

        notesViewModel.lastInsertedNote.observe(viewLifecycleOwner) { note ->
            (activity as MainActivity).getSelectedMenuItemId()?.let { categoryId ->
                if (categoryId != R.id.menu_uncategorized && categoryId != R.id.menu_notes) {
                    notesViewModel.insertNoteCategoryCrossRef(
                        NoteCategoryCrossRef(
                            note.noteId,
                            categoryId.toLong()
                        )
                    )
                }
                val intent = Intent(requireContext(), NoteEditorActivity::class.java)
                intent.putExtra(Constants.EXTRA_NOTE, note)
                startActivity(intent)
            }
        }
    }

    private fun handleEvent() {
        binding.addButton.setOnClickListener {
            notesViewModel.insert(Note(title = "", content = ""))
        }

        noteAdapter.setOnItemClickListener(object : NoteAdapter.OnItemClickListener {
            override fun onItemClickListener(note: Note) {
                val intent = Intent(requireContext(), NoteEditorActivity::class.java)
                intent.putExtra(Constants.EXTRA_NOTE, note)
                startActivity(intent)
            }
        })
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSearchEvent(event: SearchEvent) {
        val searchList = listData.filter {
            it.note.title!!.lowercase().contains(event.query.lowercase())
        }
        noteAdapter.submitList(searchList, null)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSortOptionEvent(event: SortEvent) {
        sortList(event.sortOption)
    }

    private fun sortList(sortOption: String) {
        val sortedList = when (sortOption) {
            SortOption.EDIT_DATE_NEWEST.toString() -> {
                listData.sortedByDescending { it.note.lastUpdate }
            }

            SortOption.EDIT_DATE_OLDEST.toString() -> {
                listData.sortedBy { it.note.lastUpdate }
            }

            SortOption.CREATION_DATE_NEWEST.toString() -> {
                listData.sortedByDescending { it.note.dateCreate }
            }

            SortOption.CREATION_DATE_OLDEST.toString() -> {
                listData.sortedBy { it.note.dateCreate }
            }

            SortOption.TITLE_A_Z.toString() -> {
                listData.sortedBy { it.note.title?.lowercase() }
            }

            SortOption.TITLE_Z_A.toString() -> {
                listData.sortedByDescending { it.note.title?.lowercase() }
            }

            SortOption.COLOR.toString() -> {
                listData.sortedBy { it.note.color }
            }

            else -> {
                listData
            }
        }
        noteAdapter.submitList(sortedList, sortOption)
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    companion object {
        fun newInstance() = NotesFragment()
    }
}