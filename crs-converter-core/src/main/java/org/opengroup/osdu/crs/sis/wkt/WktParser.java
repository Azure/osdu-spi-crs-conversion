package org.opengroup.osdu.crs.sis.wkt;

import java.util.ArrayList;
import java.util.List;

public class WktParser {
private static final int CR_CHAR = 13;
    private static final int LF_CHAR = 10;

    public WktSection parseWkt(String wktString) {
        return parseSection(wktString);
    }

    private WktSection parseSection(String sectionString) {
        int sectionStartIndex = sectionString.indexOf("[");
        int sectionEndIndex = sectionString.lastIndexOf("]");
        if (sectionStartIndex == -1) {
            return null;
        }
        if (sectionEndIndex == -1) {
            throw new IllegalArgumentException("Invalid wkt string");
        }
        String type = "";
        if (sectionStartIndex > 0) {
            type = sectionString.substring(0, sectionStartIndex);
        }
        String sectionNoType = sectionString.substring(sectionStartIndex + 1, sectionEndIndex);
        List<String> tokens = getTokens(sectionNoType);
        List<IWktAttribute> attributes = new ArrayList<>();
        List<WktSection> subSections = new ArrayList<>();
        for (String currentToken : tokens) {
            if (isSection(currentToken)) {
                WktSection currentSubSection = parseSection(currentToken);
                subSections.add(currentSubSection);
            } else {
                IWktAttribute currentAttribute = parseAttribute(currentToken);
                attributes.add(currentAttribute);
            }
        }
        return new WktSection(type, attributes, subSections);
    }

    private IWktAttribute parseAttribute(String token) {
        if (token.startsWith("\"")) {
            return new WktStringAttribute(token);
        }
        try {
            double value = Double.parseDouble(token);
            return new WktDoubleAttribute(value);
        } catch (NumberFormatException ex) {
            return new WktStringAttribute(token);
        }
    }

    private boolean isSection(String token) {
        int numberOfQuotes = 0;
        boolean startBracketFound = false;
        boolean endBracketFound = false;
        for (int i = 0; i < token.length(); i++) {
            char currentCharacter = token.charAt(i);
            if (currentCharacter == '"') {
                numberOfQuotes++;
            }
            if (currentCharacter == '[' && numberOfQuotes % 2 == 0) {
                startBracketFound = true;
            }
            if (currentCharacter == ']' && numberOfQuotes % 2 == 0) {
                endBracketFound = true;
            }
        }
        if (startBracketFound && endBracketFound) {
            return true;
        }
        return false;
    }

    private List<String> getTokens(String sectionString) {
        List<String> tokens = new ArrayList<>();
        StringBuilder currentToken = new StringBuilder();
        int numberOfQuotes = 0;
        int bracketCount = 0;
        for (int i = 0; i < sectionString.length(); i++) {
            char currentCharacter = sectionString.charAt(i);
            if (currentCharacter == '"') {
                numberOfQuotes++;
            }
            if (currentCharacter == CR_CHAR && numberOfQuotes % 2 == 0) {
                continue;
            }
            if (currentCharacter == LF_CHAR && numberOfQuotes % 2 == 0) {
                continue;
            }
            if (currentCharacter == ' ' && numberOfQuotes % 2 == 0) {
                continue;
            }
            if (currentCharacter == '[') {
                bracketCount++;
            }
            if (currentCharacter == ']') {
                bracketCount--;
            }
            if (currentCharacter == ',' && numberOfQuotes % 2 == 0 && bracketCount == 0) {
                tokens.add(currentToken.toString());
                currentToken = new StringBuilder();
                numberOfQuotes = 0;
                continue;
            }
            currentToken.append(currentCharacter);
        }
        if (currentToken.length() > 0) {
            tokens.add(currentToken.toString());
        }
        return tokens;
    }
}
