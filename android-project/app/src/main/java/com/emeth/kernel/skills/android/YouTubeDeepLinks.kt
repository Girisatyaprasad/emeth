package com.emeth.kernel.skills.android

import android.content.Intent
import android.net.Uri

object YouTubeDeepLinks {
    const val PACKAGE_NAME = "com.google.android.youtube"

    fun getSearchIntent(query: String): Intent {
        val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=${Uri.encode(query)}"))
        i.setPackage(PACKAGE_NAME)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        return i
    }

    fun getPlayVideoIntent(videoId: String): Intent {
        val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=$videoId"))
        i.setPackage(PACKAGE_NAME)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        return i
    }

    fun getPlayVideoUrlIntent(url: String): Intent {
        val i = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        i.setPackage(PACKAGE_NAME)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        return i
    }

    fun getShortsIntent(shortId: String? = null): Intent {
        val url = if (shortId != null) "https://www.youtube.com/shorts/$shortId" else "https://www.youtube.com/shorts/"
        val i = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        i.setPackage(PACKAGE_NAME)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        return i
    }

    fun getWatchLaterIntent(): Intent {
        val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/playlist?list=WL"))
        i.setPackage(PACKAGE_NAME)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        return i
    }

    fun getHistoryIntent(): Intent {
        val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/feed/history"))
        i.setPackage(PACKAGE_NAME)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        return i
    }

    fun getSubscriptionsIntent(): Intent {
        val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/feed/subscriptions"))
        i.setPackage(PACKAGE_NAME)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        return i
    }

    fun getPlaylistsIntent(): Intent {
        val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/feed/playlists"))
        i.setPackage(PACKAGE_NAME)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        return i
    }

    fun getLikedVideosIntent(): Intent {
        val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/playlist?list=LL"))
        i.setPackage(PACKAGE_NAME)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        return i
    }

    fun getYouPageIntent(): Intent {
        val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/feed/you"))
        i.setPackage(PACKAGE_NAME)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        return i
    }

    fun getTrendingIntent(): Intent {
        val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/feed/trending"))
        i.setPackage(PACKAGE_NAME)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        return i
    }

    fun getGamingIntent(): Intent {
        val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/gaming"))
        i.setPackage(PACKAGE_NAME)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        return i
    }

    fun getMusicIntent(): Intent {
        val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://music.youtube.com/"))
        // we might want to launch the specific youtube music app package if needed, but this works
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        return i
    }

    fun getDownloadsIntent(): Intent {
        val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/feed/downloads"))
        i.setPackage(PACKAGE_NAME)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        return i
    }

    fun getNotificationsIntent(): Intent {
        val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/notifications"))
        i.setPackage(PACKAGE_NAME)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        return i
    }

    fun getMoviesIntent(): Intent {
        val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/feed/storefront"))
        i.setPackage(PACKAGE_NAME)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        return i
    }
}
