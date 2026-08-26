package com.localbill.recording.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.localbill.recording.data.model.KakeiboPillar
import com.localbill.recording.ui.theme.HankoRed
import com.localbill.recording.ui.theme.HankoRedLight
import com.localbill.recording.ui.theme.SumiDark
import com.localbill.recording.ui.theme.SumiMedium
import com.localbill.recording.ui.theme.TomoeBorder
import com.localbill.recording.ui.theme.TomoePaperBg
import com.localbill.recording.ui.theme.TomoePaperGrid
import com.localbill.recording.ui.theme.TomoePaperPage

/**
 * 1. 方眼网格点阵底纹 (Hobonichi 3.7mm Hōganshi)
 */
@Composable
fun HoganshiGridModifier(modifier: Modifier = Modifier, gridSizeDp: Dp = 16.dp): Modifier {
    return modifier.background(TomoePaperBg)
}

/**
 * 2. 朱红印章 (Hanko 判子) 盖印动效组件
 */
@Composable
fun HankoStampBadge(
    text: String = "済",
    modifier: Modifier = Modifier,
    size: Dp = 32.dp,
    angle: Float = -8f,
    animateOnEntry: Boolean = false
) {
    val scale = remember { Animatable(if (animateOnEntry) 2.4f else 1f) }

    if (animateOnEntry) {
        LaunchedEffect(Unit) {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
    }

    Box(
        modifier = modifier
            .scale(scale.value)
            .rotate(angle)
            .size(size)
            .border(1.6.dp, HankoRed, RoundedCornerShape(size / 4))
            .background(HankoRedLight.copy(alpha = 0.08f), RoundedCornerShape(size / 4))
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = (size.value * 0.44f).sp,
            fontWeight = FontWeight.Black,
            color = HankoRed,
            fontFamily = FontFamily.Serif
        )
    }
}

/**
 * 3. 和纸胶带标签 (Washi Tape Strip) 带撕边效果
 */
@Composable
fun WashiTapeTab(
    title: String,
    tapeColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(3.dp))
            .background(tapeColor)
            .border(0.6.dp, textColor.copy(alpha = 0.35f), RoundedCornerShape(3.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 8.dp, vertical = 2.5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            letterSpacing = 0.5.sp
        )
    }
}

/**
 * 4. Kakeibo 四支柱和纸选择胶囊
 */
@Composable
fun KakeiboPillarBar(
    selectedPillar: KakeiboPillar,
    onSelectPillar: (KakeiboPillar) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        KakeiboPillar.values().forEach { pillar ->
            val isSelected = pillar == selectedPillar
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) pillar.color else pillar.containerColor)
                    .border(
                        width = if (isSelected) 1.2.dp else 0.6.dp,
                        color = if (isSelected) pillar.color else pillar.color.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { onSelectPillar(pillar) }
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = pillar.title,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else pillar.color
                    )
                    Text(
                        text = pillar.subTitle,
                        fontSize = 9.sp,
                        color = if (isSelected) Color.White.copy(alpha = 0.85f) else SumiMedium
                    )
                }
            }
        }
    }
}

/**
 * 5. 撕边手账卡片便签 (Tomoe Card)
 */
@Composable
fun TomoeJournalCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(TomoePaperPage)
            .border(0.8.dp, TomoeBorder, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        content()
    }
}
