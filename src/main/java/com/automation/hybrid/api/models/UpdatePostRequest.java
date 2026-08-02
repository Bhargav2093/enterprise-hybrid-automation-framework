package com.automation.hybrid.api.models;

public record UpdatePostRequest(int id, String title, String body, int userId) {
}
