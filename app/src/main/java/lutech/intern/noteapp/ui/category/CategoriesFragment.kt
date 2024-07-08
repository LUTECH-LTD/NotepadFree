package lutech.intern.noteapp.ui.category

import android.app.AlertDialog
import android.app.Dialog
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
        observeViewModel()
        initCategoriesRecyclerView()
        initEvents()
    }

    private fun observeViewModel() {
        categoryViewModel.categories.observe(viewLifecycleOwner) { categories ->
            categoryAdapter.submitCategories(categories)
        }
    }

    private fun initCategoriesRecyclerView() {
        binding.rcvCategories.adapter = categoryAdapter
    }

    private fun initEvents() {
        binding.btnAdd.setOnClickListener { handleInsertCategory() }

        categoryAdapter.setOnItemClickListener(object : CategoryAdapter.OnItemClickListener {
            override fun onEditButtonClickListener(category: Category) {
                showEditCategoryDialog(category)
            }

            override fun onDeleteButtonClickListener(category: Category) {
                showDeleteCategoryDialog(category)
            }
        })

    }

    private fun handleInsertCategory() {
        val name = binding.edtName.text.toString().trim()
        if (name.isEmpty()) {
            return
        }

        val category = Category(name = name)
        insertCategory(category)
    }

    private fun insertCategory(category: Category) {
        categoryViewModel.insertCategory(category) { isSuccess ->
            if (isSuccess) {
                binding.edtName.text?.clear()
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
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            handleUpdateCategory(dialogBinding, category, dialog)
        }
    }

    private fun handleUpdateCategory(
        dialogBinding: DialogEditCategoryBinding,
        category: Category,
        dialog: Dialog,
    ) {
        val name = dialogBinding.nameEditText.text.toString().trim()
        if (name.isEmpty()) {
            dialogBinding.messageError.visibility = View.VISIBLE
            return
        }

        val categoryToUpdate = Category(category.categoryId, name)
        updateCategory(categoryToUpdate, dialog, dialogBinding)
    }

    private fun updateCategory(
        category: Category,
        dialog: Dialog,
        dialogBinding: DialogEditCategoryBinding
    ) {
        categoryViewModel.updateCategory(category) { isSuccess ->
            if (isSuccess) {
                dialog.dismiss()
                dialogBinding.messageError.visibility = View.GONE
            } else {
                dialogBinding.messageError.visibility = View.VISIBLE
            }
        }
    }

    private fun showDeleteCategoryDialog(category: Category) {
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
        categoryViewModel.deleteCategory(category)
    }

    companion object {
        fun newInstance() = CategoriesFragment()
    }
}