package lutech.intern.noteapp.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import lutech.intern.noteapp.constant.SortOption

class SharedViewModel : ViewModel() {
    private val _sortType = MutableLiveData(SortOption.EDIT_DATE_NEWEST.toString())
    val sortType: LiveData<String> get() = _sortType

    fun setSortType(type: String) {
        _sortType.value = type
    }
}