package com.amazon.test;

import com.amazon.lib.Normalizer;
import com.amazon.models.CustomerRecord;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class NormalizerTest {

    public static void main(String[] args) {
        try {
            Normalizer normalizer = new Normalizer();
            CustomerRecord record = CustomerRecord.builder().address("  address1")
                    .city("  City1")
                    .country("  US")
                    .firstName("  First1")
                    .lastName(" Last1")
                    .email("  Tumble@tumble.com ")
                    .phone(" 1-888-123-4567  ")
                    .postal(" 12345  ")
                    .state("  AlabaMA  ")
                    .build();

            record = normalizer.normalizeRecord(record);

            assertEquals("address1", record.getAddress());

            assertEquals("city1", record.getCity());
            assertEquals("us", record.getCountry());
            assertEquals("first", record.getFirstName());
            assertEquals("last", record.getLastName());
            assertEquals("tumble@tumble.com", record.getEmail());
            assertEquals("18881234567", record.getPhone());
            assertEquals("12345", record.getPostal());
            assertEquals("al", record.getState());

        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }
}
