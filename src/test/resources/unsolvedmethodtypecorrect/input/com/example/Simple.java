package com.example;

import org.example.LocalVariables;
import org.example.Foo;

class Simple {
    // Target method.
    void bar() {
        Foo f = new Foo();
        f.getLocals().set(0);
        baz();
    }

    void baz() {
        // To trigger JavaTypeCorrect.
        Foo f = new Foo();
        final LocalVariables locals = f.getLocals();
    }
}
