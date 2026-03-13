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

enum class SortOption(@StringRes val titleRes: Int) {
  NAME(R.string.home_sort_name),
  DATE(R.string.home_sort_date),
  DURATION(R.string.home_sort_duration),
  SIZE(R.string.home_sort_size),
}

class HomeViewModel(
  private val mediaRepository: MediaRepository,
) : ViewModel() {

  private val _allMedia = MutableStateFlow<List<MediaItem>>(emptyList())

  private val _searchQuery = MutableStateFlow("")
  val searchQuery = _searchQuery.asStateFlow()

  private val _sortOption = MutableStateFlow(SortOption.DATE)
  val sortOption = _sortOption.asStateFlow()

  private val _isGridView = MutableStateFlow(true)
  val isGridView = _isGridView.asStateFlow()

  private val _selectedItems = MutableStateFlow<Set<Long>>(emptySet())
  val selectedItems = _selectedItems.asStateFlow()

  private val _isLoading = MutableStateFlow(true)
  val isLoading = _isLoading.asStateFlow()

  val mediaItems = combine(_allMedia, _searchQuery, _sortOption) { items, query, sort ->
    val filtered = if (query.isBlank()) {
      items
    } else {
      items.filter {
        it.displayName.contains(query, ignoreCase = true) ||
          it.folderName?.contains(query, ignoreCase = true) == true
      }
    }
    when (sort) {
      SortOption.NAME -> filtered.sortedBy { it.displayName.lowercase() }
      SortOption.DATE -> filtered.sortedByDescending { it.dateModified }
      SortOption.DURATION -> filtered.sortedByDescending { it.duration }
      SortOption.SIZE -> filtered.sortedByDescending { it.size }
    }
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

  fun setSearchQuery(query: String) {
    _searchQuery.update { query }
  }

  fun setSortOption(sort: SortOption) {
    _sortOption.update { sort }
  }

  fun toggleGridView() {
    _isGridView.update { !it }
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
