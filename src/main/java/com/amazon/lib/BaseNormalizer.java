package com.amazon.lib;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.amazon.models.NormalizeModel;
import com.amazon.util.Operations;

import javax.annotation.Nullable;


/**
 * Base normalizer class to abstract loading of normalizer rules.
 */
@NoArgsConstructor
@Slf4j
public class BaseNormalizer implements Serializable {
    final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public final static NormalizeModel normalizeRules;
    static final String rulesFileName = "normalizeRules.json";

    static{
        try {
            final ClassLoader classLoader = BaseNormalizer.class.getClassLoader();
            final InputStream inputStream = classLoader.getResourceAsStream(rulesFileName);
            if (inputStream == null) {
                throw new FileNotFoundException();
            }
            normalizeRules = OBJECT_MAPPER.readValue(inputStream, NormalizeModel.class);
        } catch (NullPointerException | IOException e) {
            log.error("Error getting normalizeRules file {}", rulesFileName);
            throw new RuntimeException(e);
        }
    }

    /**
     * A general method for normalizing an element.
     * @param element - an input element
     * @param elementRules - the normalization rules for the element
     * @param country - country of the record element
     * @return - the normalized element
     */
    public String normalizeElement(
        @Nullable String element,
        ArrayList<ArrayList<NormalizeModel.RuleOrMapName>> elementRules,
        String country
    ) {
        if (element == null || element.isEmpty()) {
            return element;
        }

        String resultElement = element;
        for (ArrayList<NormalizeModel.RuleOrMapName> rule : elementRules) {
            if (rule.isEmpty()) {
                continue;
            }

            Map<String, String> ruleMap = getMap(rule, normalizeRules.getMappings(), country);
            resultElement = Operations.pickOperation(resultElement, rule.get(0), ruleMap, country);
        }

        return resultElement;
    }

    /**
     * A general method for normalizing an element.
     * @param element - an input element
     * @param elementRules - the normalization rules for the element
     * @return - the normalized element
     */
    public String normalizeElement(String element, ArrayList<ArrayList<NormalizeModel.RuleOrMapName>> elementRules) {
        return normalizeElement(element, elementRules, "");
    }

    /**
     * Gets a mapping for a rule, if there is one.
     * @param rule - a list of the rule name and mapping
     * @param mappings - all of the mappings from the normalizeRules json file
     * @param country - country
     * @return - a mapping
     */
    @SuppressWarnings("checkstyle:CyclomaticComplexity")
    private Map<String, String> getMap(
        ArrayList<NormalizeModel.RuleOrMapName> rule,
        NormalizeModel.Mappings mappings,
        String country
    ) {
        if (rule.size() < 2) {
            return new HashMap<>();
        }

        NormalizeModel.RuleOrMapName mapName = rule.get(1);
        switch (mapName) {
            case countryMap:
                return mappings.getCountryMap();
            case stateMap:
                return mappings.getStateMap();
            case numberMap:
                return mappings.getNumberMap();
            case directionMap:
                return mappings.getDirectionMap();
            case specialCharacterMap:
                return mappings.getSpecialCharacterMap();
            case delimiterMap:
                return mappings.getDelimiterMap();
            case defaultAddressMap:
                return mappings.getDefaultAddressMap();
            case addressMap:
                if (mappings.getAddressMap().containsKey(country)) {
                    return mappings.getAddressMap().get(country);
                }
                return new HashMap<>();
            case phonePrefixMap:
                if (mappings.getPhonePrefixMap().containsKey(country)) {
                    return mappings.getPhonePrefixMap().get(country);
                }
                return new HashMap<>();
            default:
                return new HashMap<>();
        }
    }
}