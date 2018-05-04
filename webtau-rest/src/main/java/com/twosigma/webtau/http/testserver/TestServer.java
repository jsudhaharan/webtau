package com.twosigma.webtau.http.testserver;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.twosigma.webtau.http.HttpRequestHeader;
import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

import com.twosigma.webtau.http.HttpUrl;
import com.twosigma.webtau.http.config.HttpConfiguration;
import com.twosigma.webtau.http.config.HttpConfigurations;

public class TestServer implements HttpConfiguration {
    private int port;
    private Map<String, TestServerResponse> getResponses;
    private Map<String, TestServerResponse> postResponses;
    private Map<String, TestServerResponse> putResponses;
    private Map<String, TestServerResponse> deleteResponses;
    private Server server;

    public TestServer() {
        getResponses = new HashMap<>();
        postResponses = new HashMap<>();
        putResponses = new HashMap<>();
        deleteResponses = new HashMap<>();
    }

    public void start(int port) {
        this.port = port;
        server = new Server(port);
        server.setHandler(new RequestHandler());
        try {
            server.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        HttpConfigurations.add(this);
    }

    public void stop() {
        try {
            server.stop();
            port = 0;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        HttpConfigurations.remove(this);
    }

    public void registerGet(String relativeUrl, TestServerResponse response) {
        getResponses.put(relativeUrl, response);
    }

    public void registerPost(String relativeUrl, TestServerResponse response) {
        postResponses.put(relativeUrl, response);
    }

    public void registerPut(String relativeUrl, TestServerResponse response) {
        putResponses.put(relativeUrl, response);
    }

    public void registerDelete(String relativeUrl, TestServerResponse response) {
        deleteResponses.put(relativeUrl, response);
    }

    @Override
    public String fullUrl(String url) {
        if (HttpUrl.isFull(url)) {
            return url;
        }

        return HttpUrl.concat("http://localhost:" + port, url);
    }

    @Override
    public HttpRequestHeader fullHeader(HttpRequestHeader given) {
        return given;
    }

    private class RequestHandler extends AbstractHandler {
        @Override
        public void handle(String url, Request baseRequest, HttpServletRequest request,
                           HttpServletResponse response) throws IOException, ServletException {

            Map<String, TestServerResponse> responses = findResponses(request);

            TestServerRequest serverRequest = new TestServerRequest();
            serverRequest.setRequestBody(IOUtils.toString(baseRequest.getReader()));
            serverRequest.setRequestType(baseRequest.getContentType());

            TestServerResponse testServerResponse = responses.get(baseRequest.getOriginalURI());
            if (testServerResponse == null) {
                response.setStatus(404);
            } else {
                String responseBody = testServerResponse.responseBody(serverRequest);
                response.setStatus(200);
                response.setContentType(testServerResponse.responseType(serverRequest));
                response.getWriter().println(responseBody != null ? responseBody : "");
            }

            baseRequest.setHandled(true);
        }

        private Map<String, TestServerResponse> findResponses(HttpServletRequest request) {
            switch (request.getMethod()) {
                case "GET":
                    return getResponses;
                case "POST":
                    return postResponses;
                case "PUT":
                    return putResponses;
                case "DELETE":
                    return deleteResponses;
                default:
                    return Collections.emptyMap();

            }
        }
    }
}