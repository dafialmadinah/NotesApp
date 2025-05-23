package data

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.util.UUID

interface ImageApi {
    @Multipart
    @POST("upload.php")
    suspend fun uploadImage(@Part image: MultipartBody.Part): ImageUploadResponse
}

data class ImageUploadResponse(
    val success: Boolean,
    val imageUrl: String?,
    val error: String?
)

class NoteRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance().reference.child("notes")
    private val imageApi: ImageApi

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.110.105/notesapp/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        imageApi = retrofit.create(ImageApi::class.java)
    }

    suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            Log.d("NoteRepository", "Attempting login for $email")
            auth.signInWithEmailAndPassword(email, password).await()
            Log.d("NoteRepository", "Login successful for $email")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("NoteRepository", "Login failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun register(email: String, password: String): Result<Unit> {
        return try {
            Log.d("NoteRepository", "Attempting register for $email")
            auth.createUserWithEmailAndPassword(email, password).await()
            Log.d("NoteRepository", "Register successful for $email")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("NoteRepository", "Register failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    fun getCurrentUserId(): String? = auth.currentUser?.uid.also {
        Log.d("NoteRepository", "Current user ID: $it")
    }

    suspend fun saveNote(note: Note): Result<Unit> {
        return try {
            Log.d("NoteRepository", "Saving note: $note")
            val noteId = note.id.ifEmpty { UUID.randomUUID().toString() }
            val userId = getCurrentUserId() ?: throw IllegalStateException("User not logged in")
            val noteToSave = note.copy(id = noteId, userId = userId)
            db.child(userId).child(noteId).setValue(noteToSave).await()
            Log.d("NoteRepository", "Note saved successfully with ID: $noteId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("NoteRepository", "Failed to save note: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun getNotes(): Result<List<Note>> {
        return try {
            val userId = getCurrentUserId() ?: throw IllegalStateException("User not logged in")
            val snapshot = db.child(userId).get().await()
            val notes = mutableListOf<Note>()
            snapshot.children.forEach { data ->
                val note = data.getValue(Note::class.java)
                note?.let { notes.add(it) }
            }
            Log.d("NoteRepository", "Fetched ${notes.size} notes")
            Result.success(notes)
        } catch (e: Exception) {
            Log.e("NoteRepository", "Failed to fetch notes: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun uploadImage(uri: Uri, context: Context): Result<String> {
        return try {
            Log.d("NoteRepository", "Uploading image from URI: $uri")
            val file = File(context.cacheDir, "temp_image_${UUID.randomUUID()}.jpg")
            context.contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("image", file.name, requestFile)
            Log.d("NoteRepository", "Sending image to server...")
            val response = imageApi.uploadImage(body)
            Log.d("NoteRepository", "Server response: $response")
            if (response.success && response.imageUrl != null) {
                Log.d("NoteRepository", "Image uploaded successfully. URL: ${response.imageUrl}")
                Result.success(response.imageUrl)
            } else {
                val error = response.error ?: "Failed to upload image"
                Log.e("NoteRepository", "Image upload failed: $error")
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            Log.e("NoteRepository", "Failed to upload image: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun downloadImage(imageUrl: String, context: Context): Result<Uri> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("NoteRepository", "Downloading image from URL: $imageUrl")
                val fileName = "note_image_${UUID.randomUUID()}.jpg"
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = File(downloadsDir, fileName)

                URL(imageUrl).openStream().use { input ->
                    FileOutputStream(file).use { output ->
                        input.copyTo(output)
                    }
                }

                Log.d("NoteRepository", "Image downloaded to: ${file.absolutePath}")
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                Result.success(uri)
            } catch (e: Exception) {
                Log.e("NoteRepository", "Failed to download image: ${e.message}", e)
                Result.failure(e)
            }
        }
    }

    suspend fun deleteNote(noteId: String): Result<Unit> {
        return try {
            Log.d("NoteRepository", "Deleting note with ID: $noteId")
            val userId = getCurrentUserId() ?: throw IllegalStateException("User not logged in")
            db.child(userId).child(noteId).removeValue().await()
            Log.d("NoteRepository", "Note deleted successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("NoteRepository", "Failed to delete note: ${e.message}", e)
            Result.failure(e)
        }
    }
}