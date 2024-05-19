package com.mad.kanakupulla

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import android.content.Context
import com.google.android.material.button.MaterialButton

class HomeScreen : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var smsReceiver: SmsReceiver
    private var isReceiverEnabled = true
    private val prefsName = "MyPrefsFile"
    private val isFirstTime = "isFirstTime"

    companion object {
        const val SMS_PERMISSION_CODE = 123
    }

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
        val sharedPreferences = getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        val isFirst = sharedPreferences.getBoolean(isFirstTime, true)
        if (isFirst) {
            showCustomDialog()

            // Update the flag to indicate that the dialog has been shown
            val editor = sharedPreferences.edit()
            editor.putBoolean(isFirstTime, false)
            editor.apply()
        }

        smsReceiver = SmsReceiver()

        registerReceiver()

        checkAndRequestPermissions()


        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.mytoolbar)
        toolbar.title = "Kanaku Pulla"
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
        val unpaidFragment=Unpaid_tab()

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
                R.id.unpaid->{
                    setFragment(unpaidFragment)
                    bottomNavigationView.setBackgroundColor(ContextCompat.getColor(this, R.color.expense_color))
                    return@setOnNavigationItemSelectedListener true
                }
                // Add more cases for other menu items if needed
                else -> false
            }
        }


    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                Toast.makeText(this, "SMS permissions granted", Toast.LENGTH_SHORT).show()
            } else {
                // Permission denied
                Toast.makeText(this, "SMS permissions denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        if (isReceiverEnabled) {
            unregisterReceiver(smsReceiver)
        }
    }
    private fun showCustomDialog() {
        // Create a new Dialog instance
        val dialog = Dialog(this)
        // Set the custom dialog layout
        dialog.setContentView(R.layout.alert_dialog)

        // Optionally, you can find and set listeners on the dialog's buttons
        val okButton = dialog.findViewById<MaterialButton>(R.id.alertbtnok)
        okButton.setOnClickListener {
            // Handle the OK button click event
            dialog.dismiss() // Dismiss the dialog
        }

        // Show the dialog
        dialog.show()
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
    private fun registerReceiver() {
        val intentFilter = IntentFilter("android.provider.Telephony.SMS_RECEIVED")
        registerReceiver(smsReceiver, intentFilter)
    }
    private fun checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS), SMS_PERMISSION_CODE)
        } else {
            // Permissions already granted
            Toast.makeText(this, "SMS permissions granted", Toast.LENGTH_SHORT).show()
        }
    }



    private fun displaySelectedListener(itemId: Int) {
        var fragment: Fragment? = null

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNaviagatioBar)
        when (itemId) {
            R.id.Profile ->{
                startActivity(Intent(this,Profile_page::class.java))
            }
            R.id.Graph->
            {
                startActivity(Intent(applicationContext, GraphActivity::class.java))
                finish()
            }
            R.id.Home ->{
                startActivity(Intent(this,HomeScreen::class.java))
                finish()
            }
            R.id.logout -> {
                mAuth.signOut()
                startActivity(Intent(this,IntroActivity::class.java))
                finish()
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
