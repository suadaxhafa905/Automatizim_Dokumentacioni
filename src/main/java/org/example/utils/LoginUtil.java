package org.example.utils;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class LoginUtil {

    public static void login(
            WebDriver driver,
            String loginUrl,
            String username,
            String password
    ) {

        WebDriverWait wait =
                new WebDriverWait(driver, Duration.ofSeconds(15));

        driver.get(loginUrl);
        waitForPage(driver);

        System.out.println("LOGIN URL: " + driver.getCurrentUrl());

        clickOptionalLoginMethodButton(driver);

        System.out.println("Pas optional login button: " + driver.getCurrentUrl());

        WebElement usernameInput =
                findUsernameInput(driver);

        System.out.println("Username input u gjet: " + describe(usernameInput));

        usernameInput.clear();
        usernameInput.sendKeys(username);

        WebElement passwordInput =
                findPasswordInput(driver);

        System.out.println("Password input u gjet: " + describe(passwordInput));

        passwordInput.clear();
        passwordInput.sendKeys(password);

        sleep(500);

        WebElement submitButton =
                findSubmitButton(driver);

        System.out.println("Submit button u gjet: " + describe(submitButton));

        clickElement(driver, submitButton);

        waitForPage(driver);

        System.out.println("URL pas login: " + driver.getCurrentUrl());
    }

    private static void clickOptionalLoginMethodButton(WebDriver driver) {

        List<By> selectors = List.of(
                By.xpath("//button[contains(normalize-space(.),'Identifikohu me të dhëna')]"),
                By.xpath("//button[contains(normalize-space(.),'Identifikohu')]"),
                By.xpath("//button[contains(normalize-space(.),'Hyr')]"),
                By.xpath("//button[contains(normalize-space(.),'Login')]"),
                By.xpath("//button[contains(normalize-space(.),'Sign in')]"),
                By.xpath("//a[contains(normalize-space(.),'Identifikohu me të dhëna')]"),
                By.xpath("//a[contains(normalize-space(.),'Identifikohu')]"),
                By.xpath("//a[contains(normalize-space(.),'Hyr')]"),
                By.xpath("//a[contains(normalize-space(.),'Login')]")
        );

        WebElement button = findFirstVisible(driver, selectors, false);

        if (button == null) {
            System.out.println("Nuk u gjet optional login method button. Vazhdoj direkt...");
            return;
        }

        System.out.println("Klikoj optional login method button: " + describe(button));
        clickElement(driver, button);
        sleep(1200);
        waitForPage(driver);
    }

    private static WebElement findUsernameInput(WebDriver driver) {

        List<By> selectors = List.of(
                By.name("email"),
                By.name("username"),
                By.name("userName"),
                By.name("login"),
                By.name("user"),
                By.name("nid"),
                By.id("email"),
                By.id("username"),
                By.id("userName"),
                By.id("login"),
                By.id("user"),
                By.id("nid"),
                By.cssSelector("input[type='email']"),
                By.cssSelector("input[autocomplete='username']"),
                By.xpath("//input[contains(translate(@placeholder,'ABCDEFGHIJKLMNOPQRSTUVWXYZËÇ','abcdefghijklmnopqrstuvwxyzëç'),'email')]"),
                By.xpath("//input[contains(translate(@placeholder,'ABCDEFGHIJKLMNOPQRSTUVWXYZËÇ','abcdefghijklmnopqrstuvwxyzëç'),'username')]"),
                By.xpath("//input[contains(translate(@placeholder,'ABCDEFGHIJKLMNOPQRSTUVWXYZËÇ','abcdefghijklmnopqrstuvwxyzëç'),'përdorues')]"),
                By.xpath("//input[contains(translate(@placeholder,'ABCDEFGHIJKLMNOPQRSTUVWXYZËÇ','abcdefghijklmnopqrstuvwxyzëç'),'perdorues')]"),
                By.xpath("//input[contains(translate(@placeholder,'ABCDEFGHIJKLMNOPQRSTUVWXYZËÇ','abcdefghijklmnopqrstuvwxyzëç'),'nid')]"),
                By.xpath("//input[not(@type='password') and not(@type='hidden') and not(@disabled)]")
        );

        WebElement input = findFirstVisible(driver, selectors, true);

        if (input == null) {
            printAllInputs(driver);
            throw new NoSuchElementException("Nuk u gjet fusha username/email.");
        }

        return input;
    }

    private static WebElement findPasswordInput(WebDriver driver) {

        List<By> selectors = List.of(
                By.name("password"),
                By.name("pass"),
                By.id("password"),
                By.id("pass"),
                By.cssSelector("input[type='password']"),
                By.cssSelector("input[autocomplete='current-password']")
        );

        WebElement input = findFirstVisible(driver, selectors, true);

        if (input == null) {
            printAllInputs(driver);
            throw new NoSuchElementException("Nuk u gjet fusha password.");
        }

        return input;
    }

    private static WebElement findSubmitButton(WebDriver driver) {

        List<By> selectors = List.of(
                By.id("kt_sign_in_submit"),
                By.cssSelector("button[type='submit']"),
                By.cssSelector("input[type='submit']"),
                By.xpath("//button[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZËÇ','abcdefghijklmnopqrstuvwxyzëç'),'identifikohu')]"),
                By.xpath("//button[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZËÇ','abcdefghijklmnopqrstuvwxyzëç'),'hyr')]"),
                By.xpath("//button[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZËÇ','abcdefghijklmnopqrstuvwxyzëç'),'login')]"),
                By.xpath("//button[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZËÇ','abcdefghijklmnopqrstuvwxyzëç'),'sign in')]"),
                By.xpath("//button[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZËÇ','abcdefghijklmnopqrstuvwxyzëç'),'vazhdo')]"),
                By.xpath("//button[not(@disabled)]")
        );

        WebElement button = findFirstVisible(driver, selectors, true);

        if (button == null) {
            printAllButtons(driver);
            throw new NoSuchElementException("Nuk u gjet butoni submit/login.");
        }

        return button;
    }

    private static WebElement findFirstVisible(
            WebDriver driver,
            List<By> selectors,
            boolean requireEnabled
    ) {

        for (By selector : selectors) {

            try {

                List<WebElement> elements =
                        driver.findElements(selector);

                for (WebElement element : elements) {

                    try {

                        String tag =
                                element.getTagName();

                        String type =
                                element.getAttribute("type");

                        String disabled =
                                element.getAttribute("disabled");

                        if ("hidden".equalsIgnoreCase(type)) {
                            continue;
                        }

                        if (requireEnabled
                                && disabled != null
                                && !disabled.isBlank()) {
                            continue;
                        }

                        return element;

                    } catch (Exception ignored) {
                    }
                }

            } catch (Exception ignored) {
            }
        }

        return null;
    }

    private static void clickElement(
            WebDriver driver,
            WebElement element
    ) {

        try {

            ((JavascriptExecutor) driver)
                    .executeScript(
                            "arguments[0].scrollIntoView({block:'center'});",
                            element
                    );

            sleep(300);

            try {
                element.click();
            } catch (Exception e) {
                ((JavascriptExecutor) driver)
                        .executeScript("arguments[0].click();", element);
            }

        } catch (Exception e) {
            throw new RuntimeException("Nuk u klikua elementi: " + describe(element), e);
        }
    }

    private static void waitForPage(WebDriver driver) {

        try {

            JavascriptExecutor js =
                    (JavascriptExecutor) driver;

            for (int i = 0; i < 30; i++) {

                String state =
                        String.valueOf(
                                js.executeScript("return document.readyState")
                        );

                if ("complete".equalsIgnoreCase(state)) {
                    break;
                }

                sleep(300);
            }

            sleep(1000);

        } catch (Exception ignored) {
        }
    }

    private static String describe(WebElement element) {

        if (element == null) {
            return "null";
        }

        try {
            return "tag="
                    + element.getTagName()
                    + ", text='"
                    + element.getText()
                    + "', id='"
                    + element.getAttribute("id")
                    + "', name='"
                    + element.getAttribute("name")
                    + "', type='"
                    + element.getAttribute("type")
                    + "', class='"
                    + element.getAttribute("class")
                    + "', placeholder='"
                    + element.getAttribute("placeholder")
                    + "'";
        } catch (Exception e) {
            return "element";
        }
    }

    private static void printAllInputs(WebDriver driver) {

        System.out.println("INPUTS NE FAQE:");

        List<WebElement> inputs =
                driver.findElements(By.tagName("input"));

        for (WebElement input : inputs) {
            System.out.println(describe(input));
        }
    }

    private static void printAllButtons(WebDriver driver) {

        System.out.println("BUTTONS NE FAQE:");

        List<WebElement> buttons =
                driver.findElements(By.xpath("//button | //input[@type='submit'] | //a"));

        for (WebElement button : buttons) {
            System.out.println(describe(button));
        }
    }

    private static void sleep(long millis) {

        try {
            Thread.sleep(millis);
        } catch (Exception ignored) {
        }
    }
}