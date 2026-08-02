package com.automation.hybrid.selfhealing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Accumulates HealingEvents across the whole test run (thread-safe) and writes them to a JSON
 * report at suite end (see listeners.SelfHealingSuiteListener), so healed locators are a
 * demoable artifact rather than just log lines.
 */
public final class HealingReportManager {

    private static final List<HealingEvent> EVENTS = new CopyOnWriteArrayList<>();

    private HealingReportManager() {
    }

    public static void record(HealingEvent event) {
        EVENTS.add(event);
    }

    public static List<HealingEvent> events() {
        return Collections.unmodifiableList(EVENTS);
    }

    public static void writeReport(String path) {
        try {
            File file = new File(path);
            File parent = file.getParentFile();
            if (parent != null) {
                parent.mkdirs();
            }
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            mapper.findAndRegisterModules();
            mapper.writeValue(file, EVENTS);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write self-healing report to " + path, e);
        }
    }
}
