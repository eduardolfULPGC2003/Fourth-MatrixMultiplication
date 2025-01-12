from mrjob.job import MRJob
from itertools import combinations

class MRFrequentItemset(MRJob):

    # Mapper: Generate pairs (or larger combinations) of items
    def mapper(self, _, line):
        # Assume each line represents a basket of items, e.g., "item1 item2 item3"
        items = line.strip().split()
        # Generate combinations of items (e.g., pairs)
        for r in range(2, len(items) + 1):
            for comb in combinations(items, r):
                yield tuple(sorted(comb)), 1

    # Reducer: Sum up the occurrences of each combination
    def reducer(self, pair, counts):
        total_count = sum(counts)
        if total_count >= 2:  # You can adjust this threshold as needed
            yield pair, total_count

if __name__ == '__main__':
    MRFrequentItemset.run()