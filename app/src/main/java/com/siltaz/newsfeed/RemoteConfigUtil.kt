package com.siltaz.newsfeed

import android.util.Log
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings

object RemoteConfigUtil {
    private const val TAG = "RemoteConfigUtil"
    private const val ADS_ENABLED = "ads_enabled"

    private val DEFAULTS: HashMap<String, Any> = hashMapOf(
        ADS_ENABLED to false
    )

    private lateinit var remoteConfig: FirebaseRemoteConfig

    fun init(){
        remoteConfig = getFirebaseRemoteConfig()
    }

    private fun getFirebaseRemoteConfig(): FirebaseRemoteConfig {
        val remoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = if(BuildConfig.DEBUG){0}else{60*60}
        }

        remoteConfig.apply {
            setConfigSettingsAsync(configSettings)
            setDefaultsAsync(DEFAULTS)
            fetchAndActivate().addOnCompleteListener{
                Log.d(TAG, "Remote config fetch complete")
            }
        }

        return remoteConfig
    }

    fun getAdsEnabled(): Boolean = remoteConfig.getBoolean(ADS_ENABLED)

}