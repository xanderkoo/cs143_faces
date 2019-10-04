package com.amazonaws.samples;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;

public class ClientFactory {

	public static AmazonRekognition createClient() {
		ClientConfiguration clientConfig = new ClientConfiguration();
		clientConfig.setConnectionTimeout(30000);
		clientConfig.setRequestTimeout(60000);
		clientConfig.setProtocol(Protocol.HTTPS);

		AWSCredentialsProvider credentialsProvider = new ProfileCredentialsProvider();

		return AmazonRekognitionClientBuilder.standard().withClientConfiguration(clientConfig)
				.withCredentials(credentialsProvider).withRegion("eu-west-1").build();
	}
}
