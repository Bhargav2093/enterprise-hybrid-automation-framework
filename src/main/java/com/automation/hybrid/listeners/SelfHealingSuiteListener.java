package com.automation.hybrid.listeners;

import com.automation.hybrid.config.ConfigManager;
import com.automation.hybrid.selfhealing.HealingReportManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ISuite;
import org.testng.ISuiteListener;

/**
 * Flushes every HealingEvent recorded during the suite to a JSON report at suite end,
 * so self-healing is a reviewable artifact (target/self-healing-report.json) rather than
 * only visible in logs.
 */
public class SelfHealingSuiteListener implements ISuiteListener {

    private static final Logger LOGGER = LogManager.getLogger(SelfHealingSuiteListener.class);

    @Override
    public void onFinish(ISuite suite) {
        String path = ConfigManager.get("selfhealing.report.path", "target/self-healing-report.json");
        HealingReportManager.writeReport(path);
        LOGGER.info("Wrote self-healing report ({} event(s)) to {}",
                HealingReportManager.events().size(), path);
    }
}
