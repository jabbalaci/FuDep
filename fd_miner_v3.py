#!/usr/bin/env python3

"""
The TANE alg. It finds FDs in a dataset.

It finds minimal, non-redundant FDs.
It uses stripped partitions.
It uses the C+ set for RHS candidates.

This Python version is not very fast but it was very
useful for prototyping.

Author: Laszlo Szathmary, jabba.laci@gmail.com
Year: 2017
"""

import sys
from pathlib import Path
from pprint import pprint

RED = '\033[1;31m'
GREEN = '\033[0;32m'
NO_COLOR = '\033[0m'
#
OK = '{c}✔{nc}'.format(c=GREEN, nc=NO_COLOR)
NOT_OK = '{c}✘{nc}'.format(c=RED, nc=NO_COLOR)


class FileFormatException(Exception):
    """
    If there is a problem with the input file.
    """
    pass


class Columns:
    """
    Represent the columns of the input dataset.
    """
    def __init__(self, attr_names):
        """
        It gets the attr. names in a list, e.g. ['a', 'b', 'c'].
        Assign a unique ID to each attr.: [1, 2, 3].
        Then assign the IDs and the names: {1: 'a', 2: 'b', 3: 'c'}.
        """
        self.attr_names = attr_names
        self.attr_ids = list(range(1, len(attr_names)+1))
        self.set_attr_ids = set(self.attr_ids)
        self.d = dict(zip(self.attr_ids, attr_names))

    def to_str(self, attrs):
        """
        attrs is a tuple of IDs, like (1, 2). It is converted back
        to a string: "ab", since 1 represents "a", and 2 represents "b" (example).
        """
        s = ""
        for val in attrs:
            s += self.d[val]
        return s

    def __str__(self):
        """
        for debug
        """
        return str(self.attr_names)
# endclass Columns


class Partitions:
    """
    Represent the partitions of the attribute sets.
    """
    def __init__(self, columns, table):
        """
        Process the input dataset column by column and calculate
        the partition of each attribute.
        self.d is a dictionary where
          - key: a tuple, which is the attribute set (using column IDs)
          - value: a set of tuples, where a tuple is an equivalence class
        self.error_value is a dictionary
          e(X) is the minimum fraction of tuples to remove for X to be key
          e(X) = 1 - |PI_x| / r
          e(X) = (||PI'_x|| / |PI'_x|) / r
          where ||PI'_x|| is the sum of sizes of eq. classes in PI'
          and PI' is the stripped partition of PI
          where r is the number of rows in the dataset
          Since r is a constant, we don't store it. So for instance,
          instead of 5/8, we simply store 5.
        """
        self.columns = columns
        self.table = table
        self.d = {}
        self.error_value = {}
        for col in range(len(table[0])):
            row_id = 0
            partition_id = columns.attr_ids[col]
            tmp = {}
            for row in table:
                row_id += 1
                val = row[col]
                if val not in tmp:
                    tmp[val] = []
                tmp[val].append(row_id)
            #
            self.d[(partition_id,)] = set()
            for li in tmp.values():
                if len(li) > 1:    # keep stripped partitions only
                    self.d[(partition_id,)].add(tuple(li))
        #
        for key, value in self.d.items():
            self.error_value[key] = self.calculate_error_value(value)
        #
        # self.show()

    def calculate_error_value(self, value):
        """
        Calculate e(X).
        Returns the numerator only, without the denominator,
        since the denominator is always the same (the number of rows in the dataset).
        """
        double_bar = sum(len(eq_class) for eq_class in value)
        return double_bar - len(value)

    def register(self, key, value):
        """
        Register an attr. set (key) and its corresponding partition (value).
        """
        self.d[key] = value
        self.error_value[key] = self.calculate_error_value(value)

    def show(self):
        """
        for debug
        """
        for key, value in self.d.items():
            print("{k}: {v} [e: {e}]".format(k=key, v=value, e=self.error_value[key]))
# endclass Partitions


class Stripper:
    """
    Calculate the product of two stripped partitions
    """
    def __init__(self, num_rows):
        self.T = [0] * (num_rows + 1)
        self.S = []
        for i in range(num_rows + 1):
            self.S.append([])

    def stripped_product(self, set_a, set_b):
        T = self.T
        S = self.S
        cnt = 0
        res = set()
        for eq_class in set_a:
            cnt += 1
            for val in eq_class:
                T[val] = cnt
        #
        for eq_class in set_b:
            for val in eq_class:
                if T[val]:
                    key = T[val]
                    S[key].append(val)
            #
            for val in eq_class:
                if T[val]:
                    key = T[val]
                    if len(S[key]) > 1:
                        res.add(tuple(S[key]))
                    S[key].clear()
            #
        #
        for eq_class in set_a:
            for val in eq_class:
                T[val] = 0
        #
        return res
# endclass Stripper


class Dataset:
    """
    Represent the input dataset.

    For the sake of simplicity, the maximal number of attributes is limited to 26
    (it's the number of characters in the English alphabet).
    """
    alphabet = "abcdefghijklmnopqrstuvwxyz"

    def __init__(self, fname):
        self.fname = fname
        self.auto_attr_names = False
        self.attr_names = None    # will be set later
        self.table = []    # a matrix, representing the dataset
        self.read_dataset(fname)
        self.columns = Columns(self.attr_names)
        self.partitions = Partitions(self.columns, self.table)
        self.num_columns = len(self.table[0])
        self.num_rows = len(self.table)
        self.table = []    # clear it, it's not needed anymore (free memory)
        self.valid_rules = ValidRules()
        self.stripper = Stripper(self.num_rows)

    def parse_line(self, line):
        """
        Parse a line and return the attr. values in a list.
        """
        return [w.strip() for w in line.split(',')]

    def read_column_names(self, fpath):
        with fpath.open() as f:
            line = f.readline().rstrip("\n")
            return self.parse_line(line)


    def read_dataset(self, fname):
        """
        Read the dataset in, which must be a CSV file.
        If you want to specify the names of the attributes, provide
        a file next to the csv file with the extension .cols , which
        contains just one line, the name of the attributes, separated by a comma.
        """
        auto_cols = True
        csv = Path(fname)
        cols = Path(csv.parent, csv.stem + ".cols")
        if cols.is_file():
            auto_cols = False
            self.attr_names = self.read_column_names(cols)
        #
        with csv.open() as f:
            for line in f:
                line = line.rstrip("\n")
                if not line or line.startswith('#'):
                    continue
                self.table.append(self.parse_line(line))
        # endwith
        if auto_cols:
            length = len(self.table[0])
            if length > len(Dataset.alphabet):
                raise FileFormatException("Error: the dataset may have max. {n} attributes!".format(len(Dataset.alphabet)))
            self.attr_names = list(Dataset.alphabet[:length])
        #
        self.verify_dataset()

    def verify_dataset(self):
        """
        Check if there's any problem with the dataset (e.g. a row has more or less
        attr. values then the other rows).
        """
        length = len(self.attr_names)
        cnt = 0
        for row in self.table:
            cnt += 1
            if len(row) != length:
                raise FileFormatException("Error: number of attributes in row {n} should be {m}!".format(n=cnt, m=length))
        #
        if len(self.attr_names) != len(set(self.attr_names)):
            raise FileFormatException("Error: the attribute names must be unique (no duplicates)!")

    def show(self):
        """
        Visualizing the dataset.
        """
        print(self.columns)
        print("-" * 20)
        for row in self.table:
            print(row)
# endclass Dataset


class ValidRules:
    """
    Collection of valid rules.
    """
    def __init__(self):
        self.rules = []

    def add_rule(self, rule):
        """
        Add a (valid) rule to the list.
        """
        self.rules.append(rule)

    def show(self):
        """
        Display all the valid rules and print some statistics.
        """
        print("Minimal, non-redundant FDs:")
        print()
        for rule in self.rules:
            print(rule)
        print()
        print("Number of minimal, non-redundant FDs:", len(self.rules))
# endclass ValidRules


class Rule:
    """
    Representing a functional dependency (FD). I simply call it a rule
    since it has the following form (example): ab -> c.
    """
    def __init__(self, lhs, rhs, db):    # lhs and rhs are tuples
        """
        A rule has a left-hand side (tuple) and a right-hand-side (also tuple).
        The tuple contains column IDs. By default, the rule is not valid.
        If it's still valid, we'll set that later.
        """
        self.lhs = lhs
        self.rhs = rhs
        self.db = db
        self.valid = False    # Does this FD hold? If yes, it'll be set later.

    def __repr__(self):
        """
        Visualizing a rule (LHS, RHS, is it valid?).
        """
        left = self.db.columns.to_str(self.lhs)
        right = self.db.columns.to_str(self.rhs)
        return "{0} -> {1} ({2})".format(left,
                                         right,
                                         OK if self.valid else NOT_OK)
# endclass Rule


class Row:
    """
    We use a levelwise approach. When working with a level, we have
    rows in it. This class represents such a row.
    """
    def __init__(self, attrs, level):    # attrs is a tuple
        """
        What does a row contain?
        - an attr. set, e.g. xy (actually, we store the column IDs)
        - partition of the attr. set
        - list of rules generated from the attr. set
        """
        self.attrs = attrs
        self.db = level.db
        self.level = level
        self.partition = None    # will be set later
        self.c_plus = set()      # C+ value, represented as a set
        if len(attrs) == 1:
            self.partition = self.db.partitions.d[attrs]
        #
        self.rules = []    # will be set later

    def compute_c_plus(self):
        """
        C+ set for RHS candidates.
        """
        attrs = self.attrs
        prev_level = self.level.prev_level
        if len(attrs) in [1, 2]:    # 1 or 2
            self.c_plus = set(self.db.columns.set_attr_ids)    # we make a copy of it (not a reference!)
        else:
            res = None
            for i in range(len(attrs)):
                sub = attrs[:i] + attrs[i+1:]
                if res is None:
                    res = prev_level.c_plus_dict[sub]
                    continue
                #
                res = res.intersection(prev_level.c_plus_dict[sub])
            #
            self.c_plus = res

    def is_rule_valid(self, rule):
        """
        Is the given rule valid?
        It's based on Lemma 3 of the TANE research paper (page 7).
        """
        part_a = self.db.partitions.error_value[rule.lhs]
        part_b = self.db.partitions.error_value[self.attrs]
        return part_a == part_b

    def set_partition(self, row_a, row_b):
        """
        The current row's attr. set is generated by joining row_a and row_b.
        To get the partition of the current row, it's enough to join the
        partitions of row_a and row_b.

        When ready, we also register it in the Partitions class because we'll need
        it later.
        """
        set_a = row_a.partition
        set_b = row_b.partition
        #
        res = self.db.stripper.stripped_product(set_a, set_b)
        #
        self.partition = res
        # register it in class Partitions too
        self.db.partitions.register(self.attrs, res)

    def generate_rules(self):
        """
        generate the rules (and check which rules are valid)
        """
        if len(self.attrs) > 1:
            rhs_candidate_set = set(self.attrs).intersection(self.c_plus)
            for i in range(len(self.attrs)):
                attr = self.attrs[i]
                if attr in rhs_candidate_set:
                    li = list(self.attrs)
                    rhs = attr
                    del li[i]
                    rule = Rule(tuple(li), (rhs,), self.db)
                    rule.valid = self.is_rule_valid(rule)
                    if rule.valid:
                        self.db.valid_rules.add_rule(rule)
                        self.c_plus.remove(rhs)
                        r_minus_x = self.db.columns.set_attr_ids.difference(set(self.attrs))
                        for attr in r_minus_x:
                            try:
                                self.c_plus.remove(attr)
                            except:
                                pass    # no error if we want to remove an element that is not present in the set
                        #
                    #
                    self.rules.append(rule)

    @classmethod
    def join(cls, row_a, row_b, level):
        """
        Apriori's join method. If they are joinable, return a new Row
        object, otherwise return None.
        """
        length = len(row_a.attrs)
        pre_a = row_a.attrs[:length-1]
        pre_b = row_b.attrs[:length-1]
        if pre_a == pre_b:
            return Row(row_a.attrs + row_b.attrs[-1:], level)
        #
        return None

    def __str__(self):
        """
        Visualizing the row.
        """
        attrs = self.db.columns.to_str(self.attrs)
        # show_partition = True
        show_partition = False
        show_c_plus = False
        return "{a} {p}{cp} {r}".format(a=attrs,
                                   p=self.partition if show_partition else "",
                                   cp=self.c_plus if show_c_plus else "",
                                   r=self.rules)
# endclass Row


class Level:
    """
    Represents a level during the levelwise exploration.
    """
    def __init__(self, length, db, prev_level):
        """
        length: length of the attr. sets on this level
        prev_level: If it's level i, then prev_level is level (i-1)
        It contains a list of Row objects.
        If this is level 1, then initialize it with the attributes of the dataset.
        If it's level 2, 3, ..., then use the previous level to generate the attr. sets.
        """
        if prev_level:
            prev_level.prune()
        #
        self.length = length
        self.db = db
        self.prev_level = prev_level
        self.rows = []
        self.set_of_itemsets = set()
        self.c_plus_dict = {}    # dictionary, where key: itemset, value: C+ value of the itemset
        if length == 1:
            self.initialize_level_1()
        else:
            self.create_level_from_prev_level()
        #
        for row in self.rows:
            self.c_plus_dict[row.attrs] = row.c_plus

    def prune(self):
        """
        Remove rows whose C+ value is empty.
        """
        self.rows = [row for row in self.rows if len(row.c_plus) > 0]

    def initialize_level_1(self):
        """
        Add the attributes to the level (1-long attr. sets).
        """
        for attr_id in self.db.columns.attr_ids:
            row = Row((attr_id,), self)
            row.compute_c_plus()
            self.rows.append(row)
            self.set_of_itemsets.add((attr_id,))

    def create_level_from_prev_level(self):
        """
        Using the idea of Apriori's join, populate the rows using the
        attr. sets of the previous level.
        Rows in prev_level whose C+ value was empty have already been pruned.
        """
        length = len(self.prev_level.rows)
        for i in range(0, length-1):
            row_i = self.prev_level.rows[i]
            # if not row_i.c_plus:    # if it's an empty set: skip it
            #     continue
            for j in range(i+1, length):
                row_j = self.prev_level.rows[j]
                # if not row_j.c_plus:    # if it's an empty set: skip it
                #     continue
                new_row = Row.join(row_i, row_j, self)
                if new_row:
                    itemset = new_row.attrs
                    if self.prev_level.contains_all_subsets_of(itemset):
                        new_row.compute_c_plus()
                        new_row.set_partition(row_i, row_j)
                        new_row.generate_rules()
                        self.rows.append(new_row)
                        self.set_of_itemsets.add(itemset)
                else:
                    break

    def contains_all_subsets_of(self, t):
        """
        t is a tuple representing an itemset (or attribute set)
        Generate the 1-size smaller subsets of t and check if the table
        contains all these subsets.
        Example: t = abc. Generate ab, ac, and bc, and check if they are
                 in the table.
        Level 1 table contains all the attributes.
        Level 2 table contains 2-long attr. sets that are generated from the 1-long attributes.
        No need to check the tables at Level 1 and 2.
        Start this pruning process with Level 3.
        """
        if len(t) >= 3:
            for i in range(len(t)):
                sub = t[:i] + t[i+1:]
                if sub not in self.set_of_itemsets:
                    return False
                #
            #
        #
        return True

    def show(self):
        """
        Visualizing the level.
        """
        print("-" * 30)
        print("Level", self.length)
        for row in self.rows:
            print(row)

    def is_empty(self):
        """
        Empty if it has no rows.
        """
        return len(self.rows) == 0
# endclass Level


def levelwise_exploration(db):
    """
    Start at level 1, then create level 2 from level 1, etc.
    When the current level is empty: STOP!
    """
    level = Level(1, db, None)
    level.show()
    # return    # debug, show Level 1 only
    i = 1
    while True:
        level = Level(i+1, db, level)
        i += 1
        if level.is_empty():
            break
        level.show()
    #
    print("=" * 30)


def main():
    """
    Read the input dataset then start the levelwise exploration.
    """
    try:
        fname = sys.argv[1]
    except IndexError:
        print("Error: provide an input file!", file=sys.stderr)
        exit(1)
    #
    db = Dataset(fname)
    # db.show()
    levelwise_exploration(db)
    db.valid_rules.show()
    # print('-' * 40)
    # db.valid_rules.show_minimal_rules()

##############################################################################

if __name__ == "__main__":
    main()
