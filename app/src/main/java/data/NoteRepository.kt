package data

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.io.File
import java.util.UUID

// Retrofit API Interface
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
            .baseUrl("http://192.168.56.1/notesapp/upload.php/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        imageApi = retrofit.create(ImageApi::class.java)
    }

    suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(email: String, password: String): Result<Unit> {
        return try {
            auth.createUserWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getCurrentUserId(): String? = auth.currentUser?.uid

    suspend fun saveNote(note: Note): Result<Unit> {
        return try {
            val noteId = note.id.ifEmpty { UUID.randomUUID().toString() }
            val noteToSave = note.copy(id = noteId, userId = getCurrentUserId() ?: "")
            db.child(getCurrentUserId() ?: "").child(noteId).setValue(noteToSave).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getNotes(): Result<List<Note>> {
        return try {
            val snapshot = db.child(getCurrentUserId() ?: "").get().await()
            val notes = mutableListOf<Note>()
            snapshot.children.forEach { data ->
                val note = data.getValue(Note::class.java)
                note?.let { notes.add(it) }
            }
            Result.success(notes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadImage(uri: Uri, context: Context): Result<String> {
        return try {
            val file = File(context.cacheDir, "temp_image_${UUID.randomUUID()}.jpg")
            context.contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("image", file.name, requestFile)
            val response = imageApi.uploadImage(body)
            if (response.success && response.imageUrl != null) {
                Result.success(response.imageUrl)
            } else {
                Result.failure(Exception(response.error ?: "Failed to upload image"))
            }
        } catch (e: Exception) {
            Result.failure(e)
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