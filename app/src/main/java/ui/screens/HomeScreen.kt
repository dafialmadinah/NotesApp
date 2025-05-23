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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import data.Note
import data.NoteRepository
import kotlinx.coroutines.launch
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

    HomeScreenContent(
        notes = notes,
        errorMessage = errorMessage,
        onAddNote = onAddNote,
        onEditNote = onEditNote,
        onDeleteNote = { noteId ->
            coroutineScope.launch {
                val result = repository.deleteNote(noteId)
                if (result.isSuccess) {
                    loadNotes()
                } else {
                    errorMessage = "Failed to delete note: ${result.exceptionOrNull()?.message}"
                }
            }
        },
        onDownloadImage = { url ->
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
        }
    )
}

@Composable
private fun HomeScreenContent(
    notes: List<Note>,
    errorMessage: String,
    onAddNote: () -> Unit,
    onEditNote: (String) -> Unit,
    onDeleteNote: (String) -> Unit,
    onDownloadImage: (String) -> Unit
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                Log.d("HomeScreen", "Add Note button clicked")
                try {
                    onAddNote()
                } catch (e: Exception) {
                    Log.e("HomeScreen", "Error navigating to AddEditNoteScreen: ${e.message}", e)
                }
            }) {
                Text("+")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(notes) { note ->
                    NoteCard(
                        note = note,
                        onEditNote = onEditNote,
                        onDeleteNote = onDeleteNote,
                        onDownloadImage = onDownloadImage
                    )
                }
            }

            if (errorMessage.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun NoteCard(
    note: Note,
    onEditNote: (String) -> Unit,
    onDeleteNote: (String) -> Unit,
    onDownloadImage: (String) -> Unit
) {
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
                IconButton(onClick = { onDeleteNote(note.id) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Note")
                }
                note.imageUrl?.let { url ->
                    IconButton(onClick = { onDownloadImage(url) }) {
                        Icon(Icons.Default.Download, contentDescription = "Download Image")
                    }
                }
            }
        }
    }
}

// Preview functions
@Preview(showBackground = true, name = "Empty Notes List")
@Composable
private fun HomeScreenEmptyPreview() {
    MaterialTheme {
        HomeScreenContent(
            notes = emptyList(),
            errorMessage = "",
            onAddNote = {},
            onEditNote = {},
            onDeleteNote = {},
            onDownloadImage = {}
        )
    }
}

@Preview(showBackground = true, name = "Notes with Content")
@Composable
private fun HomeScreenWithNotesPreview() {
    val sampleNotes = listOf(
        Note(
            id = "1",
            title = "Sample Note 1",
            content = "This is a sample note content that shows how the note will look in the list.",
            imageUrl = null
        ),
        Note(
            id = "2",
            title = "Note with Image",
            content = "This note has an image attached to demonstrate the image display functionality.",
            imageUrl = "https://example.com/sample-image.jpg"
        ),
        Note(
            id = "3",
            title = "Another Note",
            content = "More sample content to show multiple notes in the list.",
            imageUrl = null
        )
    )

    MaterialTheme {
        HomeScreenContent(
            notes = sampleNotes,
            errorMessage = "",
            onAddNote = {},
            onEditNote = {},
            onDeleteNote = {},
            onDownloadImage = {}
        )
    }
}

@Preview(showBackground = true, name = "Error State")
@Composable
private fun HomeScreenErrorPreview() {
    val sampleNotes = listOf(
        Note(
            id = "1",
            title = "Sample Note",
            content = "This shows how error messages appear",
            imageUrl = null
        )
    )

    MaterialTheme {
        HomeScreenContent(
            notes = sampleNotes,
            errorMessage = "Failed to load notes from server. Please check your internet connection.",
            onAddNote = {},
            onEditNote = {},
            onDeleteNote = {},
            onDownloadImage = {}
        )
    }
}

@Preview(showBackground = true, name = "Single Note Card")
@Composable
private fun NoteCardPreview() {
    val sampleNote = Note(
        id = "1",
        title = "Sample Note Title",
        content = "This is a sample note content that demonstrates how individual note cards will appear in the application.",
        imageUrl = "https://example.com/sample-image.jpg"
    )

    MaterialTheme {
        NoteCard(
            note = sampleNote,
            onEditNote = {},
            onDeleteNote = {},
            onDownloadImage = {}
        )
    }
}