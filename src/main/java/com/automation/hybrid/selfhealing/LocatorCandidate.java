package com.automation.hybrid.selfhealing;

import org.openqa.selenium.By;

/**
 * One candidate strategy for locating an element, e.g. {type: "css", value: "input[data-qa='signup-name']"}.
 */
public record LocatorCandidate(String type, String value) {

    public By toBy() {
        return switch (type.toLowerCase()) {
            case "id" -> By.id(value);
            case "name" -> By.name(value);
            case "css" -> By.cssSelector(value);
            case "xpath" -> By.xpath(value);
            case "linktext" -> By.linkText(value);
            case "partiallinktext" -> By.partialLinkText(value);
            case "classname" -> By.className(value);
            case "tagname" -> By.tagName(value);
            default -> throw new IllegalArgumentException("Unsupported locator type: " + type);
        };
    }

    @Override
    public String toString() {
        return type + "=" + value;
    }
}
