package com.github.jabbalaci.fudep;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import com.github.jabbalaci.fudep.tane.Columns;

public class Test {

    public static void main(String[] args) {
        Test main = new Test();
        main.start(args);
    }

    private void start(String[] args) {
        List<String> li = new ArrayList<>();
        li.add("x"); li.add("y"); li.add("z");
        Columns c = new Columns(li);
        BitSet bs = new BitSet();
        bs.set(1); bs.set(3);
        System.out.println(c.to_str(bs));
    }

}
