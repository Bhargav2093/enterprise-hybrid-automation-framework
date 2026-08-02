package com.automation.hybrid.api.client;

import com.automation.hybrid.api.specs.RequestSpecFactory;
import com.automation.hybrid.reporting.ReportLogger;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;

/**
 * Thin, reusable wrapper around REST Assured's given()/when() so test classes don't repeat
 * request-building boilerplate. Every call is logged as a report step (request + resulting
 * status code) so API test runs get the same step-by-step trail as UI runs.
 */
public class ApiClient {

    private RequestSpecification request() {
        return given().spec(RequestSpecFactory.base());
    }

    public Response get(String path) {
        return logged("GET", path, request().when().get(path));
    }

    public Response post(String path, Object body) {
        return logged("POST", path, request().body(body).when().post(path));
    }

    public Response put(String path, Object body) {
        return logged("PUT", path, request().body(body).when().put(path));
    }

    public Response delete(String path) {
        return logged("DELETE", path, request().when().delete(path));
    }

    private Response logged(String method, String path, Response response) {
        ReportLogger.step(method + " " + path + " -> " + response.getStatusCode());
        return response;
    }
}
