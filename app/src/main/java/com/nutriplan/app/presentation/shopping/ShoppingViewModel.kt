package com.nutriplan.app.presentation.shopping

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutriplan.app.domain.model.ShoppingItem
import com.nutriplan.app.domain.usecase.ClearPurchasedUseCase
import com.nutriplan.app.domain.usecase.ClearShoppingListUseCase
import com.nutriplan.app.domain.usecase.DeleteShoppingItemUseCase
import com.nutriplan.app.domain.usecase.GenerateShoppingListUseCase
import com.nutriplan.app.domain.usecase.GetShoppingListUseCase
import com.nutriplan.app.domain.usecase.SetItemPurchasedUseCase
import com.nutriplan.app.util.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Bevásárlólista ViewModel.
 */
@HiltViewModel
class ShoppingViewModel @Inject constructor(
    getShoppingListUseCase: GetShoppingListUseCase,
    private val generateShoppingListUseCase: GenerateShoppingListUseCase,
    private val setItemPurchasedUseCase: SetItemPurchasedUseCase,
    private val deleteShoppingItemUseCase: DeleteShoppingItemUseCase,
    private val clearPurchasedUseCase: ClearPurchasedUseCase,
    private val clearShoppingListUseCase: ClearShoppingListUseCase
) : ViewModel() {

    val items: StateFlow<List<ShoppingItem>> = getShoppingListUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Egyszeri esemény a generálás eredményéről (tételek száma)
    private val _generatedCount = MutableSharedFlow<Int>()
    val generatedCount: SharedFlow<Int> = _generatedCount.asSharedFlow()

    init {
        Logger.i(Logger.Tags.VIEWMODEL, "ShoppingViewModel létrehozva")
    }

    /** Bevásárlólista generálása a heti tervből. */
    fun generate() {
        viewModelScope.launch {
            Logger.i(Logger.Tags.VIEWMODEL, "ShoppingViewModel – lista generálása")
            val count = generateShoppingListUseCase()
            _generatedCount.emit(count)
        }
    }

    /** Tétel megvásárolt állapotának váltása. */
    fun togglePurchased(item: ShoppingItem) {
        viewModelScope.launch {
            setItemPurchasedUseCase(item.id, !item.purchased)
        }
    }

    /** Tétel törlése. */
    fun deleteItem(id: Long) {
        viewModelScope.launch {
            deleteShoppingItemUseCase(id)
        }
    }

    /** Megvásárolt tételek törlése. */
    fun clearPurchased() {
        viewModelScope.launch {
            clearPurchasedUseCase()
        }
    }

    /** Teljes lista törlése. */
    fun clearAll() {
        viewModelScope.launch {
            clearShoppingListUseCase()
        }
    }
}
