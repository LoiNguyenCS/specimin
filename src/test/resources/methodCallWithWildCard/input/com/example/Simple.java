package com.example;

import java.util.List;

class Simple {
    public <B, T extends B> T getInstance(Class<T> type) {
        return cast(type, get(type));
    }

    private static <B, T extends B> T cast(Class<T> type, B value) {
       throw new RuntimeException();
    }

    private <T> T get(Class<T> type) {
        throw new RuntimeException();
    }
}
