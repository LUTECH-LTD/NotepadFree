package lutech.intern.noteapp.ui.category

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
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
        observeDataViewModel()
        handleEvent()
    }

    private fun initViews() {
        initRecyclerView()
    }

    private fun initRecyclerView() {
        binding.categoriesRecyclerView.adapter = categoryAdapter
    }

    private fun observeDataViewModel() {
        categoryViewModel.categories.observe(viewLifecycleOwner) { categories ->
            categoryAdapter.submitCategories(categories)
        }

        categoryViewModel.insertResult.observe(viewLifecycleOwner) { isSuccess ->
            if (isSuccess) {
                binding.nameEditText.text?.clear()
            }
        }
    }

    private fun handleEvent() {
        binding.addButton.setOnClickListener {
            val name = binding.nameEditText.text.toString().trim()
            if (name.isNotEmpty()) {
                categoryViewModel.insert(Category(name = name))
            }
        }

        categoryAdapter.setOnItemClickListener(object : CategoryAdapter.OnItemClickListener {
            override fun onEditButtonListener(category: Category) {
                showEditCategoryDialog(category)
            }

            override fun onDeleteButtonListener(category: Category) {
                showConfirmDeleteCategoryDialog(category)
            }
        })
    }

    private fun showEditCategoryDialog(category: Category) {
        val dialogBinding = DialogEditCategoryBinding.inflate(layoutInflater)
        dialogBinding.nameEditText.setText(category.name)

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

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val newName = dialogBinding.nameEditText.text.toString().trim()
            if (newName.isNotEmpty()) {
                categoryViewModel.update(Category(category.id, newName))
            } else {
                dialogBinding.messageError.visibility = View.VISIBLE
            }
        }

        val observeUpdateInsert = Observer<Boolean?> { isSuccess ->
            isSuccess?.let {
                if (it) {
                    dialog.dismiss()
                    dialogBinding.messageError.visibility = View.GONE
                } else {
                    dialogBinding.messageError.visibility = View.VISIBLE
                }
                categoryViewModel.resetUpdateResult()
            }
        }
        categoryViewModel.updateResult.observe(viewLifecycleOwner, observeUpdateInsert)

        dialog.setOnDismissListener {
            categoryViewModel.updateResult.removeObserver(observeUpdateInsert)
        }
    }

    private fun showConfirmDeleteCategoryDialog(category: Category) {
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

    companion object {
        fun newInstance() = CategoriesFragment()
    }
}