package com.healthmate.ai.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.awt.Desktop;
import java.net.URI;

/**
 * Automatically opens the default web browser to the app's home page once startup completes.
 * This only fires in environments with a graphical desktop (java.awt.Desktop support) — on
 * headless servers (Railway, Docker, CI, etc.) it silently does nothing, so it's safe to leave
 * enabled everywhere. Can be disabled entirely via app.browser.auto-open=false.
 */
@Component
public class BrowserLauncher {

    private static final Logger log = LoggerFactory.getLogger(BrowserLauncher.class);

    @Value("${server.port:8080}")
    private String port;

    @Value("${app.browser.auto-open:true}")
    private boolean autoOpenEnabled;

    @EventListener(ApplicationReadyEvent.class)
    public void openBrowserOnStartup() {
        if (!autoOpenEnabled) {
            return;
        }
        String url = "http://localhost:" + port;

        if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            log.info("No desktop browser support detected (likely a server environment) — skipping auto-open. "
                    + "Visit {} manually.", url);
            return;
        }

        try {
            Desktop.getDesktop().browse(new URI(url));
            log.info("Opened {} in your default browser.", url);
        } catch (Exception e) {
            log.warn("Could not auto-open browser, please visit {} manually. Reason: {}", url, e.getMessage());
        }
    }
}
