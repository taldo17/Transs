package com.serverless;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.transs.Jira;
import com.transs.RekognitionImageRecognition;
import com.transs.TranssService;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Handler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

	private static final Logger LOG = Logger.getLogger(Handler.class);
	private TranssService transsService = new TranssService(new RekognitionImageRecognition(), new Jira());

	@Override
	public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
		BasicConfigurator.configure();
		List<Object> body =  input.keySet().stream().filter(key -> key.equalsIgnoreCase("body")).collect(Collectors.toList());
        String printings = input.get(body.get(0)).toString();
        System.out.println("printing the body: " + printings);
        transsService.analyzeImageAndUpdateALM(printings.getBytes());
		LOG.info("received: " + input);
		Response responseBody = new Response("Go Serverless v1.x! Your function executed successfully! Changed", input);
		return ApiGatewayResponse.builder()
				.setStatusCode(200)
				.setObjectBody(responseBody)
				.setHeaders(Collections.singletonMap("X-Powered-By", "AWS Lambda & serverless"))
				.build();
	}
}
