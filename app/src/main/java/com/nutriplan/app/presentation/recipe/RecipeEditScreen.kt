package com.nutriplan.app.presentation.recipe

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.nutriplan.app.presentation.scanner.BarcodeScannerOverlay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.nutriplan.app.R
import com.nutriplan.app.domain.model.IngredientCategory
import com.nutriplan.app.domain.model.MealType
import com.nutriplan.app.domain.model.MeasurementUnit
import com.nutriplan.app.presentation.components.LabeledDropdown
import com.nutriplan.app.presentation.util.label
import com.nutriplan.app.util.ImageStorage
import java.io.File

/**
 * Recept szerkesztő/létrehozó képernyő.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeEditScreen(
    onDone: () -> Unit,
    viewModel: RecipeEditViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showScanner by remember { mutableStateOf(false) }

    // Kamera-engedély kérése a vonalkód-olvasóhoz
    val scannerPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) showScanner = true }

    fun openScanner() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            showScanner = true
        } else {
            scannerPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Sikeres mentés után visszalépünk
    LaunchedEffect(state.saved) {
        if (state.saved) onDone()
    }

    // A beolvasás eredményének megjelenítése snackbarként
    val successMsg = stringResource(R.string.scan_success)
    val notFoundMsg = stringResource(R.string.scan_not_found)
    val errorMsg = stringResource(R.string.scan_error)
    LaunchedEffect(state.scanFeedback) {
        val message = when (state.scanFeedback) {
            ScanFeedback.SUCCESS -> successMsg
            ScanFeedback.NOT_FOUND -> notFoundMsg
            ScanFeedback.ERROR -> errorMsg
            null -> null
        }
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.consumeScanFeedback()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            if (state.isEditing) R.string.edit_recipe else R.string.new_recipe
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    TextButton(onClick = viewModel::save) {
                        Text(stringResource(R.string.save))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::onNameChange,
                label = { Text(stringResource(R.string.recipe_name)) },
                isError = state.nameError,
                supportingText = if (state.nameError) {
                    { Text(stringResource(R.string.field_required)) }
                } else null,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Recept-fotó: kamera vagy galéria
            RecipePhotoSection(
                imagePath = state.imagePath,
                onImageSelected = viewModel::onImageSelected,
                onImageRemoved = viewModel::onImageRemoved
            )

            LabeledDropdown(
                label = stringResource(R.string.meal_type),
                selected = state.mealType,
                options = MealType.entries,
                optionLabel = { it.label() },
                onSelected = viewModel::onMealTypeChange
            )

            // Vonalkód beolvasása: kitölti a nevet és a 100 g-os tápértéket
            OutlinedButton(
                onClick = { openScanner() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.QrCodeScanner, contentDescription = null)
                Text("  ${stringResource(R.string.scan_barcode)}")
            }

            // Tápérték mezők két sorban
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = state.calories,
                    onValueChange = viewModel::onCaloriesChange,
                    label = { Text(stringResource(R.string.calories)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = state.protein,
                    onValueChange = viewModel::onProteinChange,
                    label = { Text(stringResource(R.string.protein)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = state.carbs,
                    onValueChange = viewModel::onCarbsChange,
                    label = { Text(stringResource(R.string.carbs)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = state.fat,
                    onValueChange = viewModel::onFatChange,
                    label = { Text(stringResource(R.string.fat)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Text(
                text = stringResource(R.string.ingredients),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 8.dp)
            )

            state.ingredients.forEachIndexed { index, ingredient ->
                IngredientEditor(
                    form = ingredient,
                    onNameChange = { viewModel.onIngredientNameChange(index, it) },
                    onQuantityChange = {
                        viewModel.updateIngredient(index, ingredient.copy(quantity = it.replace(',', '.').filter { c -> c.isDigit() || c == '.' }))
                    },
                    onUnitChange = { viewModel.updateIngredient(index, ingredient.copy(unit = it)) },
                    onCategoryChange = { viewModel.updateIngredient(index, ingredient.copy(category = it)) },
                    onRemove = { viewModel.removeIngredient(index) }
                )
            }

            OutlinedButton(
                onClick = viewModel::addIngredient,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Text(
                    text = "  ${stringResource(R.string.add_ingredient)}",
                )
            }

            // Elkészítési útmutató (többsoros)
            Text(
                text = stringResource(R.string.instructions),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 8.dp)
            )
            OutlinedTextField(
                value = state.instructions,
                onValueChange = viewModel::onInstructionsChange,
                label = { Text(stringResource(R.string.instructions_hint)) },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

        // Termék-lekérdezés töltésjelzője
        if (state.isLookingUp) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        // Teljes képernyős vonalkód-olvasó
        if (showScanner) {
            BarcodeScannerOverlay(
                onBarcode = { value ->
                    showScanner = false
                    viewModel.onBarcodeScanned(value)
                },
                onClose = { showScanner = false }
            )
        }
    }
}

/** Recept-fotó kiválasztása kamerával vagy galériából, előnézettel. */
@Composable
private fun RecipePhotoSection(
    imagePath: String?,
    onImageSelected: (String?) -> Unit,
    onImageRemoved: () -> Unit
) {
    val context = LocalContext.current
    // A kamera által írandó fájl (a sikeres visszatérésig megőrizzük)
    var pendingCameraFile by remember { mutableStateOf<File?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) onImageSelected(pendingCameraFile?.absolutePath) else pendingCameraFile = null
    }

    fun launchCamera() {
        val file = ImageStorage.newImageFile(context)
        pendingCameraFile = file
        cameraLauncher.launch(ImageStorage.uriFor(context, file))
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) launchCamera() }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) onImageSelected(ImageStorage.importFrom(context, uri))
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (imagePath != null) {
            Box(modifier = Modifier.fillMaxWidth()) {
                AsyncImage(
                    model = File(imagePath),
                    contentDescription = stringResource(R.string.recipe_image),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
                IconButton(
                    onClick = onImageRemoved,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = stringResource(R.string.remove_image),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_GRANTED
                    ) {
                        launchCamera()
                    } else {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.PhotoCamera, contentDescription = null)
                Text("  ${stringResource(R.string.take_photo)}")
            }
            OutlinedButton(
                onClick = {
                    galleryLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.PhotoLibrary, contentDescription = null)
                Text("  ${stringResource(R.string.choose_gallery)}")
            }
        }
    }
}

/** Egyetlen hozzávaló sor szerkesztője. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IngredientEditor(
    form: IngredientForm,
    onNameChange: (String) -> Unit,
    onQuantityChange: (String) -> Unit,
    onUnitChange: (MeasurementUnit) -> Unit,
    onCategoryChange: (IngredientCategory) -> Unit,
    onRemove: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = form.name,
                    onValueChange = onNameChange,
                    label = { Text(stringResource(R.string.ingredient_name)) },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onRemove) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = stringResource(R.string.remove),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = form.quantity,
                    onValueChange = onQuantityChange,
                    label = { Text(stringResource(R.string.quantity)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                LabeledDropdown(
                    label = stringResource(R.string.unit),
                    selected = form.unit,
                    options = MeasurementUnit.entries,
                    optionLabel = { it.label() },
                    onSelected = onUnitChange,
                    modifier = Modifier.weight(1f)
                )
            }
            LabeledDropdown(
                label = stringResource(R.string.category),
                selected = form.category,
                options = IngredientCategory.entries,
                optionLabel = { it.label() },
                onSelected = onCategoryChange
            )
        }
    }
}
