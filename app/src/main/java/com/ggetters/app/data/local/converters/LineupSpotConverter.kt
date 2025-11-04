package com.ggetters.app.data.local.converters

import androidx.room.TypeConverter
import com.ggetters.app.data.model.LineupSpot
import com.google.common.reflect.TypeToken
import com.google.gson.Gson

class LineupSpotConverter {
    @TypeConverter
    fun fromList(spots: List<LineupSpot>): String =
        Gson().toJson(spots)

    @TypeConverter
    fun toList(json: String?): List<LineupSpot> {
        if (json.isNullOrBlank()) {
            return emptyList()
        }
        return Gson().fromJson(json, object : TypeToken<List<LineupSpot>>() {}.type)
            ?: emptyList()
    }
}
