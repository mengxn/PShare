package me.codego.share

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.content.FileProvider
import com.sina.weibo.sdk.api.ImageObject
import com.sina.weibo.sdk.api.MultiImageObject
import com.sina.weibo.sdk.api.TextObject
import com.sina.weibo.sdk.api.WebpageObject
import com.sina.weibo.sdk.api.WeiboMultiMessage
import com.sina.weibo.sdk.auth.AuthInfo
import com.sina.weibo.sdk.auth.Oauth2AccessToken
import com.sina.weibo.sdk.auth.WbAuthListener
import com.sina.weibo.sdk.common.UiError
import com.sina.weibo.sdk.openapi.IWBAPI
import com.sina.weibo.sdk.openapi.WBAPIFactory
import com.sina.weibo.sdk.share.WbShareCallback
import java.io.File
import java.util.UUID

/**
 * 微博
 * @author mengxn
 * @date 2021/8/11
 */
class WeiboActivity : Activity(), WbShareCallback, WbAuthListener {

    companion object {
        private const val THUMB_LENGTH_LIMIT = 32768

        const val KEY_ERROR = "error"
    }

    private lateinit var mWbApi: IWBAPI
    private var mType: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mWbApi = WBAPIFactory.createWBAPI(this)
        if (!mWbApi.isWBAppInstalled) {
            toast("手机中没有查找到「微博」")
            finish()
            return
        }
        val appKey = intent.getStringExtra(PShare.KEY_WEIBO_APP_KEY)
        val redirectUrl = intent.getStringExtra(PShare.KEY_WEIBO_REDIRECT_URI)
        val scope = intent.getStringExtra(PShare.KEY_WEIBO_SCOPE)
        // register
        val authInfo = AuthInfo(this, appKey, redirectUrl, scope)
        mWbApi.registerApp(this, authInfo)
        // start task
        mType = intent.getIntExtra(PShare.KEY_TYPE, 0)
        val result = when (mType) {
            PShare.TYPE_LOGIN -> {
                mWbApi.authorize(this, this)
                true
            }
            PShare.TYPE_SHARE_IMAGE -> shareImage(intent)
            PShare.TYPE_SHARE_URL -> shareUrl(intent)
            else -> false
        }
        if (!result) {
            finish()
        }
    }

    private fun shareImage(intent: Intent): Boolean {
        var multiMessage: WeiboMultiMessage? = null
        val imgArr = intent.getStringArrayExtra(PShare.KEY_IMAGE_PATH_LIST)
        // 多图模式
        if (!imgArr.isNullOrEmpty() && imgArr.size > 1 && mWbApi.isWBAppSupportMultipleImage) {
            val uriList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // 在多图分享时图片资源仅支持共享路径 data/应用包名/files/ 下图片文件
                val authority = "${packageName}.fileProvider"
                imgArr.map { FileProvider.getUriForFile(this, authority, File(it)) }
            } else {
                imgArr.map { Uri.fromFile(File(it)) }
            }
            val multiImageObject = MultiImageObject().apply {
                this.imageList = uriList as ArrayList<Uri>
            }
            multiMessage = WeiboMultiMessage().apply {
                this.multiImageObject = multiImageObject
            }
        }
        if (multiMessage == null) {
            // 单图本地文件
            val imgPath = if (imgArr?.isNotEmpty() == true) imgArr[0] else intent.getStringExtra(PShare.KEY_IMAGE_PATH)
            if (imgPath?.isNotEmpty() == true) {
                val imageObj = ImageObject().apply {
                    this.setImagePath(imgPath)
                }
                multiMessage = WeiboMultiMessage().apply {
                    this.imageObject = imageObj
                }
            }
        }
        if (multiMessage == null) {
            // 单图
            val image = intent.getParcelableExtra<Bitmap>(PShare.KEY_IMAGE)
            if (image != null) {
                val imageObj = ImageObject().apply {
                    this.setImageData(image)
                }
                multiMessage = WeiboMultiMessage().apply {
                    this.imageObject = imageObj
                }
            }
        }
        if (multiMessage != null) {
            mWbApi.shareMessage(this, multiMessage, true)
            return true
        }
        Log.e("Share", "image is empty")
        return false
    }

    private fun shareUrl(intent: Intent): Boolean {
        val textObj = TextObject().apply {
            this.text = intent.getStringExtra(PShare.KEY_TEXT)
        }
        val bitmap = intent.getParcelableExtra<Bitmap>(PShare.KEY_IMAGE)
            ?: BitmapFactory.decodeFile(intent.getStringExtra(PShare.KEY_IMAGE_PATH))
        val webObj = WebpageObject().apply {
            this.identify = UUID.randomUUID().toString()
            this.actionUrl = intent.getStringExtra(PShare.KEY_URL)
            this.title = intent.getStringExtra(PShare.KEY_TITLE)
            this.description = intent.getStringExtra(PShare.KEY_SUMMARY)
            this.thumbData = bitmap.toBytes(THUMB_LENGTH_LIMIT)
            this.defaultText = "share"
        }
        mWbApi.shareMessage(this, WeiboMultiMessage().apply {
            this.mediaObject = webObj
            this.textObject = textObj
        }, true)
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (mType) {
            PShare.TYPE_LOGIN -> mWbApi.authorizeCallback(this, requestCode, resultCode, data)
            else -> mWbApi.doResultIntent(data, this)
        }
    }

    /**
     * 授权登录成功回调
     *
     * @param oauth2AccessToken
     */
    override fun onComplete(oauth2AccessToken: Oauth2AccessToken) {
        setResult(RESULT_OK, Intent().apply {
            putExtras(Bundle().apply {
                this.putString(Oauth2AccessToken.KEY_UID, oauth2AccessToken.uid)
                this.putString(Oauth2AccessToken.KEY_ACCESS_TOKEN, oauth2AccessToken.accessToken)
                this.putString(Oauth2AccessToken.KEY_REFRESH_TOKEN, oauth2AccessToken.refreshToken)
                this.putString(Oauth2AccessToken.KEY_SCREEN_NAME, oauth2AccessToken.screenName)
                this.putLong(Oauth2AccessToken.KEY_EXPIRES_IN, oauth2AccessToken.expiresTime)
            })
        })
        finish()
    }

    /**
     * 分享成功回调
     *
     */
    override fun onComplete() {
        setResult(RESULT_OK)
        finish()
    }

    override fun onError(err: UiError?) {
        Log.e("Share", "code:${err?.errorCode}, msg:${err?.errorMessage}, detail:${err?.errorDetail}")
        setResult(RESULT_CANCELED, Intent().apply {
            putExtra(KEY_ERROR, err?.errorMessage)
        })
        finish()
    }

    override fun onCancel() {
        setResult(RESULT_CANCELED)
        finish()
    }
}