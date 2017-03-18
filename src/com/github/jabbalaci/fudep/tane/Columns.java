package com.github.jabbalaci.fudep.tane;

import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

public class Columns {

    private String[] attr_names;
    public int[] attr_ids;
    private BitSet bitset_attr_ids = new BitSet();

    public Columns(List<String> attr_names) {
        this.attr_names = new String[attr_names.size() + 1];
        this.attr_names[0] = "Not used!!!";
        for (int i=0; i < attr_names.size(); ++i) {
            this.attr_names[i+1] = attr_names.get(i);
        }
        //
        this.attr_ids = new int[attr_names.size()];
        for (int i=0; i < attr_ids.length; ++i) {
            this.attr_ids[i] = i+1;
        }
        //
        for (int val: this.attr_ids) {
            bitset_attr_ids.set(val);
        }
    }

    public BitSet getBitsetAttrIds() {
        return this.bitset_attr_ids;
    }

    public String to_str(BitSet attrs) {
        StringBuilder sb = new StringBuilder();
        for (int i = attrs.nextSetBit(0); i != -1; i = attrs.nextSetBit(i + 1)) {
            sb.append(this.attr_names[i]);
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return Arrays.toString(this.attr_names);
    }

}
