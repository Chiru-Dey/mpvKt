package live.mehiz.mpvkt.domain.mediabrowser

import android.content.ContentUris
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MediaRepository(private val context: Context) {

  @Suppress("LongMethod")
  suspend fun getVideos(): List<MediaItem> = withContext(Dispatchers.IO) {
    val videos = mutableListOf<MediaItem>()
    val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
    } else {
      MediaStore.Video.Media.EXTERNAL_CONTENT_URI
    }

    val projection = arrayOf(
      MediaStore.Video.Media._ID,
      MediaStore.Video.Media.DISPLAY_NAME,
      MediaStore.Video.Media.DURATION,
      MediaStore.Video.Media.SIZE,
      MediaStore.Video.Media.DATE_MODIFIED,
      MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
    )

    val sortOrder = "${MediaStore.Video.Media.DATE_MODIFIED} DESC"

    context.contentResolver.query(
      collection,
      projection,
      null,
      null,
      sortOrder,
    )?.use { cursor ->
      val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
      val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
      val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
      val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
      val dateModifiedColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED)
      val bucketColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)

      while (cursor.moveToNext()) {
        val id = cursor.getLong(idColumn)
        val contentUri = ContentUris.withAppendedId(
          MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
          id,
        )
        videos.add(
          MediaItem(
            id = id,
            displayName = cursor.getString(nameColumn) ?: "",
            uri = contentUri,
            duration = cursor.getLong(durationColumn),
            size = cursor.getLong(sizeColumn),
            dateModified = cursor.getLong(dateModifiedColumn),
            folderName = cursor.getString(bucketColumn),
          ),
        )
      }
    }
    videos
  }
}
