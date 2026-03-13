package live.mehiz.mpvkt.ui.theme

import androidx.annotation.StringRes
import live.mehiz.mpvkt.R

enum class ThemeColor(@StringRes val titleRes: Int, val seed: Long) {
  MPV_VIOLET(R.string.theme_color_mpv_violet, 0xFF6650A4),
  MATERIAL_BLUE(R.string.theme_color_material_blue, 0xFF1565C0),
  MX_BLUE(R.string.theme_color_mx_blue, 0xFF1976D2),
  RED(R.string.theme_color_red, 0xFFB71C1C),
  PINK(R.string.theme_color_pink, 0xFFAD1457),
  ORANGE(R.string.theme_color_orange, 0xFFE65100),
  YELLOW(R.string.theme_color_yellow, 0xFFF9A825),
  GREEN(R.string.theme_color_green, 0xFF2E7D32),
  TEAL(R.string.theme_color_teal, 0xFF00695C),
  CYAN(R.string.theme_color_cyan, 0xFF00838F),
  INDIGO(R.string.theme_color_indigo, 0xFF283593),
  DEEP_PURPLE(R.string.theme_color_deep_purple, 0xFF4527A0),
  BROWN(R.string.theme_color_brown, 0xFF4E342E),
  GREY(R.string.theme_color_grey, 0xFF424242),
}
