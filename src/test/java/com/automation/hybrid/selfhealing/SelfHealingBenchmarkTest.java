package com.automation.hybrid.selfhealing;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Proves self-healing works against every element actually defined in the real
 * {@code locators.json} - not a live browser run (no WebDriver/network available in every CI
 * environment this might run in), but the real production {@link SelfHealingLocator#find}
 * driven through a mocked {@link WebDriver} whose primary locator always "breaks". This is what
 * backs the README's self-healing coverage numbers: real code, real config, real assertions,
 * just without a real browser attached.
 *
 * <p>Also writes a benchmark summary and the real {@link HealingReportManager} JSON report to
 * {@code samples/selfhealing-benchmark/}, so the numbers are a committed, reproducible artifact
 * rather than a one-off console print.
 */
public class SelfHealingBenchmarkTest {

    private static final String OUTPUT_DIR = "samples/selfhealing-benchmark";

    @Test
    public void everyElementWithAFallbackHealsWhenItsPrimaryLocatorBreaks() throws IOException {
        System.setProperty("explicit.wait.seconds", "1"); // keep the no-fallback control cases fast

        Map<String, Map<String, List<LocatorCandidate>>> locators = loadRawLocators();

        List<ElementRecord> healable = new ArrayList<>();
        List<ElementRecord> singleCandidate = new ArrayList<>();
        int totalCandidates = 0;

        for (Map.Entry<String, Map<String, List<LocatorCandidate>>> page : locators.entrySet()) {
            for (Map.Entry<String, List<LocatorCandidate>> element : page.getValue().entrySet()) {
                ElementRecord record = new ElementRecord(page.getKey(), element.getKey(), element.getValue());
                totalCandidates += record.candidates.size();
                if (record.candidates.size() >= 2) {
                    healable.add(record);
                } else {
                    singleCandidate.add(record);
                }
            }
        }

        int totalElements = healable.size() + singleCandidate.size();
        int eventsBefore = HealingReportManager.events().size();

        for (ElementRecord record : healable) {
            assertHealsViaFirstFallback(record);
        }
        for (ElementRecord record : singleCandidate) {
            assertGenuinelyUnhealable(record);
        }

        int healingEventsRecorded = HealingReportManager.events().size() - eventsBefore;
        Assert.assertEquals(healingEventsRecorded, healable.size(),
                "expected exactly one HealingEvent per healable element");

        writeArtifacts(locators, healable, singleCandidate, totalElements, totalCandidates);
    }

    private void assertHealsViaFirstFallback(ElementRecord record) {
        LocatorCandidate brokenPrimary = record.candidates.get(0);
        LocatorCandidate firstFallback = record.candidates.get(1);
        WebElement fakeElement = mock(WebElement.class);
        WebDriver driver = mock(WebDriver.class);

        when(driver.findElements(any(By.class))).thenAnswer(invocation -> {
            By requested = invocation.getArgument(0);
            if (requested.toString().equals(firstFallback.toBy().toString())) {
                return List.of(fakeElement);
            }
            return List.of();
        });

        WebElement resolved = SelfHealingLocator.find(driver, record.page, record.elementKey);

        Assert.assertSame(resolved, fakeElement,
                record.page + "." + record.elementKey + " should resolve via its first fallback "
                        + "once the primary (" + brokenPrimary + ") is broken");
    }

    private void assertGenuinelyUnhealable(ElementRecord record) {
        WebDriver driver = mock(WebDriver.class);
        when(driver.findElements(any(By.class))).thenReturn(List.of());

        Assert.assertThrows(NoSuchElementException.class,
                () -> SelfHealingLocator.find(driver, record.page, record.elementKey));
    }

    private Map<String, Map<String, List<LocatorCandidate>>> loadRawLocators() throws IOException {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("locators.json")) {
            Assert.assertNotNull(in, "locators.json must be on the test classpath");
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(in, new TypeReference<>() {
            });
        }
    }

    private void writeArtifacts(
            Map<String, Map<String, List<LocatorCandidate>>> locators,
            List<ElementRecord> healable,
            List<ElementRecord> singleCandidate,
            int totalElements,
            int totalCandidates
    ) throws IOException {
        File outputDir = new File(OUTPUT_DIR);
        outputDir.mkdirs();

        HealingReportManager.writeReport(OUTPUT_DIR + "/self-healing-report.json");

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("pagesDefined", locators.size());
        summary.put("elementsDefined", totalElements);
        summary.put("elementsWithFallback", healable.size());
        summary.put("elementsWithFallbackPercent",
                Math.round(healable.size() * 1000.0 / totalElements) / 10.0);
        summary.put("singleCandidateElements", singleCandidate.size());
        summary.put("averageCandidatesPerElement",
                Math.round(totalCandidates * 100.0 / totalElements) / 100.0);
        summary.put("healingEventsProvenInThisRun", healable.size());
        summary.put("singleCandidateElementKeys",
                singleCandidate.stream().map(r -> r.page + "." + r.elementKey).sorted().toList());

        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.writeValue(new File(OUTPUT_DIR + "/benchmark-summary.json"), summary);
    }

    private record ElementRecord(String page, String elementKey, List<LocatorCandidate> candidates) {
    }
}
