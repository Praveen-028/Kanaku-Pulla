package com.example.kanakupulla

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.kanakupulla.MainActivity
import com.example.kanakupulla.R
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth

class RegisterationActivity : AppCompatActivity() {
    private lateinit var mEmail: EditText
    private lateinit var mPass: EditText
    private lateinit var cmPass: EditText
    private lateinit var btnReg: Button
    private lateinit var mSignin: TextView
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registeration)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        mAuth = FirebaseAuth.getInstance()
        mDialog = ProgressDialog(this)
        registration()
    }

    private fun registration() {
        mEmail = findViewById(R.id.email_register)
        mPass = findViewById(R.id.password_register) // Should this be findViewById(R.id.password_register) instead?
        cmPass = findViewById(R.id.cmpassword_register)
        btnReg = findViewById(R.id.Btn_register)
        mSignin = findViewById(R.id.Signup_reg)

        btnReg.setOnClickListener {
            val email = mEmail.text.toString().trim()
            val cnpass = cmPass.text.toString().trim()
            val pass = mPass.text.toString().trim()

            if (TextUtils.isEmpty(email)) {
                mEmail.error = "Email Required"
            }
            if (TextUtils.isEmpty(pass)) {
                mPass.error = "Password Required"
            }
            if (TextUtils.isEmpty(cnpass)) {
                cmPass.error = "Confirm Password Required"
            }
            if (cnpass != pass) {
                mPass.error = "Password Not Match"
                cmPass.error = "Password Not Match"
            } else {
                mDialog.setMessage("Processing..")
                mDialog.show()
                mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(this, OnCompleteListener { task ->
                    if (task.isSuccessful) {
                        mDialog.dismiss()
                        Toast.makeText(applicationContext, "Registration Complete", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(applicationContext, MainActivity::class.java))
                        finish()
                    } else {
                        mDialog.dismiss()
                        Toast.makeText(applicationContext, "Registration Failed", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }
    }

    fun tologin(view: View) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
