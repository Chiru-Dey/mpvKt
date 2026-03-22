package live.mehiz.mpvkt.ui.home

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import live.mehiz.mpvkt.R
import live.mehiz.mpvkt.domain.mediabrowser.MediaItem
import live.mehiz.mpvkt.domain.mediabrowser.MediaRepository
import live.mehiz.mpvkt.preferences.preference.PreferenceStore
import live.mehiz.mpvkt.preferences.preference.getEnum

enum class SortOption(@StringRes val titleRes: Int) {
  TITLE(R.string.home_sort_title),
  DATE_ADDED(R.string.home_sort_date_added),
  DATE_MODIFIED(R.string.home_sort_date_modified),
  PLAYED_TIME(R.string.home_sort_played_time),
  DURATION(R.string.home_sort_duration),
  SIZE(R.string.home_sort_size),
  RESOLUTION(R.string.home_sort_resolution),
  PATH(R.string.home_sort_path),
  FILE_TYPE(R.string.home_sort_file_type)
}

class HomeViewModel(
  private val mediaRepository: MediaRepository,
  private val preferenceStore: PreferenceStore,
) : ViewModel() {

  private val _allMedia = MutableStateFlow<List<MediaItem>>(emptyList())

  private val _searchQuery = MutableStateFlow("")
  val searchQuery = _searchQuery.asStateFlow()

  val sortOption = preferenceStore.getEnum("home_sort_option", SortOption.TITLE)
  val sortAscending = preferenceStore.getBoolean("home_sort_ascending", true)

  val isGridView = preferenceStore.getBoolean("home_grid_view", false)

  private val _selectedItems = MutableStateFlow<Set<Long>>(emptySet())
  val selectedItems = _selectedItems.asStateFlow()

  private val _isLoading = MutableStateFlow(true)
  val isLoading = _isLoading.asStateFlow()

  private val _isRefreshing = MutableStateFlow(false)
  val isRefreshing = _isRefreshing.asStateFlow()

  val mediaItems = combine(
    _allMedia,
    _searchQuery,
    sortOption.changes(),
    sortAscending.changes(),
  ) { items, query, sort, ascending ->
    val filtered = if (query.isBlank()) {
      items
    } else {
      items.filter {
        it.displayName.contains(query, ignoreCase = true) ||
          it.folderName?.contains(query, ignoreCase = true) == true
      }
    }
    
    val sorted = when (sort) {
      SortOption.TITLE -> filtered.sortedBy { it.displayName.lowercase() }
      SortOption.DATE_ADDED -> filtered.sortedBy { it.dateAdded }
      SortOption.DATE_MODIFIED -> filtered.sortedBy { it.dateModified }
      SortOption.PLAYED_TIME -> filtered.sortedBy { it.lastPlayedAt }
      SortOption.DURATION -> filtered.sortedBy { it.duration }
      SortOption.SIZE -> filtered.sortedBy { it.size }
      SortOption.RESOLUTION -> filtered.sortedBy { it.width * it.height }
      SortOption.PATH -> filtered.sortedBy { it.folderName?.lowercase() ?: "" }
      SortOption.FILE_TYPE -> filtered.sortedBy { it.displayName.substringAfterLast('.', "").lowercase() }
    }
    
    if (ascending) sorted else sorted.reversed()
  }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

  val isSelectionMode = combine(_selectedItems) { (selected) ->
    selected.isNotEmpty()
  }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

  init {
    loadMedia()
  }

  fun loadMedia() {
    viewModelScope.launch {
      _isLoading.update { true }
      _allMedia.update { mediaRepository.getVideos() }
      _isLoading.update { false }
    }
  }

  fun refresh() {
    viewModelScope.launch {
      _isRefreshing.update { true }
      _allMedia.update { mediaRepository.getVideos() }
      _isRefreshing.update { false }
    }
  }

  fun setSearchQuery(query: String) {
    _searchQuery.update { query }
  }

  fun setSortOption(sort: SortOption) {
    if (sortOption.get() == sort) {
      sortAscending.set(!sortAscending.get())
    } else {
      sortOption.set(sort)
      sortAscending.set(true)
    }
  }

  fun updateLastPlayed(id: Long) {
    preferenceStore.getLong("last_played_$id", 0L).set(System.currentTimeMillis())
    loadMedia() // refresh the list so Played Time sort updates
  }

  fun toggleGridView() {
    isGridView.set(!isGridView.get())
  }

  fun toggleSelection(id: Long) {
    _selectedItems.update {
      if (it.contains(id)) it - id else it + id
    }
  }

  fun clearSelection() {
    _selectedItems.update { emptySet() }
  }

  fun selectAll() {
    _selectedItems.update { _allMedia.value.map { it.id }.toSet() }
  }
}
