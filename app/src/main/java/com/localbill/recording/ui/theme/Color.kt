package com.localbill.recording.ui.theme

import androidx.compose.ui.graphics.Color

// ==========================================
// 北欧人文杂志风 (Nordic Editorial & Paper)
// ==========================================

// 纸张底色与墨水色
val PaperLight = Color(0xFFFAF8F5)          // 温暖米白纸质底色
val PaperSurface = Color(0xFFFFFFFF)        // 纯白表层
val PaperSurfaceSubtle = Color(0xFFF4EFEA)  // 暖沙色微底
val PaperBorder = Color(0xFFE7E2DA)         // 极细纸质分隔线

val InkPrimary = Color(0xFF1C1917)          // 浓郁暖黑墨水字
val InkSecondary = Color(0xFF78716C)        // 暖岩灰副标题字
val InkTertiary = Color(0xFFA8A29E)         // 极淡注释灰
val InkQuaternary = Color(0xFFD6D3D1)       // 极弱底线灰

// 主题点缀色 (北欧陶土与松针绿)
val AccentTerracotta = Color(0xFFC2410C)    // 陶土红（开销强调）
val AccentSageGreen = Color(0xFF15803D)     // 鼠尾草墨绿（主基调）
val AccentWarmAmber = Color(0xFFD97706)     // 暖琥珀
val AccentDeepNavy = Color(0xFF1E293B)      // 普鲁士蓝

// 暗色主题 (深邃黑胶与石墨质感)
val PaperDark = Color(0xFF121212)           // 深邃石墨黑
val SurfaceDark = Color(0xFF1E1E1E)         // 磨砂表面
val SurfaceDarkSubtle = Color(0xFF262626)   // 浅层石墨
val BorderDark = Color(0xFF333333)          // 暗色分界线
val InkDarkPrimary = Color(0xFFF5F5F4)      // 暖白字
val InkDarkSecondary = Color(0xFFA8A29E)    // 浅灰字

// 人文杂志专属分类调色盘 (低纯度、高质感、高级大地色调)
val EditorialCategoryColors = listOf(
    0xFF15803DL, // 森林墨绿 (饮食/自然)
    0xFFB45309L, // 琥珀赭石 (学习/成长)
    0xFFC2410CL, // 赤陶砖红 (餐饮/外出)
    0xFF0369A1L, // 普鲁士蓝 (交通/出行)
    0xFF6B21A8L, // 贵族深紫 (服饰/美妆)
    0xFF0F766EL, // 冷杉青绿 (日用/居家)
    0xFFBE123CL, // 浆果冷红 (娱乐/休闲)
    0xFF4338CAL, // 经典靛蓝 (数码/办公)
    0xFF44403CL, // 石墨炭灰 (其他/杂项)
    0xFF854D0EL  // 暖褐驼色 (医疗/保障)
)
