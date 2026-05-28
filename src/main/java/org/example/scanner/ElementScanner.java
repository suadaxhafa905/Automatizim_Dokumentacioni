

/*
Analizon DOM-in e faqes dhe:

zbulon elementët UI
lexon inpute
butona
tabela
labels
select
headings

Është:

UI element analyzer

 */

package org.example.scanner;

import org.example.model.ElementInfo;
import org.example.utils.ElementFilter;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

public class ElementScanner {

    private final WebDriver driver;

    public ElementScanner(WebDriver driver) {
        this.driver = driver;
    }

    public List<ElementInfo> scanElements() {

        List<ElementInfo> result = new ArrayList<>();

        List<WebElement> elements =
                driver.findElements(
                        By.xpath(
                                "//*[" +
                                        "self::input or " +
                                        "self::button or " +
                                        "self::select or " +
                                        "self::textarea or " +
                                        "self::table or " +
                                        "self::label or " +
                                        "self::a" +
                                        "]" +
                                        "[not(ancestor::aside)]" +
                                        "[not(ancestor::nav)]" +
                                        "[not(ancestor::*[contains(@class,'menu')])]" +
                                        "[not(ancestor::*[contains(@class,'sidebar')])]"
                        )
                );

        scanElementList(elements, result);

        return result;
    }

    public List<ElementInfo> scanElements(WebElement container) {

        List<ElementInfo> result = new ArrayList<>();

        if (container == null) {
            return result;
        }

        List<WebElement> elements =
                container.findElements(
                        By.xpath(
                                ".//*[" +
                                        "self::input or " +
                                        "self::button or " +
                                        "self::select or " +
                                        "self::textarea or " +
                                        "self::table or " +
                                        "self::label or " +
                                        "self::a" +
                                        "]"
                        )
                );

        scanElementList(elements, result);

        return result;
    }

    private void scanElementList(
            List<WebElement> elements,
            List<ElementInfo> result
    ) {

        for (WebElement element : elements) {

            try {

                if (!element.isDisplayed()) {
                    continue;
                }

                String tagName = safeTag(element);
                String text = clean(safeText(element));
                String type = safeAttribute(element, "type");
                String name = safeAttribute(element, "name");
                String placeholder = safeAttribute(element, "placeholder");
                String id = safeAttribute(element, "id");
                String title = safeAttribute(element, "title");
                String ariaLabel = safeAttribute(element, "aria-label");
                String className = safeAttribute(element, "class");

                if ("table".equalsIgnoreCase(tagName)) {
                    text = extractTableSummary(element);
                }

                if (text.isEmpty()) {
                    text = getTextFromAttributes(
                            ariaLabel,
                            title,
                            className
                    );
                }

                String cssSelector = buildCssSelector(element);
                String label = extractLabel(element);
                boolean required = isRequired(element);
                List<String> tableColumns = extractTableColumns(element);

                String actionType = detectActionType(text, className);
                String section = detectSection(element);
                String helpText = extractHelpText(element);
                String containerType = detectContainerType(element);

                ElementInfo elementInfo =
                        new ElementInfo(
                                tagName,
                                text,
                                type,
                                name,
                                placeholder,
                                id,
                                cssSelector,
                                label,
                                required,
                                tableColumns,
                                actionType,
                                section,
                                helpText,
                                containerType
                        );

                if (!ElementFilter.shouldInclude(elementInfo)) {
                    continue;
                }

                if (isDuplicate(result, elementInfo)) {
                    continue;
                }

                result.add(elementInfo);

            } catch (Exception ignored) {
            }
        }
    }

    private String extractLabel(WebElement element) {

        try {

            String id = safeAttribute(element, "id");

            if (!id.isEmpty()) {

                List<WebElement> labels =
                        driver.findElements(By.xpath("//label[@for='" + id + "']"));

                for (WebElement label : labels) {

                    String text = clean(label.getText());

                    if (isValidLabel(text)) {
                        return text;
                    }
                }
            }

        } catch (Exception ignored) {
        }

        try {

            List<WebElement> nearbyLabels =
                    element.findElements(By.xpath("./preceding::label[1]"));

            for (WebElement label : nearbyLabels) {

                String text = clean(label.getText());

                if (isValidLabel(text)) {
                    return text;
                }
            }

        } catch (Exception ignored) {
        }

        return safeAttribute(element, "placeholder");
    }

    private boolean isRequired(WebElement element) {

        String required = safeAttribute(element, "required");
        String ariaRequired = safeAttribute(element, "aria-required");
        String className = safeAttribute(element, "class");
        String label = extractLabel(element);

        return !required.isEmpty()
                || ariaRequired.equalsIgnoreCase("true")
                || className.toLowerCase().contains("required")
                || label.contains("*");
    }

    private List<String> extractTableColumns(WebElement table) {

        List<String> columns = new ArrayList<>();

        try {

            if (!"table".equalsIgnoreCase(table.getTagName())) {
                return columns;
            }

            List<WebElement> headers = table.findElements(By.tagName("th"));

            for (WebElement header : headers) {

                String text = clean(header.getText());

                if (text.isEmpty()) {
                    continue;
                }

                if (text.length() > 40) {
                    continue;
                }

                if (text.matches(".*\\d+.*")) {
                    continue;
                }

                columns.add(text);
            }

        } catch (Exception ignored) {
        }

        return columns;
    }

    private String detectActionType(String text, String className) {

        String value = (safe(text) + " " + safe(className)).toLowerCase();

        if (value.contains("kërko") || value.contains("kerko") || value.contains("search")) {
            return "SEARCH";
        }

        if (value.contains("filtro") || value.contains("filter")) {
            return "FILTER";
        }

        if (value.contains("ruaj") || value.contains("save")) {
            return "SAVE";
        }

        if (value.contains("fshi") || value.contains("delete") || value.contains("trash") || value.contains("remove")) {
            return "DELETE";
        }

        if (value.contains("modifiko") || value.contains("ndrysho") || value.contains("edit") || value.contains("pencil")) {
            return "EDIT";
        }

        if (value.contains("import") || value.contains("upload")) {
            return "IMPORT";
        }

        if (value.contains("export") || value.contains("shkarko") || value.contains("download") || value.contains("pdf") || value.contains("excel")) {
            return "EXPORT";
        }

        if (value.contains("verifiko") || value.contains("verify")) {
            return "VERIFY";
        }

        if (value.contains("dërgo") || value.contains("dergo") || value.contains("send")) {
            return "SEND";
        }

        if (value.contains("shto") || value.contains("add") || value.contains("plus")) {
            return "ADD";
        }

        return "";
    }

    private String detectSection(WebElement element) {

        try {

            List<WebElement> headers =
                    element.findElements(
                            By.xpath(
                                    "./ancestor::*[" +
                                            "contains(@class,'card') " +
                                            "or contains(@class,'panel') " +
                                            "or contains(@class,'accordion') " +
                                            "or contains(@class,'modal')" +
                                            "][1]" +
                                            "//*[self::h1 or self::h2 or self::h3 or self::h4]"
                            )
                    );

            for (WebElement header : headers) {

                String text = clean(header.getText());

                if (isValidLabel(text)) {
                    return text;
                }
            }

        } catch (Exception ignored) {
        }

        return "";
    }

    private String extractHelpText(WebElement element) {

        String title = safeAttribute(element, "title");

        if (isValidLabel(title)) {
            return title;
        }

        String ariaDescription = safeAttribute(element, "aria-description");

        if (isValidLabel(ariaDescription)) {
            return ariaDescription;
        }

        return "";
    }

    private String detectContainerType(WebElement element) {

        try {

            if (!element.findElements(
                    By.xpath("./ancestor::*[contains(@class,'modal') or @role='dialog']")
            ).isEmpty()) {
                return "MODAL";
            }

            if (!element.findElements(
                    By.xpath("./ancestor::*[contains(@class,'tab')]")
            ).isEmpty()) {
                return "TAB";
            }

            if (!element.findElements(
                    By.xpath("./ancestor::*[contains(@class,'accordion')]")
            ).isEmpty()) {
                return "ACCORDION";
            }

        } catch (Exception ignored) {
        }

        return "PAGE";
    }

    private String extractTableSummary(WebElement table) {
        return "Tabela e të dhënave";
    }

    private String buildCssSelector(WebElement element) {

        String id = safeAttribute(element, "id");

        if (!id.isEmpty()) {
            return "#" + id;
        }

        String name = safeAttribute(element, "name");

        if (!name.isEmpty()) {
            return element.getTagName() + "[name='" + name + "']";
        }

        String ariaLabel = safeAttribute(element, "aria-label");

        if (!ariaLabel.isEmpty()) {
            return element.getTagName() + "[aria-label='" + ariaLabel + "']";
        }

        String title = safeAttribute(element, "title");

        if (!title.isEmpty()) {
            return element.getTagName() + "[title='" + title + "']";
        }

        return element.getTagName();
    }

    private String getTextFromAttributes(String ariaLabel, String title, String className) {

        if (isValidLabel(ariaLabel)) {
            return ariaLabel;
        }

        if (isValidLabel(title)) {
            return title;
        }

        String lower = safe(className).toLowerCase();

        if (lower.contains("add") || lower.contains("plus")) {
            return "Shto";
        }

        if (lower.contains("delete") || lower.contains("trash") || lower.contains("remove")) {
            return "Fshi";
        }

        if (lower.contains("edit") || lower.contains("pencil")) {
            return "Modifiko";
        }

        if (lower.contains("import") || lower.contains("upload")) {
            return "Importo";
        }

        if (lower.contains("export") || lower.contains("download")) {
            return "Eksporto";
        }

        if (lower.contains("filter")) {
            return "Filtro";
        }

        if (lower.contains("search")) {
            return "Kërko";
        }

        return "";
    }

    private boolean isDuplicate(List<ElementInfo> existing, ElementInfo current) {

        for (ElementInfo item : existing) {

            boolean sameTag = safe(item.getTagName()).equalsIgnoreCase(safe(current.getTagName()));
            boolean sameText = safe(item.getText()).equalsIgnoreCase(safe(current.getText()));
            boolean sameSelector = safe(item.getCssSelector()).equalsIgnoreCase(safe(current.getCssSelector()));

            if (sameTag && sameText && sameSelector && !safe(current.getText()).isEmpty()) {
                return true;
            }
        }

        return false;
    }

    private boolean isValidLabel(String text) {

        if (text == null || text.isBlank()) {
            return false;
        }

        text = clean(text);
        String lower = text.toLowerCase();

        if (text.length() < 2 || text.length() > 80) {
            return false;
        }

        if (text.matches("^[0-9\\s]+$")) {
            return false;
        }

        if (text.matches(".*\\d{4,}.*")) {
            return false;
        }

        if (lower.contains("e para")
                || lower.contains("e fundit")
                || lower.contains("e kaluara")
                || lower.contains("tjetra")
                || lower.contains("pagination")
                || lower.contains("rows per page")
                || lower.contains("items per page")
                || lower.contains("placeholder")
                || lower.contains("example")
                || lower.contains("javascript")) {
            return false;
        }

        if (lower.contains("999") || lower.contains("000")) {
            return false;
        }

        return true;
    }

    private String safeTag(WebElement element) {

        try {
            return element.getTagName();
        } catch (Exception e) {
            return "";
        }
    }

    private String safeText(WebElement element) {

        try {
            return element.getText().trim();
        } catch (Exception e) {
            return "";
        }
    }

    private String safeAttribute(WebElement element, String attribute) {

        try {
            String value = element.getAttribute(attribute);

            return value != null ? value.trim() : "";

        } catch (Exception e) {
            return "";
        }
    }

    private String clean(String text) {

        if (text == null) {
            return "";
        }

        return text
                .replace("\n", " ")
                .replace("\r", " ")
                .replaceAll("[ \\t]+", " ")
                .trim();
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
