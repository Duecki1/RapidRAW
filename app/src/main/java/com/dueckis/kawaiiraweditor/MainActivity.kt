package com.dueckis.kawaiiraweditor

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalConfiguration
import com.dueckis.kawaiiraweditor.ui.theme.KawaiiRawEditorTheme
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.Executors
import kotlin.math.roundToInt
import kotlin.ranges.ClosedFloatingPointRange

private data class GalleryItem(
    val projectId: String,
    val fileName: String,
    val thumbnail: Bitmap? = null
)

private enum class Screen {
    Gallery,
    Editor
}

private enum class AdjustmentField {
    Brightness,
    Contrast,
    Highlights,
    Shadows,
    Whites,
    Blacks,
    Saturation,
    Temperature,
    Tint,
    Vibrance,
    Clarity,
    Dehaze,
    Structure,
    Centre,
    Sharpness,
    LumaNoiseReduction,
    ColorNoiseReduction,
    ChromaticAberrationRedCyan,
    ChromaticAberrationBlueYellow,
    ToneMapper
}

private data class AdjustmentState(
    val brightness: Float = 0f,
    val contrast: Float = 0f,
    val highlights: Float = 0f,
    val shadows: Float = 0f,
    val whites: Float = 0f,
    val blacks: Float = 0f,
    val saturation: Float = 0f,
    val temperature: Float = 0f,
    val tint: Float = 0f,
    val vibrance: Float = 0f,
    val clarity: Float = 0f,
    val dehaze: Float = 0f,
    val structure: Float = 0f,
    val centre: Float = 0f,
    val sharpness: Float = 0f,
    val lumaNoiseReduction: Float = 0f,
    val colorNoiseReduction: Float = 0f,
    val chromaticAberrationRedCyan: Float = 0f,
    val chromaticAberrationBlueYellow: Float = 0f,
    val toneMapper: String = "basic"
) {
    fun valueFor(field: AdjustmentField): Float {
        return when (field) {
            AdjustmentField.Brightness -> brightness
            AdjustmentField.Contrast -> contrast
            AdjustmentField.Highlights -> highlights
            AdjustmentField.Shadows -> shadows
            AdjustmentField.Whites -> whites
            AdjustmentField.Blacks -> blacks
            AdjustmentField.Saturation -> saturation
            AdjustmentField.Temperature -> temperature
            AdjustmentField.Tint -> tint
            AdjustmentField.Vibrance -> vibrance
            AdjustmentField.Clarity -> clarity
            AdjustmentField.Dehaze -> dehaze
            AdjustmentField.Structure -> structure
            AdjustmentField.Centre -> centre
            AdjustmentField.Sharpness -> sharpness
            AdjustmentField.LumaNoiseReduction -> lumaNoiseReduction
            AdjustmentField.ColorNoiseReduction -> colorNoiseReduction
            AdjustmentField.ChromaticAberrationRedCyan -> chromaticAberrationRedCyan
            AdjustmentField.ChromaticAberrationBlueYellow -> chromaticAberrationBlueYellow
            AdjustmentField.ToneMapper -> 0f // ToneMapper is a string, not a float
        }
    }

    fun withValue(field: AdjustmentField, value: Float): AdjustmentState {
        return when (field) {
            AdjustmentField.Brightness -> copy(brightness = value)
            AdjustmentField.Contrast -> copy(contrast = value)
            AdjustmentField.Highlights -> copy(highlights = value)
            AdjustmentField.Shadows -> copy(shadows = value)
            AdjustmentField.Whites -> copy(whites = value)
            AdjustmentField.Blacks -> copy(blacks = value)
            AdjustmentField.Saturation -> copy(saturation = value)
            AdjustmentField.Temperature -> copy(temperature = value)
            AdjustmentField.Tint -> copy(tint = value)
            AdjustmentField.Vibrance -> copy(vibrance = value)
            AdjustmentField.Clarity -> copy(clarity = value)
            AdjustmentField.Dehaze -> copy(dehaze = value)
            AdjustmentField.Structure -> copy(structure = value)
            AdjustmentField.Centre -> copy(centre = value)
            AdjustmentField.Sharpness -> copy(sharpness = value)
            AdjustmentField.LumaNoiseReduction -> copy(lumaNoiseReduction = value)
            AdjustmentField.ColorNoiseReduction -> copy(colorNoiseReduction = value)
            AdjustmentField.ChromaticAberrationRedCyan -> copy(chromaticAberrationRedCyan = value)
            AdjustmentField.ChromaticAberrationBlueYellow -> copy(chromaticAberrationBlueYellow = value)
            AdjustmentField.ToneMapper -> this
        }
    }
    
    fun withToneMapper(mapper: String): AdjustmentState {
        return copy(toneMapper = mapper)
    }

    fun toJson(): String {
        val payload = JSONObject().apply {
            put("brightness", brightness)
            put("contrast", contrast)
            put("highlights", highlights)
            put("shadows", shadows)
            put("whites", whites)
            put("blacks", blacks)
            put("saturation", saturation)
            put("temperature", temperature)
            put("tint", tint)
            put("vibrance", vibrance)
            put("clarity", clarity)
            put("dehaze", dehaze)
            put("structure", structure)
            put("centre", centre)
            put("sharpness", sharpness)
            put("lumaNoiseReduction", lumaNoiseReduction)
            put("colorNoiseReduction", colorNoiseReduction)
            put("chromaticAberrationRedCyan", chromaticAberrationRedCyan)
            put("chromaticAberrationBlueYellow", chromaticAberrationBlueYellow)
            put("toneMapper", toneMapper)
            put("masks", JSONArray())
        }
        return payload.toString()
    }
}

private data class AdjustmentControl(
    val field: AdjustmentField,
    val label: String,
    val range: ClosedFloatingPointRange<Float>,
    val step: Float,
    val formatter: (Float) -> String = { value -> String.format(Locale.US, "%.0f", value) }
)

private data class RenderRequest(
    val version: Long,
    val adjustments: AdjustmentState
)

private val basicSection = listOf(
    AdjustmentControl(
        field = AdjustmentField.Brightness,
        label = "Brightness",
        range = -5f..5f,
        step = 0.01f,
        formatter = { value -> String.format(Locale.US, "%.2f", value) }
    ),
    AdjustmentControl(field = AdjustmentField.Contrast, label = "Contrast", range = -100f..100f, step = 1f),
    AdjustmentControl(field = AdjustmentField.Highlights, label = "Highlights", range = -100f..100f, step = 1f),
    AdjustmentControl(field = AdjustmentField.Shadows, label = "Shadows", range = -100f..100f, step = 1f),
    AdjustmentControl(field = AdjustmentField.Whites, label = "Whites", range = -100f..100f, step = 1f),
    AdjustmentControl(field = AdjustmentField.Blacks, label = "Blacks", range = -100f..100f, step = 1f)
)

private val colorSection = listOf(
    AdjustmentControl(field = AdjustmentField.Saturation, label = "Saturation", range = -100f..100f, step = 1f),
    AdjustmentControl(field = AdjustmentField.Temperature, label = "Temperature", range = -100f..100f, step = 1f),
    AdjustmentControl(field = AdjustmentField.Tint, label = "Tint", range = -100f..100f, step = 1f),
    AdjustmentControl(field = AdjustmentField.Vibrance, label = "Vibrance", range = -100f..100f, step = 1f)
)

private val detailsSection = listOf(
    AdjustmentControl(field = AdjustmentField.Clarity, label = "Clarity", range = -100f..100f, step = 1f),
    AdjustmentControl(field = AdjustmentField.Dehaze, label = "Dehaze", range = -100f..100f, step = 1f),
    AdjustmentControl(field = AdjustmentField.Structure, label = "Structure", range = -100f..100f, step = 1f),
    AdjustmentControl(field = AdjustmentField.Centre, label = "Centré", range = -100f..100f, step = 1f),
    AdjustmentControl(field = AdjustmentField.Sharpness, label = "Sharpness", range = -100f..100f, step = 1f),
    AdjustmentControl(field = AdjustmentField.LumaNoiseReduction, label = "Luminance NR", range = 0f..100f, step = 1f),
    AdjustmentControl(field = AdjustmentField.ColorNoiseReduction, label = "Color NR", range = 0f..100f, step = 1f),
    AdjustmentControl(field = AdjustmentField.ChromaticAberrationRedCyan, label = "CA Red/Cyan", range = -100f..100f, step = 1f),
    AdjustmentControl(field = AdjustmentField.ChromaticAberrationBlueYellow, label = "CA Blue/Yellow", range = -100f..100f, step = 1f)
)

private val adjustmentSections = listOf(
    "Basic" to basicSection,
    "Color" to colorSection
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KawaiiRawEditorTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    RapidRawEditorScreen()
                }
            }
        }
    }
}

@Composable
private fun RapidRawEditorScreen() {
    val context = LocalContext.current
    val storage = remember { ProjectStorage(context) }
    
    var currentScreen by remember { mutableStateOf(Screen.Gallery) }
    var galleryItems by remember { mutableStateOf<List<GalleryItem>>(emptyList()) }
    var selectedItem by remember { mutableStateOf<GalleryItem?>(null) }
    var refreshTrigger by remember { mutableIntStateOf(0) }

    // Load existing projects on startup and when returning from editor
    LaunchedEffect(refreshTrigger) {
        val projects = storage.getAllProjects()
        galleryItems = withContext(Dispatchers.IO) {
            projects.map { metadata ->
                val thumbnailBytes = storage.loadThumbnail(metadata.id)
                val thumbnail = thumbnailBytes?.let { bytes ->
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                }
                GalleryItem(
                    projectId = metadata.id,
                    fileName = metadata.fileName,
                    thumbnail = thumbnail
                )
            }
        }
    }

    when (currentScreen) {
        Screen.Gallery -> GalleryScreen(
            items = galleryItems,
            onItemClick = { item ->
                selectedItem = item
                currentScreen = Screen.Editor
            },
            onAddClick = { newItem ->
                galleryItems = galleryItems + newItem
            }
        )
        Screen.Editor -> EditorScreen(
            galleryItem = selectedItem,
            onBackClick = {
                currentScreen = Screen.Gallery
                refreshTrigger++
            }
        )
    }
}

@Composable
private fun GalleryScreen(
    items: List<GalleryItem>,
    onItemClick: (GalleryItem) -> Unit,
    onAddClick: (GalleryItem) -> Unit
) {
    val context = LocalContext.current
    val storage = remember { ProjectStorage(context) }
    val coroutineScope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    
    // Calculate columns based on screen width (each item ~150-200dp)
    val columns = when {
        screenWidthDp >= 900 -> 5  // Large tablets
        screenWidthDp >= 600 -> 4  // Small tablets
        screenWidthDp >= 400 -> 3  // Large phones
        else -> 2  // Small phones
    }
    
    val mimeTypes = arrayOf("image/x-sony-arw", "image/*")
    val pickRaw = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        coroutineScope.launch {
            val bytes = withContext(Dispatchers.IO) {
                context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            }
            if (bytes != null) {
                val name = displayNameForUri(context, uri)
                // Import the file into app storage
                val projectId = withContext(Dispatchers.IO) {
                    storage.importRawFile(name, bytes)
                }
                val item = GalleryItem(
                    projectId = projectId,
                    fileName = name,
                    thumbnail = null
                )
                onAddClick(item)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.systemBars)) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            Text(
                text = "Gallery",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
                color = MaterialTheme.colorScheme.onSurface
            )

            if (items.isEmpty()) {
                // Empty state
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "No RAW files yet",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tap the + button to add RAW files",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                // Grid of items
                LazyVerticalGrid(
                    columns = GridCells.Fixed(columns),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items.size) { index ->
                        GalleryItemCard(
                            item = items[index],
                            onClick = { onItemClick(items[index]) }
                        )
                    }
                }
            }
        }

        // FAB
        FloatingActionButton(
            onClick = { pickRaw.launch(mimeTypes) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add RAW file")
        }
    }
}

@Composable
private fun GalleryItemCard(
    item: GalleryItem,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp,
            pressedElevation = 3.dp
        )
    ) {
        Column {
            // Thumbnail
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                contentAlignment = Alignment.Center
            ) {
                if (item.thumbnail != null) {
                    Image(
                        bitmap = item.thumbnail.asImageBitmap(),
                        contentDescription = item.fileName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = "RAW",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Text(
                text = item.fileName,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(12.dp),
                maxLines = 2,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun EditorScreen(
    galleryItem: GalleryItem?,
    onBackClick: () -> Unit
) {
    if (galleryItem == null) {
        onBackClick()
        return
    }

    val context = LocalContext.current
    val storage = remember { ProjectStorage(context) }
    val scrollState = rememberScrollState()
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    var rawBytes by remember { mutableStateOf<ByteArray?>(null) }
    var adjustments by remember { mutableStateOf(AdjustmentState()) }
    var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var isExporting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    val renderVersion = remember { AtomicLong(0L) }
    val lastPreviewVersion = remember { AtomicLong(0L) }
    val renderRequests = remember { Channel<RenderRequest>(capacity = Channel.CONFLATED) }
    val renderDispatcher = remember { Executors.newSingleThreadExecutor().asCoroutineDispatcher() }
    DisposableEffect(renderDispatcher) {
        onDispose { renderDispatcher.close() }
    }

    // Load RAW file and adjustments from storage
    LaunchedEffect(galleryItem.projectId) {
        rawBytes = withContext(Dispatchers.IO) {
            storage.loadRawBytes(galleryItem.projectId)
        }
        val savedAdjustmentsJson = withContext(Dispatchers.IO) {
            storage.loadAdjustments(galleryItem.projectId)
        }
        // Parse saved adjustments if they exist
        if (savedAdjustmentsJson != "{}") {
            try {
                val json = org.json.JSONObject(savedAdjustmentsJson)
                adjustments = AdjustmentState(
                    brightness = json.optDouble("brightness", 0.0).toFloat(),
                    contrast = json.optDouble("contrast", 0.0).toFloat(),
                    highlights = json.optDouble("highlights", 0.0).toFloat(),
                    shadows = json.optDouble("shadows", 0.0).toFloat(),
                    whites = json.optDouble("whites", 0.0).toFloat(),
                    blacks = json.optDouble("blacks", 0.0).toFloat(),
                    saturation = json.optDouble("saturation", 0.0).toFloat(),
                    temperature = json.optDouble("temperature", 0.0).toFloat(),
                    tint = json.optDouble("tint", 0.0).toFloat(),
                    vibrance = json.optDouble("vibrance", 0.0).toFloat(),
                    clarity = json.optDouble("clarity", 0.0).toFloat(),
                    dehaze = json.optDouble("dehaze", 0.0).toFloat(),
                    structure = json.optDouble("structure", 0.0).toFloat(),
                    centre = json.optDouble("centre", 0.0).toFloat(),
                    sharpness = json.optDouble("sharpness", 0.0).toFloat(),
                    lumaNoiseReduction = json.optDouble("lumaNoiseReduction", 0.0).toFloat(),
                    colorNoiseReduction = json.optDouble("colorNoiseReduction", 0.0).toFloat(),
                    chromaticAberrationRedCyan = json.optDouble("chromaticAberrationRedCyan", 0.0).toFloat(),
                    chromaticAberrationBlueYellow = json.optDouble("chromaticAberrationBlueYellow", 0.0).toFloat(),
                    toneMapper = json.optString("toneMapper", "basic")
                )
            } catch (e: Exception) {
                // Keep default adjustments if parsing fails
            }
        }
    }

    LaunchedEffect(adjustments) {
        val version = renderVersion.incrementAndGet()
        renderRequests.trySend(RenderRequest(version = version, adjustments = adjustments))

        // Debounce persisting adjustments (I/O) for slider drags.
        delay(350)
        val json = withContext(Dispatchers.Default) { adjustments.toJson() }
        withContext(Dispatchers.IO) {
            storage.saveAdjustments(galleryItem.projectId, json)
        }
    }

    LaunchedEffect(rawBytes) {
        val raw = rawBytes ?: return@LaunchedEffect

        // Ensure we always start with a render request for the current state.
        renderRequests.trySend(
            RenderRequest(
                version = renderVersion.incrementAndGet(),
                adjustments = adjustments
            )
        )

        var currentRequest = renderRequests.receive()
        while (true) {
            while (true) {
                val next = renderRequests.tryReceive().getOrNull() ?: break
                currentRequest = next
            }

            val requestVersion = currentRequest.version
            val requestJson = withContext(renderDispatcher) { currentRequest.adjustments.toJson() }

            // Stage 1: super-low quality (fast feedback while dragging).
            val superLowBitmap = withContext(renderDispatcher) {
                val bytes = runCatching { LibRawDecoder.lowlowdecode(raw, requestJson) }.getOrNull()
                bytes?.decodeToBitmap()
            }
            if (superLowBitmap != null && requestVersion > lastPreviewVersion.get()) {
                previewBitmap = superLowBitmap
                lastPreviewVersion.set(requestVersion)
            }

            // If the user is still moving the slider, keep updating the super-low preview only.
            val maybeUpdatedAfterSuperLow = withTimeoutOrNull(60) { renderRequests.receive() }
            if (maybeUpdatedAfterSuperLow != null) {
                currentRequest = maybeUpdatedAfterSuperLow
                continue
            }

            // Stage 2: low quality (still fast, but clearer).
            val lowBitmap = withContext(renderDispatcher) {
                val bytes = runCatching { LibRawDecoder.lowdecode(raw, requestJson) }.getOrNull()
                bytes?.decodeToBitmap()
            }
            if (lowBitmap != null && requestVersion > lastPreviewVersion.get()) {
                previewBitmap = lowBitmap
                lastPreviewVersion.set(requestVersion)
            }

            // Debounce before running the expensive preview render.
            val maybeUpdatedAfterLow = withTimeoutOrNull(180) { renderRequests.receive() }
            if (maybeUpdatedAfterLow != null) {
                currentRequest = maybeUpdatedAfterLow
                continue
            }

            isLoading = true
            val fullBitmap = withContext(renderDispatcher) {
                val bytes = runCatching { LibRawDecoder.decode(raw, requestJson) }.getOrNull()
                bytes?.decodeToBitmap()
            }
            isLoading = false

            if (requestVersion == renderVersion.get()) {
                errorMessage = if (fullBitmap == null) "Failed to render preview." else null
                if (fullBitmap != null) {
                    previewBitmap = fullBitmap
                    lastPreviewVersion.set(requestVersion)
                }

                // Generate and save thumbnail only for the latest completed render.
                fullBitmap?.let { bmp ->
                    withContext(Dispatchers.IO) {
                        val maxSize = 512
                        val scale = minOf(maxSize.toFloat() / bmp.width, maxSize.toFloat() / bmp.height)
                        val scaledWidth = (bmp.width * scale).toInt()
                        val scaledHeight = (bmp.height * scale).toInt()
                        val thumbnail = Bitmap.createScaledBitmap(bmp, scaledWidth, scaledHeight, true)

                        val outputStream = java.io.ByteArrayOutputStream()
                        thumbnail.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
                        val thumbnailBytes = outputStream.toByteArray()

                        storage.saveThumbnail(galleryItem.projectId, thumbnailBytes)

                        if (thumbnail != bmp) {
                            thumbnail.recycle()
                        }
                    }
                }
            }

            currentRequest = renderRequests.receive()
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (isTablet) {
                // Tablet layout: Full screen content with top bars overlaid
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                        // Left column: Preview (3/4 width)
                        Box(
                            modifier = Modifier
                                .weight(3f)
                                .fillMaxHeight()
                        ) {
                            ImagePreview(bitmap = previewBitmap, isLoading = isLoading)
                        }

                        // Right column: Controls (with top padding for status bar + app bar)
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(MaterialTheme.colorScheme.surface)
                        ) {
                            Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
                            Spacer(modifier = Modifier.height(56.dp)) // Top app bar height
                            
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(scrollState)
                                    .padding(horizontal = 16.dp, vertical = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                ToneMapperSection(
                                toneMapper = adjustments.toneMapper,
                                onToneMapperChange = { mapper ->
                                    adjustments = adjustments.withToneMapper(mapper)
                                }
                            )

                            adjustmentSections.forEach { (sectionTitle, controls) ->
                                Text(
                                    text = sectionTitle,
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(top = 8.dp),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    controls.forEach { control ->
                                        val currentValue = adjustments.valueFor(control.field)
                                        AdjustmentSlider(
                                            label = control.label,
                                            value = currentValue,
                                            range = control.range,
                                            step = control.step,
                                            formatter = control.formatter,
                                            onValueChange = { snapped ->
                                                adjustments = adjustments.withValue(control.field, snapped)
                                            }
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Export button at bottom
                            ExportButton(
                                rawBytes = rawBytes,
                                adjustments = adjustments,
                                isExporting = isExporting,
                                nativeDispatcher = renderDispatcher,
                                context = context,
                                onExportStart = { isExporting = true },
                                onExportComplete = { success, message ->
                                    isExporting = false
                                    if (success) {
                                        statusMessage = message
                                        errorMessage = null
                                    } else {
                                        errorMessage = message
                                        statusMessage = null
                                    }
                                }
                            )
                            
                            errorMessage?.let {
                                Text(
                                    text = it,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }

                            statusMessage?.let {
                                Text(
                                    text = it,
                                    color = Color(0xFF1B5E20),
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                            
                                Spacer(modifier = Modifier.height(20.dp))
                            }
                        }
                    }
                
                // Overlay status bar and top app bar on top
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Status bar background
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .windowInsetsTopHeight(WindowInsets.statusBars)
                            .background(MaterialTheme.colorScheme.surface)
                    )
                    
                    // Top app bar
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 3.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = onBackClick) {
                                Icon(
                                    Icons.Default.ArrowBack,
                                    contentDescription = "Back to gallery",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = galleryItem.fileName,
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            } else {
                // Phone layout: fixed preview on top, fixed-height scrollable controls at bottom (like Lightroom)
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Status bar spacer
                    Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
                    
                    // Top app bar
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 3.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = onBackClick) {
                                Icon(
                                    Icons.Default.ArrowBack,
                                    contentDescription = "Back to gallery",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = galleryItem.fileName,
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    
                    // Fixed preview - fills remaining space
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        ImagePreview(bitmap = previewBitmap, isLoading = isLoading)
                    }

                    // Fixed-height scrollable controls panel at bottom
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 3.dp,
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(scrollState)
                                .padding(horizontal = 16.dp, vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Export button at top of controls
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ExportButton(
                                    rawBytes = rawBytes,
                                    adjustments = adjustments,
                                    isExporting = isExporting,
                                    nativeDispatcher = renderDispatcher,
                                    context = context,
                                    onExportStart = { isExporting = true },
                                    onExportComplete = { success, message ->
                                        isExporting = false
                                        if (success) {
                                            statusMessage = message
                                            errorMessage = null
                                        } else {
                                            errorMessage = message
                                            statusMessage = null
                                        }
                                    }
                                )
                            }
                            
                            errorMessage?.let {
                                Text(
                                    text = it,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            statusMessage?.let {
                                Text(
                                    text = it,
                                    color = Color(0xFF1B5E20),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            // Tone Mapper Section
                            ToneMapperSection(
                                toneMapper = adjustments.toneMapper,
                                onToneMapperChange = { mapper ->
                                    adjustments = adjustments.withToneMapper(mapper)
                                }
                            )

                            adjustmentSections.forEach { (sectionTitle, controls) ->
                                Text(
                                    text = sectionTitle,
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(top = 8.dp),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    controls.forEach { control ->
                                        val currentValue = adjustments.valueFor(control.field)
                                        AdjustmentSlider(
                                            label = control.label,
                                            value = currentValue,
                                            range = control.range,
                                            step = control.step,
                                            formatter = control.formatter,
                                            onValueChange = { snapped ->
                                                adjustments = adjustments.withValue(control.field, snapped)
                                            }
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                        }
                    }
                }
            }
            
            // Status bar background overlay to prevent image showing through
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsTopHeight(WindowInsets.statusBars)
                    .align(Alignment.TopCenter),
                color = MaterialTheme.colorScheme.surface
            ) {}
        }
    }
}

@Composable
private fun ExportButton(
    rawBytes: ByteArray?,
    adjustments: AdjustmentState,
    isExporting: Boolean,
    nativeDispatcher: CoroutineDispatcher,
    context: Context,
    onExportStart: () -> Unit,
    onExportComplete: (Boolean, String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    
    Button(
        onClick = {
            val raw = rawBytes
            if (isExporting || raw == null) return@Button
            val currentAdjustments = adjustments
            onExportStart()
            coroutineScope.launch {
                val currentJson = withContext(Dispatchers.Default) { currentAdjustments.toJson() }
                val fullBytes = withContext(nativeDispatcher) {
                    runCatching { LibRawDecoder.decodeFullRes(raw, currentJson) }.getOrNull()
                }
                if (fullBytes == null) {
                    onExportComplete(false, "Export failed.")
                } else {
                    val savedUri = withContext(Dispatchers.IO) {
                        saveJpegToPictures(context, fullBytes)
                    }
                    if (savedUri != null) {
                        onExportComplete(true, "Saved to $savedUri")
                    } else {
                        onExportComplete(false, "Export failed: could not save JPEG.")
                    }
                }
            }
        },
        enabled = rawBytes != null && !isExporting
    ) {
        if (isExporting) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(18.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text("Export JPEG")
    }
}

@Composable
private fun ToneMapperSection(
    toneMapper: String,
    onToneMapperChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = "Tone Mapper",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp),
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
            tonalElevation = 0.dp
        ) {
            Row(
                modifier = Modifier.padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                FilledTonalButton(
                    onClick = { onToneMapperChange("basic") },
                    modifier = Modifier.weight(1f),
                    colors = androidx.compose.material3.ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (toneMapper == "basic") 
                            MaterialTheme.colorScheme.secondaryContainer 
                        else 
                            MaterialTheme.colorScheme.surfaceContainerHighest,
                        contentColor = if (toneMapper == "basic")
                            MaterialTheme.colorScheme.onSecondaryContainer
                        else
                            MaterialTheme.colorScheme.onSurface
                    ),
                    shape = MaterialTheme.shapes.large
                ) {
                    Text("Basic")
                }
                FilledTonalButton(
                    onClick = { onToneMapperChange("agx") },
                    modifier = Modifier.weight(1f),
                    colors = androidx.compose.material3.ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (toneMapper == "agx") 
                            MaterialTheme.colorScheme.secondaryContainer 
                        else 
                            MaterialTheme.colorScheme.surfaceContainerHighest,
                        contentColor = if (toneMapper == "agx")
                            MaterialTheme.colorScheme.onSecondaryContainer
                        else
                            MaterialTheme.colorScheme.onSurface
                    ),
                    shape = MaterialTheme.shapes.large
                ) {
                    Text("AgX")
                }
            }
        }
    }
}

@Composable
private fun AdjustmentSlider(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    step: Float,
    formatter: (Float) -> String,
    onValueChange: (Float) -> Unit
) {
    val colors = SliderDefaults.colors(
        activeTrackColor = MaterialTheme.colorScheme.primary,
        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
        thumbColor = MaterialTheme.colorScheme.primary
    )
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = formatter(value),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Slider(
            value = value,
            onValueChange = { newValue ->
                val snapped = snapToStep(newValue, step, range)
                onValueChange(snapped)
            },
            valueRange = range,
            colors = colors
        )
    }
}

private fun snapToStep(
    value: Float,
    step: Float,
    range: ClosedFloatingPointRange<Float>
): Float {
    val clamped = value.coerceIn(range.start, range.endInclusive)
    if (step <= 0f) return clamped
    val steps = ((clamped - range.start) / step).roundToInt()
    return (range.start + steps * step).coerceIn(range.start, range.endInclusive)
}

@Composable
private fun ImagePreview(bitmap: Bitmap?, isLoading: Boolean) {
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Processed preview",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(0.5f, 5f)
                            offsetX += pan.x
                            offsetY += pan.y
                        }
                    }
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offsetX,
                        translationY = offsetY
                    )
            )
        } else {
            Text(
                text = "No preview yet",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (isLoading) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

private fun displayNameForUri(context: Context, uri: Uri): String {
    return context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
        ?.use { cursor ->
            val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (idx >= 0 && cursor.moveToFirst()) cursor.getString(idx) else null
        } ?: uri.lastPathSegment ?: "Imported RAW"
}

private fun saveJpegToPictures(context: Context, jpegBytes: ByteArray): Uri? {
    val filename = "KawaiiRaw_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.jpg"
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/KawaiiRawEditor")
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }
    }
    val resolver = context.contentResolver
    val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
    if (uri != null) {
        try {
            resolver.openOutputStream(uri)?.use { it.write(jpegBytes) }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
            }
        } catch (e: Exception) {
            resolver.delete(uri, null, null)
            return null
        }
    }
    return uri
}

private fun ByteArray.decodeToBitmap(): Bitmap? =
    BitmapFactory.decodeByteArray(this, 0, size)
