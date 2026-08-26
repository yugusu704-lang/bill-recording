package com.localbill.recording.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// =========================================================================
// 🌸 ほぼ日手帳 (Hobonichi Techo) × 日本家計簿 (Kakeibo) 设计色彩系统
// =========================================================================

// 1. 巴川纸 (Tomoe River Paper) 与手账纸质底色
val TomoePaperBg = Color(0xFFFAF7F2)            // 巴川纸温润象牙米白
val TomoePaperPage = Color(0xFFFFFFFF)          // 手账内页纯白
val TomoePaperGrid = Color(0x122D2B2A)          // 3.7mm 方眼点阵/浅灰网格线
val TomoePaperSpine = Color(0xFFEADBCE)         // 账本折页与书脊阴影
val TomoeBorder = Color(0xFFE4DCD0)             // 纸质边缘细腻细线

// 兼容别名
val WashiPaperBg = TomoePaperBg
val WashiPaperSubtle = Color(0xFFF3EEE6)
val WashiCardBg = TomoePaperPage
val WashiBorder = TomoeBorder

// 2. 朱红印章 (Hanko 判子)
val HankoRed = Color(0xFFC83838)                // 朱红印泥原色
val HankoRedLight = Color(0x22C83838)           // 浅朱红盖印晕开
val HankoSealBg = Color(0xFFFFF0F0)             // 印章微容器底

// 3. 日本家计簿 (Kakeibo) 消费四支柱专属色
val PillarNeeds = Color(0xFF5B7382)             // 【必要·消費】 灰蓝/灰石
val PillarNeedsLight = Color(0xFFEBF0F3)

val PillarWants = Color(0xFFD47A88)             // 【心动·浪費】 落樱珊瑚粉
val PillarWantsLight = Color(0xFFFBF0F2)

val PillarCulture = Color(0xFF5D8469)           // 【文化·投資】 抹茶/若草绿
val PillarCultureLight = Color(0xFFEDF4EF)

val PillarUnexpected = Color(0xFFD4973B)        // 【突发·予想外】 山吹金
val PillarUnexpectedLight = Color(0xFFFCF5EB)

// 常用别名兼容
val MatchaPrimary = PillarCulture
val SakuraAccent = PillarWants
val SakuraSoft = PillarWantsLight
val YamabukiGold = PillarUnexpected

// 4. 和纸胶带 (Washi Tape) 撕边半透明色彩
val WashiTapeMatcha = Color(0xCCE2EFE5)         // 抹茶和纸胶带
val WashiTapeSakura = Color(0xCCFBE8EB)         // 樱落和纸胶带
val WashiTapeGold = Color(0xCCFDF3E3)           // 暖金和纸胶带
val WashiTapeBlue = Color(0xCCE6EFF3)           // 浅葱和纸胶带

// 5. 传统手写墨水字阶 (Handwriting Sumi Ink)
val SumiDark = Color(0xFF2B2826)                // 浓墨深石褐
val SumiMedium = Color(0xFF6B6560)              // 淡墨中灰
val SumiLight = Color(0xFFA8A29C)               // 极淡铅笔灰

// 别名兼容
val SumiInk = SumiDark
val SumiSecondary = SumiMedium
val SumiTertiary = SumiLight

// 6. 和风雅致分类调色板
val JapaneseCategoryColors = listOf(
    0xFF5D8469L, // 若草 (餐饮)
    0xFF5B7382L, // 浅葱 (学习)
    0xFF4A8576L, // 若竹 (健康/自然)
    0xFF7F7199L, // 桔梗 (出行)
    0xFFD47A88L, // 樱花 (服饰/生活)
    0xFFD4973BL, // 山吹 (居家)
    0xFFC83838L, // 朱红 (娱乐)
    0xFF64748BL, // 蓝灰 (办公)
    0xFF9A7B66L, // 栗皮 (其他)
    0xFF8C7355L  // 柴染 (手作)
)
