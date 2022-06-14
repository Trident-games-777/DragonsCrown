package com.robtopx.geometrydashworl.data

import kotlinx.serialization.Serializable

@Serializable
data class Param(
    val name: String,
    val value: String
)