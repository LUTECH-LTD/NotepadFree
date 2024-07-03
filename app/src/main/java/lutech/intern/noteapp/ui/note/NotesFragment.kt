package lutech.intern.noteapp.ui.note

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import lutech.intern.noteapp.adapter.NoteAdapter
import lutech.intern.noteapp.data.model.Note
import lutech.intern.noteapp.databinding.FragmentNotesBinding
import lutech.intern.noteapp.ui.NoteEditorActivity

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
        notesViewModel.notes.observe(viewLifecycleOwner) {
            noteAdapter.submitNotes(it)
        }
    }

    private fun handleEvent() {
        binding.addButton.setOnClickListener {
            notesViewModel.insert(Note())
        }
    }

    companion object {
        fun newInstance() = NotesFragment()
    }
}