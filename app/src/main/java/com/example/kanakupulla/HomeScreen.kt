package com.example.kanakupulla

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import androidx.activity.enableEdgeToEdge
import androidx.annotation.NonNull
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class HomeScreen : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var mAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home_screen)

        // Set window insets listener to handle padding
        findViewById<View>(R.id.main)?.let { mainView ->
            ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.mytoolbar)
        toolbar.title = "ExpenseManager"
        setSupportActionBar(toolbar)

        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerlayout)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawe_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        val dashboardFragment = DashboardFragment()
        val incomeFragment=IncomeFragment()
        val expenseFragment=ExpenseFragment()

        mAuth=FirebaseAuth.getInstance()

        setFragment(dashboardFragment)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNaviagatioBar)

        val frameLayout = findViewById<FrameLayout>(R.id.main_frame)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.dashbord -> {
                    setFragment(dashboardFragment)
                    bottomNavigationView.setBackgroundColor(ContextCompat.getColor(this, R.color.dashboard_color))
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.income ->{
                    setFragment(incomeFragment)
                    bottomNavigationView.setBackgroundColor(ContextCompat.getColor(this, R.color.income_color))
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.expense ->{
                    setFragment(expenseFragment)
                    bottomNavigationView.setBackgroundColor(ContextCompat.getColor(this, R.color.expense_color))
                    return@setOnNavigationItemSelectedListener true
                }
                // Add more cases for other menu items if needed
                else -> false
            }
        }


    }

    private fun setFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.main_frame, fragment)
            addToBackStack(null) // Optionally add this if you want to add the transaction to the back stack
            commit()
        }
    }


    override fun onBackPressed() {
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerlayout)
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }



    private fun displaySelectedListener(itemId: Int) {
        var fragment: Fragment? = null

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNaviagatioBar)
        when (itemId) {
            R.id.dashbord -> {
                fragment = DashboardFragment()
                setFragment(fragment)
                bottomNavigationView.setBackgroundColor(ContextCompat.getColor(this, R.color.dashboard_color))
            }
            R.id.income -> {
                fragment = IncomeFragment()
                setFragment(fragment)
                bottomNavigationView.setBackgroundColor(ContextCompat.getColor(this, R.color.income_color))
            }
            R.id.expense -> {
                fragment = ExpenseFragment()
                setFragment(fragment)
                bottomNavigationView.setBackgroundColor(ContextCompat.getColor(this, R.color.expense_color))
            }
            R.id.logout -> {
                mAuth.signOut()
                startActivity(Intent(this,MainActivity::class.java))
                finish()
            }
        }
        fragment?.let {
            val ft = supportFragmentManager.beginTransaction()
            ft.replace(R.id.main_frame, it)
            ft.commit()
        }
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerlayout)
        drawerLayout.closeDrawer(GravityCompat.START)
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        displaySelectedListener(menuItem.itemId)
        return true
    }
}
