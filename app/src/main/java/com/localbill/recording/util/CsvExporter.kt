package com.localbill.recording.util

import com.localbill.recording.data.entity.RecordWithCategory
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

object CsvExporter {

    /**
     * 将账单记录导出为标准 CSV 文件（带 UTF-8 BOM 避免 Excel 中文乱码）
     */
    fun exportRecordsToCsv(records: List<RecordWithCategory>, targetFile: File): Boolean {
        return try {
            FileOutputStream(targetFile).use { fos ->
                // 写入 UTF-8 BOM (0xEF, 0xBB, 0xBF) 解决 Excel 打开乱码问题
                fos.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
                OutputStreamWriter(fos, StandardCharsets.UTF_8).use { writer ->
                    // 写入表头
                    writer.write("ID,记账时间,主分类,子分类,金额(元),备注,创建时间\n")

                    for (item in records) {
                        val record = item.record
                        val dateStr = DateTimeUtils.toLocalDateTime(record.timestamp)
                            .format(DateTimeUtils.CSV_DATE_TIME_FORMATTER)
                        val createdStr = DateTimeUtils.toLocalDateTime(record.createdAt)
                            .format(DateTimeUtils.CSV_DATE_TIME_FORMATTER)

                        val mainCat = escapeCsvField(item.displayCategoryName)
                        val subCat = escapeCsvField(item.displaySubCategoryName ?: "")
                        val note = escapeCsvField(record.note)
                        val amount = String.format(java.util.Locale.US, "%.2f", record.amount)

                        writer.write("${record.id},\"$dateStr\",$mainCat,$subCat,$amount,$note,\"$createdStr\"\n")
                    }
                    writer.flush()
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 处理 CSV 特殊字符转义（双引号、逗号、换行符）
     */
    fun escapeCsvField(field: String): String {
        var result = field.replace("\"", "\"\"")
        return if (result.contains(",") || result.contains("\n") || result.contains("\r") || result.contains("\"")) {
            "\"$result\""
        } else {
            "\"$result\""
        }
    }
}
