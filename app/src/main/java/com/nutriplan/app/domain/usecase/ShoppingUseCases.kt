package com.nutriplan.app.domain.usecase

import com.nutriplan.app.domain.model.ShoppingItem
import com.nutriplan.app.domain.repository.ShoppingRepository
import com.nutriplan.app.util.Logger
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Bevásárlólista use case-ek.
 */

/** A bevásárlólista lekérése. */
class GetShoppingListUseCase @Inject constructor(
    private val repository: ShoppingRepository
) {
    operator fun invoke(): Flow<List<ShoppingItem>> {
        Logger.d(Logger.Tags.SHOPPING, "GetShoppingListUseCase meghívva")
        return repository.getShoppingList()
    }
}

/** Bevásárlólista automatikus generálása a heti tervből. */
class GenerateShoppingListUseCase @Inject constructor(
    private val repository: ShoppingRepository
) {
    suspend operator fun invoke(): Int {
        Logger.i(Logger.Tags.SHOPPING, "GenerateShoppingListUseCase meghívva")
        return repository.generateFromPlan()
    }
}

/** Egy tétel megvásárolt állapotának váltása. */
class SetItemPurchasedUseCase @Inject constructor(
    private val repository: ShoppingRepository
) {
    suspend operator fun invoke(id: Long, purchased: Boolean) {
        Logger.i(Logger.Tags.SHOPPING, "SetItemPurchasedUseCase meghívva, id=$id, purchased=$purchased")
        repository.setPurchased(id, purchased)
    }
}

/** Egy tétel törlése a bevásárlólistáról. */
class DeleteShoppingItemUseCase @Inject constructor(
    private val repository: ShoppingRepository
) {
    suspend operator fun invoke(id: Long) {
        Logger.i(Logger.Tags.SHOPPING, "DeleteShoppingItemUseCase meghívva, id=$id")
        repository.deleteItem(id)
    }
}

/** A megvásárolt tételek törlése. */
class ClearPurchasedUseCase @Inject constructor(
    private val repository: ShoppingRepository
) {
    suspend operator fun invoke() {
        Logger.i(Logger.Tags.SHOPPING, "ClearPurchasedUseCase meghívva")
        repository.clearPurchased()
    }
}

/** A teljes bevásárlólista törlése. */
class ClearShoppingListUseCase @Inject constructor(
    private val repository: ShoppingRepository
) {
    suspend operator fun invoke() {
        Logger.i(Logger.Tags.SHOPPING, "ClearShoppingListUseCase meghívva")
        repository.clearAll()
    }
}
