package com.example;

import org.first.Mammal;

class Dog extends Mammal {
    boolean tired = false;
    public void makeASound(int times) {
        super.makeASound(times);
        if ( times > 10) {
            tired = true;
        }
    }
}