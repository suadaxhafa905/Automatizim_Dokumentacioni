package org.example.scanner;

import org.example.model.ElementInfo;
import org.example.model.ModalInfo;
import org.example.utils.ScreenshotUtil;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ModalScanner {

    private final WebDriver driver;

    public ModalScanner(WebDriver driver) {
        this.driver = driver;
    }

    public List<ModalInfo> scanPageModals(String screenshotName) {

        List<ModalInfo> modals = new ArrayList<>();
        Set<String> capturedTypes = new HashSet<>();

        String originalUrl = driver.getCurrentUrl();

        List<WebElement> buttons = findModalButtons();

        int modalCounter = 1;

        for (WebElement button : buttons) {

            try {
                String signature = getButtonSignature(button);
                String actionType = detectModalActionType(signature);

                if (actionType.isBlank() || capturedTypes.contains(actionType)) {
                    continue;
                }

                capturedTypes.add(actionType);

                String buttonText = resolveButtonText(button);

                clickSafely(button);
                Thread.sleep(300);

                if (!driver.getCurrentUrl().equals(originalUrl)) {
                    driver.navigate().to(originalUrl);
                    waitForPageLoad();
                    continue;
                }

                WebElement modal = findOpenedModal();

                if (modal == null) {
                    continue;
                }

                String modalTitle = resolveModalTitle(modal);

                String modalScreenshotPath =
                        ScreenshotUtil.captureElementScreenshot(
                                modal,
                                screenshotName + "_modal_" + modalCounter
                        );

                ElementScanner elementScanner = new ElementScanner(driver);

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

                Thread.sleep(300);

            } catch (Exception ignored) {
                try {
                    driver.navigate().to(originalUrl);
                    waitForPageLoad();
                } catch (Exception ignoredAgain) {
                }
            }
        }

        return modals;
    }

    private List<WebElement> findModalButtons() {

        List<WebElement> allButtons =
                driver.findElements(
                        By.xpath(
                                "//*[self::button or self::a or @role='button' " +
                                        "or contains(@class,'btn') " +
                                        "or contains(@class,'action-button') " +
                                        "or .//*[contains(@class,'fa-plus') " +
                                        "or contains(@class,'fa-edit') " +
                                        "or contains(@class,'fa-pen') " +
                                        "or contains(@class,'fa-trash') " +
                                        "or contains(@class,'plus') " +
                                        "or contains(@class,'edit') " +
                                        "or contains(@class,'pencil') " +
                                        "or contains(@class,'trash')]]"
                        )
                );

        WebElement addButton = null;
        WebElement editButton = null;
        WebElement deleteButton = null;

        for (WebElement button : allButtons) {

            try {
                if (!button.isDisplayed() || !button.isEnabled()) {
                    continue;
                }

                String signature = getLightButtonSignature(button);
                String actionType = detectModalActionType(signature);

                if (actionType.isBlank()) {
                    signature = getFullButtonSignature(button);
                    actionType = detectModalActionType(signature);
                }

                if (actionType.isBlank()) {
                    continue;
                }

                if (isUnsafeButton(button, signature)) {
                    continue;
                }

                if ("ADD".equals(actionType) && addButton == null) {
                    addButton = button;
                }

                if ("EDIT".equals(actionType) && editButton == null) {
                    editButton = button;
                }

                if ("DELETE".equals(actionType) && deleteButton == null) {
                    deleteButton = button;
                }

                if (addButton != null && editButton != null && deleteButton != null) {
                    break;
                }

            } catch (Exception ignored) {
            }
        }

        List<WebElement> result = new ArrayList<>();

        if (addButton != null) {
            result.add(addButton);
        }

        if (editButton != null) {
            result.add(editButton);
        }

        if (deleteButton != null) {
            result.add(deleteButton);
        }

        return result;
    }

    private String getLightButtonSignature(WebElement button) {

        String text = clean(button.getText());
        String title = clean(button.getAttribute("title"));
        String aria = clean(button.getAttribute("aria-label"));
        String className = clean(button.getAttribute("class"));

        return (
                text + " " +
                        title + " " +
                        aria + " " +
                        className
        ).toLowerCase();
    }

    private String getFullButtonSignature(WebElement button) {

        String lightSignature = getLightButtonSignature(button);
        String innerHtml = clean(button.getAttribute("innerHTML"));

        return (
                lightSignature + " " +
                        innerHtml
        ).toLowerCase();
    }

    private String getButtonSignature(WebElement button) {
        return getFullButtonSignature(button);
    }

    private String detectModalActionType(String value) {

        if (value == null || value.isBlank()) {
            return "";
        }

        String lower = value.toLowerCase();

        if (lower.contains("fshi")
                || lower.contains("delete")
                || lower.contains("remove")
                || lower.contains("trash")
                || lower.contains("fa-trash")) {
            return "DELETE";
        }

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

        return "";
    }

    private boolean isUnsafeButton(WebElement button, String signature) {

        String tag = clean(button.getTagName()).toLowerCase();
        String type = clean(button.getAttribute("type")).toLowerCase();
        String href = clean(button.getAttribute("href")).toLowerCase();
        String routerLink = clean(button.getAttribute("routerlink")).toLowerCase();
        String ngRouterLink = clean(button.getAttribute("ng-reflect-router-link")).toLowerCase();

        if ("submit".equals(type)) {
            return true;
        }

        if ("a".equals(tag)
                && !href.isBlank()
                && !href.equals("#")
                && !href.startsWith("javascript")) {
            return true;
        }

        if (!routerLink.isBlank() || !ngRouterLink.isBlank()) {
            return true;
        }

        String lower = signature.toLowerCase();

        return lower.contains("logout")
                || lower.contains("dil")
                || lower.contains("download")
                || lower.contains("export")
                || lower.contains("pagination")
                || lower.contains("next")
                || lower.contains("previous")
                || lower.contains("kërko")
                || lower.contains("kerko")
                || lower.contains("search")
                || lower.contains("filtro")
                || lower.contains("filter")
                || lower.contains("ruaj")
                || lower.contains("save")
                || lower.contains("submit");
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

        if (signature.contains("fa-trash")
                || signature.contains("trash")
                || signature.contains("delete")
                || signature.contains("fshi")) {
            return "Fshi";
        }

        if (signature.contains("fa-eye")
                || signature.contains("view")
                || signature.contains("details")
                || signature.contains("detaje")
                || signature.contains("shiko")) {
            return "Shiko detaje";
        }

        return "Buton";
    }

    private WebElement findOpenedModal() {

        List<By> selectors = List.of(
                By.cssSelector(".modal.show"),
                By.cssSelector("[role='dialog']"),
                By.cssSelector(".p-dialog"),
                By.cssSelector(".mat-dialog-container"),
                By.cssSelector(".cdk-overlay-pane"),
                By.cssSelector(".dialog"),
                By.cssSelector(".popup"),
                By.cssSelector(".offcanvas"),
                By.xpath("//div[contains(@class,'modal')]"),
                By.xpath("//div[contains(@class,'dialog')]"),
                By.xpath("//div[contains(@class,'popup')]")
        );

        for (By selector : selectors) {
            try {
                for (WebElement modal : driver.findElements(selector)) {
                    if (modal.isDisplayed()
                            && modal.getSize().height > 150
                            && modal.getSize().width > 200) {
                        return modal;
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
                for (WebElement title : modal.findElements(By.xpath(xpath))) {
                    String text = clean(title.getText());
                    if (!text.isBlank() && text.length() <= 90) {
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
                                    ".//*[contains(@class,'btn-close') "
                                            + "or @aria-label='Close' "
                                            + "or @aria-label='Mbyll' "
                                            + "or normalize-space(.)='Mbyll' "
                                            + "or normalize-space(.)='Anulo' "
                                            + "or normalize-space(.)='×']"
                            )
                    );

            for (WebElement close : closeButtons) {
                if (close.isDisplayed() && close.isEnabled()) {
                    clickSafely(close);
                    Thread.sleep(300);
                    return;
                }
            }

            new Actions(driver).sendKeys(Keys.ESCAPE).perform();
            Thread.sleep(300);

        } catch (Exception ignored) {
        }
    }

    private void clickSafely(WebElement element) {

        try {
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({block:'center'});",
                    element
            );

            Thread.sleep(200);

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

    private void waitForPageLoad() {

        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;

            for (int i = 0; i < 15; i++) {
                String state =
                        String.valueOf(
                                js.executeScript("return document.readyState")
                        );

                if ("complete".equalsIgnoreCase(state)) {
                    break;
                }

                Thread.sleep(200);
            }

            Thread.sleep(300);

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
}