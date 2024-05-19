package com.mad.kanakupulla

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class Profile_page : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase: DatabaseReference
    private lateinit var firestore: FirebaseFirestore
    private var isEditing: Boolean = false
    private lateinit var profilePic: CircleImageView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_page)
        enableEdgeToEdge()

        firestore = FirebaseFirestore.getInstance()

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference.child("Users")

        val currentUser: FirebaseUser? = mAuth.currentUser
        val uid: String = currentUser?.uid ?: ""
        val Femail: String? = currentUser?.email
        val emailTextView: TextView = findViewById(R.id.textView6)
        emailTextView.text = Femail
        val mobileTextView: EditText = findViewById(R.id.textView10)
        profilePic = findViewById(R.id.imageView7)
        val passchagne: TextView = findViewById(R.id.textView8)

        val dobTextView: EditText = findViewById(R.id.textView12)
        val AccountNumberText: EditText = findViewById(R.id.textView14)

        val mobileNumRef = FirebaseDatabase.getInstance().reference.child("Users").child(uid).child("mobile")
        mobileNumRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val mobileNumber = dataSnapshot.getValue(String::class.java)
                if (mobileNumber != null) {
                    mobileTextView.setText(mobileNumber)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
            }
        })

        val accref = FirebaseDatabase.getInstance().reference.child("Users").child(uid).child("AccNo")
        accref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val Accountnumber = dataSnapshot.getValue(String::class.java)
                if (Accountnumber != null) {
                    AccountNumberText.setText(Accountnumber)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
            }
        })

        val DobRef = FirebaseDatabase.getInstance().reference.child("Users").child(uid).child("dob")
        DobRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val DOB = dataSnapshot.getValue(String::class.java)
                if (DOB != null) {
                    dobTextView.setText(DOB)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
            }
        })

        // Fetch user image from Firebase Firestore
        firestore.collection("images")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                val imageUrl = document.getString("imageUrl")
                // If image URL is found, load the image into ImageView
                if (!imageUrl.isNullOrEmpty()) {
                    // Use a library like Picasso or Glide to load the image from URL
                    Picasso.get().load(imageUrl).into(profilePic)
                } else {
                    // If no image URL is found, set a default image
                    profilePic.setImageResource(R.drawable.pror)
                }
            }
            .addOnFailureListener { exception ->
                // Handle errors
                Log.d("Error in displaying image", "$exception")
            }

        val editButton: TextView = findViewById(R.id.textView16)
        editButton.setOnClickListener {
            if (!isEditing) {
                // Switch to EditText
                mobileTextView.isEnabled = true
                dobTextView.isEnabled = true
                AccountNumberText.isEnabled = true
                isEditing = true
                profilePic.setOnClickListener {
                    openImagePicker()
                }
                passchagne.setOnClickListener {
                    val currentUser = mAuth.currentUser
                    val uid = currentUser?.uid
                    if (uid != null) {
                        resetPassword(Femail.toString())
                    } else {
                        Toast.makeText(
                            applicationContext,
                            "User not authenticated",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                editButton.setText("Save")
            } else {
                mobileTextView.isEnabled = false
                dobTextView.isEnabled = false
                AccountNumberText.isEnabled = false
                isEditing = false
                val mobile: String = mobileTextView.text.toString().trim()
                val dob: String = dobTextView.text.toString().trim()
                val accno: String = AccountNumberText.text.toString().trim()

                // Save user data to Firebase Realtime Database and Firestore
                saveUserDataToFirestore(mobile, dob, accno)
                saveUserDataToRealtimeDatabase(mobile, dob, accno)

                editButton.setText("Edit")
            }
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        pickImageLauncher.launch(intent)
    }

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val imageUri = data?.data
                profilePic.setImageURI(imageUri)

                // Upload image to Firebase Firestore
                uploadImageToFirestore(imageUri)
            }
        }

    private fun uploadImageToFirestore(imageUri: Uri?) {
        imageUri?.let { uri ->
            // Access a Cloud Firestore instance
            val db = FirebaseFirestore.getInstance()

            // Generate a unique filename for the image
            val filename = UUID.randomUUID().toString()

            // Access a Cloud Storage instance
            val storageRef = FirebaseStorage.getInstance().reference.child("images/$filename")

            // Upload image to Cloud Storage
            storageRef.putFile(uri)
                .addOnSuccessListener { taskSnapshot ->
                    // Get the download URL from the task snapshot
                    storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        // Update the image URL in Firestore under the user's UID
                        val uid = mAuth.uid.toString()
                        firestore.collection("images")
                            .document(uid)
                            .set(hashMapOf("imageUrl" to downloadUri.toString()))
                            .addOnSuccessListener {
                                // Image URL successfully updated
                            }
                            .addOnFailureListener { e ->
                                // Handle errors
                                Log.d("Error updating image URL", "$e")
                            }
                    }
                }
                .addOnFailureListener { e ->
                    // Handle errors
                    Log.d("Error uploading image", "$e")
                }
        }
    }

    private fun saveUserDataToFirestore(mobile: String, dob: String, accno: String) {
        val currentUser: FirebaseUser? = mAuth.currentUser
        val uid: String = currentUser?.uid ?: ""

        // Create a HashMap to store user data
        val userMap = HashMap<String, Any>()
        userMap["mobile"] = mobile
        userMap["dob"] = dob
        userMap["AccNo"] = accno

        // Store the data in Firebase Firestore under the user's UID
        firestore.collection("Users").document(uid)
            .set(userMap)
            .addOnSuccessListener {
                // Data successfully saved
                // You can add any necessary UI updates or notifications here
            }
            .addOnFailureListener { e ->
                // Handle any errors that occurred while saving data
                Log.d("Error saving user data", "$e")
            }
    }

    private fun saveUserDataToRealtimeDatabase(mobile: String, dob: String, accno: String) {
        val currentUser: FirebaseUser? = mAuth.currentUser
        val uid: String = currentUser?.uid ?: ""

        // Create a HashMap to store user data
        val userMap = HashMap<String, Any>()
        userMap["mobile"] = mobile
        userMap["dob"] = dob
        userMap["AccNo"] = accno

        // Store the data in Firebase Realtime Database under the user's UID
        mDatabase.child(uid).updateChildren(userMap)
            .addOnSuccessListener {
                // Data successfully saved
                // You can add any necessary UI updates or notifications here
            }
            .addOnFailureListener { e ->
                // Handle any errors that occurred while saving data
                Log.d("Error saving user data in Realtime Database", "$e")
            }
    }

    private fun resetPassword(email: String) {
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
