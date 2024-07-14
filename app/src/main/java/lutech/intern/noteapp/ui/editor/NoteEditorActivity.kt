package lutech.intern.noteapp.ui.editor

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.print.PrintManager
import android.text.Spannable
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.forEach
import androidx.lifecycle.lifecycleScope
import com.github.dhaval2404.colorpicker.MaterialColorPickerDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import lutech.intern.noteapp.R
import lutech.intern.noteapp.constant.Constants
import lutech.intern.noteapp.data.entity.Note
import lutech.intern.noteapp.data.entity.TextStyles
import lutech.intern.noteapp.databinding.ActivityNoteEditorBinding
import lutech.intern.noteapp.ui.editor.adapter.PrintNoteAdapter
import lutech.intern.noteapp.utils.DateTimeUtils
import lutech.intern.noteapp.utils.DrawableUtils

class NoteEditorActivity : AppCompatActivity() {
    private val binding by lazy { ActivityNoteEditorBinding.inflate(layoutInflater) }
    private val viewModel: NoteEditorViewModel by viewModels()
    private val currentNoteId by lazy { intent.getLongExtra(Constants.EXTRA_NOTE_ID, 0L) }
    private var currentNote: Note? = null
    private var isEditorMode = true
    private var isShowFormattingBar = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initViews()
        initEvents()
        initObservers()
    }

    private fun initViews() {
        Log.d(Constants.TAG, "initViews")
        initToolbar()
        initLayoutMode()
        initFormattingBar()
    }

    private fun initToolbar() {
        Log.d(Constants.TAG, "initToolbar")
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun initLayoutMode() {
        Log.d(Constants.TAG, "initLayoutMode")
        if (isEditorMode) {
            binding.layoutEditorMode.visibility = View.VISIBLE
            binding.layoutReadMode.visibility = View.GONE
        } else {
            binding.layoutEditorMode.visibility = View.GONE
            binding.layoutReadMode.visibility = View.VISIBLE
        }
    }

    private fun initFormattingBar() {
        Log.d(Constants.TAG, "initFormattingBar")
        binding.formattingBar.visibility = if (isShowFormattingBar) View.VISIBLE else View.GONE
    }

    private fun initEvents() {
        Log.d(Constants.TAG, "initEvents")
        binding.layoutReadMode.setOnClickListener {
            handleSwitchReadMode()
        }

        binding.btnHideFormattingBar.setOnClickListener {
            handleShowFormattingBar()
        }

        binding.cbFontBold.setOnCheckedChangeListener { _, isChecked ->
            val spannable = binding.contentEditText.text
            val start = binding.contentEditText.selectionStart
            val end = binding.contentEditText.selectionEnd
            if (isChecked) {
                spannable?.setSpan(StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            } else {
                val spans = spannable?.getSpans(start, end, StyleSpan::class.java)
                spans?.forEach { span ->
                    if (span.style == Typeface.BOLD) {
                        val spanStart = spannable.getSpanStart(span)
                        val spanEnd = spannable.getSpanEnd(span)
                        if (spanStart >= start && spanEnd <= end) {
                            spannable.removeSpan(span)
                        } else {
                            spannable.removeSpan(span)
                            if (spanStart < start) {
                                spannable.setSpan(StyleSpan(span.style), spanStart, start, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                            }
                            if (spanEnd > end) {
                                spannable.setSpan(StyleSpan(span.style), end, spanEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                            }
                        }
                    }
                }
            }
        }

        binding.cbFontItalic.setOnCheckedChangeListener { _, isChecked ->
            val spannable = binding.contentEditText.text
            val start = binding.contentEditText.selectionStart
            val end = binding.contentEditText.selectionEnd
            if (isChecked) {
                spannable?.setSpan(StyleSpan(Typeface.ITALIC), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            } else {
                val spans = spannable?.getSpans(start, end, StyleSpan::class.java)
                spans?.forEach { span ->
                    if (span.style == Typeface.ITALIC) {
                        val spanStart = spannable.getSpanStart(span)
                        val spanEnd = spannable.getSpanEnd(span)
                        if (spanStart >= start && spanEnd <= end) {
                            spannable.removeSpan(span)
                        } else {
                            spannable.removeSpan(span)
                            if (spanStart < start) {
                                spannable.setSpan(StyleSpan(span.style), spanStart, start, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                            }
                            if (spanEnd > end) {
                                spannable.setSpan(StyleSpan(span.style), end, spanEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                            }
                        }
                    }
                }
            }
        }

        binding.cbFontUnderline.setOnCheckedChangeListener { _, isChecked ->
            val spannable = binding.contentEditText.text
            val start = binding.contentEditText.selectionStart
            val end = binding.contentEditText.selectionEnd
            if (isChecked) {
                spannable?.setSpan(UnderlineSpan(), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            } else {
                val spans = spannable?.getSpans(start, end, UnderlineSpan::class.java)
                spans?.forEach { span ->
                    val spanStart = spannable.getSpanStart(span)
                    val spanEnd = spannable.getSpanEnd(span)
                    if (spanStart >= start && spanEnd <= end) {
                        spannable.removeSpan(span)
                    } else {
                        spannable.removeSpan(span)
                        if (spanStart < start) {
                            spannable.setSpan(UnderlineSpan(), spanStart, start, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        }
                        if (spanEnd > end) {
                            spannable.setSpan(UnderlineSpan(), end, spanEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        }
                    }
                }
            }
        }

        binding.cbFontStrikethrough.setOnCheckedChangeListener { _, isChecked ->
            val spannable = binding.contentEditText.text
            val start = binding.contentEditText.selectionStart
            val end = binding.contentEditText.selectionEnd
            if (isChecked) {
                spannable?.setSpan(StrikethroughSpan(), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            } else {
                val spans = spannable?.getSpans(start, end, StrikethroughSpan::class.java)
                spans?.forEach { span ->
                    val spanStart = spannable.getSpanStart(span)
                    val spanEnd = spannable.getSpanEnd(span)
                    if (spanStart >= start && spanEnd <= end) {
                        spannable.removeSpan(span)
                    } else {
                        spannable.removeSpan(span)
                        if (spanStart < start) {
                            spannable.setSpan(StrikethroughSpan(), spanStart, start, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        }
                        if (spanEnd > end) {
                            spannable.setSpan(StrikethroughSpan(), end, spanEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        }
                    }
                }
            }
        }

        binding.cbFontColor.setOnClickListener {
            MaterialColorPickerDialog
                .Builder(this)
                .setColors(resources.getStringArray(R.array.themeColorText))
                .setColorListener { _, colorHex ->
                    val spannable = binding.contentEditText.text
                    val start = binding.contentEditText.selectionStart
                    val end = binding.contentEditText.selectionEnd
                    if (start != -1 && end != -1) {
                        spannable?.setSpan(ForegroundColorSpan(Color.parseColor(colorHex)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                }
                .show()
        }

        binding.cbFontHighlight.setOnClickListener {
            MaterialColorPickerDialog
                .Builder(this)
                .setColors(resources.getStringArray(R.array.themeColorHex))
                .setColorListener { _, colorHex ->
                    val spannable = binding.contentEditText.text
                    val start = binding.contentEditText.selectionStart
                    val end = binding.contentEditText.selectionEnd
                    if (start != -1 && end != -1) {
                        spannable?.setSpan(BackgroundColorSpan(Color.parseColor(colorHex)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                }
                .show()
        }

    }

    private fun initObservers() {
        Log.d(Constants.TAG, "initObservers")
        viewModel.getNoteWithTextStylesById(currentNoteId)
        viewModel.noteWithTextStyles.observe(this) { noteWithTextStyles ->
            Log.d(Constants.TAG, "initObservers: noteWithTextStyles")
            currentNote = noteWithTextStyles.note

            currentNote?.let { note ->
                val spannable = SpannableString(note.content)
                val listTextStyles = noteWithTextStyles.textStyles

                listTextStyles.forEach { textStyle ->
                    val start = textStyle.start
                    val end = textStyle.end

                    if (textStyle.isBold) {
                        spannable.setSpan(StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }

                    if (textStyle.isItalic) {
                        spannable.setSpan(StyleSpan(Typeface.ITALIC), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }

                    if (textStyle.isUnderline) {
                        spannable.setSpan(UnderlineSpan(), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }

                    if (textStyle.isStrikethrough) {
                        spannable.setSpan(StrikethroughSpan(), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }

                    if (textStyle.color != null) {
                        spannable.setSpan(ForegroundColorSpan(Color.parseColor(textStyle.color)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }

                    if (textStyle.highlight != null) {
                        spannable.setSpan(BackgroundColorSpan(Color.parseColor(textStyle.highlight)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                }

                binding.titleEditText.setText(note.title)
                binding.tvTitle.text = binding.titleEditText.text
                binding.contentEditText.setText(spannable)
                binding.tvContent.text = spannable

                if (Color.parseColor(note.color) == ContextCompat.getColor(this, R.color.color_beige)) {
                    binding.main.setBackgroundColor(ContextCompat.getColor(this, R.color.color_beige_medium))
                    binding.toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.color_brown))
                    binding.layoutEditorMode.setBackgroundResource(R.drawable.bg_beige_radius)
                    binding.layoutReadMode.setBackgroundResource(R.drawable.bg_beige_radius)
                } else {
                    binding.main.setBackgroundColor(DrawableUtils.darkenColor(Color.parseColor(note.color), 0.5f))
                    binding.toolbar.setBackgroundColor(DrawableUtils.darkenColor(Color.parseColor(note.color), 0.5f))
                    binding.layoutEditorMode.background = DrawableUtils.createSolidDrawable(this, note.color)
                    binding.layoutReadMode.background = DrawableUtils.createSolidDrawable(this, note.color)
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        Log.d(Constants.TAG, "onCreateOptionsMenu")
        val menuRes = if (isEditorMode) R.menu.menu_editor_mode else R.menu.menu_read_mode
        menuInflater.inflate(menuRes, menu)

        val menuShowFormattingBar = menu?.findItem(R.id.menu_show_formatting_bar)
        menuShowFormattingBar?.isEnabled = !isShowFormattingBar

        val menuSearch = menu?.findItem(R.id.menu_search)
        menuSearch?.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                Log.d(Constants.TAG, "onMenuItemActionExpand")
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                Log.d(Constants.TAG, "onMenuItemActionCollapse")
                invalidateOptionsMenu()
                return true
            }

        })

        val searchView = menuSearch?.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                Log.d(Constants.TAG, "onQueryTextChange")
                return true
            }
        })
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d(Constants.TAG, "onOptionsItemSelected")
        return when (item.itemId) {
            R.id.menu_save -> {
                handleSaveNote()
                true
            }

            R.id.menu_undo -> {
                Log.d(Constants.TAG, "Undo selected")
                true
            }

            R.id.menu_redo -> {
                Log.d(Constants.TAG, "Redo selected")
                true
            }

            R.id.menu_undo_all -> {
                Log.d(Constants.TAG, "Undo all selected")
                true
            }

            R.id.menu_search -> {
                handleSearchItemSelected()
                true
            }

            R.id.menu_share -> {
                handleShareNote()
                true
            }

            R.id.menu_export -> {
                Log.d(Constants.TAG, "Export selected")
                true
            }

            R.id.menu_delete -> {
                handleDeleteNote()
                true
            }

            R.id.menu_categorize -> {
                Log.d(Constants.TAG, "Categorize selected")
                true
            }

            R.id.menu_colorize -> {
                handleColorize()
                true
            }

            R.id.menu_switch_mode -> {
                handleSwitchReadMode()
                true
            }

            R.id.menu_print -> {
                handlePrint()
                true
            }

            R.id.menu_show_formatting_bar -> {
                handleShowFormattingBar()
                true
            }

            R.id.menu_show_info -> {
                handleShowInfo()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun handleSaveNote() {
        Log.d(Constants.TAG, "handleSaveNote")
        val title = binding.titleEditText.text.toString().trim()
        val content = binding.contentEditText.text.toString().trim()
        currentNote?.let {note ->
            val newNote = Note(
                noteId = note.noteId,
                title = title,
                content = content,
                color = note.color,
                dateCreate = note.dateCreate
            )

            lifecycleScope.launch {
                viewModel.updateNote(newNote).join()
                viewModel.deleteTextStylesByNoteId(newNote.noteId).join()

                val spannable = binding.contentEditText.text
                val styleSpans = spannable?.getSpans(0, spannable.length, StyleSpan::class.java)
                val underlineSpans = spannable?.getSpans(0, spannable.length, UnderlineSpan::class.java)
                val strikethroughSpans = spannable?.getSpans(0, spannable.length, StrikethroughSpan::class.java)
                val foregroundColorSpans = spannable?.getSpans(0, spannable.length, ForegroundColorSpan::class.java)
                val backgroundColorSpans = spannable?.getSpans(0, spannable.length, BackgroundColorSpan::class.java)

                val underlineSpansMap = underlineSpans?.associate { spannable.getSpanStart(it) to spannable.getSpanEnd(it) } ?: emptyMap()
                val strikethroughSpansMap = strikethroughSpans?.associate { spannable.getSpanStart(it) to spannable.getSpanEnd(it) } ?: emptyMap()
                val foregroundColorSpansMap = foregroundColorSpans?.associate { spannable.getSpanStart(it) to it.foregroundColor } ?: emptyMap()
                val backgroundColorSpansMap = backgroundColorSpans?.associate { spannable.getSpanStart(it) to it.backgroundColor } ?: emptyMap()

                styleSpans?.forEach { span ->
                    val start = spannable.getSpanStart(span)
                    val end = spannable.getSpanEnd(span)
                    val isBold = span.style == Typeface.BOLD
                    val isItalic = span.style == Typeface.ITALIC
                    if (isBold) {
                        viewModel.insertTextStyles(TextStyles(noteId = newNote.noteId, start = start, end = end, isBold = true)).join()
                    }
                    if (isItalic) {
                        viewModel.insertTextStyles(TextStyles(noteId = newNote.noteId, start = start, end = end, isItalic = true)).join()
                    }
                }


                underlineSpans?.forEach { span ->
                    val start = spannable.getSpanStart(span)
                    val end = spannable.getSpanEnd(span)
                    val isUnderline = underlineSpansMap.any { it.key >= start && it.value <= end }

                    if (isUnderline) {
                        viewModel.insertTextStyles(TextStyles(noteId = newNote.noteId, start = start, end = end, isUnderline = true)).join()
                    }
                }


                strikethroughSpans?.forEach { span ->
                    val start = spannable.getSpanStart(span)
                    val end = spannable.getSpanEnd(span)
                    val isStrikethrough = strikethroughSpansMap.any { it.key >= start && it.value <= end }

                    if (isStrikethrough) {
                        viewModel.insertTextStyles(TextStyles(noteId = newNote.noteId, start = start, end = end, isStrikethrough = true)).join()
                    }
                }

                foregroundColorSpans?.forEach { span ->
                    val start = spannable.getSpanStart(span)
                    val end = spannable.getSpanEnd(span)
                    val textColor = foregroundColorSpansMap[start]?.let { String.format("#%06X", 0xFFFFFF and it) }

                    if (!textColor.isNullOrBlank()) {
                        viewModel.insertTextStyles(TextStyles(noteId = newNote.noteId, start = start, end = end, color = textColor)).join()
                    }
                }

                backgroundColorSpans?.forEach { span ->
                    val start = spannable.getSpanStart(span)
                    val end = spannable.getSpanEnd(span)
                    val backgroundColor = backgroundColorSpansMap[start]?.let { String.format("#%06X", 0xFFFFFF and it) }

                    if (!backgroundColor.isNullOrBlank()) {
                        viewModel.insertTextStyles(TextStyles(noteId = newNote.noteId, start = start, end = end, highlight = backgroundColor)).join()
                    }
                }
                viewModel.getNoteWithTextStylesById(currentNoteId)

                Toast.makeText(this@NoteEditorActivity, "Saved", Toast.LENGTH_SHORT).show()
            }

        }
    }

    private fun handleShareNote() {
        Log.d(Constants.TAG, "handleShareNote")
        handleSaveNote()
        currentNote?.let { note ->
            val intent = Intent(Intent.ACTION_SEND).apply {
                putExtra(Intent.EXTRA_TEXT, note.content)
                type = "text/plain"
            }
            startActivity(Intent.createChooser(intent, null))
        }
    }

    private fun handleDeleteNote() {
        Log.d(Constants.TAG, "handleDeleteNote")
        currentNote?.let { note ->
            val message = note.title.ifEmpty {
                note.content.ifEmpty {
                    getString(R.string.untitled)
                }
            }
            val builder = AlertDialog.Builder(this).apply {
                setMessage(getString(R.string.delete_note_message, message))
                setPositiveButton(R.string.ok) { _,_ ->
                    viewModel.deleteNote(note)
                    finish()
                }
                setNegativeButton(R.string.cancel) { dialog, _ ->
                    dialog.dismiss()
                }
            }
            val dialog = builder.create()
            dialog.show()
        }
    }

    private fun handleSearchItemSelected() {
        Log.d(Constants.TAG, "handleSearchItemSelected")
        lifecycleScope.launch(Dispatchers.Main) {
            binding.toolbar.menu?.let { menu ->
                menu.forEach { menuItem ->
                    menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW)
                }

                menu.findItem(R.id.menu_search_custom).apply {
                    isVisible = true
                    setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                }
            }
        }
    }

    private fun handleColorize() {
        Log.d(Constants.TAG, "handleColorize")
        currentNote?.let { note ->
            MaterialColorPickerDialog
                .Builder(this)
                .setColors(resources.getStringArray(R.array.themeColorHex))
                .setDefaultColor(note.color)
                .setColorListener { _, colorHex ->
                    viewModel.updateNoteColor(note.noteId, colorHex)
                }
                .show()
        }
    }

    private fun handleSwitchReadMode() {
        Log.d(Constants.TAG, "handleSwitchReadMode")
        isEditorMode = !isEditorMode
        handleSaveNote()
        initLayoutMode()
        invalidateOptionsMenu()
    }

    private fun handlePrint() {
        Log.d(Constants.TAG, "handlePrint")
        val printManager = this.getSystemService(Context.PRINT_SERVICE) as PrintManager
        printManager.print(getString(R.string.job_name), PrintNoteAdapter(this), null)
    }

    private fun handleShowFormattingBar() {
        Log.d(Constants.TAG, "handleShowFormattingBar")
        isShowFormattingBar = !isShowFormattingBar
        val menuShowFormattingBar = binding.toolbar.menu.findItem(R.id.menu_show_formatting_bar)
        menuShowFormattingBar?.isEnabled = !isShowFormattingBar
        initFormattingBar()
    }

    private fun handleShowInfo() {
        Log.d(Constants.TAG, "handleShowInfo")
        val content = binding.contentEditText.text.toString()
        val words = content.split("\\s+".toRegex()).filter { it.isNotBlank() }.size
        val lines = content.split("\n").size
        val characters = content.trim().replace("\n", "").length
        val charactersWithoutSpaces = content.trim().replace("\\s".toRegex(), "").length
        val createdAt = currentNote?.let { DateTimeUtils.getFormattedDateTime(it.dateCreate) }
        val updatedAt = currentNote?.let { DateTimeUtils.getFormattedDateTime(it.lastUpdate) }

        val message = """
                    Words: $words
                    Wrapped lines: $lines
                    Characters: $characters
                    Characters without whitespaces: $charactersWithoutSpaces
                    Created at: $createdAt
                    Updated at: $updatedAt
                """.trimIndent()

        AlertDialog.Builder(this).apply {
            setMessage(message)
            setPositiveButton(R.string.ok, null)
        }.create().show()
    }
}