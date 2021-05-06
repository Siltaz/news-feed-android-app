package com.siltaz.newsfeed

import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.siltaz.newsfeed.databinding.ActivityMainBinding
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity(), NewsItemClicked {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mAdapter: NewsFeedAdapter
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // Fetching user location
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val country = getCountryCodeFromLocation(20.7504968, 73.7287397)
        fetchData(country)

        mAdapter = NewsFeedAdapter(this)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = mAdapter
        binding.countryFlag.setImageResource(if (country == "us") R.drawable.usa else R.drawable.india)
    }

    // Detects country code using latitude and longitude and return 2 letter country code
    private fun getCountryCodeFromLocation(latitude: Double, longitude: Double): String {
        var country = "in"

        try {
            val geoCoder = Geocoder(this, Locale.getDefault())
            val addresses: List<Address> = geoCoder.getFromLocation(latitude, longitude, 1)
            country = addresses[0].countryCode.toLowerCase()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return country
    }

    // Fetches news feeds
    private fun fetchData(country: String) {

        // Original NewsAPI link only works for localhost only so used same API but hosted on someone else's server
        val url = "https://saurav.tech/NewsAPI/top-headlines/category/general/$country.json"

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            {
                val newsJsonArray = it.getJSONArray("articles")
                val newsArray = ArrayList<News>()
                for (i in 0 until newsJsonArray.length()) {
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
        val intent = Intent(this, WebviewActivity::class.java)
        intent.putExtra(WebviewActivity.URL_EXTRA, item.url)
        startActivity(intent)
    }
}