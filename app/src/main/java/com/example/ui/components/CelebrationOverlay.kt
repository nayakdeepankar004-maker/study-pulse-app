package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ui.theme.PulseAmberLight
import com.example.ui.theme.PulseBlue
import com.example.ui.theme.PulseEmerald
import com.example.ui.theme.PulsePurpleLight
import com.example.ui.theme.PulseRoseLight
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class BlossomParticle(
    val angleRad: Float,
    val distancePx: Float,
    val spinDeg: Float,
    val color: Color,
    val sizeDp: Float
)

@Composable
fun CelebrationOverlay(
    celebrationTitle: String?,
    celebrationNonce: Long,
    onBannerFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val burstProgress = remember { Animatable(0f) }

    val particles = remember(celebrationNonce) {
        if (celebrationNonce == 0L) emptyList()
        else {
            val palette = listOf(
                PulseRoseLight,
                PulseAmberLight,
                PulseEmerald,
                PulseBlue,
                PulsePurpleLight,
                Color(0xFFF9A8D4)
            )
            List(26) { idx ->
                val rnd = Random(celebrationNonce + idx * 31L)
                BlossomParticle(
                    angleRad = (rnd.nextFloat() * 2f * PI).toFloat(),
                    distancePx = 180f + rnd.nextFloat() * 340f,
                    spinDeg = rnd.nextFloat() * 360f - 180f,
                    color = palette[idx % palette.size],
                    sizeDp = 12f + rnd.nextFloat() * 10f
                )
            }
        }
    }

    LaunchedEffect(celebrationNonce) {
        if (celebrationNonce != 0L) {
            burstProgress.snapTo(0f)
            burstProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1350, easing = FastOutSlowInEasing)
            )
        }
    }

    LaunchedEffect(celebrationTitle, celebrationNonce) {
        if (celebrationTitle != null) {
            delay(2600L)
            onBannerFinished()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (particles.isNotEmpty() && burstProgress.value < 1f) {
            val t = burstProgress.value
            val alpha = (1f - t).coerceIn(0f, 1f)
            Canvas(modifier = Modifier.fillMaxSize()) {
                val origin = Offset(size.width * 0.5f, size.height * 0.52f)
                particles.forEach { p ->
                    val dx = cos(p.angleRad) * p.distancePx * t
                    val dy = sin(p.angleRad) * p.distancePx * t - (120f * t)
                    val center = Offset(origin.x + dx, origin.y + dy)
                    val radius = (p.sizeDp * (0.65f + 0.65f * t)).dp.toPx() * 0.5f

                    rotate(degrees = p.spinDeg * t, pivot = center) {
                        for (petal in 0 until 5) {
                            val theta = (petal * 72f) * (PI / 180f)
                            val px = center.x + (cos(theta) * radius * 0.85f).toFloat()
                            val py = center.y + (sin(theta) * radius * 0.85f).toFloat()
                            drawCircle(
                                color = p.color.copy(alpha = alpha),
                                radius = radius * 0.55f,
                                center = Offset(px, py)
                            )
                        }
                        drawCircle(
                            color = PulseAmberLight.copy(alpha = alpha),
                            radius = radius * 0.35f,
                            center = center
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = celebrationTitle != null,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(tween(220)),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(tween(260)),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 12.dp, start = 16.dp, end = 16.dp)
        ) {
            Surface(
                modifier = Modifier.widthIn(max = 400.dp).fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                shadowElevation = 12.dp
            ) {
                Row(
                    modifier = Modifier
                        .background(Brush.horizontalGradient(listOf(Color(0xFF059669), Color(0xFF2563EB))))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = "Congratulations",
                        tint = PulseAmberLight,
                        modifier = Modifier.size(28.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "CONGRATULATIONS!",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "\"${celebrationTitle.orEmpty().take(36)}\" Completed!",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.92f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
