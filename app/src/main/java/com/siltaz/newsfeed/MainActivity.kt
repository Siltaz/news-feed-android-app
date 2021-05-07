package com.siltaz.newsfeed

import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.birjuvachhani.locus.Locus
import com.google.android.gms.ads.MobileAds
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.get
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.onesignal.OneSignal
import com.siltaz.newsfeed.databinding.ActivityMainBinding
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity(), NewsItemClicked {

    private lateinit var remoteConfig: FirebaseRemoteConfig
    private lateinit var binding: ActivityMainBinding
    private lateinit var mAdapter: NewsFeedAdapter
    private var isAdsEnabled = false

    private val DEFAULTS: Map<String, Any> = mapOf(
        ADS_ENABLED to false
    )

    companion object {
        private const val TAG = "NF::MainActivity"
        private const val ADS_ENABLED = "ads_enabled"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.swipeRefresh.setOnRefreshListener {
            initFirebaseRemoteConfig()
            if (binding.swipeRefresh.isRefreshing) binding.swipeRefresh.isRefreshing = false
        }
        initFirebaseRemoteConfig()
    }

    private fun initFirebaseRemoteConfig() {
        // Firebase RemoteConfig
        remoteConfig = Firebase.remoteConfig
        remoteConfig.setConfigSettingsAsync(remoteConfigSettings {
            minimumFetchIntervalInSeconds = 0
        })
        remoteConfig.setDefaultsAsync(DEFAULTS)
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener(this) {
                isAdsEnabled = remoteConfig[ADS_ENABLED].asBoolean()
                if (isAdsEnabled) MobileAds.initialize(this) {}
                Log.d(TAG, "AdMob Enabled. $isAdsEnabled")

                initOneSignal()
                fetchNewsFeeds()
            }
    }

    private fun fetchNewsFeeds() {

        // Fetching User Location
        Locus.getCurrentLocation(this) { result ->
            var country = "in"
            result.location?.let { country = getCountryCodeFromLocation(it.latitude, it.longitude) }
            result.error?.let { Log.d(TAG, "Location Fetch Failed !! Country set to IN") }

            createFeedsLists(country)
            Log.d(TAG, "Current Location: $country")

            mAdapter = NewsFeedAdapter(this, this)
            binding.recyclerView.layoutManager = LinearLayoutManager(this)
            binding.recyclerView.adapter = mAdapter
            binding.countryFlag.setImageResource(if (country == "us") R.drawable.usa else R.drawable.india)
        }
    }

    private fun initOneSignal() {
        val ONESIGNAL_APP_ID = "709d3073-ef46-4e70-a600-6db69ace8c0d"

        // Enable verbose OneSignal logging to debug issues if needed.
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.ERROR, OneSignal.LOG_LEVEL.NONE);

        // OneSignal Initialization
        OneSignal.initWithContext(this);
        OneSignal.setAppId(ONESIGNAL_APP_ID);

        // Opens WebView for feed received in notification
        OneSignal.setNotificationOpenedHandler { result ->
            val data = result.notification.additionalData
            if (!data.isNull("NEWS_URL")) {
                val intent = Intent(this, WebviewActivity::class.java)
                intent.putExtra(WebviewActivity.URL_EXTRA, data.getString("NEWS_URL"))
                startActivity(intent)
            }
        }
    }

    // Detects country code using latitude and longitude and return 2 letter country code
    private fun getCountryCodeFromLocation(latitude: Double, longitude: Double): String {
        var country = "in"

        try {
            val geoCoder = Geocoder(this, Locale.getDefault())
            val addresses: List<Address> = geoCoder.getFromLocation(latitude, longitude, 1)
            country = addresses[0].countryCode.toLowerCase(Locale.ROOT)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return country
    }

    // Fetches news feeds
    private fun createFeedsLists(country: String) {

        // Original NewsAPI link only works for localhost only so used same API but hosted on someone else's server
        val url = "https://saurav.tech/NewsAPI/top-headlines/category/general/$country.json"

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            {
                val newsJsonArray = it.getJSONArray("articles")
                val newsArray = ArrayList<News>()
                for (i in 0 until newsJsonArray.length()) {
                    if (isAdsEnabled) {
                        if (i % 5 == 0 && i != 0) {
                            val news = News("", "", "", "", "", "")
                            newsArray.add(news)
                        }
                    }

                    val newsJsonObject = newsJsonArray.getJSONObject(i)
                    val news = News(
                        newsJsonObject.getJSONObject("source").getString("name"),
                        newsJsonObject.getString("title"),
                        newsJsonObject.getString("description"),
                        newsJsonObject.getString("url"),
                        newsJsonObject.getString("urlToImage"),
                        newsJsonObject.getString("publishedAt")
                    )
                    newsArray.add(news)
                }
                mAdapter.updatedNews(newsArray)
            },
            {
            }
        )

        VolleySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest)

    }

    override fun onItemClicked(item: News) {
        if (item.url.isNotEmpty()) {
            val intent = Intent(this, WebviewActivity::class.java)
            intent.putExtra(WebviewActivity.URL_EXTRA, item.url)
            startActivity(intent)
        }
    }
}