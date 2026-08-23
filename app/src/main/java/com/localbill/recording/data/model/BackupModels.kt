package com.localbill.recording.data.model

import com.google.gson.annotations.SerializedName

data class CategoryBackupDto(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("iconName") val iconName: String,
    @SerializedName("colorHex") val colorHex: Long,
    @SerializedName("parentId") val parentId: Long?,
    @SerializedName("isBuiltIn") val isBuiltIn: Boolean,
    @SerializedName("sortOrder") val sortOrder: Int
)

data class RecordBackupDto(
    @SerializedName("id") val id: Long,
    @SerializedName("amount") val amount: Double,
    @SerializedName("categoryId") val categoryId: Long,
    @SerializedName("subCategoryId") val subCategoryId: Long?,
    @SerializedName("note") val note: String,
    @SerializedName("timestamp") val timestamp: Long,
    @SerializedName("imagePath") val imagePath: String?,
    @SerializedName("createdAt") val createdAt: Long
)

data class BackupPayload(
    @SerializedName("version") val version: Int = 1,
    @SerializedName("exportTimestamp") val exportTimestamp: Long = System.currentTimeMillis(),
    @SerializedName("appVersion") val appVersion: String = "1.0.0",
    @SerializedName("categories") val categories: List<CategoryBackupDto>,
    @SerializedName("records") val records: List<RecordBackupDto>
)
