/*
 * Copyright 1999 Phil Burk, Mobileer Inc
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

package com.softsynth.util;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Enumeration;
import java.util.Vector;

class WildcardFilenameFilter implements FilenameFilter {
    String pattern;

    public WildcardFilenameFilter(String pattern) {
        this.pattern = pattern;
    }

    /* abc*frog*xyz =?= abctreefroglegxyz */
    static boolean wildMatch(String pattern, String candidate) {
        int starPos = pattern.indexOf('*');
        // System.out.println("------pattern = " + pattern + ", candidate = " + candidate +
        // ", starPos = " + starPos );
        if (starPos < 0)
            return (pattern.equalsIgnoreCase(candidate));
        else if (starPos > 0) {
            // System.out.println("------ beginning, pattern = " + pattern + ", candidate = " +
            // candidate );
            if (pattern.regionMatches(true, 0, candidate, 0, starPos)) {
                /* Recursively check remainder of string. */
                return wildMatch(pattern.substring(starPos), candidate.substring(starPos));
            }
            return false;
        } else {
            /* Compare remainder of string. */
            // System.out.println("------ remainder, pattern = " + pattern + ", candidate = " +
            // candidate );
            if (pattern.regionMatches(true, 1, candidate, 0, candidate.length()))
                return true;

            for (int i = 0; i < candidate.length(); i++) {
                if (wildMatch(pattern.substring(1), candidate.substring(i)))
                    return true;
            }
        }
        return false;
    }

    @Override
    public boolean accept(File dir, String name) {
        return wildMatch(pattern, name);
    }
}

/**
 * Wildcard search for files in directory.
 * 
 * @author Phil Burk (C) 1999 SoftSynth.com
 */

public class FileSearch {
    /**
     * Expand vector of filenames that may contain wildcards into a new vector of filenames without
     * wildcards.
     */
    public static Vector expandFilenames(Vector filenames) {
        Vector expandedFilenames = new Vector();
        Enumeration e = filenames.elements();
        while (e.hasMoreElements()) {
            expandFilename((String) e.nextElement(), expandedFilenames);
        }
        return expandedFilenames;
    }

    /**
     * Expand filename that may contain wildcards and add to a vector of filenames without
     * wildcards.
     */
    public static void expandFilename(String filename, Vector expandedFilenames) {
        int lastSepPos = filename.lastIndexOf(File.separatorChar);

        // System.out.println("expandFilename: separator = " + File.separatorChar + " , index = " +
        // lastSepPos );

        int starPos = filename.indexOf('*');
        if (starPos < 0) {
            expandedFilenames.addElement(filename);
        } else if (starPos < lastSepPos) {
            TextOutput.error("Wildcard * not allowed in directory names! " + filename);
        } else {
            String parent;
            String wildFile;
            if (lastSepPos < 0) {
                parent = ".";
                wildFile = filename;
            } else {
                parent = filename.substring(0, lastSepPos);
                wildFile = filename.substring(lastSepPos + 1);
            }

            File dir = new File(parent);
            // System.out.println("expandFilename: parent = " + parent + " , dir = " + dir );
            if (!dir.exists()) {
                TextOutput.error("Invalid directory = " + parent);
            }

            String[] files = dir.list(new WildcardFilenameFilter(wildFile));
            if (files == null)
                return;
            for (int i = 0; i < files.length; i++) {
                expandedFilenames.addElement(parent + File.separatorChar + files[i]);
            }
        }
    }

    /**
     * Extract file name from full pathname. For example, if pathname is "/usr/data/report.txt" then
     * the output will be "report.txt".
     */
    public static String removeParent(String pathName) {
        int lastSepPos = pathName.lastIndexOf(File.separatorChar);
        if (lastSepPos < 0) {
            return pathName;
        } else {
            return pathName.substring(lastSepPos + 1);
        }
    }

    /**
     * Remove any dot extension from file name. For example, if pathname is "/usr/data/report.txt"
     * then the output will be "/usr/data/report".
     */
    public static String removeExtension(String pathName) {
        int lastDotPos = pathName.lastIndexOf('.');
        if (lastDotPos > 0) {
            return pathName.substring(0, lastDotPos);
        } else {
            return pathName;
        }
    }

    public static void main(String args[]) {
        System.out.println("FileSearch - by Phil Burk");
        boolean result;
        result = WildcardFilenameFilter.wildMatch("abc*frog*xyz", "abctreefroglegxyz");
        System.out.println("result = " + result);
        result = WildcardFilenameFilter.wildMatch("abc*frog*xyz", "abctreefrxglegxyz");
        System.out.println("result = " + result);

        test();
    }

    static void test() {
        Vector expandedFilenames = new Vector();
        expandFilename("../data/ov*.mid", expandedFilenames);
        Enumeration e = expandedFilenames.elements();
        while (e.hasMoreElements()) {
            String name = (String) e.nextElement();
            System.out.println("file = " + name);
        }
    }

    /********************************************************
     * Create directory if it doesn't exist.
     */
    public static void createDirectoryIfNeeded(String directoryName) throws SecurityException {
        createDirectoryIfNeeded(new File(directoryName));
    }

    /********************************************************
     * Create directory with the given name if it doesn't exist.
     */
    public static void createDirectoryIfNeeded(File dir) throws SecurityException {
        if (!dir.isDirectory()) {
            if (!dir.mkdirs()) {
                TextOutput.error("Could not make output directory " + dir.getAbsolutePath());
            }
        }
    }

}
