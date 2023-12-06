package me.codego.share

import android.graphics.Bitmap
import me.codego.delegate.IRequest

class ShareRequest(private val request: IRequest) : IShareRequest {
    override fun login(): IShareRequest {
        request.with(PShare.KEY_TYPE, PShare.TYPE_LOGIN)
        return this
    }

    override fun text(text: String): IShareRequest {
        request.with(PShare.KEY_TYPE, PShare.TYPE_SHARE_TEXT)
        request.with(PShare.KEY_TEXT, text)
        return this
    }

    override fun image(image: Bitmap): IShareRequest {
        request.with(PShare.KEY_TYPE, PShare.TYPE_SHARE_IMAGE)
        request.with(PShare.KEY_IMAGE, image)
        return this
    }

    override fun image(vararg imagePath: String): IShareRequest {
        request.with(PShare.KEY_TYPE, PShare.TYPE_SHARE_IMAGE)
        if (imagePath.size > 1) {
            request.with(PShare.KEY_IMAGE_PATH_LIST, imagePath)
        } else if (imagePath.isNotEmpty()) {
            request.with(PShare.KEY_IMAGE_PATH, imagePath[0])
        }
        return this
    }

    override fun url(url: String, title: String, summary: String, preview: Bitmap): IShareRequest {
        request.with(PShare.KEY_TYPE, PShare.TYPE_SHARE_URL)
        request.with(PShare.KEY_URL, url)
        request.with(PShare.KEY_TITLE, title)
        request.with(PShare.KEY_SUMMARY, summary)
        request.with(PShare.KEY_IMAGE, preview)
        return this
    }

    override fun url(url: String, title: String, summary: String, preview: String): IShareRequest {
        request.with(PShare.KEY_TYPE, PShare.TYPE_SHARE_URL)
        request.with(PShare.KEY_URL, url)
        request.with(PShare.KEY_TITLE, title)
        request.with(PShare.KEY_SUMMARY, summary)
        request.with(PShare.KEY_IMAGE_PATH, preview)
        return this
    }

    override fun openMiniProgram(miniId: String, path: String?): IShareRequest {
        request.with(PShare.KEY_TYPE, PShare.TYPE_MINI_PROGRAM)
        request.with(PShare.KEY_MINI_ID, miniId)
        request.with(PShare.KEY_MINI_PATH, path)
        return this
    }

    override fun shareMiniProgram(miniId: String, path: String?, url: String, title: String, summary: String, preview: String): IShareRequest {
        request.with(PShare.KEY_TYPE, PShare.TYPE_SHARE_MINI_PROGRAM)
        request.with(PShare.KEY_MINI_ID, miniId)
        request.with(PShare.KEY_MINI_PATH, path)
        request.with(PShare.KEY_URL, url)
        request.with(PShare.KEY_TITLE, title)
        request.with(PShare.KEY_SUMMARY, summary)
        request.with(PShare.KEY_IMAGE_PATH, preview)
        return this
    }

    override fun to(option: IShareOption, requestCode: Int): IRequest {
        request.with(option.getOption())
        return request.to(option.getTarget(), requestCode)
    }
}