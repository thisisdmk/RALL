package com.looter.rall.data.api.models

import org.json.JSONArray

fun <T> JSONArray.map(block: JSONArray.(index: Int) -> T) = (0 until length()).map { block(it) }