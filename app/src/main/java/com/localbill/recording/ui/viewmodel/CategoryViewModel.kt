package com.localbill.recording.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.localbill.recording.data.entity.CategoryEntity
import com.localbill.recording.data.repository.CategoryRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class CategoryNode(
    val mainCategory: CategoryEntity,
    val subCategories: List<CategoryEntity> = emptyList()
)

data class CategoryUiState(
    val categoryNodes: List<CategoryNode> = emptyList(),
    val isLoading: Boolean = false
)

sealed class CategoryEvent {
    data class ShowToast(val message: String) : CategoryEvent()
    object CategorySaved : CategoryEvent()
}

class CategoryViewModel(
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoryUiState())
    val uiState: StateFlow<CategoryUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<CategoryEvent>()
    val eventFlow: SharedFlow<CategoryEvent> = _eventFlow.asSharedFlow()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            combine(
                categoryRepository.mainCategoriesFlow,
                categoryRepository.allCategoriesFlow
            ) { mainList, allList ->
                mainList.map { main ->
                    CategoryNode(
                        mainCategory = main,
                        subCategories = allList.filter { it.parentId == main.id }
                    )
                }
            }.collect { nodes ->
                _uiState.value = CategoryUiState(categoryNodes = nodes, isLoading = false)
            }
        }
    }

    fun addOrUpdateCategory(
        id: Long = 0,
        name: String,
        iconName: String,
        colorHex: Long,
        parentId: Long? = null,
        isBuiltIn: Boolean = false,
        sortOrder: Int = 0
    ) {
        if (name.isBlank()) {
            viewModelScope.launch { _eventFlow.emit(CategoryEvent.ShowToast("分类名称不能为空")) }
            return
        }

        viewModelScope.launch {
            if (id == 0L) {
                categoryRepository.addCategory(
                    name = name.trim(),
                    iconName = iconName,
                    colorHex = colorHex,
                    parentId = parentId,
                    sortOrder = sortOrder
                )
            } else {
                categoryRepository.updateCategory(
                    CategoryEntity(
                        id = id,
                        name = name.trim(),
                        iconName = iconName,
                        colorHex = colorHex,
                        parentId = parentId,
                        isBuiltIn = isBuiltIn,
                        sortOrder = sortOrder
                    )
                )
            }
            _eventFlow.emit(CategoryEvent.CategorySaved)
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            val count = categoryRepository.countRecordsUsingCategory(category.id)
            if (count > 0) {
                _eventFlow.emit(CategoryEvent.ShowToast("该分类下已有 ${count} 笔支出记录，无法删除"))
                return@launch
            }

            val success = categoryRepository.deleteCategory(category)
            if (success) {
                _eventFlow.emit(CategoryEvent.ShowToast("已删除分类「${category.name}」"))
            } else {
                _eventFlow.emit(CategoryEvent.ShowToast("删除失败，该分类或其子分类下有关联账单记录"))
            }
        }
    }

    class Factory(
        private val categoryRepository: CategoryRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CategoryViewModel(categoryRepository) as T
        }
    }
}
