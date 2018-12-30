package org.andcreator.assistant.notification;

import org.andcreator.assistant.constant.CPUConstant;

import java.util.Arrays;

class CpuNotificationDataDistributor {

    public static CpuNotificationData[] distributeNotificationData(int[] cpuUsages, int coreDistributionMode) {

        // index = 0的cpuUsages是“所有CPU利用率的平均值”
        final int coreCount = cpuUsages.length - 1;

        switch (coreDistributionMode) {
        case CPUConstant.CORE_DISTRIBUTION_MODE_1ICON_UNSORTED:
            return distributeNotificationData_1IconUnsorted(coreCount, cpuUsages);

        case CPUConstant.CORE_DISTRIBUTION_MODE_1ICON_SORTED:
            return distributeNotificationData_1IconSorted(coreCount, cpuUsages);

        case CPUConstant.CORE_DISTRIBUTION_MODE_2ICONS:
        default:
            return distributeNotificationData_2Icons(coreCount, cpuUsages);
        }
    }

    private static CpuNotificationData[] distributeNotificationData_1IconUnsorted(int coreCount, int[] cpuUsages) {

        if (coreCount <= 4) {
            // 4芯或更少
            final CpuNotificationData data[] = new CpuNotificationData[1];

            // icon1
            data[0] = new CpuNotificationData();
            data[0].cpuUsages = cpuUsages;
            data[0].coreNoStart = 1;
            data[0].coreNoEnd = coreCount;

            return data;
        }

        {
            //5核或更多
            final CpuNotificationData data[] = new CpuNotificationData[1];

            // icon1
            data[0] = new CpuNotificationData();
            data[0].cpuUsages = new int[1+4];
            System.arraycopy(cpuUsages, 0, data[0].cpuUsages, 0, 5);

            data[0].coreNoStart = 1;
            data[0].coreNoEnd = 4;

            return data;
        }
    }

    private static CpuNotificationData[] distributeNotificationData_1IconSorted(int coreCount, int[] cpuUsagesIn) {

        final CpuNotificationData data[] = new CpuNotificationData[1];

        //
        final int[] cpuUsages = new int[cpuUsagesIn.length];
        System.arraycopy(cpuUsagesIn, 0, cpuUsages, 0, cpuUsagesIn.length);

        // icon1
        data[0] = new CpuNotificationData();
        data[0].cpuUsages = new int[1+min(coreCount, 4)];
        data[0].cpuUsages[0] = cpuUsages[0];

        // 复制以防止通过排序进行破坏
        Arrays.sort(cpuUsages, 1, cpuUsages.length);

        // 按降序选择
        for (int i=0; i<min(coreCount, 4); i++) {
            data[0].cpuUsages[i+1] = cpuUsages[cpuUsages.length-i-1];
        }

        data[0].coreNoStart = 1;
        data[0].coreNoEnd = min(coreCount, 4);

        return data;
    }

    @SuppressWarnings("SameParameterValue")
    private static int min(int a, int b) {
        return a < b ? a : b;
    }

    private static CpuNotificationData[] distributeNotificationData_2Icons(int coreCount, int[] cpuUsages) {

        if (coreCount <= 4) {
            // 4芯或更少
            final CpuNotificationData data[] = new CpuNotificationData[1];

            // icon1
            data[0] = new CpuNotificationData();
            data[0].cpuUsages = cpuUsages;
            data[0].coreNoStart = 1;
            data[0].coreNoEnd = coreCount;

            return data;
        }

        if (coreCount == 6) {
            // 由于它是6个核心，分为三个部分
            final CpuNotificationData data[] = new CpuNotificationData[2];

            // icon1
            data[0] = new CpuNotificationData();
            data[0].cpuUsages = new int[3 + 1];
            data[0].coreNoStart = 1;
            data[0].coreNoEnd = 3;
            System.arraycopy(cpuUsages, 0, data[0].cpuUsages, 0, 4);

            // icon2
            data[1] = new CpuNotificationData();
            data[1].cpuUsages = new int[3 + 1];
            data[1].coreNoStart = 4;
            data[1].coreNoEnd = 6;
            // icon2 Index = 0“也是”所有CPU利用率的平均值“
            data[1].cpuUsages[0] = cpuUsages[0];
            System.arraycopy(cpuUsages, 4, data[1].cpuUsages, 1, 3);

            return data;
        }

        {
            //4芯或更多（不包括6芯）

            // 把它分成两部分
            final CpuNotificationData data[] = new CpuNotificationData[2];

            // icon1
            data[0] = new CpuNotificationData();
            data[0].cpuUsages = new int[5];
            data[0].coreNoStart = 1;
            data[0].coreNoEnd = 4;
            System.arraycopy(cpuUsages, 0, data[0].cpuUsages, 0, 5);

            // icon2
            data[1] = new CpuNotificationData();
            data[1].cpuUsages = new int[1 + coreCount - 4];
            data[1].coreNoStart = 5;
            data[1].coreNoEnd = coreCount;
            // icon2 index=0 也是“所有CPU利用率的平均值”
            data[1].cpuUsages[0] = cpuUsages[0];
            System.arraycopy(cpuUsages, 5, data[1].cpuUsages, 1, coreCount - 4);

            return data;
        }
    }
}
