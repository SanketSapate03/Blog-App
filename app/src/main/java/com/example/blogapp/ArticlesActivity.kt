package com.example.blogapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.AdapterView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.blogapp.Adapter.ArticleAdapter
import com.example.blogapp.Model.BlogItemModel
import com.example.blogapp.databinding.ActivityArticlesBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ArticlesActivity : AppCompatActivity() {
    private val binding: ActivityArticlesBinding by lazy {
        ActivityArticlesBinding.inflate(layoutInflater)
    }

    private lateinit var databaseReference: DatabaseReference
    private val auth = FirebaseAuth.getInstance()
    private lateinit var articleAdapter: ArticleAdapter
    private val EDIT_BLOGREQUEST_CODE=123


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //handle back brn
        binding.backButton.setOnClickListener{
            finish()
        }

        val currentUserId = auth.currentUser?.uid
        val recyclerView = binding.articlesRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)

        if(currentUserId!=null){
            articleAdapter =
                ArticleAdapter(this, emptyList(), object : ArticleAdapter.OnItemClickListener {
                    override fun onEditClickListner(blogItem: BlogItemModel) {
                        val intent= Intent(this@ArticlesActivity,EditBlogActivity::class.java)
                        intent.putExtra("blogItem",blogItem)
                        startActivityForResult(intent,EDIT_BLOGREQUEST_CODE)
                    }

                    override fun onReadMoreClickListner(blogItem: BlogItemModel) {
                        val intent= Intent(this@ArticlesActivity,ReadMoreActivity::class.java)
                        intent.putExtra("blogItem",blogItem)
                        startActivity(intent)
                    }

                    override fun onDeleteClickListner(blogItem: BlogItemModel) {
                        deleteBlogPost(blogItem)
                    }

                })
        }

        recyclerView.adapter = articleAdapter

        //get saved blog data from db
        databaseReference =
            FirebaseDatabase.getInstance("https://blog-app-49a72-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("blogs")
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val blogSavedList = ArrayList<BlogItemModel>()

                for (postSnapshot in snapshot.children) {
                    val blogSaved: BlogItemModel? = postSnapshot.getValue(BlogItemModel::class.java)

                    if (blogSaved != null && currentUserId == blogSaved.userId) {
                        blogSavedList.add(blogSaved)
                    }
                }
                articleAdapter.setData(blogSavedList)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@ArticlesActivity,
                    "Error loading your articles",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

    }

    //deletion of article work
    private fun deleteBlogPost(blogItem: BlogItemModel) {
        val postId = blogItem.postId
        val blogPostReference = databaseReference.child(postId)
        blogPostReference.removeValue()

            .addOnSuccessListener {
                Toast.makeText(
                    this@ArticlesActivity,
                    "Blog Post Deleted Successfully",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener {
                Toast.makeText(
                    this@ArticlesActivity,
                    "Blog Post Deletion Unsuccessful",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==EDIT_BLOGREQUEST_CODE && resultCode== Activity.RESULT_OK){

        }
    }
}