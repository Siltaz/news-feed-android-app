package com.siltaz.newsfeed

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.nativead.NativeAdView


class NewsFeedAdapter(private val listener: NewsItemClicked, private val mCtx: Context) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items: ArrayList<News> = ArrayList()
    private val itemNews = 1
    private val itemAd = 2

    override fun getItemViewType(position: Int): Int {
        return if (items[position].title.isEmpty()) itemAd else itemNews
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return if (viewType == itemNews) {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.item_news, parent, false)
            val viewHolder = NewsViewHolder(view)
            view.setOnClickListener {
                listener.onItemClicked(items[viewHolder.adapterPosition])
            }
            viewHolder
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_ads, parent, false) as NativeAdView
            val viewHolder = AdsViewHolder(view)
            viewHolder
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (holder) {
            is AdsViewHolder -> {
                val adLoader = AdLoader.Builder(mCtx, "ca-app-pub-3940256099942544/2247696110")
                    .forNativeAd { ad ->
                        holder.adTitle.text = ad.headline
                        holder.adDescription.text = ad.body
                        holder.adButton.text = ad.callToAction
                        holder.adImage.setImageDrawable(ad.images[0].drawable)
                    }
                    .build()

                adLoader.loadAd(AdRequest.Builder().build())

            }
            is NewsViewHolder -> {
                val currentItem = items[position]
                holder.title.text = currentItem.title
                holder.description.text = currentItem.description
                holder.source.text = currentItem.source
                Glide.with(holder.itemView.context).load(currentItem.imageUrl).into(holder.image)
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun updatedNews(updatedNews: ArrayList<News>) {
        items.clear()
        items.addAll(updatedNews)

        notifyDataSetChanged()
    }
}

class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val title: TextView = itemView.findViewById(R.id.title)
    val image: ImageView = itemView.findViewById(R.id.image)
    val description: TextView = itemView.findViewById(R.id.description)
    val source: TextView = itemView.findViewById(R.id.source)
}

class AdsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val adTitle: TextView = itemView.findViewById(R.id.ad_title)
    val adImage: ImageView = itemView.findViewById(R.id.ad_media)
    val adDescription: TextView = itemView.findViewById(R.id.ad_description)
    val adButton: Button = itemView.findViewById(R.id.ad_button)
}

interface NewsItemClicked {
    fun onItemClicked(item: News)
}