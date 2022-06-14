package com.robtopx.geometrydashworl.repository.firestore

import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.robtopx.geometrydashworl.data.Params
import com.robtopx.geometrydashworl.repository.Repository.Companion.MAX_DOWNLOAD_SIZE
import com.robtopx.geometrydashworl.repository.Repository.Companion.PARAMS_FILE_NAME
import com.robtopx.geometrydashworl.repository.Repository.Companion.STORAGE_URL
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class ParamsFirestoreRepository {
    private val storage = Firebase.storage(STORAGE_URL)

    suspend fun downloadParams(): Map<String, String> = suspendCoroutine { completion ->
        val params = mutableMapOf<String, String>()
        storage.reference.child(PARAMS_FILE_NAME)
            .getBytes(MAX_DOWNLOAD_SIZE)
            .addOnSuccessListener { byteArray ->
                Json.decodeFromString<Params>(byteArray.decodeToString()).params.map { param ->
                    params.put(param.name, param.value)
                }
                completion.resume(params)
            }
    }
}