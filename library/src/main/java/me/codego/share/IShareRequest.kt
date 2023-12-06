package me.codego.share

import android.graphics.Bitmap
import me.codego.delegate.IRequest

interface IShareRequest {

    fun login(): IShareRequest

    fun text(text: String): IShareRequest

    fun image(image: Bitmap): IShareRequest

    fun image(vararg imagePath: String): IShareRequest

    fun url(url: String, title: String, summary: String, preview: Bitmap): IShareRequest

    fun url(url: String, title: String, summary: String, preview: String): IShareRequest

    fun openMiniProgram(miniId: String, path: String? = null): IShareRequest

    fun shareMiniProgram(miniId: String, path: String? = null, url: String, title: String, summary: String, preview: String): IShareRequest

    fun to(option: IShareOption, requestCode: Int): IRequest

}