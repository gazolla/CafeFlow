package com.cafeflow.core.base;

@FunctionalInterface
public interface ThrowingSupplier<T> {
    T get() throws Exception;
}
