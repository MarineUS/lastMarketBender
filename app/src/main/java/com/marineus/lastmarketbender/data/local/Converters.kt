package com.marineus.lastmarketbender.data.local

import androidx.room.TypeConverter
import com.marineus.lastmarketbender.data.model.BusinessType

class Converters {
    @TypeConverter
    fun fromBusinessType(type: BusinessType): String {
        return type.name
    }

    @TypeConverter
    fun toBusinessType(data: String): BusinessType {
        return BusinessType.valueOf(data)
    }

    @TypeConverter
    fun fromList(list: List<String>): String {
        return list.joinToString(",")
    }

    @TypeConverter
    fun toList(data: String): List<String> {
        return if (data.isEmpty()) emptyList() else data.split(",")
    }
}
