/*
 * Copyright 2009 Phil Burk, Mobileer Inc
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

package com.jsyn.util;

import com.jsyn.instruments.SubtractiveSynthVoice;
import com.jsyn.unitgen.UnitVoice;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestVoiceAllocator {
    VoiceAllocator allocator;
    int max = 4;
    UnitVoice[] voices;

    @BeforeEach
    private void beforeEach() {
        voices = new UnitVoice[max];
        for (int i = 0; i < max; i++) {
            voices[i] = new SubtractiveSynthVoice();
        }

        allocator = new VoiceAllocator(voices);
    }

    @Test
    public void testAllocation() {
        assertEquals(max, allocator.getVoiceCount(), "get max");

        int tag1 = 61;
        int tag2 = 62;
        int tag3 = 63;
        int tag4 = 64;
        int tag5 = 65;
        int tag6 = 66;
        UnitVoice voice1 = allocator.allocate(tag1);
        assertTrue((voice1 != null), "voice should be non-null");

        UnitVoice voice2 = allocator.allocate(tag2);
        assertTrue((voice2 != null), "voice should be non-null");
        assertTrue((voice2 != voice1), "new voice ");

        UnitVoice voice = allocator.allocate(tag1);
        assertTrue((voice == voice1), "should be voice1 again ");

        voice = allocator.allocate(tag2);
        assertTrue((voice == voice2), "should be voice2 again ");

        UnitVoice voice3 = allocator.allocate(tag3);
        @SuppressWarnings("unused")
        UnitVoice voice4 = allocator.allocate(tag4);

        UnitVoice voice5 = allocator.allocate(tag5);
        assertTrue((voice5 == voice1), "ran out so get voice1 as oldest");

        voice = allocator.allocate(tag2);
        assertTrue((voice == voice2), "should be voice2 again ");

        // Now voice 3 should be the oldest cuz voice 2 was touched.
        UnitVoice voice6 = allocator.allocate(tag6);
        assertTrue((voice6 == voice3), "ran out so get voice3 as oldest");
    }

    @Test
    public void testOff() {
        int tag1 = 61;
        int tag2 = 62;
        int tag3 = 63;
        int tag4 = 64;
        int tag5 = 65;
        int tag6 = 66;
        UnitVoice voice1 = allocator.allocate(tag1);
        UnitVoice voice2 = allocator.allocate(tag2);
        UnitVoice voice3 = allocator.allocate(tag3);
        UnitVoice voice4 = allocator.allocate(tag4);

        assertTrue(allocator.isOn(tag3), "voice 3 should start on");
        allocator.off(tag3);
        assertFalse(allocator.isOn(tag3), "voice 3 should now be off");

        allocator.off(tag2);

        UnitVoice voice5 = allocator.allocate(tag5);
        assertTrue((voice5 == voice3), "should get voice3 cuz off first");
        UnitVoice voice6 = allocator.allocate(tag6);
        assertTrue((voice6 == voice2), "should get voice2 cuz off second");
        voice3 = allocator.allocate(tag3);
        assertTrue((voice3 == voice1), "should get voice1 cuz on first");

        voice1 = allocator.allocate(tag1);
        assertTrue((voice1 == voice4), "should get voice4 cuz next up");
    }
}
