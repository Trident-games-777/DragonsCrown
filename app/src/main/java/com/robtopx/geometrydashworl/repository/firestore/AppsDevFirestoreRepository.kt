package com.robtopx.geometrydashworl.repository.firestore

import com.google.firebase.storage.FirebaseStorage
import com.robtopx.geometrydashworl.repository.Repository
import com.robtopx.geometrydashworl.repository.Repository.Companion.APPS_DEV_KEY_FILE_NAME
import com.robtopx.geometrydashworl.repository.Repository.Companion.MAX_DOWNLOAD_SIZE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class AppsDevFirestoreRepository(
    private val firebaseStorage: FirebaseStorage,
    private val uploadingScope: CoroutineScope
) : Repository {
    override suspend fun downloadFile(): String = suspendCoroutine { completion ->
        firebaseStorage.reference.child(APPS_DEV_KEY_FILE_NAME).getBytes(MAX_DOWNLOAD_SIZE)
            .addOnSuccessListener { byteArray ->
                completion.resume(String(byteArray))
            }.addOnFailureListener { exception ->
                completion.resumeWithException(exception)
            }
    }

    override suspend fun uploadFile(file: String) {
        uploadingScope.launch {
            firebaseStorage.reference.child(APPS_DEV_KEY_FILE_NAME).putBytes(file.toByteArray())
        }
    }
}