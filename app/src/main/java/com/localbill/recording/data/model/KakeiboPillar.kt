package com.localbill.recording.data.model

import androidx.compose.ui.graphics.Color
import com.localbill.recording.ui.theme.PillarCulture
import com.localbill.recording.ui.theme.PillarCultureLight
import com.localbill.recording.ui.theme.PillarNeeds
import com.localbill.recording.ui.theme.PillarNeedsLight
import com.localbill.recording.ui.theme.PillarUnexpected
import com.localbill.recording.ui.theme.PillarUnexpectedLight
import com.localbill.recording.ui.theme.PillarWants
import com.localbill.recording.ui.theme.PillarWantsLight

/**
 * 日本家计簿 (Kakeibo) 四大消费支柱
 */
enum class KakeiboPillar(
    val code: String,
    val title: String,
    val subTitle: String,
    val color: Color,
    val containerColor: Color,
    val description: String
) {
    NEEDS("NEEDS", "必要", "消費", PillarNeeds, PillarNeedsLight, "生存必须、房租、餐饮水电"),
    WANTS("WANTS", "心动", "浪費", PillarWants, PillarWantsLight, "欲望爱好、休闲娱乐、即兴购物"),
    CULTURE("CULTURE", "文化", "投資", PillarCulture, PillarCultureLight, "书籍学习、自我提升、艺术旅行"),
    UNEXPECTED("UNEXPECTED", "突发", "予想外", PillarUnexpected, PillarUnexpectedLight, "医疗突发、维修人情、意外支出");

    companion object {
        fun fromCode(code: String?): KakeiboPillar {
            return values().find { it.code.equals(code, ignoreCase = true) } ?: NEEDS
        }
    }
}
