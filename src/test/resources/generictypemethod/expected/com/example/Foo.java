package com.example;

import org.testing.UnsolvedType;
import java.util.Set;

class Foo {

    protected Baz<UnsolvedType, Set<UnsolvedType>> field;

    void bar() {
        field.baz();
    }
}
