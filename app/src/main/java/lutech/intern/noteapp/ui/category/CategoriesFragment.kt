package lutech.intern.noteapp.ui.category

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import lutech.intern.noteapp.R
import lutech.intern.noteapp.adapter.CategoryAdapter
import lutech.intern.noteapp.data.model.Category
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
        initViewModel()
        handleEvent()
    }

    private fun initViews() {
        initRecyclerView()
    }

    private fun initRecyclerView() {
        binding.categoriesRecyclerView.adapter = categoryAdapter
    }

    private fun initViewModel() {
        categoryViewModel.categories.observe(viewLifecycleOwner) { categories ->
            categoryAdapter.submitCategories(categories)
        }
    }

    private fun handleEvent() {
        binding.addButton.setOnClickListener {
            val name = binding.nameEditText.text.toString().trim()
            if (name.isNotEmpty()) {
                categoryViewModel.insert(Category(name = name))
                binding.nameEditText.text?.clear()
            }
        }

        categoryAdapter.setOnItemClickListener(object : CategoryAdapter.OnItemClickListener {
            override fun onEditButtonClicked(category: Category) {
                showDialogEditCategory(category)
            }

            override fun onDeleteButtonClicked(category: Category) {
                showDialogConfirmDeleteCategory(category)
            }
        })
    }

    private fun showDialogConfirmDeleteCategory(category: Category) {
        val builder = AlertDialog.Builder(requireContext())
        builder.apply {
            setMessage("Delete category '${category.name}'? Notes from the category won't be deleted.")
            setPositiveButton(R.string.ok) { _, _ ->
                categoryViewModel.delete(category)
            }
            setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun showDialogEditCategory(category: Category) {
        val dialogBinding = DialogEditCategoryBinding.inflate(layoutInflater)
        dialogBinding.nameEditText.setText(category.name)

        val builder = AlertDialog.Builder(requireContext())
        builder.apply {
            setView(dialogBinding.root)
            setPositiveButton(R.string.ok) { dialog, _ ->
                val newName = dialogBinding.nameEditText.text.toString().trim()
                if (newName.isNotEmpty()) {
                    categoryViewModel.update(Category(category.id, newName))
                }
            }
            setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
        }

        val dialog = builder.create()
        dialog.show()
    }

    companion object {
        fun newInstance() = CategoriesFragment()
    }
}