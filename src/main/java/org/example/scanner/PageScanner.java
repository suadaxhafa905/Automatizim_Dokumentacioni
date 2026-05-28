

/*
Analizon faqen aktuale:

-merr titullin
-screenshot
-elementët
-krijon PageInfo

Është:

Page metadata extractor


 */
package org.example.scanner;

import org.example.model.ElementInfo;
import org.example.model.ModalInfo;
import org.example.model.PageInfo;
import org.example.utils.ScreenshotUtil;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PageScanner {

    private final WebDriver driver;

    public PageScanner(WebDriver driver) {
        this.driver = driver;
    }

    public PageInfo scanCurrentPage(String screenshotName) {

        waitForPageLoad();

        String pageTitle = resolvePageTitle();
        String helpSectionText = extractHelpSectionText();

      //  triggerPageActionsBeforeScreenshot();

        String screenshotPath = ScreenshotUtil.captureScreenshot(driver, screenshotName);

        ElementScanner elementScanner = new ElementScanner(driver);
        List<ElementInfo> elements = elementScanner.scanElements();

      // List<ModalInfo> modals = scanPageModals(screenshotName);

      List<ModalInfo> modals = new ArrayList<>();

        return new PageInfo(
                pageTitle,
                driver.getCurrentUrl(),
                screenshotPath,
                elements,
                helpSectionText,
                modals
        );
    }

    private List<ModalInfo> scanPageModals(String screenshotName) {

        List<ModalInfo> modals = new ArrayList<>();
        Set<String> capturedActionTypes = new HashSet<>();

        int modalCounter = 1;
        int maxAttempts = 1;  //suada

        for (int i = 0; i < maxAttempts; i++) {

            try {

                List<WebElement> buttons = findRealActionButtons();

                if (i >= buttons.size()) {
                    break;
                }

                WebElement button = buttons.get(i);

                String signature = getButtonSignature(button);
                String actionType = detectModalActionType(signature);
                String buttonText = resolveButtonText(button);

            //    if (buttonText.isBlank() || actionType.isBlank()) {
               //     continue;
          //      }

                if (actionType.isBlank()) {
                    continue;
                }

                if (capturedActionTypes.contains(actionType)) {
                    continue;
                }

                capturedActionTypes.add(actionType);

                ((JavascriptExecutor) driver).executeScript(
                        "arguments[0].scrollIntoView({block:'center'});",
                        button
                );

                Thread.sleep(700);

                clickElementSafely(button);

                Thread.sleep(700);

                WebElement modal = findOpenedModal();

                if (modal == null) {

                    System.out.println("Modal NUK u hap");

                    continue;
                }

                System.out.println("Modal u hap me sukses");




                String modalTitle = resolveModalTitle(modal);

                String modalScreenshotPath =
                        ScreenshotUtil.captureElementScreenshot(
                                modal,
                                screenshotName + "_modal_" + modalCounter
                        );

                ElementScanner modalElementScanner = new ElementScanner(driver);
                List<ElementInfo> modalElements = modalElementScanner.scanElements(modal);

                modals.add(
                        new ModalInfo(
                                modalTitle,
                                buttonText,
                                modalScreenshotPath,
                                modalElements
                        )
                );

                closeModal(modal);
                driver.switchTo().defaultContent();

                new Actions(driver)
                        .sendKeys(Keys.ESCAPE)
                        .perform();

             //   System.out.println("Klikova butonin: " + buttonText);  //debug

                Thread.sleep(500);
                modalCounter++;
                Thread.sleep(500);



            } catch (StaleElementReferenceException ignored) {
            } catch (Exception ignored) {
            }

        }

        return modals;

    }


    private List<WebElement> findRealActionButtons() {

        List<WebElement> allButtons = driver.findElements(
                By.xpath(
                        "//*[self::button or self::a or @role='button' or contains(@class,'btn') or contains(@class,'action-button')]"
                )
        );

        List<WebElement> result = new ArrayList<>();
        Set<String> addedActionTypes = new HashSet<>();

        for (WebElement button : allButtons) {

            try {

                if (!button.isDisplayed() || !button.isEnabled()) {
                    continue;
                }

                String signature = getButtonSignature(button);
                String actionType = detectModalActionType(signature);

                if (actionType.isBlank()) {
                    continue;
                }

                if (isUnsafeOrNonModalButton(signature)) {
                    continue;
                }

                if (addedActionTypes.contains(actionType)) {
                    continue;
                }

                addedActionTypes.add(actionType);
                result.add(button);

            } catch (Exception ignored) {
            }
        }

        return result;
    }

    private String getButtonSignature(WebElement button) {

        String text = clean(button.getText());
        String title = clean(button.getAttribute("title"));
        String aria = clean(button.getAttribute("aria-label"));
        String className = clean(button.getAttribute("class"));
        String type = clean(button.getAttribute("type"));
        String role = clean(button.getAttribute("role"));
        String innerHtml = clean(button.getAttribute("innerHTML"));
        String outerHtml = clean(button.getAttribute("outerHTML"));

        return (
                text + " " +
                        title + " " +
                        aria + " " +
                        className + " " +
                        type + " " +
                        role + " " +
                        innerHtml + " " +
                        outerHtml
        ).toLowerCase();
    }

    private String resolveButtonText(WebElement button) {

        String text = clean(button.getText());
        String title = clean(button.getAttribute("title"));
        String aria = clean(button.getAttribute("aria-label"));

        if (!text.isBlank()) {
            return text;
        }

        if (!title.isBlank()) {
            return title;
        }

        if (!aria.isBlank()) {
            return aria;
        }

        String signature = getButtonSignature(button);

        if (signature.contains("fa-plus")
                || signature.contains("plus")
                || signature.contains("add")
                || signature.contains("shto")) {
            return "Shto";
        }

        if (signature.contains("fa-edit")
                || signature.contains("fa-pen")
                || signature.contains("pencil")
                || signature.contains("edit")
                || signature.contains("modifiko")
                || signature.contains("ndrysho")) {
            return "Modifiko";
        }

        if (signature.contains("fa-eye")
                || signature.contains("view")
                || signature.contains("details")
                || signature.contains("detaje")
                || signature.contains("shiko")) {
            return "Shiko detaje";
        }

        if (signature.contains("fa-trash")) {
            return "Fshi";
        }


        return "";
    }

    private String detectModalActionType(String value) {

        if (value == null || value.isBlank()) {
            return "";
        }

        String lower = value.toLowerCase();

        if (lower.contains("shto")
                || lower.contains("add")
                || lower.contains("plus")
                || lower.contains("fa-plus")) {
            return "ADD";
        }

        if (lower.contains("modifiko")
                || lower.contains("ndrysho")
                || lower.contains("edit")
                || lower.contains("pencil")
                || lower.contains("fa-edit")
                || lower.contains("fa-pen")) {
            return "EDIT";
        }

        if (lower.contains("detaje")
                || lower.contains("shiko")
                || lower.contains("view")
                || lower.contains("details")
                || lower.contains("fa-eye")) {
            return "VIEW";
        }

        if (lower.contains("fa-trash")
                || lower.contains("delete")
                || lower.contains("fshi")) {
            return "DELETE";
        }

        return "";
    }

    private boolean isUnsafeOrNonModalButton(String signature) {

        if (signature == null) {
            return true;
        }

        String lower = signature.toLowerCase();

        /*
        return lower.contains("fshi")
                || lower.contains("delete")
                || lower.contains("remove")
                || lower.contains("trash")
                || lower.contains("logout")
                || lower.contains("dil")
                || lower.contains("shkarko")
                || lower.contains("download")
                || lower.contains("eksport")
                || lower.contains("export")
                || lower.contains("pagination")
                || lower.contains("next")
                || lower.contains("previous")
                || lower.contains("e para")
                || lower.contains("e fundit")
                || lower.contains("tjetra")
                || lower.contains("e kaluara")
                || lower.contains("role=\"tab\"")
                || lower.contains("role='tab'");

         */

        return lower.contains("logout")
                || lower.contains("dil")
                || lower.contains("download")
                || lower.contains("export")
                || lower.contains("pagination")
                || lower.contains("next")
                || lower.contains("previous");
    }
/*
    private void clickElementSafely(WebElement element) {

        try {
            element.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].click();",
                    element
            );
        }
    }
 */
    private void clickElementSafely(WebElement element) {

        try {

            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({block:'center'});",
                    element
            );

            Thread.sleep(500);

            try {
                element.click();
            } catch (Exception e) {

                ((JavascriptExecutor) driver).executeScript(
                        "arguments[0].click();",
                        element
                );
            }

        } catch (Exception ignored) {
        }
    }


    /*
    private WebElement findOpenedModal() {

        String[] modalXpaths = {
                "//*[contains(@class,'modal') and contains(@class,'show')]",
                "//*[@role='dialog' and not(contains(@style,'display: none'))]",
                "//*[contains(@class,'dialog') and not(contains(@style,'display: none'))]",
                "//*[contains(@class,'modal-content')]/ancestor::*[contains(@class,'modal')][1]",
                "//*[contains(@class,'cdk-overlay-pane') and .//*[@role='dialog']]"
        };

        for (String xpath : modalXpaths) {

            try {

                List<WebElement> elements = driver.findElements(By.xpath(xpath));

                for (WebElement element : elements) {

                    if (element.isDisplayed()) {
                        return element;
                    }
                }

            } catch (Exception ignored) {
            }
        }

        return null;
    }

     */



    private WebElement findOpenedModal() {

        List<By> modalSelectors = List.of(

                By.cssSelector(".modal.show"),
                By.cssSelector(".modal.in"),
                By.cssSelector("[role='dialog']"),
                By.cssSelector(".p-dialog"),
                By.cssSelector(".mat-dialog-container"),
                By.cssSelector(".cdk-overlay-pane"),
                By.cssSelector(".swal2-popup"),
                By.cssSelector(".ng-trigger"),
                By.cssSelector(".dialog"),
                By.cssSelector(".popup"),
                By.cssSelector(".drawer"),
                By.cssSelector(".offcanvas"),

                By.xpath("//div[contains(@class,'modal')]"),
                By.xpath("//div[contains(@class,'dialog')]"),
                By.xpath("//div[contains(@class,'popup')]"),
                By.xpath("//div[contains(@class,'overlay')]")
        );

        for (By selector : modalSelectors) {

            try {

                List<WebElement> modals =
                        driver.findElements(selector);

                for (WebElement modal : modals) {

                    try {

                        if (modal.isDisplayed()
                                && modal.getSize().height > 150
                                && modal.getSize().width > 200) {

                            return modal;
                        }

                    } catch (Exception ignored) {
                    }
                }

            } catch (Exception ignored) {
            }
        }

        return null;
    }



    private String resolveModalTitle(WebElement modal) {

        String[] titleXpaths = {
                ".//*[contains(@class,'modal-title')]",
                ".//*[self::h1 or self::h2 or self::h3 or self::h4]",
                ".//*[contains(@class,'title')]"
        };

        for (String xpath : titleXpaths) {

            try {

                List<WebElement> titles = modal.findElements(By.xpath(xpath));

                for (WebElement title : titles) {

                    String text = clean(title.getText());

                    if (isValidTitle(text)) {
                        return text;
                    }
                }

            } catch (Exception ignored) {
            }
        }

        return "Dritare modale";
    }

    private void closeModal(WebElement modal) {

        try {

            List<WebElement> closeButtons = modal.findElements(
                    By.xpath(
                            ".//*[contains(@class,'btn-close') " +
                                    "or @aria-label='Close' " +
                                    "or @aria-label='Mbyll' " +
                                    "or normalize-space(.)='Mbyll' " +
                                    "or normalize-space(.)='Anulo' " +
                                    "or normalize-space(.)='×']"
                    )
            );

            for (WebElement closeButton : closeButtons) {

                if (closeButton.isDisplayed() && closeButton.isEnabled()) {
                    clickElementSafely(closeButton);
                    Thread.sleep(900);
                    return;
                }
            }

            new Actions(driver).sendKeys(Keys.ESCAPE).perform();
            Thread.sleep(900);

        } catch (Exception ignored) {
        }
    }

    private void waitForPageLoad() {

        try {

            JavascriptExecutor js = (JavascriptExecutor) driver;

            for (int i = 0; i < 20; i++) {

                String state = js.executeScript("return document.readyState").toString();

                if ("complete".equals(state)) {
                    break;
                }

                Thread.sleep(400);
            }

            Thread.sleep(500);

        } catch (Exception ignored) {
        }
    }

    private String extractHelpSectionText() {

        try {
            List<WebElement> helpTriggers = driver.findElements(
                    By.xpath(
                            "//*[contains(translate(normalize-space(.), " +
                                    "'ABCDEFGHIJKLMNOPQRSTUVWXYZËÇ', " +
                                    "'abcdefghijklmnopqrstuvwxyzëç'), " +
                                    "'informacion mbi funksionimin')]"
                    )
            );

            if (helpTriggers.isEmpty()) {
                return "";
            }

            String bestText = "";

            for (WebElement trigger : helpTriggers) {

                try {
                    ((JavascriptExecutor) driver).executeScript(
                            "arguments[0].scrollIntoView({block:'center'});",
                            trigger
                    );

                    Thread.sleep(300);

                    ((JavascriptExecutor) driver).executeScript(
                            "arguments[0].click();",
                            trigger
                    );

                    Thread.sleep(500);

                    List<WebElement> candidates = driver.findElements(
                            By.xpath(
                                    "//*[self::div or self::section or self::article]" +
                                            "[contains(@class,'modal-body') " +
                                            "or contains(@class,'card-body') " +
                                            "or contains(@class,'accordion-body') " +
                                            "or contains(@class,'collapse') " +
                                            "or contains(@class,'help') " +
                                            "or contains(@class,'description') " +
                                            "or contains(@class,'info') " +
                                            "or contains(@class,'content')]"
                            )
                    );

                    for (WebElement candidate : candidates) {

                        if (!candidate.isDisplayed()) {
                            continue;
                        }

                        String text = cleanHelpText(candidate.getText());

                        if (!isValidHelpText(text)) {
                            continue;
                        }

                        if (scoreHelpText(text) > scoreHelpText(bestText)) {
                            bestText = text;
                        }
                    }

                    if (!bestText.isBlank()) {
                        return bestText;
                    }

                } catch (Exception ignored) {
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    private boolean isValidHelpText(String text) {

        if (text == null || text.isBlank()) {
            return false;
        }

        String lower = text.toLowerCase();

        if (text.length() < 80 || text.length() > 2500) {
            return false;
        }

        if (lower.contains("2026©")
                || lower.contains("soft & solution")
                || lower.contains("dashboard")
                || lower.contains("konfigurime statistika")) {
            return false;
        }

        if (looksLikeUniversityList(text)) {
            return false;
        }

        return lower.contains("mund")
                || lower.contains("përdor")
                || lower.contains("klik")
                || lower.contains("shto")
                || lower.contains("modifiko")
                || lower.contains("fshi")
                || lower.contains("kërko")
                || lower.contains("shfaq");
    }

    private boolean looksLikeUniversityList(String text) {

        String lower = text.toLowerCase();
        int count = 0;

        String[] indicators = {
                "universiteti",
                "akademia",
                "kolegji",
                "shkolla e lartë",
                "institucioni privat",
                "epitech",
                "albanian university",
                "western balkans"
        };

        for (String indicator : indicators) {
            if (lower.contains(indicator)) {
                count++;
            }
        }

        return count >= 3 && !lower.contains("ju mund të");
    }

    private int scoreHelpText(String text) {

        if (text == null) {
            return 0;
        }

        String lower = text.toLowerCase();
        int score = 0;

        if (lower.contains("ju mund të")) score += 20;
        if (lower.contains("në këtë faqe")) score += 20;
        if (lower.contains("përdoruesi")) score += 10;
        if (lower.contains("klikoni")) score += 10;
        if (lower.contains("shtoni")) score += 10;
        if (lower.contains("modifikoni")) score += 10;
        if (lower.contains("fshini")) score += 10;
        if (lower.contains("kërkoni")) score += 10;
        if (lower.contains("•")) score += 15;

        score += Math.min(text.length() / 50, 20);

        if (looksLikeUniversityList(text)) {
            score -= 100;
        }

        return score;
    }

    private String cleanHelpText(String text) {

        if (text == null) {
            return "";
        }

        return text
                .replaceAll("(?i)informacion mbi funksionimin e faqes", "")
                .replaceAll("2026©.*", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String resolvePageTitle() {

        String[] titleXPaths = {
                "//*[@id='kt_app_content_container']//*[self::h1 or self::h2 or self::h3]",
                "//*[@id='kt_app_content_container']//*[self::p or self::span or self::div][contains(@class,'title') or contains(@class,'text-') or contains(@class,'fw-bold')]",
                "//*[@id='kt_app_content']//*[self::h1 or self::h2 or self::h3]",
                "//*[@id='kt_app_content']//*[self::p or self::span or self::div][contains(@class,'title') or contains(@class,'text-') or contains(@class,'fw-bold')]",
                "//main//*[self::h1 or self::h2 or self::h3]",
                "//main//*[self::p or self::span or self::div][contains(@class,'title') or contains(@class,'text-') or contains(@class,'fw-bold')]"
        };

        for (String xpath : titleXPaths) {

            try {

                List<WebElement> elements = driver.findElements(By.xpath(xpath));

                for (WebElement element : elements) {

                    if (!element.isDisplayed()) {
                        continue;
                    }

                    String text = clean(element.getText());

                    if (isValidTitle(text) && isTopContentElement(element)) {
                        return text;
                    }
                }

            } catch (Exception ignored) {
            }
        }

        return getReadableTitleFromUrl();
    }

    private boolean isValidTitle(String text) {

        if (text == null || text.isBlank()) {
            return false;
        }

        text = clean(text);
        String lower = text.toLowerCase();

        if (text.length() < 3 || text.length() > 90) {
            return false;
        }

        if (text.matches("\\d+") || text.matches(".*\\d+%.*") || lower.contains("%")) {
            return false;
        }

        if (lower.contains("total:")
                || lower.contains("pagination")
                || lower.contains("rows per page")) {
            return false;
        }

        if (lower.endsWith(":") || lower.endsWith("...")) {
            return false;
        }

        if (lower.equals("faqja")
                || lower.equals("smial")
                || lower.equals("menu")
                || lower.equals("navigation")) {
            return false;
        }

        if (lower.equals("shto")
                || lower.equals("modifiko")
                || lower.equals("fshi")
                || lower.equals("importo")
                || lower.equals("eksporto")
                || lower.equals("ruaj")
                || lower.equals("kërko")
                || lower.equals("kerko")
                || lower.equals("filtro")
                || lower.equals("pastro")) {
            return false;
        }

        return !lower.contains("klikoni")
                && !lower.contains("zgjidhni")
                && !lower.contains("plotësoni")
                && !lower.contains("ju lutem")
                && !lower.contains("informacion mbi funksionimin")
                && !lower.contains("dokument i gjeneruar")
                && !lower.contains("duke treguar");
    }

    private boolean isTopContentElement(WebElement element) {

        try {

            int y = element.getLocation().getY();
            int pageHeight = driver.manage().window().getSize().getHeight();

            return y > 40 && y < pageHeight * 0.50;

        } catch (Exception e) {
            return true;
        }
    }

    private String getReadableTitleFromUrl() {

        String url = driver.getCurrentUrl();

        if (url == null || url.isBlank()) {
            return "Faqja";
        }

        String lastPart = url.substring(url.lastIndexOf("/") + 1);

        if (lastPart.isBlank()) {
            return "Faqja";
        }

        return lastPart
                .replaceAll("([a-z])([A-Z])", "$1 $2")
                .replaceAll("([A-Z]+)([A-Z][a-z])", "$1 $2")
                .replace("-", " ")
                .replace("_", " ")
                .trim();
    }

    private void triggerPageActionsBeforeScreenshot() {

        if (!isStatisticsLikePage()) {
            return;
        }

        String[] actionTexts = {
                "Filtro",
                "Kërko",
                "Kerko",
                "Apliko",
                "Shfaq",
                "Gjenero",
                "Statistika"
        };

        for (String text : actionTexts) {

            try {

                List<WebElement> elements = driver.findElements(
                        By.xpath(
                                "//*[self::button or self::a or @role='button' or contains(@class,'btn')]" +
                                        "[contains(normalize-space(.),'" + text + "')]"
                        )
                );

                for (WebElement element : elements) {

                    if (!element.isDisplayed() || !element.isEnabled()) {
                        continue;
                    }

                    ((JavascriptExecutor) driver).executeScript(
                            "arguments[0].scrollIntoView({block:'center'});",
                            element
                    );

                    Thread.sleep(600);
                    clickElementSafely(element);
                    Thread.sleep(3500);
                    return;
                }

            } catch (Exception ignored) {
            }
        }
    }

    private boolean isStatisticsLikePage() {

        String source = driver.getPageSource().toLowerCase();

        return source.contains("chart")
                || source.contains("grafik")
                || source.contains("statistik")
                || source.contains("canvas")
                || source.contains("apexcharts")
                || source.contains("highcharts");
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
}
