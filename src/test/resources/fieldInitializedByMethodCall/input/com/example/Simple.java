package com.example;

import com.mypackage.C;

class Simple {
    final int b = C.get();
    int bar() {
        return b++;
    }
}
