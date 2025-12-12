use std::sync::Arc;
use std::sync::Mutex;

use anyhow::{anyhow, Result};
use image::{DynamicImage, GrayImage};
use serde::{Deserialize, Serialize};
use tokio::sync::Mutex as TokioMutex;

#[derive(Default)]
pub struct AiModels {
    pub sam_encoder: (),
    pub sam_decoder: (),
    pub u2netp: (),
    pub sky_seg: (),
    pub clip_model: Option<()>,
    pub clip_tokenizer: Option<()>,
}

#[derive(Clone)]
pub struct ImageEmbeddings {
    pub path_hash: String,
    pub original_size: (u32, u32),
}

pub struct AiState {
    pub models: Arc<AiModels>,
    pub embeddings: Option<ImageEmbeddings>,
}

#[derive(Serialize, Deserialize, Debug, Clone, Default)]
#[serde(rename_all = "camelCase")]
pub struct AiSubjectMaskParameters {
    pub start_x: f64,
    pub start_y: f64,
    pub end_x: f64,
    pub end_y: f64,
    #[serde(default)]
    pub mask_data_base64: Option<String>,
    #[serde(default)]
    pub rotation: Option<f32>,
    #[serde(default)]
    pub flip_horizontal: Option<bool>,
    #[serde(default)]
    pub flip_vertical: Option<bool>,
    #[serde(default)]
    pub orientation_steps: Option<u8>,
}

#[derive(Serialize, Deserialize, Debug, Clone, Default)]
#[serde(rename_all = "camelCase")]
pub struct AiSkyMaskParameters {
    #[serde(default)]
    pub mask_data_base64: Option<String>,
    #[serde(default)]
    pub rotation: Option<f32>,
    #[serde(default)]
    pub flip_horizontal: Option<bool>,
    #[serde(default)]
    pub flip_vertical: Option<bool>,
    #[serde(default)]
    pub orientation_steps: Option<u8>,
}

#[derive(Serialize, Deserialize, Debug, Clone, Default)]
#[serde(rename_all = "camelCase")]
pub struct AiForegroundMaskParameters {
    #[serde(default)]
    pub mask_data_base64: Option<String>,
    #[serde(default)]
    pub rotation: Option<f32>,
    #[serde(default)]
    pub flip_horizontal: Option<bool>,
    #[serde(default)]
    pub flip_vertical: Option<bool>,
    #[serde(default)]
    pub orientation_steps: Option<u8>,
}

fn unsupported() -> Result<Arc<AiModels>> {
    Err(anyhow!("AI processing is not supported on Android builds"))
}

pub async fn get_or_init_ai_models(
    _app_handle: &tauri::AppHandle,
    _ai_state_mutex: &Mutex<Option<AiState>>,
    _ai_init_lock: &TokioMutex<()>,
) -> Result<Arc<AiModels>> {
    unsupported()
}

pub fn generate_image_embeddings(
    _image: &DynamicImage,
    _sam_encoder: &(),
) -> Result<ImageEmbeddings> {
    Err(anyhow!("AI processing is not supported on Android builds"))
}

pub fn run_sam_decoder(
    _sam_decoder: &(),
    _embeddings: &ImageEmbeddings,
    _start_point: (f64, f64),
    _end_point: (f64, f64),
) -> Result<GrayImage> {
    Err(anyhow!("AI processing is not supported on Android builds"))
}

pub fn run_sky_seg_model(
    _image: &DynamicImage,
    _sky_seg_session: &(),
) -> Result<GrayImage> {
    Err(anyhow!("AI processing is not supported on Android builds"))
}

pub fn run_u2netp_model(
    _image: &DynamicImage,
    _u2netp_session: &(),
) -> Result<GrayImage> {
    Err(anyhow!("AI processing is not supported on Android builds"))
}
