package com.dueckis.kawaiiraweditor

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material3.Checkbox
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
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
    fun toJsonObject(includeToneMapper: Boolean = true): JSONObject {
        return JSONObject().apply {
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
            if (includeToneMapper) {
                put("toneMapper", toneMapper)
            }
        }
    }

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

    fun toJson(masks: List<MaskState> = emptyList()): String {
        val payload = toJsonObject(includeToneMapper = true).apply {
            put(
                "masks",
                JSONArray().apply {
                    masks.forEach { put(it.toJsonObject()) }
                }
            )
        }
        return payload.toString()
    }
}

private enum class SubMaskMode {
    Additive,
    Subtractive,
}

private enum class EditorPanelTab {
    Adjustments,
    Masks,
}

private fun AdjustmentState.isNeutralForMask(): Boolean {
    fun nearZero(v: Float) = kotlin.math.abs(v) <= 0.000001f
    return nearZero(brightness) &&
        nearZero(contrast) &&
        nearZero(highlights) &&
        nearZero(shadows) &&
        nearZero(whites) &&
        nearZero(blacks) &&
        nearZero(saturation) &&
        nearZero(temperature) &&
        nearZero(tint) &&
        nearZero(vibrance) &&
        nearZero(clarity) &&
        nearZero(dehaze) &&
        nearZero(structure) &&
        nearZero(centre) &&
        nearZero(sharpness) &&
        nearZero(lumaNoiseReduction) &&
        nearZero(colorNoiseReduction) &&
        nearZero(chromaticAberrationRedCyan) &&
        nearZero(chromaticAberrationBlueYellow)
}

private data class MaskPoint(
    val x: Float,
    val y: Float
)

private data class BrushLineState(
    val tool: String = "brush",
    val brushSize: Float,
    val feather: Float = 0.5f,
    val order: Long = 0L,
    val points: List<MaskPoint>
)

private data class SubMaskState(
    val id: String,
    val visible: Boolean = true,
    val mode: SubMaskMode = SubMaskMode.Additive,
    val lines: List<BrushLineState> = emptyList()
)

private data class MaskState(
    val id: String,
    val name: String,
    val visible: Boolean = true,
    val invert: Boolean = false,
    val opacity: Float = 100f,
    val adjustments: AdjustmentState = AdjustmentState(),
    val subMasks: List<SubMaskState> = emptyList()
) {
    fun toJsonObject(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("name", name)
            put("visible", visible)
            put("invert", invert)
            put("opacity", opacity)
            put("adjustments", adjustments.toJsonObject(includeToneMapper = false))
            put(
                "subMasks",
                JSONArray().apply {
                    subMasks.forEach { put(it.toJsonObject()) }
                }
            )
        }
    }
}

private fun SubMaskState.toJsonObject(): JSONObject {
    return JSONObject().apply {
        put("id", id)
        put("type", "brush")
        put("visible", visible)
        put("mode", mode.name.lowercase(Locale.US))
        put(
            "parameters",
            JSONObject().apply {
                put(
                    "lines",
                    JSONArray().apply {
                        lines.forEach { line ->
                            put(
                                JSONObject().apply {
                                    put("tool", line.tool)
                                    put("brushSize", line.brushSize)
                                    put("feather", line.feather)
                                    put("order", line.order)
                                    put(
                                        "points",
                                        JSONArray().apply {
                                            line.points.forEach { point ->
                                                put(JSONObject().apply {
                                                    put("x", point.x)
                                                    put("y", point.y)
                                                })
                                            }
                                        }
                                    )
                                }
                            )
                        }
                    }
                )
            }
        )
    }
}

private data class BrushEvent(
    val order: Long,
    val mode: SubMaskMode,
    val brushSize: Float,
    val points: List<MaskPoint>
)

private fun buildMaskOverlayBitmap(mask: MaskState, targetWidth: Int, targetHeight: Int): Bitmap {
    val width = targetWidth.coerceAtLeast(1)
    val height = targetHeight.coerceAtLeast(1)
    val baseDim = minOf(width, height).toFloat()

    fun denorm(value: Float, max: Int): Float {
        val maxCoord = (max - 1).coerceAtLeast(1).toFloat()
        return if (value <= 1.5f) (value * maxCoord).coerceIn(0f, maxCoord) else value
    }

    val events = buildList {
        mask.subMasks.forEach { sub ->
            if (!sub.visible) return@forEach
            sub.lines.forEach { line ->
                val effectiveMode =
                    if (line.tool == "eraser") SubMaskMode.Subtractive else sub.mode
                add(
                    BrushEvent(
                        order = line.order,
                        mode = effectiveMode,
                        brushSize = line.brushSize,
                        points = line.points
                    )
                )
            }
        }
    }.sortedBy { it.order }

    val overlay = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(overlay)
    canvas.drawColor(android.graphics.Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

    val addPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.argb(140, 255, 23, 68) // red overlay
        style = android.graphics.Paint.Style.STROKE
        strokeCap = android.graphics.Paint.Cap.ROUND
        strokeJoin = android.graphics.Paint.Join.ROUND
    }
    val subPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        style = android.graphics.Paint.Style.STROKE
        strokeCap = android.graphics.Paint.Cap.ROUND
        strokeJoin = android.graphics.Paint.Join.ROUND
    }

    fun brushPx(brushSizeRaw: Float): Float {
        return if (brushSizeRaw <= 1.5f) (brushSizeRaw * baseDim).coerceAtLeast(1f) else brushSizeRaw
    }

    events.forEach { event ->
        val paint = if (event.mode == SubMaskMode.Additive) addPaint else subPaint
        paint.strokeWidth = brushPx(event.brushSize)

        if (event.points.isEmpty()) return@forEach
        if (event.points.size == 1) {
            val p = event.points[0]
            val x = denorm(p.x, width)
            val y = denorm(p.y, height)
            canvas.drawCircle(x, y, paint.strokeWidth / 2f, paint)
            return@forEach
        }

        val path = android.graphics.Path()
        val first = event.points.first()
        path.moveTo(denorm(first.x, width), denorm(first.y, height))
        event.points.drop(1).forEach { p ->
            path.lineTo(denorm(p.x, width), denorm(p.y, height))
        }
        canvas.drawPath(path, paint)
    }

    return overlay
}

private data class AdjustmentControl(
    val field: AdjustmentField,
    val label: String,
    val range: ClosedFloatingPointRange<Float>,
    val step: Float,
    val defaultValue: Float = 0f,
    val formatter: (Float) -> String = { value -> String.format(Locale.US, "%.0f", value) }
)

private data class RenderRequest(
    val version: Long,
    val adjustmentsJson: String
)

private val basicSection = listOf(
    AdjustmentControl(
        field = AdjustmentField.Brightness,
        label = "Brightness",
        range = -5f..5f,
        step = 0.01f,
        defaultValue = 0f,
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
    AdjustmentControl(field = AdjustmentField.LumaNoiseReduction, label = "Luminance NR", range = 0f..100f, step = 1f, defaultValue = 0f),
    AdjustmentControl(field = AdjustmentField.ColorNoiseReduction, label = "Color NR", range = 0f..100f, step = 1f, defaultValue = 0f),
    AdjustmentControl(field = AdjustmentField.ChromaticAberrationRedCyan, label = "CA Red/Cyan", range = -100f..100f, step = 1f),
    AdjustmentControl(field = AdjustmentField.ChromaticAberrationBlueYellow, label = "CA Blue/Yellow", range = -100f..100f, step = 1f)
)

private val adjustmentSections = listOf(
    "Basic" to basicSection,
    "Color" to colorSection,
    "Details" to detailsSection
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
    var masks by remember { mutableStateOf<List<MaskState>>(emptyList()) }
    var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var isExporting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    var panelTab by remember { mutableStateOf(EditorPanelTab.Adjustments) }
    var selectedMaskId by remember { mutableStateOf<String?>(null) }
    var selectedSubMaskId by remember { mutableStateOf<String?>(null) }
    var isPaintingMask by remember { mutableStateOf(false) }
    var brushSize by remember { mutableStateOf(60f) }
    var showMaskOverlay by remember { mutableStateOf(true) }
    val strokeOrder = remember { AtomicLong(0L) }

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

                val masksArr = json.optJSONArray("masks") ?: JSONArray()
                val parsedMasks = (0 until masksArr.length()).mapNotNull { idx ->
                    val maskObj = masksArr.optJSONObject(idx) ?: return@mapNotNull null
                    val maskId = maskObj.optString("id").takeIf { it.isNotBlank() }
                        ?: java.util.UUID.randomUUID().toString()
                    val maskAdjustmentsObj = maskObj.optJSONObject("adjustments") ?: JSONObject()
                    val maskAdjustments = AdjustmentState(
                        brightness = maskAdjustmentsObj.optDouble("brightness", 0.0).toFloat(),
                        contrast = maskAdjustmentsObj.optDouble("contrast", 0.0).toFloat(),
                        highlights = maskAdjustmentsObj.optDouble("highlights", 0.0).toFloat(),
                        shadows = maskAdjustmentsObj.optDouble("shadows", 0.0).toFloat(),
                        whites = maskAdjustmentsObj.optDouble("whites", 0.0).toFloat(),
                        blacks = maskAdjustmentsObj.optDouble("blacks", 0.0).toFloat(),
                        saturation = maskAdjustmentsObj.optDouble("saturation", 0.0).toFloat(),
                        temperature = maskAdjustmentsObj.optDouble("temperature", 0.0).toFloat(),
                        tint = maskAdjustmentsObj.optDouble("tint", 0.0).toFloat(),
                        vibrance = maskAdjustmentsObj.optDouble("vibrance", 0.0).toFloat(),
                        clarity = maskAdjustmentsObj.optDouble("clarity", 0.0).toFloat(),
                        dehaze = maskAdjustmentsObj.optDouble("dehaze", 0.0).toFloat(),
                        structure = maskAdjustmentsObj.optDouble("structure", 0.0).toFloat(),
                        centre = maskAdjustmentsObj.optDouble("centre", 0.0).toFloat(),
                        sharpness = maskAdjustmentsObj.optDouble("sharpness", 0.0).toFloat(),
                        lumaNoiseReduction = maskAdjustmentsObj.optDouble("lumaNoiseReduction", 0.0).toFloat(),
                        colorNoiseReduction = maskAdjustmentsObj.optDouble("colorNoiseReduction", 0.0).toFloat(),
                        chromaticAberrationRedCyan = maskAdjustmentsObj.optDouble("chromaticAberrationRedCyan", 0.0).toFloat(),
                        chromaticAberrationBlueYellow = maskAdjustmentsObj.optDouble("chromaticAberrationBlueYellow", 0.0).toFloat(),
                        toneMapper = adjustments.toneMapper
                    )

                    val subMasksArr = maskObj.optJSONArray("subMasks") ?: JSONArray()
                    val subMasks = (0 until subMasksArr.length()).mapNotNull { sIdx ->
                        val subObj = subMasksArr.optJSONObject(sIdx) ?: return@mapNotNull null
                        val subId = subObj.optString("id").takeIf { it.isNotBlank() }
                            ?: java.util.UUID.randomUUID().toString()
                        val modeStr = subObj.optString("mode", "additive").lowercase(Locale.US)
                        val mode = if (modeStr == "subtractive") SubMaskMode.Subtractive else SubMaskMode.Additive
                        val paramsObj = subObj.optJSONObject("parameters") ?: JSONObject()
                        val linesArr = paramsObj.optJSONArray("lines") ?: JSONArray()
                        val lines = (0 until linesArr.length()).mapNotNull { lIdx ->
                            val lineObj = linesArr.optJSONObject(lIdx) ?: return@mapNotNull null
                            val pointsArr = lineObj.optJSONArray("points") ?: JSONArray()
                            val points = (0 until pointsArr.length()).mapNotNull { pIdx ->
                                val pObj = pointsArr.optJSONObject(pIdx) ?: return@mapNotNull null
                                MaskPoint(
                                    x = pObj.optDouble("x", 0.0).toFloat(),
                                    y = pObj.optDouble("y", 0.0).toFloat()
                                )
                            }
                            BrushLineState(
                                tool = lineObj.optString("tool", "brush"),
                                brushSize = lineObj.optDouble("brushSize", 50.0).toFloat(),
                                feather = lineObj.optDouble("feather", 0.5).toFloat(),
                                order = lineObj.optLong("order", 0L),
                                points = points
                            )
                        }
                        SubMaskState(
                            id = subId,
                            visible = subObj.optBoolean("visible", true),
                            mode = mode,
                            lines = lines
                        )
                    }

                    MaskState(
                        id = maskId,
                        name = maskObj.optString("name", "Mask"),
                        visible = maskObj.optBoolean("visible", true),
                        invert = maskObj.optBoolean("invert", false),
                        opacity = maskObj.optDouble("opacity", 100.0).toFloat(),
                        adjustments = maskAdjustments,
                        subMasks = subMasks
                    )
                }
                val maxOrder = parsedMasks
                    .flatMap { it.subMasks }
                    .flatMap { it.lines }
                    .maxOfOrNull { it.order } ?: 0L
                strokeOrder.set(maxOf(strokeOrder.get(), maxOrder))
                masks = parsedMasks
                if (selectedMaskId == null && parsedMasks.isNotEmpty()) {
                    selectedMaskId = parsedMasks.first().id
                    selectedSubMaskId = parsedMasks.first().subMasks.firstOrNull()?.id
                }
            } catch (e: Exception) {
                // Keep default adjustments if parsing fails
            }
        }
    }

    LaunchedEffect(adjustments, masks) {
        val json = withContext(Dispatchers.Default) { adjustments.toJson(masks) }
        val version = renderVersion.incrementAndGet()
        renderRequests.trySend(RenderRequest(version = version, adjustmentsJson = json))

        // Debounce persisting adjustments (I/O) for slider drags + mask edits.
        delay(350)
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
                adjustmentsJson = adjustments.toJson(masks)
            )
        )

        var currentRequest = renderRequests.receive()
        while (true) {
            while (true) {
                val next = renderRequests.tryReceive().getOrNull() ?: break
                currentRequest = next
            }

            val requestVersion = currentRequest.version
            val requestJson = currentRequest.adjustmentsJson

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

    val selectedMaskForOverlay = masks.firstOrNull { it.id == selectedMaskId }
    val isMaskMode = panelTab == EditorPanelTab.Masks
    val isPaintingEnabled = isMaskMode && isPaintingMask && selectedMaskId != null && selectedSubMaskId != null

    val onBrushStrokeFinished: (List<MaskPoint>, Float) -> Unit = onBrush@{ points, brushSizeNorm ->
        val maskId = selectedMaskId ?: return@onBrush
        val subId = selectedSubMaskId ?: return@onBrush
        if (points.isEmpty()) return@onBrush
        val newLine = BrushLineState(
            tool = "brush",
            brushSize = brushSizeNorm,
            feather = 0.5f,
            order = strokeOrder.incrementAndGet(),
            points = points
        )
        masks = masks.map { mask ->
            if (mask.id != maskId) return@map mask
            mask.copy(
                subMasks = mask.subMasks.map { sub ->
                    if (sub.id != subId) sub else sub.copy(lines = sub.lines + newLine)
                }
            )
        }
        showMaskOverlay = true
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
                            ImagePreview(
                                bitmap = previewBitmap,
                                isLoading = isLoading,
                                maskOverlay = selectedMaskForOverlay,
                                isMaskMode = isMaskMode,
                                showMaskOverlay = showMaskOverlay,
                                isPainting = isPaintingEnabled,
                                brushSize = brushSize,
                                onBrushStrokeFinished = onBrushStrokeFinished
                            )
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
                                TabbedEditorControls(
                                    panelTab = panelTab,
                                    onPanelTabChange = { panelTab = it },
                                    adjustments = adjustments,
                                    onAdjustmentsChange = { adjustments = it },
                                    masks = masks,
                                    onMasksChange = { masks = it },
                                    selectedMaskId = selectedMaskId,
                                    onSelectedMaskIdChange = { selectedMaskId = it },
                                    selectedSubMaskId = selectedSubMaskId,
                                    onSelectedSubMaskIdChange = { selectedSubMaskId = it },
                                    isPaintingMask = isPaintingMask,
                                    onPaintingMaskChange = { isPaintingMask = it },
                                    showMaskOverlay = showMaskOverlay,
                                    onShowMaskOverlayChange = { showMaskOverlay = it },
                                    brushSize = brushSize,
                                    onBrushSizeChange = { brushSize = it }
                                )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Export button at bottom
                            ExportButton(
                                rawBytes = rawBytes,
                                adjustments = adjustments,
                                masks = masks,
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
                        ImagePreview(
                            bitmap = previewBitmap,
                            isLoading = isLoading,
                            maskOverlay = selectedMaskForOverlay,
                            isMaskMode = isMaskMode,
                            showMaskOverlay = showMaskOverlay,
                            isPainting = isPaintingEnabled,
                            brushSize = brushSize,
                            onBrushStrokeFinished = onBrushStrokeFinished
                        )
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
                                        masks = masks,
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

                            TabbedEditorControls(
                                panelTab = panelTab,
                                onPanelTabChange = { panelTab = it },
                                adjustments = adjustments,
                                onAdjustmentsChange = { adjustments = it },
                                masks = masks,
                                onMasksChange = { masks = it },
                                selectedMaskId = selectedMaskId,
                                onSelectedMaskIdChange = { selectedMaskId = it },
                                selectedSubMaskId = selectedSubMaskId,
                                onSelectedSubMaskIdChange = { selectedSubMaskId = it },
                                isPaintingMask = isPaintingMask,
                                onPaintingMaskChange = { isPaintingMask = it },
                                showMaskOverlay = showMaskOverlay,
                                onShowMaskOverlayChange = { showMaskOverlay = it },
                                brushSize = brushSize,
                                onBrushSizeChange = { brushSize = it }
                            )
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
    masks: List<MaskState>,
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
            val currentMasks = masks
            onExportStart()
            coroutineScope.launch {
                val currentJson = withContext(Dispatchers.Default) { currentAdjustments.toJson(currentMasks) }
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
private fun TabbedEditorControls(
    panelTab: EditorPanelTab,
    onPanelTabChange: (EditorPanelTab) -> Unit,
    adjustments: AdjustmentState,
    onAdjustmentsChange: (AdjustmentState) -> Unit,
    masks: List<MaskState>,
    onMasksChange: (List<MaskState>) -> Unit,
    selectedMaskId: String?,
    onSelectedMaskIdChange: (String?) -> Unit,
    selectedSubMaskId: String?,
    onSelectedSubMaskIdChange: (String?) -> Unit,
    isPaintingMask: Boolean,
    onPaintingMaskChange: (Boolean) -> Unit,
    showMaskOverlay: Boolean,
    onShowMaskOverlayChange: (Boolean) -> Unit,
    brushSize: Float,
    onBrushSizeChange: (Float) -> Unit
) {
    TabRow(selectedTabIndex = if (panelTab == EditorPanelTab.Adjustments) 0 else 1) {
        Tab(
            selected = panelTab == EditorPanelTab.Adjustments,
            onClick = {
                onPaintingMaskChange(false)
                onPanelTabChange(EditorPanelTab.Adjustments)
            },
            text = { Text("Adjust") }
        )
        Tab(
            selected = panelTab == EditorPanelTab.Masks,
            onClick = {
                onShowMaskOverlayChange(true)
                onPanelTabChange(EditorPanelTab.Masks)
            },
            text = { Text("Mask") }
        )
    }

    when (panelTab) {
        EditorPanelTab.Adjustments -> {
            ToneMapperSection(
                toneMapper = adjustments.toneMapper,
                onToneMapperChange = { mapper -> onAdjustmentsChange(adjustments.withToneMapper(mapper)) }
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
                            defaultValue = control.defaultValue,
                            formatter = control.formatter,
                            onValueChange = { snapped ->
                                onAdjustmentsChange(adjustments.withValue(control.field, snapped))
                            }
                        )
                    }
                }
            }
        }

        EditorPanelTab.Masks -> {
            val selectedMask = masks.firstOrNull { it.id == selectedMaskId }
            val selectedSubMask = selectedMask?.subMasks?.firstOrNull { it.id == selectedSubMaskId }

            FilledTonalButton(
                onClick = {
                    val newMaskId = java.util.UUID.randomUUID().toString()
                    val newSubId = java.util.UUID.randomUUID().toString()
                    val newMask = MaskState(
                        id = newMaskId,
                        name = "Mask ${masks.size + 1}",
                        subMasks = listOf(SubMaskState(id = newSubId, mode = SubMaskMode.Additive))
                    )
                    onMasksChange(masks + newMask)
                    onSelectedMaskIdChange(newMaskId)
                    onSelectedSubMaskIdChange(newSubId)
                    onPaintingMaskChange(true)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Brush Mask")
            }

            if (masks.isEmpty()) {
                Text(
                    text = "Create a mask to start painting.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                return
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                masks.forEachIndexed { index, mask ->
                    val isSelected = mask.id == selectedMaskId
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected)
                                MaterialTheme.colorScheme.secondaryContainer
                            else
                                MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = mask.visible,
                                onCheckedChange = { checked ->
                                    onMasksChange(
                                        masks.map { m -> if (m.id == mask.id) m.copy(visible = checked) else m }
                                    )
                                }
                            )
                            Text(
                                text = mask.name,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        onPaintingMaskChange(false)
                                        onShowMaskOverlayChange(mask.adjustments.isNeutralForMask())
                                        onSelectedMaskIdChange(mask.id)
                                        onSelectedSubMaskIdChange(mask.subMasks.firstOrNull()?.id)
                                    },
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                            IconButton(
                                onClick = {
                                    if (index <= 0) return@IconButton
                                    val reordered = masks.toMutableList()
                                    val tmp = reordered[index - 1]
                                    reordered[index - 1] = reordered[index]
                                    reordered[index] = tmp
                                    onMasksChange(reordered.toList())
                                }
                            ) { Text("↑") }
                            IconButton(
                                onClick = {
                                    if (index >= masks.lastIndex) return@IconButton
                                    val reordered = masks.toMutableList()
                                    val tmp = reordered[index + 1]
                                    reordered[index + 1] = reordered[index]
                                    reordered[index] = tmp
                                    onMasksChange(reordered.toList())
                                }
                            ) { Text("↓") }
                            IconButton(
                                onClick = {
                                    val remaining = masks.filterNot { it.id == mask.id }
                                    onMasksChange(remaining)
                                    if (selectedMaskId == mask.id) {
                                        onPaintingMaskChange(false)
                                        onSelectedMaskIdChange(remaining.firstOrNull()?.id)
                                        onSelectedSubMaskIdChange(remaining.firstOrNull()?.subMasks?.firstOrNull()?.id)
                                    }
                                }
                            ) { Text("✕") }
                        }
                    }
                }
            }

            if (selectedMask == null) return

            Text(
                text = "Mask Settings",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp),
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Invert", color = MaterialTheme.colorScheme.onSurface)
                Checkbox(
                    checked = selectedMask.invert,
                    onCheckedChange = { checked ->
                        onMasksChange(
                            masks.map { m -> if (m.id == selectedMask.id) m.copy(invert = checked) else m }
                        )
                    }
                )
            }

            Text("Opacity: ${selectedMask.opacity.roundToInt()}%", color = MaterialTheme.colorScheme.onSurface)
            Slider(
                value = selectedMask.opacity.coerceIn(0f, 100f),
                onValueChange = { newValue ->
                    onMasksChange(
                        masks.map { m -> if (m.id == selectedMask.id) m.copy(opacity = newValue) else m }
                    )
                },
                valueRange = 0f..100f
            )

            Text(
                text = "Submasks",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp),
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalButton(
                    onClick = {
                        val newSubId = java.util.UUID.randomUUID().toString()
                        val updated = masks.map { m ->
                            if (m.id != selectedMask.id) m
                            else m.copy(subMasks = m.subMasks + SubMaskState(id = newSubId, mode = SubMaskMode.Additive))
                        }
                        onMasksChange(updated)
                        onSelectedSubMaskIdChange(newSubId)
                    },
                    modifier = Modifier.weight(1f)
                ) { Text("Add +") }
                FilledTonalButton(
                    onClick = {
                        val newSubId = java.util.UUID.randomUUID().toString()
                        val updated = masks.map { m ->
                            if (m.id != selectedMask.id) m
                            else m.copy(subMasks = m.subMasks + SubMaskState(id = newSubId, mode = SubMaskMode.Subtractive))
                        }
                        onMasksChange(updated)
                        onSelectedSubMaskIdChange(newSubId)
                    },
                    modifier = Modifier.weight(1f)
                ) { Text("Subtract -") }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                selectedMask.subMasks.forEach { sub ->
                    val isSelected = sub.id == selectedSubMaskId
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected)
                                MaterialTheme.colorScheme.tertiaryContainer
                            else
                                MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = sub.visible,
                                onCheckedChange = { checked ->
                                    val updated = masks.map { m ->
                                        if (m.id != selectedMask.id) m
                                        else m.copy(
                                            subMasks = m.subMasks.map { s -> if (s.id == sub.id) s.copy(visible = checked) else s }
                                        )
                                    }
                                    onMasksChange(updated)
                                }
                            )
                            Text(
                                text = if (sub.mode == SubMaskMode.Additive) "Add" else "Subtract",
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onSelectedSubMaskIdChange(sub.id) },
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                            IconButton(
                                onClick = {
                                    val updated = masks.map { m ->
                                        if (m.id != selectedMask.id) m
                                        else m.copy(subMasks = m.subMasks.filterNot { it.id == sub.id })
                                    }
                                    onMasksChange(updated)
                                    if (selectedSubMaskId == sub.id) {
                                        onPaintingMaskChange(false)
                                        onSelectedSubMaskIdChange(updated.firstOrNull { it.id == selectedMask.id }?.subMasks?.firstOrNull()?.id)
                                    }
                                }
                            ) { Text("✕") }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Paint", color = MaterialTheme.colorScheme.onSurface)
                Checkbox(
                    checked = isPaintingMask,
                    onCheckedChange = { checked ->
                        onPaintingMaskChange(checked)
                        if (checked) onShowMaskOverlayChange(true)
                    }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Show Overlay", color = MaterialTheme.colorScheme.onSurface)
                Checkbox(
                    checked = showMaskOverlay,
                    onCheckedChange = { checked -> onShowMaskOverlayChange(checked) },
                    enabled = selectedMask.adjustments.isNeutralForMask()
                )
            }

            if (selectedSubMask == null) {
                Text(
                    text = "Select a submask to paint.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                return
            }

            Text("Brush Size: ${brushSize.roundToInt()} px", color = MaterialTheme.colorScheme.onSurface)
            Slider(
                value = brushSize.coerceIn(2f, 400f),
                onValueChange = { onBrushSizeChange(it) },
                valueRange = 2f..400f
            )

            Text(
                text = "Mask Adjustments",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp),
                color = MaterialTheme.colorScheme.onSurface
            )

            adjustmentSections.forEach { (sectionTitle, controls) ->
                Text(
                    text = sectionTitle,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(top = 8.dp),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    controls.forEach { control ->
                        val currentValue = selectedMask.adjustments.valueFor(control.field)
                        AdjustmentSlider(
                            label = control.label,
                            value = currentValue,
                            range = control.range,
                            step = control.step,
                            defaultValue = control.defaultValue,
                            formatter = control.formatter,
                            onValueChange = { snapped ->
                                val updated = masks.map { m ->
                                    if (m.id != selectedMask.id) m
                                    else m.copy(adjustments = m.adjustments.withValue(control.field, snapped))
                                }
                                onMasksChange(updated)
                                val newMask = updated.firstOrNull { it.id == selectedMask.id }
                                onShowMaskOverlayChange(newMask?.adjustments?.isNeutralForMask() == true)
                            }
                        )
                    }
                }
            }
        }
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
    defaultValue: Float,
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
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(label, defaultValue) {
                    detectTapGestures(onDoubleTap = { onValueChange(defaultValue) })
                },
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
private fun ImagePreview(
    bitmap: Bitmap?,
    isLoading: Boolean,
    maskOverlay: MaskState? = null,
    isMaskMode: Boolean = false,
    showMaskOverlay: Boolean = true,
    isPainting: Boolean = false,
    brushSize: Float = 60f,
    onBrushStrokeFinished: ((List<MaskPoint>, Float) -> Unit)? = null
) {
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    val currentStroke = remember { mutableStateListOf<MaskPoint>() }
    val density = LocalDensity.current

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val containerW = with(density) { maxWidth.toPx() }
        val containerH = with(density) { maxHeight.toPx() }

        if (bitmap != null) {
            val bmpW = bitmap.width.toFloat().coerceAtLeast(1f)
            val bmpH = bitmap.height.toFloat().coerceAtLeast(1f)
            val baseScale = minOf(containerW / bmpW, containerH / bmpH)
            val displayW = bmpW * baseScale
            val displayH = bmpH * baseScale
            val left = (containerW - displayW) / 2f
            val top = (containerH - displayH) / 2f

            val baseDim = minOf(bmpW, bmpH)

            fun toImagePoint(pos: Offset): MaskPoint {
                val nx = ((pos.x - left) / displayW).coerceIn(0f, 1f)
                val ny = ((pos.y - top) / displayH).coerceIn(0f, 1f)
                return MaskPoint(x = nx, y = ny)
            }

            val imageModifier = Modifier
                .fillMaxSize()
                .let { base ->
                    if (isMaskMode) base
                    else base
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
                }

            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Processed preview",
                contentScale = ContentScale.Fit,
                modifier = imageModifier
            )

            if (isMaskMode) {
                val overlayBitmap = remember(maskOverlay, showMaskOverlay, bitmap.width, bitmap.height) {
                    if (!showMaskOverlay || maskOverlay == null || !maskOverlay.adjustments.isNeutralForMask()) {
                        null
                    } else {
                        val maxDim = 768
                        val w = bitmap.width
                        val h = bitmap.height
                        val scale = if (w >= h) maxDim.toFloat() / w.coerceAtLeast(1) else maxDim.toFloat() / h.coerceAtLeast(1)
                        val outW = (w * scale).toInt().coerceAtLeast(1)
                        val outH = (h * scale).toInt().coerceAtLeast(1)
                        buildMaskOverlayBitmap(maskOverlay, outW, outH)
                    }
                }
                if (overlayBitmap != null) {
                    Image(
                        bitmap = overlayBitmap.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                if (currentStroke.isNotEmpty()) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val brushSizePx = brushSize * baseScale
                        val strokeWidth = brushSizePx.coerceAtLeast(1f)
                        val maxX = (bmpW - 1f).coerceAtLeast(1f)
                        val maxY = (bmpH - 1f).coerceAtLeast(1f)
                        fun toDisplayOffset(p: MaskPoint): Offset {
                            val px = if (p.x <= 1.5f) p.x * maxX else p.x
                            val py = if (p.y <= 1.5f) p.y * maxY else p.y
                            return Offset(left + px * baseScale, top + py * baseScale)
                        }
                        val path = Path().apply {
                            val first = currentStroke.firstOrNull()?.let(::toDisplayOffset) ?: return@Canvas
                            moveTo(first.x, first.y)
                            currentStroke.drop(1).forEach { p ->
                                val o = toDisplayOffset(p)
                                lineTo(o.x, o.y)
                            }
                        }
                        drawPath(
                            path = path,
                            color = Color(0x88FFFFFF),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )
                    }
                }
            }

            if (isMaskMode && isPainting && onBrushStrokeFinished != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(isPainting, brushSize, bitmap) {
                            detectDragGestures(
                                onDragStart = { start ->
                                    currentStroke.clear()
                                    currentStroke.add(toImagePoint(start))
                                },
                                onDrag = { change, _ ->
                                    change.consume()
                                    val newPoint = toImagePoint(change.position)
                                    val last = currentStroke.lastOrNull()
                                    val brushSizeNorm = (brushSize / baseDim).coerceAtLeast(0.0001f)
                                    val minStep = (brushSizeNorm / 6f).coerceAtLeast(0.003f)
                                    if (last == null) {
                                        currentStroke.add(newPoint)
                                    } else {
                                        val dx = newPoint.x - last.x
                                        val dy = newPoint.y - last.y
                                        if (dx * dx + dy * dy >= minStep * minStep) {
                                            currentStroke.add(newPoint)
                                        }
                                    }
                                },
                                onDragEnd = {
                                    if (currentStroke.isNotEmpty()) {
                                        val brushSizeNorm = (brushSize / baseDim).coerceAtLeast(0.0001f)
                                        onBrushStrokeFinished(currentStroke.toList(), brushSizeNorm)
                                    }
                                    currentStroke.clear()
                                },
                                onDragCancel = { currentStroke.clear() }
                            )
                        }
                )
            }
        } else {
            Text(
                text = "No preview yet",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (isLoading) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
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
