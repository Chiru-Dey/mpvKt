package live.mehiz.mpvkt.domain.mediabrowser

import android.net.Uri
import androidx.compose.runtime.Immutable

@Immutable
data class MediaItem(
  val id: Long,
  val displayName: String,
  val uri: Uri,
  val filePath: String,
  val duration: Long,
  val size: Long,
  val dateAdded: Long,
  val dateModified: Long,
  val folderName: String?,
  val width: Int,
  val height: Int,
  val lastPlayedAt: Long = 0L,
)
