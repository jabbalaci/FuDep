package com.github.jabbalaci.fudep;

import com.github.jabbalaci.fudep.tane.Tane;

public class Main {

    public static void main(String[] args) {
        Main main = new Main();
        main.start(args);
    }

    private void start(String[] args) {
        Tane tane = new Tane(args);
        tane.start();
    }

}
