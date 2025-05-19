package ui.screens

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import data.Note
import data.NoteRepository
import kotlinx.coroutines.launch

@Composable
fun AddEditNoteScreen(
    noteId: String?,
    onNoteSaved: () -> Unit
) {
    val repository = NoteRepository()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var existingImageUrl by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(noteId) {
        if (noteId != null) {
            Log.d("AddEditNoteScreen", "Loading note with ID: $noteId")
            val result = repository.getNotes()
            if (result.isSuccess) {
                val note = result.getOrNull()?.find { it.id == noteId }
                if (note != null) {
                    title = note.title
                    content = note.content
                    existingImageUrl = note.imageUrl
                    Log.d("AddEditNoteScreen", "Note loaded: $note")
                } else {
                    errorMessage = "Note not found"
                }
            } else {
                errorMessage = "Failed to load note: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        imageUri = uri
        Log.d("AddEditNoteScreen", "Image picked: $uri")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = content,
            onValueChange = { content = it },
            label = { Text("Content") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        existingImageUrl?.let { url ->
            AsyncImage(
                model = url,
                contentDescription = "Existing Note Image",
                modifier = Modifier.size(100.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        Button(
            onClick = { launcher.launch("image/*") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Pick Image")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                if (title.isBlank() || content.isBlank()) {
                    errorMessage = "Title and Content cannot be empty"
                    return@Button
                }
                Log.d("AddEditNoteScreen", "Save Note clicked. Title: $title, Content: $content, ImageUri: $imageUri")
                coroutineScope.launch {
                    try {
                        val imageUrl = imageUri?.let {
                            Log.d("AddEditNoteScreen", "Uploading image...")
                            val result = repository.uploadImage(it, context)
                            result.getOrNull().also {
                                if (result.isFailure) {
                                    Log.e("AddEditNoteScreen", "Image upload failed: ${result.exceptionOrNull()?.message}")
                                }
                            }
                        } ?: existingImageUrl
                        Log.d("AddEditNoteScreen", "Image URL: $imageUrl")
                        val note = Note(
                            id = noteId ?: "",
                            title = title,
                            content = content,
                            imageUrl = imageUrl
                        )
                        Log.d("AddEditNoteScreen", "Saving note: $note")
                        val result = repository.saveNote(note)
                        if (result.isSuccess) {
                            Log.d("AddEditNoteScreen", "Note saved successfully")
                            onNoteSaved()
                        } else {
                            val error = result.exceptionOrNull()?.message ?: "Failed to save note"
                            Log.e("AddEditNoteScreen", "Failed to save note: $error")
                            errorMessage = error
                        }
                    } catch (e: Exception) {
                        Log.e("AddEditNoteScreen", "Exception while saving note: ${e.message}", e)
                        errorMessage = "An error occurred: ${e.message}"
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = title.isNotBlank() && content.isNotBlank()
        ) {
            Text(if (noteId != null) "Update Note" else "Save Note")
        }
        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(errorMessage, color = MaterialTheme.colorScheme.error)
        }
    }
}