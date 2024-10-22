package com.amazon.lambda;

import com.amazon.util.Environment;
import com.amazon.models.AddressValidationInput;
import com.amazon.models.SelectObjectContentHandler;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.location.AmazonLocation;
import com.amazonaws.services.location.AmazonLocationClientBuilder;
import com.amazonaws.services.location.model.SearchForTextResult;
import com.amazonaws.services.location.model.SearchPlaceIndexForTextRequest;
import com.amazonaws.services.location.model.SearchPlaceIndexForTextResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.CSVInput;
import software.amazon.awssdk.services.s3.model.CompressionType;
import software.amazon.awssdk.services.s3.model.ExpressionType;
import software.amazon.awssdk.services.s3.model.FileHeaderInfo;
import software.amazon.awssdk.services.s3.model.InputSerialization;
import software.amazon.awssdk.services.s3.model.JSONOutput;
import software.amazon.awssdk.services.s3.model.OutputSerialization;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.RecordsEvent;
import software.amazon.awssdk.services.s3.model.SelectObjectContentEventStream.EventType;
import software.amazon.awssdk.services.s3.model.SelectObjectContentRequest;

public class AddressValidationHandler implements RequestHandler<AddressValidationInput, AddressValidationInput> {

    private static final String INPUT_BUCKET_NAME = Environment.getInputSourceBucketName();
    private static final String OUTPUT_BUCKET_NAME = Environment.getOutputSourceBucketName();
    private static final String INPUT_FILE_NAME = Environment.getInputSourceFileName();
    private static final String PLACE_INDEX_NAME = Environment.getPlaceIndexName();
    private static final String NORMALIZED_LABEL_FIELD = "normalizedlabelfield";

    private final AwsCredentialsProvider credentialsProvider = DefaultCredentialsProvider.create();
    private final S3AsyncClient s3AsyncClient = S3AsyncClient.builder()
                                                             .credentialsProvider(credentialsProvider)
                                                             .build();
    private final AmazonLocation locationClient = AmazonLocationClientBuilder.defaultClient();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    static {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * 1. Loads data from S3 file named 'entity_resolution_address.csv'
     * 2. Parses and reformats data into a list representing rows in a table
     * 3. For each row in list, validate address is valid mailing address and normalize
     * 4. Write new CSV file to output with columns: { SourceId, Address(original), NormalizedAddress }
     */
    @Override
    public AddressValidationInput handleRequest(AddressValidationInput input, Context context) {
        LambdaLogger logger = context.getLogger();
        logger.log(String.format("Starting with input: %s", input));

        final String sourceField = input.getSourceFieldName().isEmpty() ? "AddressId" : input.getSourceFieldName();
        final String addressField = input.getAddressFieldName().isEmpty() ? "Address" : input.getAddressFieldName();
        final String inputKey = String.join("/", INPUT_FILE_NAME);
        final String outputKey = String.join("/", "address_validation_results", input.getOutputFileName());

        try {
            final List<String> loaderContent = getSelectObjectContent(INPUT_BUCKET_NAME, inputKey);
            final List<LinkedHashMap<String, String>> processedContents = processFileContent(loaderContent);

            for (LinkedHashMap<String, String> row : processedContents) {
                final String address = row.get(addressField);
                if (Objects.isNull(address) || address.isEmpty()) {
                    row.put(NORMALIZED_LABEL_FIELD, "No value for address");
                    continue;
                }
                final SearchPlaceIndexForTextResult resp = locationClient.searchPlaceIndexForText(
                    new SearchPlaceIndexForTextRequest().withText(address)
                                                        .withIndexName(PLACE_INDEX_NAME)
                                                        .withFilterCategories("AddressType")
                                                        .withMaxResults(1)
                );
                final List<SearchForTextResult> results = resp.getResults();
                if (results.isEmpty() || results.get(0).getRelevance() < 0.9) {
                    row.put(NORMALIZED_LABEL_FIELD, "Invalid Address");
                    continue;
                }
                row.put(NORMALIZED_LABEL_FIELD, results.get(0).getPlace().getLabel());
            }

            final List<String> outputHeaderFields = List.of(sourceField, addressField, NORMALIZED_LABEL_FIELD);
            List<List<String>> rowsToOutput = processedContents.stream().map(row -> {
                LinkedList<String> values = new LinkedList<>();
                for (String field : outputHeaderFields) {
                    values.add(row.get(field));
                }
                return values;
            }).collect(Collectors.toList());

            writeAndUploadOutputFile(outputHeaderFields, rowsToOutput, OUTPUT_BUCKET_NAME, outputKey);
            logger.log(String.format("Results written to: s3://%s/address_validation_results/%s", OUTPUT_BUCKET_NAME, input.getOutputFileName()));

        } catch (IOException | ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        return input;
    }

    /**
     * Returns a list of serialized strings from S3 object's content
     *
     * @param bucket bucket
     * @param key    key
     * @return List
     */
    private List<String> getSelectObjectContent(String bucket, String key) {
        final SelectObjectContentHandler responseHandler = new SelectObjectContentHandler();
        s3AsyncClient.selectObjectContent(createSelectObjectContentRequest(bucket, key), responseHandler).join();
        return responseHandler.receivedEvents().stream()
                              .filter(e -> e.sdkEventType() == EventType.RECORDS)
                              .map(r -> ((RecordsEvent) r).payload().asUtf8String()).collect(Collectors.toList());
    }

    /***
     * Method to create the SelectObjectContent request
     *
     * @param bucket bucket
     * @param key key
     * @return SelectObjectContentRequest
     */
    private SelectObjectContentRequest createSelectObjectContentRequest(String bucket, String key) {
        return SelectObjectContentRequest.builder()
                                         .bucket(bucket)
                                         .key(key)
                                         .expression("SELECT * FROM s3object s")
                                         .expressionType(ExpressionType.SQL)
                                         .inputSerialization(InputSerialization.builder()
                                                                               .csv(CSVInput.builder()
                                                                                            .fileHeaderInfo(FileHeaderInfo.USE)
                                                                                            .build())
                                                                               .compressionType(CompressionType.NONE)
                                                                               .build())
                                         .outputSerialization(OutputSerialization.builder()
                                                                                 .json(JSONOutput.builder().recordDelimiter("\n").build())
                                                                                 .build())
                                         .build();
    }

    /**
     * Process and filter the given rows from file content
     *
     * Note: Why join is needed?
     * S3 SelectObjectContent can give partial data in 1st string and remaining in the 2nd string.
     * So when we receive a List of String, we first join them to get the full data and,
     * finally split by delimiter \n to get all the rows separated.
     *
     * @param inputRows inputRows
     * @return convertedResult
     * @throws JsonProcessingException JsonProcessingException
     */
    private List<LinkedHashMap<String, String>> processFileContent(List<String> inputRows) throws JsonProcessingException {
        final String inputRowsJoined = String.join("", inputRows);
        final List<LinkedHashMap<String, String>> rows = new ArrayList<>();
        final List<String> inputRowsSplit = Arrays.asList(inputRowsJoined.split("\n"));
        for (String row : inputRowsSplit) {
            rows.add(convertToMap(row));
        }
        return rows;
    }

    /**
     * Deserialize of a row
     *
     * @param row row
     * @return LinkedHashMap
     */
    private LinkedHashMap<String, String> convertToMap(String row) throws JsonProcessingException {
        return objectMapper.readValue(row, new TypeReference<LinkedHashMap<String,String>>(){});
    }

    /**
     * Handles writing the result to respective S3 bucket using the given key
     *
     * @param header        header
     * @param rowsToOutput  rowsToOutput
     * @param bucket        bucket
     * @param key           key
     * @throws IOException IOException
     * @throws ExecutionException ExecutionException
     * @throws InterruptedException InterruptedException
     */
    private void writeAndUploadOutputFile(List<String> header, List<List<String>> rowsToOutput, String bucket, String key)
        throws IOException, ExecutionException, InterruptedException {
        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try (final CSVWriter writer = new CSVWriter(new OutputStreamWriter(stream, StandardCharsets.UTF_8))) {
            // Add the header first
            final String[] newHeader = header.toArray(String[]::new);
            writer.writeNext(newHeader);
            for (List<String> row : rowsToOutput) {
                final String[] newRow = new String[row.size()];
                writer.writeNext(row.toArray(newRow));
            }
        }
        s3AsyncClient.putObject(PutObjectRequest.builder()
                                                .bucket(bucket)
                                                .key(key)
                                                .build(), AsyncRequestBody.fromBytes(stream.toByteArray())).get();
    }
}
