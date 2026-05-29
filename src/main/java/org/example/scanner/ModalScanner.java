package org.example.scanner;

import org.example.model.ElementInfo;
import org.example.model.ModalInfo;
import org.example.model.ModalScanResult;
import org.example.model.PageActionInfo;
import org.example.utils.ScreenshotUtil;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;

import org.example.model.ModalScanResult;
import org.example.model.PageActionInfo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/*

Identifikon dhe klikon butonat funksionalë si:

Shto
Modifiko
Fshi
Shiko

dhe kontrollon nëse hapen modals. Gjithashtu gjeneron screenshot dhe skanon elementët e modalit.
 */
public class ModalScanner {

    private final WebDriver driver;

    public ModalScanner(WebDriver driver) {
        this.driver = driver;
    }

    //  public List<ModalInfo> scanPageModals(String screenshotName)
    public ModalScanResult scanPageModals(String screenshotName)

    {

        List<ModalInfo> modals = new ArrayList<>();
        List<PageActionInfo> pageActions = new ArrayList<>();


        Set<String> capturedModalKeys = new HashSet<>();

        String originalUrl = driver.getCurrentUrl();

        List<ButtonCandidate> buttons =
                findClickablePageButtons();

        int modalCounter = 1;

     //  for (int i = 0; i < buttons.size(); i++)
        for (ButtonCandidate candidate : buttons)
        {

            try {
/*
                driver.navigate().to(originalUrl);
                waitForPageLoad();

                buttons = findClickablePageButtons();

                if (i >= buttons.size()) {
                    break;
                }

 */



          //      ButtonCandidate candidate = buttons.get(i);

                WebElement button = candidate.element;
                String buttonText = candidate.text;

                clickSafely(button);

                Thread.sleep(400);

                if (!driver.getCurrentUrl().equals(originalUrl)) {
                    driver.navigate().to(originalUrl);
                    waitForPageLoad();
                    continue;
                }

                WebElement modal = findOpenedModal();

                if (modal == null) {
                    closePossibleOverlay();

                    pageActions.add(
                            new PageActionInfo(
                                    buttonText,
                                    candidate.actionType,
                                    generateActionDescription(
                                            buttonText,
                                            candidate.actionType
                                    )
                            )
                    );

                    continue;

                }

                String modalTitle = resolveModalTitle(modal);

                String modalKey =
                        buttonText.toLowerCase() + "|" + modalTitle.toLowerCase();

                if (capturedModalKeys.contains(modalKey)) {
                    closeModal(modal);
                    continue;
                }

                capturedModalKeys.add(modalKey);

                String modalScreenshotPath =
                        ScreenshotUtil.captureElementScreenshot(
                                modal,
                                screenshotName + "_modal_" + modalCounter
                        );

                ElementScanner elementScanner =
                        new ElementScanner(driver);

                List<ElementInfo> elements =
                        elementScanner.scanElements(modal);

                modals.add(
                        new ModalInfo(
                                modalTitle,
                                buttonText,
                                modalScreenshotPath,
                                elements
                        )
                );

                closeModal(modal);

                modalCounter++;

                Thread.sleep(250);

            } catch (Exception ignored) {

                try {
                    driver.navigate().to(originalUrl);
                    waitForPageLoad();
                    closePossibleOverlay();
                } catch (Exception ignoredAgain) {
                }
            }
        }

        //    return modals;

        return new ModalScanResult(
                modals,
                pageActions
        );
    }



    private List<ButtonCandidate> findClickablePageButtons() {

        List<ButtonCandidate> result = new ArrayList<>();

        addCandidates(result, "ADD", List.of(
                "//*[self::button or self::a or @role='button'][contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZËÇ','abcdefghijklmnopqrstuvwxyzëç'),'shto')]",
                "//*[self::button or self::a or @role='button' or contains(@class,'btn')][.//*[contains(@class,'fa-plus') or contains(@class,'bi-plus') or contains(@class,'plus')]]"
        ));

        addCandidates(result, "EDIT", List.of(
                "//*[self::button or self::a or @role='button'][contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZËÇ','abcdefghijklmnopqrstuvwxyzëç'),'modifiko')]",
                "//*[self::button or self::a or @role='button' or contains(@class,'btn')][.//*[contains(@class,'fa-edit') or contains(@class,'fa-pen') or contains(@class,'fa-pencil') or contains(@class,'bi-pencil') or contains(@class,'pencil')]]",
                "//*[self::button or self::a or @role='button' or contains(@class,'btn')][contains(@class,'edit') or contains(@title,'Modifiko') or contains(@aria-label,'Modifiko')]"
        ));

        addCandidates(result, "DELETE", List.of(
                "//*[self::button or self::a or @role='button'][contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZËÇ','abcdefghijklmnopqrstuvwxyzëç'),'fshi')]",
                "//*[self::button or self::a or @role='button' or contains(@class,'btn')][.//*[contains(@class,'fa-trash') or contains(@class,'bi-trash') or contains(@class,'trash')]]",
                "//*[self::button or self::a or @role='button' or contains(@class,'btn')][contains(@class,'delete') or contains(@class,'remove') or contains(@title,'Fshi') or contains(@aria-label,'Fshi')]"
        ));

        addCandidates(result, "VIEW", List.of(
                "//*[self::button or self::a or @role='button'][contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZËÇ','abcdefghijklmnopqrstuvwxyzëç'),'shiko')]",
                "//*[self::button or self::a or @role='button' or contains(@class,'btn')][.//*[contains(@class,'fa-eye') or contains(@class,'bi-eye') or contains(@class,'eye')]]"
        ));

        return result;
    }



    private void addCandidates(
            List<ButtonCandidate> result,
            String actionType,
            List<String> xpaths
    ) {

        Set<String> added = new HashSet<>();

        for (String xpath : xpaths) {

            try {

                List<WebElement> elements =
                        driver.findElements(By.xpath(xpath));

                for (WebElement element : elements) {

                    try {

                        if (!element.isDisplayed()
                                || !element.isEnabled()) {
                            continue;
                        }

                        if (isInsideTableHeader(element)) {
                            continue;
                        }

                        String signature =
                                getButtonSignature(element);

                        String text =
                                resolveButtonText(element);

                        if (text == null || text.isBlank()) {
                            text = resolveTextByActionType(actionType);
                        }

                        if (!isValidClickableButton(
                                element,
                                signature,
                                text
                        )) {
                            continue;
                        }

                        String key =
                                actionType + "|" + signature;

                        if (added.contains(key)) {
                            continue;
                        }

                        added.add(key);

                        result.add(
                                new ButtonCandidate(
                                        element,
                                        text,
                                        actionType
                                )
                        );

                    } catch (Exception ignored) {
                    }
                }

            } catch (Exception ignored) {
            }
        }
    }

    private String resolveTextByActionType(String actionType) {

        if ("ADD".equals(actionType)) {
            return "Shto";
        }

        if ("EDIT".equals(actionType)) {
            return "Modifiko";
        }

        if ("DELETE".equals(actionType)) {
            return "Fshi";
        }

        if ("VIEW".equals(actionType)) {
            return "Shiko detaje";
        }

        return "Buton";
    }


    private boolean isValidClickableButton(
            WebElement button,
            String signature,
            String text
    ) {

        if (text == null || text.isBlank()) {
            return false;
        }

        String lower = (signature + " " + text).toLowerCase();
        String actionType = detectActionType(signature, text);

        if (lower.contains("logout")
                || lower.contains("dil")
                || lower.contains("pagination")
                || lower.contains("next")
                || lower.contains("previous")
                || lower.contains("e para")
                || lower.contains("e fundit")
                || lower.contains("tjetra")
                || lower.contains("e kaluara")
                || lower.contains("pastro")
                || lower.contains("clear")
                || lower.contains("reset")) {
            return false;
        }

        String tag = clean(button.getTagName()).toLowerCase();
        String href = clean(button.getAttribute("href")).toLowerCase();
        String routerLink = clean(button.getAttribute("routerlink")).toLowerCase();
        String ngRouterLink = clean(button.getAttribute("ng-reflect-router-link")).toLowerCase();

        if ("EDIT".equals(actionType)
                || "DELETE".equals(actionType)
                || "VIEW".equals(actionType)) {
            return true;
        }

        if ("a".equals(tag)
                && !href.isBlank()
                && !href.equals("#")
                && !href.startsWith("javascript")) {
            return false;
        }

        if (!routerLink.isBlank() || !ngRouterLink.isBlank()) {
            return false;
        }

        return true;
    }

    private String getButtonSignature(WebElement element) {

        try {

            return String.join(" ",
                    safeAttr(element, "class"),
                    safeAttr(element, "title"),
                    safeAttr(element, "aria-label"),
                    safeAttr(element, "data-icon"),
                    element.getTagName(),
                    safeAttr(element, "innerHTML")
            );

        } catch (Exception e) {
            return "";
        }
    }

    private String safeAttr(WebElement element, String attr) {

        try {

            String value = element.getAttribute(attr);

            return value == null ? "" : value.trim();

        } catch (Exception e) {

            return "";
        }
    }

    private String resolveButtonText(WebElement button) {

        String text = clean(button.getText());
        String title = clean(button.getAttribute("title"));
        String aria = clean(button.getAttribute("aria-label"));

        if (isReadableButtonText(text)) {
            return text;
        }

        if (isReadableButtonText(title)) {
            return title;
        }

        if (isReadableButtonText(aria)) {
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

        if (signature.contains("fa-trash")
                || signature.contains("trash")
                || signature.contains("delete")
                || signature.contains("fshi")
                || signature.contains("remove")) {
            return "Fshi";
        }

        if (signature.contains("fa-eye")
                || signature.contains("view")
                || signature.contains("details")
                || signature.contains("detaje")
                || signature.contains("shiko")) {
            return "Shiko detaje";
        }

        return "";
    }

    private boolean isReadableButtonText(String text) {

        if (text == null || text.isBlank()) {
            return false;
        }

        String clean = clean(text);
        String lower = clean.toLowerCase();

        if (clean.length() < 2 || clean.length() > 60) {
            return false;
        }

        if (lower.contains("rows per page")
                || lower.contains("pagination")
                || lower.contains("items per page")) {
            return false;
        }

        return true;
    }

    private String detectActionType(String signature, String text) {

        String value =
                (signature + " " + text).toLowerCase();

        // CREATE
        if (value.contains("shto")
                || value.contains("add")
                || value.contains("plus")
                || value.contains("create")) {

            return "ADD";
        }

        // EDIT
        if (value.contains("modifiko")
                || value.contains("edit")
                || value.contains("update")
                || value.contains("pencil")
                || value.contains("fa-edit")
                || value.contains("fa-pencil")
                || value.contains("bi-pencil")
                || value.contains("icon-edit")) {

            return "EDIT";
        }

        // DELETE
        if (value.contains("fshi")
                || value.contains("delete")
                || value.contains("trash")
                || value.contains("remove")
                || value.contains("fa-trash")
                || value.contains("bi-trash")
                || value.contains("icon-delete")) {

            return "DELETE";
        }

        // VIEW
        if (value.contains("view")
                || value.contains("details")
                || value.contains("eye")
                || value.contains("fa-eye")) {

            return "VIEW";
        }

        return "OTHER";
    }

    private int getActionPriority(String actionType) {

        if ("ADD".equals(actionType)) {
            return 1;
        }

        if ("EDIT".equals(actionType)) {
            return 2;
        }

        if ("DELETE".equals(actionType)) {
            return 3;
        }

        if ("VIEW".equals(actionType)) {
            return 4;
        }

        return 99;
    }

    private WebElement findOpenedModal() {

        List<By> selectors = List.of(
                By.cssSelector(".modal.show"),
                By.cssSelector(".modal.in"),
                By.cssSelector("[role='dialog']"),
                By.cssSelector(".p-dialog"),
                By.cssSelector(".mat-dialog-container"),
                By.cssSelector(".cdk-overlay-pane"),
                By.cssSelector(".dialog"),
                By.cssSelector(".popup"),
                By.cssSelector(".offcanvas"),
                By.xpath("//div[contains(@class,'modal') and not(contains(@style,'display: none'))]"),
                By.xpath("//div[contains(@class,'dialog') and not(contains(@style,'display: none'))]"),
                By.xpath("//div[contains(@class,'popup') and not(contains(@style,'display: none'))]")
        );

        for (By selector : selectors) {

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

                List<WebElement> titles =
                        modal.findElements(By.xpath(xpath));

                for (WebElement title : titles) {

                    String text = clean(title.getText());

                    if (!text.isBlank()
                            && text.length() <= 90) {
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

            List<WebElement> closeButtons =
                    modal.findElements(
                            By.xpath(
                                    ".//*[contains(@class,'btn-close') " +
                                            "or @aria-label='Close' " +
                                            "or @aria-label='Mbyll' " +
                                            "or normalize-space(.)='Mbyll' " +
                                            "or normalize-space(.)='Anulo' " +
                                            "or normalize-space(.)='Cancel' " +
                                            "or normalize-space(.)='×']"
                            )
                    );

            for (WebElement close : closeButtons) {

                if (close.isDisplayed()
                        && close.isEnabled()) {

                    clickSafely(close);

                    Thread.sleep(250);

                    return;
                }
            }

            new Actions(driver)
                    .sendKeys(Keys.ESCAPE)
                    .perform();

            Thread.sleep(250);

        } catch (Exception ignored) {
        }
    }

    private void closePossibleOverlay() {

        try {

            new Actions(driver)
                    .sendKeys(Keys.ESCAPE)
                    .perform();

            Thread.sleep(200);

        } catch (Exception ignored) {
        }
    }

    private void clickSafely(WebElement element) {

        try {

            ((JavascriptExecutor) driver)
                    .executeScript(
                            "arguments[0].scrollIntoView({block:'center'});",
                            element
                    );

            Thread.sleep(150);

            try {
                element.click();
            } catch (Exception e) {

                ((JavascriptExecutor) driver)
                        .executeScript(
                                "arguments[0].click();",
                                element
                        );
            }

        } catch (Exception ignored) {
        }
    }

    private void waitForPageLoad() {

        try {

            JavascriptExecutor js =
                    (JavascriptExecutor) driver;

            for (int i = 0; i < 10; i++) {

                String state =
                        String.valueOf(
                                js.executeScript(
                                        "return document.readyState"
                                )
                        );

                if ("complete".equalsIgnoreCase(state)) {
                    break;
                }

                Thread.sleep(150);
            }

            Thread.sleep(200);

        } catch (Exception ignored) {
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

    private static class ButtonCandidate {

        private final WebElement element;
        private final String text;
        private final String actionType;

        private ButtonCandidate(
                WebElement element,
                String text,
                String actionType
        ) {
            this.element = element;
            this.text = text;
            this.actionType = actionType;
        }
    }

    private String generateActionDescription(
            String buttonText,
            String actionType
    ) {
        String text = buttonText == null ? "" : buttonText.trim();
        String type = actionType == null ? "" : actionType.trim().toUpperCase();

        if ("SEARCH".equals(type)) {
            return "Butoni \"" + text + "\" përdoret për kërkimin e të dhënave sipas kritereve të vendosura.";
        }

        if ("FILTER".equals(type)) {
            return "Butoni \"" + text + "\" përdoret për filtrimin e rezultateve në faqe.";
        }

        if ("EXPORT".equals(type)) {
            return "Butoni \"" + text + "\" përdoret për eksportimin ose shkarkimin e të dhënave.";
        }

        if ("IMPORT".equals(type)) {
            return "Butoni \"" + text + "\" përdoret për importimin ose ngarkimin e të dhënave në sistem.";
        }

        if ("SAVE".equals(type)) {
            return "Butoni \"" + text + "\" përdoret për ruajtjen e të dhënave të vendosura.";
        }

        if ("ADD".equals(type)) {
            return "Butoni \"" + text + "\" përdoret për shtimin e një rekordi të ri.";
        }

        if ("EDIT".equals(type)) {
            return "Butoni \"" + text + "\" përdoret për modifikimin e të dhënave ekzistuese.";
        }

        if ("DELETE".equals(type)) {
            return "Butoni \"" + text + "\" përdoret për fshirjen ose çaktivizimin e të dhënës përkatëse.";
        }

        if ("VIEW".equals(type)) {
            return "Butoni \"" + text + "\" përdoret për shikimin ose konsultimin e detajeve.";
        }

        return "Butoni \"" + text + "\" përdoret për ekzekutimin e funksionalitetit përkatës në faqe.";
    }


    private boolean isInsideTable(WebElement element) {
        try {
            return !element.findElements(
                    By.xpath(
                            "./ancestor::table " +
                                    "| ./ancestor::tbody " +
                                    "| ./ancestor::tr " +
                                    "| ./ancestor::td"
                    )
            ).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isInsideTableHeader(WebElement element) {
        try {
            return !element.findElements(
                    By.xpath(
                            "./ancestor::thead " +
                                    "| ./ancestor::th"
                    )
            ).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }
}