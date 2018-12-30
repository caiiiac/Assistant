package org.andcreator.assistant.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import org.andcreator.assistant.bean.AllCoreFrequencyInfo;
import org.andcreator.assistant.bean.OneCpuInfo;

import java.util.ArrayList;

public class CpuUtil {

    /**
     * 计算CPU利用率
     *
     * @param currentInfo
     * @param lastInfo
     * @return CPU利用率数组（元素数必须始终为1或更多，[0]所有CPU，[1]和每个核心之外），或者为null
     */
    @Nullable
    public static int[] calcCpuUsages(ArrayList<OneCpuInfo> currentInfo, @Nullable ArrayList<OneCpuInfo> lastInfo) {

        if (currentInfo == null || lastInfo == null) {
            // 针对NPE的措施（基本上，不应该发生，服务何时死亡？）
            return null;
        }

        final int nLast = lastInfo.size();
        final int nCurr = currentInfo.size();
        if (nLast == 0 || nCurr == 0) {
            Log.d("CPU"," no info: [" + nLast + "][" + nCurr + "]");
            return null;
        }

        // 比较上次和次的较小的号码
        // *即使更改了Galaxy S II等中出现的内核数量，也可以通过与较少数量的内核进行比较来尽可能显示CPU利用率
        final int n = (nLast < nCurr ? nLast : nCurr);
        final int[] cpuUsages = new int[n];
        for (int i=0; i<n; i++) {
            final OneCpuInfo last = lastInfo.get(i);
            final OneCpuInfo curr = currentInfo.get(i);

            // 「差异“所以它应该适合int。
            final int totalDiff = (int) (curr.total - last.total);
            if (totalDiff > 0) {
                final int idleDiff = (int) (curr.idle - last.idle);

                cpuUsages[i] = 100 - idleDiff*100 / totalDiff;

            } else {
                cpuUsages[i] = 0;
            }
        }

        return cpuUsages;
    }

    /**
     * 伪CPU使用率是根据每个核心的频率（及其最小值/最大值）计算的
     */
    @NonNull
    public static int[] calcCpuUsagesByCoreFrequencies(AllCoreFrequencyInfo fi) {

        final int coreCount = fi.freqs.length;

        // [0]是整体，[1]〜[coreCount]是每个核心的CPU利用率
        final int[] cpuUsages = new int[coreCount+1];

        // 计算每个核心的CPU利用率
        for (int i = 0; i < coreCount; i++) {
            cpuUsages[i+1] = CpuUtil.getClockPercent(fi.freqs[i], fi.minFreqs[i], fi.maxFreqs[i]);
        }

        // 计算总体CPU使用率
        int freqSum = 0;
        int minFreqSum = 0;
        int maxFreqSum = 0;
        for (int i = 0; i < coreCount; i++) {
            freqSum += fi.freqs[i];
            minFreqSum += fi.minFreqs[i];
            maxFreqSum += fi.maxFreqs[i];
        }
        cpuUsages[0] = CpuUtil.getClockPercent(freqSum, minFreqSum, maxFreqSum);

        return cpuUsages;
    }

    /**
     *格式化时钟频率显示
     *
     * @param clockHz 时钟频率[KHz]
     * @return “XX MHz”或“X.X GHz”
     */
    public static String formatFreq(int clockHz) {

        if (clockHz < 1000*1000) {
            return (clockHz / 1000) + " MHz";
        }

        // a.b GHz
        final int a = (clockHz / 1000 / 1000);
        // a.b b值为GHz
        final int b = (clockHz / 1000 / 100) % 10;
        return a + "." + b + " GHz";
    }

    /**
     * 返回最活跃核心的索引
     */
    public static int getActiveCoreIndex(int[] freqs) {

        int targetCore = 0;
        for (int i = 1; i < freqs.length; i++) {
            if (freqs[i] > freqs[targetCore]) {
                targetCore = i;
            }
        }
        return targetCore;
    }

    /**
     * 从时钟频率的当前/最小/最大值计算[0,100]％
     */
    public static int getClockPercent(int currentFreq, int minFreq, int maxFreq) {
        if (maxFreq - minFreq <= 0) {
            return 0;
        }
        return maxFreq >= 0 ? ((currentFreq - minFreq) * 100 / (maxFreq - minFreq)) : 0;
    }

}
