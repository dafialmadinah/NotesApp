package ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import data.Note
import data.NoteRepository
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    onAddNote: () -> Unit,
    onEditNote: (String) -> Unit
) {
    val repository = NoteRepository()
    val coroutineScope = rememberCoroutineScope()
    var notes by remember { mutableStateOf<List<Note>>(emptyList()) }
    var errorMessage by remember { mutableStateOf("") }

    fun loadNotes() {
        coroutineScope.launch {
            val result = repository.getNotes()
            if (result.isSuccess) {
                notes = result.getOrNull() ?: emptyList()
            } else {
                errorMessage = result.exceptionOrNull()?.message ?: "Failed to load notes"
            }
        }
    }

    LaunchedEffect(Unit) {
        loadNotes()
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAddNote) {
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


