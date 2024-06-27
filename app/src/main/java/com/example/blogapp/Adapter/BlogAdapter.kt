package com.example.blogapp.Adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.blogapp.Model.BlogItemModel
import com.example.blogapp.R
import com.example.blogapp.ReadMoreActivity
import com.example.blogapp.databinding.BlogItemBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class BlogAdapter(private val items: MutableList<BlogItemModel>) :
    RecyclerView.Adapter<BlogAdapter.BlogViewHolder>() {

    //declare var
    private val databaseReference: DatabaseReference =
        FirebaseDatabase.getInstance("https://blog-app-49a72-default-rtdb.asia-southeast1.firebasedatabase.app").reference
    private val currentUSer = FirebaseAuth.getInstance().currentUser

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlogViewHolder {
        val inflater: LayoutInflater = LayoutInflater.from(parent.context)
        val binding: com.example.blogapp.databinding.BlogItemBinding =
            BlogItemBinding.inflate(inflater, parent, false)
        return BlogViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BlogViewHolder, position: Int) {
        val blogItem: BlogItemModel = items[position]
        holder.bind(blogItem)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class BlogViewHolder(private val binding: BlogItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(blogItemModel: BlogItemModel) {
            binding.heading.text = blogItemModel.heading

            val postId = blogItemModel.postId
            val context = binding.root.context

            Glide.with(binding.profile.context).load(blogItemModel.profileImageUrl)
                .into(binding.profile)
            binding.userName.text = blogItemModel.userName
            binding.date.text = blogItemModel.date
            binding.post.text = blogItemModel.post
            binding.likeCountBtn.text = blogItemModel.likeCount.toString()

            //set on click listner
            binding.root.setOnClickListener {
                val context: Context = binding.root.context
                val intent = Intent(context, ReadMoreActivity::class.java)
                intent.putExtra("blogItem", blogItemModel)
                context.startActivity(intent)
            }

            //like btn listner
            //check current user has liked the post and update the like btn
            val postLikeReference = databaseReference.child("blogs").child(postId).child("likes")
            val currentUserLiked = currentUSer?.uid?.let { uid ->
                postLikeReference.child(uid)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                binding.likeBtn.setImageResource(R.drawable.heart_fill_red)
                            } else {
                                binding.likeBtn.setImageResource(R.drawable.heart_black)
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {

                        }

                    })
            }

            //handling click btn
            binding.likeBtn.setOnClickListener {
                if (currentUSer != null) {
                    handleLikedBtnClicked(postId, blogItemModel, binding)
                } else {
                    Toast.makeText(context, "You Have to Login First", Toast.LENGTH_SHORT).show()
                }
            }

            //set the initial icon based in saved status
            val userReference = databaseReference.child("users").child(currentUSer?.uid ?: "")
            val postSaveReference = userReference.child("saveBlogPost").child(postId)

            postSaveReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        //if blog already saved
                        binding.postSaveBtn.setImageResource(R.drawable.saved_articles_fill_red)
                    } else {
                        //if blog not saved yet
                        binding.postSaveBtn.setImageResource(R.drawable.unsaved_article_red)
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })

            //handling save button clicked
            binding.postSaveBtn.setOnClickListener {
                if (currentUSer != null) {
                    handleSaveBtnClicked(postId, blogItemModel, binding)
                } else {
                    Toast.makeText(context, "You Have to Login First", Toast.LENGTH_SHORT).show()
                }
            }


        }

    }


    private fun handleLikedBtnClicked(
        postId: String,
        blogItemModel: BlogItemModel,
        binding: BlogItemBinding,
    ) {
        val userReference = databaseReference.child("users").child(currentUSer!!.uid)
        val postLikeReference = databaseReference.child("blogs").child(postId).child("likes")

        //user has already liked then unlike
        postLikeReference.child(currentUSer.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        userReference.child("likes").child(postId).removeValue()
                            .addOnSuccessListener {
                                postLikeReference.child(currentUSer.uid).removeValue()
                                blogItemModel.likedBy?.remove(currentUSer.uid)
                                updateLikeBtnImage(binding, false)

                                //decrement the like in db
                                val newLikeCnt = blogItemModel.likeCount - 1
                                blogItemModel.likeCount = newLikeCnt
                                databaseReference.child("blogs").child(postId).child("likeCount")
                                    .setValue(newLikeCnt)
                                notifyDataSetChanged()
                            }.addOnFailureListener { e ->
                                Log.e("Liked Clicked", "FAILED TO UNLIKE $e")
                            }
                    } else {
                        //user has not liked the post so likedit
                        userReference.child("likes").child(postId).setValue(true)
                            .addOnSuccessListener {
                                postLikeReference.child(currentUSer.uid).setValue(true)
                                blogItemModel.likedBy?.add(currentUSer.uid)
                                updateLikeBtnImage(binding, true)

                                //increase the like in db
                                val newLikeCnt = blogItemModel.likeCount + 1
                                blogItemModel.likeCount = newLikeCnt
                                databaseReference.child("blogs").child(postId).child("likeCount")
                                    .setValue(newLikeCnt)
                                notifyDataSetChanged()
                            }.addOnFailureListener { e ->
                                Log.e("Liked Clicked", "FAILED TO LIKE $e")
                            }
                    }
                }

                private fun updateLikeBtnImage(binding: BlogItemBinding, liked: Boolean) {
                    if (liked) {
                        binding.likeBtn.setImageResource(R.drawable.heart_black)
                    } else {
                        binding.likeBtn.setImageResource(R.drawable.heart_fill_red)
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    private fun handleSaveBtnClicked(
        postId: String,
        blogItemModel: BlogItemModel,
        binding: BlogItemBinding,
    ) {
        val userReference = databaseReference.child("users").child(currentUSer!!.uid)
        userReference.child("saveBlogPost").child(postId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    if (snapshot.exists()) {
                        //the blog is currently saved so unsaved
                        userReference.child("saveBlogPost").child(postId).removeValue()
                            .addOnSuccessListener {
                                //update the ui
                                val clickedBlogItem = items.find { it.post == postId }
                                clickedBlogItem?.isSaved = false
                                notifyDataSetChanged()

                                val context = binding.root.context
                                Toast.makeText(context, "Blog Unsaved", Toast.LENGTH_SHORT).show()
                            }.addOnFailureListener {
                                val context = binding.root.context
                                Toast.makeText(
                                    context, "Failed to Unsave The Blog", Toast.LENGTH_SHORT
                                ).show()
                            }

                        binding.postSaveBtn.setImageResource(R.drawable.unsaved_article_red)
                    } else {
                        //blog is not saved so save it
                        userReference.child("saveBlogPost").child(postId).setValue(true)
                            .addOnSuccessListener {
                                //update ui
                                val clickedBlogItem = items.find { it.postId == postId }
                                clickedBlogItem?.isSaved = true
                                notifyDataSetChanged()

                                val context = binding.root.context
                                Toast.makeText(context, "Blog Saved", Toast.LENGTH_SHORT).show()
                            }.addOnFailureListener {
                                val context = binding.root.context
                                Toast.makeText(
                                    context, "Failed to Save the Blog", Toast.LENGTH_SHORT
                                ).show()
                            }

                        //change the button icon
                        binding.postSaveBtn.setImageResource(R.drawable.saved_articles_fill_red)
                    }


                }

                override fun onCancelled(error: DatabaseError) {

                }

            })

    }

    fun updateData(saveBlogArticles:List<BlogItemModel>) {
        items.clear()
        items.addAll(saveBlogArticles)
        notifyDataSetChanged()
    }
}