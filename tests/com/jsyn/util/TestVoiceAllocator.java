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

import junit.framework.TestCase;

import com.jsyn.instruments.SubtractiveSynthVoice;
import com.jsyn.unitgen.UnitVoice;

public class TestVoiceAllocator extends TestCase {
    VoiceAllocator allocator;
    int max = 4;
    private UnitVoice[] voices;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        voices = new UnitVoice[max];
        for (int i = 0; i < max; i++) {
            voices[i] = new SubtractiveSynthVoice();
        }

        allocator = new VoiceAllocator(voices);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testAllocation() {
        assertEquals("get max", max, allocator.getVoiceCount());

        int tag1 = 61;
        int tag2 = 62;
        int tag3 = 63;
        int tag4 = 64;
        int tag5 = 65;
        int tag6 = 66;
        UnitVoice voice1 = allocator.allocate(tag1);
        assertTrue("voice should be non-null", (voice1 != null));

        UnitVoice voice2 = allocator.allocate(tag2);
        assertTrue("voice should be non-null", (voice2 != null));
        assertTrue("new voice ", (voice2 != voice1));

        UnitVoice voice = allocator.allocate(tag1);
        assertTrue("should be voice1 again ", (voice == voice1));

        voice = allocator.allocate(tag2);
        assertTrue("should be voice2 again ", (voice == voice2));

        UnitVoice voice3 = allocator.allocate(tag3);
        @SuppressWarnings("unused")
        UnitVoice voice4 = allocator.allocate(tag4);

        UnitVoice voice5 = allocator.allocate(tag5);
        assertTrue("ran out so get voice1 as oldest", (voice5 == voice1));

        voice = allocator.allocate(tag2);
        assertTrue("should be voice2 again ", (voice == voice2));

        // Now voice 3 should be the oldest cuz voice 2 was touched.
        UnitVoice voice6 = allocator.allocate(tag6);
        assertTrue("ran out so get voice3 as oldest", (voice6 == voice3));
    }

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

        assertTrue("voice 3 should start on", allocator.isOn(tag3));
        allocator.off(tag3);
        assertEquals("voice 3 should now be off", false, allocator.isOn(tag3));

        allocator.off(tag2);

        UnitVoice voice5 = allocator.allocate(tag5);
        assertTrue("should get voice3 cuz off first", (voice5 == voice3));
        UnitVoice voice6 = allocator.allocate(tag6);
        assertTrue("should get voice2 cuz off second", (voice6 == voice2));
        voice3 = allocator.allocate(tag3);
        assertTrue("should get voice1 cuz on first", (voice3 == voice1));

        voice1 = allocator.allocate(tag1);
        assertTrue("should get voice4 cuz next up", (voice1 == voice4));
    }
}
