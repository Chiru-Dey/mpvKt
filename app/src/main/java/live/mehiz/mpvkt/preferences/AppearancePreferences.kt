package live.mehiz.mpvkt.preferences

import android.os.Build
import live.mehiz.mpvkt.preferences.preference.PreferenceStore
import live.mehiz.mpvkt.preferences.preference.getEnum
import live.mehiz.mpvkt.ui.theme.DarkMode
import live.mehiz.mpvkt.ui.theme.ThemeColor

class AppearancePreferences(preferenceStore: PreferenceStore) {
  val darkMode = preferenceStore.getEnum("dark_mode", DarkMode.System)
  val materialYou = preferenceStore.getBoolean("material_you", Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
  val themeColor = preferenceStore.getEnum("theme_color", ThemeColor.MPV_VIOLET)
}
