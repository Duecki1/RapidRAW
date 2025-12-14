//Code taken from RapidRAW by CyberTimon
//https://github.com/CyberTimon/RapidRAW

mod raw_processing;

use anyhow::{Context, Result};
use image::{codecs::jpeg::JpegEncoder, imageops::FilterType, DynamicImage, ImageBuffer, Rgba};
use jni::objects::{JByteArray, JClass, JString};
use jni::sys::jbyteArray;
use jni::JNIEnv;
use log::{error, info};
#[cfg(target_os = "android")]
use log::Level;
use raw_processing::develop_raw_image;
use serde::Deserialize;
use serde_json::from_str;
use std::ops::AddAssign;
use std::ptr;
use std::sync::Once;

#[derive(Clone, Copy, Default)]
struct AdjustmentValues {
    exposure: f32,
    brightness: f32,
    contrast: f32,
    highlights: f32,
    shadows: f32,
    whites: f32,
    blacks: f32,
    saturation: f32,
    temperature: f32,
    tint: f32,
    vibrance: f32,
    clarity: f32,
    dehaze: f32,
    structure: f32,
    centre: f32,
    sharpness: f32,
    luma_noise_reduction: f32,
    color_noise_reduction: f32,
    chromatic_aberration_red_cyan: f32,
    chromatic_aberration_blue_yellow: f32,
    tone_mapper: ToneMapper,
}

#[derive(Clone, Copy, Default, Debug, Deserialize)]
#[serde(rename_all = "lowercase")]
enum ToneMapper {
    #[default]
    Basic,
    Agx,
}

struct AdjustmentScales {
    exposure: f32,
    brightness: f32,
    contrast: f32,
    highlights: f32,
    shadows: f32,
    whites: f32,
    blacks: f32,
    saturation: f32,
    temperature: f32,
    tint: f32,
    vibrance: f32,
    clarity: f32,
    dehaze: f32,
    structure: f32,
    centre: f32,
    sharpness: f32,
    luma_noise_reduction: f32,
    color_noise_reduction: f32,
    chromatic_aberration_red_cyan: f32,
    chromatic_aberration_blue_yellow: f32,
}

const ADJUSTMENT_SCALES: AdjustmentScales = AdjustmentScales {
    exposure: 0.8,
    brightness: 0.8,
    contrast: 100.0,
    highlights: 150.0,
    shadows: 100.0,
    whites: 30.0,
    blacks: 60.0,
    saturation: 100.0,
    temperature: 25.0,
    tint: 100.0,
    vibrance: 100.0,
    clarity: 200.0,
    dehaze: 750.0,
    structure: 200.0,
    centre: 250.0,  // RapidRAW uses 250.0
    sharpness: 80.0,
    luma_noise_reduction: 100.0,
    color_noise_reduction: 100.0,
    chromatic_aberration_red_cyan: 10000.0,  // RapidRAW uses 10000.0
    chromatic_aberration_blue_yellow: 10000.0,  // RapidRAW uses 10000.0
};

impl AdjustmentValues {
    fn normalized(self, scales: &AdjustmentScales) -> Self {
        fn scale(value: f32, divisor: f32) -> f32 {
            if divisor.abs() < std::f32::EPSILON {
                value
            } else {
                value / divisor
            }
        }

        AdjustmentValues {
            exposure: scale(self.exposure, scales.exposure),
            brightness: scale(self.brightness, scales.brightness),
            contrast: scale(self.contrast, scales.contrast),
            highlights: scale(self.highlights, scales.highlights),
            shadows: scale(self.shadows, scales.shadows),
            whites: scale(self.whites, scales.whites),
            blacks: scale(self.blacks, scales.blacks),
            saturation: scale(self.saturation, scales.saturation),
            temperature: scale(self.temperature, scales.temperature),
            tint: scale(self.tint, scales.tint),
            vibrance: scale(self.vibrance, scales.vibrance),
            clarity: scale(self.clarity, scales.clarity),
            dehaze: scale(self.dehaze, scales.dehaze),
            structure: scale(self.structure, scales.structure),
            centre: scale(self.centre, scales.centre),
            sharpness: scale(self.sharpness, scales.sharpness),
            luma_noise_reduction: scale(self.luma_noise_reduction, scales.luma_noise_reduction),
            color_noise_reduction: scale(self.color_noise_reduction, scales.color_noise_reduction),
            chromatic_aberration_red_cyan: scale(self.chromatic_aberration_red_cyan, scales.chromatic_aberration_red_cyan),
            chromatic_aberration_blue_yellow: scale(self.chromatic_aberration_blue_yellow, scales.chromatic_aberration_blue_yellow),
            tone_mapper: self.tone_mapper,
        }
    }
}

impl AddAssign for AdjustmentValues {
    fn add_assign(&mut self, rhs: Self) {
        self.exposure += rhs.exposure;
        self.brightness += rhs.brightness;
        self.contrast += rhs.contrast;
        self.highlights += rhs.highlights;
        self.shadows += rhs.shadows;
        self.whites += rhs.whites;
        self.blacks += rhs.blacks;
        self.saturation += rhs.saturation;
        self.temperature += rhs.temperature;
        self.tint += rhs.tint;
        self.vibrance += rhs.vibrance;
        self.clarity += rhs.clarity;
        self.dehaze += rhs.dehaze;
        self.structure += rhs.structure;
        self.centre += rhs.centre;
        self.sharpness += rhs.sharpness;
        self.luma_noise_reduction += rhs.luma_noise_reduction;
        self.color_noise_reduction += rhs.color_noise_reduction;
        self.chromatic_aberration_red_cyan += rhs.chromatic_aberration_red_cyan;
        self.chromatic_aberration_blue_yellow += rhs.chromatic_aberration_blue_yellow;
    }
}

#[derive(Clone, Default, Deserialize)]
#[serde(default)]
struct MaskPayload {
    enabled: bool,
    exposure: f32,
    brightness: f32,
    contrast: f32,
    highlights: f32,
    shadows: f32,
    whites: f32,
    blacks: f32,
    saturation: f32,
    temperature: f32,
    tint: f32,
    vibrance: f32,
}

impl MaskPayload {
    fn to_values(&self) -> AdjustmentValues {
        AdjustmentValues {
            exposure: self.exposure,
            brightness: self.brightness,
            contrast: self.contrast,
            highlights: self.highlights,
            shadows: self.shadows,
            whites: self.whites,
            blacks: self.blacks,
            saturation: self.saturation,
            temperature: self.temperature,
            tint: self.tint,
            vibrance: self.vibrance,
            ..Default::default()
        }
    }
}

#[derive(Clone, Default, Deserialize)]
#[serde(default, rename_all = "camelCase")]
struct AdjustmentsPayload {
    exposure: f32,
    brightness: f32,
    contrast: f32,
    highlights: f32,
    shadows: f32,
    whites: f32,
    blacks: f32,
    saturation: f32,
    temperature: f32,
    tint: f32,
    vibrance: f32,
    clarity: f32,
    dehaze: f32,
    structure: f32,
    centre: f32,
    sharpness: f32,
    luma_noise_reduction: f32,
    color_noise_reduction: f32,
    chromatic_aberration_red_cyan: f32,
    chromatic_aberration_blue_yellow: f32,
    #[serde(default)]
    tone_mapper: ToneMapper,
    masks: Vec<MaskPayload>,
}

impl AdjustmentsPayload {
    fn to_values(&self) -> AdjustmentValues {
        AdjustmentValues {
            exposure: self.exposure,
            brightness: self.brightness,
            contrast: self.contrast,
            highlights: self.highlights,
            shadows: self.shadows,
            whites: self.whites,
            blacks: self.blacks,
            saturation: self.saturation,
            temperature: self.temperature,
            tint: self.tint,
            vibrance: self.vibrance,
            clarity: self.clarity,
            dehaze: self.dehaze,
            structure: self.structure,
            centre: self.centre,
            sharpness: self.sharpness,
            luma_noise_reduction: self.luma_noise_reduction,
            color_noise_reduction: self.color_noise_reduction,
            chromatic_aberration_red_cyan: self.chromatic_aberration_red_cyan,
            chromatic_aberration_blue_yellow: self.chromatic_aberration_blue_yellow,
            tone_mapper: self.tone_mapper,
        }
    }
}

fn clamp_to_u8(value: f32) -> u8 {
    value.clamp(0.0, 255.0) as u8
}

fn smoothstep(edge0: f32, edge1: f32, x: f32) -> f32 {
    if (edge1 - edge0).abs() < std::f32::EPSILON {
        return 0.0;
    }
    let t = ((x - edge0) / (edge1 - edge0)).clamp(0.0, 1.0);
    t * t * (3.0 - 2.0 * t)
}

fn parse_adjustments(json: Option<&str>) -> AdjustmentValues {
    if let Some(data) = json {
        let trimmed = data.trim();
        if trimmed.is_empty() {
            return AdjustmentValues::default();
        }
        match from_str::<AdjustmentsPayload>(trimmed) {
            Ok(payload) => {
                let mut values = payload.to_values();
                for mask in payload.masks.iter().filter(|mask| mask.enabled) {
                    values += mask.to_values();
                }
                values
            }
            Err(err) => {
                error!("Failed to parse adjustments JSON: {}", err);
                AdjustmentValues::default()
            }
        }
    } else {
        AdjustmentValues::default()
    }
}

fn get_luma(color: [f32; 3]) -> f32 {
    color[0] * 0.2126 + color[1] * 0.7152 + color[2] * 0.0722
}

fn rgb_to_hue(color: [f32; 3]) -> f32 {
    let r = color[0];
    let g = color[1];
    let b = color[2];
    let max = r.max(g).max(b);
    let min = r.min(g).min(b);
    let delta = max - min;
    
    if delta < 0.0001 {
        return 0.0;
    }
    
    let hue = if max == r {
        60.0 * (((g - b) / delta) % 6.0)
    } else if max == g {
        60.0 * (((b - r) / delta) + 2.0)
    } else {
        60.0 * (((r - g) / delta) + 4.0)
    };
    
    if hue < 0.0 { hue + 360.0 } else { hue }
}

fn apply_filmic_brightness(colors: [f32; 3], brightness_adj: f32) -> [f32; 3] {
    if brightness_adj.abs() < 0.00001 {
        return colors;
    }
    
    const RATIONAL_CURVE_MIX: f32 = 0.95;
    const MIDTONE_STRENGTH: f32 = 1.2;
    let original_luma = get_luma(colors);
    
    if original_luma.abs() < 0.00001 {
        return colors;
    }
    
    let direct_adj = brightness_adj * (1.0 - RATIONAL_CURVE_MIX);
    let rational_adj = brightness_adj * RATIONAL_CURVE_MIX;
    let scale = 2f32.powf(direct_adj);
    let k = 2f32.powf(-rational_adj * MIDTONE_STRENGTH);
    
    let luma_abs = original_luma.abs();
    let luma_floor = luma_abs.floor();
    let luma_fract = luma_abs - luma_floor;
    let shaped_fract = luma_fract / (luma_fract + (1.0 - luma_fract) * k);
    let shaped_luma_abs = luma_floor + shaped_fract;
    let new_luma = original_luma.signum() * shaped_luma_abs * scale;
    
    let chroma = [colors[0] - original_luma, colors[1] - original_luma, colors[2] - original_luma];
    let total_luma_scale = new_luma / original_luma;
    let chroma_scale = total_luma_scale.powf(0.8);
    
    [
        new_luma + chroma[0] * chroma_scale,
        new_luma + chroma[1] * chroma_scale,
        new_luma + chroma[2] * chroma_scale,
    ]
}

fn apply_tonal_adjustments(mut colors: [f32; 3], contrast: f32, shadows: f32, whites: f32, blacks: f32) -> [f32; 3] {
    // Whites adjustment
    if whites.abs() > 0.00001 {
        let white_level = 1.0 - whites * 0.25;
        for channel in colors.iter_mut() {
            *channel /= white_level.max(0.01);
        }
    }
    
    // Blacks adjustment
    if blacks.abs() > 0.00001 {
        let luma = get_luma(colors).max(0.0);
        let mask = 1.0 - smoothstep(0.0, 0.25, luma);
        if mask > 0.001 {
            let adjustment = blacks * 0.75;
            let factor = 2f32.powf(adjustment);
            for channel in colors.iter_mut() {
                let adjusted = *channel * factor;
                *channel = *channel + (adjusted - *channel) * mask;
            }
        }
    }
    
    // Shadows adjustment
    if shadows.abs() > 0.00001 {
        let luma = get_luma(colors).max(0.0);
        let mask = (1.0 - smoothstep(0.0, 0.4, luma)).powf(3.0);
        if mask > 0.001 {
            let adjustment = shadows * 1.5;
            let factor = 2f32.powf(adjustment);
            for channel in colors.iter_mut() {
                let adjusted = *channel * factor;
                *channel = *channel + (adjusted - *channel) * mask;
            }
        }
    }
    
    // Contrast adjustment
    if contrast.abs() > 0.00001 {
        const GAMMA: f32 = 2.2;
        let safe_rgb = [colors[0].max(0.0), colors[1].max(0.0), colors[2].max(0.0)];
        let mut perceptual = [
            safe_rgb[0].powf(1.0 / GAMMA),
            safe_rgb[1].powf(1.0 / GAMMA),
            safe_rgb[2].powf(1.0 / GAMMA),
        ];
        
        for p in perceptual.iter_mut() {
            *p = p.clamp(0.0, 1.0);
        }
        
        let strength = 2f32.powf(contrast * 1.25);
        for i in 0..3 {
            if perceptual[i] < 0.5 {
                perceptual[i] = 0.5 * (2.0 * perceptual[i]).powf(strength);
            } else {
                perceptual[i] = 1.0 - 0.5 * (2.0 * (1.0 - perceptual[i])).powf(strength);
            }
        }
        
        let contrast_adjusted = [
            perceptual[0].powf(GAMMA),
            perceptual[1].powf(GAMMA),
            perceptual[2].powf(GAMMA),
        ];
        
        // Mix based on whether we're over 1.0
        for i in 0..3 {
            let mix_factor = smoothstep(1.0, 1.01, safe_rgb[i]);
            colors[i] = contrast_adjusted[i] + (colors[i] - contrast_adjusted[i]) * mix_factor;
        }
    }
    
    colors
}

fn apply_highlights_adjustment(mut colors: [f32; 3], highlights: f32) -> [f32; 3] {
    if highlights.abs() < 0.00001 {
        return colors;
    }
    
    let luma = get_luma(colors).max(0.0);
    let mask_input = (luma * 1.5).tanh();
    let highlight_mask = smoothstep(0.3, 0.95, mask_input);
    
    if highlight_mask < 0.001 {
        return colors;
    }
    
    if highlights < 0.0 {
        let new_luma = if luma <= 1.0 {
            let gamma = 1.0 - highlights * 1.75;
            luma.powf(gamma)
        } else {
            let luma_excess = luma - 1.0;
            let compression_strength = -highlights * 6.0;
            let compressed_excess = luma_excess / (1.0 + luma_excess * compression_strength);
            1.0 + compressed_excess
        };
        
        let tonally_adjusted = [
            colors[0] * (new_luma / luma.max(0.0001)),
            colors[1] * (new_luma / luma.max(0.0001)),
            colors[2] * (new_luma / luma.max(0.0001)),
        ];
        
        let desaturation_amount = smoothstep(1.0, 10.0, luma);
        let white_point = [new_luma, new_luma, new_luma];
        
        for i in 0..3 {
            let final_adjusted = tonally_adjusted[i] + (white_point[i] - tonally_adjusted[i]) * desaturation_amount;
            colors[i] = colors[i] + (final_adjusted - colors[i]) * highlight_mask;
        }
    } else {
        let adjustment = highlights * 1.75;
        let factor = 2f32.powf(adjustment);
        for i in 0..3 {
            let final_adjusted = colors[i] * factor;
            colors[i] = colors[i] + (final_adjusted - colors[i]) * highlight_mask;
        }
    }
    
    colors
}

fn apply_color_adjustments(mut colors: [f32; 3], settings: &AdjustmentValues) -> [f32; 3] {
    // Temperature and Tint adjustment (applied early like RapidRAW)
    // RapidRAW: temp_kelvin_mult = vec3(1.0 + temp * 0.2, 1.0 + temp * 0.05, 1.0 - temp * 0.2)
    // RapidRAW: tint_mult = vec3(1.0 + tnt * 0.25, 1.0 - tnt * 0.25, 1.0 + tnt * 0.25)
    let temp_kelvin_mult = [
        1.0 + settings.temperature * 0.2,
        1.0 + settings.temperature * 0.05,
        1.0 - settings.temperature * 0.2,
    ];
    let tint_mult = [
        1.0 + settings.tint * 0.25,
        1.0 - settings.tint * 0.25,
        1.0 + settings.tint * 0.25,
    ];
    colors[0] *= temp_kelvin_mult[0] * tint_mult[0];
    colors[1] *= temp_kelvin_mult[1] * tint_mult[1];
    colors[2] *= temp_kelvin_mult[2] * tint_mult[2];
    
    // Brightness (filmic curve)
    colors = apply_filmic_brightness(colors, settings.brightness);
    
    // Tonal adjustments (contrast, shadows, whites, blacks)
    colors = apply_tonal_adjustments(colors, settings.contrast, settings.shadows, settings.whites, settings.blacks);
    
    // Highlights
    colors = apply_highlights_adjustment(colors, settings.highlights);
    
    let luma = get_luma(colors);
    
    // Saturation adjustment - RapidRAW: mix(vec3(luma), processed, 1.0 + sat)
    if settings.saturation != 0.0 {
        let sat_mix = 1.0 + settings.saturation;
        colors[0] = luma + (colors[0] - luma) * sat_mix;
        colors[1] = luma + (colors[1] - luma) * sat_mix;
        colors[2] = luma + (colors[2] - luma) * sat_mix;
    }

    // Vibrance adjustment - RapidRAW implementation with skin tone protection
    if settings.vibrance != 0.0 {
        let c_max = colors[0].max(colors[1]).max(colors[2]);
        let c_min = colors[0].min(colors[1]).min(colors[2]);
        let delta = c_max - c_min;
        
        if delta >= 0.02 {
            let current_sat = delta / c_max.max(0.001);
            
            if settings.vibrance > 0.0 {
                // Positive vibrance: affect low saturation colors more, protect skin tones
                let sat_mask = 1.0 - smoothstep(0.4, 0.9, current_sat);
                
                // Skin tone protection (hue around 25 degrees)
                let hue = rgb_to_hue(colors);
                let skin_center = 25.0;
                let hue_dist = (hue - skin_center).abs().min(360.0 - (hue - skin_center).abs());
                let is_skin = smoothstep(35.0, 10.0, hue_dist);
                let skin_dampener = 1.0 - is_skin * 0.4; // mix(1.0, 0.6, is_skin)
                
                let amount = settings.vibrance * sat_mask * skin_dampener * 3.0;
                let vib_mix = 1.0 + amount;
                colors[0] = luma + (colors[0] - luma) * vib_mix;
                colors[1] = luma + (colors[1] - luma) * vib_mix;
                colors[2] = luma + (colors[2] - luma) * vib_mix;
            } else {
                // Negative vibrance: desaturate already saturated colors
                let desat_mask = 1.0 - smoothstep(0.2, 0.8, current_sat);
                let amount = settings.vibrance * desat_mask;
                let vib_mix = 1.0 + amount;
                colors[0] = luma + (colors[0] - luma) * vib_mix;
                colors[1] = luma + (colors[1] - luma) * vib_mix;
                colors[2] = luma + (colors[2] - luma) * vib_mix;
            }
        }
    }

    // Clarity, Dehaze, Structure, and Centré adjustments
    let clarity_gain = settings.clarity / 100.0 * 0.15;
    let dehaze_gain = settings.dehaze / 500.0;
    let structure_gain = settings.structure / 200.0 * 0.1;
    let centre_gain = settings.centre / 200.0 * 0.08;
    let detail_mask = ((luma - 0.5) * 2.0).clamp(-1.0, 1.0);
    
    for channel in colors.iter_mut() {
        *channel += clarity_gain * detail_mask;
        *channel += dehaze_gain * (luma - 0.5);
        *channel += structure_gain * detail_mask;
        *channel += centre_gain * detail_mask.abs();
    }

    // Sharpness (basic local contrast enhancement)
    let sharpness_gain = settings.sharpness / 100.0 * 0.12;
    for channel in colors.iter_mut() {
        *channel += sharpness_gain * detail_mask;
    }
    
    // Clamp final values
    for channel in colors.iter_mut() {
        *channel = channel.clamp(0.0, 1.0);
    }

    colors
}

fn linear_to_srgb(linear: f32) -> f32 {
    let v = linear.max(0.0);
    if v <= 0.0031308 {
        12.92 * v
    } else {
        1.055 * v.powf(1.0 / 2.4) - 0.055
    }
}

fn srgb_to_linear(srgb: f32) -> f32 {
    let v = srgb.max(0.0);
    if v <= 0.04045 {
        v / 12.92
    } else {
        ((v + 0.055) / 1.055).powf(2.4)
    }
}

fn apply_default_raw_processing(colors: [f32; 3], use_basic_tone_mapper: bool) -> [f32; 3] {
    if !use_basic_tone_mapper {
        return colors; // AgX doesn't need default processing
    }
    
    // RapidRAW applies default brightness and contrast to RAW images
    // when using Basic tone mapper
    const BRIGHTNESS_GAMMA: f32 = 1.1;
    const CONTRAST_MIX: f32 = 0.75;
    
    // Convert to sRGB
    let srgb = [
        linear_to_srgb(colors[0]),
        linear_to_srgb(colors[1]),
        linear_to_srgb(colors[2]),
    ];
    
    // Apply brightness gamma
    let brightened = [
        srgb[0].powf(1.0 / BRIGHTNESS_GAMMA),
        srgb[1].powf(1.0 / BRIGHTNESS_GAMMA),
        srgb[2].powf(1.0 / BRIGHTNESS_GAMMA),
    ];
    
    // Apply contrast S-curve
    let contrast_curve = [
        brightened[0] * brightened[0] * (3.0 - 2.0 * brightened[0]),
        brightened[1] * brightened[1] * (3.0 - 2.0 * brightened[1]),
        brightened[2] * brightened[2] * (3.0 - 2.0 * brightened[2]),
    ];
    
    // Mix in the contrast
    let contrasted = [
        brightened[0] + (contrast_curve[0] - brightened[0]) * CONTRAST_MIX,
        brightened[1] + (contrast_curve[1] - brightened[1]) * CONTRAST_MIX,
        brightened[2] + (contrast_curve[2] - brightened[2]) * CONTRAST_MIX,
    ];
    
    // Convert back to linear
    [
        srgb_to_linear(contrasted[0]),
        srgb_to_linear(contrasted[1]),
        srgb_to_linear(contrasted[2]),
    ]
}

fn render_raw(
    raw_bytes: &[u8],
    adjustments_json: Option<&str>,
    fast_demosaic: bool,
    max_width: Option<u32>,
    max_height: Option<u32>,
) -> Result<Vec<u8>> {
    let highlight_compression = 2.5;  // RapidRAW default
    let dynamic_image = develop_raw_image(raw_bytes, fast_demosaic, highlight_compression)
        .context("Failed to decode RAW image")?;
    // If a maximum size was requested, downscale BEFORE the per-pixel adjustments
    // to avoid performing expensive color math at full sensor resolution.
    let mut dynamic_image = dynamic_image;
    if let (Some(max_w), Some(max_h)) = (max_width, max_height) {
        if dynamic_image.width() > max_w || dynamic_image.height() > max_h {
            // Fit within the requested box while preserving aspect ratio.
            let w = dynamic_image.width() as f32;
            let h = dynamic_image.height() as f32;
            let scale = (max_w as f32 / w).min(max_h as f32 / h);
            let target_w = ((w * scale).max(1.0)) as u32;
            let target_h = ((h * scale).max(1.0)) as u32;
            // Use a faster filter for preview downscale (Triangle is a good quality/speed tradeoff).
            dynamic_image = dynamic_image.resize(target_w, target_h, FilterType::Triangle);
        }
    }

    let parsed_adjustments = parse_adjustments(adjustments_json);
    let adjustment_values = parsed_adjustments.normalized(&ADJUSTMENT_SCALES);

    let use_basic_tone_mapper = matches!(adjustment_values.tone_mapper, ToneMapper::Basic);

    let linear_buffer = dynamic_image.to_rgba32f();
    let width = linear_buffer.width();
    let height = linear_buffer.height();

    let result_buffer: ImageBuffer<Rgba<u8>, _> = ImageBuffer::from_fn(width, height, |x, y| {
        let pixel = linear_buffer.get_pixel(x, y);
        
        // Start with linear RAW pixel data
        let mut colors = [pixel[0], pixel[1], pixel[2]];
        
        // Apply default RAW processing (brightness + contrast boost for Basic tone mapper)
        colors = apply_default_raw_processing(colors, use_basic_tone_mapper);
        
        let adjusted = apply_color_adjustments(colors, &adjustment_values);
        Rgba([
            clamp_to_u8(linear_to_srgb(adjusted[0]) * 255.0),
            clamp_to_u8(linear_to_srgb(adjusted[1]) * 255.0),
            clamp_to_u8(linear_to_srgb(adjusted[2]) * 255.0),
            255,
        ])
    });

    let mut final_image = DynamicImage::ImageRgba8(result_buffer);
    // We already downscaled earlier for previews; apply final resize only if caller still requested
    // a different size than what we've already produced.
    if let (Some(max_w), Some(max_h)) = (max_width, max_height) {
        if final_image.width() > max_w || final_image.height() > max_h {
            final_image = final_image.resize(max_w, max_h, FilterType::Lanczos3);
        }
    }

    let mut encoded = Vec::new();
    let quality = if fast_demosaic { 88 } else { 96 };
    let mut encoder = JpegEncoder::new_with_quality(&mut encoded, quality);
    encoder.encode_image(&final_image)?;
    Ok(encoded)
}

static LOGGER_INIT: Once = Once::new();

fn ensure_logger() {
    LOGGER_INIT.call_once(|| {
        #[cfg(target_os = "android")]
        {
            android_logger::init_once(
                android_logger::Config::default()
                    .with_min_level(Level::Info)
                    .with_tag("kawaiiraweditor"),
            );
        }
    });
}

fn convert_raw_array(env: &JNIEnv, raw_array: JByteArray) -> Option<Vec<u8>> {
    match env.convert_byte_array(raw_array) {
        Ok(bytes) => Some(bytes),
        Err(err) => {
            error!("Failed to read raw byte array: {}", err);
            None
        }
    }
}

fn make_byte_array(env: &JNIEnv, bytes: &[u8]) -> jbyteArray {
    match env.byte_array_from_slice(bytes) {
        Ok(array) => array.into_raw() as jbyteArray,
        Err(err) => {
            error!("Failed to build byte array: {}", err);
            ptr::null_mut()
        }
    }
}

fn read_adjustments_json(env: &mut JNIEnv, adjustments: JString) -> Option<String> {
    match env.get_string(&adjustments) {
        Ok(js) => {
            let trimmed = js.to_string_lossy().trim().to_string();
            if trimmed.is_empty() {
                None
            } else {
                Some(trimmed)
            }
        }
        Err(err) => {
            error!("Failed to read adjustments JSON: {}", err);
            None
        }
    }
}

#[no_mangle]
pub extern "system" fn Java_com_dueckis_kawaiiraweditor_LibRawDecoder_lowdecode(
    mut env: JNIEnv,
    _: JClass,
    raw_data: JByteArray,
    adjustments_json: JString,
) -> jbyteArray {
    ensure_logger();
    let bytes = match convert_raw_array(&env, raw_data) {
        Some(b) => b,
        None => return ptr::null_mut(),
    };

    let adjustments = read_adjustments_json(&mut env, adjustments_json);

    // Request a small fast preview (300x300) for interactive slider updates.
    match render_raw(&bytes, adjustments.as_deref(), true, Some(300), Some(300)) {
        Ok(payload) => make_byte_array(&env, &payload),
        Err(err) => {
            error!("Failed to render preview: {}", err);
            ptr::null_mut()
        }
    }
}


#[no_mangle]
pub extern "system" fn Java_com_dueckis_kawaiiraweditor_LibRawDecoder_lowlowdecode(
    mut env: JNIEnv,
    _: JClass,
    raw_data: JByteArray,
    adjustments_json: JString,
) -> jbyteArray {
    ensure_logger();
    let bytes = match convert_raw_array(&env, raw_data) {
        Some(b) => b,
        None => return ptr::null_mut(),
    };

    let adjustments = read_adjustments_json(&mut env, adjustments_json);

    // Request a small fast preview (300x300) for interactive slider updates.
    match render_raw(&bytes, adjustments.as_deref(), true, Some(100), Some(100)) {
        Ok(payload) => make_byte_array(&env, &payload),
        Err(err) => {
            error!("Failed to render preview: {}", err);
            ptr::null_mut()
        }
    }
}

#[no_mangle]
pub extern "system" fn Java_com_dueckis_kawaiiraweditor_LibRawDecoder_decode(
    mut env: JNIEnv,
    _: JClass,
    raw_data: JByteArray,
    adjustments_json: JString,
) -> jbyteArray {
    ensure_logger();
    let bytes = match convert_raw_array(&env, raw_data) {
        Some(b) => b,
        None => return ptr::null_mut(),
    };

    let adjustments = read_adjustments_json(&mut env, adjustments_json);

    // Request a small fast preview (300x300) for interactive slider updates.
    match render_raw(&bytes, adjustments.as_deref(), true, Some(1920), Some(1080)) {
        Ok(payload) => make_byte_array(&env, &payload),
        Err(err) => {
            error!("Failed to render preview: {}", err);
            ptr::null_mut()
        }
    }
}

#[no_mangle]
pub extern "system" fn Java_com_dueckis_kawaiiraweditor_LibRawDecoder_decodeFullRes(
    mut env: JNIEnv,
    _: JClass,
    raw_data: JByteArray,
    adjustments_json: JString,
) -> jbyteArray {
    ensure_logger();
    let bytes = match convert_raw_array(&env, raw_data) {
        Some(b) => b,
        None => return ptr::null_mut(),
    };

    let adjustments = read_adjustments_json(&mut env, adjustments_json);

    match render_raw(&bytes, adjustments.as_deref(), false, None, None) {
        Ok(payload) => make_byte_array(&env, &payload),
        Err(err) => {
            error!("Failed to render full-resolution image: {}", err);
            ptr::null_mut()
        }
    }
}
