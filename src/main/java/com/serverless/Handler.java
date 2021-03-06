package com.serverless;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.transs.RekognitionImageRecognition;
import com.transs.TranssService;
import com.transs.Trello;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import sun.misc.BASE64Decoder;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

public class Handler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

	private static final Logger LOG = Logger.getLogger(Handler.class);
	private TranssService transsService = new TranssService(new RekognitionImageRecognition(), new Trello());

	@Override
	public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
		BasicConfigurator.configure();
		BASE64Decoder decoder = new BASE64Decoder();

		Object bodyObject = input.get("body");
		if(bodyObject == null){
			LOG.error("The body is null!!!");
			return null;
		}
		JSONObject jsonObject = new JSONObject(bodyObject.toString());
		Object image = jsonObject.get("image");
		LOG.info("Got the Image");
        Object token = jsonObject.get("token");
        LOG.info("The token is " + token);
        Object boardId = jsonObject.get("boardId");
        LOG.info("The board id is " + boardId);
		byte[] imageByte = null;
		try
		{
			LOG.info("Taldo: decoding");
			imageByte = decoder.decodeBuffer(image.toString());
			LOG.info("Taldo: decoded");
		}
		catch (IOException e)
		{
			LOG.error("Taldo: got exception while decoding:  ", e);
		}
        transsService.analyzeImageAndUpdateALM(imageByte, boardId.toString(), token.toString());
		Response responseBody = new Response("Go Serverless v1.x! Your function executed successfully! Changed", input);
		return ApiGatewayResponse.builder()
				.setStatusCode(200)
				.setObjectBody(responseBody)
				.setHeaders(Collections.singletonMap("X-Powered-By", "AWS Lambda & serverless"))
				.build();
	}
}
