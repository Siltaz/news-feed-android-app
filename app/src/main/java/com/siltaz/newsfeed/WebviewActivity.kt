package com.siltaz.newsfeed

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebViewClient
import com.siltaz.newsfeed.databinding.ActivityMainBinding
import com.siltaz.newsfeed.databinding.ActivityWebviewBinding

class WebviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWebviewBinding

    companion object {
        const val URL_EXTRA = "feed_url"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebviewBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val url = intent.getStringExtra(URL_EXTRA)

        binding.webView.webViewClient = WebViewClient()
        binding.webView.apply {
            if (url != null) {
                loadUrl(url)
            }
        }
    }
}