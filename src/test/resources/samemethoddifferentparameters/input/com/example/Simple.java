package com.example;

import com.github.javaparser.resolution.UnsolvedSymbolException;
import javalanguage.Method;

class Simple {

    void bar() {
        Method.getString("Hello");
        try {
            throw new UnsolvedSymbolException();
        } catch (IllegalArgumentException | UnsolvedSymbolException e) {
            Method.getString("Bye", e);
        }
    }
}
