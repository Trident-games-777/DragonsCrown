package com.robtopx.geometrydashworl.repository.local

import android.content.SharedPreferences
import com.robtopx.geometrydashworl.repository.Repository
import com.robtopx.geometrydashworl.repository.Repository.Companion.BASE_URL
import com.robtopx.geometrydashworl.repository.Repository.Companion.DEFAULT_URL
import com.robtopx.geometrydashworl.repository.Repository.Companion.URL_KEY

class UrlLocalRepository(
    private val sharedPref: SharedPreferences,
) : Repository {
    override suspend fun downloadFile(): String = sharedPref.getString(URL_KEY, DEFAULT_URL)!!

    override suspend fun uploadFile(file: String) {
        val current = downloadFile()
        if (current.contains(BASE_URL)
            || current == DEFAULT_URL
        ) {
            with(sharedPref.edit()) {
                putString(URL_KEY, file)
                apply()
            }
        }
    }
}