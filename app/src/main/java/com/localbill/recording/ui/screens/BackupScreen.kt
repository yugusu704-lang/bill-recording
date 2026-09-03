package com.localbill.recording.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.localbill.recording.ui.theme.DeepGreen
import com.localbill.recording.ui.theme.TextDark
import com.localbill.recording.ui.theme.TextSecondary
import com.localbill.recording.ui.theme.WarmBorder
import com.localbill.recording.ui.theme.WarmBone
import com.localbill.recording.ui.theme.WarmSurface
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

    val exportFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let { viewModel.exportBackupToFile(context, it) }
    }

    val restoreFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { viewModel.restoreBackupFile(context, it) }
    }

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
        containerColor = WarmBone
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column {
                    Text(
                        text = "设置",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "数据安全、备份与导出都在这里",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            item {
                SettingsSectionTitle(text = "数据安全")
            }

            item {
                SettingsPrivacyCard()
            }

            item {
                SettingsSectionTitle(text = "备份与恢复")
            }

            item {
                SettingsJsonCard(
                    onExport = { viewModel.exportBackupJson() },
                    onRestore = {
                        restoreJsonInput = ""
                        isRestoreDialogVisible = true
                    },
                    onExportFile = { exportFileLauncher.launch("bill_backup_${System.currentTimeMillis()}.json") },
                    onRestoreFile = { restoreFileLauncher.launch(arrayOf("application/json", "text/plain")) }
                )
            }

            item {
                SettingsSectionTitle(text = "数据导出")
            }

            item {
                SettingsCsvCard(onExport = { viewModel.exportToCsv(context) })
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (isJsonResultDialogVisible) {
        AlertDialog(
            onDismissRequest = { isJsonResultDialogVisible = false },
            containerColor = WarmSurface,
            title = { Text(text = "备份已生成", fontWeight = FontWeight.Bold, color = TextDark) },
            text = {
                Column {
                    Text(
                        text = "以下为导出的 JSON 备份数据：",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
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
                    colors = ButtonDefaults.buttonColors(containerColor = DeepGreen)
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("复制到剪贴板")
                }
            },
            dismissButton = {
                TextButton(onClick = { isJsonResultDialogVisible = false }) {
                    Text("关闭", color = TextSecondary)
                }
            }
        )
    }

    if (isRestoreDialogVisible) {
        AlertDialog(
            onDismissRequest = { isRestoreDialogVisible = false },
            containerColor = WarmSurface,
            title = { Text(text = "从 JSON 恢复备份", fontWeight = FontWeight.Bold, color = TextDark) },
            text = {
                Column {
                    Text(
                        text = "请粘贴之前导出的 JSON 备份文本（注意：导入将覆盖当前所有数据）：",
                        style = MaterialTheme.typography.bodySmall,
                        color = DeepGreen
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
                    colors = ButtonDefaults.buttonColors(containerColor = DeepGreen)
                ) {
                    Text("确认覆盖恢复", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { isRestoreDialogVisible = false }) {
                    Text("取消", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun SettingsSectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = TextSecondary,
        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
    )
}

@Composable
private fun SettingsPrivacyCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(WarmSurface)
            .border(1.dp, WarmBorder, RoundedCornerShape(14.dp))
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
                    .background(DeepGreen.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = DeepGreen,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "纯本地离线安全保障",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Text(
                    text = "所有账单与分类仅保存在当前设备本地 SQLite 数据库，无任何远程上传。",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun SettingsJsonCard(
    onExport: () -> Unit,
    onRestore: () -> Unit,
    onExportFile: () -> Unit,
    onRestoreFile: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(WarmSurface)
            .border(1.dp, WarmBorder, RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "JSON 全量备份",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "包含全部主分类、子分类以及所有历史流水账单，适用于换机迁移与归档。",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onExport,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = DeepGreen),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "导出备份", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onRestore,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.CloudDownload, contentDescription = null, tint = TextDark, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "导入恢复", fontWeight = FontWeight.Bold, color = TextDark)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onExportFile,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(text = "导出到文件", fontWeight = FontWeight.Bold, color = TextDark)
                }

                OutlinedButton(
                    onClick = onRestoreFile,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(text = "从文件恢复", fontWeight = FontWeight.Bold, color = DeepGreen)
                }
            }
        }
    }
}

@Composable
private fun SettingsCsvCard(onExport: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(WarmSurface)
            .border(1.dp, WarmBorder, RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "导出 Excel 表格 (CSV)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "生成带 UTF-8 BOM 的标准 CSV 表格文件，可直接用 Excel 或 WPS 开启多维分析。",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onExport,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = WarmBone),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.FileDownload, contentDescription = null, tint = TextDark, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "导出 CSV 表格文件", fontWeight = FontWeight.Bold, color = TextDark)
            }
        }
    }
}




