package com.localbill.recording.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ==========================================
// LIGHTCORE PRISM UI DESIGN SYSTEM
// ==========================================

// 1. 核心色彩 (1. COLOR)
val PrismWhite = Color(0xFFFFFFFF)          // White - 纯白卡片表层
val PrismSnow = Color(0xFFFAFAFC)           // Snow - 极其纯净通透的雪域白背景
val PrismSlate = Color(0xFFF2F4F8)          // Slate - 胶囊按钮/微容器浅灰
val PrismBorder = Color(0xFFE6E8EF)         // Border - 1px 极细微光高精度描边
val PrismBlack = Color(0xFF0A0A0C)          // Black - 深邃高对比墨黑文字/图标

// 次级文本与注释
val PrismTextSecondary = Color(0xFF64748B)  // 蓝调副标题灰
val PrismTextTertiary = Color(0xFF94A3B8)   // 浅注释灰
val PrismTextQuaternary = Color(0xFFCBD5E1) // 极淡占位灰

// 2. 棱镜折射五彩光谱渐变 (2. PRISM GRADIENTS)
val PrismPeach = Color(0xFFFFB86B)          // 珊瑚橙 / 蜜桃
val PrismGold = Color(0xFFFFE57A)           // 闪金 / 柔黄
val PrismCyan = Color(0xFF7EE8FF)           // 青空蓝 / 霓虹青
val PrismLavender = Color(0xFFC588FF)       // 薰衣草紫 / 幻紫
val PrismPink = Color(0xFFFF8ED8)           // 玫粉 / 霓虹粉

// 五彩光谱画刷
val PrismSpectralBrush = Brush.linearGradient(
    colors = listOf(
        PrismPeach,
        PrismGold,
        PrismCyan,
        PrismLavender,
        PrismPink
    )
)

// 蓝紫微光画刷 (用于主折线走势图与核心光晕)
val PrismLuminousBrush = Brush.linearGradient(
    colors = listOf(
        PrismCyan,
        PrismLavender,
        PrismPink
    )
)

// 暖光渐变画刷
val PrismWarmBrush = Brush.linearGradient(
    colors = listOf(
        PrismPeach,
        PrismGold,
        PrismPink
    )
)

// 暗色主题 (Darkcore Prism)
val PrismDarkBackground = Color(0xFF0D0E12)
val PrismDarkSurface = Color(0xFF16181F)
val PrismDarkSlate = Color(0xFF222530)
val PrismDarkBorder = Color(0xFF2E3342)
val PrismDarkTextPrimary = Color(0xFFF8FAFC)
val PrismDarkTextSecondary = Color(0xFF94A3B8)

// 3. Lightcore 分类全彩光谱调色盘
val LightcoreCategoryColors = listOf(
    0xFF0284C7L, // 青空电光蓝 (学习/科技)
    0xFF16A34AL, // 翡翠薄荷绿 (饮食/日常)
    0xFFEA580CL, // 珊瑚炽热橙 (餐饮/外出)
    0xFF7C3AEDL, // 幻光薰衣草紫 (交通/出行)
    0xFFE11D48L, // 霓虹玫红 (服饰/美妆)
    0xFF0D9488L, // 极光冷青 (居家/生活)
    0xFFD97706L, // 琥珀日光 (数码/娱乐)
    0xFF4F46E5L, // 皇家星际靛 (商务/金融)
    0xFF475569L, // 钛金属灰 (其他/杂项)
    0xFFDB2777L  // 樱花霓虹粉 (医疗/健康)
)
