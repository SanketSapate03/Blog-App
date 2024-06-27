package com.example.blogapp.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.blogapp.Model.BlogItemModel
import com.example.blogapp.databinding.ActivityArticlesBinding
import com.example.blogapp.databinding.ArticlesItemBinding

class ArticleAdapter(
    private val context: Context,
    private var blogList: List<BlogItemModel>,
    private val itemClickListener: OnItemClickListener,
) : RecyclerView.Adapter<ArticleAdapter.ArticleViewHolder>() {

    interface OnItemClickListener {
        fun onEditClickListner(blogItem: BlogItemModel)
        fun onReadMoreClickListner(blogItem: BlogItemModel)
        fun onDeleteClickListner(blogItem: BlogItemModel)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ArticleAdapter.ArticleViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ArticlesItemBinding.inflate(inflater, parent, false)
        return ArticleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ArticleAdapter.ArticleViewHolder, position: Int) {
        val blogItem = blogList[position]
        holder.bind(blogItem)
    }

    override fun getItemCount(): Int {
        return blogList.size
    }

    fun setData(blogSavedList: ArrayList<BlogItemModel>) {
        this.blogList = blogSavedList
        notifyDataSetChanged()
    }

    inner class ArticleViewHolder(private val binding: ArticlesItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(blogItem: BlogItemModel) {

            binding.heading.text = blogItem.heading
            Glide.with(binding.profile.context).load(blogItem.profileImageUrl)
                .into(binding.profile)
            binding.userName.text = blogItem.userName
            binding.date.text = blogItem.date
            binding.post.text = blogItem.post


            //handle read more
            binding.readMoreBtn.setOnClickListener {
                itemClickListener.onReadMoreClickListner(blogItem)
            }

            //handle edit
            binding.editbtn.setOnClickListener {
                itemClickListener.onEditClickListner(blogItem)
            }

            //handle delete
            binding.deleteBtn.setOnClickListener {
                itemClickListener.onDeleteClickListner(blogItem)
            }

        }

    }

}