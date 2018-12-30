package org.andcreator.assistant;

import org.andcreator.assistant.IUsageUpdateCallback;

interface IUsageUpdateService {

    void registerCallback(IUsageUpdateCallback callback);
    
    void unregisterCallback(IUsageUpdateCallback callback);
    
    void stopResident();

    void startResident();
    
    void reloadSettings();
}