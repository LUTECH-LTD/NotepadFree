package lutech.intern.noteapp.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import lutech.intern.noteapp.databinding.ActivityNoteEditorBinding

class NoteEditorActivity : AppCompatActivity() {
    private val binding by lazy { ActivityNoteEditorBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initViews()
    }

    private fun initViews() {
        initToolbar()
    }

    private fun initToolbar() {
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }
}