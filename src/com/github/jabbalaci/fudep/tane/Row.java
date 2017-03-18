package com.github.jabbalaci.fudep.tane;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Set;

import com.github.jabbalaci.fudep.utils.Utils;

public class Row {

    private BitSet attrs;
    private Dataset db;
    private Level level;
    private Set<BitSet> partition;
    private BitSet c_plus;
    private List<Rule> rules = new ArrayList<>();

    public Row(BitSet attrs, Level level) {
        this.attrs = attrs;
        this.db = level.db;
        this.level = level;
        //
        if (attrs.cardinality() == 1) {
            this.partition = this.db.partitions.getMap().get(attrs);
        }
    }

    public void compute_c_cplus() {
        Level prev_level = this.level.getPrevLevel();
        int which_level = this.level.getLength();
        if (which_level == 1 || which_level == 2) {
            this.c_plus = (BitSet) this.db.columns.getBitsetAttrIds().clone();    // copy, not a reference
        } else {
            BitSet res = null;
            for (BitSet sub: Apriori.getOneSizeSmallerSubsetsOf(this.attrs)) {
                if (res == null) {
                    res = (BitSet) prev_level.getCPlusDict().get(sub).clone();    // copy, not a reference
                    continue;
                }
                //
                res.and(prev_level.getCPlusDict().get(sub));
            }
            this.c_plus = res;
        }
    }

    public BitSet getAttrs() {
        return this.attrs;
    }

    public BitSet getCPlus() {
        return this.c_plus;
    }

    @Override
    public String toString() {
        String attrs = this.db.columns.to_str(this.attrs);
        boolean show_partition = false;
        boolean show_c_plus = false;
        return String.format("%s %s%s%s", attrs,
                                            show_partition ? this.partition : "",
                                            show_c_plus ? this.c_plus : "",
                                            this.rules);
    }

    public void set_partition(Row row_a, Row row_b) {
        Set<BitSet> set_a = row_a.partition;
        Set<BitSet> set_b = row_b.partition;
        
//        Set<BitSet> res = new HashSet<>();
//        BitSet tmp;
//
//        for (BitSet t1: set_a) {
//            for (BitSet t2: set_b) {
//                tmp = (BitSet) t1.clone();
//                tmp.and(t2);
//                if (tmp.cardinality() > 1) {
//                    res.add(tmp);
//                }
//            }
//        }
//        this.partition = res;
        
        Set<BitSet> res = this.db.stripper.stripped_product(set_a, set_b);
        this.partition = res;
        
        this.db.partitions.register(this.attrs, res);
    }

    public void generate_rules() {
        if (this.attrs.cardinality() > 1) {
            BitSet rhs_candidate_set = (BitSet) this.attrs.clone();
            rhs_candidate_set.and(this.c_plus);
            //
            BitSet lhs, rhs, r_minus_x;
            Rule rule;
            int i;

            for (int attr = this.attrs.nextSetBit(0); attr >= 0; attr = this.attrs.nextSetBit(attr+1)) {
                if (rhs_candidate_set.get(attr)) {
                    lhs = (BitSet) this.attrs.clone();
                    lhs.clear(attr);
                    rhs = new BitSet(); rhs.set(attr);
                    rule = new Rule(lhs, rhs, this.db);
                    rule.setValid(this.is_rule_valid(rule));
                    if (rule.isValid()) {
                        this.db.valid_rules.add_rule(rule);
                        this.c_plus.clear(attr);    // rhs
                        r_minus_x = Utils.minus(this.db.columns.getBitsetAttrIds(), this.attrs);
                        for (i = r_minus_x.nextSetBit(0); i >= 0; i = r_minus_x.nextSetBit(i+1)) {
                            this.c_plus.clear(i);
                        }
                    }
                    this.rules.add(rule);
                }
            }
        }
    }

    private boolean is_rule_valid(Rule rule) {
        int part_a = this.db.partitions.getErrorValue().get(rule.getLhs());
        int part_b = this.db.partitions.getErrorValue().get(this.attrs);
        return part_a == part_b;
    }

}
