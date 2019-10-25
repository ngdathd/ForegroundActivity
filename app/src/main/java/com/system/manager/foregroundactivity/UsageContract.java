package com.system.manager.foregroundactivity;

import java.util.List;

public interface UsageContract {

    interface View {
        void onUsageStatsRetrieved(List<UsageStatsWrapper> list);

        void onUserHasNoPermission();
    }

    interface Presenter {
        void retrieveUsageStats();
    }
}
