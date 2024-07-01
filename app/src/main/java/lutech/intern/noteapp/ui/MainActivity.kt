package lutech.intern.noteapp.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import lutech.intern.noteapp.R
import lutech.intern.noteapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initViews()
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

                }

                R.id.menu_edit_categories -> {

                }

                R.id.menu_backup -> {
                    val intent = Intent(this, BackupActivity::class.java)
                    startActivity(intent)
                }

                R.id.menu_trash -> {

                }

                R.id.menu_setting -> {
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
                }

                R.id.menu_rate -> {

                }

                R.id.menu_help -> {
                    val intent = Intent(this, HelpActivity::class.java)
                    startActivity(intent)
                }

                R.id.menu_privacy_policy -> {
                    val intent = Intent(this, PrivacyPolicyActivity::class.java)
                    startActivity(intent)
                }
            }
            binding.drawerLayout.closeDrawers()
            true
        }
    }
}