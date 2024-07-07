package lutech.intern.noteapp.ui.category

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import lutech.intern.noteapp.R
import lutech.intern.noteapp.adapter.CategoryAdapter
import lutech.intern.noteapp.data.entity.Category
import lutech.intern.noteapp.databinding.DialogEditCategoryBinding
import lutech.intern.noteapp.databinding.FragmentCategoriesBinding

class CategoriesFragment : Fragment() {
    private val binding by lazy { FragmentCategoriesBinding.inflate(layoutInflater) }
    private val categoryViewModel: CategoryViewModel by viewModels()
    private val categoryAdapter by lazy { CategoryAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        observeCategoriesUpdate()
        handleEvent()
    }

    private fun initViews() {
        initRecyclerView()
    }

    private fun initRecyclerView() {
        binding.categoriesRecyclerView.adapter = categoryAdapter
    }

    private fun observeCategoriesUpdate() {
        categoryViewModel.categories.observe(viewLifecycleOwner) { categories ->
            categoryAdapter.submitCategories(categories)
        }
    }

    private fun handleEvent() {
        binding.addButton.setOnClickListener { insertCategory() }
        categoryAdapter.setOnItemClickListener(object : CategoryAdapter.OnItemClickListener {
            override fun onEditButtonListener(category: Category) {
                showEditCategoryDialog(category)
            }

            override fun onDeleteButtonListener(category: Category) {
                showConfirmDeleteCategoryDialog(category)
            }
        })

    }

    private fun insertCategory() {
        val name = binding.nameEditText.text.toString().trim()
        if (name.isEmpty()) {
            return
        }

        val category = Category(name = name)
        categoryViewModel.insert(category) { isInsertSuccessful ->
            if (isInsertSuccessful) {
                binding.nameEditText.text?.clear()
            }
        }
    }

    private fun showEditCategoryDialog(category: Category) {
        val dialogBinding = DialogEditCategoryBinding.inflate(layoutInflater)

        val builder = AlertDialog.Builder(requireContext())
        builder.apply {
            setView(dialogBinding.root)
            setPositiveButton(R.string.ok, null)
            setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
        }

        val dialog = builder.create()
        dialog.show()

        dialogBinding.nameEditText.setText(category.name)
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener { updateCategory(dialogBinding, category, dialog) }
    }

    private fun updateCategory(
        dialogBinding: DialogEditCategoryBinding,
        category: Category,
        dialog: AlertDialog
    ) {
        val newName = dialogBinding.nameEditText.text.toString().trim()

        if (newName.isEmpty()) {
            dialogBinding.messageError.visibility = View.VISIBLE
            return
        }

        val categoryToUpdate = Category(category.categoryId, newName)
        categoryViewModel.update(categoryToUpdate) { isUpdateSuccessful ->
            if (isUpdateSuccessful) {
                dialog.dismiss()
                dialogBinding.messageError.visibility = View.GONE
            } else {
                dialogBinding.messageError.visibility = View.VISIBLE
            }
        }
    }

    private fun showConfirmDeleteCategoryDialog(category: Category) {
        val builder = AlertDialog.Builder(requireContext())
        builder.apply {
            setMessage("Delete category '${category.name}'? Notes from the category won't be deleted.")
            setPositiveButton(R.string.ok) { _, _ ->
                deleteCategory(category)
            }
            setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun deleteCategory(category: Category) {
        categoryViewModel.delete(category)
    }

    companion object {
        fun newInstance() = CategoriesFragment()
    }
}