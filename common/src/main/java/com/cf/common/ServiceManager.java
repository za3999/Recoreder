package com.cf.common;

import java.util.HashMap;
import java.util.Iterator;
import java.util.ServiceLoader;

public final class ServiceManager {

    private static ServiceManager mInstance = new ServiceManager();
    private HashMap mServices = new HashMap();

    private ServiceManager() {
    }

    public static <T> T getServices(Class<T> clazz) {
        if (mInstance.mServices.containsKey(clazz)) {
            return (T) mInstance.mServices.get(clazz);
        }
        Iterator<T> iterator = ServiceLoader.load(clazz).iterator();
        T service = null;
        if (iterator != null && iterator.hasNext()) {
            service = iterator.next();
            if (service != null) {
                mInstance.mServices.put(clazz, service);
            }
        }
        return service;
    }
}
