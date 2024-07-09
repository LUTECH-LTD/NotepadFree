package lutech.intern.noteapp.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import lutech.intern.noteapp.data.entity.Category
import lutech.intern.noteapp.data.entity.NoteWithCategories
import lutech.intern.noteapp.databinding.ItemCategorySelectBinding

class CategorySelectedAdapter() :
    RecyclerView.Adapter<CategorySelectedAdapter.CategorySelectedViewHolder>() {
    private val categories = mutableListOf<Category>()
    private val noteCategories = mutableListOf<Category>()
    private var onCheckedChange: ((Category, Boolean) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategorySelectedViewHolder {
        val binding =
            ItemCategorySelectBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategorySelectedViewHolder(binding)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(categories: List<Category>, noteCategories: List<Category>) {
        this.categories.clear()
        this.categories.addAll(categories)
        this.noteCategories.clear()
        this.noteCategories.addAll(noteCategories)
        notifyDataSetChanged()
    }

    fun setOnCheckedChange(onCheckedChange: ((Category, Boolean) -> Unit)) {
        this.onCheckedChange = onCheckedChange
    }

    override fun getItemCount() = categories.size

    override fun onBindViewHolder(holder: CategorySelectedViewHolder, position: Int) {
        holder.onBind(categories[position])
    }

    inner class CategorySelectedViewHolder(private val binding: ItemCategorySelectBinding) :
        ViewHolder(binding.root) {
        fun onBind(category: Category) {
            binding.checkbox.isChecked = noteCategories.contains(category)
            binding.nameCategoryTextView.text = category.name
            binding.checkbox.setOnCheckedChangeListener { _, isChecked ->
                onCheckedChange?.invoke(category, isChecked)
            }
        }
    }
}