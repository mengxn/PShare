package me.codego.share

import android.os.Bundle
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX
import com.tencent.mm.opensdk.modelmsg.WXMiniProgramObject

abstract class WeixinOption() : IShareOption {

    abstract fun getAppId(): String

    open fun getScope(): String {
        // 只能填 snsapi_userinfo
        return "snsapi_userinfo"
    }

    open fun getScene(): Int {
        return SendMessageToWX.Req.WXSceneSession
    }

    open fun getMiniProgramType(): Int {
        return WXMiniProgramObject.MINIPROGRAM_TYPE_PREVIEW
    }

    override fun getOption(): Bundle {
        return Bundle().apply {
            putString(PShare.KEY_WEIXIN_APP_ID, getAppId())
            putString(PShare.KEY_WEIXIN_SCOPE, getScope())
            putInt(PShare.KEY_WEIXIN_MINIPROGRAM_TYPE, getMiniProgramType())
            putInt(PShare.KEY_SCENE, getScene())
        }
    }

}