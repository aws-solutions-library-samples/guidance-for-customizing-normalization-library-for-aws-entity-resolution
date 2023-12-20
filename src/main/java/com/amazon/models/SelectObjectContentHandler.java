package com.amazon.models;

import java.util.ArrayList;
import java.util.List;
import software.amazon.awssdk.core.async.SdkPublisher;
import software.amazon.awssdk.services.s3.model.SelectObjectContentEventStream;
import software.amazon.awssdk.services.s3.model.SelectObjectContentResponse;
import software.amazon.awssdk.services.s3.model.SelectObjectContentResponseHandler;

/***
 * SelectObjectContent is not supported in the regular client, we need to use S3 async client
 *
 * SelectObjectContent response includes EventStream types, which require HTTP/2.
 * HTTP/2 is only supported by the async Netty HTTP Client currently.
 *
 * Related support links:
 *  a) <a href="https://github.com/aws/aws-sdk-java-v2/pull/2943">...</a>
 *  b) <a href="https://github.com/aws/aws-sdk-java-v2/issues/4300">...</a>
 */
public class SelectObjectContentHandler implements SelectObjectContentResponseHandler {
    private List<SelectObjectContentEventStream> receivedEventList = new ArrayList<>();

    @Override
    public void responseReceived(SelectObjectContentResponse response) {}

    @Override
    public void onEventStream(SdkPublisher<SelectObjectContentEventStream> publisher) {
        publisher.subscribe(receivedEventList::add);
    }

    @Override
    public void exceptionOccurred(Throwable throwable) {/* no-op */}

    @Override
    public void complete() {/* no-op */}

    public List<SelectObjectContentEventStream> receivedEvents() {
        return List.copyOf(receivedEventList);
    }

    public void setReceivedEvents(List<SelectObjectContentEventStream> receivedEvents) {
        this.receivedEventList = List.copyOf(receivedEvents);
    }
}

