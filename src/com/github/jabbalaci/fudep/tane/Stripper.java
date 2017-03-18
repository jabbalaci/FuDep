package com.github.jabbalaci.fudep.tane;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Stripper {
	
	private int[] T;
	private List<BitSet> S;

	public Stripper(int num_rows) {
		T = new int[num_rows + 1];
		S = new ArrayList<>();
		for (int i = 0; i < T.length; ++i) {
			T[i] = 0;
			S.add(new BitSet());
		}
	}

	public Set<BitSet> stripped_product(Set<BitSet> set_a, Set<BitSet> set_b) {
		int cnt = 0;
		Set<BitSet> res = new HashSet<>();
		int val, key;
		BitSet tmp;
		//
		for (BitSet eq_class: set_a) {
			++cnt;
			for (val = eq_class.nextSetBit(0); val>=0; val = eq_class.nextSetBit(val+1)) {
	            T[val] = cnt;
	        }
		}
		for (BitSet eq_class: set_b) {
			for (val = eq_class.nextSetBit(0); val>=0; val = eq_class.nextSetBit(val+1)) {
	            if (T[val] > 0) {
	            	key = T[val];
	            	S.get(key).set(val);
	            }
	        }
			for (val = eq_class.nextSetBit(0); val>=0; val = eq_class.nextSetBit(val+1)) {
				if (T[val] > 0) {
					key = T[val];
					tmp = S.get(key);
					if (tmp.cardinality() > 1) {
						res.add((BitSet) tmp.clone());
					}
					tmp.clear();
				}
			}
		}
		for (BitSet eq_class: set_a) {
			for (val = eq_class.nextSetBit(0); val>=0; val = eq_class.nextSetBit(val+1)) {
	            T[val] = 0;
	        }
		}
		//
		return res;
	}

}
