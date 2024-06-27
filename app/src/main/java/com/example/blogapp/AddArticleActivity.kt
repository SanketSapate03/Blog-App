package com.example.blogapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.blogapp.Model.BlogItemModel
import com.example.blogapp.Model.UserData
import com.example.blogapp.databinding.ActivityAddArticleBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date

class AddArticleActivity : AppCompatActivity() {
    private val binding: ActivityAddArticleBinding by lazy {
        ActivityAddArticleBinding.inflate(layoutInflater)
    }

    private val databaseReference: DatabaseReference =
        FirebaseDatabase.getInstance("https://blog-app-49a72-default-rtdb.asia-southeast1.firebasedatabase.app")
            .getReference("blogs")
    private val userReference: DatabaseReference =
        FirebaseDatabase.getInstance("https://blog-app-49a72-default-rtdb.asia-southeast1.firebasedatabase.app")
            .getReference("users")
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //click listner on bak btn
        binding.addBlogBtn.setOnClickListener {
            finish()
        }

        binding.addBlogBtn.setOnClickListener {
            val title = binding.blogTitle.editText?.text.toString().trim()
            val description = binding.blogDescription.editText?.text.toString().trim()

            if (title.isEmpty() || description.isEmpty()) {
                Toast.makeText(this, "Please Fill All the Fields", Toast.LENGTH_SHORT).show()
            }

            //get current user
            val user: FirebaseUser? = auth.currentUser
            if (user != null) {
                val userId = user.uid
                val userName = user.displayName ?: "Anonymous"
                val userImageUrl = user.photoUrl ?: ""

                //fetching user name and user profile from database
                userReference.child(userId)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val userData: UserData? = snapshot.getValue(UserData::class.java)
                            if (userData != null) {
                                val userNameFromDB: String = userData.name
                                val userImageUrlFromDB: String = userData.profileImage

                                //val current date
                                val currentDate = SimpleDateFormat("yyyy-MM-dd").format(Date())

                                //creating a blog item model
                                val blogItem = BlogItemModel(
                                    title,
                                    userNameFromDB,
                                    currentDate,
                                    description,
                                    userId,
                                    0,
                                    userImageUrlFromDB
                                )

                                //generating unique key for the blog post
                                val key = databaseReference.push().key
                                if (key != null) {
                                    blogItem.postId = key
                                    val blogreferance: DatabaseReference =
                                        databaseReference.child(key)
                                    blogreferance.setValue(blogItem).addOnCompleteListener {
                                        if (it.isSuccessful) finish()
                                        else Toast.makeText(
                                            this@AddArticleActivity,
                                            "Failed to add blog",
                                            Toast.LENGTH_SHORT
                                        ).show()


                                    }

                                }

                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            TODO("Not yet implemented")
                        }

                    })
            }
        }

    }
}