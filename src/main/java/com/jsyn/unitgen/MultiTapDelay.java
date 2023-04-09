package com.jsyn.unitgen;

import com.jsyn.dsp.SimpleDelay;
import com.jsyn.ports.UnitInputPort;

public class MultiTapDelay extends UnitFilter {

    /** Pre-delay time in milliseconds. */
    public UnitInputPort preDelayMillis;
    private final int mMaxPreDelayFrames;
    private SimpleDelay mPreDelay;
    private SimpleDelay mDelay;
    private final int[] mPositions;
    private final float[] mGains;

    private int mPreDelayFrames = 0;

    public MultiTapDelay(final int[] positions,
                final float[] gains,
                final int maxPreDelayFrames) {
        mPositions = positions;
        mGains = gains;

        preDelayMillis = new UnitInputPort("PreDelayMillis");
        double maxMillis = maxPreDelayFrames * 1000.0 / 48000; // TODO handle unknown frame rate better
        preDelayMillis.setup(0.0, Math.min(20.0, maxMillis), maxMillis);
        addPort(preDelayMillis);
        mMaxPreDelayFrames = Math.max(1, maxPreDelayFrames);
        mPreDelay = new SimpleDelay(maxPreDelayFrames);

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

        double preDelayMS = preDelayMillis.getValues()[0];
        int preDelayFrames = (int)(preDelayMS * 0.001 * getFrameRate());
        preDelayFrames = Math.max(1, Math.min(mMaxPreDelayFrames, preDelayFrames));

        for (int i = start; i < limit; i++) {
            mPreDelay.add((float) inputs[i]);
            mDelay.add(mPreDelay.read(preDelayFrames));
            double sum = 0.0;
            for (int tap = 0; tap < mPositions.length; tap++) {
                sum += mDelay.read(mPositions[tap]);
            }
            outputs[i] = sum;
        }
    }
}
