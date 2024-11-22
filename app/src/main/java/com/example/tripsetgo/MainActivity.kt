package com.example.tripsetgo
import HomeFragment
import com.example.tripsetgo.ItineraryFragment


import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var homeContent: RelativeLayout
    private lateinit var fragmentContainer: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<View>(R.id.fragment_container).visibility = View.GONE
        findViewById<View>(R.id.home_content).visibility = View.VISIBLE

        // Initialize views
        drawerLayout = findViewById(R.id.drawer_layout)
        homeContent = findViewById(R.id.home_content)
        fragmentContainer = findViewById(R.id.fragment_container)
        firebaseAuth = FirebaseAuth.getInstance()

        val carditinerary = findViewById<ImageView>(R.id.imageItinerary)
        val carddocs = findViewById<ImageView>(R.id.imageDocs)
        val cardphotos = findViewById<ImageView>(R.id.imagePhotos)
        val cardexpense = findViewById<ImageView>(R.id.imageExpenses)

        // Card click listeners
        carditinerary.setOnClickListener {
            loadFragment(ItineraryFragment())
        }
        carddocs.setOnClickListener {
            loadFragment(DocsFragment())
        }
        cardphotos.setOnClickListener {
            val galleryFragment = GalleryFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, galleryFragment)
                .addToBackStack("home")  // Add a name to identify this transaction
                .commit()
            findViewById<View>(R.id.home_content).visibility = View.GONE
            findViewById<View>(R.id.fragment_container).visibility = View.VISIBLE
        }
        cardexpense.setOnClickListener {
            loadFragment(ExpenseFragment())
        }

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        setupNavigationViews()
    }

    private fun setupNavigationViews() {
        val navigationView: NavigationView = findViewById(R.id.navigation_view)
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)

        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    showHomeContent()
                }
                R.id.nav_logout -> {
                    firebaseAuth.signOut()
                    startActivity(Intent(this, SignInActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                    finish()
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    showHomeContent()
                    true
                }
                R.id.nav_itinerary -> {
                    loadFragment(ItineraryFragment())
                    true
                }
                R.id.nav_docs -> {
                    loadFragment(DocsFragment())
                    true
                }
                R.id.nav_photo -> {
                    loadFragment(GalleryFragment())
                    true
                }
                R.id.nav_expense -> {
                    loadFragment(ExpenseFragment())
                    true
                }
                else -> false
            }
        }

        // Set initial selection
        bottomNavigationView.selectedItemId = R.id.nav_home
    }

    private fun loadFragment(fragment: Fragment) {
        // Hide home content when loading a fragment
        findViewById<View>(R.id.home_content)?.visibility = View.GONE

        // Show fragment container
        findViewById<View>(R.id.fragment_container)?.visibility = View.VISIBLE

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun showHomeContent() {
        // Clear back stack
        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)

        // Show home content and hide fragment container
        homeContent.visibility = View.VISIBLE
        fragmentContainer.visibility = View.GONE
    }

    override fun onBackPressed() {
        when {
            drawerLayout.isDrawerOpen(GravityCompat.START) -> {
                drawerLayout.closeDrawer(GravityCompat.START)
            }
            supportFragmentManager.backStackEntryCount > 1 -> {
                supportFragmentManager.popBackStack()
            }
            supportFragmentManager.backStackEntryCount == 1 -> {
                // Check if we're in CapsuleContentFragment
                val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
                if (currentFragment is CapsuleContentFragment) {
                    supportFragmentManager.popBackStack()
                } else {
                    // We're in a main level fragment (like GalleryFragment)
                    supportFragmentManager.popBackStack()
                    findViewById<View>(R.id.home_content)?.visibility = View.VISIBLE
                    findViewById<View>(R.id.fragment_container)?.visibility = View.GONE
                }
            }
            else -> {
                super.onBackPressed()
            }
        }
    }
}