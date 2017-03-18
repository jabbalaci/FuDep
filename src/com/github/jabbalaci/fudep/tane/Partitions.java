package com.github.jabbalaci.fudep.tane;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Partitions {

    private final static boolean DEBUG = false;

    private Map<BitSet, Set<BitSet>> map = new HashMap<>();

    private Map<BitSet, Integer> error_value = new HashMap<>();

    public Map<BitSet, Set<BitSet>> getMap() {
        return this.map;
    }

    public Map<BitSet, Integer> getErrorValue() {
        return this.error_value;
    }

    public Partitions(Columns columns, List<String[]> matrix) {
        int width = matrix.get(0).length;
        int row_id;
        int partition_id;
        Map<String, List<Integer>> tmp;
        String val;
        BitSet bitset_li;

        for (int col = 0; col < width; ++col) {
            row_id = 0;
            partition_id = columns.attr_ids[col];
            tmp = new HashMap<>();
            for (String[] row: matrix) {
                ++row_id;
                val = row[col];
                if (tmp.containsKey(val) == false) {
                    tmp.put(val, new ArrayList<>());
                }
                tmp.get(val).add(row_id);
            }
            BitSet bs = new BitSet(); bs.set(partition_id);
            this.map.put(bs, new HashSet<>());
            for (List<Integer> li: tmp.values()) {
                if (li.size() > 1) {
                    bitset_li = list_to_bitset(li);
                    this.map.get(bs).add(bitset_li);
                }
            }
        }

        Set<BitSet> value;
        for (BitSet key: this.map.keySet()) {
            value = this.map.get(key);
            this.error_value.put(key, this.calculate_error_value(value));
        }

        if (DEBUG) this.show();
    }

    public void register(BitSet key, Set<BitSet> value) {
        this.map.put(key, value);
        this.error_value.put(key, this.calculate_error_value(value));
    }

    private int calculate_error_value(Set<BitSet> value) {
        int double_bar = 0;
        for (BitSet eq_class: value) {
            double_bar += eq_class.cardinality();
        }
        return double_bar - value.size();
    }

    private BitSet list_to_bitset(List<Integer> li) {
        BitSet bs = new BitSet();
        for (int i: li) {
            bs.set(i);
        }
        return bs;
    }

    private void show() {
        for (BitSet key: this.map.keySet()) {
            System.out.println(key + ": " + this.map.get(key) + " [e: " + this.error_value.get(key) + "]");
        }
    }

}
