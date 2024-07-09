package lutech.intern.noteapp.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import lutech.intern.noteapp.data.entity.Category
import lutech.intern.noteapp.data.entity.NoteWithCategories
import lutech.intern.noteapp.databinding.ItemCategorySelectBinding

class CategorySelectionAdapter() : RecyclerView.Adapter<CategorySelectionAdapter.CategorySelectionViewHolder>() {
    private val categories = mutableListOf<Category>()
    private val selectedCategories = mutableListOf<Category>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategorySelectionViewHolder {
        val binding = ItemCategorySelectBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategorySelectionViewHolder(binding)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(categories: List<Category>) {
        this.categories.clear()
        this.categories.addAll(categories)
        notifyDataSetChanged()
    }


    override fun getItemCount() = categories.size

    override fun onBindViewHolder(holder: CategorySelectionViewHolder, position: Int) {
        holder.onBind(categories[position])
    }

    inner class CategorySelectionViewHolder(private val binding: ItemCategorySelectBinding) : ViewHolder(binding.root) {
        fun onBind(category: Category) {
            binding.checkbox.isChecked = selectedCategories.contains(category)
            binding.nameCategoryTextView.text = category.name
            binding.checkbox.setOnCheckedChangeListener { _, isChecked ->
                if(isChecked) {
                    selectedCategories.add(category)
                } else {
                    selectedCategories.remove(category)
                }
            }
        }
    }

    fun getSelectedCategories() : List<Category> {
        return selectedCategories
    }

    fun getCategories() : List<Category> {
        return categories
    }

    fun clearSelectedCategories() {
        this.selectedCategories.clear()
        notifyDataSetChanged()
    }
}