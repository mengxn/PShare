# PShare

聚合三方分享，链式调用，可扩展

## 集成

#### 添加依赖配置
```gradle
# project build.gradle
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}

# module build.gralde
dependencies {
    ...
    implementation 'com.github.mengxn:PShare:0.1'
    // 依赖此库
    implementation("com.github.mengxn:ActivityDelegate:1.0.4")
}
```

#### 微信（可选）
> 微信配置要求，如需集成微信登录、分享功能，需要增加此配置  

在主项目包名下，新建**wxapi.WXEntryActivity**，继承于**WXEntryActivity**，并在**AndroidManifest**下添加配置
```xml
<activity
    android:name="com.your.package.wxapi.WXEntryActivity"
    android:exported="true"
    android:launchMode="singleTask"
    android:theme="@android:style/Theme.Translucent.NoTitleBar" />
```

#### QQ（可选）
> QQ配置要求，如需集成QQ登录、分享功能，需要增加此配置  

在**AndroidManifest**下添加以下配置
```xml
<activity
    android:name="com.tencent.tauth.AuthActivity"
    android:exported="true"
    android:launchMode="singleTask"
    android:noHistory="true">
    <intent-filter>
        <action android:name="android.intent.action.VIEW" />

        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />

        <data android:scheme="tencent你的AppId" />
    </intent-filter>
</activity>
```

#### 微博（可选）
> 不需要额外配置

## 使用
```kotlin
// 微信分享配置信息
val weixinOption = object : WeixinOption() {

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
// 发起微信登录请求
PShare.from(this).login().to(weixinOption, 1).result { data ->
    val resp = SendAuth.Resp()
    resp.fromBundle(data.extras!!)
    if (resp.errCode == BaseResp.ErrCode.ERR_OK) {
        Log.d("PShare", "login result ${resp.state} ${resp.code}")
        toast("登录成功")
    } else {
        toast("登录失败")
    }
}
```
更多示例请参考**me/codego/share/example/MainActivity.kt**