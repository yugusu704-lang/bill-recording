package com.localbill.recording.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.localbill.recording.ui.theme.MatchaPrimary
import com.localbill.recording.ui.theme.SakuraAccent
import com.localbill.recording.ui.theme.SumiInk
import com.localbill.recording.ui.theme.SumiSecondary
import com.localbill.recording.ui.theme.WashiBorder
import com.localbill.recording.ui.theme.WashiCardBg
import com.localbill.recording.ui.theme.WashiPaperBg
import com.localbill.recording.ui.theme.WashiPaperSubtle
import com.localbill.recording.ui.viewmodel.BackupEvent
import com.localbill.recording.ui.viewmodel.BackupViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun BackupScreen(
    viewModel: BackupViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var isRestoreDialogVisible by remember { mutableStateOf(false) }
    var restoreJsonInput by remember { mutableStateOf("") }
    var isJsonResultDialogVisible by remember { mutableStateOf(false) }
    var currentExportedJson by remember { mutableStateOf("") }

    LaunchedEffect(viewModel.eventFlow) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is BackupEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is BackupEvent.ExportJsonSuccess -> {
                    currentExportedJson = event.jsonContent
                    isJsonResultDialogVisible = true
                }
                is BackupEvent.ExportCsvSuccess -> {}
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = WashiPaperBg
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column {
                    Text(
                        text = "数据管理与备份",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = SumiInk
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "100% 纯本地离线隐私安全，支持 JSON 全量备份与 Excel/CSV 报表导出",
                        style = MaterialTheme.typography.bodySmall,
                        color = SumiSecondary
                    )
                }
            }

            // 隐私宣言和纸卡片
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(WashiCardBg)
                        .border(0.8.dp, WashiBorder, RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MatchaPrimary.copy(alpha = 0.12f))
                                .border(0.8.dp, MatchaPrimary.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = MatchaPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "🌸 纯本地离线安全保障",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = SumiInk
                            )
                            Text(
                                text = "所有账单与分类仅保存在当前设备本地 SQLite 数据库，无任何远程上传。",
                                style = MaterialTheme.typography.bodySmall,
                                color = SumiSecondary
                            )
                        }
                    }
                }
            }

            // JSON 备份与恢复和纸卡片
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(WashiCardBg)
                        .border(0.8.dp, WashiBorder, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "全量数据备份 (JSON)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SumiInk
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "包含全部主分类、子分类以及所有历史流水账单，适用于换机迁移与归档。",
                            style = MaterialTheme.typography.bodySmall,
                            color = SumiSecondary
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { viewModel.exportBackupJson() },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = MatchaPrimary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "导出备份", fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    restoreJsonInput = ""
                                    isRestoreDialogVisible = true
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(imageVector = Icons.Default.CloudDownload, contentDescription = null, tint = SumiInk, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "导入恢复", fontWeight = FontWeight.Bold, color = SumiInk)
                            }
                        }
                    }
                }
            }

            // CSV 导出和纸卡片
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(WashiCardBg)
                        .border(0.8.dp, WashiBorder, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "导出为 Excel 表格 (CSV)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SumiInk
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "生成带 UTF-8 BOM 的标准 CSV 表格文件，可直接用 Excel 或 WPS 开启多维分析。",
                            style = MaterialTheme.typography.bodySmall,
                            color = SumiSecondary
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = { viewModel.exportToCsv(context) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = WashiPaperSubtle),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.FileDownload, contentDescription = null, tint = SumiInk, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "导出 CSV 表格文件", fontWeight = FontWeight.Bold, color = SumiInk)
                        }
                    }
                }
            }
        }
    }

    if (isJsonResultDialogVisible) {
        AlertDialog(
            onDismissRequest = { isJsonResultDialogVisible = false },
            containerColor = Color.White,
            title = { Text(text = "备份已生成", fontWeight = FontWeight.Bold, color = SumiInk) },
            text = {
                Column {
                    Text(
                        text = "以下为导出的 JSON 备份数据：",
                        style = MaterialTheme.typography.bodySmall,
                        color = SumiSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = currentExportedJson,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("BillBackup", currentExportedJson)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "备份内容已复制到剪贴板", Toast.LENGTH_SHORT).show()
                        isJsonResultDialogVisible = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MatchaPrimary)
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("复制到剪贴板")
                }
            },
            dismissButton = {
                TextButton(onClick = { isJsonResultDialogVisible = false }) {
                    Text("关闭", color = SumiSecondary)
                }
            }
        )
    }

    if (isRestoreDialogVisible) {
        AlertDialog(
            onDismissRequest = { isRestoreDialogVisible = false },
            containerColor = Color.White,
            title = { Text(text = "从 JSON 恢复备份", fontWeight = FontWeight.Bold, color = SumiInk) },
            text = {
                Column {
                    Text(
                        text = "请粘贴之前导出的 JSON 备份文本（注意：导入将覆盖当前所有数据）：",
                        style = MaterialTheme.typography.bodySmall,
                        color = SakuraAccent
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = restoreJsonInput,
                        onValueChange = { restoreJsonInput = it },
                        placeholder = { Text("在此粘贴 JSON 文本...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (restoreJsonInput.isBlank()) {
                            Toast.makeText(context, "请输入有效的 JSON 内容", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        viewModel.restoreFromJson(restoreJsonInput.trim())
                        isRestoreDialogVisible = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SakuraAccent)
                ) {
                    Text("确认覆盖恢复", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { isRestoreDialogVisible = false }) {
                    Text("取消", color = SumiSecondary)
                }
            }
        )
    }
}
