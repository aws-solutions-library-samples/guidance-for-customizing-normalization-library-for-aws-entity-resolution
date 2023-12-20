package com.amazon.models;

import lombok.Data;

@Data
public class AddressValidationInput {
    private String outputFileName;
    private String addressFieldName;
    private String sourceFieldName;
}
