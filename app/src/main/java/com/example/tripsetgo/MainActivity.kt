package com.example.tripsetgo
import HomeFragment
import ProfileFragment
import SearchFragment
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        val navigationView: NavigationView = findViewById(R.id.navigation_view)
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)

        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Load home fragment
                    loadFragment(HomeFragment())
                }
                R.id.nav_search -> {
                    // Load search fragment
                    loadFragment(SearchFragment())
                }
                R.id.nav_profile -> {
                    // Load profile fragment
                    loadFragment(ProfileFragment())
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Load home fragment
                    loadFragment(HomeFragment())
                    true
                }
                R.id.nav_search -> {
                    // Load search fragment
                    loadFragment(SearchFragment())
                    true
                }
                R.id.nav_profile -> {
                    // Load profile fragment
                    loadFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }

        // Load the default fragment
        if (savedInstanceState == null) {
            bottomNavigationView.selectedItemId = R.id.nav_home
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit()
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
