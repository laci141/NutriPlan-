package com.nutriplan.app.presentation.shopping

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.PlaylistAddCheck
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import android.content.Intent
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nutriplan.app.R
import com.nutriplan.app.domain.model.IngredientCategory
import com.nutriplan.app.domain.model.ShoppingItem
import com.nutriplan.app.presentation.components.EmptyState
import com.nutriplan.app.presentation.util.ShoppingShare
import com.nutriplan.app.presentation.util.displayName
import com.nutriplan.app.presentation.util.formatQuantity
import com.nutriplan.app.presentation.util.label

/**
 * Bevásárlólista képernyő – generálás, megvásárlás jelölése, törlés.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingScreen(viewModel: ShoppingViewModel = hiltViewModel()) {
    val items by viewModel.items.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val generatedMessage = stringResource(R.string.shopping_generated)
    val context = LocalContext.current
    val shareTitle = stringResource(R.string.share_list)

    // A generálás eredményének megjelenítése snackbarban
    LaunchedEffect(Unit) {
        viewModel.generatedCount.collect { count ->
            snackbarHostState.showSnackbar(String.format(generatedMessage, count))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.shopping_title)) },
                actions = {
                    IconButton(
                        onClick = {
                            if (items.isNotEmpty()) {
                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.shopping_title))
                                    putExtra(Intent.EXTRA_TEXT, ShoppingShare.buildText(context, items))
                                }
                                context.startActivity(Intent.createChooser(sendIntent, shareTitle))
                            }
                        }
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = shareTitle)
                    }
                    IconButton(onClick = viewModel::clearPurchased) {
                        Icon(Icons.Filled.PlaylistAddCheck, contentDescription = stringResource(R.string.clear_purchased))
                    }
                    IconButton(onClick = viewModel::clearAll) {
                        Icon(Icons.Filled.DeleteSweep, contentDescription = stringResource(R.string.clear_all))
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = viewModel::generate,
                icon = { Icon(Icons.Filled.Refresh, contentDescription = null) },
                text = { Text(stringResource(R.string.generate_list)) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (items.isEmpty()) {
            EmptyState(
                message = stringResource(R.string.empty_shopping),
                modifier = Modifier.padding(padding)
            )
        } else {
            // Kategóriák szerint csoportosítva, rögzített sorrendben
            val grouped = items.groupBy { it.category }
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 96.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IngredientCategory.entries.forEach { category ->
                    val categoryItems = grouped[category].orEmpty()
                    if (categoryItems.isNotEmpty()) {
                        item(key = "header_${category.key}") {
                            Text(
                                text = category.label(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                            )
                        }
                        items(categoryItems, key = { it.id }) { shoppingItem ->
                            ShoppingRow(
                                item = shoppingItem,
                                onToggle = { viewModel.togglePurchased(shoppingItem) },
                                onDelete = { viewModel.deleteItem(shoppingItem.id) },
                                // Finom animáció a tételek megjelenésekor/átrendeződésekor
                                modifier = Modifier.animateItem()
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Egy bevásárlólista sor megvásárolt jelölővel és törlés gombbal. */
@Composable
private fun ShoppingRow(
    item: ShoppingItem,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = item.purchased, onCheckedChange = { onToggle() })
        Text(
            text = "${item.displayName()} – ${formatQuantity(item.quantity)} ${item.unit.label()}",
            style = MaterialTheme.typography.bodyLarge,
            textDecoration = if (item.purchased) TextDecoration.LineThrough else TextDecoration.None,
            color = if (item.purchased) MaterialTheme.colorScheme.onSurfaceVariant
            else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onDelete) {
            Icon(
                Icons.Filled.Delete,
                contentDescription = stringResource(R.string.delete),
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}
