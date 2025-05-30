package com.mungkive.application.ui.map

import com.mungkive.application.ui.feed.FeedData
import com.naver.maps.geometry.LatLng

data class MapMarkerData(
    val position: LatLng,
    val feed: FeedData?
)
