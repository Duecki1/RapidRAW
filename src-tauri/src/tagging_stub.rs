use std::collections::HashMap;

use serde_json::Value;
use tauri::{AppHandle, State};

use crate::AppState;

pub const COLOR_TAG_PREFIX: &str = "color:";
pub const USER_TAG_PREFIX: &str = "user:";

#[tauri::command]
pub async fn start_background_indexing(
    _folder_path: String,
    _app_handle: AppHandle,
    _state: State<'_, AppState>,
) -> Result<(), String> {
    Err("AI tagging is not available on Android builds yet.".to_string())
}

#[tauri::command]
pub fn clear_ai_tags(_state: State<AppState>) -> Result<(), String> {
    Err("AI tagging is not available on Android builds yet.".to_string())
}

#[tauri::command]
pub fn clear_all_tags(_state: State<AppState>) -> Result<(), String> {
    Err("AI tagging is not available on Android builds yet.".to_string())
}

#[tauri::command]
pub fn add_tag_for_paths(
    _paths: Vec<String>,
    _tags: Vec<String>,
    _state: State<AppState>,
) -> Result<(), String> {
    Ok(())
}

#[tauri::command]
pub fn remove_tag_for_paths(
    _paths: Vec<String>,
    _tags: Vec<String>,
    _state: State<AppState>,
) -> Result<(), String> {
    Ok(())
}

pub fn extract_color_tags(_image: &image::DynamicImage) -> Vec<String> {
    Vec::new()
}

pub fn add_or_remove_tags_for_path(
    _path: &str,
    _add_tags: &[String],
    _remove_tags: &[String],
    _state: &State<AppState>,
) -> Result<(), String> {
    Ok(())
}

pub fn build_tag_counts(
    _paths: &[String],
    _state: &State<AppState>,
) -> Result<HashMap<String, usize>, String> {
    Ok(HashMap::new())
}

pub fn get_tags_for_path(_path: &str, _state: &State<AppState>) -> Result<Vec<String>, String> {
    Ok(Vec::new())
}

pub fn save_tag_cache(_state: &State<AppState>) -> Result<(), String> {
    Ok(())
}

pub fn refresh_tag_hierarchy_cache(
    _state: &State<AppState>,
    _app_handle: &AppHandle,
) -> Result<(), String> {
    Ok(())
}

pub fn load_ai_tagging_models(
    _app_handle: &AppHandle,
    _state: &State<AppState>,
) -> Result<(), String> {
    Ok(())
}

pub fn process_image_with_models(
    _image: &image::DynamicImage,
    _models: &crate::ai_processing::AiModels,
) -> Result<Vec<String>, String> {
    Ok(Vec::new())
}

pub fn extract_tags_from_metadata(_metadata: &Value) -> Vec<String> {
    Vec::new()
}
