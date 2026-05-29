package org.example.scanner;
import org.example.model.PageInteractionInfo;
import org.example.utils.ScreenshotUtil;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import java.util.*;
public class PageInteractionScanner {
    private final WebDriver driver;
    public PageInteractionScanner(WebDriver driver) {
        this.driver = driver;
    }
    public List<PageInteractionInfo> scanPageInteractions(String screenshotName) {
        List<PageInteractionInfo> interactions = new ArrayList<>();
        List<WebElement> elements = findInteractiveElements();
        Set<String> processed = new HashSet<>();
        int counter = 1;

        PageInteractionInfo tableWorkflow =
                scanRandomTableRowWorkflow(screenshotName);

        if (tableWorkflow != null) {
            interactions.add(tableWorkflow);
        }

        if (tableWorkflow != null && tableWorkflow.isPageChanged()) {
            interactions.addAll(
                    scanDetailPageActionButtons(screenshotName)
            );
        }


        for (WebElement element : elements) {
            try {
                if (!element.isDisplayed() || !element.isEnabled()) {
                    continue;
                }
                if (isInsideTableHeader(element)) {
                    continue;
                }
                String elementType = detectElementType(element);
                String elementText = resolveElementText(element);
                if ("TABLE_ROW".equals(elementType)) {
                    continue;
                }
                if (!isUsefulInteraction(elementType, elementText)) {
                    continue;
                }
                String actionType = detectActionType(elementType);
                String interactionKey =
                        buildInteractionKey(elementType, elementText, actionType);
                if (processed.contains(interactionKey)) {
                    continue;
                }
                processed.add(interactionKey);
                String description =
                        generateDescription(elementType, elementText, actionType);
                String originalUrl = driver.getCurrentUrl();
                interact(element, elementType);
                Thread.sleep(500);
                boolean pageChanged =
                        !driver.getCurrentUrl().equals(originalUrl);
                boolean modalOpened =
                        isModalOpened();
                String resultDescription =
                        generateResultDescription(
                                modalOpened,
                                pageChanged,
                                elementType
                        );


                String interactionScreenshot =
                        captureInteractionScreenshot(
                                element,
                                screenshotName + "_interaction_" + counter,
                                elementType
                        );


                interactions.add(
                        new PageInteractionInfo(
                                elementType,
                                elementText,
                                actionType,
                                description,
                                resultDescription,
                                interactionScreenshot,
                                modalOpened,
                                pageChanged
                        )
                );
                counter++;
                closeOpenedModal();
                if (pageChanged) {
                    driver.navigate().back();
                    Thread.sleep(1000);
                }
            } catch (Exception ignored) {
            }
        }
        return interactions;
    }


    private List<WebElement> findInteractiveElements() {

        return driver.findElements(
                By.xpath(
                        "//table//tbody//tr[td and not(contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZËÇ','abcdefghijklmnopqrstuvwxyzëç'),'nuk ka asnjë')) and not(contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZËÇ','abcdefghijklmnopqrstuvwxyzëç'),'nuk ka asnje'))] " +

                                "| //*[@role='row' and .//*[@role='cell'] and not(contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZËÇ','abcdefghijklmnopqrstuvwxyzëç'),'nuk ka asnjë')) and not(contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZËÇ','abcdefghijklmnopqrstuvwxyzëç'),'nuk ka asnje'))] " +

                                "| //button " +
                                "| //*[@role='button'] " +
                                "| //a[contains(@class,'btn')] " +

                                "| //input[@type='checkbox'] " +
                                "| //input[@type='radio'] " +

                                "| //select " +
                                "| //mat-select " +
                                "| //p-dropdown " +
                                "| //*[@role='combobox'] " +
                                "| //*[contains(@class,'ng-select')] " +
                                "| //*[contains(@class,'select2')] " +

                                "| //*[@role='tab'] " +
                                "| //li[contains(@class,'tab')] " +

                                "| //div[contains(@class,'tree')]//*[self::span or self::div]"
                )
        );
    }

    private void interact(WebElement element, String type) {
        try {
            scrollIntoView(element);
            /*
            if ("TABLE_ROW".equals(type)) {
                new Actions(driver).doubleClick(element).perform();
                return;
            }

             */

            if ("TABLE_ROW".equals(type)) {

                WebElement target = element;

                try {
                    List<WebElement> cells =
                            element.findElements(By.xpath("./td | .//*[@role='cell']"));

                    if (!cells.isEmpty()) {
                        target = cells.get(0);
                    }

                    ((JavascriptExecutor) driver).executeScript(
                            "arguments[0].scrollIntoView({block:'center'});",
                            target
                    );

                    Thread.sleep(400);

                    new Actions(driver)
                            .moveToElement(target)
                            .click()
                            .pause(200)
                            .doubleClick()
                            .perform();

                    Thread.sleep(800);

                } catch (Exception e) {

                    try {
                        ((JavascriptExecutor) driver)
                                .executeScript(
                                        "arguments[0].dispatchEvent(new MouseEvent('dblclick', {bubbles:true}));",
                                        target
                                );
                    } catch (Exception ignored) {
                    }
                }

                return;
            }


            if ("DROPDOWN".equals(type)) {
                element.click();
                return;
            }
            if ("CHECKBOX".equals(type) || "RADIO".equals(type)) {
                element.click();
                return;
            }
            if ("TREE_ITEM".equals(type)) {
                element.click();
                Thread.sleep(300);
                return;
            }
            try {
                element.click();
            } catch (Exception e) {
                ((JavascriptExecutor) driver)
                        .executeScript("arguments[0].click();", element);
            }
        } catch (Exception ignored) {
        }
    }
    private String detectElementType(WebElement element) {
        try {
            String tag = safe(element.getTagName()).toLowerCase();
            String role = safe(element.getAttribute("role")).toLowerCase();
            String type = safe(element.getAttribute("type")).toLowerCase();
            String clazz = safe(element.getAttribute("class")).toLowerCase();

            if ("tr".equals(tag) || "row".equals(role)) {
                return "TABLE_ROW";
            }

            if ("checkbox".equals(type)) {
                return "CHECKBOX";
            }
            if ("radio".equals(type)) {
                return "RADIO";
            }
            if ("select".equals(tag)
                    || "mat-select".equals(tag)
                    || "p-dropdown".equals(tag)
                    || clazz.contains("dropdown")
                 //   || clazz.contains("select")
                    || "combobox".equals(role)
                    || clazz.contains("ng-select")
                    || clazz.contains("select2")

            ) {
                return "DROPDOWN";
            }
            if ("tr".equals(tag)) {
                return "TABLE_ROW";
            }
            if ("tab".equals(role)
                    || clazz.contains("nav-link")
                    || clazz.contains("tab")) {
                return "TAB";
            }
            if (isTreeElement(element)) {
                return "TREE_ITEM";
            }
            if ("button".equals(tag)
                    || "button".equals(role)
                    || clazz.contains("btn")) {
                return "BUTTON";
            }
            return "INTERACTIVE_ELEMENT";
        } catch (Exception e) {
            return "INTERACTIVE_ELEMENT";
        }
    }
    private boolean isTreeElement(WebElement element) {
        try {
            String clazz = safe(element.getAttribute("class")).toLowerCase();
            String text = safe(element.getText()).toLowerCase();
            if (clazz.contains("tree")
                    || clazz.contains("folder")
                    || clazz.contains("node")) {
                return true;
            }
            return !element.findElements(
                    By.xpath("./ancestor::*[contains(@class,'tree')]")
            ).isEmpty()
                    && text.length() > 1;
        } catch (Exception e) {
            return false;
        }
    }
    private String detectActionType(String elementType) {
        switch (elementType) {
            case "BUTTON":
                return "CLICK";
            case "DROPDOWN":
                return "OPEN_DROPDOWN";
            case "CHECKBOX":
            case "RADIO":
                return "TOGGLE";
            case "TABLE_ROW":
                return "DOUBLE_CLICK";
            case "TAB":
                return "CHANGE_TAB";
            case "TREE_ITEM":
                return "EXPAND_COLLAPSE";
            default:
                return "INTERACT";
        }
    }
    private String resolveElementText(WebElement element) {
        try {
            String tag = safe(element.getTagName()).toLowerCase();
            String type = safe(element.getAttribute("type")).toLowerCase();
            String aria = safe(element.getAttribute("aria-label"));
            if (isValidText(aria)) {
                return cleanText(aria);
            }
            String title = safe(element.getAttribute("title"));
            if (isValidText(title)) {
                return cleanText(title);
            }
            /*
            String placeholder = safe(element.getAttribute("placeholder"));
            if (isValidText(placeholder)) {
                return cleanText(placeholder);
            }
            String label = findNearbyLabel(element);
            if (isValidText(label)) {
                return cleanText(label);
            }

             */

            String label = findNearbyLabel(element);
            if (isValidText(label)) {
                return cleanText(label);
            }

            String placeholder = safe(element.getAttribute("placeholder"));
            if (isValidText(placeholder)) {
                return cleanText(placeholder);
            }

            String text = safe(element.getText());
            if (isValidText(text)) {
                return cleanText(text);
            }
            String clazz = safe(element.getAttribute("class")).toLowerCase();
            if ("checkbox".equals(type)) {
                return "përzgjedhjen";
            }
            if ("radio".equals(type)) {
                return "opsionin";
            }
            if (clazz.contains("trash") || clazz.contains("delete")) {
                return "Fshi";
            }
            if (clazz.contains("edit") || clazz.contains("pencil")) {
                return "Modifiko";
            }
            if (clazz.contains("plus") || clazz.contains("add")) {
                return "Shto";
            }
            if (clazz.contains("download") || clazz.contains("export")) {
                return "Shkarko";
            }
            if ("button".equals(tag)) {
                return "Buton";
            }
            return "";
        } catch (Exception e) {
            return "";
        }
    }
    private String findNearbyLabel(WebElement element) {
        try {
            String id = safe(element.getAttribute("id"));
            if (!id.isBlank()) {
                List<WebElement> labels =
                        driver.findElements(By.xpath("//label[@for='" + id + "']"));
                for (WebElement label : labels) {
                    String text = safe(label.getText());
                    if (isValidText(text)) {
                        return text;
                    }
                }
            }
            List<WebElement> labels =
                    element.findElements(By.xpath("./preceding::label[1]"));
            for (WebElement label : labels) {
                String text = safe(label.getText());
                if (isValidText(text)) {
                    return text;
                }
            }
        } catch (Exception ignored) {
        }
        return "";
    }
    private boolean isUsefulInteraction(String elementType, String elementText) {
        if (elementType == null || elementType.isBlank()) {
            return false;
        }
        String text =
                elementText == null
                        ? ""
                        : elementText.trim().toLowerCase();
        if (text.equals("i")
                || text.equals("svg")
                || text.equals("icon")
                || text.equals("element")
                || text.length() <= 1) {
            return false;
        }
        if (text.contains("e para")
                || text.contains("e kaluara")
                || text.contains("tjetra")
                || text.contains("e fundit")
                || text.contains("pagination")
                || text.contains("rows per page")) {
            return false;
        }

        if (text.endsWith(":")) {
            return false;
        }

        if (text.contains("--zgjidhni--")
                || text.equals("zgjidhni")
                || text.equals("zgjidh")) {
            return false;
        }
        return true;
    }
    private String generateDescription(
            String elementType,
            String text,
            String actionType
    ) {
        text = cleanText(text);
        if (text.isBlank()) {
            text = "elementin përkatës";
        }
        String lower = text.toLowerCase();
        if ("BUTTON".equals(elementType)) {
            if (lower.contains("kërko") || lower.contains("kerko") || lower.contains("search")) {
                return "Kryen kërkimin sipas kritereve të vendosura.";
            }
            if (lower.contains("pastro") || lower.contains("clear") || lower.contains("reset")) {
                return "Pastron filtrat ose vlerat e vendosura në faqe.";
            }
            if (lower.contains("shkarko")
                    || lower.contains("download")
                    || lower.contains("eksporto")
                    || lower.contains("export")) {
                return "Shkarkon ose eksporton të dhënat e faqes.";
            }
            if (lower.contains("shto") || lower.contains("add") || lower.contains("+")) {
                return "Hap funksionalitetin për shtimin e një rekordi të ri.";
            }
            if (lower.contains("fshi") || lower.contains("delete") || lower.contains("trash")) {
                return "Hap funksionalitetin për fshirjen ose çaktivizimin e të dhënës përkatëse.";
            }
            if (lower.contains("modifiko") || lower.contains("ndrysho") || lower.contains("edit")) {
                return "Hap funksionalitetin për modifikimin e të dhënave ekzistuese.";
            }
            if (lower.contains("kthehu") || lower.contains("back")) {
                return "Kthen përdoruesin në faqen e mëparshme.";
            }
            return "Klikon butonin \"" + text + "\".";
        }
        if ("DROPDOWN".equals(elementType)) {
            return "Hap listën \"" + text + "\" për përzgjedhje.";
        }
        if ("CHECKBOX".equals(elementType)) {
            return "Aktivizon ose çaktivizon përzgjedhjen \"" + text + "\".";
        }
        if ("RADIO".equals(elementType)) {
            return "Zgjedh opsionin \"" + text + "\".";
        }
        if ("TABLE_ROW".equals(elementType)) {
            return "Hap detajet e rreshtit të përzgjedhur.";
        }
        if ("TAB".equals(elementType)) {
            return "Ndryshon seksionin aktiv në \"" + text + "\".";
        }
        if ("TREE_ITEM".equals(elementType)) {
            return "Hap ose mbyll elementin hierarkik \"" + text + "\".";
        }
        return "Ndërvepron me elementin \"" + text + "\".";
    }
    private String generateResultDescription(
            boolean modalOpened,
            boolean pageChanged,
            String elementType
    ) {
        if (modalOpened) {
            return "Pas ndërveprimit hapet dritarja përkatëse.";
        }
        if (pageChanged) {
            return "Pas ndërveprimit hapet faqja ose seksioni përkatës.";
        }
        if ("DROPDOWN".equals(elementType)) {
            return "Lista e përzgjedhjes hapet me sukses.";
        }
        if ("CHECKBOX".equals(elementType)
                || "RADIO".equals(elementType)) {
            return "Gjendja e përzgjedhjes ndryshohet me sukses.";
        }
        if ("TABLE_ROW".equals(elementType)) {
            return "Shfaqen detajet e regjistrimit të përzgjedhur.";
        }
        if ("TREE_ITEM".equals(elementType)) {
            return "Struktura hierarkike përditësohet me sukses.";
        }
        if ("TAB".equals(elementType)) {
            return "Shfaqet seksioni i përzgjedhur.";
        }
        return "Ndërveprimi kryhet me sukses.";
    }
    private String buildInteractionKey(
            String elementType,
            String elementText,
            String actionType
    ) {
        String text =
                elementText == null
                        ? ""
                        : elementText.toLowerCase().trim();
        if ("TREE_ITEM".equals(elementType)) {
            return "TREE_ITEM|EXPAND_COLLAPSE";
        }
        if ("TABLE_ROW".equals(elementType)) {
            return "TABLE_ROW|DOUBLE_CLICK";
        }
        if ("CHECKBOX".equals(elementType)) {
            return "CHECKBOX|TOGGLE";
        }
        if ("RADIO".equals(elementType)) {
            return "RADIO|TOGGLE";
        }
        if ("DROPDOWN".equals(elementType)) {
            return "DROPDOWN|OPEN";
        }
        if ("TAB".equals(elementType)) {
            return "TAB|" + text;
        }
        if ("BUTTON".equals(elementType)) {
            if (text.contains("kërko") || text.contains("kerko") || text.contains("search")) {
                return "BUTTON|SEARCH";
            }
            if (text.contains("pastro") || text.contains("clear") || text.contains("reset")) {
                return "BUTTON|CLEAR";
            }
            if (text.contains("shkarko")
                    || text.contains("download")
                    || text.contains("eksporto")
                    || text.contains("export")) {
                return "BUTTON|EXPORT";
            }
            if (text.contains("shto") || text.contains("add") || text.contains("+")) {
                return "BUTTON|ADD";
            }
            if (text.contains("fshi")
                    || text.contains("delete")
                    || text.contains("trash")) {
                return "BUTTON|DELETE";
            }
            if (text.contains("modifiko")
                    || text.contains("ndrysho")
                    || text.contains("edit")
                    || text.contains("pencil")) {
                return "BUTTON|EDIT";
            }
            if (text.contains("kthehu") || text.contains("back")) {
                return "BUTTON|BACK";
            }
            return "BUTTON|" + text;
        }
        return elementType + "|" + actionType;
    }
    private String captureInteractionScreenshot(
            WebElement element,
            String screenshotName,
            String elementType
    ) {

        if ("BUTTON".equals(elementType)
                || "DROPDOWN".equals(elementType)
                || "CHECKBOX".equals(elementType)
                || "RADIO".equals(elementType)) {
            return "";
        }

        try {
            if (element != null && element.isDisplayed()) {
                return ScreenshotUtil.captureElementScreenshot(
                        element,
                        screenshotName
                );
            }
        } catch (Exception ignored) {
        }

        return "";
    }

    private boolean isModalOpened() {
        try {
            List<WebElement> modals =
                    driver.findElements(
                            By.xpath(
                                    "//*[contains(@class,'modal') and not(contains(@style,'display: none'))] " +
                                            "| //*[@role='dialog'] " +
                                            "| //p-dialog " +
                                            "| //*[contains(@class,'dialog')] " +
                                            "| //*[contains(@class,'popup')]"
                            )
                    );
            for (WebElement modal : modals) {
                if (modal.isDisplayed()
                        && modal.getSize().height > 120
                        && modal.getSize().width > 180) {
                    return true;
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }
    private void closeOpenedModal() {
        try {
            List<WebElement> buttons =
                    driver.findElements(
                            By.xpath(
                                    "//button[contains(.,'Mbyll')] " +
                                            "| //button[contains(.,'Close')] " +
                                            "| //button[contains(.,'Anulo')] " +
                                            "| //button[contains(@class,'close')] " +
                                            "| //*[@aria-label='Close']"
                            )
                    );
            for (WebElement button : buttons) {
                try {
                    if (button.isDisplayed() && button.isEnabled()) {
                        button.click();
                        Thread.sleep(500);
                        return;
                    }
                } catch (Exception ignored) {
                }
            }
            new Actions(driver)
                    .sendKeys(Keys.ESCAPE)
                    .perform();
            Thread.sleep(300);
        } catch (Exception ignored) {
        }
    }
    private boolean isInsideTableHeader(WebElement element) {
        try {
            return !element.findElements(
                    By.xpath("./ancestor::thead | ./ancestor::th")
            ).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }
    private void scrollIntoView(WebElement element) {
        try {
            ((JavascriptExecutor) driver)
                    .executeScript(
                            "arguments[0].scrollIntoView({block:'center'});",
                            element
                    );
            Thread.sleep(150);
        } catch (Exception ignored) {
        }
    }
    private boolean isValidText(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }
        text = cleanText(text);
        String lower = text.toLowerCase();
        if (text.length() < 2 || text.length() > 90) {
            return false;
        }
        if (lower.equals("i")
                || lower.equals("svg")
                || lower.equals("icon")
                || lower.equals("input")
                || lower.equals("button")) {
            return false;
        }
        if (lower.equals("--zgjidhni--")
                || lower.equals("zgjidhni")
                || lower.equals("zgjidh")
                || lower.endsWith(":")) {
            return false;
        }

        if (lower.matches("\\d+")) {
            return false;
        }
        if (lower.contains("rows per page")
                || lower.contains("pagination")
                || lower.contains("javascript")) {
            return false;
        }
        return true;
    }
    private String cleanText(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\n", " ")
                .replace("\r", " ")
                .replaceAll("[ \\t]+", " ")
                .trim();
    }
    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private PageInteractionInfo scanRandomTableRowWorkflow(String screenshotName) {

        try {

            List<WebElement> rows =
                    driver.findElements(
                            By.xpath(
                                    "//table//tbody//tr[" +
                                            "td " +
                                            "and string-length(normalize-space(.)) > 20 " +
                                            "and not(contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZËÇ','abcdefghijklmnopqrstuvwxyzëç'),'nuk ka asnjë')) " +
                                            "and not(contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZËÇ','abcdefghijklmnopqrstuvwxyzëç'),'nuk ka asnje'))" +
                                            "]"
                            )
                    );

            if (rows.isEmpty()) {
                return null;
            }

            Random random = new Random();
            WebElement row =
                    rows.get(random.nextInt(rows.size()));

            WebElement target =
                    findBestRowClickTarget(row);

            String originalUrl =
                    driver.getCurrentUrl();

            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({block:'center'});",
                    target
            );

            Thread.sleep(500);

            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].dispatchEvent(new MouseEvent('mouseover', {bubbles:true}));" +
                            "arguments[0].dispatchEvent(new MouseEvent('mousedown', {bubbles:true}));" +
                            "arguments[0].dispatchEvent(new MouseEvent('mouseup', {bubbles:true}));" +
                            "arguments[0].dispatchEvent(new MouseEvent('click', {bubbles:true}));" +
                            "arguments[0].dispatchEvent(new MouseEvent('dblclick', {bubbles:true}));",
                    target
            );

            Thread.sleep(1500);

            boolean pageChanged =
                    !driver.getCurrentUrl().equals(originalUrl);

            String screenshotPath =
                    ScreenshotUtil.captureScreenshot(
                            driver,
                            screenshotName + "_table_row_details"
                    );

            return new PageInteractionInfo(
                    "TABLE_ROW",
                    "rresht i rastësishëm në tabelë",
                    "DOUBLE_CLICK",
                    "Hap detajet e një aplikimi duke zgjedhur një rresht rastësor nga tabela.",
                    pageChanged
                            ? "Pas zgjedhjes së rreshtit hapet faqja e detajeve të aplikimit."
                            : "Rreshti u klikua, por faqja e detajeve nuk u hap automatikisht.",
                    screenshotPath,
                    false,
                    pageChanged
            );

        } catch (Exception e) {
            return null;
        }
    }


    private WebElement findBestRowClickTarget(WebElement row) {

        try {

            List<WebElement> cells =
                    row.findElements(
                            By.xpath(
                                    "./td[" +
                                            "not(.//button) " +
                                            "and not(.//input) " +
                                            "and not(.//a) " +
                                            "and string-length(normalize-space(.)) > 2" +
                                            "]"
                            )
                    );

            if (cells.size() >= 2) {
                return cells.get(1);
            }

            if (!cells.isEmpty()) {
                return cells.get(0);
            }

        } catch (Exception ignored) {
        }

        return row;
    }


    private List<PageInteractionInfo> scanDetailPageActionButtons(String screenshotName) {

        List<PageInteractionInfo> actions = new ArrayList<>();

        try {
            List<WebElement> buttons = driver.findElements(
                    By.xpath(
                            "//button[not(ancestor::*[@role='dialog'])] " +
                                    "| //*[@role='button' and not(ancestor::*[@role='dialog'])]"
                    )
            );

            Set<String> processed = new HashSet<>();
            int counter = 1;

            for (WebElement button : buttons) {

                try {
                    if (!button.isDisplayed() || !button.isEnabled()) {
                        continue;
                    }

                    String text = resolveDetailButtonText(button);

                    if (text.isBlank()) {
                        continue;
                    }

                    String key = text.toLowerCase().trim();

                    if (processed.contains(key)) {
                        continue;
                    }

                    processed.add(key);
/*
                    String description = generateDetailButtonDescription(text);

                    actions.add(
                            new PageInteractionInfo(
                                    "DETAIL_BUTTON",
                                    text,
                                    "CLICK",
                                    description,
                                    "Ky veprim mund të përdoret nga faqja e detajeve të aplikimit.",
                                    "",
                                    false,
                                    false
                            )
                    );

 */
                    String description = generateDetailButtonDescription(text);

                    String buttonScreenshot =
                            ScreenshotUtil.captureElementScreenshot(
                                    button,
                                    screenshotName + "_detail_button_" + counter
                            );

                    actions.add(
                            new PageInteractionInfo(
                                    "DETAIL_BUTTON",
                                    text,
                                    "CLICK",
                                    description,
                                    "Ky veprim mund të përdoret nga faqja e detajeve të aplikimit.",
                                    buttonScreenshot,
                                    false,
                                    false
                            )
                    );

                    counter++;

                } catch (Exception ignored) {
                }
            }

        } catch (Exception ignored) {
        }

        return actions;
    }


    private String resolveDetailButtonText(WebElement button) {

        String text = cleanText(button.getText());

        if (isValidText(text)) {
            return text;
        }

        String title = cleanText(button.getAttribute("title"));

        if (isValidText(title)) {
            return title;
        }

        String aria = cleanText(button.getAttribute("aria-label"));

        if (isValidText(aria)) {
            return aria;
        }

        String clazz = safe(button.getAttribute("class")).toLowerCase();
        String html = safe(button.getAttribute("innerHTML")).toLowerCase();

        if (clazz.contains("bell") || html.contains("bell")) {
            return "Njoftim";
        }

        if (clazz.contains("edit") || clazz.contains("pencil") || html.contains("edit") || html.contains("pencil")) {
            return "Modifiko aplikimin";
        }

        if (clazz.contains("undo") || clazz.contains("back") || html.contains("undo") || html.contains("arrow-left")) {
            return "Kthehu pas";
        }

        if (clazz.contains("redo") || html.contains("redo") || html.contains("arrow-right")) {
            return "Vazhdo në hapin tjetër";
        }

        if (clazz.contains("trash") || clazz.contains("delete") || html.contains("trash")) {
            return "Fshi";
        }

        return "";
    }


    private String generateDetailButtonDescription(String text) {

        String lower = text.toLowerCase();

        if (lower.contains("njoftim")) {
            return "Dërgon ose shfaq njoftimin për aplikimin e përzgjedhur.";
        }

        if (lower.contains("modifiko")) {
            return "Hap mundësinë për modifikimin e të dhënave të aplikimit.";
        }

        if (lower.contains("kthehu")) {
            return "Kthen përdoruesin në hapin ose faqen e mëparshme të aplikimit.";
        }

        if (lower.contains("vazhdo")) {
            return "Vazhdon rrjedhën e punës drejt hapit tjetër të aplikimit.";
        }

        if (lower.contains("fshi")) {
            return "Fshin ose çaktivizon rekordin përkatës.";
        }

        return "Ekzekuton veprimin \"" + text + "\" në faqen e detajeve të aplikimit.";
    }

}