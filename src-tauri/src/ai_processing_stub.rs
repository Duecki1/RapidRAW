use std::sync::{Arc, Mutex};

use anyhow::Result;
use image::{DynamicImage, GrayImage};
use serde::{Deserialize, Serialize};
use tokio::sync::Mutex as TokioMutex;

#[derive(Clone, Default)]
pub struct Session;

pub struct AiModels {
    pub sam_encoder: Mutex<Session>,
    pub sam_decoder: Mutex<Session>,
    pub u2netp: Mutex<Session>,
    pub sky_seg: Mutex<Session>,
    pub clip_model: Option<Mutex<Session>>,
    pub clip_tokenizer: Option<()>,
}

impl Clone for AiModels {
    fn clone(&self) -> Self {
        Self {
            sam_encoder: Mutex::new(Session::default()),
            sam_decoder: Mutex::new(Session::default()),
            u2netp: Mutex::new(Session::default()),
            sky_seg: Mutex::new(Session::default()),
            clip_model: None,
            clip_tokenizer: None,
        }
    }
}

#[derive(Clone)]
pub struct ImageEmbeddings {
    pub path_hash: String,
    pub embeddings: (),
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

pub async fn get_or_init_ai_models(
    _app_handle: &tauri::AppHandle,
    _ai_state_mutex: &Mutex<Option<AiState>>,
    _ai_init_lock: &TokioMutex<()>,
) -> Result<Arc<AiModels>> {
    Err(anyhow::anyhow!(
        "AI models are not available on Android yet."
    ))
}

pub fn generate_image_embeddings(
    _image: &DynamicImage,
    _encoder: &Mutex<Session>,
) -> Result<ImageEmbeddings> {
    Err(anyhow::anyhow!(
        "AI embeddings are not available on Android yet."
    ))
}

pub fn run_sam_decoder(
    _decoder: &Mutex<Session>,
    _embeddings: &ImageEmbeddings,
    _start_point: (f64, f64),
    _end_point: (f64, f64),
) -> Result<GrayImage> {
    Err(anyhow::anyhow!(
        "AI subject masking is not available on Android yet."
    ))
}

pub fn run_sky_seg_model(
    _image: &DynamicImage,
    _sky_seg_session: &Mutex<Session>,
) -> Result<GrayImage> {
    Err(anyhow::anyhow!(
        "AI sky masking is not available on Android yet."
    ))
}

pub fn run_u2netp_model(
    _image: &DynamicImage,
    _u2netp_session: &Mutex<Session>,
) -> Result<GrayImage> {
    Err(anyhow::anyhow!(
        "AI foreground masking is not available on Android yet."
    ))
}
