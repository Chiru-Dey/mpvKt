package live.mehiz.mpvkt.ui.home

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.text.format.Formatter
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import `is`.xyz.mpv.Utils.PROTOCOLS
import kotlinx.serialization.Serializable
import live.mehiz.mpvkt.R
import live.mehiz.mpvkt.domain.mediabrowser.MediaItem
import live.mehiz.mpvkt.presentation.Screen
import live.mehiz.mpvkt.ui.player.PlayerActivity
import live.mehiz.mpvkt.ui.preferences.PreferencesScreen
import live.mehiz.mpvkt.ui.theme.spacing
import live.mehiz.mpvkt.ui.utils.LocalBackStack
import org.koin.compose.viewmodel.koinViewModel

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
    val context = LocalContext.current
    val backstack = LocalBackStack.current
    val viewModel = koinViewModel<HomeViewModel>()
    val mediaItems by viewModel.mediaItems.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val sortOption by viewModel.sortOption.collectAsState()
    val isGridView by viewModel.isGridView.collectAsState()
    val selectedItems by viewModel.selectedItems.collectAsState()
    val isSelectionMode by viewModel.isSelectionMode.collectAsState()

    var hasPermission by remember { mutableStateOf(hasVideoPermission(context)) }
    var showUrlDialog by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    var isSearchActive by remember { mutableStateOf(false) }
    var showFabMenu by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
      ActivityResultContracts.RequestPermission(),
    ) { granted ->
      hasPermission = granted
      if (granted) viewModel.loadMedia()
    }

    LaunchedEffect(Unit) {
      if (hasPermission) viewModel.loadMedia()
    }

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
              IconButton(onClick = {
                val shareIntent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                  type = "video/*"
                  val uris = mediaItems.filter { selectedItems.contains(it.id) }
                    .map { it.uri }
                  putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
                }
                context.startActivity(Intent.createChooser(shareIntent, null))
                viewModel.clearSelection()
              }) {
                Icon(Icons.Default.Share, null)
              }
            },
            colors = TopAppBarDefaults.topAppBarColors(
              containerColor = MaterialTheme.colorScheme.primaryContainer,
            ),
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
                  DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { showSortMenu = false },
                  ) {
                    SortOption.entries.forEach { option ->
                      DropdownMenuItem(
                        text = { Text(stringResource(option.titleRes)) },
                        onClick = {
                          viewModel.setSortOption(option)
                          showSortMenu = false
                        },
                        leadingIcon = if (sortOption == option) {
                          { Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary) }
                        } else {
                          null
                        },
                      )
                    }
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
        if (isGridView) {
          LazyVerticalGrid(
            columns = GridCells.Adaptive(160.dp),
            modifier = Modifier
              .fillMaxSize()
              .padding(padding),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall),
          ) {
            items(mediaItems, key = { it.id }) { item ->
              MediaGridItem(
                item = item,
                isSelected = selectedItems.contains(item.id),
                isSelectionMode = isSelectionMode,
                onClick = {
                  if (isSelectionMode) {
                    viewModel.toggleSelection(item.id)
                  } else {
                    playFile(item.uri.toString(), context)
                  }
                },
                onLongClick = { viewModel.toggleSelection(item.id) },
              )
            }
          }
        } else {
          LazyColumn(
            modifier = Modifier
              .fillMaxSize()
              .padding(padding),
          ) {
            items(mediaItems, key = { it.id }) { item ->
              MediaListItem(
                item = item,
                isSelected = selectedItems.contains(item.id),
                isSelectionMode = isSelectionMode,
                onClick = {
                  if (isSelectionMode) {
                    viewModel.toggleSelection(item.id)
                  } else {
                    playFile(item.uri.toString(), context)
                  }
                },
                onLongClick = { viewModel.toggleSelection(item.id) },
              )
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
