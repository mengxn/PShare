package me.codego.share.example

import android.Manifest
import android.app.Activity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import me.codego.share.example.wxapi.WXEntryActivity
import com.sina.weibo.sdk.auth.Oauth2AccessToken
import com.tencent.connect.share.QQShare
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.modelmsg.SendAuth
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX
import me.codego.share.PShare
import me.codego.share.TencentOption
import me.codego.share.WeiboOption
import me.codego.share.WeixinOption
import me.codego.share.example.databinding.ActivityMainBinding
import me.codego.share.toast
import java.io.File

class MainActivity : AppCompatActivity() {

    private val mImagePath by lazy {
        File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            "Screenshots/aaa.jpg"
        ).absolutePath
    }

    private val mWeixinOption by lazy {
        object : WeixinOption() {

            private var scene: Int = SendMessageToWX.Req.WXSceneSession

            override fun getAppId(): String {
                return "你的AppId"
            }

            override fun getTarget(): Class<out Activity> {
                return WXEntryActivity::class.java
            }

            override fun getScene(): Int {
                return scene
            }

            fun setScene(scene: Int) {
                this.scene = scene
            }
        }
    }
    private val mWeiboOption by lazy {
        object : WeiboOption() {

            override fun getAppKey(): String {
                return "你的AppId"
            }

            override fun getRedirectURI(): String {
                return "https://api.weibo.com/oauth2/default.html"
            }

            override fun getScope(): String {
                return "email,direct_messages_read,direct_messages_write"
            }
        }
    }
    private val mTencentOption by lazy {
        object : TencentOption() {

            private var scene: Int = QQShare.SHARE_TO_QQ_FLAG_QZONE_ITEM_HIDE

            override fun getAppId(): String {
                return "你的AppId"
            }

            override fun getScene(): Int {
                return scene
            }

            fun setScene(scene: Int) {
                this.scene = scene
            }
        }
    }

    private val mBinding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)
    }

    fun loginByWeixin(view: View) {
        mWeixinOption.setScene(SendMessageToWX.Req.WXSceneSession)
        PShare.from(this).login().to(mWeixinOption, 1).result { data ->
            val resp = SendAuth.Resp()
            resp.fromBundle(data.extras!!)
            if (resp.errCode == BaseResp.ErrCode.ERR_OK) {
                Log.d("PShare", "login result ${resp.state} ${resp.code}")
                toast("登录成功")
            } else {
                toast("登录失败")
            }
        }
    }

    fun shareImageByWeixin(view: View) {
        requestPermissions(Manifest.permission.READ_EXTERNAL_STORAGE) {
            if (mBinding.weixinTimelineRb.isChecked) {
                mWeixinOption.setScene(SendMessageToWX.Req.WXSceneTimeline)
            } else {
                mWeixinOption.setScene(SendMessageToWX.Req.WXSceneSession)
            }
            PShare.from(this).image(mImagePath).to(mWeixinOption, 1).result { data ->
                val resp = SendAuth.Resp()
                resp.fromBundle(data.extras!!)
                if (resp.errCode == BaseResp.ErrCode.ERR_OK) {
                    Log.d("PShare", "share result ${resp.state} ${resp.code}")
                    toast("分享成功")
                } else {
                    toast("分享失败")
                }
            }
        }
    }

    fun shareUrlByWeixin(view: View) {
        requestPermissions(Manifest.permission.READ_EXTERNAL_STORAGE) {
            if (mBinding.weixinTimelineRb.isChecked) {
                mWeixinOption.setScene(SendMessageToWX.Req.WXSceneTimeline)
            } else {
                mWeixinOption.setScene(SendMessageToWX.Req.WXSceneSession)
            }
            PShare.from(this)
                .url("http://baidu.com", "标题", "这里是简单描述", mImagePath)
                .to(mWeixinOption, 1)
                .result { data ->
                    val resp = SendAuth.Resp()
                    resp.fromBundle(data.extras!!)
                    if (resp.errCode == BaseResp.ErrCode.ERR_OK) {
                        Log.d("PShare", "share result ${resp.state} ${resp.code}")
                        toast("分享成功")
                    } else {
                        toast("分享失败")
                    }
                }
        }
    }

    fun loginByWeibo(view: View) {
        PShare.from(this).login().to(mWeiboOption, 1).result { data ->
            val token = Oauth2AccessToken.parseAccessToken(data.extras)
            if (token != null) {
                Log.d("PShare", "weibo login success ${token.uid} ${token.accessToken}")
                toast("登录成功")
            } else {
                Log.d("PShare", "weibo login failure")
                toast("登录失败")
            }
        }
    }

    fun shareImageByWeibo(view: View) {
        requestPermissions(Manifest.permission.READ_EXTERNAL_STORAGE) {
            PShare.from(this).image(mImagePath).to(mWeiboOption, 1).result {
                toast("分享成功")
            }
        }
    }

    fun shareUrlByWeibo(view: View) {
        requestPermissions(Manifest.permission.READ_EXTERNAL_STORAGE) {
            PShare.from(this)
                .url("http://baidu.com", "标题", "这里是简单描述", mImagePath)
                .to(mWeiboOption, 1)
                .result {
                    toast("分享成功")
                }
        }
    }

    fun loginByQQ(view: View) {
        mTencentOption.setScene(QQShare.SHARE_TO_QQ_FLAG_QZONE_ITEM_HIDE)
        PShare.from(this).login().to(mTencentOption, 1).result { data ->
            data.extras?.keySet()?.joinToString { key -> "${key}:${data.extras?.get(key)}" }?.let {
                Log.d("PShare", "login result $it")
                toast("登录成功")
            }
        }
    }

    fun shareImageByQQ(view: View) {
        requestPermissions(Manifest.permission.READ_EXTERNAL_STORAGE) {
            if (mBinding.tencentZoneRb.isChecked) {
                mTencentOption.setScene(QQShare.SHARE_TO_QQ_FLAG_QZONE_AUTO_OPEN)
            } else {
                mTencentOption.setScene(QQShare.SHARE_TO_QQ_FLAG_QZONE_ITEM_HIDE)
            }
            PShare.from(this).image(mImagePath).to(mTencentOption, 1).result {
                toast("分享成功")
            }
        }
    }

    fun shareUrlByQQ(view: View) {
        requestPermissions(Manifest.permission.READ_EXTERNAL_STORAGE) {
            if (mBinding.tencentZoneRb.isChecked) {
                mTencentOption.setScene(QQShare.SHARE_TO_QQ_FLAG_QZONE_AUTO_OPEN)
            } else {
                mTencentOption.setScene(QQShare.SHARE_TO_QQ_FLAG_QZONE_ITEM_HIDE)
            }
            PShare.from(this)
                .url("http://baidu.com", "标题", "这里是简单描述", mImagePath)
                .to(mTencentOption, 1)
                .result {
                    toast("分享成功")
                }
        }
    }

}