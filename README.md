TEST

问题描述：
	Unity 出的Android包，只要包含<uses-permission android:name="android.permission.INTERNET" />权限
就会在 Google Play Services 更新或者停止运行的时候触发Crash：

java.lang.NoSuchMethodError
public default void android.content.ServiceConnection.onBindingDied(android.content.ComponentName)

 bitter.jnibridge.JNIBridge.invoke(Native Method)
 bitter.jnibridge.JNIBridge$a.invoke(Unknown Source:20)
 java.lang.reflect.Proxy.invoke(Proxy.java:913)
 $Proxy0.onBindingDied(Unknown Source)
 android.app.LoadedApk$ServiceDispatcher.doConnected(LoadedApk.java:1641)
 android.app.LoadedApk$ServiceDispatcher$RunConnection.run(LoadedApk.java:1674)
 android.os.Handler.handleCallback(Handler.java:789)
 android.os.Handler.dispatchMessage(Handler.java:98)
 android.os.Looper.loop(Looper.java:171)
 android.app.ActivityThread.main(ActivityThread.java:6684)
 java.lang.reflect.Method.invoke(Native Method)
 com.android.internal.os.Zygote$MethodAndArgsCaller.run(Zygote.java:246)
 com.android.internal.os.ZygoteInit.main(ZygoteInit.java:783) 

复现方法：
1. Unity出Android包，只要包含网络权限即可，在有Google Play Service 并且翻了墙的Android 8.0以上设备上启动运行
2. 找到设置->应用和通知-> Goolge Play Service->存储-> 管理空间->清除所有数据（间隔时间短的情况下可能服务没有
启动，不能复现，最好等5分钟以上，缓存达到20M以上可能必现） crash即发生
 
经过分析导致此异常的原因应该是Unity在C++ native code 中通过反射的方法构造了ServiceConnection ，在Unity启动
后绑定了某一个系统服务，Unity构造的该Connection并没有实现Android8.0以后新增的onBindingDied 方法，导致在
Google Play Service 终止时调用onBindingDied方法出现了异常。给Unity提bug后得到如下回复：

Hello,

Thank you for your report.

Unfortunately, we no longer support Unity versions lower than Unity 2017.2.
Please update here (TECH): https://store.unity.com/download or here (LTS): https://unity3d.com/unity/qa/lts-releases

The problem is that ServiceConnection.onBindingDied was added in API level 26 and is not implemented before 2017.3 (Which is now 2017.4 LTS), we added this for GoogleAdsServiceConnection in such version and also updated the jnibridge to solve the problem.

If the issue persists after updating, please submit a new bug report describing your problem and how to reproduce it in detail.

Sorry for the inconvenience.

Regards,
Evaldas
Unity Android QA

Unity 2017.4 LTS 版本以前官方是不准备修了，只能自己想办法绕过了

原理就是通过java的反射方法，拿到当前应用绑定的所有的服务，找出Unity绑定的那个，然后解除绑定，因为该服务是Unity
在native层绑定的，无法获取更多的信息，无奈只能通过输出的类名"<native proxy object>"进行判断，详见AndroidProj内
java代码。
经测试在MainActiviy onCreate方法中，该服务并没有绑定，因此需要在Unity中进入场景后在C#中进行调用，参考UnityProj demo

警告：
	该方法非Unity官方提供的解决方案，个人研究结果，风险未知，目前项目上线运行2周（2018.08.02），暂未发现严重问题，请
自行评估风险
	根据Unity官方回复 “GoogleAdsServiceConnection” 推测可能是获取advertisingId 时会用到该服务，也可能与UnityAds有关
如果有使用Application.RequestAdvertisingIdentifierAsync 方法获取advertisingId，建议在回调完成后再移除该服务（个人推测
未经验证）
