package com.mora.matritech.utils

import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.put

fun JsonObjectBuilder.putNullable(key: String, value: Any?) {
    when (value) {
        null -> put(key, JsonNull)
        is Int -> put(key, value)
        is Long -> put(key, value)
        is Double -> put(key, value)
        is Float -> put(key, value)
        is Boolean -> put(key, value)
        is String -> put(key, value)
        else -> error("Tipo no soportado en putNullable: ${value::class}")
    }
}