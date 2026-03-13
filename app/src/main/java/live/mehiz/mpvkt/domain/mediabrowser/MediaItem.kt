package live.mehiz.mpvkt.domain.mediabrowser

import android.net.Uri

data class MediaItem(
  val id: Long,
  val displayName: String,
  val uri: Uri,
  val duration: Long,
  val size: Long,
  val dateModified: Long,
  val folderName: String?,
)
