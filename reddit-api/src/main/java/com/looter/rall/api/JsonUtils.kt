package com.looter.rall.api

import org.json.JSONArray
import kotlin.collections.map
import kotlin.ranges.until

fun <T> JSONArray.map(block: JSONArray.(index: Int) -> T) = (0 until length()).map { block(it) }