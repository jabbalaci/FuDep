package com.github.jabbalaci.fudep.tane;

import java.util.ArrayList;
import java.util.List;

public class ValidRules {

    private List<Rule> rules = new ArrayList<>();

    public void add_rule(Rule rule) {
        this.rules.add(rule);
    }

    public void show() {
        System.out.println("Minimal, non-redundant FDs:");
        System.out.println();
        for (Rule rule: this.rules) {
            System.out.println(rule);
        }
        System.out.println();
        System.out.println("Number of minimal, non-redundant FDs: " + this.rules.size());
    }

}
