package ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import data.Note
import data.NoteRepository
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast

@Composable
fun HomeScreen(
    onAddNote: () -> Unit,
    onEditNote: (String) -> Unit
) {
    val repository = NoteRepository()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var notes by remember { mutableStateOf<List<Note>>(emptyList()) }
    var errorMessage by remember { mutableStateOf("") }

    fun loadNotes() {
        coroutineScope.launch {
            try {
                val result = repository.getNotes()
                if (result.isSuccess) {
                    notes = result.getOrNull() ?: emptyList()
                } else {
                    errorMessage = result.exceptionOrNull()?.message ?: "Failed to load notes"
                }
            } catch (e: Exception) {
                Log.e("HomeScreen", "Error loading notes: ${e.message}", e)
                errorMessage = "Error loading notes: ${e.message}"
            }
        }
    }

    LaunchedEffect(Unit) {
        loadNotes()
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                Log.d("HomeScreen", "Add Note button clicked")
                try {
                    onAddNote()
                } catch (e: Exception) {
                    Log.e("HomeScreen", "Error navigating to AddEditNoteScreen: ${e.message}", e)
                    errorMessage = "Failed to open add note screen: ${e.message}"
                }
            }) {
                Text("+")
            }
        }
    ) { padding ->
        LazyColumn(
            contentPadding = padding,
            modifier = Modifier.fillMaxSize()
        ) {
            items(notes) { note ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(note.title, style = MaterialTheme.typography.titleMedium)
                            Text(note.content)
                            note.imageUrl?.let { url ->
                                AsyncImage(
                                    model = url,
                                    contentDescription = "Note Image",
                                    modifier = Modifier.size(100.dp)
                                )
                            }
                        }
                        Column {
                            IconButton(onClick = { onEditNote(note.id) }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Note")
                            }
                            IconButton(onClick = {
                                coroutineScope.launch {
                                    val result = repository.deleteNote(note.id)
                                    if (result.isSuccess) {
                                        loadNotes()
                                    } else {
                                        errorMessage = "Failed to delete note: ${result.exceptionOrNull()?.message}"
                                    }
                                }
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Note")
                            }
                            note.imageUrl?.let { url ->
                                IconButton(onClick = {
                                    coroutineScope.launch {
                                        val result = repository.downloadImage(url, context)
                                        if (result.isSuccess) {
                                            Log.d("HomeScreen", "Image downloaded: ${result.getOrNull()}")
                                            Toast.makeText(context, "Image downloaded to Downloads", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Log.e("HomeScreen", "Failed to download image: ${result.exceptionOrNull()?.message}")
                                            errorMessage = "Failed to download image: ${result.exceptionOrNull()?.message}"
                                        }
                                    }
                                }) {
                                    Icon(Icons.Default.Download, contentDescription = "Download Image")
                                }
                            }
                        }
                    }
                }
            }
        }
        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(errorMessage, color = MaterialTheme.colorScheme.error)
        }
    }
}