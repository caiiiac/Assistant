package org.andcreator.assistant.constant;

public class CPUConstant {

    public static final String LOG_NAME = "CpuStats";
    
    // Preference keys
    public static final String PREF_KEY_START_ON_BOOT = "StartOnBoot";

    public static final String PREF_KEY_UPDATE_INTERVAL_SEC = "UpdateIntervalSec";
    public static final int PREF_DEFAULT_UPDATE_INTERVAL_SEC = 2;

    public static final String PREF_KEY_SHOW_FREQUENCY_NOTIFICATION = "ShowFrequencyNotification";
    public static final String PREF_KEY_SHOW_USAGE_NOTIFICATION = "ShowUsageNotification";

    // 初始报警的延迟时间[ms]
    public static final int ALARM_STARTUP_DELAY_MSEC = 1000;

    // 更新警报间隔以维持服务[ms]
    public static final int ALARM_INTERVAL_MSEC = 60 * 1000;

    // CPU使用率通知的图标模式
    public static final String PREF_KEY_CORE_DISTRIBUTION_MODE = "CoreDistributionMode";
    public static final int CORE_DISTRIBUTION_MODE_2ICONS = 0;          // 最多2个图标（默认）
    public static final int CORE_DISTRIBUTION_MODE_1ICON_UNSORTED = 1;  // 1个图标+非排序
    public static final int CORE_DISTRIBUTION_MODE_1ICON_SORTED = 2;    // 1个图标+排序

    public static final int READ_BUFFER_SIZE = 1024;

    public static final int BMP280Temperature = 65536;
}
