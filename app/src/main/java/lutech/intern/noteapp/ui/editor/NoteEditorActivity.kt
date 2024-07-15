package lutech.intern.noteapp.ui.editor

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.print.PrintManager
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.forEach
import androidx.lifecycle.lifecycleScope
import com.github.dhaval2404.colorpicker.MaterialColorPickerDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import lutech.intern.noteapp.R
import lutech.intern.noteapp.adapter.CategorySelectedAdapter
import lutech.intern.noteapp.constant.Constants
import lutech.intern.noteapp.data.IndexRange
import lutech.intern.noteapp.data.entity.Category
import lutech.intern.noteapp.data.entity.Note
import lutech.intern.noteapp.data.entity.TextStyles
import lutech.intern.noteapp.data.entity.relations.NoteCategoryCrossRef
import lutech.intern.noteapp.databinding.ActivityNoteEditorBinding
import lutech.intern.noteapp.databinding.DialogSelectCategoryBinding
import lutech.intern.noteapp.databinding.DialogTextSizeBinding
import lutech.intern.noteapp.ui.editor.adapter.PrintNoteAdapter
import lutech.intern.noteapp.utils.DateTimeUtils
import lutech.intern.noteapp.utils.DrawableUtils
import lutech.intern.noteapp.utils.FileManager


class NoteEditorActivity : AppCompatActivity() {
    private val binding by lazy { ActivityNoteEditorBinding.inflate(layoutInflater) }
    private val viewModel: NoteEditorViewModel by viewModels()
    private var categories: List<Category> = emptyList()
    private val categorySelectedAdapter by lazy { CategorySelectedAdapter() }
    private val currentNoteId by lazy { intent.getLongExtra(Constants.EXTRA_NOTE_ID, 0L) }
    private var indexRange = 0
    private var indexRanges: List<IndexRange>?= null
    private var currentNote: Note? = null
    private var isEditorMode = true
    private var isShowFormattingBar = false

    private val openDocumentTreeLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.data?.let { uri ->
                    currentNote?.let { note ->
                        FileManager(this).exportFileToFolder(uri, note.title, note.content)
                    }
                }
            }
        }

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

    @SuppressLint("ClickableViewAccessibility")
    private fun initEvents() {
        Log.d(Constants.TAG, "initEvents")
        binding.layoutReadMode.setOnClickListener {
            handleSwitchReadMode()
        }

        binding.btnHideFormattingBar.setOnClickListener {
            handleShowFormattingBar()
        }

        binding.cbFontBold.setOnClickListener {
            val isChecked = binding.cbFontBold.isChecked
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

        binding.cbFontItalic.setOnClickListener {
            val isChecked = binding.cbFontItalic.isChecked
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

        binding.cbFontUnderline.setOnClickListener {
            val isChecked = binding.cbFontUnderline.isChecked
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

        binding.cbFontStrikethrough.setOnClickListener {
            val isChecked = binding.cbFontStrikethrough.isChecked
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
                        spannable?.setSpan(
                            BackgroundColorSpan(Color.parseColor(colorHex)),
                            start,
                            end,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                }
                .show()
        }

        binding.cbFontSize.setOnClickListener {
            val dialogBinding = DialogTextSizeBinding.inflate(layoutInflater)
            dialogBinding.btnReset.setOnClickListener {
                dialogBinding.seekBar.progress = 18
            }

            dialogBinding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    dialogBinding.tvTextSize.setTextSize(TypedValue.COMPLEX_UNIT_SP, progress.toFloat())
                    dialogBinding.tvTextSize.text = "Text size $progress"
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                }
            })

            AlertDialog.Builder(this).apply {
                setView(dialogBinding.root)
                setPositiveButton(R.string.ok) { _, _ ->
                    val spannable = binding.contentEditText.text
                    val start = binding.contentEditText.selectionStart
                    val end = binding.contentEditText.selectionEnd
                    val textSize = dialogBinding.seekBar.progress.toFloat()

                    if (start != -1 && end != -1) {
                        val spans = spannable?.getSpans(start, end, RelativeSizeSpan::class.java)
                        if (spans != null) {
                            for (span in spans) {
                                spannable.removeSpan(span)
                            }
                        }

                        spannable?.setSpan(RelativeSizeSpan(textSize / 18F), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                }
                setNegativeButton(R.string.cancel, null)
            }.create().show()
        }

        binding.contentEditText.setOnTouchListener { _, event ->
            Log.d(Constants.TAG, "setOnTouchListener")
            if (event.action == MotionEvent.ACTION_UP) {
                binding.contentEditText.post {
                    val startPosition = binding.contentEditText.selectionStart
                    val endPosition = binding.contentEditText.selectionEnd
                    val spannable = binding.contentEditText.text

                    binding.cbFontBold.isChecked = false
                    binding.cbFontItalic.isChecked = false
                    binding.cbFontUnderline.isChecked = false
                    binding.cbFontStrikethrough.isChecked = false

                    val styleSpans = spannable?.getSpans(startPosition, endPosition, StyleSpan::class.java)
                    styleSpans?.forEach { span ->
                        when (span.style) {
                            Typeface.BOLD -> binding.cbFontBold.isChecked = true
                            Typeface.ITALIC -> binding.cbFontItalic.isChecked = true
                        }
                    }

                    val underlineSpans = spannable?.getSpans(startPosition, endPosition, UnderlineSpan::class.java)
                    if (!underlineSpans.isNullOrEmpty()) {
                        binding.cbFontUnderline.isChecked = true
                    }

                    val strikethroughSpans = spannable?.getSpans(startPosition, endPosition, StrikethroughSpan::class.java)
                    if (!strikethroughSpans.isNullOrEmpty()) {
                        binding.cbFontStrikethrough.isChecked = true
                    }
                }
            }
            false
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

                if(listTextStyles.isNotEmpty()) {
                    isShowFormattingBar = true
                    initFormattingBar()
                }

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

                    spannable.setSpan(RelativeSizeSpan(textStyle.textSize / 18), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }

                binding.titleEditText.setText(note.title)
                binding.tvTitle.text = binding.titleEditText.text
                binding.contentEditText.setText(spannable)
                binding.tvContent.text =  binding.contentEditText.text

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

        viewModel.categoryWithNotes.observe(this) { categoryWithNotes ->
            Log.d(Constants.TAG, "initObservers: categoryWithNotes")
            categories = categoryWithNotes.map { it.category }
            val selectedCategories = categoryWithNotes.filter {
                it.notes.any { note -> note.noteId == currentNoteId }
            }.map {
                it.category
            }

            categorySelectedAdapter.submitList(categories, selectedCategories)
        }

        viewModel.indexRanges.observe(this) { list ->
            indexRanges = list
            val menu = binding.toolbar.menu
            val itemSearchCustom = menu.findItem(R.id.menu_search_custom)
            val actionView = itemSearchCustom?.actionView
            val content = binding.contentEditText.text as Spannable

            actionView?.let { view ->
                if(list.isEmpty()) {
                    view.findViewById<LinearLayout>(R.id.main).alpha = 0.5F
                    view.findViewById<AppCompatImageButton>(R.id.btnUp).isEnabled = false
                    view.findViewById<AppCompatImageButton>(R.id.btnDown).isEnabled = false
                    view.findViewById<TextView>(R.id.tvCountSearch)?.text = "0/0"

                    viewModel.getNoteWithTextStylesById(currentNoteId)
                } else {
                    view.findViewById<LinearLayout>(R.id.main).alpha = 1F
                    view.findViewById<AppCompatImageButton>(R.id.btnUp).isEnabled = true
                    view.findViewById<AppCompatImageButton>(R.id.btnDown).isEnabled = true
                    view.findViewById<TextView>(R.id.tvCountSearch).text = "${indexRange + 1}/${list.size}"
                    customSpannableContentNote(content, list)

                    // action click
                    view.findViewById<AppCompatImageButton>(R.id.btnDown).setOnClickListener {
                        Log.d(Constants.TAG, "btnDown setOnClickListener")
                        indexRange = if (indexRange == 0) list.size - 1 else indexRange - 1
                        customSpannableContentNote(content, list)
                        view.findViewById<TextView>(R.id.tvCountSearch).text = "${indexRange + 1}/${list.size}"
                    }

                    view.findViewById<AppCompatImageButton>(R.id.btnUp).setOnClickListener {
                        Log.d(Constants.TAG, "btnUp setOnClickListener")
                        indexRange = if (indexRange < list.size - 1) indexRange + 1 else 0
                        customSpannableContentNote(content, list)
                        view.findViewById<TextView>(R.id.tvCountSearch).text = "${indexRange + 1}/${list.size}"
                    }
                }
            }
        }
    }

    private fun customSpannableContentNote(content: Spannable, list: List<IndexRange>) {
        val spannableString = SpannableStringBuilder(content)
        for (range in list) {
            val color = if (list[indexRange] == range) {
                ContextCompat.getColor(this, R.color.highlight_color2)
            } else {
                ContextCompat.getColor(this, R.color.highlight_color)
            }

            spannableString.setSpan(
                BackgroundColorSpan(color),
                range.start,
                range.end,
                SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        binding.contentEditText.text = spannableString
        binding.tvContent.text = spannableString
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
                val content = binding.contentEditText.text as Spannable
                indexRange = 0
                viewModel.searchContentNote(content, newText ?: "")
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
                handleUndoAll()
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
                handleExportNote()
                true
            }

            R.id.menu_delete -> {
                handleDeleteNote()
                true
            }

            R.id.menu_categorize -> {
                handleCategorize()
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
                val sizeSpans = spannable?.getSpans(0, spannable.length, RelativeSizeSpan::class.java)

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
                        viewModel.insertTextStyles(TextStyles(noteId = newNote.noteId, start = start, end = end, isBold = true, isItalic = isItalic)).join()
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
                        if(backgroundColor == "#FFFFFF") {
                            viewModel.insertTextStyles(TextStyles(noteId = newNote.noteId, start = start, end = end, highlight = null)).join()
                        } else {
                            viewModel.insertTextStyles(TextStyles(noteId = newNote.noteId, start = start, end = end, highlight = backgroundColor)).join()
                        }
                    }
                }

                sizeSpans?.forEach { span ->
                    val start = spannable.getSpanStart(span)
                    val end = spannable.getSpanEnd(span)
                    val textSize = span.sizeChange
                    viewModel.insertTextStyles(TextStyles(noteId = newNote.noteId, start = start, end = end, textSize = textSize * 18F)).join()
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

    private fun handleExportNote() {
        Log.d(Constants.TAG, "handleExportNote")
        handleSaveNote()
        Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).also { intent ->
            openDocumentTreeLauncher.launch(intent)
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

    private fun handleUndoAll() {
        Log.d(Constants.TAG, "handleUndoAll")
        val builder = AlertDialog.Builder(this).apply {
            setMessage(getString(R.string.undo_all_message))
            setPositiveButton(R.string.ok) { _,_ ->
                viewModel.getNoteWithTextStylesById(currentNoteId)
            }
            setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
        }
        val dialog = builder.create()
        dialog.show()
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

    private fun handleCategorize() {
        Log.d(Constants.TAG, "handleCategorize")
        if(categories.isEmpty()) {
            AlertDialog.Builder(this).apply {
                setMessage(getString(R.string.categories_empty_message))
                setPositiveButton(R.string.ok, null)
            }.create().show()
        } else {
            val selectedCategoriesState: MutableMap<Long, Boolean> = mutableMapOf()
            categorySelectedAdapter.setOnCheckedChange { category, isChecked ->
                selectedCategoriesState[category.categoryId] = isChecked
            }

            val dialogBinding = DialogSelectCategoryBinding.inflate(layoutInflater)
            dialogBinding.categoriesRecyclerView.adapter = categorySelectedAdapter
            AlertDialog.Builder(this).apply {
                setView(dialogBinding.root)
                setPositiveButton(R.string.ok) { _, _ ->
                    selectedCategoriesState.forEach { (categoryId, isChecked) ->
                        currentNote?.let { note ->
                            if (isChecked) {
                                viewModel.insertNoteCategoryCrossRef(NoteCategoryCrossRef(note.noteId, categoryId))
                            } else {
                                viewModel.deleteNoteCategoryCrossRef(note.noteId, categoryId)
                            }
                        }
                    }
                    Toast.makeText(this@NoteEditorActivity, "Update success", Toast.LENGTH_SHORT).show()
                }
                setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                    dialog.dismiss()
                }
            }.create().show()
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
        handleSaveNote()
        isEditorMode = !isEditorMode
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