package data

data class Note(
    val id: String = "",
    val userId: String? = null,
    val title: String = "",
    val content: String = "",
    val imageUrl: String? = null
)