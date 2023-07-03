package com.amazon.models;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * A class to map the normalizeRulesTests json file to a java object.
 */
@Getter
@Setter
public class NormalizeTestModel {
    /**
     * The address model.
     */
    @Getter
    @Setter
    public static class AddressModel {
        private Map<String, String> direction;
        private Map<String, String> defaultCountry;
        private Map<String, String> us;
        private Map<String, String> it;
        private Map<String, String> es;
        private Map<String, String> fr;
        private Map<String, String> uk;
    }

    /**
     * The unit tests from normalizeRulesTests.json.
     */
    @Getter
    @Setter
    public static class Tests {
        private Map<String, String> firstName;
        private Map<String, String> lastName;
        private Map<String, String> middleName;
        private Map<String, String> email;
        private Map<String, Map<String, String>> phone;
        private Map<String, String> city;
        private Map<String, String> state;
        private Map<String, String> postal;
        private Map<String, String> county;
        private Map<String, String> country;
        private AddressModel address;
    }

    /**
     * The tests.
     */
    private Tests tests;
}
