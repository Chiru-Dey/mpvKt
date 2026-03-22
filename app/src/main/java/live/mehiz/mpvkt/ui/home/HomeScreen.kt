package live.mehiz.mpvkt.ui.home

import androidx.compose.ui.graphics.asImageBitmap
import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.MediaStore
import android.text.format.Formatter
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.AlertDialog
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import `is`.xyz.mpv.Utils.PROTOCOLS
import kotlinx.serialization.Serializable
import live.mehiz.mpvkt.R
import live.mehiz.mpvkt.domain.mediabrowser.MediaItem
import live.mehiz.mpvkt.preferences.preference.collectAsState
import live.mehiz.mpvkt.presentation.Screen
import live.mehiz.mpvkt.ui.player.PlayerActivity
import live.mehiz.mpvkt.ui.preferences.PreferencesScreen
import live.mehiz.mpvkt.ui.theme.spacing
import live.mehiz.mpvkt.ui.utils.LocalBackStack
import org.koin.compose.viewmodel.koinViewModel
import coil3.asImage
import okio.Path.Companion.toOkioPath
import okio.source
import okio.buffer

private fun formatDuration(ms: Long): String {
  val totalSeconds = ms / 1000
  val hours = totalSeconds / 3600
  val minutes = (totalSeconds % 3600) / 60
  val seconds = totalSeconds % 60
  return if (hours > 0) "%d:%02d:%02d".format(hours, minutes, seconds)
  else "%d:%02d".format(minutes, seconds)
}

private fun hasVideoPermission(context: Context): Boolean {
  return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    ContextCompat.checkSelfPermission(
      context,
      Manifest.permission.READ_MEDIA_VIDEO,
    ) == PackageManager.PERMISSION_GRANTED
  } else {
    ContextCompat.checkSelfPermission(
      context,
      Manifest.permission.READ_EXTERNAL_STORAGE,
    ) == PackageManager.PERMISSION_GRANTED
  }
}

private fun getVideoPermission(): String {
  return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    Manifest.permission.READ_MEDIA_VIDEO
  } else {
    Manifest.permission.READ_EXTERNAL_STORAGE
  }
}


@Serializable
object HomeScreen : Screen {
  @OptIn(ExperimentalMaterial3Api::class)
  @Composable
  override fun Content() {
    android.util.Log.d("VThumb", ">>> Content() is running <<<")
    val context = android.content.ContextWrapper(androidx.compose.ui.platform.LocalContext.current)
    val backstack = LocalBackStack.current
    val viewModel = koinViewModel<HomeViewModel>()

    val mediaItems by viewModel.mediaItems.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val sortOption by viewModel.sortOption.collectAsState()
    val sortAscending by viewModel.sortAscending.collectAsState()
    val isGridView by viewModel.isGridView.collectAsState()
    val selectedItems by viewModel.selectedItems.collectAsState()
    val isSelectionMode by viewModel.isSelectionMode.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    var hasPermission by remember { mutableStateOf(hasVideoPermission(context)) }
    var showUrlDialog by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    var isSearchActive by remember { mutableStateOf(false) }
    var showFabMenu by remember { mutableStateOf(false) }
    var itemsToDelete by remember { mutableStateOf<List<MediaItem>?>(null) }
    var itemToRename by remember { mutableStateOf<MediaItem?>(null) }

    val gridState = androidx.compose.foundation.lazy.grid.rememberLazyGridState()
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()

    val permissionLauncher = rememberLauncherForActivityResult(
      ActivityResultContracts.RequestPermission(),
    ) { granted ->
      hasPermission = granted
      if (granted) viewModel.loadMedia()
    }

    LaunchedEffect(Unit) {
      if (hasPermission) viewModel.loadMedia()
    }

    val topAppBarColors = TopAppBarDefaults.topAppBarColors(
      containerColor = MaterialTheme.colorScheme.primaryContainer,
    )

    Scaffold(
      topBar = {
        if (isSelectionMode) {
          TopAppBar(
            title = { Text(stringResource(R.string.home_selected_count, selectedItems.size)) },
            navigationIcon = {
              IconButton(onClick = viewModel::clearSelection) {
                Icon(Icons.Default.Close, null)
              }
            },
            actions = {
              IconButton(onClick = viewModel::selectAll) {
                Icon(Icons.Default.SelectAll, null)
              }
            },
            colors = topAppBarColors,
          )
        } else {
          TopAppBar(
            title = {
              if (isSearchActive) {
                OutlinedTextField(
                  value = searchQuery,
                  onValueChange = viewModel::setSearchQuery,
                  placeholder = { Text(stringResource(R.string.home_search_hint)) },
                  singleLine = true,
                  trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                      IconButton(onClick = { viewModel.setSearchQuery("") }) {
                        Icon(Icons.Default.Clear, null)
                      }
                    }
                  },
                  modifier = Modifier.fillMaxWidth(),
                )
              } else {
                Text(text = stringResource(id = R.string.app_name))
              }
            },
            navigationIcon = {
              if (isSearchActive) {
                IconButton(onClick = {
                  isSearchActive = false
                  viewModel.setSearchQuery("")
                }) {
                  Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                }
              } else {
                Image(
                  painter = painterResource(id = R.drawable.ic_launcher_foreground),
                  contentDescription = "app_logo",
                )
              }
            },
            actions = {
              if (!isSearchActive) {
                IconButton(onClick = { isSearchActive = true }) {
                  Icon(Icons.Default.Search, null)
                }
                Box {
                  IconButton(onClick = { showSortMenu = true }) {
                    Icon(Icons.Default.Sort, null)
                  }
                  if (sortOption != SortOption.TITLE || !sortAscending) {
                    Box(
                      modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .size(6.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                    )
                  }
                }
                IconButton(onClick = viewModel::toggleGridView) {
                  Icon(
                    if (isGridView) Icons.Default.ViewList else Icons.Default.GridView,
                    null,
                  )
                }
                IconButton(onClick = { backstack.add(PreferencesScreen) }) {
                  Icon(Icons.Default.Settings, null)
                }
              }
            },
          )
        }
      },
      bottomBar = {
        androidx.compose.animation.AnimatedVisibility(
          visible = isSelectionMode,
          enter = androidx.compose.animation.slideInVertically(initialOffsetY = { it }),
          exit = androidx.compose.animation.slideOutVertically(targetOffsetY = { it })
        ) {
          androidx.compose.material3.Surface(
            tonalElevation = 3.dp,
            color = MaterialTheme.colorScheme.surface
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = MaterialTheme.spacing.small, horizontal = MaterialTheme.spacing.medium)
                .navigationBarsPadding(),
              horizontalArrangement = Arrangement.SpaceEvenly
            ) {
              val selectedMedia = mediaItems.filter { selectedItems.contains(it.id) }
              SelectionActionItem(
                icon = Icons.Default.PlayArrow,
                label = stringResource(R.string.home_action_play),
                enabled = selectedMedia.isNotEmpty(),
                onClick = {
                  if (selectedMedia.isNotEmpty()) {
                    if (selectedMedia.size == 1) {
                      viewModel.updateLastPlayed(selectedMedia.first().id)
                      playFile(selectedMedia.first().uri.toString(), context)
                    } else {
                      val tempPlaylist = java.io.File(context.cacheDir, "playlist.m3u")
                      tempPlaylist.writeText(selectedMedia.joinToString("\n") { it.uri.toString() })
                      playFile(tempPlaylist.absolutePath, context)
                    }
                    viewModel.clearSelection()
                  }
                }
              )
              SelectionActionItem(
                icon = Icons.Default.Share,
                label = stringResource(R.string.home_action_share),
                enabled = selectedMedia.isNotEmpty(),
                onClick = {
                  val shareIntent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                    type = "video/*"
                    val uris = selectedMedia.map { it.uri }
                    putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
                  }
                  context.startActivity(Intent.createChooser(shareIntent, null))
                  viewModel.clearSelection()
                }
              )
              SelectionActionItem(
                icon = Icons.Default.Edit,
                label = stringResource(R.string.home_action_rename),
                enabled = selectedMedia.size == 1,
                onClick = {
                  itemToRename = selectedMedia.first()
                }
              )
              SelectionActionItem(
                icon = Icons.Default.Delete,
                label = stringResource(R.string.home_action_delete),
                enabled = selectedMedia.isNotEmpty(),
                onClick = {
                  itemsToDelete = selectedMedia
                }
              )
            }
          }
        }
      },
      floatingActionButton = {
        if (!isSelectionMode) {
          Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.smaller),
          ) {
            AnimatedVisibility(showFabMenu, enter = fadeIn(), exit = fadeOut()) {
              Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.smaller),
              ) {
                val documentPicker = rememberLauncherForActivityResult(
                  ActivityResultContracts.OpenDocument(),
                ) {
                  if (it == null) return@rememberLauncherForActivityResult
                  playFile(it.toString(), context)
                }
                SmallFloatingActionButton(
                  onClick = {
                    documentPicker.launch(arrayOf("*/*"))
                    showFabMenu = false
                  },
                ) {
                  Icon(Icons.Default.FileOpen, stringResource(R.string.home_pick_file))
                }
                val directoryPicker = rememberLauncherForActivityResult(
                  ActivityResultContracts.OpenDocumentTree(),
                ) {
                  if (it == null) return@rememberLauncherForActivityResult
                  val fileManager = com.github.k1rakishou.fsaf.FileManager(context)
                  backstack.add(FilePickerScreen(fileManager.fromUri(it)!!.getFullPath()))
                  showFabMenu = false
                }
                SmallFloatingActionButton(
                  onClick = {
                    directoryPicker.launch(null)
                    showFabMenu = false
                  },
                ) {
                  Icon(Icons.Default.FolderOpen, stringResource(R.string.home_open_file_picker))
                }
                SmallFloatingActionButton(
                  onClick = {
                    showUrlDialog = true
                    showFabMenu = false
                  },
                ) {
                  Icon(Icons.Default.Link, stringResource(R.string.home_open_url))
                }
              }
            }
            FloatingActionButton(onClick = { showFabMenu = !showFabMenu }) {
              Icon(Icons.Default.Add, null)
            }
          }
        }
      },
    ) { padding ->
      if (!hasPermission) {
        PermissionRationale(
          modifier = Modifier
            .fillMaxSize()
            .padding(padding),
          onGrantClick = { permissionLauncher.launch(getVideoPermission()) },
        )
      } else if (isLoading) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(padding),
          contentAlignment = Alignment.Center,
        ) {
          CircularProgressIndicator()
        }
      } else if (mediaItems.isEmpty()) {
        EmptyState(
          modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        )
      } else {
        androidx.compose.material3.pulltorefresh.PullToRefreshBox(
          isRefreshing = isRefreshing,
          onRefresh = { viewModel.refresh() },
          modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        ) {
          if (isGridView) {
            LazyVerticalGrid(
              state = gridState,
              columns = GridCells.Adaptive(160.dp),
              modifier = Modifier.fillMaxSize(),
              horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall),
              verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall),
            ) {
              items(mediaItems, key = { it.id }, contentType = { "media_item" }) { item ->
                MediaGridItem(
                  item = item,
                  isSelected = selectedItems.contains(item.id),
                  isSelectionMode = isSelectionMode,
                  onClick = {
                    if (isSelectionMode) {
                      viewModel.toggleSelection(item.id)
                    } else {
                      viewModel.updateLastPlayed(item.id)
                      playFile(item.uri.toString(), context)
                    }
                  },
                  onLongClick = { viewModel.toggleSelection(item.id) },
                )
              }
            }
          } else {
            LazyColumn(
              state = listState,
              modifier = Modifier.fillMaxSize(),
            ) {
              items(mediaItems, key = { it.id }, contentType = { "media_item" }) { item ->
                MediaListItem(
                  item = item,
                  isSelected = selectedItems.contains(item.id),
                  isSelectionMode = isSelectionMode,
                  onClick = {
                    if (isSelectionMode) {
                      viewModel.toggleSelection(item.id)
                    } else {
                      viewModel.updateLastPlayed(item.id)
                      playFile(item.uri.toString(), context)
                    }
                  },
                  onLongClick = { viewModel.toggleSelection(item.id) },
                )
              }
            }
          }
        }
      }

      if (showUrlDialog) {
        UrlInputDialog(
          onDismiss = { showUrlDialog = false },
          onOpenUrl = { url ->
            showUrlDialog = false
            playFile(url, context)
          },
        )
      }

      if (showSortMenu) {
        androidx.compose.material3.ModalBottomSheet(onDismissRequest = { showSortMenu = false }) {
          Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = MaterialTheme.spacing.medium),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              stringResource(R.string.home_sort_by),
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(bottom = MaterialTheme.spacing.medium)
            )
            SortOption.entries.forEach { option ->
              val isSelected = sortOption == option
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(8.dp))
                  .background(if (isSelected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent)
                  .clickable {
                    viewModel.setSortOption(option)
                    showSortMenu = false
                  }
                  .padding(MaterialTheme.spacing.medium),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text(
                  stringResource(option.titleRes),
                  style = MaterialTheme.typography.bodyLarge,
                  color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface
                )
                if (isSelected) {
                  Icon(
                    if (sortAscending) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                  )
                }
              }
            }
            Spacer(Modifier.height(MaterialTheme.spacing.large))
          }
        }
      }

      if (itemsToDelete != null) {
        val count = itemsToDelete!!.size
        AlertDialog(
          onDismissRequest = { itemsToDelete = null },
          title = { Text(stringResource(R.string.home_delete_confirm_title)) },
          text = { Text(stringResource(R.string.home_delete_confirm_body, count)) },
          confirmButton = {
            TextButton(onClick = {
              itemsToDelete?.forEach { item ->
                try {
                  context.contentResolver.delete(item.uri, null, null)
                } catch (e: Exception) {
                  e.printStackTrace()
                }
              }
              itemsToDelete = null
              viewModel.clearSelection()
              viewModel.loadMedia()
            }) { Text(stringResource(R.string.home_action_delete), color = MaterialTheme.colorScheme.error) }
          },
          dismissButton = {
            TextButton(onClick = { itemsToDelete = null }) { Text(stringResource(R.string.generic_cancel)) }
          }
        )
      }

      if (itemToRename != null) {
        var newName by remember(itemToRename) { mutableStateOf(itemToRename!!.displayName.substringBeforeLast('.')) }
        val extension = ".${itemToRename!!.displayName.substringAfterLast('.')}"
        AlertDialog(
          onDismissRequest = { itemToRename = null },
          title = { Text(stringResource(R.string.home_action_rename)) },
          text = {
            OutlinedTextField(
              value = newName,
              onValueChange = { newName = it },
              singleLine = true,
              suffix = { Text(extension) }
            )
          },
          confirmButton = {
            TextButton(
              enabled = newName.isNotBlank() && newName + extension != itemToRename!!.displayName,
              onClick = {
                val values = android.content.ContentValues().apply {
                  put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, newName + extension)
                }
                try {
                  context.contentResolver.update(itemToRename!!.uri, values, null, null)
                } catch (e: Exception) {
                  e.printStackTrace()
                }
                itemToRename = null
                viewModel.clearSelection()
                viewModel.loadMedia()
            }) { Text(stringResource(R.string.home_action_rename)) }
          },
          dismissButton = {
            TextButton(onClick = { itemToRename = null }) { Text(stringResource(R.string.generic_cancel)) }
          }
        )
      }
    }
  }

  fun playFile(
    filepath: String,
    context: Context,
  ) {
    val i = Intent(Intent.ACTION_VIEW, filepath.toUri())
    i.setClass(context, PlayerActivity::class.java)
    context.startActivity(i)
  }

  // Basically a copy of:
  // https://github.com/mpv-android/mpv-android/blob/32cbff3cedea73b4616b34542cb95bf1d00504cc/app/src/main/java/is/xyz/mpv/Utils.kt#L406
  internal fun isURLValid(url: String): Boolean {
    val uri = url.toUri()
    return uri.isHierarchical && !uri.isRelative &&
      !(uri.host.isNullOrBlank() && uri.path.isNullOrBlank()) &&
      PROTOCOLS.contains(uri.scheme)
  }
}

@Composable
private fun UrlInputDialog(
  onDismiss: () -> Unit,
  onOpenUrl: (String) -> Unit,
) {
  var url by remember { mutableStateOf("") }
  var isValid by remember { mutableStateOf(true) }
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(stringResource(R.string.home_open_url)) },
    text = {
      OutlinedTextField(
        value = url,
        onValueChange = {
          url = it
          isValid = it.isEmpty() || HomeScreen.isURLValid(it)
        },
        label = { Text(stringResource(R.string.home_url_input_label)) },
        isError = !isValid,
        singleLine = true,
        supportingText = {
          if (!isValid) Text(stringResource(R.string.home_invalid_protocol))
        },
      )
    },
    confirmButton = {
      TextButton(
        onClick = { onOpenUrl(url) },
        enabled = url.isNotBlank() && isValid,
      ) { Text(stringResource(R.string.generic_ok)) }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(stringResource(R.string.generic_cancel))
      }
    },
  )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MediaGridItem(
  item: MediaItem,
  isSelected: Boolean,
  isSelectionMode: Boolean,
  onClick: () -> Unit,
  onLongClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  Card(
    modifier = modifier
      .combinedClickable(
        onClick = onClick,
        onLongClick = onLongClick,
      ),
    colors = CardDefaults.cardColors(
      containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
      } else {
        MaterialTheme.colorScheme.surfaceContainerLow
      },
    ),
  ) {
    Box {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .aspectRatio(16f / 9f)
          .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
          .background(MaterialTheme.colorScheme.surfaceContainerHighest),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          Icons.Default.VideoLibrary,
          contentDescription = null,
          modifier = Modifier.size(48.dp),
          tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
        )
        VideoThumbnail(
          mediaId = item.id,
          context = context,
          modifier = Modifier.matchParentSize()
      )

        // Duration badge
        Box(
          Modifier
            .align(Alignment.BottomEnd)
            .padding(MaterialTheme.spacing.extraSmall)
            .background(
              color = Color.Black.copy(alpha = 0.7f),
              shape = RoundedCornerShape(4.dp),
            )
            .padding(horizontal = 6.dp, vertical = 2.dp),
        ) {
          Text(
            formatDuration(item.duration),
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
          )
        }
      }
      if (isSelectionMode) {
        Icon(
          if (isSelected) Icons.Default.CheckCircle else Icons.Default.CheckCircle,
          contentDescription = null,
          modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(MaterialTheme.spacing.extraSmall)
            .size(24.dp),
          tint = if (isSelected) {
            MaterialTheme.colorScheme.primary
          } else {
            Color.White.copy(alpha = 0.6f)
          },
        )
      }
    }
    Column(
      modifier = Modifier.padding(MaterialTheme.spacing.smaller),
    ) {
      Text(
        item.displayName,
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.Medium,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
      )
      Row(
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall),
      ) {
        item.folderName?.let {
          Text(
            it,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
          )
        }
        Text(
          Formatter.formatShortFileSize(context, item.size),
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MediaListItem(
  item: MediaItem,
  isSelected: Boolean,
  isSelectionMode: Boolean,
  onClick: () -> Unit,
  onLongClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  Row(
    modifier = modifier
      .fillMaxWidth()
      .combinedClickable(
        onClick = onClick,
        onLongClick = onLongClick,
      )
      .background(
        if (isSelected) MaterialTheme.colorScheme.primaryContainer
        else Color.Transparent,
      )
      .padding(
        horizontal = MaterialTheme.spacing.medium,
        vertical = MaterialTheme.spacing.smaller,
      ),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
  ) {
    Box(
      modifier = Modifier
        .size(width = 72.dp, height = 48.dp)
        .clip(RoundedCornerShape(8.dp))
        .background(MaterialTheme.colorScheme.surfaceContainerHighest),
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        Icons.Default.VideoLibrary,
        contentDescription = null,
        modifier = Modifier.size(24.dp),
        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
      )
      VideoThumbnail(
          mediaId = item.id,
          context = context,
          modifier = Modifier.matchParentSize()
      )
    }
    Column(modifier = Modifier.weight(1f)) {
      Text(
        item.displayName,
        style = MaterialTheme.typography.bodyMedium,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
      Row(
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.smaller),
      ) {
        Text(
          formatDuration(item.duration),
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        item.folderName?.let {
          Text(
            "• $it",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
        Text(
          "• ${Formatter.formatShortFileSize(context, item.size)}",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
    if (isSelectionMode) {
      Icon(
        Icons.Default.CheckCircle,
        contentDescription = null,
        tint = if (isSelected) {
          MaterialTheme.colorScheme.primary
        } else {
          MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
        },
      )
    }
  }
}

@Composable
private fun PermissionRationale(
  modifier: Modifier = Modifier,
  onGrantClick: () -> Unit,
) {
  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Icon(
      Icons.Default.VideoLibrary,
      contentDescription = null,
      modifier = Modifier.size(64.dp),
      tint = MaterialTheme.colorScheme.primary,
    )
    Spacer(Modifier.height(MaterialTheme.spacing.medium))
    Text(
      stringResource(R.string.home_permission_title),
      style = MaterialTheme.typography.headlineSmall,
    )
    Spacer(Modifier.height(MaterialTheme.spacing.smaller))
    Text(
      stringResource(R.string.home_permission_body),
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(Modifier.height(MaterialTheme.spacing.large))
    Button(onClick = onGrantClick) {
      Text(stringResource(R.string.home_permission_grant))
    }
  }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Icon(
      Icons.Default.VideoLibrary,
      contentDescription = null,
      modifier = Modifier.size(64.dp),
      tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
    )
    Spacer(Modifier.height(MaterialTheme.spacing.medium))
    Text(
      stringResource(R.string.home_no_videos),
      style = MaterialTheme.typography.titleMedium,
    )
    Spacer(Modifier.height(MaterialTheme.spacing.smaller))
    Text(
      stringResource(R.string.home_no_videos_hint),
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
  }
}

private val thumbnailCache = object : android.util.LruCache<Long, android.graphics.Bitmap>(
    (Runtime.getRuntime().maxMemory() / 8).toInt()
) {
    override fun sizeOf(key: Long, value: android.graphics.Bitmap) = value.byteCount
}

@Composable
fun VideoThumbnail(
    mediaId: Long,
    context: Context,
    modifier: Modifier = Modifier
) {
    var bitmap by remember(mediaId) { mutableStateOf(thumbnailCache[mediaId]) }

    LaunchedEffect(mediaId) {
        if (bitmap != null) return@LaunchedEffect
        bitmap = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            thumbnailCache[mediaId]?.let { return@withContext it }
            try {
                val uri = ContentUris.withAppendedId(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI, mediaId
                )

                // Resolve file path via DATA column
                val filePath = context.contentResolver.query(
                    uri,
                    arrayOf(MediaStore.Video.Media.DATA),
                    null, null, null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) cursor.getString(0) else null
                }
                android.util.Log.d("VThumb", "mediaId=$mediaId filePath=$filePath")

                // PATH 1: Sidecar image
                if (filePath != null) {
                    val videoFile = java.io.File(filePath)
                    val baseName = videoFile.nameWithoutExtension
                    val parentDir = videoFile.parentFile
                    android.util.Log.d("VThumb", "mediaId=$mediaId checking sidecar in ${parentDir?.absolutePath} for base=$baseName")
                    for (ext in listOf("jpg", "jpeg", "png", "webp")) {
                        val sidecar = java.io.File(parentDir, "$baseName.$ext")
                        android.util.Log.d("VThumb", "mediaId=$mediaId sidecar check: ${sidecar.absolutePath} exists=${sidecar.exists()}")
                        if (sidecar.exists()) {
                            val bmp = android.graphics.BitmapFactory.decodeFile(sidecar.absolutePath)
                            if (bmp != null) {
                                android.util.Log.d("VThumb", "mediaId=$mediaId SIDECAR HIT ${sidecar.name}")
                                thumbnailCache.put(mediaId, bmp)
                                return@withContext bmp
                            }
                        }
                    }
                }

                // PATH 2: Frame extraction
                val retriever = android.media.MediaMetadataRetriever()
                if (filePath != null) retriever.setDataSource(filePath)
                else retriever.setDataSource(context, uri)
                val durationMs = retriever.extractMetadata(
                    android.media.MediaMetadataRetriever.METADATA_KEY_DURATION
                )?.toLongOrNull() ?: 0L
                val seekUs = if (durationMs > 0) (durationMs * 1000L * 0.1).toLong() else 10_000_000L
                val bmp = retriever.getFrameAtTime(seekUs, android.media.MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                retriever.release()
                if (bmp != null) thumbnailCache.put(mediaId, bmp)
                bmp
            } catch (e: Exception) {
                android.util.Log.e("VThumb", "error for mediaId=$mediaId", e)
                null
            }
        }
    }

    if (bitmap != null) {
        androidx.compose.foundation.Image(
            bitmap = bitmap!!.asImageBitmap(),
            contentDescription = null,
            modifier = modifier,
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )
    }
}



@Composable
fun SelectionActionItem(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  label: String,
  enabled: Boolean = true,
  onClick: () -> Unit
) {
  Column(
    modifier = Modifier
      .clip(RoundedCornerShape(8.dp))
      .clickable(enabled = enabled, onClick = onClick)
      .padding(MaterialTheme.spacing.small)
      .alpha(if (enabled) 1f else 0.38f),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Icon(icon, contentDescription = label)
    Text(label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp))
  }
}
