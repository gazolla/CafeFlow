package com.cafeflow.core.base;

@FunctionalInterface
public interface ThrowingRunnable {
    void run() throws Exception;
}
