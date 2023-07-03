package com.amazon.lib;

import java.io.IOException;
import java.io.Serializable;

import lombok.extern.slf4j.Slf4j;
import com.amazon.models.CustomerRecord;

/**
 * A class to normalize data specific for Locke usecase.
 */
@Slf4j
public class Normalizer extends BaseNormalizer implements Serializable {
    /**
     * Constructor for Normalize.
     * @throws IOException - throws exception with the normalizeRules.json file
     */
    public Normalizer() throws IOException {
        super();
    }

    /**
     * Normalize a record.
     * @param record - an input record
     * @return - a normalized record
     */
    public CustomerRecord normalizeRecord(CustomerRecord record) {
        record.setFirstName(normalizeFirstName(record.getFirstName()));
        record.setLastName(normalizeLastName(record.getLastName()));
        record.setMiddleName(normalizeMiddleName(record.getMiddleName()));
        record.setEmail(normalizeEmail(record.getEmail()));

        // Need to do country before address/phone, since address/phone needs to the normalized country
        record.setCountry(normalizeCountry(record.getCountry()));
        record.setPhone(normalizePhone(record.getPhone(), record.getCountry()));
        record.setAddress(normalizeAddress(record.getAddress(), record.getCountry()));
        record.setCity(normalizeCity(record.getCity()));
        record.setCounty(normalizeCounty(record.getCounty()));
        record.setPostal(normalizePostal(record.getPostal()));
        record.setState(normalizeState(record.getState()));

        return record;
    }

    /**
     * Normalize an input address.
     * @param address - the input address
     * @param country - the input country
     * @return - a normalized address
     */
    public String normalizeAddress(String address, String country) {
        return normalizeElement(address, normalizeRules.getAddress().getRules(), country);
    }

    /**
     * Normalize an input city.
     * @param city - the input city
     * @return - a normalized city
     */
    public String normalizeCity(String city) {
        return normalizeElement(city, normalizeRules.getCity().getRules());
    }

    /**
     * Normalize a country.
     * @param country - the input country
     * @return - a normalized country
     */
    public String normalizeCountry(String country) {
        return normalizeElement(country, normalizeRules.getCountry().getRules());
    }

    /**
     * Normalize a county.
     * @param county - the input county
     * @return - a normalized county
     */
    public String normalizeCounty(String county) {
        return normalizeElement(county, normalizeRules.getCounty().getRules());
    }

    /**
     * Normalize an email.
     * @param email - the input email
     * @return - a normalized email
     */
    public String normalizeEmail(String email) {
        return normalizeElement(email, normalizeRules.getEmail().getRules());
    }

    /**
     * Normalize a first name.
     * @param firstName - the input first name
     * @return - a normalized first name
     */
    public String normalizeFirstName(String firstName) {
        return normalizeElement(firstName, normalizeRules.getFirstName().getRules());
    }

    /**
     * Normalize a last name.
     * @param lastName - the input last name
     * @return - a normalized last name
     */
    public String normalizeLastName(String lastName) {
        return normalizeElement(lastName, normalizeRules.getLastName().getRules());
    }

    /**
     * Normalize a middle name.
     * @param middleName - the input last name
     * @return - a normalized last name
     */
    public String normalizeMiddleName(String middleName) {
        return normalizeElement(middleName, normalizeRules.getMiddleName().getRules());
    }

    /**
     * Normalize a phone.
     * @param phone - the input phone number
     * @param country - the country
     * @return - a normalized phone number
     */
    public String normalizePhone(String phone, String country) {
        return normalizeElement(phone, normalizeRules.getPhone().getRules(), country);
    }

    /**
     * Normalize a phone.
     * @param phone - the input phone number
     * @return - a normalized phone number
     */
    public String normalizePhone(String phone) {
        return normalizePhone(phone, "");
    }

    /**
     * Normalize a postal code.
     * @param postal - the input postal code
     * @return - a normalized postal code
     */
    public String normalizePostal(String postal) {
        return normalizeElement(postal, normalizeRules.getPostal().getRules());
    }

    /**
     * Normalize a state.
     * @param state - the input state
     * @return - a normalized state
     */
    public String normalizeState(String state) {
        return normalizeElement(state, normalizeRules.getState().getRules());
    }
}
