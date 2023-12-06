package me.codego.share

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.tencent.connect.UserInfo
import com.tencent.connect.share.QQShare
import com.tencent.tauth.IUiListener
import com.tencent.tauth.Tencent
import com.tencent.tauth.UiError
import org.json.JSONObject

/**
 * QQ互联
 * @author mengxn
 * @date 2021/8/10
 */
class TencentActivity : Activity(), IUiListener {

    private lateinit var mTencent: Tencent

    /**
     * 操作类型
     */
    private var mType: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appId = intent?.getStringExtra(PShare.KEY_TENCENT_APP_ID)
        if (appId.isNullOrBlank()) {
            Log.e("PShare", "tencent app id not config")
            return
        }
        mTencent = Tencent.createInstance(appId, applicationContext, "${packageName}.fileProvider")
        if (!mTencent.isQQInstalled(this)) {
            toast("手机中没有查找到「QQ」")
            finish()
            return
        }
        mType = intent.getIntExtra(PShare.KEY_TYPE, 0)
        when (mType) {
            PShare.TYPE_LOGIN -> login(intent)
            PShare.TYPE_SHARE_IMAGE -> shareImage(intent)
            PShare.TYPE_SHARE_URL -> shareUrl(intent)
            else -> {
                toast("参数有误")
                finish()
            }
        }
    }

    private fun login(intent: Intent) {
        if (mTencent.isSessionValid) {
            // request user info
            UserInfo(this, mTencent.qqToken).getUserInfo(this)
        } else {
            val scope = intent.getStringExtra(PShare.KEY_TENCENT_SCOPE)
            mTencent.login(this, scope, this)
        }
    }

    private fun shareImage(intent: Intent) {
        val bundle = Bundle().apply {
            putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_IMAGE)
            putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, intent.getStringExtra(PShare.KEY_IMAGE_PATH))
            putInt(QQShare.SHARE_TO_QQ_EXT_INT, intent.getIntExtra(PShare.KEY_SCENE, QQShare.SHARE_TO_QQ_FLAG_QZONE_ITEM_HIDE))
        }
        mTencent.shareToQQ(this, bundle, this)
    }

    private fun shareUrl(intent: Intent) {
        val bundle = Bundle().apply {
            putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT)
            putString(QQShare.SHARE_TO_QQ_TARGET_URL, intent.getStringExtra(PShare.KEY_URL))
            putString(QQShare.SHARE_TO_QQ_TITLE, intent.getStringExtra(PShare.KEY_TITLE)?.take(QQShare.QQ_SHARE_TITLE_MAX_LENGTH))
            putString(QQShare.SHARE_TO_QQ_SUMMARY, intent.getStringExtra(PShare.KEY_SUMMARY)?.take(QQShare.QQ_SHARE_SUMMARY_MAX_LENGTH))
            putString(QQShare.SHARE_TO_QQ_IMAGE_URL, intent.getStringExtra(PShare.KEY_IMAGE_PATH))
            putInt(QQShare.SHARE_TO_QQ_EXT_INT, intent.getIntExtra(PShare.KEY_SCENE, QQShare.SHARE_TO_QQ_FLAG_QZONE_ITEM_HIDE))
        }
        mTencent.shareToQQ(this, bundle, this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d("Share", "onActivityResult")
        Tencent.onActivityResultData(requestCode, resultCode, data, this)
    }

    /**
     * login response
     * {"ret":0,"openid":"182AE45F705157C47D5C02E3AEF89D84","access_token":"31C0DF09B7C59B60DF9297A6BD47717B","pay_token":"47A047D78EE15169510A3BD535F892DC","expires_in":7776000,"pf":"desktop_m_qq-10000144-android-2002-","pfkey":"9bc3bc265fed874228f4f041405a30de","msg":"","login_cost":139,"query_authority_cost":0,"authority_cost":0,"expires_time":1636363426370}
     *
     * user info response
     * {"ret":0,"msg":"","is_lost":0,"nickname":"╭ゝFaTe","gender":"男","gender_type":1,"province":"北京","city":"朝阳","year":"1991","constellation":"","figureurl":"http:\/\/qzapp.qlogo.cn\/qzapp\/1105575492\/182AE45F705157C47D5C02E3AEF89D84\/30","figureurl_1":"http:\/\/qzapp.qlogo.cn\/qzapp\/1105575492\/182AE45F705157C47D5C02E3AEF89D84\/50","figureurl_2":"http:\/\/qzapp.qlogo.cn\/qzapp\/1105575492\/182AE45F705157C47D5C02E3AEF89D84\/100","figureurl_qq_1":"http:\/\/thirdqq.qlogo.cn\/g?b=oidb&k=ozCtgwOX44Qn6o99HPYhqg&s=40&t=1556700447","figureurl_qq_2":"http:\/\/thirdqq.qlogo.cn\/g?b=oidb&k=ozCtgwOX44Qn6o99HPYhqg&s=100&t=1556700447","figureurl_qq":"http:\/\/thirdqq.qlogo.cn\/g?b=oidb&k=ozCtgwOX44Qn6o99HPYhqg&s=640&t=1556700447","figureurl_type":"1","is_yellow_vip":"0","vip":"0","yellow_vip_level":"0","level":"0","is_yellow_year_vip":"0"}
     *
     * @param obj
     */
    override fun onComplete(obj: Any?) {
        Log.d("Share", obj.toString())
        val json = obj as? JSONObject
        if (json == null) {
            Log.d("Share", "解析异常")
            setResult(RESULT_CANCELED)
            finish()
            return
        }

        if (json.getInt("ret") != 0) {
            Log.d("Share", "数据异常")
            setResult(RESULT_CANCELED)
            finish()
            return
        }

        when (mType) {
            PShare.TYPE_LOGIN -> {
                if (json.has("openid")) {
                    // 处理登录回调
                    mTencent.openId = json.optString("openid")
                    mTencent.setAccessToken(
                        json.optString("access_token"),
                        json.optString("expires_in")
                    )
                    // request user info
                    UserInfo(this, mTencent.qqToken).getUserInfo(this)
                } else {
                    // 处理用户信息回调
                    setResult(RESULT_OK, Intent().apply {
                        putExtra("openid", mTencent.openId)
                        putExtra("access_token", mTencent.accessToken)
                        putExtra("expires_in", mTencent.expiresIn)
                        putExtras(Bundle().apply {
                            json.keys().forEach { key ->
                                this.putString(key, json.getString(key))
                            }
                        })
                    })
                    finish()
                }
            }
            else -> {
                setResult(RESULT_OK)
                finish()
            }
        }
    }

    override fun onError(err: UiError?) {
        Log.e("Share", "code ${err?.errorCode} ${err?.errorMessage}")
        setResult(RESULT_CANCELED)
        finish()
    }

    override fun onCancel() {
        Log.d("Share", "onCancel")
        setResult(RESULT_CANCELED)
        finish()
    }

    override fun onWarning(p0: Int) {
        Log.d("Share", "onWarning $p0")
    }

}