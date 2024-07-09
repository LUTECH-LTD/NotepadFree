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
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModel
import com.github.dhaval2404.colorpicker.MaterialColorPickerDialog
import com.github.dhaval2404.colorpicker.model.ColorShape
import com.github.dhaval2404.colorpicker.model.ColorSwatch
import lutech.intern.noteapp.R
import lutech.intern.noteapp.adapter.CategorySelectedAdapter
import lutech.intern.noteapp.common.NoteApplication
import lutech.intern.noteapp.constant.Constants
import lutech.intern.noteapp.data.entity.Category
import lutech.intern.noteapp.data.entity.Note
import lutech.intern.noteapp.data.entity.NoteWithCategories
import lutech.intern.noteapp.data.entity.relations.NoteCategoryCrossRef
import lutech.intern.noteapp.databinding.ActivityNoteEditorBinding
import lutech.intern.noteapp.databinding.DialogSelectCategoryBinding
import lutech.intern.noteapp.ui.main.MainActivity
import lutech.intern.noteapp.ui.note.NotesFragment
import lutech.intern.noteapp.utils.DateTimeUtils
import lutech.intern.noteapp.utils.DrawableUtils
import lutech.intern.noteapp.utils.FileManager
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStreamWriter
import java.util.Stack
import kotlin.math.log

class NoteEditorActivity : AppCompatActivity() {
    private val binding by lazy { ActivityNoteEditorBinding.inflate(layoutInflater) }
    private val noteEditorViewModel: NoteEditorViewModel by viewModels()
    private var categories: List<Category> = emptyList()
    private val categorySelectedAdapter by lazy {
        CategorySelectedAdapter()
    }
    private val historyList = ArrayList<String>()
    private val openDocumentTreeLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.data?.let { uri ->
                    getNoteFormIntent()?.let {
                        FileManager(this).exportFileToFolder(
                            uri,
                            it.title,
                            it.content
                        )
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initViews()

        noteEditorViewModel.categoryWithNotes.observe(this) { categoryWithNotes ->
            val currentId = getNoteFormIntent()?.noteId
            categories = categoryWithNotes.map { it.category }
            val selectedCategories = categoryWithNotes.filter {
                it.notes.any { note -> note.noteId == currentId }
            }.map {
                it.category
            }

            categorySelectedAdapter.submitList(categories, selectedCategories)
        }

        historyList.add(0, binding.textEditText.text.toString())
        binding.textEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                s?.let {
                    if (historyList.contains(it.toString())) return
                    historyList.add(it.toString())
                    invalidateOptionsMenu()
                }
            }
        })
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

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        if (historyList.size <= 1) {
            val menuItem = menu?.findItem(R.id.menu_undo)
            menuItem?.isEnabled = false
            val spannable = SpannableString(menuItem?.title)
            spannable.setSpan(
                ForegroundColorSpan(
                    ContextCompat.getColor(
                        this,
                        R.color.color_menu_enabled
                    )
                ), 0, spannable.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
            menuItem?.title = spannable

            menu?.findItem(R.id.menu_undo_all)?.isEnabled = false
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
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
                true
            }

            R.id.menu_undo -> {
                if (historyList.size > 1) { // Kiểm tra nếu có ít nhất 2 phiên bản trong lịch sử
                    historyList.removeAt(historyList.size - 1) // Xóa phiên bản hiện tại khỏi danh sách
                    val lastVersion = historyList.last() // Lấy phiên bản trước đó
                    binding.textEditText.setText(lastVersion)
                    binding.textEditText.setSelection(lastVersion.length)
                }
                invalidateOptionsMenu()

                true
            }

            R.id.menu_redo -> {
                showToast("Redo clicked")
                true
            }

            R.id.menu_undo_all -> {
                val builder = AlertDialog.Builder(this)
                builder.apply {
                    setMessage("Remove all of the note changes made since the last opening of the note?")
                    setPositiveButton("Undo All") { _, _ ->
                        binding.textEditText.setText(historyList.first())
                        binding.textEditText.setSelection(historyList.first().length)
                        historyList.clear()
                        historyList.add(binding.textEditText.text.toString())
                        invalidateOptionsMenu()
                    }
                    setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                        dialog.dismiss()
                    }
                }

                val dialog = builder.create()
                dialog.show()

                true
            }

            R.id.menu_share -> {
                val currentNote = getNoteFormIntent()
                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, currentNote?.content)
                    type = "text/plain"
                }

                val shareIntent = Intent.createChooser(sendIntent, null)
                startActivity(shareIntent)
                true
            }

            R.id.menu_export -> {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                openDocumentTreeLauncher.launch(intent)
                true
            }

            R.id.menu_delete -> {
                val currentNote = getNoteFormIntent()
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
                return true
            }

            R.id.menu_categorize -> {
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
                                val currentId = getNoteFormIntent()?.noteId

                                if (isChecked) {
                                    noteEditorViewModel.insertNoteCategoryCrossRef(
                                        NoteCategoryCrossRef(
                                            currentId!!,
                                            categoryId
                                        )
                                    )
                                } else {
                                    noteEditorViewModel.deleteNoteCategoryCrossRef(
                                        currentId!!,
                                        categoryId
                                    )
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
                return true
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
                            Log.e(Constants.TAG, "showPopupMenu: $colorHex")
                            noteEditorViewModel.update(
                                Note(
                                    noteId = it.noteId,
                                    title = it.title,
                                    content = it.content,
                                    color = colorHex,
                                    dateCreate = it.dateCreate
                                )
                            )
                            if (color == ContextCompat.getColor(this, R.color.color_beige)) {
                                // default color
                                binding.main.setBackgroundColor(
                                    ContextCompat.getColor(
                                        this,
                                        R.color.color_beige_medium
                                    )
                                )
                                binding.toolbar.setBackgroundColor(
                                    ContextCompat.getColor(
                                        this,
                                        R.color.color_brown
                                    )
                                )
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
                return true
            }

            R.id.menu_switch_mode -> {
                showToast("Switch mode clicked")
                true
            }

            R.id.menu_print -> {
                printDocument("PhuHM")
                true
            }

            R.id.menu_show_formatting_bar -> {
                showToast("Show formatting bar clicked")
                true
            }

            R.id.menu_show_info -> {
                val currentNote = getNoteFormIntent()

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

                return true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun printDocument(textToPrint: String) {
        val printManager = getSystemService(Context.PRINT_SERVICE) as PrintManager

        val printAdapter = object : PrintDocumentAdapter() {
            override fun onLayout(attributes: PrintAttributes?, oldAttributes: PrintAttributes?, cancellationSignal: CancellationSignal?, callback: LayoutResultCallback?, extras: Bundle?) {
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

            override fun onWrite(pages: Array<out PageRange>?, destination: ParcelFileDescriptor?, cancellationSignal: CancellationSignal?, callback: WriteResultCallback?) {
                val input = textToPrint.toByteArray().inputStream()
                val output = FileOutputStream(destination?.fileDescriptor)
                input.copyTo(output)
            }
        }

        val printJob = printManager.print("JobName", printAdapter, null)
    }
}