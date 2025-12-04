package com.example.proyecto_notas.data.local

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromString(value: String?): List<String> {
        // Si el valor es nulo o está vacío, devuelve una lista vacía
        return value?.split(',')
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() } ?: emptyList()
    }

    @TypeConverter
    fun fromList(list: List<String>?): String {
        // Une la lista en un solo String separado por comas
        return list?.joinToString(",") ?: ""
    }
}
