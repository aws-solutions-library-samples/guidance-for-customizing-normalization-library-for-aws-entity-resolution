package com.amazon.util;

import org.apache.commons.lang3.NotImplementedException;

import static com.amazon.constants.AddressValidationConstants.INPUT_SOURCE_BUCKET_NAME;
import static com.amazon.constants.AddressValidationConstants.INPUT_SOURCE_FILE_NAME;
import static com.amazon.constants.AddressValidationConstants.OUTPUT_SOURCE_BUCKET_NAME;
import static com.amazon.constants.AddressValidationConstants.PLACE_INDEX_NAME;

public final class Environment {
    private Environment() {
        throw new NotImplementedException("Utility classes should not be instanced.");
    }

    public static String getInputSourceBucketName() {
        return System.getenv(INPUT_SOURCE_BUCKET_NAME);
    }
    public static String getInputSourceFileName() {
        return System.getenv(INPUT_SOURCE_FILE_NAME);
    }
    public static String getPlaceIndexName() {
        return System.getenv(PLACE_INDEX_NAME);
    }
    public static String getOutputSourceBucketName() {
        return System.getenv(OUTPUT_SOURCE_BUCKET_NAME);
    }

}
