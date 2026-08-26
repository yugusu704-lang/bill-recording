package com.localbill.recording.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ==========================================
// 🌸 JAPANESE AIRY & WABI-SABI COLOR PALETTE
// ==========================================

// 1. 和纸宣白基底 (Washi Paper Backgrounds)
val WashiPaperBg = Color(0xFFFAF8F5)          // 温润手工宣纸米白底色
val WashiPaperSubtle = Color(0xFFF3EEE6)      // 柔和纸质浅灰/微容器色
val WashiCardBg = Color(0xFFFFFFFF)           // 纯净和纸卡片纯白
val WashiBorder = Color(0xFFE8E2D6)          // 纸质细线描边 (0.8dp 细腻透光)
val WashiBorderSubtle = Color(0xFFF1ECE3)    // 极淡分界线

// 2. 和风自然与意境主色调 (Nature & Zen Aesthetics)
val MatchaPrimary = Color(0xFF587A63)        // 若草抹茶绿 (主色：沉静、自然、克制)
val MatchaLight = Color(0xFF7D9F88)          // 浅抹茶绿 (微高亮)
val MatchaContainer = Color(0xFFEBF2EC)      // 抹茶淡色容器底色

val SakuraAccent = Color(0xFFD87D88)         // 樱落浅粉 (强调色：优雅、柔和、治愈)
val SakuraSoft = Color(0xFFF7E6E9)           // 樱花柔粉微底色

val YamabukiGold = Color(0xFFD4973B)         // 山吹金 (点缀色：丰盈、温煦、阳光)
val YamabukiLight = Color(0xFFFAF0DE)        // 山吹淡金容器

// 3. 和风水墨墨色 (Sumi Calligraphy Inks)
val SumiInk = Color(0xFF2A2E2B)              // 暖砚浓墨 (主标题、金额、高权重文字)
val SumiSecondary = Color(0xFF6D736E)        // 淡墨中灰 (副标题、分类说明)
val SumiTertiary = Color(0xFFA4AAA5)         // 极淡墨微灰 (时间戳、辅助注释、空态文字)

// 4. 和风意境渐变 (Zen Atmosphere Gradients)
val WashiZenGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFFAF8F5),
        Color(0xFFF4EFE7)
    )
)

val MatchaGradient = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFF587A63),
        Color(0xFF6F947B)
    )
)

val SakuraGlowGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFFF7E6E9),
        Color(0xFFEBF2EC)
    )
)

// 5. 和风分类雅致色系 (Japanese Aesthetic Category Palette)
val JapaneseCategoryColors = listOf(
    0xFF587A63L, // 若草 (餐饮/饮食)
    0xFF5B8A99L, // 浅葱 (学习/科技/知识)
    0xFF4A8576L, // 若竹 (健康/水果/自然)
    0xFF7F7199L, // 桔梗 (交通/出行/远方)
    0xFFD87D88L, // 樱花 (服饰/美容/生活)
    0xFFD4973BL, // 山吹 (日用/居家/生活)
    0xFFC25353L, // 茜红 (娱乐/数码/好物)
    0xFF5C6F84L, // 蓝鼠 (商务/办公/账务)
    0xFF996B54L, // 落叶 (其他/杂项)
    0xFF8C7355L  // 柴染 (文具/手作)
)
