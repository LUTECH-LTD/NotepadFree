package lutech.intern.noteapp.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import lutech.intern.noteapp.constant.Constants
import lutech.intern.noteapp.constant.MenuMode
import lutech.intern.noteapp.data.entity.Category
import lutech.intern.noteapp.databinding.ItemCategoryBinding
import lutech.intern.noteapp.event.MenuModeEvent
import lutech.intern.noteapp.ui.main.MainActivity
import org.greenrobot.eventbus.EventBus

class CategoryAdapter : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {
    private val categories = mutableListOf<Category>()
    private var listener: OnItemClickListener? = null
    @SuppressLint("NotifyDataSetChanged")
    fun submitCategories(categories: List<Category>) {
        this.categories.clear()
        this.categories.addAll(categories)
        notifyDataSetChanged()
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding)
    }

    override fun getItemCount() = categories.size

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.onBind(categories[position])
    }

    inner class CategoryViewHolder(private val binding: ItemCategoryBinding) : ViewHolder(binding.root) {
        fun onBind(category: Category) {
            binding.nameCategoryTextView.text = category.name
            binding.editButton.setOnClickListener { listener?.onEditButtonListener(category) }
            binding.deleteButton.setOnClickListener { listener?.onDeleteButtonListener(category) }
        }
    }

    interface OnItemClickListener {
        fun onEditButtonListener(category: Category)
        fun onDeleteButtonListener(category: Category)
    }
}