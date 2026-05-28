/*
MenuCrawler

Përdoret për:
- zbulimin automatik të menuve dhe linkeve të sistemit
- navigimin në faqet e brendshme
- mbledhjen e URL-ve për skanim

Mbledh automatikisht linket e brendshme të sistemit nga elementët a[href].
Përdor domain-in aktual, shmang linke si logout, login, mailto, tel,
javascript dhe heq fragmentet #.
*/


package org.example.crawler;

import org.openqa.selenium.*;
import java.net.URI;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class MenuCrawler {

    private final WebDriver driver;

    public MenuCrawler(WebDriver driver) {
        this.driver = driver;
    }

    public Set<String> collectInternalLinks() {

        Set<String> links = new LinkedHashSet<>();

        waitForPageLoad();

        expandAllMenus();

        waitForPageLoad();

        String currentUrl = driver.getCurrentUrl();
        String baseDomain = extractBaseDomain(currentUrl);

        List<WebElement> elements =
                driver.findElements(By.cssSelector("a[href]"));

        for (WebElement element : elements) {

            try {

                String href = element.getAttribute("href");

                if (href == null || href.trim().isEmpty()) {
                    continue;
                }

                href = href.trim();

                if (shouldIgnoreLink(href)) {
                    continue;
                }

                String linkDomain = extractBaseDomain(href);

                if (baseDomain.equalsIgnoreCase(linkDomain)) {
                    links.add(removeFragment(href));
                }

            } catch (Exception ignored) {
            }
        }

        return links;
    }

    private void expandAllMenus() {

        for (int round = 0; round < 4; round++) {

            List<WebElement> toggles =
                    driver.findElements(
                            By.xpath(
                                    "//*[" +
                                            "@aria-expanded='false' " +
                                            "or contains(@class,'collapsed') " +
                                            "or contains(@class,'accordion-button') " +
                                            "or contains(@class,'menu-link') " +
                                            "or contains(@class,'menu-title') " +
                                            "or contains(@class,'nav-link')" +
                                            "]"
                            )
                    );

            for (WebElement toggle : toggles) {

                try {

                    if (!toggle.isDisplayed() || !toggle.isEnabled()) {
                        continue;
                    }

                    String tag = safe(toggle.getTagName()).toLowerCase();
                    String href = safe(toggle.getAttribute("href"));
                    String role = safe(toggle.getAttribute("role")).toLowerCase();
                    String ariaExpanded = safe(toggle.getAttribute("aria-expanded")).toLowerCase();
                    String className = safe(toggle.getAttribute("class")).toLowerCase();

                    if ("a".equals(tag)
                            && !href.isBlank()
                            && !href.equals("#")
                            && !href.toLowerCase().contains("javascript")) {
                        continue;
                    }

                    if (!ariaExpanded.equals("false")
                            && !role.contains("button")
                            && !className.contains("collapsed")
                            && !className.contains("accordion")
                            && !className.contains("menu")
                            && !className.contains("nav")) {
                        continue;
                    }

                    ((JavascriptExecutor) driver)
                            .executeScript(
                                    "arguments[0].scrollIntoView({block:'center'});",
                                    toggle
                            );

                    Thread.sleep(200);

                    try {
                        toggle.click();
                    } catch (Exception e) {
                        ((JavascriptExecutor) driver)
                                .executeScript(
                                        "arguments[0].click();",
                                        toggle
                                );
                    }

                    Thread.sleep(300);

                } catch (Exception ignored) {
                }
            }
        }
    }

    private void waitForPageLoad() {

        try {

            JavascriptExecutor js = (JavascriptExecutor) driver;

            for (int i = 0; i < 20; i++) {

                String state =
                        String.valueOf(
                                js.executeScript("return document.readyState")
                        );

                if ("complete".equalsIgnoreCase(state)) {
                    break;
                }

                Thread.sleep(300);
            }

            Thread.sleep(600);

        } catch (Exception ignored) {
        }
    }

    private boolean shouldIgnoreLink(String href) {

        String lower = href.toLowerCase();

        return lower.contains("logout")
                || lower.contains("signout")
                || lower.contains("login")
                || lower.contains("javascript:")
                || lower.startsWith("mailto:")
                || lower.startsWith("tel:")
                || lower.contains("/#")
                || lower.endsWith("#");
    }

    private String extractBaseDomain(String url) {

        try {

            URI uri = new URI(url);

            String host = uri.getHost();

            if (host == null) {
                return "";
            }

            return host.startsWith("www.")
                    ? host.substring(4)
                    : host;

        } catch (Exception e) {
            return "";
        }
    }

    private String removeFragment(String url) {

        int index = url.indexOf("#");

        if (index == -1) {
            return url;
        }

        return url.substring(0, index);
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}