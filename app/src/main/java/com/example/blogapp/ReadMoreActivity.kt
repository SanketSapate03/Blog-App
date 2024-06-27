package com.example.blogapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.blogapp.Model.BlogItemModel
import com.example.blogapp.databinding.ActivityReadMoreBinding
import com.example.blogapp.databinding.BlogItemBinding

class ReadMoreActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReadMoreBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding= ActivityReadMoreBinding.inflate(layoutInflater)

        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //click listner on bak btn
        binding.backBtn.setOnClickListener{
            finish()
        }
        //receive data send form adpter
        val blogs=intent.getParcelableExtra<BlogItemModel>("blogItem")
        if(blogs!=null){
            //retrieve user related data
            binding.titleText.text=blogs.heading
            binding.profileName.text=blogs.userName
            binding.readMoreDate.text=blogs.date
            binding.blogDescriptionTextView.text=blogs.post

            val userImageUrl=blogs.profileImageUrl
            Glide.with(this)
                .load(userImageUrl)
                .apply(RequestOptions.circleCropTransform())
                .into(binding.profileImage)

        }else{
            Toast.makeText(this, "Failed to load blog", Toast.LENGTH_SHORT).show()
        }
    }
}