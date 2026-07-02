package com.pulsecaden.samples.micronautlambda;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amazonaws.services.lambda.runtime.ClientContext;
import com.amazonaws.services.lambda.runtime.CognitoIdentity;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import io.micronaut.function.aws.proxy.payload2.APIGatewayV2HTTPEventFunction;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("integration")
class HelloControllerTest {

    @Test
    void helloReturnsLayerMessageThroughApiGatewayEvent() {
        APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
        APIGatewayV2HTTPEvent.RequestContext requestContext = new APIGatewayV2HTTPEvent.RequestContext();
        APIGatewayV2HTTPEvent.RequestContext.Http http = new APIGatewayV2HTTPEvent.RequestContext.Http();
        http.setMethod("GET");
        http.setPath("/hello");
        requestContext.setHttp(http);
        event.setRequestContext(requestContext);
        event.setRawPath("/hello");
        event.setRouteKey("GET /hello");

        String previousPort = System.getProperty("micronaut.server.port");
        System.setProperty("micronaut.server.port", "0");
        APIGatewayV2HTTPEventFunction handler = null;
        try {
            handler = new APIGatewayV2HTTPEventFunction();
            APIGatewayV2HTTPResponse response = handler.handleRequest(event, new TestContext());

            assertEquals(200, response.getStatusCode());
            assertTrue(response.getBody().contains("\"message\":\"Hello from Micronaut Lambda layer\""));
        } finally {
            if (handler != null) {
                handler.close();
            }
            if (previousPort == null) {
                System.clearProperty("micronaut.server.port");
            } else {
                System.setProperty("micronaut.server.port", previousPort);
            }
        }
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
            return "micronaut-lambda-function";
        }

        @Override
        public String getFunctionVersion() {
            return "$LATEST";
        }

        @Override
        public String getInvokedFunctionArn() {
            return "arn:aws:lambda:us-east-1:123456789012:function:micronaut-lambda-function";
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
