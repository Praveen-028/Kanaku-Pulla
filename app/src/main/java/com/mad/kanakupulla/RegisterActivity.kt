package com.mad.kanakupulla

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
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

class RegisterActivity : AppCompatActivity() {
    private lateinit var mEmail: EditText
    private lateinit var mPass: EditText
    private lateinit var cmPass: EditText
    private lateinit var btnReg: ImageView
    private lateinit var mSignin: TextView
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register) // Make sure this layout ID matches your XML file
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        mAuth = FirebaseAuth.getInstance()
        mAuth.currentUser?.let {
            startActivity(Intent(applicationContext, Login2Activity::class.java))
            finish()
        }
        mDialog = ProgressDialog(this)
        registration()
    }

    private fun isValidEmail(email: String): Boolean {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun registration() {
        mEmail = findViewById(R.id.email_register) ?: return
        mPass = findViewById(R.id.password_register) ?: return
        cmPass = findViewById(R.id.cmpassword_register) ?: return
        btnReg = findViewById(R.id.Btn_register) ?: return
        mSignin = findViewById(R.id.Signup_reg) ?: return

        btnReg.setOnClickListener {
            val email = mEmail.text.toString().trim()
            val cnpass = cmPass.text.toString().trim()
            val pass = mPass.text.toString().trim()

            if (!isValidEmail(email)) {
                mEmail.error = "Enter a valid email address"
                return@setOnClickListener
            }
            if (TextUtils.isEmpty(pass)) {
                mPass.error = "Password Required"
                return@setOnClickListener
            }
            if (TextUtils.isEmpty(cnpass)) {
                cmPass.error = "Confirm Password Required"
                return@setOnClickListener
            }
            if (cnpass != pass) {
                mPass.error = "Password Not Match"
                cmPass.error = "Password Not Match"
                return@setOnClickListener
            }

            mDialog.setMessage("Processing..")
            mDialog.show()
            mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(this, OnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = mAuth.currentUser
                    user?.sendEmailVerification()
                        ?.addOnCompleteListener { verificationTask ->
                            if (verificationTask.isSuccessful) {
                                mDialog.dismiss()
                                Toast.makeText(
                                    applicationContext,
                                    "Registration Complete. Please check your email for verification.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                startActivity(
                                    Intent(
                                        applicationContext,
                                        Login2Activity::class.java
                                    )
                                )
                                finish()
                            } else {
                                mDialog.dismiss()
                                Toast.makeText(
                                    applicationContext,
                                    "Failed to send verification email.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                } else {
                    mDialog.dismiss()
                    Toast.makeText(applicationContext, "Registration Failed", Toast.LENGTH_SHORT).show()
                }
            })
        }
        mSignin.setOnClickListener{
            val intent = Intent(this, Login2Activity::class.java)
            startActivity(intent)
            finish()
        }
    }

    fun tologin() {
        val intent = Intent(this, Login2Activity::class.java)
        startActivity(intent)
        finish()
    }
}
