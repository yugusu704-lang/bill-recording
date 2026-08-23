package com.localbill.recording.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ==========================================
// CLAUDE WARM CREAM & TRANSLUCENT GLASS
// ==========================================

// 1. Claude 经典温润象牙/米沙纸质背景
val ClaudeWarmBg = Color(0xFFFBF9F5)          // 温暖奶油米白底色
val ClaudeWarmBgSubtle = Color(0xFFF3EDE2)    // 暖沙色微底/微容器
val ClaudeBorder = Color(0xFFE8E2D6)          // 暖调纸质细描边
val ClaudeBorderLight = Color(0xFFF0EBE1)     // 极淡分界线

// 2. 玻璃拟态 (Glassmorphism) 透明材质
val GlassCardBackground = Color(0xCCFFFFFF)   // 80% 不透明度纯白毛玻璃
val GlassCardBackgroundLight = Color(0x99FFFFFF) // 60% 不透明度轻薄毛玻璃
val GlassCardBorder = Color(0xEEFFFFFF)       // 93% 高光玻璃边缘
val GlassPillBackground = Color(0x80F3EDE2)   // 半透明微胶囊底色

// 3. Claude 标志性陶土红与文字墨色
val ClaudeTerracotta = Color(0xFFD96B43)      // Claude 标志性陶土红 (Terracotta)
val ClaudePeach = Color(0xFFF2A97B)           // 柔和珊瑚蜜桃
val ClaudeInk = Color(0xFF2C2B29)             // 温暖石墨黑墨水字
val ClaudeTextSecondary = Color(0xFF73706A)   // 温暖中灰副标题
val ClaudeTextTertiary = Color(0xFFA8A39A)    // 极淡注释灰

// 4. 棱镜与 Claude 暖光交融渐变 (Warm Prism Gradient)
val PrismWarmSpectralBrush = Brush.linearGradient(
    colors = listOf(
        Color(0xFFD96B43), // 陶土红
        Color(0xFFF59E0B), // 暖琥珀金
        Color(0xFF2DD4BF), // 薄荷极光青
        Color(0xFFA855F7), // 梦幻薰衣草紫
        Color(0xFFF43F5E)  // 霓虹珊瑚粉
    )
)

val PrismSoftGlowBrush = Brush.linearGradient(
    colors = listOf(
        Color(0xFFF2A97B),
        Color(0xFFC084FC),
        Color(0xFF818CF8)
    )
)

// 5. Claude 风格分类高级调色盘
val ClaudeCategoryColors = listOf(
    0xFFD96B43L, // 陶土红 (饮食/日常)
    0xFF2563EBL, // 蔚蓝 (学习/科技)
    0xFF059669L, // 翡翠绿 (自然/水果)
    0xFF7C3AEDL, // 紫罗兰 (交通/出行)
    0xFFE11D48L, // 浆果红 (服饰/美妆)
    0xFFD97706L, // 暖琥珀 (日用/居家)
    0xFF0891B2L, // 碧海青 (数码/娱乐)
    0xFF4F46E5L, // 皇家蓝 (商务/财务)
    0xFF57534EL, // 暖石灰 (其他/杂项)
    0xFFDB2777L  // 樱花粉 (医疗/健康)
)
