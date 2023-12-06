package me.codego.share

import android.app.Activity
import android.os.Bundle

interface IShareOption {

    fun getTarget(): Class<out Activity>

    fun getOption(): Bundle
}