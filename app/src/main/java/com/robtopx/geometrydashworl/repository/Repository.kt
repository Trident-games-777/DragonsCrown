package com.robtopx.geometrydashworl.repository

interface Repository {
    suspend fun downloadFile(): String
    suspend fun uploadFile(file: String)

    companion object {
        const val BASE_URL = "zdfuipigctknk.xyz/dragons.php"

        //Remote repository constants
        const val STORAGE_URL = "gs://dragonscrown-8bcd2.appspot.com"
        const val MAX_DOWNLOAD_SIZE = 5L * 1024 * 1024

        const val URL_FILE_NAME = "url.txt"
        const val APPS_DEV_KEY_FILE_NAME = "apps_dev_key.txt"
        const val ONE_SIGNAL_KEY_FILE_NAME = "one_signal_key.txt"
        const val PARAMS_FILE_NAME = "params.json"

        //Local repository constants
        const val PREF_NAME = "prefs"
        const val URL_KEY = "url_key"
        const val DEFAULT_URL = "empty"
    }
}