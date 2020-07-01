package com.huawei.clustering;

import androidx.annotation.Nullable;

final class Preconditions {

    static <T> T checkNotNull(@Nullable T reference) {
        if (reference == null) {
            throw new NullPointerException();
        }
        return reference;
    }

    static void checkArgument(boolean expression) {
        if (!expression) {
            throw new IllegalArgumentException();
        }
    }

    private Preconditions() {
    }
}