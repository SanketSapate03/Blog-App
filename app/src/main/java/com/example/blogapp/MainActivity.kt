package com.example.blogapp

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.blogapp.Adapter.BlogAdapter
import com.example.blogapp.Model.BlogItemModel
import com.example.blogapp.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private lateinit var databaseReference: DatabaseReference
    private val blogItems = mutableListOf<BlogItemModel>()
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //to go on profile section
        binding.profileImage.setOnClickListener{
            startActivity(Intent(this,ProfileActivity::class.java))
        }
        //to go on profile section
        binding.cardView2.setOnClickListener{
            startActivity(Intent(this,ProfileActivity::class.java))
        }

        //to go on saved post
        binding.saveArticleBtn.setOnClickListener{
            startActivity(Intent(this,SavedArticlesActivity::class.java))
        }

        //fetching user
        auth = FirebaseAuth.getInstance()
        databaseReference =
            FirebaseDatabase.getInstance("https://blog-app-49a72-default-rtdb.asia-southeast1.firebasedatabase.app").reference.child(
                "blogs"
            )

        val userId: String? = auth.currentUser?.uid

        //set user profile
        if (userId != null) {
            loadUserProfileImage(userId)
        }

        //set up blog post into recycler view
        //initialize the recycler view and set up the adapter
        val recyclerView: RecyclerView = binding.blogRecyclerView
        val blogAdapter = BlogAdapter(blogItems)

        recyclerView.adapter = blogAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        //fetch data from db 
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                blogItems.clear()
                for (snapshot in snapshot.children) {
                    val blogItem = snapshot.getValue(BlogItemModel::class.java)
                    if (blogItem != null) {
                        blogItems.add(blogItem)
                    }

                }
                //reverses the list
                blogItems.reverse()

                //notify the adapter that the data has changed
                blogAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Blog  loading failed", Toast.LENGTH_SHORT).show()
            }

        })


        binding.floatingAddArticleBtn.setOnClickListener {
            startActivity(Intent(this, AddArticleActivity::class.java))
        }

    }

    //function to load image from db
    private fun loadUserProfileImage(userId: String) {
        val userReference: DatabaseReference =
            FirebaseDatabase.getInstance("https://blog-app-49a72-default-rtdb.asia-southeast1.firebasedatabase.app")
                .reference.child("users").child(userId)

        userReference.child("profileImage").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val profileImageUrl: String? = snapshot.getValue(String::class.java)
                if (profileImageUrl != null) {
                    Glide.with(this@MainActivity).load(profileImageUrl).into(binding.profileImage)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Error loading profile image", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

}
