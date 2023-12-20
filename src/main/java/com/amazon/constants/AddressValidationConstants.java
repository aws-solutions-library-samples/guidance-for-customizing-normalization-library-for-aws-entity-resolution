package com.amazon.constants;

import org.apache.commons.lang3.NotImplementedException;

public final class AddressValidationConstants {

    private AddressValidationConstants() {
        throw new NotImplementedException("Utility classes should not be instanced.");
    }

    public static final String INPUT_SOURCE_ARN = "INPUT_SOURCE_ARN";
    public static final String INPUT_SOURCE_BUCKET_NAME = "INPUT_SOURCE_BUCKET_NAME";
    public static final String INPUT_SOURCE_FILE_NAME = "INPUT_SOURCE_FILE_NAME";
    public static final String PLACE_INDEX_NAME = "PLACE_INDEX_NAME";
    public static final String OUTPUT_SOURCE_BUCKET_NAME = "OUTPUT_SOURCE_BUCKET_NAME";
}
