package com.wizered67.resourcemanager;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Adam on 2/22/2017.
 */
public class Mapping {
    private Map<String, String> identifierToFilename;
    private Map<String, String> filenameToIdentifier;

    public Mapping() {
        identifierToFilename = new HashMap<String, String>();
        filenameToIdentifier = new HashMap<String, String>();
    }

    public void addMapping(String identifier, String filename) {
        identifierToFilename.put(identifier, filename);
        filenameToIdentifier.put(filename, identifier);
    }

    public String getFilename(String identifier) {
        return identifierToFilename.get(identifier);
    }

    public String getIdentifier(String filename) {
        return filenameToIdentifier.get(filename);
    }
}
