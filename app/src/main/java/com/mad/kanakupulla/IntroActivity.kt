package com.mad.kanakupulla

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class IntroActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_intro)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        mAuth = FirebaseAuth.getInstance()
        mAuth.currentUser?.let {
            startActivity(Intent(applicationContext, HomeScreen::class.java))
            finish()

        }
    }
    fun tologin(view: View)
    {
        val intent = Intent(this, Login2Activity::class.java)
        startActivity(intent)
    }
    fun toregister(view: View)
    {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }
}