package com.example.smartexpense.ui.add_edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartexpense.data.local.entity.Transaction
import com.example.smartexpense.data.local.entity.TransactionType
import com.example.smartexpense.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class AddEditViewModel @Inject constructor(
    private val repository: TransactionRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val transactionId: Long? = savedStateHandle.get<Long>("transactionId")?.takeIf { it != -1L }

    private val _uiState = MutableStateFlow(AddEditUiState())
    val uiState: StateFlow<AddEditUiState> = _uiState

    init {
        if (transactionId != null) {
            loadTransaction(transactionId)
        }
    }

    private fun loadTransaction(id: Long) {

        viewModelScope.launch {
            repository.getAllTransactions().collect { list ->
                val transaction = list.find { it.id == id }
                transaction?.let {
                    _uiState.value = AddEditUiState(
                        title = it.title,
                        amount = it.amount.toString(),
                        category = it.category,
                        type = it.type,
                        date = it.date,
                        note = it.note,
                        isEditing = true
                    )
                }
            }
        }
    }

    fun onEvent(event: AddEditEvent) {
        when (event) {
            is AddEditEvent.TitleChanged -> _uiState.value = _uiState.value.copy(title = event.title)
            is AddEditEvent.AmountChanged -> _uiState.value = _uiState.value.copy(amount = event.amount)
            is AddEditEvent.CategoryChanged -> _uiState.value = _uiState.value.copy(category = event.category)
            is AddEditEvent.TypeChanged -> _uiState.value = _uiState.value.copy(type = event.type)
            is AddEditEvent.NoteChanged -> _uiState.value = _uiState.value.copy(note = event.note)
            is AddEditEvent.Save -> saveTransaction()
            is AddEditEvent.Delete -> deleteTransaction()
        }
    }

    private fun saveTransaction() {
        val state = _uiState.value
        if (state.title.isBlank() || state.amount.isBlank()) return

        val amount = state.amount.toDoubleOrNull() ?: 0.0
        val transaction = Transaction(
            id = transactionId ?: 0,
            title = state.title,
            amount = amount,
            category = state.category,
            date = state.date,
            paymentMethod = "Manual",
            note = state.note,
            type = state.type
        )

        viewModelScope.launch {
            if (state.isEditing) {
                repository.updateTransaction(transaction)
            } else {
                repository.insertTransaction(transaction)
            }

        }
    }

    private fun deleteTransaction() {
        transactionId?.let {
            viewModelScope.launch {

            }
        }
    }
}

data class AddEditUiState(
    val title: String = "",
    val amount: String = "",
    val category: String = "Food",
    val type: TransactionType = TransactionType.EXPENSE,
    val date: Date = Date(),
    val note: String = "",
    val isEditing: Boolean = false
)

sealed class AddEditEvent {
    data class TitleChanged(val title: String) : AddEditEvent()
    data class AmountChanged(val amount: String) : AddEditEvent()
    data class CategoryChanged(val category: String) : AddEditEvent()
    data class TypeChanged(val type: TransactionType) : AddEditEvent()
    data class NoteChanged(val note: String) : AddEditEvent()
    object Save : AddEditEvent()
    object Delete : AddEditEvent()
}
