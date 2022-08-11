package cn.xihan.qdds

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.view.View
import android.widget.*
import com.highcapable.yukihookapi.YukiHookAPI
import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.factory.current
import com.highcapable.yukihookapi.hook.log.loggerE
import com.highcapable.yukihookapi.hook.param.PackageParam
import com.highcapable.yukihookapi.hook.type.android.ActivityClass
import com.highcapable.yukihookapi.hook.type.java.*
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit
import de.robv.android.xposed.XSharedPreferences
import de.robv.android.xposed.XposedHelpers.*


/**
 * @项目名 : BaseHook
 * @作者 : MissYang
 * @创建时间 : 2022/7/4 16:32
 * @介绍 :
 */
@InjectYukiHookWithXposed(modulePackageName = "cn.xihan.qdds", entryClassName = "HookEntryInit")
class HookEntry : IYukiHookXposedInit {

    override fun onInit() {
        YukiHookAPI.configs {
            debugTag = "yuki"
            isDebug = BuildConfig.DEBUG
        }

    }

    override fun onHook() = YukiHookAPI.encase {

        loadApp(name = QD_PACKAGE_NAME) {

            if (prefs.getBoolean("isEnableAutoSign")) {
                autoSignIn(versionCode)
            }

            if (prefs.getBoolean("isEnableOldLayout")) {
                enableOldLayout(versionCode)
            }

            if (prefs.getBoolean("isEnableLocalCard")) {
                enableLocalCard(versionCode)
            }

            if (prefs.getBoolean("isEnableRemoveBookshelfFloat")) {
                removeBookshelfFloatWindow(versionCode)
            }

            if (prefs.getBoolean("isEnableRemoveBookshelfBottomAd")) {
                removeBottomNavigationCenterAd(versionCode)
            }

            if (prefs.getBoolean("isEnableDisableAd")) {
                disableAd(versionCode)
            }

            splashPage(versionCode)

            if (prefs.getBoolean("isEnableHideBottomDot")) {
                hideBottomRedDot(versionCode)
            }

            if (prefs.getBoolean("isEnableCloseQSNModeDialog")) {
                removeQSNYDialog(versionCode)
            }

            if (prefs.getBoolean("isEnableRemoveUpdate")) {
                removeUpdate(versionCode)
            }
        }


    }


    companion object {

        /**
         * 起点包名
         */
        val QD_PACKAGE_NAME by lazy {
            getPref()?.getString("packageName", "com.qidian.QDReader") ?: "com.qidian.QDReader"
        }

        val versionCode by lazy { getApplicationVersionCode(QD_PACKAGE_NAME) }

        fun getPref(): SharedPreferences? {
            val pref = XSharedPreferences(BuildConfig.APPLICATION_ID)
            return if (pref.file.canRead()) pref else null
        }

    }

}


/**
 * 通过反射获取控件
 * @param param 参数
 * @param name 字段名
 */
@Throws(NoSuchFieldException::class, IllegalAccessException::class)
inline fun <reified T : View> getView(param: Any, name: String): T? {
    return getParam<T>(param, name)
}

/**
 * 反射获取任何类型
 */
@Throws(NoSuchFieldException::class, IllegalAccessException::class)
inline fun <reified T> getParam(param: Any, name: String): T? {
    val clazz: Class<*> = param.javaClass
    val field = clazz.getDeclaredField(name)
    field.isAccessible = true
    return field[param] as? T
}

/**
 * 利用 Reflection 获取当前的系统 Context
 */
fun getSystemContext(): Context {
    val activityThreadClass = findClass("android.app.ActivityThread", null)
    val activityThread = callStaticMethod(activityThreadClass, "currentActivityThread")
    val context = callMethod(activityThread, "getSystemContext") as? Context
    return context ?: throw Error("Failed to get system context.")
}

/**
 * 获取指定应用的 APK 路径
 */
fun getApplicationApkPath(packageName: String): String {
    val pm = getSystemContext().packageManager
    val apkPath = pm.getApplicationInfo(packageName, 0).publicSourceDir
    return apkPath ?: throw Error("Failed to get the APK path of $packageName")
}

/**
 * 获取指定应用的版本号
 */
fun getApplicationVersionCode(packageName: String): Int {
    val pm = getSystemContext().packageManager
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        pm.getPackageInfo(packageName, 0).longVersionCode.toInt()
    } else {
        pm.getPackageInfo(packageName, 0).versionCode
    }
}

/**
 * 打印当前调用栈
 */
fun printCallStack(className: String = "") {
    loggerE(msg = "className: $className")
    loggerE(msg = "Dump Stack: ---------------start----------------")
    val ex = Throwable()
    val stackElements = ex.stackTrace
    stackElements.forEachIndexed { index, stackTraceElement ->
        loggerE(msg = "Dump Stack: $index: $stackTraceElement")
    }
    loggerE(msg = "Dump Stack: ---------------end----------------")
}

fun PackageParam.autoSignIn(versionCode: Int) {
    if (prefs.getBoolean("isEnableOldLayout")) {
        oldAutoSignIn(versionCode)
    } else {
        newAutoSignIn(versionCode)
    }
}

/**
 * 老版布局自动签到
 */
fun PackageParam.oldAutoSignIn(versionCode: Int) {
    when (versionCode) {
        in 758..784 -> {
            findClass("com.qidian.QDReader.ui.view.bookshelfview.CheckInReadingTimeView").hook {
                injectMember {
                    method {
                        name = "S"
                    }
                    afterHook {
                        val m = getView<TextView>(
                            instance, "m"
                        )
                        val l = getView<LinearLayout>(
                            instance, "l"
                        )
                        m?.let { mtv ->
                            if (mtv.text == "签到" || mtv.text == "签到领奖") {
                                l?.performClick()
                            }
                        }
                    }
                }
            }
        }
        else -> loggerE(msg = "自动签到不支持的版本号为: $versionCode")
    }
}

/**
 * 新版布局自动签到
 */
fun PackageParam.newAutoSignIn(versionCode: Int) {
    when (versionCode) {
        in 758..784 -> {
            findClass("com.qidian.QDReader.ui.view.bookshelfview.CheckInReadingTimeViewNew").hook {
                injectMember {
                    method {
                        name = "E"
                    }
                    afterHook {
                        val s = getView<LinearLayout>(
                            instance, "s"
                        )
                        val qd = getParam<Any>(
                            instance, "s"
                        )
                        qd?.let { qdv ->
                            val e = getView<TextView>(
                                qdv, "e"
                            )
                            e?.let { etv ->
                                if (etv.text == "签到") {
                                    s?.performClick()
                                }
                            }
                        }
                    }
                }
            }
        }
        else -> loggerE(msg = "自动签到不支持的版本号为: $versionCode")
    }
}

/**
 * Hook 启用旧版布局
 */
fun PackageParam.enableOldLayout(versionCode: Int) {
    when (versionCode) {
        in 758..800 -> {
            findClass("com.qidian.QDReader.component.config.QDAppConfigHelper\$Companion").hook {
                injectMember {
                    method {
                        name = "getBookShelfNewStyle"
                    }
                    replaceToFalse()
                }
            }
        }
        else -> loggerE(msg = "启用旧版布局不支持的版本号为: $versionCode")
    }
}

/**
 * Hook 启用本地至尊卡
 */
fun PackageParam.enableLocalCard(versionCode: Int) {

    when (versionCode) {
        in 758..800 -> {

            findClass("com.qidian.QDReader.repository.entity.UserAccountDataBean\$MemberBean").hook {
                injectMember {
                    method {
                        name = "getMemberType"
                    }
                    replaceTo(2)
                }

                injectMember {
                    method {
                        name = "getIsMember"
                    }
                    replaceTo(1)
                }
            }
        }
        else -> loggerE(msg = "启用本地至尊卡不支持的版本号为: $versionCode")
    }

}

/**
 * Hook 移除书架右下角浮窗
 */
fun PackageParam.removeBookshelfFloatWindow(versionCode: Int) {
    when (versionCode) {
        in 758..768 -> {
            findClass("com.qidian.QDReader.ui.fragment.QDBookShelfPagerFragment").hook {
                injectMember {
                    method {
                        name = "loadBookShelfAd"
                    }
                    intercept()
                }

                injectMember {
                    method {
                        name = "onViewInject"
                        param(View::class.java)
                    }
                    afterHook {
                        val imgAdIconClose = getView<ImageView>(
                            instance, "imgAdIconClose"
                        )
                        imgAdIconClose?.visibility = View.GONE
                        val layoutImgAdIcon = getView<LinearLayout>(
                            instance, "layoutImgAdIcon"
                        )
                        layoutImgAdIcon?.visibility = View.GONE

                        val imgBookShelfActivityIcon = getView<ImageView>(
                            instance, "imgBookShelfActivityIcon"
                        )
                        imgBookShelfActivityIcon?.visibility = View.GONE
                    }
                }
            }
        }
        in 772..800 -> {
            findClass("com.qidian.QDReader.ui.fragment.QDBookShelfPagerFragment").hook {
                injectMember {
                    method {
                        name = "loadBookShelfAd"
                    }
                    intercept()
                }

                injectMember {
                    method {
                        name = "showBookShelfHoverAd"
                    }
                    intercept()
                }

                injectMember {
                    method {
                        name = "onViewInject"
                        param(View::class.java)
                    }
                    afterHook {
                        val layoutImgAdIcon = getView<LinearLayout>(
                            instance, "layoutImgAdIcon"
                        )
                        layoutImgAdIcon?.visibility = View.GONE
                    }
                }

            }
        }
        else -> {
            loggerE(msg = "移除书架右下角浮窗不支持的版本号为: $versionCode")
        }
    }
}

/**
 * Hook 移除底部导航栏中心广告
 */
fun PackageParam.removeBottomNavigationCenterAd(versionCode: Int) {
    when (versionCode) {
        in 758..800 -> {
            findClass("com.qidian.QDReader.ui.activity.MainGroupActivity\$t").hook {
                injectMember {
                    method {
                        name = "c"
                    }
                    intercept()
                }
            }
        }
        else -> loggerE(msg = "移除底部导航栏中心广告不支持的版本号为: $versionCode")
    }
}

/**
 * Hook 禁用广告
 */
fun PackageParam.disableAd(versionCode: Int) {
    when (versionCode) {
        in 758..800 -> {
            findClass("com.qq.e.comm.constants.CustomPkgConstants").hook {
                injectMember {
                    method {
                        name = "getAssetPluginName"
                    }
                    replaceTo("")
                }
            }

            findClass("com.qq.e.comm.b").hook {
                injectMember {
                    method {
                        name = "a"
                    }
                    intercept()
                }
            }

            findClass("com.qidian.QDReader.start.AsyncMainGDTTask").hook {
                injectMember {
                    method {
                        name = "create"
                        returnType = StringType
                    }
                    intercept()
                }
            }

            findClass("com.qidian.QDReader.start.AsyncMainGameADSDKTask").hook {
                injectMember {
                    method {
                        name = "create"
                        returnType = StringType
                    }
                    intercept()
                }
            }
        }
        else -> loggerE(msg = "禁用广告不支持的版本号为: $versionCode")
    }
}

/**
 * Hook 闪屏页相关
 */
fun PackageParam.splashPage(versionCode: Int) {
    if (prefs.getBoolean("isEnableSplash")) {
        if (prefs.getBoolean("isEnableCustomSplash")) {
            enableCustomSplash(versionCode)
        }
    } else {
        disableSplash(versionCode)
    }
}

/**
 * 关闭闪屏页
 */
fun PackageParam.disableSplash(versionCode: Int) {
    when (versionCode) {
        in 758..800 -> {
            findClass("com.qidian.QDReader.bll.splash.SplashManager").hook {
                injectMember {
                    method {
                        name = "k"
                    }
                    intercept()
                }
            }

            findClass("com.qidian.QDReader.ui.activity.SplashImageActivity").hook {
                injectMember {
                    method {
                        name = "showSplashImage"
                        param(StringType)
                    }
                    afterHook {
                        val mSplashHelper = getParam<Any>(instance, "mSplashHelper")
                        mSplashHelper?.current {
                            method {
                                name = "e"
                            }.call()

                        }
                    }
                }
            }
        }
        else -> loggerE(msg = "闪屏页不支持的版本号为: $versionCode")
    }
}

/**
 * 启用自定义闪屏页
 */
fun PackageParam.enableCustomSplash(versionCode: Int) {
    when (versionCode) {
        in 758..800 -> {
            findClass("com.qidian.QDReader.ui.activity.SplashImageActivity").hook {
                if (!prefs.getBoolean("isEnableCustomSplashImageShowAllButton")) {
                    injectMember {
                        method {
                            name = "onCreate"
                        }
                        afterHook {
                            val btnSkip = getView<Button>(instance, "btnSkip")
                            btnSkip?.visibility = View.GONE
                            val ivTop = getView<ImageView>(instance, "ivTop")
                            ivTop?.visibility = View.GONE
                            val layoutShadow = getParam<RelativeLayout>(instance, "layoutShadow")
                            layoutShadow?.visibility = View.GONE
                            val mGotoActivityShimmer =
                                getView<FrameLayout>(instance, "mGotoActivityShimmer")
                            mGotoActivityShimmer?.visibility = View.GONE
                        }
                    }
                }
                injectMember {
                    method {
                        name = "start"
                        param(
                            "com.qidian.QDReader.ui.activity.SplashActivity".clazz,
                            StringType,
                            StringType,
                            LongType,
                            IntType
                        )
                    }

                    beforeHook {
                        val customSplashImageFilePath = prefs.getString("customSplashImageFilePath")
                        if (customSplashImageFilePath.isNotBlank()) {
                            args(index = 1).set(customSplashImageFilePath)
                        }
                        val customBookId = prefs.getString("customBookId")
                        if (customBookId.isNotBlank()) {
                            args(index = 2).set("QDReader://ShowBook/$customBookId")
                        }

                        args(index = 4).set(prefs.getInt("customSplashImageType", 0))
                    }

                    afterHook {
                        // 打印传入的参数
                        //loggerE(msg = " \nargs[1]: ${args[1] as String} \nargs[2]: ${args[2] as String} \nargs[3]: ${args[3] as Long} \nargs[4]: ${args[4] as Int}")
                    }


                }
            }
        }
        else -> loggerE(msg = "闪屏页不支持的版本号为: $versionCode")
    }
}

/**
 * Hook 启用隐藏底部小红点
 */
fun PackageParam.hideBottomRedDot(versionCode: Int) {
    when (versionCode) {
        in 758..768 -> {
            findClass("com.qidian.QDReader.ui.widget.maintab.a").hook {
                injectMember {
                    method {
                        name = "h"
                        returnType = IntType
                    }
                    replaceTo(1)
                }
            }
        }
        in 772..800 -> {
            findClass("com.qidian.QDReader.ui.widget.maintab.e").hook {
                injectMember {
                    method {
                        name = "h"
                        returnType = IntType
                    }
                    replaceTo(1)
                }
            }
        }
        else -> loggerE(msg = "隐藏底部小红点不支持的版本号为: $versionCode")
    }
}

/**
 * 移除青少年模式弹框
 */
fun PackageParam.removeQSNYDialog(versionCode: Int) {
    findClass("com.qidian.QDReader.bll.manager.QDTeenagerManager").hook {
        injectMember {
            method {
                name = "isTeenLimitShouldShow"
                param(IntType)
                returnType = BooleanType
            }
            replaceToFalse()
        }

        injectMember {
            method {
                name = "judgeTeenUserTimeLimit\$lambda-3\$lambda-2"
                param(ActivityClass)
                returnType = UnitType
            }
            intercept()
        }
    }
    val dialogClassName: String? = when (versionCode) {
        in 758..768 -> "com.qidian.QDReader.bll.helper.v1"
        772 -> "com.qidian.QDReader.bll.helper.w1"
        in 776..784 -> "com.qidian.QDReader.bll.helper.t1"
        else -> null
    }
    dialogClassName?.hook {
        injectMember {
            method {
                name = "show"
                superClass()
            }
            intercept()
        }
    } ?: loggerE(msg = "移除青少年模式弹框不支持的版本号为: $versionCode")
}

/**
 * 禁用检查更新
 */
fun PackageParam.removeUpdate(versionCode: Int) {
    when (versionCode) {
        in 758..784 -> {

            findClass("w4.h").hook {
                injectMember {
                    method {
                        name = "l"
                        returnType = UnitType
                    }
                    intercept()
                }
            }

            findClass("com.qidian.QDReader.ui.activity.MainGroupActivity").hook {
                injectMember {
                    method {
                        name = "checkUpdate"
                        returnType = UnitType
                    }
                    intercept()
                }
            }

            findClass("com.qidian.QDReader.ui.fragment.QDFeedListPagerFragment").hook {
                injectMember {
                    method {
                        name = "checkAppUpdate"
                        returnType = UnitType
                    }
                    intercept()
                }
            }

            findClass("com.tencent.upgrade.core.UpdateCheckProcessor").hook {
                injectMember {
                    method {
                        name = "checkAppUpgrade"
                        returnType = Unit
                    }
                    intercept()
                }
            }

            findClass("com.qidian.QDReader.util.z4").hook {
                injectMember {
                    method {
                        name = "b"
                        returnType = UnitType
                    }
                    intercept()
                }

                injectMember {
                    method {
                        name = "a"
                        returnType = UnitType
                    }
                    intercept()
                }
            }

            findClass("com.tencent.upgrade.core.UpgradeManager").hook {
                injectMember {
                    method {
                        name = "init"
                        returnType = UnitType
                    }
                    intercept()
                }
            }

            findClass("com.qidian.QDReader.ui.activity.AboutActivity").hook {
                injectMember {
                    method {
                        name = "updateVersion"
                        returnType = UnitType
                    }
                    intercept()
                }

                injectMember {
                    method {
                        name = "getVersionNew"
                        returnType = UnitType
                    }
                    intercept()
                }


            }


        }
        else -> loggerE(msg = "禁用检查更新不支持的版本号为: $versionCode")
    }
}






