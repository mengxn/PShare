package me.codego.share.example

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.tbruyelle.rxpermissions3.Permission
import com.tbruyelle.rxpermissions3.RxPermissions
import io.reactivex.rxjava3.disposables.Disposable
import me.codego.share.toast

/**
 * 权限检查
 *
 * @author mengxn
 * @date 12/1/20
 */
class PermissionInspector {

    private var mPermissionDisposable: Disposable? = null
    private var mRxPermissions: RxPermissions
    private var mActivity: FragmentActivity? = null

    constructor(activity: FragmentActivity) {
        mActivity = activity
        mRxPermissions = RxPermissions(activity)
    }

    constructor(fragment: Fragment) {
        mActivity = fragment.activity
        mRxPermissions = RxPermissions(fragment)
    }

    fun request(showTip: Boolean = true, vararg permissions: String, block: () -> Unit) {
        mPermissionDisposable?.dispose()
        mPermissionDisposable = mRxPermissions
            .requestEachCombined(*permissions)
            .subscribe { permission ->
                if (permission.granted) {
                    block()
                } else {
                    if (showTip) {
                        showTip(permission)
                    }
                }
            }
    }

    /**
     * 权限提示，引导用户开启权限
     */
    private fun showTip(permission: Permission) {

        fun getReadableName(permissionName: String): String? {
            return when (permissionName) {
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission_group.STORAGE -> "SD卡存储"
                Manifest.permission.CAMERA,
                Manifest.permission_group.CAMERA -> "相机"
                Manifest.permission.READ_CALENDAR,
                Manifest.permission.WRITE_CALENDAR,
                Manifest.permission_group.CALENDAR -> "日历"
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.WRITE_CONTACTS,
                Manifest.permission.GET_ACCOUNTS,
                Manifest.permission_group.CONTACTS -> "通讯录"
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission_group.LOCATION -> "位置"
                Manifest.permission.RECORD_AUDIO -> "录音"
                Manifest.permission_group.MICROPHONE -> "麦克风"
                Manifest.permission.READ_PHONE_STATE -> "电话状态"
                Manifest.permission.CALL_PHONE -> "拨打电话"
                Manifest.permission.READ_CALL_LOG -> "读取通话记录"
                Manifest.permission.WRITE_CALL_LOG -> "修改通话记录"
                Manifest.permission.USE_SIP -> "视频服务"
                Manifest.permission_group.PHONE -> "电话"
                Manifest.permission.SEND_SMS -> "发送短信"
                Manifest.permission.READ_SMS,
                Manifest.permission.RECEIVE_SMS -> "读取短信"
                Manifest.permission.RECEIVE_WAP_PUSH,
                Manifest.permission.RECEIVE_MMS -> "读取彩信"
                Manifest.permission_group.SMS -> "短信息"
                Manifest.permission.READ_MEDIA_IMAGES -> "相册"
                else -> "相关功能"
            }
        }

        fun getDenyPermission(activity: FragmentActivity, permission: Permission): String {
            return permission.name
                .split(",")
                .asSequence()
                .map {
                    it.trim()
                }
                .filter {
                    !RxPermissions(activity).isGranted(it)
                }
                .map {
                    getReadableName(it)
                }
                .toSet()
                .joinToString()
        }

        fun openSetting() {
            mActivity?.let { context ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.data = Uri.fromParts("package", context.packageName, null)
                if (intent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(intent)
                }
            }
        }

        mActivity?.let { activity ->
            val denyPermission = getDenyPermission(activity, permission)
            val tip = "请在应用管理中授予“$denyPermission”访问权限"
            if (permission.shouldShowRequestPermissionRationale) {
                activity.toast(tip)
            } else {
                AlertDialog.Builder(activity)
                    .setTitle("提示")
                    .setMessage("请在应用管理中授予“$denyPermission”访问权限")
                    .setPositiveButton("去设置") { _, _ -> openSetting() }
                    .setNegativeButton("取消") { _, _ -> }
                    .show()
            }
        }
    }
}

fun FragmentActivity.requestPermissions(vararg permissions: String, block: () -> Unit) {
    PermissionInspector(this).request(true, *permissions, block = block)
}

fun FragmentActivity.requestPermissions(
    showTip: Boolean = true,
    vararg permissions: String,
    block: () -> Unit
) {
    PermissionInspector(this).request(showTip, *permissions, block = block)
}

fun Fragment.requestPermissions(vararg permissions: String, block: () -> Unit) {
    PermissionInspector(this).request(true, *permissions, block = block)
}

fun Fragment.requestPermissions(
    showTip: Boolean = true,
    vararg permissions: String,
    block: () -> Unit
) {
    PermissionInspector(this).request(showTip, *permissions, block = block)
}