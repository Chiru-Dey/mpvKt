package live.mehiz.mpvkt.ui.player.controls

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import live.mehiz.mpvkt.ui.theme.spacing
import kotlin.math.roundToInt

private val quickSpeeds = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SpeedPickerPanel(
  speed: Float,
  onSpeedChange: (Float) -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier
      .clip(RoundedCornerShape(16.dp))
      .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.92f))
      .padding(MaterialTheme.spacing.medium),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.smaller),
  ) {
    Text(
      "%.2f×".format(speed),
      style = MaterialTheme.typography.headlineMedium,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.primary,
    )
    Slider(
      value = speed,
      onValueChange = { onSpeedChange(((it * 20).roundToInt() / 20f)) },
      valueRange = 0.25f..3.0f,
      modifier = Modifier.fillMaxWidth(),
      colors = SliderDefaults.colors(
        thumbColor = MaterialTheme.colorScheme.primary,
        activeTrackColor = MaterialTheme.colorScheme.primary,
      ),
    )
    FlowRow(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(
        MaterialTheme.spacing.extraSmall,
        Alignment.CenterHorizontally,
      ),
    ) {
      quickSpeeds.forEach { preset ->
        FilterChip(
          selected = (speed * 100).roundToInt() == (preset * 100).roundToInt(),
          onClick = { onSpeedChange(preset) },
          label = { Text("%.2f×".format(preset), style = MaterialTheme.typography.labelSmall) },
        )
      }
    }
  }
}
