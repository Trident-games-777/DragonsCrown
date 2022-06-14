package com.robtopx.geometrydashworl.factory

interface Factory<T> {
    fun create(fileName: String): T
}