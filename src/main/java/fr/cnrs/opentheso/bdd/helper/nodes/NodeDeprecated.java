package fr.cnrs.opentheso.bdd.helper.nodes;

import java.text.Normalizer;

/**
 *
 * @author miledrousset
 */
public class NodeDeprecated implements Comparable {

    private String deprecatedId;
    private String deprecatedLabel;
    private String replacedById;
    private String replacedByLabel;
    
    public NodeDeprecated() {
    }

    public String getDeprecatedId() {
        return deprecatedId;
    }

    public void setDeprecatedId(String deprecatedId) {
        this.deprecatedId = deprecatedId;
    }

    public String getDeprecatedLabel() {
        return deprecatedLabel;
    }

    public void setDeprecatedLabel(String deprecatedLabel) {
        this.deprecatedLabel = deprecatedLabel;
    }

    public String getReplacedById() {
        return replacedById;
    }

    public void setReplacedById(String replacedById) {
        this.replacedById = replacedById;
    }

    public String getReplacedByLabel() {
        return replacedByLabel;
    }

    public void setReplacedByLabel(String replacedByLabel) {
        this.replacedByLabel = replacedByLabel;
    }
    
    
    @Override
    public int compareTo(Object o) {
        String str1, str2;
        str1 = Normalizer.normalize(this.deprecatedLabel, Normalizer.Form.NFD);
        str1 = str1.replaceAll("[^\\p{ASCII}]", "");
        str2 = Normalizer.normalize(((NodeDeprecated)o).deprecatedLabel, Normalizer.Form.NFD);
        str2 = str2.replaceAll("[^\\p{ASCII}]", "");
        return naturalCompare(str1, str2, true);
        //return str1.toUpperCase().compareTo(str2.toUpperCase());
    }

    private int naturalCompare(String a, String b, boolean ignoreCase) {
        if (ignoreCase) {
            a = a.toLowerCase();
            b = b.toLowerCase();
        }
        int aLength = a.length();
        int bLength = b.length();
        int minSize = Math.min(aLength, bLength);
        char aChar, bChar;
        boolean aNumber, bNumber;
        boolean asNumeric = false;
        int lastNumericCompare = 0;
        for (int i = 0; i < minSize; i++) {
            aChar = a.charAt(i);
            bChar = b.charAt(i);
            aNumber = aChar >= '0' && aChar <= '9';
            bNumber = bChar >= '0' && bChar <= '9';
            if (asNumeric)
                if (aNumber && bNumber) {
                    if (lastNumericCompare == 0)
                        lastNumericCompare = aChar - bChar;
                } else if (aNumber)
                    return 1;
                else if (bNumber)
                    return -1;
                else if (lastNumericCompare == 0) {
                    if (aChar != bChar)
                        return aChar - bChar;
                    asNumeric = false;
                } else
                    return lastNumericCompare;
            else if (aNumber && bNumber) {
                asNumeric = true;
                if (lastNumericCompare == 0)
                    lastNumericCompare = aChar - bChar;
            } else if (aChar != bChar)
                return aChar - bChar;
        }
        if (asNumeric)
            if (aLength > bLength && a.charAt(bLength) >= '0' && a.charAt(bLength) <= '9') // as number
                return 1;  // a has bigger size, thus b is smaller
            else if (bLength > aLength && b.charAt(aLength) >= '0' && b.charAt(aLength) <= '9') // as number
                return -1;  // b has bigger size, thus a is smaller
            else if (lastNumericCompare == 0)
              return aLength - bLength;
            else
                return lastNumericCompare;
        else
            return aLength - bLength;
    }        


}
