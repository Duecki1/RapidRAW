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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dueckis.kawaiiraweditor.ui.theme.KawaiiRawEditorTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.ranges.ClosedFloatingPointRange

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
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var rawBytes by remember { mutableStateOf<ByteArray?>(null) }
    var fileName by remember { mutableStateOf<String?>(null) }
    var adjustments by remember { mutableStateOf(AdjustmentState()) }
    var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var isExporting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var debouncedAdjustmentJson by remember { mutableStateOf(adjustments.toJson()) }

    val mimeTypes = arrayOf("image/x-sony-arw", "image/*")
    val pickRaw = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        coroutineScope.launch {
            val bytes = withContext(Dispatchers.IO) {
                context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            }
            if (bytes == null) {
                errorMessage = "Unable to read selected file."
                return@launch
            }
            rawBytes = bytes
            fileName = displayNameForUri(context, uri)
            previewBitmap = null
            errorMessage = null
            statusMessage = null
        }
    }

    LaunchedEffect(adjustments) {
        delay(200)
        debouncedAdjustmentJson = adjustments.toJson()
    }

    LaunchedEffect(rawBytes, debouncedAdjustmentJson) {
        val raw = rawBytes ?: return@LaunchedEffect
        isLoading = true
        errorMessage = null
        val previewBytes = withContext(Dispatchers.Default) {
            runCatching { LibRawDecoder.decode(raw, debouncedAdjustmentJson) }.getOrNull()
        }
        if (previewBytes == null) {
            errorMessage = "Failed to render preview."
        } else {
            previewBitmap = previewBytes.decodeToBitmap()
        }
        isLoading = false
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = { pickRaw.launch(mimeTypes) }) {
                    Text("Load RAW")
                }
                Button(
                    onClick = {
                        if (rawBytes == null || isExporting) return@Button
                        val currentJson = debouncedAdjustmentJson
                        isExporting = true
                        errorMessage = null
                        statusMessage = null
                        coroutineScope.launch {
                            val fullBytes = withContext(Dispatchers.Default) {
                                runCatching { LibRawDecoder.decodeFullRes(rawBytes!!, currentJson) }.getOrNull()
                            }
                            if (fullBytes == null) {
                                errorMessage = "Export failed."
                            } else {
                                val savedUri = withContext(Dispatchers.IO) {
                                    saveJpegToPictures(context, fullBytes)
                                }
                                if (savedUri != null) {
                                    statusMessage = "Saved to $savedUri"
                                } else {
                                    errorMessage = "Export failed: could not save JPEG."
                                }
                            }
                            isExporting = false
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

            Text(
                text = fileName ?: "No RAW file selected",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
            )

            ImagePreview(bitmap = previewBitmap, isLoading = isLoading)

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
                    modifier = Modifier.padding(top = 8.dp)
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
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Button(
                onClick = { onToneMapperChange("basic") },
                modifier = Modifier.weight(1f),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = if (toneMapper == "basic") 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text("Basic")
            }
            Button(
                onClick = { onToneMapperChange("agx") },
                modifier = Modifier.weight(1f),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = if (toneMapper == "agx") 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text("AgX")
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
            Text(text = label, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = formatter(value),
                style = MaterialTheme.typography.bodySmall,
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
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Processed preview",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
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
