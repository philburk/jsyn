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

public class JavaTools {

    @SuppressWarnings("rawtypes")
    public static Class loadClass(String className, boolean verbose) {
        Class newClass = null;
        try {
            newClass = Class.forName(className);
        } catch (Throwable e) {
            if (verbose)
                System.out.println("Caught " + e);
        }
        if (newClass == null) {
            try {
                ClassLoader systemLoader = ClassLoader.getSystemClassLoader();
                newClass = Class.forName(className, true, systemLoader);
            } catch (Throwable e) {
                if (verbose)
                    System.out.println("Caught " + e);
            }
        }
        return newClass;
    }

    /**
     * First try Class.forName(). If this fails, try Class.forName() using
     * ClassLoader.getSystemClassLoader().
     * 
     * @return Class or null
     */
    @SuppressWarnings("rawtypes")
    public static Class loadClass(String className) {
        /**
         * First try Class.forName(). If this fails, try Class.forName() using
         * ClassLoader.getSystemClassLoader().
         * 
         * @return Class or null
         */
        return loadClass(className, true);
    }

}
