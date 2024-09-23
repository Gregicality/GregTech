package gregtech.api.pattern.pattern;

import gregtech.api.util.GTLog;
import gregtech.api.util.RelativeDirection;

import com.google.common.base.Preconditions;

import java.util.*;

/**
 * The default aisle strategy, supporting repeatable (multi) aisles.
 */
public class BasicAisleStrategy extends AisleStrategy {

    protected final List<int[]> multiAisles = new ArrayList<>();
    protected final List<PatternAisle> aisles = new ArrayList<>();
    protected final int[] result = new int[2];

    @Override
    public boolean check(boolean flip) {
        int offset = 0;
        for (int[] multiAisle : multiAisles) {
            int result = checkMultiAisle(multiAisle, offset, flip);
            if (result == -1) return false;
            offset += result * (multiAisle[3] - multiAisle[2]);
        }
        return true;
    }

    public int getMultiAisleRepeats(int index) {
        return multiAisles.get(index)[4];
    }

    protected int checkMultiAisle(int[] multi, int offset, boolean flip) {
        int aisleOffset = 0;
        // go from 0 to max repeat
        for (int i = 1; i <= multi[1]; i++) {
            // check all aisles in the multi aisle
            for (int j = multi[2]; j < multi[3]; j++) {
                int result = checkRepeatAisle(j, offset + aisleOffset, flip);
                // same logic as normal aisle check
                if (result == -1) {
                    if (i <= multi[0]) return -1;
                    return multi[4] = i - 1;
                }
                aisleOffset += result;
            }
        }

        return multi[4] = multi[1];
    }

    protected int checkRepeatAisle(int index, int offset, boolean flip) {
        PatternAisle aisle = aisles.get(index);
        for (int i = 1; i <= aisle.maxRepeats; i++) {
            boolean result = checkAisle(index, offset + i - 1, flip);
            if (!result) {
                if (i <= aisle.minRepeats) return -1;

                return aisles.get(index).actualRepeats = i - 1;
            }
        }

        return aisles.get(index).actualRepeats = aisle.maxRepeats;
    }

    @Override
    protected void finish(int[] dimensions, RelativeDirection[] directions, List<PatternAisle> aisles) {
        super.finish(dimensions, directions, aisles);

        // maybe just set the reference? but then the field cant be final so idk
        this.aisles.addAll(aisles);

        BitSet covered = new BitSet(aisles.size());
        int sum = 0;
        for (int[] arr : multiAisles) {
            covered.set(arr[2], arr[3]);
            sum += arr[3] - arr[2];
        }

        if (sum != covered.cardinality()) {
            GTLog.logger.error("Overlapping multiAisles. " +
                    "Total of {} aisles in the multiAisles but only {} distinct aisles.", sum, covered.cardinality());
            multiAisleError();
        }
        if (sum > aisles.size()) {
            GTLog.logger.error("multiAisles out of bounds. Total of {} aisles but {} aisles in multiAisles.",
                    aisles.size(), sum);
            multiAisleError();
        }

        int i = covered.nextClearBit(0);
        while ((i = covered.nextClearBit(i)) < aisles.size()) {
            multiAisles.add(new int[] { 1, 1, i, i + 1, -1 });
            covered.set(i);
        }

        multiAisles.sort(Comparator.comparingInt(a -> a[2]));
        GTLog.logger.error("lawrence");
        for (int[] arr : multiAisles) {
            GTLog.logger.error(Arrays.toString(arr));
        }
    }

    protected void multiAisleError() {
        GTLog.logger.error(
                "multiAisles in the pattern, formatted as [ minRepeats, maxRepeats, startInclusive, endExclusive ] ");
        for (int[] arr : multiAisles) {
            GTLog.logger.error(Arrays.toString(arr));
        }
        throw new IllegalStateException("Illegal multiAisles, check logs above.");
    }

    // resisting the urge to make this a generic type return to allow for inheritors,,,,,,
    public BasicAisleStrategy multiAisle(int min, int max, int from, int to) {
        Preconditions.checkArgument(max >= min, "max: %s is less than min: %s", max, min);
        Preconditions.checkArgument(from >= 0, "from argument is negative: %s", from);
        Preconditions.checkArgument(to > 0, "to argument is not positive: %s", to);
        multiAisles.add(new int[] { min, max, from, to, -1 });
        return this;
    }
}