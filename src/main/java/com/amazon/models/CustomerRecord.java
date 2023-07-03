package com.amazon.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * A record to be normalized.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class CustomerRecord implements Serializable {
    private String firstName;
    private String lastName;
    private String middleName;
    private String email;
    private String phone;
    private String city;
    private String address;
    private String state;
    private String county;
    private String postal;
    private String country;
}
