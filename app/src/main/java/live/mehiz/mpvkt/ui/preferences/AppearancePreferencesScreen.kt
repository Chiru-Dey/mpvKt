package live.mehiz.mpvkt.ui.preferences

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.Serializable
import live.mehiz.mpvkt.R
import live.mehiz.mpvkt.preferences.AppearancePreferences
import live.mehiz.mpvkt.preferences.preference.collectAsState
import live.mehiz.mpvkt.presentation.Screen
import live.mehiz.mpvkt.presentation.preferences.MultiChoiceSegmentedButton
import live.mehiz.mpvkt.ui.theme.DarkMode
import live.mehiz.mpvkt.ui.theme.ThemeColor
import live.mehiz.mpvkt.ui.theme.spacing
import live.mehiz.mpvkt.ui.utils.LocalBackStack
import me.zhanghai.compose.preference.PreferenceCategory
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import me.zhanghai.compose.preference.SwitchPreference
import org.koin.compose.koinInject

@Serializable
object AppearancePreferencesScreen : Screen {
  @OptIn(ExperimentalMaterial3Api::class)
  @Composable
  override fun Content() {
    val preferences = koinInject<AppearancePreferences>()
    val context = android.content.ContextWrapper(androidx.compose.ui.platform.LocalContext.current)
    val backstack = LocalBackStack.current
    Scaffold(
      topBar = {
        TopAppBar(
          title = { Text(text = stringResource(R.string.pref_appearance_title)) },
          navigationIcon = {
            IconButton(onClick = backstack::removeLastOrNull) {
              Icon(Icons.AutoMirrored.Outlined.ArrowBack, null)
            }
          },
        )
      },
    ) { padding ->
      ProvidePreferenceLocals {
        Column(
          modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(padding),
        ) {
          // ── Dark Mode ──
          PreferenceCategory(
            title = { Text(text = stringResource(id = R.string.pref_appearance_category_theme)) },
          )
          val darkMode by preferences.darkMode.collectAsState()
          MultiChoiceSegmentedButton(
            choices = DarkMode.entries.map { context.getString(it.titleRes) }.toImmutableList(),
            selectedIndices = persistentListOf(DarkMode.entries.indexOf(darkMode)),
            onClick = { preferences.darkMode.set(DarkMode.entries[it]) },
          )
          if (darkMode == DarkMode.Black) {
            Text(
              text = stringResource(R.string.pref_appearance_black_hint),
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(
                horizontal = MaterialTheme.spacing.medium,
                vertical = MaterialTheme.spacing.extraSmall,
              ),
            )
          }

          // ── Dynamic Color Toggle ──
          val materialYou by preferences.materialYou.collectAsState()
          val isMaterialYouAvailable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
          SwitchPreference(
            value = materialYou,
            onValueChange = { preferences.materialYou.set(it) },
            title = { Text(text = stringResource(id = R.string.pref_appearance_material_you_title)) },
            summary = {
              Text(
                text = stringResource(
                  if (isMaterialYouAvailable) {
                    R.string.pref_appearance_material_you_summary
                  } else {
                    R.string.pref_appearance_material_you_summary_disabled
                  },
                ),
              )
            },
            enabled = isMaterialYouAvailable,
          )

          // ── Color Palette ──
          PreferenceCategory(
            title = { Text(text = stringResource(R.string.pref_appearance_app_color)) },
          )
          val themeColor by preferences.themeColor.collectAsState()
          val colorGridAlpha = if (materialYou) 0.38f else 1f
          LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            modifier = Modifier
              .fillMaxWidth()
              .height(280.dp)
              .padding(horizontal = MaterialTheme.spacing.medium)
              .alpha(colorGridAlpha),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.smaller),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
            userScrollEnabled = false,
          ) {
            items(ThemeColor.entries.toList()) { color ->
              ColorPickerItem(
                color = color,
                isSelected = themeColor == color && !materialYou,
                onClick = {
                  if (!materialYou) {
                    preferences.themeColor.set(color)
                  } else {
                    preferences.materialYou.set(false)
                    preferences.themeColor.set(color)
                  }
                },
                enabled = !materialYou,
              )
            }
          }

          // ── Live Preview ──
          PreferenceCategory(
            title = { Text(text = stringResource(R.string.pref_appearance_preview)) },
          )
          ThemePreviewCard()
        }
      }
    }
  }
}

@Composable
private fun ColorPickerItem(
  color: ThemeColor,
  isSelected: Boolean,
  onClick: () -> Unit,
  enabled: Boolean,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier
      .clip(RoundedCornerShape(8.dp))
      .clickable(enabled = enabled, onClick = onClick)
      .padding(vertical = 4.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Box(
      modifier = Modifier
        .size(40.dp)
        .clip(CircleShape)
        .background(Color(color.seed))
        .then(
          if (isSelected) {
            Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
          } else {
            Modifier
          },
        ),
      contentAlignment = Alignment.Center,
    ) {
      if (isSelected) {
        Icon(
          Icons.Default.CheckCircle,
          contentDescription = null,
          tint = Color.White,
          modifier = Modifier.size(20.dp),
        )
      }
    }
    Spacer(Modifier.height(4.dp))
    Text(
      text = stringResource(color.titleRes),
      style = MaterialTheme.typography.labelSmall,
      textAlign = TextAlign.Center,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )
  }
}

@Composable
private fun ThemePreviewCard(modifier: Modifier = Modifier) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = MaterialTheme.spacing.medium),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    ),
    shape = RoundedCornerShape(16.dp),
  ) {
    Column(
      modifier = Modifier.padding(MaterialTheme.spacing.medium),
      verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.smaller),
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.smaller),
      ) {
        Box(
          modifier = Modifier
            .size(width = 48.dp, height = 32.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.primary),
        )
        Box(
          modifier = Modifier
            .size(width = 48.dp, height = 32.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.secondary),
        )
        Box(
          modifier = Modifier
            .size(width = 48.dp, height = 32.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.tertiary),
        )
      }
      Card(
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surface,
        ),
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(MaterialTheme.spacing.smaller),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            "Sample text",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Button(
            onClick = {},
            colors = ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.primary,
            ),
          ) {
            Text("Button")
          }
        }
      }
    }
  }
}
