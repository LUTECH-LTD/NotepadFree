package lutech.intern.noteapp.ui.main

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.RadioButton
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import lutech.intern.noteapp.R
import lutech.intern.noteapp.constant.SortOption
import lutech.intern.noteapp.databinding.ActivityMainBinding
import lutech.intern.noteapp.databinding.DialogSortOptionsBinding
import lutech.intern.noteapp.event.SortEvent
import lutech.intern.noteapp.ui.BackupActivity
import lutech.intern.noteapp.ui.HelpActivity
import lutech.intern.noteapp.ui.note.NotesFragment
import lutech.intern.noteapp.ui.PrivacyPolicyActivity
import lutech.intern.noteapp.ui.SettingsActivity
import lutech.intern.noteapp.ui.TrashFragment
import lutech.intern.noteapp.ui.category.CategoriesFragment
import org.greenrobot.eventbus.EventBus

class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val mainViewModel: MainViewModel by viewModels()
    private var selectedMenuItemId: Int? = null
    private var sortOptionSelected = SortOption.EDIT_DATE_NEWEST.toString()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initViews()
        observeDataViewModel()

        if (savedInstanceState == null) {
            loadFragment(NotesFragment.newInstance(), getString(R.string.app_name), null)
            selectedMenuItemId = R.id.menu_notes
        }
        EventBus.getDefault().post(SortEvent(sortOptionSelected))
    }

    private fun initViews() {
        initToolbar()
        initNavigationView()
    }

    private fun initToolbar() {
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    private fun initNavigationView() {
        binding.navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_notes -> {
                    loadFragment(
                        fragment = NotesFragment.newInstance(),
                        title = getString(R.string.app_name),
                        subTitle = null
                    )
                    selectedMenuItemId = item.itemId
                }

                R.id.menu_uncategorized -> {
                    loadFragment(
                        fragment = NotesFragment.newInstance(),
                        title = getString(R.string.app_name),
                        subTitle = item.title
                    )
                    selectedMenuItemId = item.itemId
                }

                R.id.menu_edit_categories -> {
                    loadFragment(
                        fragment = CategoriesFragment.newInstance(),
                        title = getString(R.string.categories),
                        subTitle = null
                    )
                }

                R.id.menu_backup -> {
                    val intent = Intent(this, BackupActivity::class.java)
                    startActivity(intent)
                }

                R.id.menu_trash -> {
                    loadFragment(TrashFragment.newInstance(), getString(R.string.trash), null)
                }

                R.id.menu_setting -> {
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
                }

                R.id.menu_rate -> {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.uri_app)))
                    startActivity(intent)
                }

                R.id.menu_help -> {
                    val intent = Intent(this, HelpActivity::class.java)
                    startActivity(intent)
                }

                R.id.menu_privacy_policy -> {
                    val intent = Intent(this, PrivacyPolicyActivity::class.java)
                    startActivity(intent)
                }

                else -> {
                    if (item.groupId == R.id.menu_categories) {
                        loadFragment(
                            fragment = NotesFragment.newInstance(),
                            title = getString(R.string.app_name),
                            subTitle = item.title
                        )
                        selectedMenuItemId = item.itemId
                    }
                }
            }
            invalidateOptionsMenu()
            binding.drawerLayout.closeDrawers()
            true
        }
    }

    private fun loadFragment(fragment: Fragment, title: String, subTitle: CharSequence? = null) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .commitNow()

        binding.toolbar.title = title
        binding.toolbar.subtitle = subTitle
    }

    private fun observeDataViewModel() {
        mainViewModel.categories.observe(this) { categories ->
            // Update category menu
            val menu = binding.navigationView.menu
            val editCategoriesItem = menu.findItem(R.id.menu_categories)
            val subMenu = editCategoriesItem.subMenu

            subMenu?.let {
                it.clear()
                categories.forEach { category ->
                    it.add(
                        R.id.menu_categories,
                        category.categoryId.toInt(),
                        Menu.NONE,
                        category.name
                    ).setIcon(R.drawable.ic_categorized)
                }
            }

            if (categories.isNotEmpty()) {
                menuInflater.inflate(R.menu.menu_item_uncategorized, subMenu)
            }
            menuInflater.inflate(R.menu.menu_item_edit_categories, subMenu)
        }
    }

    fun getSelectedMenuItemId() = selectedMenuItemId

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        supportFragmentManager.findFragmentById(R.id.container)?.let { fragment ->
            if (fragment is NotesFragment) {
                menuInflater.inflate(R.menu.menu_option_page_notes, menu)
            }
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_sort -> {
                val dialogBinding = DialogSortOptionsBinding.inflate(layoutInflater)
                val builder = AlertDialog.Builder(this)
                val idItem = when(sortOptionSelected) {
                    SortOption.EDIT_DATE_NEWEST.toString() -> {
                        R.id.radio_edit_date_newest
                    }
                    SortOption.EDIT_DATE_OLDEST.toString() -> {
                        R.id.radio_edit_date_oldest
                    }
                    SortOption.CREATION_DATE_NEWEST.toString() -> {
                        R.id.radio_creation_date_newest
                    }
                    SortOption.CREATION_DATE_OLDEST.toString() -> {
                        R.id.radio_creation_date_oldest
                    }
                    SortOption.TITLE_A_Z.toString() -> {
                        R.id.radio_title_a_z
                    }
                    SortOption.TITLE_Z_A.toString() -> {
                        R.id.radio_title_z_a
                    }
                    SortOption.COLOR.toString() -> {
                        R.id.radio_color
                    }
                    else -> {
                        R.id.radio_edit_date_newest
                    }
                }
                dialogBinding.sortGroup.findViewById<RadioButton>(idItem).isChecked = true

                builder.apply {
                    setView(dialogBinding.root)
                    setPositiveButton(R.string.ok) { dialog, _ ->
                        EventBus.getDefault().post(SortEvent(sortOptionSelected))
                    }
                    setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                        dialog.dismiss()
                    }
                }

                val dialog = builder.create()
                dialog.show()

                dialogBinding.sortGroup.setOnCheckedChangeListener { group, checkedId ->
                    val sortOption = when (checkedId) {
                        R.id.radio_edit_date_newest -> SortOption.EDIT_DATE_NEWEST.toString()
                        R.id.radio_edit_date_oldest -> SortOption.EDIT_DATE_OLDEST.toString()
                        R.id.radio_creation_date_newest -> SortOption.CREATION_DATE_NEWEST.toString()
                        R.id.radio_creation_date_oldest -> SortOption.CREATION_DATE_OLDEST.toString()
                        R.id.radio_title_a_z -> SortOption.TITLE_A_Z.toString()
                        R.id.radio_title_z_a -> SortOption.TITLE_Z_A.toString()
                        R.id.radio_color -> SortOption.COLOR.toString()
                        else -> SortOption.EDIT_DATE_OLDEST.toString()
                    }
                    sortOptionSelected = sortOption
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }
}