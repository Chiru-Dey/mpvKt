package live.mehiz.mpvkt.domain.mediabrowser

import android.content.ContentUris
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MediaRepository(
  private val context: Context,
  private val preferenceStore: live.mehiz.mpvkt.preferences.preference.PreferenceStore
) {

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
      MediaStore.Video.Media.DATA,
      MediaStore.Video.Media.DURATION,
      MediaStore.Video.Media.SIZE,
      MediaStore.Video.Media.DATE_ADDED,
      MediaStore.Video.Media.DATE_MODIFIED,
      MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
      MediaStore.Video.Media.WIDTH,
      MediaStore.Video.Media.HEIGHT,
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
      val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
      val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
      val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
      val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
      val dateModifiedColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED)
      val bucketColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)
      val widthColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.WIDTH)
      val heightColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.HEIGHT)

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
            filePath = cursor.getString(dataColumn) ?: "",
            duration = cursor.getLong(durationColumn),
            size = cursor.getLong(sizeColumn),
            dateAdded = cursor.getLong(dateAddedColumn),
            dateModified = cursor.getLong(dateModifiedColumn),
            folderName = cursor.getString(bucketColumn),
            width = cursor.getInt(widthColumn),
            height = cursor.getInt(heightColumn),
            lastPlayedAt = preferenceStore.getLong("last_played_$id", 0L).get(),
          ),
        )
      }
    }
    videos
  }
}
