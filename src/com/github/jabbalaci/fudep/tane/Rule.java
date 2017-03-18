package com.github.jabbalaci.fudep.tane;

import java.util.BitSet;

public class Rule {

    private final static String RED = "\033[1;31m";
    private final static String GREEN = "\033[0;32m";
    private final static String NO_COLOR = "\033[0m";
    //
    private final static String OK = String.format("%s✔%s", GREEN, NO_COLOR);
    private final static String NOT_OK = String.format("%s✘%s", RED, NO_COLOR);

    private BitSet lhs;
    private BitSet rhs;
    private Dataset db;
    private boolean valid = false;    // can change later

    public Rule(BitSet lhs, BitSet rhs, Dataset db) {
        this.lhs = lhs;
        this.rhs = rhs;
        this.db = db;
    }

    public BitSet getLhs() {
        return this.lhs;
    }

    public BitSet getRhs() {
        return this.rhs;
    }

    @Override
    public String toString() {
        String left = this.db.columns.to_str(lhs);
        String right = this.db.columns.to_str(rhs);
        return String.format("%s -> %s (%s)", left, right, this.valid ? OK : NOT_OK);
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public boolean isValid() {
        return this.valid;
    }

}
