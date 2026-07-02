package com.pulsecaden.samples.corelambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.pulsecaden.samples.corelambda.layer.GreetingProvider;
import java.util.Map;

public class HelloHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    private final GreetingProvider greetingProvider = new GreetingProvider();

    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {
        if (isHelloGet(event)) {
            return jsonResponse(200, "{\"message\":\"" + greetingProvider.message() + "\"}");
        }
        return jsonResponse(404, "{\"message\":\"Not Found\"}");
    }

    private static boolean isHelloGet(APIGatewayV2HTTPEvent event) {
        if (event == null) {
            return false;
        }

        String path = event.getRawPath();
        String method = null;
        if (event.getRequestContext() != null && event.getRequestContext().getHttp() != null) {
            method = event.getRequestContext().getHttp().getMethod();
        }

        return "/hello".equals(path) && "GET".equalsIgnoreCase(method);
    }

    private static APIGatewayV2HTTPResponse jsonResponse(int statusCode, String body) {
        APIGatewayV2HTTPResponse response = new APIGatewayV2HTTPResponse();
        response.setStatusCode(statusCode);
        response.setHeaders(Map.of("Content-Type", "application/json"));
        response.setBody(body);
        return response;
    }
}
