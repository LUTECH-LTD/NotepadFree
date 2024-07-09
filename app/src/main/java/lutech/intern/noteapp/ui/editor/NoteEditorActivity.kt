package lutech.intern.noteapp.ui.editor

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.github.dhaval2404.colorpicker.MaterialColorPickerDialog
import com.github.dhaval2404.colorpicker.model.ColorShape
import com.github.dhaval2404.colorpicker.model.ColorSwatch
import lutech.intern.noteapp.R
import lutech.intern.noteapp.adapter.CategorySelectedAdapter
import lutech.intern.noteapp.constant.Constants
import lutech.intern.noteapp.data.entity.Category
import lutech.intern.noteapp.data.entity.Note
import lutech.intern.noteapp.data.entity.relations.NoteCategoryCrossRef
import lutech.intern.noteapp.databinding.ActivityNoteEditorBinding
import lutech.intern.noteapp.databinding.DialogSelectCategoryBinding
import lutech.intern.noteapp.utils.DateTimeUtils
import lutech.intern.noteapp.utils.DrawableUtils
import lutech.intern.noteapp.utils.FileManager
import java.io.FileOutputStream

class NoteEditorActivity : AppCompatActivity() {
    private val binding by lazy { ActivityNoteEditorBinding.inflate(layoutInflater) }
    private val noteEditorViewModel: NoteEditorViewModel by viewModels()
    private var categories: List<Category> = emptyList()
    private val categorySelectedAdapter by lazy { CategorySelectedAdapter() }
    private var currentNoteId: Long = 0L
    private var currentNote: Note? = null
    private val historyContent = ArrayList<String>()

    private val openDocumentTreeLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.data?.let { uri ->
                    currentNote?.let {
                        FileManager(this).exportFileToFolder(uri, it.title, it.content)
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        currentNoteId = intent.getLongExtra(Constants.EXTRA_NOTE_ID, 0L)
        initToolbar()
        observeDataViewModel()
        handleEvent()
    }

    private fun initToolbar() {
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun observeDataViewModel() {
        noteEditorViewModel.getNoteById(currentNoteId)
        noteEditorViewModel.note.observe(this) {
            currentNote = it
            currentNote?.let { note ->
                updateUI(note)
            }
        }

        noteEditorViewModel.categoryWithNotes.observe(this) { categoryWithNotes ->
            categories = categoryWithNotes.map { it.category }
            val selectedCategories = categoryWithNotes.filter {
                it.notes.any { note -> note.noteId == currentNote!!.noteId }
            }.map {
                it.category
            }

            categorySelectedAdapter.submitList(categories, selectedCategories)
        }
    }

    private fun updateUI(note: Note) {
        binding.titleEditText.setText(note.title)
        binding.textEditText.setText(note.content)
        if (Color.parseColor(note.color) == ContextCompat.getColor(this, R.color.color_beige)) {
            binding.main.setBackgroundColor(ContextCompat.getColor(this, R.color.color_beige_medium))
            binding.toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.color_brown))
            binding.layoutEdit.setBackgroundResource(R.drawable.bg_beige_radius)
        } else {
            binding.main.setBackgroundColor(DrawableUtils.darkenColor(Color.parseColor(note.color), 0.5f))
            binding.toolbar.setBackgroundColor(DrawableUtils.darkenColor(Color.parseColor(note.color), 0.5f))
            binding.layoutEdit.background = DrawableUtils.createSolidDrawable(this, note.color)
        }

        if (!historyContent.contains(note.content)) {
            historyContent.add(note.content)
            invalidateOptionsMenu()
        }
    }

    private fun handleEvent() {
        binding.textEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                s?.let {
                    if (historyContent.contains(it.toString())) return
                    historyContent.add(it.toString())
                    invalidateOptionsMenu()
                }
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_option_editor_note, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        if (historyContent.size <= 1) {
            val menuUndoItem = menu?.findItem(R.id.menu_undo)
            menuUndoItem?.isEnabled = false
            val spannable = SpannableString(menuUndoItem?.title)
            spannable.setSpan(
                ForegroundColorSpan(
                    ContextCompat.getColor(
                        this,
                        R.color.color_menu_enabled
                    )
                ), 0, spannable.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
            menuUndoItem?.title = spannable
            menu?.findItem(R.id.menu_undo_all)?.isEnabled = false
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_save -> {
                updateNote()
                true
            }

            R.id.menu_undo -> {
                undoNote()
                true
            }

            R.id.menu_redo -> {
                showToast("Redo clicked")
                true
            }

            R.id.menu_undo_all -> {
                undoAllNote()
                true
            }

            R.id.menu_share -> {
                shareNote()
                true
            }

            R.id.menu_export -> {
                exportNote()
                return true
            }

            R.id.menu_delete -> {
                deleteNote()
                return true
            }

            R.id.menu_categorize -> {
                updateCategoryNote()
                return true
            }

            R.id.menu_colorize -> {
                updateColorNote()
                return true
            }

            R.id.menu_switch_mode -> {
                showToast("Switch mode clicked")
                return true
            }

            R.id.menu_print -> {
                printDocument("")
                return true
            }

            R.id.menu_show_formatting_bar -> {
                showToast("Show formatting bar clicked")
                return true
            }

            R.id.menu_show_info -> {
                showInfoNote()
                return true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun updateNote() {
        val title = binding.titleEditText.text.toString().trim()
        val content = binding.textEditText.text.toString().trim()
        currentNote?.let {
            val note = Note(noteId = it.noteId, title = title, content = content, color = it.color, dateCreate = it.dateCreate)
            noteEditorViewModel.updateNote(note)
        }
    }

    private fun undoNote() {
        if (historyContent.size > 1) {
            historyContent.removeAt(historyContent.size - 1)
            val lastVersion = historyContent.last()
            binding.textEditText.setText(lastVersion)
            binding.textEditText.setSelection(lastVersion.length)
        }
        invalidateOptionsMenu()
    }

    private fun undoAllNote() {
        val builder = AlertDialog.Builder(this)
        builder.apply {
            setMessage("Remove all of the note changes made since the last opening of the note?")
            setPositiveButton("Undo All") { _, _ ->
                binding.textEditText.setText(historyContent.first())
                binding.textEditText.setSelection(historyContent.first().length)
                historyContent.clear()
                historyContent.add(binding.textEditText.text.toString())
                invalidateOptionsMenu()
            }
            setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun shareNote() {
        currentNote?.let {
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, it.content)
                type = "text/plain"
            }

            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)
        }
    }

    private fun exportNote() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        openDocumentTreeLauncher.launch(intent)
    }

    private fun deleteNote() {
        currentNote?.let {
            val builder = AlertDialog.Builder(this)
            builder.apply {
                setMessage("The '${it.title}' note will be deleted\nAre you sure")
                setPositiveButton(R.string.ok) { _, _ ->
                    noteEditorViewModel.deleteNote(it)
                    finish()
                }
                setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                    dialog.dismiss()
                }
            }

            val dialog = builder.create()
            dialog.show()
        }
    }

    private fun updateCategoryNote() {
        if (categories.isEmpty()) {
            val builder = AlertDialog.Builder(this)
            builder.apply {
                setMessage("Categories can be added in the app's menu. To open the menu use menu in the top left corner off the note list screen.")
                setPositiveButton(R.string.ok) { _, _ ->
                }
            }

            val dialog = builder.create()
            dialog.show()
        } else {
            val selectedCategoriesState: MutableMap<Long, Boolean> = mutableMapOf()
            categorySelectedAdapter.setOnCheckedChange { category, isChecked ->
                selectedCategoriesState[category.categoryId] = isChecked
            }

            val dialogBinding = DialogSelectCategoryBinding.inflate(layoutInflater)
            dialogBinding.categoriesRecyclerView.adapter = categorySelectedAdapter
            val builder = AlertDialog.Builder(this)
            builder.apply {
                setView(dialogBinding.root)
                setPositiveButton(R.string.ok) { _, _ ->
                    // Handel event
                    selectedCategoriesState.forEach { (categoryId, isChecked) ->
                        currentNote?.let {
                            if (isChecked) {
                                noteEditorViewModel.insertNoteCategoryCrossRef(
                                    NoteCategoryCrossRef(
                                        it.noteId,
                                        categoryId
                                    )
                                )
                            } else {
                                noteEditorViewModel.deleteNoteCategoryCrossRef(
                                    it.noteId,
                                    categoryId
                                )
                            }
                        }
                    }
                    Toast.makeText(this@NoteEditorActivity, "Update success", Toast.LENGTH_SHORT).show()
                }
                setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                    dialog.dismiss()
                }
            }

            val dialog = builder.create()
            dialog.show()
        }
    }

    private fun updateColorNote() {
        currentNote?.let {
            MaterialColorPickerDialog
                .Builder(this)
                .setColorShape(ColorShape.SQAURE)
                .setColorSwatch(ColorSwatch._200)
                .setColors(resources.getStringArray(R.array.themeColorHex))
                .setDefaultColor(it.color)
                .setColorListener { _, colorHex ->
                    val note = Note(
                        noteId = it.noteId,
                        title = it.title,
                        content = it.content,
                        color = colorHex,
                        dateCreate = it.dateCreate
                    )
                    noteEditorViewModel.updateNote(note)
                }
                .show()
        }
    }
    private fun printDocument(textToPrint: String) {
        val printManager = getSystemService(Context.PRINT_SERVICE) as PrintManager

        val printAdapter = object : PrintDocumentAdapter() {
            override fun onLayout(
                attributes: PrintAttributes?,
                oldAttributes: PrintAttributes?,
                cancellationSignal: CancellationSignal?,
                callback: LayoutResultCallback?,
                extras: Bundle?,
            ) {
                if (cancellationSignal?.isCanceled == true) {
                    callback?.onLayoutCancelled()
                    return
                }

                val builder = PrintDocumentInfo.Builder("file_name")
                builder.setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(1)
                    .build()

                callback?.onLayoutFinished(builder.build(), true)
            }

            override fun onWrite(
                pages: Array<out PageRange>?,
                destination: ParcelFileDescriptor?,
                cancellationSignal: CancellationSignal?,
                callback: WriteResultCallback?,
            ) {
                val input = textToPrint.toByteArray().inputStream()
                val output = FileOutputStream(destination?.fileDescriptor)
                input.copyTo(output)
            }
        }

        val printJob = printManager.print("JobName", printAdapter, null)
    }
    private fun showInfoNote() {
        currentNote?.let { note ->
            val wordCount = note.content.trim().split("\\s+".toRegex()).size
            val nonSpaceCharCount = note.content.replace(" ", "").length
            val lineCount = note.content.split("\n").size

            val message = """
                                    Words: $wordCount
                                    Wrapped lines: $lineCount
                                    Characters: ${note.content.length}
                                    Characters without whitespaces: $nonSpaceCharCount
                                    Created at: ${DateTimeUtils.getFormattedDateTime(note.dateCreate)}
                                    Last saved at: ${DateTimeUtils.getFormattedDateTime(note.lastUpdate)}
                                """.trimIndent()

            AlertDialog.Builder(this).apply {
                setMessage(message)
                setPositiveButton(R.string.ok) { dialog, _ ->
                    dialog.dismiss()
                }
            }.create().show()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}