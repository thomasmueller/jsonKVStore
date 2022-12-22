package org.jsonKVStore.s3;

import org.junit.Test;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

import io.findify.s3mock.S3Mock;

public class S3Test {
    
    public static int PORT = 8001;

    @Test
    public void test() {
        S3Mock api = new S3Mock.Builder().withPort(PORT).withInMemoryBackend().build();
        api.start();
        try {
            EndpointConfiguration endpoint = new EndpointConfiguration(
                    "http://localhost:" + PORT, "us-west-2");
            AmazonS3 client = AmazonS3ClientBuilder
              .standard()
              .withPathStyleAccessEnabled(true)  
              .withEndpointConfiguration(endpoint)
              .withCredentials(new AWSStaticCredentialsProvider(
                      new AnonymousAWSCredentials()))     
              .build();
            client.createBucket("testbucket");
            client.putObject("testbucket", "file/name", "contents");
        } finally {
            api.shutdown();            
            
        }
        
    }
}
