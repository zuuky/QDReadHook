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
import com.highcapable.yukihookapi.hook.type.android.ContextClass
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.IntType
import com.highcapable.yukihookapi.hook.type.java.LongType
import com.highcapable.yukihookapi.hook.type.java.StringType
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

            /**
             * 是否自动签到
             */
            if (prefs.getBoolean("isEnableAutoSign")) {
                if (prefs.getBoolean("isEnableOldLayout")) {
                    classNameAndMethodNameEntity.getOldLayoutSignInClassNameAndMethodName()?.let {
                        /**
                         * 旧版布局的自动签到
                         */
                        findClass(it[0]).hook {
                            injectMember {
                                method {
                                    name = it[1]
                                }
                                afterHook {
                                    val m = getView<TextView>(
                                        instance, it[2]
                                    )
                                    val l = getView<LinearLayout>(
                                        instance, it[3]
                                    )
                                    m?.let { mtv ->
                                        if (mtv.text == "签到") {
                                            l?.performClick()
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    classNameAndMethodNameEntity.getNewLayoutSignInClassNameAndMethodName()?.let {
                        /**
                         * 新版布局的自动签到
                         */
                        findClass(it[0]).hook {
                            injectMember {
                                method {
                                    name = it[1]
                                }
                                afterHook {
                                    val s = getView<LinearLayout>(
                                        instance, it[2]
                                    )
                                    val qd = getParam<Any>(
                                        instance, it[2]
                                    )
                                    qd?.let { qdv ->
                                        val e = getView<TextView>(
                                            qdv, it[3]
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
                }
            }

            /**
             * 是否启用旧版布局
             */
            if (prefs.getBoolean("isEnableOldLayout")) {
                classNameAndMethodNameEntity.getIsEnableOldLayoutClassNameAndMethodName()?.let {
                    findClass(it[0]).hook {
                        injectMember {
                            method {
                                name = it[1]
                            }
                            replaceToFalse()
                        }
                    }
                }

            }

            /**
             * 是否启用本地至尊卡
             */
            if (prefs.getBoolean("isEnableLocalCard")) {
                classNameAndMethodNameEntity.getIsEnableLocalCardClassNameAndMethodName()?.let {
                    findClass(it[0]).hook {
                        injectMember {
                            method {
                                name = it[1]
                            }
                            replaceTo(2)
                        }

                        injectMember {
                            method {
                                name = it[2]
                            }
                            replaceTo(1)
                        }
                    }
                }
            }

            /**
             * 是否移除书架右下角浮窗
             */
            if (prefs.getBoolean("isEnableRemoveBookshelfFloat")) {
                classNameAndMethodNameEntity.getIsEnableRemoveBookshelfFloatClassNameAndMethodName()
                    ?.let {
                        findClass(it[0]).hook {
                            injectMember {
                                method {
                                    name = it[1]
                                }
                                intercept()
                            }

                            injectMember {
                                method {
                                    name = it[2]
                                    param(View::class.java)
                                }

                                afterHook {
                                    val imgAdIconClose = getView<ImageView>(
                                        instance, it[3]
                                    )
                                    imgAdIconClose?.visibility = View.GONE


                                }


                            }

                        }
                    } ?: loggerE(msg = "版本号为: $versionCode")


            }

            /**
             * 是否去除底部导航栏中心广告
             */
            if (prefs.getBoolean("isEnableRemoveBookshelfBottomAd")) {
                classNameAndMethodNameEntity.getIsEnableRemoveBookshelfBottomAdClassNameAndMethodName()
                    ?.let {
                        findClass(it[0]).hook {
                            injectMember {
                                method {
                                    name = it[1]
                                }
                                intercept()
                            }
                        }
                    }

            }

            /**
             * 是否禁用广告
             */
            if (prefs.getBoolean("isEnableDisableAd")) {
                classNameAndMethodNameEntity.getIsEnableRemoveAdClassNameAndMethodName()?.let {
                    findClass(it[0]).hook {
                        injectMember {
                            method {
                                name = it[1]
                            }
                            replaceTo("")
                        }
                    }

                    findClass(it[2]).hook {
                        injectMember {
                            method {
                                name = it[3]
                                param(ContextClass)
                                returnType = BooleanType
                            }
                            replaceToFalse()
                        }
                    }

                    findClass(it[4]).hook {
                        injectMember {
                            method {
                                name = it[5]
                                returnType = StringType
                            }
                            intercept()
                        }
                    }

                    findClass(it[6]).hook {
                        injectMember {
                            method {
                                name = it[5]
                                returnType = StringType
                            }
                            intercept()
                        }
                    }


                }
            }

            /**
             * 是否启用闪屏页
             */
            if (prefs.getBoolean("isEnableSplash")) {
                if (prefs.getBoolean("isEnableCustomSplash")) {
                    classNameAndMethodNameEntity.getSplashClassNameAndMethodName()?.let {
                        findClass(it[2]).hook {
                            if (!prefs.getBoolean("isEnableCustomSplashImageShowAllButton")) {
                                injectMember {
                                    method {
                                        name = it[3]
                                    }
                                    afterHook {

                                        val btnSkip = getView<Button>(instance, it[4])
                                        btnSkip?.visibility = View.GONE
                                        val ivTop = getView<ImageView>(instance, it[5])
                                        ivTop?.visibility = View.GONE
                                        val layoutShadow = getParam<RelativeLayout>(instance, it[6])
                                        layoutShadow?.visibility = View.GONE
                                        val mGotoActivityShimmer =
                                            getView<FrameLayout>(instance, it[7])
                                        mGotoActivityShimmer?.visibility = View.GONE

                                    }
                                }

                            }
                            injectMember {
                                method {
                                    name = it[8]
                                    param(
                                        "com.qidian.QDReader.ui.activity.SplashActivity".clazz,
                                        StringType,
                                        StringType,
                                        LongType,
                                        IntType
                                    )
                                }

                                beforeHook {
                                    val customSplashImageFilePath =
                                        prefs.getString("customSplashImageFilePath")
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
                                    loggerE(msg = " \nargs[1]: ${args[1] as String} \nargs[2]: ${args[2] as String} \nargs[3]: ${args[3] as Long} \nargs[4]: ${args[4] as Int}")
                                }


                            }
                        }
                    }
                }
            } else {
                classNameAndMethodNameEntity.getSplashClassNameAndMethodName()?.let {
                    findClass(it[0]).hook {
                        injectMember {
                            method {
                                name = it[1]
                            }
                            intercept()
                        }
                    }

                    findClass(it[2]).hook {
                        injectMember {
                            method {
                                name = it[9]
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
            }

            /**
             * 是启用隐藏底部小红点
             */
            if (prefs.getBoolean("isEnableHideBottomDot")) {
                classNameAndMethodNameEntity.getIsEnableHideBottomRedDotClassNameAndMethodName()
                    ?.let {
                        findClass(it[0]).hook {
                            injectMember {
                                method {
                                    name = it[1]
                                    returnType = IntType
                                }
                                replaceTo(1)
                            }
                        }

                    }
            }

            /**
             * 是启用关闭青少年模式弹框
             */
            if (prefs.getBoolean("isEnableCloseQSNModeDialog")) {
                classNameAndMethodNameEntity.getIsEnableCloseTeenagerModeClassNameAndMethodName()?.let{
                    findClass(it[0]).hook {
                        injectMember {
                            method {
                                name = it[1]
                                superClass()
                            }
                            intercept()
                        }
                    }
                }

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

        val classNameAndMethodNameEntity by lazy { ClassNameAndMethodNameEntity(versionCode) }

        fun getPref(): SharedPreferences? {
            val pref = XSharedPreferences(BuildConfig.APPLICATION_ID)
            return if (pref.file.canRead()) pref else null
        }

    }

    /**
     * 返回类名和方法名
     */
    data class ClassNameAndMethodNameEntity(var versionCode: Int) {


        /**
         * 根据版本号获取旧版布局自动签到类名方法名字段名数组
         */
        fun getOldLayoutSignInClassNameAndMethodName(): Array<String>? {
            return when (versionCode) {
                in 758..760 -> arrayOf(
                    "com.qidian.QDReader.ui.view.bookshelfview.CheckInReadingTimeView",
                    "S",
                    "m",
                    "l"
                )
                else -> null
            }
        }

        /**
         * 根据版本号获取新版布局自动签到类名方法名字段名数组
         */
        fun getNewLayoutSignInClassNameAndMethodName(): Array<String>? {
            return when (versionCode) {
                in 758..760 -> arrayOf(
                    "com.qidian.QDReader.ui.view.bookshelfview.CheckInReadingTimeViewNew",
                    "E",
                    "s",
                    "e"
                )
                else -> null
            }
        }

        /**
         * 根据版本号获取是否启用旧版布局的类名方法名字段名数组
         */
        fun getIsEnableOldLayoutClassNameAndMethodName(): Array<String>? {
            return when (versionCode) {
                in 758..760 -> arrayOf(
                    "com.qidian.QDReader.component.config.QDAppConfigHelper\$Companion",
                    "getBookShelfNewStyle"
                )
                else -> null
            }
        }

        /**
         * 根据版本号获取是否启用本地至尊卡的类名方法名字段名数组
         */
        fun getIsEnableLocalCardClassNameAndMethodName(): Array<String>? {
            return when (versionCode) {
                in 758..760 -> arrayOf(
                    "com.qidian.QDReader.repository.entity.UserAccountDataBean\$MemberBean",
                    "getMemberType",
                    "getIsMember"
                )
                else -> null
            }
        }

        /**
         * 根据版本号获取是否移除书架右下角浮窗的类名方法名字段名数组
         */
        fun getIsEnableRemoveBookshelfFloatClassNameAndMethodName(): Array<String>? {
            return when (versionCode) {
                in 758..760 -> arrayOf(
                    "com.qidian.QDReader.ui.fragment.QDBookShelfPagerFragment",
                    "loadBookShelfAd",
                    "onViewInject",
                    "imgAdIconClose",
                )
                else -> null
            }
        }

        /**
         * 根据版本号获取是否移除底部导航栏中心广告的类名方法名字段名数组
         */
        fun getIsEnableRemoveBookshelfBottomAdClassNameAndMethodName(): Array<String>? {
            return when (versionCode) {
                in 758..760 -> arrayOf(
                    "com.qidian.QDReader.ui.activity.MainGroupActivity\$t", "c"
                )
                else -> null
            }
        }

        /**
         * 根据版本号获取是否启用禁止广告的类名方法名字段名数组
         */
        fun getIsEnableRemoveAdClassNameAndMethodName(): Array<String>? {
            return when (versionCode) {
                in 758..760 -> arrayOf(
                    "com.qq.e.comm.constants.CustomPkgConstants",
                    "getAssetPluginName",
                    "com.qq.e.comm.b",
                    "a",
                    "com.qidian.QDReader.start.AsyncMainGDTTask",
                    "create",
                    "com.qidian.QDReader.start.AsyncMainGameADSDKTask"
                )
                else -> null
            }
        }

        /**
         * 根据版本号获取闪屏页相关类名方法名字段名数组
         */
        fun getSplashClassNameAndMethodName(): Array<String>? {
            return when (versionCode) {
                in 758..760 -> arrayOf(
                    "com.qidian.QDReader.bll.splash.SplashManager",
                    "k",
                    "com.qidian.QDReader.ui.activity.SplashImageActivity",
                    "onCreate",
                    "btnSkip",
                    "ivTop",
                    "layoutShadow",
                    "mGotoActivityShimmer",
                    "start",
                    "showSplashImage"
                )
                else -> null
            }
        }

        /**
         * 根据版本号获取是否启用隐藏底部小红点的类名方法名字段名数组
         */
        fun getIsEnableHideBottomRedDotClassNameAndMethodName(): Array<String>? {
            return when (versionCode) {
                in 758..760 -> arrayOf(
                    "com.qidian.QDReader.ui.widget.maintab.a",
                    "h"
                )
                else -> null
            }
        }

        /**
         * 根据版本号获取是否启用关闭青少年模式弹框的类名方法名字段名数组
         */
        fun getIsEnableCloseTeenagerModeClassNameAndMethodName(): Array<String>? {
            return when (versionCode) {
                in 758..760 -> arrayOf(
                    "com.qidian.QDReader.bll.manager.QDTeenagerManager\$a",
                    "getConfig"
                )
                else -> null
            }
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
    val context = callMethod(activityThread, "getSystemContext") as Context?
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



