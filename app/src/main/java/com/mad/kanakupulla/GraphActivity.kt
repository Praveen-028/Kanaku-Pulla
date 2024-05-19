package com.mad.kanakupulla

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth

class GraphActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_graph)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.graphmain_frame)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val toolbar = findViewById<Toolbar>(R.id.mytoolbar)
        toolbar.title = "Kanaku Pulla"
        setSupportActionBar(toolbar)

        mAuth=FirebaseAuth.getInstance()

        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerlayout)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawe_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        val defaultFragment = IncGraph()
        supportFragmentManager.beginTransaction()
            .replace(R.id.graphmain_frame, defaultFragment)
            .commit()

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.graphbottomNaviagatioBar)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            handleNavigation(item)
            true
        }
    }

    private fun handleNavigation(item: MenuItem): Boolean {
        val fragment: Fragment? = when (item.itemId) {
            R.id.Profile -> {
                val intent = Intent(this, Profile_page::class.java)
                startActivity(intent)
                return true
            }
            R.id.IncGraph -> {
                // Replace with your fragment
                val fragment = IncGraph()
                supportFragmentManager.beginTransaction()
                    .replace(R.id.graphmain_frame, fragment)
                    .commit()
                return true
            }
            R.id.Graph->
            {
                startActivity(Intent(applicationContext, GraphActivity::class.java))
                finish()
                return true

            }
            R.id.Home ->{
                startActivity(Intent(this,HomeScreen::class.java))
                finish()
                return true

            }
            R.id.logout -> {
                mAuth.signOut()
                startActivity(Intent(this,IntroActivity::class.java))
                finish()
                return true
            }
            R.id.SmSEnable -> {
                val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
                val isEnabled = sharedPreferences.getBoolean("sms_reading_enabled", false)
                if (isEnabled) {
                    Toast.makeText(this, "SMS reading is already enabled", Toast.LENGTH_SHORT).show()
                } else {
                    SmsReceiver.enableSmsReading(this)
                    Toast.makeText(this, "SMS reading enabled", Toast.LENGTH_SHORT).show()
                }
                return true

            }
            R.id.SmsDisable -> {
                val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
                val isEnabled = sharedPreferences.getBoolean("sms_reading_enabled", false)
                if (!isEnabled) {
                    Toast.makeText(this, "SMS reading is already disabled", Toast.LENGTH_SHORT).show()
                } else {
                    SmsReceiver.disableSmsReading(this)
                    Toast.makeText(this, "SMS reading disabled", Toast.LENGTH_SHORT).show()
                }
                return true

            }
            R.id.ExpGraph -> {
                // Replace with your fragment
                val fragment =ExpGraph()
                supportFragmentManager.beginTransaction()
                    .replace(R.id.graphmain_frame, fragment)
                    .commit()
                return true
            }
            else -> null
        }



        return false
    }

    private fun setBottomNavigationColor(colorId: Int) {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.graphbottomNaviagatioBar)
        bottomNavigationView.setBackgroundColor(ContextCompat.getColor(this, colorId))
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        return handleNavigation(item)
    }
}
