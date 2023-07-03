package com.amazon.models;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Map;

/**
 * A class to map the normalizeRules json file to a java object.
 */
@Getter
@Setter
public class NormalizeModel {
    /**
     * List of allowed rules or mappings.
     * We have to include both the rule names and the mappings in here so the json file is parsed properly
     */
    public enum RuleOrMapName {
        TRIM,
        CONVERT_ACCENT,
        EMAIL_ADDRESS_UTIL_NORM,
        LOWERCASE,
        PHONE_NUM_UTIL_NORM,
        REPLACE_WITH_MAP,
        REPLACE_WORD_WITH_MAP,
        ENSURE_PREFIX_WITH_MAP,
        REMOVE_ALL_NON_ALPHA,
        REMOVE_ALL_NON_NUMERIC,
        REMOVE_ALL_NON_ALPHANUMERIC,
        REMOVE_ALL_NON_EMAIL_CHARS,
        REMOVE_ALL_LEADING_ZEROES,
        REMOVE_ALL_CHARS_AFTER_DASH,
        countryMap,
        stateMap,
        numberMap,
        directionMap,
        specialCharacterMap,
        delimiterMap,
        defaultAddressMap,
        addressMap,
        phonePrefixMap,
        none
    }


    /**
     * The fields in the operations list.
     */
    @Getter
    @Setter
    public static class OperationModel {
        private String name;
        private String description;
    }

    /**
     * The rules model, which is a list of rules.
     */
    @Getter
    @Setter
    public static class RuleModel {
        private ArrayList<ArrayList<RuleOrMapName>> rules;
    }

    /**
     * The mappings.
     */
    @Getter
    @Setter
    public static class Mappings {
        private Map<String, String> countryMap;
        private Map<String, String> stateMap;
        private Map<String, String> numberMap;
        private Map<String, String> directionMap;
        private Map<String, String> specialCharacterMap;
        private Map<String, String> delimiterMap;
        private Map<String, String> defaultAddressMap;
        private Map<String, Map<String, String>> addressMap;
        private Map<String, Map<String, String>> phonePrefixMap;
    }

    private ArrayList<OperationModel> operationList;
    private RuleModel name;
    private RuleModel firstName;
    private RuleModel lastName;
    private RuleModel middleName;
    private RuleModel email;
    private RuleModel phone;
    private RuleModel address;
    private RuleModel city;
    private RuleModel state;
    private RuleModel county;
    private RuleModel postal;
    private RuleModel country;
    private Mappings mappings;
}
