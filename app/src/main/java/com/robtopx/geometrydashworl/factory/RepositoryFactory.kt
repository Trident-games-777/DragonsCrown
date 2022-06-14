package com.robtopx.geometrydashworl.factory

import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.robtopx.geometrydashworl.repository.Repository
import com.robtopx.geometrydashworl.repository.Repository.Companion.APPS_DEV_KEY_FILE_NAME
import com.robtopx.geometrydashworl.repository.Repository.Companion.ONE_SIGNAL_KEY_FILE_NAME
import com.robtopx.geometrydashworl.repository.Repository.Companion.URL_FILE_NAME
import com.robtopx.geometrydashworl.repository.firestore.AppsDevFirestoreRepository
import com.robtopx.geometrydashworl.repository.firestore.OneSignalFirestoreRepository
import com.robtopx.geometrydashworl.repository.firestore.UrlFirestoreRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class RepositoryFactory(
) : Factory<Repository> {
    private val storage = Firebase.storage(Repository.STORAGE_URL)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val urlFirestoreRepository = UrlFirestoreRepository(
        firebaseStorage = storage,
        uploadingScope = scope
    )
    private val appsDevFirestoreRepository = AppsDevFirestoreRepository(
        firebaseStorage = storage,
        uploadingScope = scope
    )
    private val oneSignalFirestoreRepository = OneSignalFirestoreRepository(
        firebaseStorage = storage,
        uploadingScope = scope
    )

    override fun create(fileName: String): Repository {
        return when (fileName) {
            URL_FILE_NAME -> urlFirestoreRepository
            APPS_DEV_KEY_FILE_NAME -> appsDevFirestoreRepository
            ONE_SIGNAL_KEY_FILE_NAME -> oneSignalFirestoreRepository
            else -> throw Exception("No repository for such file")
        }
    }
}