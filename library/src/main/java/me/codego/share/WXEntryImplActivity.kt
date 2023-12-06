package me.codego.share

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentActivity
import com.tencent.mm.opensdk.modelbase.BaseReq
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.modelbiz.WXLaunchMiniProgram
import com.tencent.mm.opensdk.modelmsg.SendAuth
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX
import com.tencent.mm.opensdk.modelmsg.WXImageObject
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage
import com.tencent.mm.opensdk.modelmsg.WXMiniProgramObject
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler
import com.tencent.mm.opensdk.openapi.WXAPIFactory

/**
 * 微信登录、分享等功能
 * 这里是微信的具体实现类，因为微信规定必须在主程序包名下，所以开发者需要新建**wxapi.WXEntryActivity**，继承于WXEntryImplActivity
 *
 * @author mengxn
 * @date 12/18/20
 */
open class WXEntryImplActivity : FragmentActivity(), IWXAPIEventHandler {

    private lateinit var mWxApi: IWXAPI

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appId = intent?.getStringExtra(PShare.KEY_WEIXIN_APP_ID)
        if (appId.isNullOrBlank()) {
            Log.e("PShare", "weixin app id not config")
            return
        }
        mWxApi = WXAPIFactory.createWXAPI(this, appId, true)
        if (!mWxApi.isWXAppInstalled) {
            toast("手机中没有查找到「微信」")
            finish()
            return
        }
        mWxApi.registerApp(appId)

        val result = when (intent.getIntExtra(PShare.KEY_TYPE, 0)) {
            PShare.TYPE_LOGIN -> login(intent.extras)
            PShare.TYPE_SHARE_IMAGE -> shareImage(intent.extras)
            PShare.TYPE_SHARE_URL -> shareUrl(intent.extras)
            PShare.TYPE_MINI_PROGRAM -> openMiniProgram(intent.extras)
            PShare.TYPE_SHARE_MINI_PROGRAM -> shareMiniProgram(intent.extras)
            else -> false
        }
        // 是否需要保留当前页
        if (!result) {
            finish()
        }
    }

    private fun login(bundle: Bundle?): Boolean {
        if (bundle == null) return false
        return mWxApi.sendReq(SendAuth.Req().apply {
            this.scope = bundle.getString(PShare.KEY_WEIXIN_SCOPE)
            this.state = packageName
        })
    }

    private fun shareImage(bundle: Bundle?): Boolean {
        if (bundle == null) return false
        val bitmap = bundle.getParcelable<Bitmap>(PShare.KEY_IMAGE)
            ?: BitmapFactory.decodeFile(bundle.getString(PShare.KEY_IMAGE_PATH))
            ?: return false
        val msg = WXMediaMessage().apply {
            this.mediaObject = WXImageObject(bitmap)
            this.thumbData = bitmap.toBytes(WXMediaMessage.THUMB_LENGTH_LIMIT)
        }
        bitmap.recycle()
        return mWxApi.sendReq(createMessageReq(msg).apply {
            this.scene = bundle.getInt(PShare.KEY_SCENE, SendMessageToWX.Req.WXSceneSession)
        })
    }

    private fun shareUrl(bundle: Bundle?): Boolean {
        if (bundle == null) return false
        val bitmap = bundle.getParcelable<Bitmap>(PShare.KEY_IMAGE)
            ?: BitmapFactory.decodeFile(bundle.getString(PShare.KEY_IMAGE_PATH))
        val msg = WXMediaMessage().apply {
            this.mediaObject = WXWebpageObject(bundle.getString(PShare.KEY_URL))
            this.title = bundle.getString(PShare.KEY_TITLE)
            this.description = bundle.getString(PShare.KEY_SUMMARY)?.take(WXMediaMessage.DESCRIPTION_LENGTH_LIMIT)
            this.thumbData = bitmap.toBytes(WXMediaMessage.THUMB_LENGTH_LIMIT)
            bitmap?.recycle()
        }
        return mWxApi.sendReq(createMessageReq(msg).apply {
            this.scene = bundle.getInt(PShare.KEY_SCENE, SendMessageToWX.Req.WXSceneSession)
        })
    }

    private fun openMiniProgram(bundle: Bundle?): Boolean {
        if (bundle == null) {
            return false
        }
        mWxApi.sendReq(WXLaunchMiniProgram.Req().apply {
            this.userName = bundle.getString(PShare.KEY_MINI_ID)
            this.path = bundle.getString(PShare.KEY_MINI_PATH)
            this.miniprogramType = bundle.getInt(PShare.KEY_WEIXIN_MINIPROGRAM_TYPE)
        })
        // 打开小程序不需要回调数据，直接返回 False
        return false
    }

    private fun shareMiniProgram(bundle: Bundle?): Boolean {
        if (bundle == null) {
            return false
        }
        val miniProgram = WXMiniProgramObject().apply {
            this.userName = bundle.getString(PShare.KEY_MINI_ID)
            this.path = bundle.getString(PShare.KEY_MINI_PATH)
            this.webpageUrl = bundle.getString(PShare.KEY_URL)
            this.miniprogramType = bundle.getInt(PShare.KEY_WEIXIN_MINIPROGRAM_TYPE)
        }
        val bitmap = bundle.getParcelable<Bitmap>(PShare.KEY_IMAGE)
            ?: BitmapFactory.decodeFile(bundle.getString(PShare.KEY_IMAGE_PATH))
            ?: return false
        val msg = WXMediaMessage().apply {
            this.mediaObject = miniProgram
            this.title = bundle.getString(PShare.KEY_TITLE)
            this.description = bundle.getString(PShare.KEY_SUMMARY)?.take(WXMediaMessage.DESCRIPTION_LENGTH_LIMIT)
            this.thumbData = bitmap.toBytes(WXMediaMessage.MINI_PROGRAM__THUMB_LENGHT)
            bitmap.recycle()
        }
        return mWxApi.sendReq(createMessageReq(msg).apply {
            this.scene = bundle.getInt(PShare.KEY_SCENE, SendMessageToWX.Req.WXSceneSession)
        })
    }

    private fun createMessageReq(message: WXMediaMessage) = SendMessageToWX.Req().apply {
        this.transaction = message.toString()
        this.message = message
        this.scene = SendMessageToWX.Req.WXSceneSession
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        mWxApi.handleIntent(intent, this)
    }

    override fun onReq(p0: BaseReq) {

    }

    override fun onResp(baseResp: BaseResp) {
        val intent = Intent()
        val bundle = Bundle()
        baseResp.toBundle(bundle)
        intent.putExtras(bundle)
        setResult(RESULT_OK, intent)
        finish()
    }

}