package com.amazon.util;

import com.amazon.models.NormalizeModel;

import java.text.Normalizer;
import java.util.Map;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.amazon.constants.NormalizerConstants;
import org.apache.commons.lang3.StringUtils;

/**
 * All of the operations that can be performed to normalize data.
 */
public final class Operations {

    private static final PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();

    private Operations() { }

    /**
     * Takes an operation name/mapping and returns the string after performing the operation.
     * @param element - the input element
     * @param operation - the operation to perform
     * @param map - the mappings (will be an empty map if there is no mapping for the rule)
     * @return - the modified string
     */
    @SuppressWarnings("checkstyle:cyclomaticcomplexity")
    public static String pickOperation(String element,
                                       NormalizeModel.RuleOrMapName operation,
                                       Map<String, String> map,
                                       String country) {
        switch (operation) {
            case TRIM:
                return operationTrim(element);
            case CONVERT_ACCENT:
                return operationConvertAccent(element);
            case EMAIL_ADDRESS_UTIL_NORM:
                return operationEmailAddressNorm(element);
            case LOWERCASE:
                return operationLowercase(element);
            case PHONE_NUM_UTIL_NORM:
                return operationPhoneNumUtilNorm(element, country);
            case REMOVE_ALL_NON_ALPHA:
                return operationRemoveAllNonAlpha(element);
            case REMOVE_ALL_NON_NUMERIC:
                return operationRemoveAllNonNumeric(element);
            case REMOVE_ALL_NON_ALPHANUMERIC:
                return operationRemoveAllNonAlphaNumeric(element);
            case REMOVE_ALL_NON_EMAIL_CHARS:
                return operationRemoveAllNonEmailChars(element);
            case REMOVE_ALL_LEADING_ZEROES:
                return operationRemoveAllLeadingZeroes(element);
            case REMOVE_ALL_CHARS_AFTER_DASH:
                return operationRemoveAllCharsAfterDash(element);
            case REPLACE_WITH_MAP:
                return operationReplaceWithMap(element, map);
            case REPLACE_WORD_WITH_MAP:
                return operationReplaceWordWithMap(element, map);
            case ENSURE_PREFIX_WITH_MAP:
                return operationEnsurePrefixWithMap(element, map);
            default:
                return element;
        }
    }

    /**
     * Trim the whitespace of a string.
     * @param element - the input element
     * @return - the modified string
     */
    private static String operationTrim(String element) {
        return element.trim();
    }

    /**
     * Covert accented letter to regular letter.
     * @param element - the input element
     * @return - the modified string
     */
    private static String operationConvertAccent(String element) {
        // Special handling for a Norwegian letter that's not covered by library
        final String updatedElement = element.replaceAll("å", "aa");
        return Normalizer.normalize(updatedElement, Normalizer.Form.NFD);
    }

    /**
     * Returns a lowercase string.
     * @param element - the input element
     * @return - the modified string
     */
    private static String operationLowercase(String element) {
        return element.toLowerCase();
    }

    /**
     * If the phone number is in E.164 format, use the national format phone number as the key.
     * e.g. "+1 (206) 345-7890" -> "2063457890",
     *      "+1206.345.7890"    -> "2063457890"
     *      "+1-206-345-7890  " -> "2063457890"
     * If the phone number is not in E.164 format, extract digits from the string, and ignore other characters.
     * e.g. "(206) 345-7890"    -> "2063457890",
     *      "206.345.7890"      -> "2063457890"
     *      "abc206-345-7890.." -> "2063457890"
     *
     * What is E.164?: https://en.wikipedia.org/wiki/E.164
     * Doc: https://tiny.amazon.com/nfxtwxd6/javaiodocgooglibplibplate
     */
    /** Returns the normalized phone number by invoking the external PhoneNumberUtil library based normalization
     *
     * @param element - the input element
     * @param country - the country code or parsing
     * @return - the modified string
     */
    private static String operationPhoneNumUtilNorm(String element, String country){

        try {
            if (NormalizerConstants.usCountryCode.equalsIgnoreCase(country) || StringUtils.isBlank(country)) {
                final Phonenumber.PhoneNumber phoneNumber = phoneNumberUtil.parse(element, NormalizerConstants.usCountryCode);
                if (phoneNumberUtil.isValidNumber(phoneNumber)) { // If a given phone number is in E.164 format, it is valid.
                    return String.valueOf(phoneNumber.getNationalNumber()); // returns the National number by removing the country code.
                }
            }
        } catch (NumberParseException e){
            // If not able to parse, move ahead in removing non-numeric
        }

        return element.replaceAll("\\D+", "");
    }

    /**
     * Removes all non alpha characters.
     * @param element - the input element
     * @return - the modified string
     */
    private static String operationRemoveAllNonAlpha(String element) {
        return element.replaceAll("[^a-zA-Z]", "");
    }

    /**
     * Removes all non numeric characters.
     * @param element - the input element
     * @return - the modified string
     */
    private static String operationRemoveAllNonNumeric(String element) {
        return element.replaceAll("[^0-9]", "");
    }

    /**
     * Removes all non alphanumeric characters.
     * @param element - the input element
     * @return - the modified string
     */
    private static String operationRemoveAllNonAlphaNumeric(String element) {
        return element.replaceAll("[^a-zA-Z0-9]", "");
    }

    /**
     * Removes all non email characters.
     * @param element - the input element
     * @return - the modified string
     */
    private static String operationRemoveAllNonEmailChars(String element) {
        return element.replaceAll("[^\\w.@-]+", "");
    }


    /**
     * Perform email address specific normalization.
     * - remove everything that is outside of the first "<>"
     * - replace all upper case chars to lower case
     * - remove white space
     * - replace (AT)/(at)/(aT)/(At) with @
     * @param emailAddress
     * @return normalized emailAddress
     */
    private static String operationEmailAddressNorm(String emailAddress){
        String processedEmailAddress = StringUtils.substringBetween(emailAddress, "<", ">");
        processedEmailAddress = processedEmailAddress == null ? emailAddress : processedEmailAddress;

        return processedEmailAddress.toLowerCase().replaceAll("\\s+", "").replaceAll("\\(at\\)", "@");
    }

    /**
     * Removes all leading zeroes in a string
     * @param element - the input element
     * @return - the modified string
     */
    private static String operationRemoveAllLeadingZeroes(String element) {
        int firstNonZeroIndex = 0;
        for (int i = 0; i < element.length(); i++) {
            if (element.charAt(i) != '0') {
                firstNonZeroIndex = i;
                break;
            }
        }
        return element.substring(firstNonZeroIndex);
    }

    /**
     * Removes all characters after a dash, and the dash itself
     * @param element - the input element
     * @return - the modified string
     */
    private static String operationRemoveAllCharsAfterDash(String element) {
        int indexOfDash = element.indexOf('-');
        if (indexOfDash == -1) {
            return element;
        }

        return element.substring(0, indexOfDash);
    }

    /**
     * If the element does not start with the prefix in the map, then make it start
     * @param element - the input element
     * @param map - the mapping
     * @return - the modified string
     */
    private static String operationEnsurePrefixWithMap(String element, Map<String, String> map) {
        if (map == null || map.isEmpty()) {
            return element;
        }

        String resultElement = element;
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (!element.startsWith(entry.getKey())) {
                resultElement = entry.getValue() + resultElement;
            }
        }

        return resultElement;
    }

    /**
     * Does a string replace for every string that's present in the map.
     *   - Used mainly for replacing specific characters/delimiters.
     * @param element - the input element
     * @param map - the mapping
     * @return - the modified string
     */
    private static String operationReplaceWithMap(String element, Map<String, String> map) {
        //Iterating over the entire map and doing a string replace.  The maps that use this are small,
        //it's meant for replacing characters and delimiters, not words.
        String newElement = element;
        for (Map.Entry<String, String> entry : map.entrySet()) {
            newElement = newElement.replace(entry.getKey(), entry.getValue());
        }

        return newElement;
    }


    /**
     * Splits the element by space, and looks up each word in the given map, replacing if it is found.
     *   - This avoids replacing a word that appears inside of another word.
     * @param element - the input element
     * @param map - the mapping
     * @return - the modified string
     */
    private static String operationReplaceWordWithMap(String element, Map<String, String> map) {
        //Split the element by space so we can iterate over the words to see if they are in the map.
        String[] elementList = element.split(" ");
        for (int i = 0; i < elementList.length; i++) {
            String item = elementList[i];
            if (map.containsKey(item)) {
                elementList[i] = map.get(item);
            }
        }

        //joining with spaces so the next map can split on space too
        return String.join(" ", elementList);
    }
}
