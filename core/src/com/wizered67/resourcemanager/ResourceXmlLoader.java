package com.wizered67.resourcemanager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.XmlReader;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Adam on 2/22/2017.
 */
public class ResourceXmlLoader {
    private static final Pattern RESOURCE_PATTERN = Pattern.compile("\\s*(.+)\\s+\"(.+)\"\\s*");
    private static XmlReader.Element root;
    static {
        FileHandle xmlFile = Gdx.files.local(Resources.RESOURCES_XML);
        MixedXmlReader xmlReader = new MixedXmlReader();
        try {
            root = xmlReader.parse(xmlFile);
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    public static Mapping getResources(String type) {
        Mapping mapping = new Mapping();
        XmlReader.Element files = root.getChildByName(type);
        for (int i = 0; i < files.getChildCount(); i += 1) {
            XmlReader.Element child = files.getChild(i);
            if (child.getName().equals("text")) {
                String text = child.getText();
                text = text.replaceAll("\\r", "");
                String[] lines = text.split("\\n");
                for (String line : lines) {
                    line = line.trim();
                    Matcher matcher = RESOURCE_PATTERN.matcher(line);
                    String identifier, filename;
                    if (matcher.matches()) {
                        identifier = matcher.group(1);
                        filename = matcher.group(2);
                    } else {
                        line = line.replaceAll("\"", "");
                        identifier = line;
                        filename = line;
                    }
                    if (mapping.getFilename(identifier) != null) {
                        identifierError(identifier);
                    }
                    mapping.addMapping(identifier, filename);
                }
            }
        }
        return mapping;
    }

    private static void identifierError(String id) {
        System.err.println("Identifier '" + id + "' is already in use.");
    }
}
