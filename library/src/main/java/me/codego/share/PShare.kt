package me.codego.share

import android.content.Context
import me.codego.delegate.PIntent

/**
 *
 * @author mengxn
 * @date 2023/11/28
 */
object PShare {

    internal const val KEY_TYPE = "type"
    internal const val KEY_IMAGE = "image"
    internal const val KEY_IMAGE_PATH = "image_path"
    internal const val KEY_IMAGE_PATH_LIST = "image_path_list"
    internal const val KEY_SCENE = "scene"
    internal const val KEY_URL = "url"
    internal const val KEY_TITLE = "title"
    internal const val KEY_SUMMARY = "summary"
    internal const val KEY_TEXT = "text"
    internal const val KEY_MINI_ID = "miniId"
    internal const val KEY_MINI_PATH = "miniPath"

    internal const val TYPE_LOGIN = 1
    internal const val TYPE_SHARE_TEXT = 2
    internal const val TYPE_SHARE_IMAGE = 3
    internal const val TYPE_SHARE_URL = 4
    internal const val TYPE_MINI_PROGRAM = 5
    internal const val TYPE_SHARE_MINI_PROGRAM = 6

    const val KEY_WEIXIN_APP_ID = "weixin_app_id"
    const val KEY_WEIXIN_SCOPE = "weixin_scope"
    const val KEY_WEIXIN_SECRET = "weixin_secret"
    const val KEY_WEIXIN_MINIPROGRAM_TYPE = "weixin_miniprogram_type"

    const val KEY_WEIBO_APP_KEY = "weibo_app_key"
    const val KEY_WEIBO_REDIRECT_URI = "weibo_redirect_uri"
    const val KEY_WEIBO_SCOPE = "weibo_scope"

    const val KEY_TENCENT_APP_ID = "tencent_app_id"
    const val KEY_TENCENT_SCOPE = "tencent_scope"

    fun from(context: Context): IShareRequest {
        return ShareRequest(PIntent.from(context))
    }

}

