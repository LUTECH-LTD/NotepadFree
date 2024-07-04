package lutech.intern.noteapp.ui.note

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import lutech.intern.noteapp.R
import lutech.intern.noteapp.adapter.NoteAdapter
import lutech.intern.noteapp.constant.Constants
import lutech.intern.noteapp.data.entity.Note
import lutech.intern.noteapp.data.entity.relations.NoteCategoryCrossRef
import lutech.intern.noteapp.databinding.FragmentNotesBinding
import lutech.intern.noteapp.ui.editor.NoteEditorActivity
import lutech.intern.noteapp.ui.main.MainActivity

class NotesFragment : Fragment() {
    private val binding by lazy { FragmentNotesBinding.inflate(layoutInflater) }
    private val notesViewModel: NotesViewModel by viewModels()
    private val noteAdapter by lazy { NoteAdapter() }

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
                noteAdapter.submitList(listFilter)
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
//            notesViewModel.insert(Note(title = "Title", color = "#75A47F"))
//            notesViewModel.insert(Note(title = "Title", color = "#E68369"))
//            notesViewModel.insert(Note(title = "Title", color = "#508D4E"))
//            notesViewModel.insert(Note(title = "Title", color = "#FCF8F3"))
//            notesViewModel.insert(Note(title = "Title", color = "#96C9F4"))
//            notesViewModel.insert(Note(title = "Title", color = "#E9C46A"))
//            notesViewModel.insert(Note(title = "Title", color = "#FFB4C2"))
        }

        noteAdapter.setOnItemClickListener(object : NoteAdapter.OnItemClickListener {
            override fun onItemClickListener(note: Note) {
                val intent = Intent(requireContext(), NoteEditorActivity::class.java)
                intent.putExtra(Constants.EXTRA_NOTE, note)
                startActivity(intent)
            }
        })
    }

    companion object {
        fun newInstance() = NotesFragment()
    }
}