package lutech.intern.noteapp.ui.note

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import lutech.intern.noteapp.R
import lutech.intern.noteapp.adapter.NoteAdapter
import lutech.intern.noteapp.data.entity.Note
import lutech.intern.noteapp.databinding.FragmentNotesBinding
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
    }

    private fun handleEvent() {
        binding.addButton.setOnClickListener {
            notesViewModel.insert(Note(title = "E", content = "MyNote"))
//            notesViewModel.insert(Note(title = "B", content = "MyNote", color = "#BBE9FF"))
//            notesViewModel.insert(Note(title = "D", content = "MyNote", color = "#97BE5A"))
//            notesViewModel.insert(Note(title = "C", content = "MyNote", color = "#FFA27F"))
//            notesViewModel.insert(Note(title = "F", content = "MyNote", color = "#BACD92"))
//            notesViewModel.insert(Note(title = "A", content = "MyNote", color = "#A5DD9B"))
        }
    }

    companion object {
        fun newInstance() = NotesFragment()
    }
}