package com.example.blogapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.blogapp.Model.UserData
import com.example.blogapp.databinding.ActivitySigninAndRegistrationBinding
import com.example.blogapp.databinding.ActivityWelcomeBinding
import com.example.blogapp.register.WelcomeActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class SigninAndRegistrationActivity : AppCompatActivity() {
    private val binding: ActivitySigninAndRegistrationBinding by lazy {
        ActivitySigninAndRegistrationBinding.inflate(layoutInflater)
    }

    //for firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private val PICK_IMAGE_REQUEST = 1
    private var imageUri: Uri? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(binding.root)

        //initialising firebase authentication
        auth = FirebaseAuth.getInstance()
        database =
            FirebaseDatabase.getInstance("https://blog-app-49a72-default-rtdb.asia-southeast1.firebasedatabase.app")
        storage = FirebaseStorage.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
//for visibility of of fields
        val action: String? = intent.getStringExtra("action")

        //for login , adjust visibility for login
        if (action == "login") {
            binding.loginBtn.visibility = View.VISIBLE
            binding.loginEmail.visibility = View.VISIBLE
            binding.loginPassword.visibility = View.VISIBLE

            binding.cardView.visibility = View.GONE
            binding.registerName.visibility = View.GONE
            binding.registerEmail.visibility = View.GONE
            binding.registerPassword.visibility = View.GONE

            binding.registerBtn.isEnabled = false
            binding.registerNewHere.isEnabled = false

            binding.registerBtn.alpha = 0.5f
            binding.registerNewHere.alpha = 0.5f

            //when click on login button
            binding.loginBtn.setOnClickListener {
                val loginEmail: String = binding.loginEmail.text.toString()
                val loginPassword: String = binding.loginPassword.text.toString()

                if (loginEmail.isEmpty() || loginPassword.isEmpty()) {
                    Toast.makeText(this, "Please Fill All Details", Toast.LENGTH_SHORT).show()
                } else {
                    //logging
                    auth.signInWithEmailAndPassword(loginEmail, loginPassword)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(this, "Login Successful😎", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                            } else {
                                Toast.makeText(this, "Invalid Credential😒", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                }
            }

        } else if (action == "register") {
            binding.loginBtn.isEnabled = false
            binding.loginBtn

            binding.registerBtn.setOnClickListener {


                //get data from edit text field
                val registerName: String = binding.registerName.text.toString()
                val registerEmail: String = binding.registerEmail.text.toString()
                val registerPassword: String = binding.registerPassword.text.toString()

                if (registerName.isEmpty() || registerEmail.isEmpty() || registerPassword.isEmpty()) {
                    Toast.makeText(this, "Please Fill All the Details", Toast.LENGTH_SHORT).show()
                } else {
                    auth.createUserWithEmailAndPassword(registerEmail, registerPassword)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val user: FirebaseUser? = auth.currentUser
                                auth.signOut()
                                user?.let {

                                    //save user data into firebase realtime database
                                    val userReference: DatabaseReference =
                                        database.getReference("users") //node
                                    val userId: String = user.uid
                                    val userData = UserData(registerName, registerEmail)
                                    userReference.child(userId).setValue(userData)

                                    //upload image to firebase storage
                                    val storageReference: StorageReference = this.storage.reference.child("profile_image/$userId.jpg")
                                    storageReference.putFile(imageUri!!).addOnCompleteListener { task ->

                                            storageReference.downloadUrl.addOnCompleteListener { imageUri ->
                                                val imageUrl = imageUri.result.toString()

                                                //save image url to the realtime database
                                                userReference.child(userId).child("profileImage").setValue(imageUrl)


                                            }

                                        }

                                    Toast.makeText(this, "User Register Successfully", Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(this, WelcomeActivity::class.java))
                                    finish()
                                }

                            } else {
                                Toast.makeText(this, "User Registration Failed", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            }
        }


        //set on click lister for the choose the image
        binding.cardView.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(
                Intent.createChooser(intent, "select Image"),
                PICK_IMAGE_REQUEST
            )

        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == AppCompatActivity.RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
            Glide.with(this)
                .load(imageUri)
                .apply(RequestOptions.circleCropTransform())
                .into(binding.registerUserImage)
        }
    }

}
