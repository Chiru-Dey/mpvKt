package live.mehiz.mpvkt.ui.theme

import android.os.Build
import androidx.annotation.StringRes
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RippleConfiguration
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import live.mehiz.mpvkt.R
import live.mehiz.mpvkt.preferences.AppearancePreferences
import live.mehiz.mpvkt.preferences.preference.collectAsState
import org.koin.compose.koinInject

private val lightScheme = lightColorScheme(
  primary = primaryLight,
  onPrimary = onPrimaryLight,
  primaryContainer = primaryContainerLight,
  onPrimaryContainer = onPrimaryContainerLight,
  secondary = secondaryLight,
  onSecondary = onSecondaryLight,
  secondaryContainer = secondaryContainerLight,
  onSecondaryContainer = onSecondaryContainerLight,
  tertiary = tertiaryLight,
  onTertiary = onTertiaryLight,
  tertiaryContainer = tertiaryContainerLight,
  onTertiaryContainer = onTertiaryContainerLight,
  error = errorLight,
  onError = onErrorLight,
  errorContainer = errorContainerLight,
  onErrorContainer = onErrorContainerLight,
  background = backgroundLight,
  onBackground = onBackgroundLight,
  surface = surfaceLight,
  onSurface = onSurfaceLight,
  surfaceVariant = surfaceVariantLight,
  onSurfaceVariant = onSurfaceVariantLight,
  outline = outlineLight,
  outlineVariant = outlineVariantLight,
  scrim = scrimLight,
  inverseSurface = inverseSurfaceLight,
  inverseOnSurface = inverseOnSurfaceLight,
  inversePrimary = inversePrimaryLight,
  surfaceDim = surfaceDimLight,
  surfaceBright = surfaceBrightLight,
  surfaceContainerLowest = surfaceContainerLowestLight,
  surfaceContainerLow = surfaceContainerLowLight,
  surfaceContainer = surfaceContainerLight,
  surfaceContainerHigh = surfaceContainerHighLight,
  surfaceContainerHighest = surfaceContainerHighestLight,
)

private val darkScheme = darkColorScheme(
  primary = primaryDark,
  onPrimary = onPrimaryDark,
  primaryContainer = primaryContainerDark,
  onPrimaryContainer = onPrimaryContainerDark,
  secondary = secondaryDark,
  onSecondary = onSecondaryDark,
  secondaryContainer = secondaryContainerDark,
  onSecondaryContainer = onSecondaryContainerDark,
  tertiary = tertiaryDark,
  onTertiary = onTertiaryDark,
  tertiaryContainer = tertiaryContainerDark,
  onTertiaryContainer = onTertiaryContainerDark,
  error = errorDark,
  onError = onErrorDark,
  errorContainer = errorContainerDark,
  onErrorContainer = onErrorContainerDark,
  background = backgroundDark,
  onBackground = onBackgroundDark,
  surface = surfaceDark,
  onSurface = onSurfaceDark,
  surfaceVariant = surfaceVariantDark,
  onSurfaceVariant = onSurfaceVariantDark,
  outline = outlineDark,
  outlineVariant = outlineVariantDark,
  scrim = scrimDark,
  inverseSurface = inverseSurfaceDark,
  inverseOnSurface = inverseOnSurfaceDark,
  inversePrimary = inversePrimaryDark,
  surfaceDim = surfaceDimDark,
  surfaceBright = surfaceBrightDark,
  surfaceContainerLowest = surfaceContainerLowestDark,
  surfaceContainerLow = surfaceContainerLowDark,
  surfaceContainer = surfaceContainerDark,
  surfaceContainerHigh = surfaceContainerHighDark,
  surfaceContainerHighest = surfaceContainerHighestDark,
)

@Composable
fun MpvKtTheme(content: @Composable () -> Unit) {
  val preferences = koinInject<AppearancePreferences>()
  val darkMode by preferences.darkMode.collectAsState()
  val systemDarkTheme = isSystemInDarkTheme()
  val dynamicColor by preferences.materialYou.collectAsState()
  val themeColor by preferences.themeColor.collectAsState()
  val context = LocalContext.current

  val isDark = when (darkMode) {
    DarkMode.Light -> false
    DarkMode.Dark, DarkMode.Black -> true
    DarkMode.System -> systemDarkTheme
  }

  val baseScheme = when {
    dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
      if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }

    else -> {
      com.materialkolor.dynamicColorScheme(
        seedColor = Color(themeColor.seed),
        isDark = isDark,
        isAmoled = darkMode == DarkMode.Black,
      )
    }
  }

  val colorScheme = if (darkMode == DarkMode.Black) {
    baseScheme.copy(
      background = Color.Black,
      surface = Color.Black,
      surfaceVariant = Color(0xFF0A0A0A),
      surfaceDim = Color.Black,
      surfaceContainer = Color(0xFF050505),
      surfaceContainerLow = Color(0xFF030303),
      surfaceContainerLowest = Color.Black,
      surfaceContainerHigh = Color(0xFF0A0A0A),
      surfaceContainerHighest = Color(0xFF111111),
    )
  } else {
    baseScheme
  }

  CompositionLocalProvider(
    LocalSpacing provides Spacing(),
  ) {
    MaterialTheme(
      colorScheme = colorScheme,
      content = content,
    )
  }
}

enum class DarkMode(@StringRes val titleRes: Int) {
  Dark(R.string.pref_appearance_darkmode_dark),
  Light(R.string.pref_appearance_darkmode_light),
  Black(R.string.pref_appearance_darkmode_black),
  System(R.string.pref_appearance_darkmode_system),
}

private const val RIPPLE_DRAGGED_ALPHA = .5f
private const val RIPPLE_FOCUSED_ALPHA = .6f
private const val RIPPLE_HOVERED_ALPHA = .4f
private const val RIPPLE_PRESSED_ALPHA = .6f

@OptIn(ExperimentalMaterial3Api::class)
val playerRippleConfiguration
  @Composable get() = RippleConfiguration(
    color = MaterialTheme.colorScheme.primaryContainer,
    rippleAlpha = RippleAlpha(
      draggedAlpha = RIPPLE_DRAGGED_ALPHA,
      focusedAlpha = RIPPLE_FOCUSED_ALPHA,
      hoveredAlpha = RIPPLE_HOVERED_ALPHA,
      pressedAlpha = RIPPLE_PRESSED_ALPHA,
    ),
  )
