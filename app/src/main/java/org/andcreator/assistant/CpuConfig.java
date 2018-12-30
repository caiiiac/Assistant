package org.andcreator.assistant;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import org.andcreator.assistant.constant.CPUConstant;

public class CpuConfig {

    // 更新间隔
    public long intervalMs = CPUConstant.PREF_DEFAULT_UPDATE_INTERVAL_SEC * 1000;

    // 通知
    public boolean showUsageNotification = true;
    public boolean showFrequencyNotification = false;

    // CPU使用率通知的图标模式
    public int coreDistributionMode = CPUConstant.CORE_DISTRIBUTION_MODE_2ICONS;

    /**
     * 加载设置
     */
    public void loadSettings(Context context) {

        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);

        // 更新間隔
        final String updateIntervalSec = pref.getString(CPUConstant.PREF_KEY_UPDATE_INTERVAL_SEC, ""+CPUConstant.PREF_DEFAULT_UPDATE_INTERVAL_SEC);
        try {
            intervalMs = (int)(Double.parseDouble(updateIntervalSec) * 1000.0);

        } catch (NumberFormatException e) {
            Log.e("NumberFormatException",e.getMessage());
        }

        // CPU使用率通知
        showUsageNotification = pref.getBoolean(CPUConstant.PREF_KEY_SHOW_USAGE_NOTIFICATION, false);

        // 时钟频率通知
        showFrequencyNotification = pref.getBoolean(CPUConstant.PREF_KEY_SHOW_FREQUENCY_NOTIFICATION, false);

        // CPU使用率通知的图标模式
        try {
            final String s = pref.getString(CPUConstant.PREF_KEY_CORE_DISTRIBUTION_MODE, ""+CPUConstant.CORE_DISTRIBUTION_MODE_2ICONS);
            coreDistributionMode = Integer.parseInt(s);
        } catch (NumberFormatException e) {
            Log.e("NumberFormatException",e.getMessage());
        }
    }

}
