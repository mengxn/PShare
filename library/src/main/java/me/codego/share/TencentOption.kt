package me.codego.share

import android.app.Activity
import android.os.Bundle
import com.tencent.connect.share.QQShare
import me.codego.share.TencentActivity

abstract class TencentOption() : IShareOption {
    abstract fun getAppId(): String

    open fun getScope(): String {
        return "get_user_info"
    }

    open fun getScene(): Int {
        return QQShare.SHARE_TO_QQ_FLAG_QZONE_ITEM_HIDE
    }

    override fun getTarget(): Class<out Activity> {
        return TencentActivity::class.java
    }

    override fun getOption(): Bundle {
        return Bundle().apply {
            putString(PShare.KEY_TENCENT_APP_ID, getAppId())
            putString(PShare.KEY_TENCENT_SCOPE, getScope())
            putInt(PShare.KEY_SCENE, getScene())
        }
    }
}