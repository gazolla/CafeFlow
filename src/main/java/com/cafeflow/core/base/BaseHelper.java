package com.cafeflow.core.base;

import com.cafeflow.core.exception.HelperException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class BaseHelper {

    protected <T> T executeWithProtection(String operation, SupplierWithException<T> action) {
        try {
            log.debug("Executing {}.{}", getServiceName(), operation);
            T result = action.get();
            log.debug("Successfully executed {}.{}", getServiceName(), operation);
            return result;
        } catch (Exception e) {
            log.error("Failed to execute {}.{}: {}", getServiceName(), operation, e.getMessage());
            throw new HelperException(getServiceName(), operation, e);
        }
    }

    protected void executeWithProtection(String operation, RunnableWithException action) {
        try {
            log.debug("Executing {}.{}", getServiceName(), operation);
            action.run();
            log.debug("Successfully executed {}.{}", getServiceName(), operation);
        } catch (Exception e) {
            log.error("Failed to execute {}.{}: {}", getServiceName(), operation, e.getMessage());
            throw new HelperException(getServiceName(), operation, e);
        }
    }

    protected abstract String getServiceName();

    @FunctionalInterface
    protected interface SupplierWithException<T> {
        T get() throws Exception;
    }

    @FunctionalInterface
    protected interface RunnableWithException {
        void run() throws Exception;
    }
}
