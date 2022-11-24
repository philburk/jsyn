package com.jsyn.unitgen;

import com.jsyn.dsp.SimpleDelay;

public class MultiTapDelay extends UnitFilter {
    private SimpleDelay mDelay;
    private final int[] mPositions;
    private final float[] mGains;

    public MultiTapDelay(final int[] positions, final float[] gains) {
        mPositions = positions;
        mGains = gains;
        int maxPosition = 0;
        for (int position : positions) {
            maxPosition = Math.max(maxPosition, position);
        }
        mDelay = new SimpleDelay(maxPosition);
    }

    @Override
    public void generate(int start, int limit) {
        double[] inputs = input.getValues();
        double[] outputs = output.getValues();

        for (int i = start; i < limit; i++) {
            double sum = 0.0;
            for (int tap = 0; tap < mPositions.length; tap++) {
                sum += mDelay.read(mPositions[tap]);
            }
            mDelay.add((float) inputs[i]);
            outputs[i] = sum;
        }
    }
}
