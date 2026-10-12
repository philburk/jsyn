/*
 * Copyright 2026 Phil Burk, Mobileer Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jsyn.examples;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import com.jsyn.JSyn;
import com.jsyn.Synthesizer;
import com.jsyn.data.FloatSample;
import com.jsyn.dsp.OnePoleLowPass;
import com.jsyn.scope.AudioScope;
import com.jsyn.swing.DoubleBoundedRangeModel;
import com.jsyn.swing.DoubleBoundedRangeSlider;
import com.jsyn.swing.ExponentialRangeModel;
import com.jsyn.swing.PortControllerFactory;
import com.jsyn.unitgen.FixedRateMonoReader;
import com.jsyn.unitgen.FixedStringResonator;
import com.jsyn.unitgen.LineOut;
import com.jsyn.unitgen.PassThrough;
import com.jsyn.unitgen.PitchDetector;
import com.jsyn.unitgen.StringResonator;
import com.jsyn.unitgen.VariableStringResonator;

/**
 * Pluck a Karplus-Strong string. Compare the FixedStringResonator and the VariableStringResonator.
 * Move the frequency slider while the Variable string rings to hear a smooth pitch bend.
 * <p>
 * The pluck is a short burst of noise, shaped by the pluck controls. Try Brightness 0.1, Decay 2,
 * Pluck Position 0.2 and Pluck Softness 0.6 for a nylon string.
 *
 * @author Phil Burk (C) 2026 Mobileer Inc
 */
public class PlayPluckedString extends JFrame {
    private static final float BURST_PEAK = 0.3f;

    /** Cutoff of the softness filter, relative to the frequency, for a hard pick. */
    private static final double MAX_SOFTNESS_CUTOFF_RATIO = 64.0;

    private static final int RESOLUTION = 100000;

    private final Synthesizer synth;
    private final LineOut lineOut;
    private final PitchDetector pitchDetector;
    private final AudioScope scope;

    // Each control fans out to both resonators.
    private final PassThrough frequencyControl;
    private final PassThrough decayControl;
    private final PassThrough brightnessControl;

    private final FixedStringResonator fixedResonator;
    private final VariableStringResonator variableResonator;
    private final FixedRateMonoReader fixedExciter;
    private final FixedRateMonoReader variableExciter;

    private final Random random = new Random();
    private boolean useVariable = true;

    /** Where the string is plucked, as a fraction of its length from the bridge. */
    private double pluckPosition = 0.2;

    /** 0.0 is a hard pick, 1.0 is a soft fingertip. */
    private double pluckSoftness = 0.0;

    /** Length of the noise burst in milliseconds. It is never longer than one period. */
    private double pluckLengthMillis = 1.0;

    private final JLabel pitchLabel = new JLabel("Detected: -");
    private Timer pitchTimer;

    /** Create the synthesizer, the two resonators and the user interface. */
    public PlayPluckedString() {
        super("Plucked String");
        synth = JSyn.createSynthesizer();

        synth.add(lineOut = new LineOut());
        synth.add(pitchDetector = new PitchDetector());
        synth.add(frequencyControl = new PassThrough());
        synth.add(decayControl = new PassThrough());
        synth.add(brightnessControl = new PassThrough());
        synth.add(fixedResonator = new FixedStringResonator());
        synth.add(variableResonator = new VariableStringResonator());
        synth.add(fixedExciter = new FixedRateMonoReader());
        synth.add(variableExciter = new FixedRateMonoReader());

        frequencyControl.input.setup(40.0, 220.0, 2000.0);
        frequencyControl.input.setName("Frequency");
        decayControl.input.setup(0.1, 3.0, 20.0);
        decayControl.input.setName("Decay");
        brightnessControl.input.setup(0.0, 0.5, 1.0);
        brightnessControl.input.setName("Brightness");

        connectResonator(fixedResonator, fixedExciter);
        connectResonator(variableResonator, variableExciter);

        scope = new AudioScope(synth);
        scope.addProbe(fixedResonator.output);
        scope.addProbe(variableResonator.output);
        scope.setTriggerMode(AudioScope.TriggerMode.AUTO);

        setupUserInterface();
    }

    private void connectResonator(StringResonator resonator, FixedRateMonoReader exciter) {
        frequencyControl.output.connect(resonator.frequency);
        decayControl.output.connect(resonator.decay);
        brightnessControl.output.connect(resonator.brightness);
        exciter.output.connect(resonator.input);
        resonator.output.connect(0, lineOut.input, 0);
        resonator.output.connect(0, lineOut.input, 1);
        resonator.output.connect(pitchDetector.input);
        // Turn off the resonator when the string is silent. pluck() turns it back on.
        resonator.setupAutoDisable();
    }

    private void setupUserInterface() {
        setLayout(new BorderLayout());

        JPanel controls = new JPanel(new GridLayout(0, 1));
        final JCheckBox variableBox = new JCheckBox(
                "VariableStringResonator (Lagrange, bends smoothly)", useVariable);
        variableBox.addActionListener(e -> useVariable = variableBox.isSelected());
        controls.add(variableBox);

        controls.add(PortControllerFactory.createExponentialPortSlider(frequencyControl.input));
        controls.add(PortControllerFactory.createExponentialPortSlider(decayControl.input));
        controls.add(PortControllerFactory.createPortSlider(brightnessControl.input));

        final DoubleBoundedRangeModel positionModel = new DoubleBoundedRangeModel(
                "Pluck Position", RESOLUTION, 0.02, 0.5, pluckPosition);
        positionModel.addChangeListener(e -> pluckPosition = positionModel.getDoubleValue());
        controls.add(new DoubleBoundedRangeSlider(positionModel, 3));

        final DoubleBoundedRangeModel softnessModel = new DoubleBoundedRangeModel(
                "Pluck Softness", RESOLUTION, 0.0, 1.0, pluckSoftness);
        softnessModel.addChangeListener(e -> pluckSoftness = softnessModel.getDoubleValue());
        controls.add(new DoubleBoundedRangeSlider(softnessModel, 3));

        final ExponentialRangeModel lengthModel = new ExponentialRangeModel(
                "Pluck Length ms", RESOLUTION, 0.05, 20.0, pluckLengthMillis);
        lengthModel.addChangeListener(e -> pluckLengthMillis = lengthModel.getDoubleValue());
        controls.add(new DoubleBoundedRangeSlider(lengthModel, 3));

        JButton pluckButton = new JButton("Pluck");
        pluckButton.addActionListener(e -> pluck());
        controls.add(pluckButton);
        controls.add(pitchLabel);
        add(controls, BorderLayout.NORTH);

        scope.getView().setControlsVisible(false);
        add(scope.getView(), BorderLayout.CENTER);

        // Poll the PitchDetector and compare it with the requested frequency.
        // Keep showing the last confident result, grayed out, when the confidence drops.
        pitchTimer = new Timer(100, e -> updatePitchLabel());
    }

    private void updatePitchLabel() {
        double confidence = pitchDetector.confidence.getValue();
        boolean confident = confidence > 0.5;
        if (confident) {
            double detected = pitchDetector.frequency.getValue();
            double requested = frequencyControl.input.get();
            double cents = 1200.0 * Math.log(detected / requested) / Math.log(2.0);
            pitchLabel.setText(String.format("Detected: %.2f Hz, error %.1f cents, confidence %.2f",
                    detected, cents, confidence));
        }
        pitchLabel.setForeground(confident ? Color.BLACK : Color.GRAY);
    }

    /**
     * A short burst of noise, shaped by the pluck controls. The classic Karplus-Strong excitation
     * is a whole period of noise, which sounds long and scrapey on low notes, so the burst is
     * shorter and faded in and out with a Hann window. Plucking at a fraction of the string length
     * removes the harmonics that have a node there. That is done with a comb filter, which
     * subtracts a copy delayed by that fraction of a period. The comb also removes any DC.
     * Softness is a low pass filter, like the flesh of a fingertip compared with a pick.
     */
    private FloatSample makeBurst(double frequency) {
        double frameRate = synth.getFrameRate();
        int period = Math.max(1, (int) (frameRate / frequency));
        int burstLength = Math.max(1, Math.min(period, (int) (pluckLengthMillis * 0.001 * frameRate)));
        float[] noise = new float[burstLength];
        for (int n = 0; n < burstLength; n++) {
            double window = 0.5 - (0.5 * Math.cos(2.0 * Math.PI * (n + 1) / (burstLength + 1)));
            noise[n] = (float) ((random.nextFloat() - 0.5f) * window);
        }

        int combDelay = Math.max(1, (int) (pluckPosition * period));
        // Leave room for the comb copy and the tail of the softness filter.
        float[] data = new float[burstLength + combDelay + (period / 2)];
        for (int n = 0; n < data.length; n++) {
            float direct = (n < burstLength) ? noise[n] : 0.0f;
            int delayedIndex = n - combDelay;
            float delayed = (delayedIndex >= 0 && delayedIndex < burstLength) ? noise[delayedIndex]
                    : 0.0f;
            data[n] = direct - delayed;
        }

        OnePoleLowPass softener = new OnePoleLowPass();
        double cutoffRatio = Math.pow(MAX_SOFTNESS_CUTOFF_RATIO, 1.0 - pluckSoftness);
        softener.setCutoff(frequency * cutoffRatio, frameRate);
        float peak = 0.0f;
        for (int n = 0; n < data.length; n++) {
            data[n] = softener.process(data[n]);
            peak = Math.max(peak, Math.abs(data[n]));
        }

        // The filters change the level a lot, so normalize it.
        if (peak > 0.0f) {
            float scale = BURST_PEAK / peak;
            for (int n = 0; n < data.length; n++) {
                data[n] *= scale;
            }
        }
        return new FloatSample(data);
    }

    private void pluck() {
        final StringResonator resonator = useVariable ? variableResonator : fixedResonator;
        final FixedRateMonoReader exciter = useVariable ? variableExciter : fixedExciter;
        final FloatSample burst = makeBurst(frequencyControl.input.get());
        synth.queueCommand(() -> {
            resonator.setEnabled(true);
            exciter.dataQueue.queue(burst);
        });
    }

    /** Start the audio and the displays. */
    public void start() {
        synth.start();
        lineOut.start();
        pitchDetector.start();
        scope.start();
        pitchTimer.start();
    }

    /** Stop the audio and the displays. */
    public void stop() {
        pitchTimer.stop();
        scope.stop();
        synth.stop();
    }

    /**
     * Run the example in its own window.
     *
     * @param args not used
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            final PlayPluckedString frame = new PlayPluckedString();
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    frame.stop();
                    System.exit(0);
                }
            });
            frame.setSize(640, 720);
            frame.setVisible(true);
            frame.start();
        });
    }
}
