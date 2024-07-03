package lutech.intern.noteapp.ui.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import lutech.intern.noteapp.R
import lutech.intern.noteapp.databinding.ActivityMainBinding
import lutech.intern.noteapp.ui.BackupActivity
import lutech.intern.noteapp.ui.HelpActivity
import lutech.intern.noteapp.ui.note.NotesFragment
import lutech.intern.noteapp.ui.PrivacyPolicyActivity
import lutech.intern.noteapp.ui.SettingsActivity
import lutech.intern.noteapp.ui.TrashFragment
import lutech.intern.noteapp.ui.category.CategoriesFragment

class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val mainViewModel: MainViewModel by viewModels()
    private var itemCurrentIdSelected: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initViews()
        observeDataViewModel()

        if (savedInstanceState == null) {
            loadFragment(NotesFragment.newInstance(), getString(R.string.app_name), null)
            itemCurrentIdSelected = R.id.menu_notes
        }
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
                    itemCurrentIdSelected = item.itemId
                }

                R.id.menu_uncategorized -> {
                    loadFragment(
                        fragment = NotesFragment.newInstance(),
                        title = getString(R.string.app_name),
                        subTitle = item.title
                    )
                    itemCurrentIdSelected = item.itemId
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
                        itemCurrentIdSelected = item.itemId
                    }
                }
            }
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

    fun getItemCurrentIdSelected() = itemCurrentIdSelected
}