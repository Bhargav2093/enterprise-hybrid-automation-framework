package com.automation.hybrid.api.specs;

import com.automation.hybrid.config.ConfigManager;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

/**
 * Builds the base RequestSpecification shared by every API test: base URI, content type,
 * and URI logging (keeps normal test output readable while still showing what was called).
 */
public final class RequestSpecFactory {

    private RequestSpecFactory() {
    }

    public static RequestSpecification base() {
        RequestSpecBuilder builder = new RequestSpecBuilder()
                .setBaseUri(ConfigManager.get("api.base.url", "https://jsonplaceholder.typicode.com"))
                .setContentType(ContentType.JSON)
                .log(LogDetail.URI);
        return builder.build();
    }
}
