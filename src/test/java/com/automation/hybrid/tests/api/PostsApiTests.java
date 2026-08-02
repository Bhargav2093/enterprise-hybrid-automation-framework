package com.automation.hybrid.tests.api;

import com.automation.hybrid.api.client.ApiClient;
import com.automation.hybrid.api.models.CreatePostRequest;
import com.automation.hybrid.api.models.Post;
import com.automation.hybrid.api.models.UpdatePostRequest;
import com.automation.hybrid.reporting.ReportLogger;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

@Epic("Enterprise Hybrid Automation Framework")
@Feature("Posts API (jsonplaceholder.typicode.com)")
public class PostsApiTests {

    private final ApiClient api = new ApiClient();

    @Test(groups = {"api", "smoke"})
    @Description("GET an existing post returns 200 and a payload matching the post JSON schema")
    public void getSinglePostReturnsValidSchema() {
        Response response = api.get("/posts/1");

        response.then()
                .statusCode(200)
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("schemas/post-schema.json"));

        ReportLogger.step("Then the response matches the post JSON schema and id=1");
        Post post = response.as(Post.class);
        Assert.assertEquals(post.id(), 1);
        Assert.assertNotNull(post.title());
    }

    @Test(groups = {"api", "regression"})
    @Description("GET a non-existent post returns 404")
    public void getSinglePostNotFoundReturns404() {
        ReportLogger.step("Then the response status is 404");
        api.get("/posts/99999").then().statusCode(404);
    }

    @Test(groups = {"api", "regression"})
    @Description("GET posts filtered by userId returns only that user's posts")
    public void getPostsByUserIdReturnsFilteredResults() {
        Response response = api.get("/posts?userId=1");
        response.then().statusCode(200);

        ReportLogger.step("Then every returned post belongs to userId=1");
        List<Post> posts = List.of(response.as(Post[].class));
        Assert.assertFalse(posts.isEmpty(), "Expected at least one post for userId=1");
        Assert.assertTrue(posts.stream().allMatch(p -> p.userId() == 1),
                "Expected every returned post to belong to userId=1");
    }

    @Test(groups = {"api", "smoke"})
    @Description("POST creates a new post and echoes back the submitted title/body with a generated id")
    public void createPostReturns201WithGeneratedId() {
        CreatePostRequest request = new CreatePostRequest(
                "Automated framework API test", "Created by the Enterprise Hybrid Automation Framework", 1);

        Response response = api.post("/posts", request);
        response.then().statusCode(201);

        ReportLogger.step("Then the response echoes back the submitted title/body with a generated id");
        Post created = response.as(Post.class);
        Assert.assertEquals(created.title(), request.title());
        Assert.assertEquals(created.body(), request.body());
        Assert.assertTrue(created.id() > 0, "Expected a generated id");
    }

    @Test(groups = {"api", "regression"})
    @Description("PUT updates an existing post and returns the new title/body")
    public void updatePostReturns200WithUpdatedFields() {
        UpdatePostRequest request = new UpdatePostRequest(1, "Updated title", "Updated body", 1);

        Response response = api.put("/posts/1", request);
        response.then().statusCode(200);

        ReportLogger.step("Then the response reflects the updated title/body");
        Post updated = response.as(Post.class);
        Assert.assertEquals(updated.title(), "Updated title");
        Assert.assertEquals(updated.body(), "Updated body");
    }

    @Test(groups = {"api", "regression"})
    @Description("DELETE removes a post and returns 200")
    public void deletePostReturns200() {
        ReportLogger.step("Then the response status is 200");
        api.delete("/posts/1").then().statusCode(200);
    }
}
