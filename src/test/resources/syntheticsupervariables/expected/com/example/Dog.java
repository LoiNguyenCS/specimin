package com.example;

import org.first.Mammal;

class Dog extends Mammal {
    int legs;
    public Dog() {
        super();
        legs = super.legs + 4;
    }
}