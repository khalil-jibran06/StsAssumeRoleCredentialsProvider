package com.sample.roleassumption;

import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient;
import software.amazon.awssdk.services.kinesis.model.PutRecordRequest;
import software.amazon.awssdk.services.kinesis.model.PutRecordResponse;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.auth.StsAssumeRoleCredentialsProvider;
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest;
import software.amazon.kinesis.common.KinesisClientUtil;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Shows how to assume IAM ROLE in KCL 2.x and submit records to kinesis stream.
 * Also high lights how to bye pass existing issue in AWS SDK ( through some transient dependency ).
 */
@SuppressWarnings("ALL")
public class RoleAssumption {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        try {
            AssumeRoleRequest assumeRoleRequest = AssumeRoleRequest.builder().roleArn("SOME_ROLE_ARN").
                    roleSessionName("SOME_SESSION_NAME").build();

            // NOTE :: In some sequence mostly related to dependent jar behaviour, if explicit httpClient is not
            // set, then below error occurs ::
            /* Error: Unable to load an HTTP implementation from any provider in the chain.
                      You must declare a dependency on an appropriate HTTP implementation or pass in
                      an SdkHttpClient explicitly to the client builder.
                      1. With attached pom.xml it works just file, however when multiple different AWS SDK packages
                      are included or through other transient dependencies, this erorr occurs.
                      2. By explcit setting httpClient, it can be work around until AWS fixes the issue.
             */
            SdkHttpClient httpClient = ApacheHttpClient.builder().build();

            StsClient stsClient = StsClient.builder().region(Region.US_EAST_1).httpClient(httpClient).build();

            AwsCredentialsProvider credentialsProvider = StsAssumeRoleCredentialsProvider
                    .builder()
                    .stsClient(stsClient).refreshRequest(assumeRoleRequest)
                    .asyncCredentialUpdateEnabled(true)
                    .build();

            KinesisAsyncClient client = KinesisClientUtil.createKinesisAsyncClient(KinesisAsyncClient.builder().region(Region.US_EAST_1).credentialsProvider(credentialsProvider));

            PutRecordRequest putRecordRequest = PutRecordRequest.builder().build();
            putRecordRequest.toBuilder().streamName("SOME_KINESIS_STREAM").build();
            putRecordRequest.toBuilder().data(SdkBytes.fromByteBuffer(ByteBuffer.wrap("s".getBytes())));

            PutRecordRequest request = PutRecordRequest.builder()
                    .partitionKey("pop")
                    .streamName("SOME_KINESIS_STREAM")
                    .data(SdkBytes.fromByteBuffer(ByteBuffer.wrap("pop".getBytes())))
                    .build();
            PutRecordResponse putRecordResponse = client.putRecord(request).get(60, TimeUnit.SECONDS);
        } catch (Exception ex) {
            System.out.println("Error occured during role assumption or submission to kinesis stream :: " + ex.getMessage());
        }
    }
}
