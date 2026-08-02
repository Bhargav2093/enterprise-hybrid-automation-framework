package com.automation.hybrid.selfhealing;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Loads the centralized multi-locator repository (src/test/resources/locators.json) once,
 * mapping page -> elementKey -> ordered candidate locators.
 */
public final class LocatorRepository {

    private static final String RESOURCE_NAME = "locators.json";
    private static volatile Map<String, Map<String, List<LocatorCandidate>>> DATA;

    private LocatorRepository() {
    }

    private static Map<String, Map<String, List<LocatorCandidate>>> data() {
        if (DATA == null) {
            synchronized (LocatorRepository.class) {
                if (DATA == null) {
                    DATA = load();
                }
            }
        }
        return DATA;
    }

    private static Map<String, Map<String, List<LocatorCandidate>>> load() {
        try (InputStream in = LocatorRepository.class.getClassLoader().getResourceAsStream(RESOURCE_NAME)) {
            if (in == null) {
                throw new IllegalStateException("Missing " + RESOURCE_NAME + " on classpath");
            }
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(in, new TypeReference<>() {
            });
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load " + RESOURCE_NAME, e);
        }
    }

    public static List<LocatorCandidate> get(String page, String key) {
        Map<String, List<LocatorCandidate>> pageLocators = data().get(page);
        if (pageLocators == null) {
            throw new IllegalArgumentException("No locators defined for page: " + page);
        }
        List<LocatorCandidate> candidates = pageLocators.get(key);
        if (candidates == null || candidates.isEmpty()) {
            throw new IllegalArgumentException("No locators defined for " + page + "." + key);
        }
        return candidates;
    }
}
