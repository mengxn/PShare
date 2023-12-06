package me.codego.share

import android.app.Activity
import android.os.Bundle

abstract class WeiboOption() : IShareOption {
    abstract fun getAppKey(): String

    abstract fun getRedirectURI(): String

    abstract fun getScope(): String

    override fun getTarget(): Class<out Activity> {
        return WeiboActivity::class.java
    }

    override fun getOption(): Bundle {
        return Bundle().apply {
            putString(PShare.KEY_WEIBO_APP_KEY, getAppKey())
            putString(PShare.KEY_WEIBO_REDIRECT_URI, getRedirectURI())
            putString(PShare.KEY_WEIBO_SCOPE, getScope())
        }
    }
}