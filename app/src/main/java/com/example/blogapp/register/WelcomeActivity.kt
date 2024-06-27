package com.example.blogapp.register

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.blogapp.MainActivity
import com.example.blogapp.R
import com.example.blogapp.SigninAndRegistrationActivity
import com.example.blogapp.databinding.ActivityWelcomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser


class WelcomeActivity : AppCompatActivity() {
lateinit var binding:ActivityWelcomeBinding
    private lateinit var auth:FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityWelcomeBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        auth= FirebaseAuth.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.loginBtn.setOnClickListener {
            val intent=Intent(this,SigninAndRegistrationActivity::class.java)
            intent.putExtra("action","login")
            startActivity(intent)

        }

        binding.registerBtn.setOnClickListener {
            val intent=Intent(this,SigninAndRegistrationActivity::class.java)
            intent.putExtra("action","register")
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser: FirebaseUser?=auth.currentUser

        if(currentUser!=null){
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}