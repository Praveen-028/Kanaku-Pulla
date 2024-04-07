package com.example.kanakupulla

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Email
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth

class Login2Activity : AppCompatActivity() {
    private  lateinit var mEmail: EditText
    private  lateinit var mPass: EditText
    private  lateinit var btnlogn: ImageView
    private  lateinit var forget: TextView
    private  lateinit var mlogin: TextView
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDialog: ProgressDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login2)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        mAuth=FirebaseAuth.getInstance()
        mAuth.currentUser?.let {
            startActivity(Intent(applicationContext, HomeScreen::class.java))
            finish()
        }

        mDialog= ProgressDialog(this)

        loginDetails()
    }
    private fun loginDetails(){
        mEmail = findViewById(R.id.email_login)
        mPass = findViewById(R.id.password_login)
        btnlogn = findViewById(R.id.Btn_login)
        forget = findViewById(R.id.forgetpassword)
//        mlogin = findViewById(R.id.Signup_reg)

        btnlogn.setOnClickListener{
            val email=mEmail.text.toString().trim()
            val pass=mPass.text.toString().trim()

            if (TextUtils.isEmpty(email)) {
                mEmail.setError("Email Required")
            }
            if (TextUtils.isEmpty(pass)) {
                mPass.setError("Password Required")
            }
            mDialog.setMessage("Processing..")
            mDialog.show()
            mAuth.signInWithEmailAndPassword(email,pass).addOnCompleteListener(this,
                OnCompleteListener { task ->

                    if (task.isSuccessful)
                    {
                        mDialog.dismiss()
                        Toast.makeText(applicationContext,"Login Successfull",Toast.LENGTH_SHORT).show()
                        startActivity(Intent(applicationContext,HomeScreen::class.java))
                        finish()
                    }else{
                        mDialog.dismiss()
                        Toast.makeText(applicationContext,"Login Failed",Toast.LENGTH_SHORT).show()
                    }
                })
        }

    }
    fun toregister(view: View) {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
        finish()
    }
        fun torest(view: View) {
            val email = mEmail.text.toString().trim()

            if (TextUtils.isEmpty(email)) {
                mEmail.setError("Email Required")
                return
            }

            // Send password reset email
            mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            applicationContext,
                            "Password reset email sent to $email",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            applicationContext,
                            "Failed to send password reset email",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }