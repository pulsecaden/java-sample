package com.pulsecaden.samples.corelambda;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amazonaws.services.lambda.runtime.ClientContext;
import com.amazonaws.services.lambda.runtime.CognitoIdentity;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import org.junit.jupiter.api.Test;

class HelloHandlerTest {

    private final HelloHandler handler = new HelloHandler();

    @Test
    void helloReturnsLayerMessage() {
        APIGatewayV2HTTPEvent event = helloEvent("GET", "/hello");

        APIGatewayV2HTTPResponse response = handler.handleRequest(event, new TestContext());

        assertEquals(200, response.getStatusCode());
        assertEquals("application/json", response.getHeaders().get("Content-Type"));
        assertTrue(response.getBody().contains("\"message\":\"Hello from Core Java Lambda layer\""));
    }

    @Test
    void unknownRouteReturns404() {
        APIGatewayV2HTTPEvent event = helloEvent("GET", "/unknown");

        APIGatewayV2HTTPResponse response = handler.handleRequest(event, new TestContext());

        assertEquals(404, response.getStatusCode());
        assertTrue(response.getBody().contains("\"message\":\"Not Found\""));
    }

    private static APIGatewayV2HTTPEvent helloEvent(String method, String path) {
        APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
        APIGatewayV2HTTPEvent.RequestContext requestContext = new APIGatewayV2HTTPEvent.RequestContext();
        APIGatewayV2HTTPEvent.RequestContext.Http http = new APIGatewayV2HTTPEvent.RequestContext.Http();
        http.setMethod(method);
        http.setPath(path);
        requestContext.setHttp(http);
        event.setRequestContext(requestContext);
        event.setRawPath(path);
        event.setRouteKey(method + " " + path);
        return event;
    }

    private static final class TestContext implements Context {
        @Override
        public String getAwsRequestId() {
            return "test-request";
        }

        @Override
        public String getLogGroupName() {
            return "test-log-group";
        }

        @Override
        public String getLogStreamName() {
            return "test-log-stream";
        }

        @Override
        public String getFunctionName() {
            return "core-java-lambda-function";
        }

        @Override
        public String getFunctionVersion() {
            return "$LATEST";
        }

        @Override
        public String getInvokedFunctionArn() {
            return "arn:aws:lambda:us-east-1:123456789012:function:core-java-lambda-function";
        }

        @Override
        public CognitoIdentity getIdentity() {
            return null;
        }

        @Override
        public ClientContext getClientContext() {
            return null;
        }

        @Override
        public int getRemainingTimeInMillis() {
            return 30000;
        }

        @Override
        public int getMemoryLimitInMB() {
            return 512;
        }

        @Override
        public LambdaLogger getLogger() {
            return new LambdaLogger() {
                @Override
                public void log(String message) {
                }

                @Override
                public void log(byte[] message) {
                }
            };
        }
    }
}
