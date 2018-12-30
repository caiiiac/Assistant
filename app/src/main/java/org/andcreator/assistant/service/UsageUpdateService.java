package org.andcreator.assistant.service;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;
import org.andcreator.assistant.CpuConfig;
import org.andcreator.assistant.IUsageUpdateCallback;
import org.andcreator.assistant.IUsageUpdateService;
import org.andcreator.assistant.bean.AllCoreFrequencyInfo;
import org.andcreator.assistant.bean.OneCpuInfo;
import org.andcreator.assistant.constant.CPUConstant;
import org.andcreator.assistant.notification.NotificationPresenter;
import org.andcreator.assistant.util.CpuInfoCollector;
import org.andcreator.assistant.util.CpuUtil;

import java.util.ArrayList;

public class UsageUpdateService extends Service {


    /**
     * 設定値
     */
    private CpuConfig mConfig = new CpuConfig();

    /**
     * 通知管理类
     */
    private NotificationPresenter mNotificationPresenter = new NotificationPresenter(this, mConfig);

    /**
     * 常驻通知
     */
    private boolean mStopResident = false;

    /**
     * （对于Android 8.0或更高版本）用于从BOOT_COMPLETED执行startForeground的标志
     */
    private boolean mRequestForeground = false;

    /**
     * 睡眠标志
     */
    private boolean mSleeping = false;

    /**
     * 最后一个CPU时钟频率
     */
    private int mLastCpuClock = -1;

    /**
     * 以前收集的数据
     */
    @Nullable
    private ArrayList<OneCpuInfo> mLastCpuUsageSnapshot = null;

    /**
     * 上次CPU利用率
     */
    private int[] mLastCpuUsages = null;

    /**
     * 模式根据每个核心的频率计算CPU使用率（适用于Android 8.0或更高版本）
     */
    private boolean mUseFreqForCpuUsage = false;

    /**
     * 回调列表
     */
    private final RemoteCallbackList<IUsageUpdateCallback> mCallbackList = new RemoteCallbackList<>();

    /**
     * 回调int缓存
     */
    private int mCallbackListSize = 0;

    private long mLastExecTask = System.currentTimeMillis();

    /**
     * 通信量获取线程管理
     */
    private GatherThread mThread = null;
    private boolean mThreadActive = false;

    /**
     * 服务实施
     */
    private final IUsageUpdateService.Stub mBinder = new IUsageUpdateService.Stub() {

        @Override
        public void registerCallback(IUsageUpdateCallback callback) throws RemoteException {

            // 创建回调列表
            mCallbackList.register(callback);

            mCallbackListSize ++;
        }

        @Override
        public void unregisterCallback(IUsageUpdateCallback callback) throws RemoteException {

            // 发布回调列表
            mCallbackList.unregister(callback);

            if (mCallbackListSize > 0) {
                mCallbackListSize --;
            }
        }

        @Override
        public void stopResident() throws RemoteException {

            // 常駐停止
            UsageUpdateService.this.stopResident();
        }

        @Override
        public void startResident() throws RemoteException {

            // 释放居民停止标志
            mStopResident = false;

            // 立即call下一个onStart
            UsageUpdateService.this.scheduleNextTime(CPUConstant.ALARM_STARTUP_DELAY_MSEC);

            if (mThread == null) {
                startThread();
            }
        }

        /**
         *
         */
        @Override
        public void reloadSettings() throws RemoteException {
            // 重新加载设置
            mConfig.loadSettings(UsageUpdateService.this);

            // 通过设置删除清除通知
            mNotificationPresenter.cancelNotifications();
        }
    };


    /**
     *用于检测睡眠状态的接收器（SCREEN_ON / OFF）
     */
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action == null) {
                return;
            }

            if (action.equals(Intent.ACTION_SCREEN_ON)) {

                // 如果停止则重新启动
                mSleeping = false;

                // Call给onStart
                // 但是，如果立即更新“通知时间”，状态栏的顺序会突然改变，因此稍微延迟
                mNotificationPresenter.mNotificationTimeKeep = System.currentTimeMillis() + 30*1000;

                UsageUpdateService.this.scheduleNextTime(CPUConstant.ALARM_STARTUP_DELAY_MSEC);

                // 线程开始
                if (!mStopResident) {
                    startThread();
                }

            } else if (action.equals(Intent.ACTION_SCREEN_OFF)) {

                // 停下来
                mSleeping = true;

                // 报警停止
                stopAlarm();

                // 线程停止
                stopThread();
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {

        if (IUsageUpdateService.class.getName().equals(intent.getAction())) {
            return mBinder;
        }

        // 线程开始
        startThread();
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // 加载设置
        mConfig.loadSettings(this);

        if (mLastCpuUsageSnapshot == null) {
            mLastCpuUsageSnapshot = CpuInfoCollector.takeCpuUsageSnapshot();
        }

        // 在睡眠状态下注册接收器
        getApplicationContext().registerReceiver(mReceiver, new IntentFilter(Intent.ACTION_SCREEN_ON));
        getApplicationContext().registerReceiver(mReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));

        // 定期处理开始
        scheduleNextTime(CPUConstant.ALARM_STARTUP_DELAY_MSEC);

        // 线程开始
        startThread();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final int result = super.onStartCommand(intent, flags, startId);

        mRequestForeground = intent != null && intent.getBooleanExtra("FOREGROUND_REQUEST", false);

        // 通信量获取线程开始
        if (mThread == null) {
            startThread();
        }

        // 继续报警循环
        scheduleNextTime(CPUConstant.ALARM_INTERVAL_MSEC);

        return result;
    }


    private void execTask() {

        //-------------------------------------------------
        // 获取CPU时钟频率
        //-------------------------------------------------
        final AllCoreFrequencyInfo fi = new AllCoreFrequencyInfo(CpuInfoCollector.calcCpuCoreCount());
        CpuInfoCollector.takeAllCoreFreqs(fi);

        final int activeCoreIndex = CpuUtil.getActiveCoreIndex(fi.freqs);
        final int currentCpuClock = fi.freqs[activeCoreIndex];

        // CPU时钟频率最小值/最大值
        final int minFreq = fi.minFreqs[activeCoreIndex];
        final int maxFreq = fi.maxFreqs[activeCoreIndex];

        //-------------------------------------------------
        // 获取CPU利用率
        //-------------------------------------------------
        // 获取CPU利用率snapshot
        int[] cpuUsages = null;
        if (!mUseFreqForCpuUsage) {
            final ArrayList<OneCpuInfo> currentCpuUsageSnapshot = CpuInfoCollector.takeCpuUsageSnapshot();
            if (currentCpuUsageSnapshot != null) {
                // 计算CPU利用率
                cpuUsages = CpuUtil.calcCpuUsages(currentCpuUsageSnapshot, mLastCpuUsageSnapshot);
                // 保存snapshot
                mLastCpuUsageSnapshot = currentCpuUsageSnapshot;
            } else {
                // 我无法得到它，所以我回避它
                mUseFreqForCpuUsage = true;
            }
        }
        if (cpuUsages == null) {
            // 后备模式（因为在Android 8.0及更高版本中无法访问/ proc / stat，CPU使用率是从核心频率计算的）
            cpuUsages = CpuUtil.calcCpuUsagesByCoreFrequencies(fi);
        }


        //-------------------------------------------------
        // 通知判断
        //-------------------------------------------------
        // 如果CPU使用率与上次相同，请勿通知
        // 如果您只将通知图标与舍入值进行比较，则可以将其与舍入值进行比较，但由于CPU使用率％出现在通知文本中，因此您可以通过完全匹配对其进行比较
        final boolean updated = isUpdated(currentCpuClock, cpuUsages);
        mLastCpuUsages = cpuUsages;
        mLastCpuClock = currentCpuClock;

        //-------------------------------------------------
        // 通知
        //-------------------------------------------------
        if (updated) {
            // 状态栏通知
            mNotificationPresenter.updateNotifications(cpuUsages, currentCpuClock, minFreq, maxFreq, mRequestForeground);
            mRequestForeground = false;

            // 通过回调通知
            distributeToCallbacks(cpuUsages, fi);
        }
        mLastExecTask = System.currentTimeMillis();
    }

    private boolean isUpdated(int currentCpuClock, int[] cpuUsages) {

        if (mLastCpuClock != currentCpuClock) {
            return true;
        } else if (mLastCpuUsages == null || cpuUsages == null) {
            return true;
        } else if (cpuUsages.length != mLastCpuUsages.length) {
            // 关心核心数量波动（似乎在Galaxy S II等中很常见）
            return true;
        } else {
            // 如果存在不相同的值，请更新
            final int n = cpuUsages.length;
            for (int i=0; i<n; i++) {
                if (cpuUsages[i] != mLastCpuUsages[i]) {
                    return true;
                }
            }
        }
        return false;
    }

    private void distributeToCallbacks(int[] cpuUsages, AllCoreFrequencyInfo fi) {

        if (mCallbackListSize >= 1) {
            final int n = mCallbackList.beginBroadcast();

            // 为防万一，更新回调次数
            mCallbackListSize = n;

//                if (MyLog.debugMode) {
//                    MyLog.d("- broadcast:" + n);
//                }

            // 收集所有核心的CPU频率
            for (int i=0; i<n; i++) {
                try {
                    mCallbackList.getBroadcastItem(i).updateUsage(cpuUsages,
                        fi.freqs, fi.minFreqs, fi.maxFreqs);
                } catch (RemoteException e) {
//                      MyLog.e(e);
                }
            }
            mCallbackList.finishBroadcast();
        }
    }

    @Override
    public void onDestroy() {

        stopAlarm();

        // 停止线程获取
        stopThread();

        // 释放睡眠状态接收器
        getApplicationContext().unregisterReceiver(mReceiver);

        // 删除通知
        final NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert nm != null;
        nm.cancelAll();

        super.onDestroy();
    }

    /**
     * 保留下一次发布服务
     */
    public void scheduleNextTime(long intervalMs) {

        // 如果发出终止服务的指令，请勿在下次预约。
        if (mStopResident) {
            return;
        }
        if (mSleeping) {
            return;
        }

        final long now = System.currentTimeMillis();

        // 设置闹钟
        final Intent intent = new Intent(this, this.getClass());
        final PendingIntent alarmSender = PendingIntent.getService(
            this,
            0,
            intent,
            0
        );
        // 设置为调用onStartCommand

        final AlarmManager am = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        assert am != null;

        am.set(AlarmManager.RTC, now + intervalMs, alarmSender);

    }

    /**
     * 常驻停止
     */
    public void stopResident() {

        mStopResident = true;

        // 线程停止
        stopThread();

        // 报警停止
        stopAlarm();

        // 删除通知
        final NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert nm != null;
        nm.cancelAll();

        // 常駐停止
        stopSelf();
    }

    /**
     * 停止闹钟
     */
    private void stopAlarm() {

        // 指定服务名称
        final Intent intent = new Intent(this, this.getClass());

        // 释放警报
        final PendingIntent pendingIntent = PendingIntent.getService(
            this,
            0, // 如果将此值设置为-1，则不会成功
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        );

        final AlarmManager am = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        assert am != null;
        am.cancel(pendingIntent);
        // @see http://creadorgranoeste.blogspot.com/2011/06/alarmmanager.html
    }

    private void startThread() {

        if (mThread == null) {
            mThread = new GatherThread();
            mThreadActive = true;
            mThread.start();
        }
    }

    private void stopThread() {

        if (mThreadActive && mThread != null) {


            mThreadActive = false;
            while (true) {
                try {
                    mThread.join();
                    break;
                } catch (InterruptedException ignored) {
                    Log.e("InterruptedException",ignored.getMessage());
                }
            }
            mThread = null;
        }
    }

    /**
     * 通信量获取线程
     */
    private class GatherThread extends Thread {

        @Override
        public void run() {

            while (mThread != null && mThreadActive) {

                SystemClock.sleep(mConfig.intervalMs);

                if (mThreadActive && !mStopResident) {
                    execTask();
                }
            }

        }
    }
}
