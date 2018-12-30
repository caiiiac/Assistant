package org.andcreator.assistant.util;

import android.support.annotation.Nullable;
import android.util.Log;
import org.andcreator.assistant.bean.AllCoreFrequencyInfo;
import org.andcreator.assistant.bean.OneCpuInfo;
import org.andcreator.assistant.constant.CPUConstant;

import java.io.*;
import java.util.ArrayList;
import java.util.regex.Pattern;

@SuppressWarnings("WeakerAccess")
public class CpuInfoCollector {

    /**
     * 核心计数缓存
     */
    private static int sLastCpuCoreCount = -1;

    /**
     * Gets the number of cores available in this device, across all processors.
     * Requires: Ability to peruse the filesystem at "/sys/devices/system/cpu"
     *
     * @return The number of cores, or "availableProcessors" if failed to get result
     */
    // from http://stackoverflow.com/questions/7962155/how-can-you-detect-a-dual-core-cpu-on-an-android-device-from-code
    public static int calcCpuCoreCount() {

        if (sLastCpuCoreCount >= 1) {
            // 缓存
            return sLastCpuCoreCount;
        }

        try {
            // 获取包含CPU信息的目录
            final File dir = new File("/sys/devices/system/cpu/");
            //过滤只列出我们关心的设备
            final File[] files = dir.listFiles(new FileFilter() {

                @Override
                public boolean accept(File pathname) {
                    //检查文件名是否为“cpu”，后跟一位数字
                    if (Pattern.matches("cpu[0-9]", pathname.getName())) {
                        return true;
                    }
                    return false;
                }
            });

            // 返回核心数（虚拟CPU设备）
            sLastCpuCoreCount = files.length;

        } catch(Exception e) {
            sLastCpuCoreCount = Runtime.getRuntime().availableProcessors();
        }

        return sLastCpuCoreCount;
    }

    /**
     * 获取当前的CPU频率
     *
     * @return 数值如384000（在采集错误的情况下为0）
     */
    private static int takeCurrentCpuFreq(int coreIndex) {
        return readIntegerFile("/sys/devices/system/cpu/cpu" + coreIndex + "/cpufreq/scaling_cur_freq");
    }

    /**
     * 获取最小CPU频率
     *
     * @return 数值如384000（在采集错误的情况下为0）
     */
    private static int takeMinCpuFreq(int coreIndex) {
        return readIntegerFile("/sys/devices/system/cpu/cpu" + coreIndex + "/cpufreq/cpuinfo_min_freq");
    }

    /**
     * 获取最大CPU频率
     *
     * @return 数值如384000（在采集错误的情况下为0）
     */
    private static int takeMaxCpuFreq(int coreIndex) {
        return readIntegerFile("/sys/devices/system/cpu/cpu" + coreIndex + "/cpufreq/cpuinfo_max_freq");
    }

    /**
     * 获取所有内核的CPU频率
     */
    public static void takeAllCoreFreqs(AllCoreFrequencyInfo fi) {
        final int n = calcCpuCoreCount();

        for (int i = 0; i < n; i++) {
            fi.freqs[i] = takeCurrentCpuFreq(i);
            fi.minFreqs[i] = takeMinCpuFreq(i);
            fi.maxFreqs[i] = takeMaxCpuFreq(i);
        }
    }

    private static int readIntegerFile(String filePath) {

        try {
            final BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(filePath)), 1000);
            final String line = reader.readLine();
            reader.close();

            return Integer.parseInt(line);
        } catch (Exception e) {

            // 我无法获取休眠核心的数据，因此不输出日志
            //MyLog.e(e);

            return 0;
        }
    }

    /**
     * 从/ proc / stat获取每个核心的CPU值
     *
     * @return 每个核心的CPU值列表（错误时元素0的数量）
     */
    @Nullable
    public static ArrayList<OneCpuInfo> takeCpuUsageSnapshot() {

        // [0]作为一个整体，[1]及之后是各个CPU
        final ArrayList<OneCpuInfo> result = new ArrayList<>();

        try {
            final BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream("/proc/stat")), CPUConstant.READ_BUFFER_SIZE);
//          final RandomAccessFile reader = new RandomAccessFile("/proc/stat", "r");

//          MyLog.i("load:-----");

            String line;
            while ((line=reader.readLine()) != null) {
                if (!line.startsWith("cpu")) {
                    break;
                }
//              MyLog.i(" load:" + load);

                //     user     nice    system  idle    iowait  irq     softirq     steal
                //cpu  48200 4601 35693 979258 5095 1 855 0 0 0
                //cpu0 26847 1924 25608 212324 2212 1 782 0 0 0
                //cpu1 8371 1003 4180 254096 1026 0 50 0 0 0
                //cpu2 8450 983 3916 252872 1304 0 9 0 0 0
                //cpu3 4532 691 1989 259966 553 0 14 0 0 0

                final String[] tokens = line.split(" +");
                final OneCpuInfo oci = new OneCpuInfo();
                oci.idle = Long.parseLong(tokens[4]);
                oci.total = Long.parseLong(tokens[1])
                        + Long.parseLong(tokens[2])
                        + Long.parseLong(tokens[3])
                        + oci.idle
                        + Long.parseLong(tokens[5])
                        + Long.parseLong(tokens[6])
                        + Long.parseLong(tokens[7]);
                result.add(oci);
            }

            reader.close();
        } catch (Exception ex) {
            Log.e("OneCpuInfo",ex.getMessage());
            return null;
        }

        return result;
    }
}
