package lutech.intern.noteapp.ui.editor

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.github.dhaval2404.colorpicker.MaterialColorPickerDialog
import com.github.dhaval2404.colorpicker.model.ColorShape
import com.github.dhaval2404.colorpicker.model.ColorSwatch
import lutech.intern.noteapp.R
import lutech.intern.noteapp.common.NoteApplication
import lutech.intern.noteapp.constant.Constants
import lutech.intern.noteapp.data.entity.Note
import lutech.intern.noteapp.databinding.ActivityNoteEditorBinding
import lutech.intern.noteapp.ui.note.NotesFragment
import lutech.intern.noteapp.utils.DrawableUtils
import kotlin.math.log

class NoteEditorActivity : AppCompatActivity() {
    private val binding by lazy { ActivityNoteEditorBinding.inflate(layoutInflater) }
    private val noteEditorViewModel: NoteEditorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initViews()
    }

    private fun initViews() {
        initToolbar()
        initViewEditText()
    }

    private fun initToolbar() {
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun initViewEditText() {
        val note = getNoteFormIntent()
        note?.let {
            binding.titleEditText.setText(it.title)
            binding.textEditText.setText(it.content)
            if (Color.parseColor(it.color) == ContextCompat.getColor(this, R.color.color_beige)) {
                Log.e(Constants.TAG, "Color default")
            } else {
                binding.main.setBackgroundColor(
                    DrawableUtils.darkenColor(
                        Color.parseColor(it.color),
                        0.5f
                    )
                )
                binding.toolbar.setBackgroundColor(
                    DrawableUtils.darkenColor(
                        Color.parseColor(it.color),
                        0.5f
                    )
                )
                binding.layoutEdit.background = DrawableUtils.createSolidDrawable(this, it.color)
            }
        }
    }

    private fun getNoteFormIntent(): Note? {
        return intent.getSerializableExtra(Constants.EXTRA_NOTE) as? Note
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_option_editor_note, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_save -> {
                val title = binding.titleEditText.text.toString().trim()
                val content = binding.textEditText.text.toString().trim()
                val currentNote = getNoteFormIntent()
                currentNote?.let {
                    noteEditorViewModel.update(
                        Note(
                            noteId = it.noteId,
                            title = title,
                            content = content,
                            color = it.color,
                            dateCreate = it.dateCreate
                        )
                    )
                }
                return true
            }

            R.id.menu_more -> {
                showPopupMenu(findViewById(item.itemId))
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.inflate(R.menu.menu_popup_editor_note) // Tạo một menu mới cho PopupMenu
        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_categorize -> {
                    Toast.makeText(this, "menu_categorize", Toast.LENGTH_SHORT).show()
                    true
                }

                R.id.menu_colorize -> {
                    val note = getNoteFormIntent()
                    note?.let {
                        MaterialColorPickerDialog
                            .Builder(this)
                            .setTitle("Select color")
                            .setColorShape(ColorShape.SQAURE)
                            .setColorSwatch(ColorSwatch._200)
                            .setColors(resources.getStringArray(R.array.themeColorHex))
                            .setDefaultColor(it.color)
                            .setColorListener { color, colorHex ->
                                Log.e(Constants.TAG, "showPopupMenu: $colorHex", )
                                noteEditorViewModel.update(
                                    Note(
                                        noteId = it.noteId,
                                        title = it.title,
                                        content = it.content,
                                        color = colorHex,
                                        dateCreate = it.dateCreate
                                    )
                                )
                                if(color == ContextCompat.getColor(this, R.color.color_beige)) {
                                    // default color
                                    binding.main.setBackgroundColor(ContextCompat.getColor(this, R.color.color_beige_medium))
                                    binding.toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.color_brown))
                                    binding.layoutEdit.setBackgroundResource(R.drawable.bg_beige_radius)
                                } else {
                                    // Update viewColor
                                    binding.main.setBackgroundColor(
                                        DrawableUtils.darkenColor(
                                            Color.parseColor(colorHex),
                                            0.5f
                                        )
                                    )
                                    binding.toolbar.setBackgroundColor(
                                        DrawableUtils.darkenColor(
                                            Color.parseColor(colorHex),
                                            0.5f
                                        )
                                    )
                                    binding.layoutEdit.background =
                                        DrawableUtils.createSolidDrawable(this, colorHex)
                                }

                            }
                            .show()
                    }
                    true
                }

                else -> false
            }
        }
        popupMenu.show()
    }
}