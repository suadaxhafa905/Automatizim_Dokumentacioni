package org.example;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.example.crawler.MenuCrawler;
import org.example.generator.AcceptanceTestGenerator;
import org.example.generator.ManualGenerator;
import org.example.model.PageInfo;
import org.example.scanner.PageScanner;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import org.example.model.ModalInfo;
import org.example.scanner.ModalScanner;

import java.util.*;

import org.openqa.selenium.chrome.ChromeOptions;

/*
hap browser-in
bën login
crawl-on faqet
skanon sistemin
gjeneron manualin
gjeneron acceptance tests

Është entry point i projektit.
 */

public class Main {

    public static void main(String[] args) throws Exception {

        long startTime = System.currentTimeMillis();

        WebDriverManager.chromedriver().setup();

  //      WebDriver driver = new ChromeDriver();

        ChromeOptions options = new ChromeOptions();

        options.addArguments("--disable-notifications");
        options.addArguments("--disable-popup-blocking");
        options.addArguments("--disable-infobars");
        options.addArguments("--disable-save-password-bubble");

        options.addArguments("--disable-features=PasswordLeakDetection");
        options.addArguments("--disable-features=PasswordManagerOnboarding");
        options.addArguments("--disable-features=AutofillServerCommunication");
        options.addArguments("--disable-features=AutofillEnableAccountWalletStorage");

        options.addArguments("--password-store=basic");
        options.addArguments("--use-mock-keychain");

        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        Map<String, Object> prefs =
                new HashMap<>();

        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
        prefs.put("profile.password_manager_leak_detection", false);
        prefs.put("profile.default_content_setting_values.notifications", 2);

        options.setExperimentalOption(
                "prefs",
                prefs
        );

        WebDriver driver =
                new ChromeDriver(options);

        driver.manage().window().maximize();

      //  driver.get("https://smial.arsimi.gov.al/");

        driver.get(" http://46.105.74.97:2340/auth/login");
        Thread.sleep(1000);

        driver.findElement(
                By.xpath("//button[contains(text(),'Identifikohu me të dhëna')]")
        ).click();

        Thread.sleep(1000);

        driver.findElement(By.name("email"))
                .sendKeys("admin.test");

        driver.findElement(By.name("password"))
                .sendKeys("Test@123");

        driver.findElement(By.id("kt_sign_in_submit"))
                .click();

        Thread.sleep(1000);

        List<PageInfo> scannedPages =
                new ArrayList<>();

        PageScanner pageScanner = new PageScanner(driver);

        PageInfo dashboardPage = pageScanner.scanCurrentPage("dashboard_page");

        scannedPages.add(dashboardPage);

        MenuCrawler menuCrawler =
                new MenuCrawler(driver);

        Set<String> links =
                menuCrawler.collectInternalLinks();

        System.out.println("Faqet e gjetura:");

        Set<String> visitedUrls =
                new HashSet<>();

        visitedUrls.add(
                driver.getCurrentUrl()
        );

        int pageIndex = 2;

        for (String link : links) {

            System.out.println(link);

            if (visitedUrls.contains(link)) {
                continue;
            }

            visitedUrls.add(link);

            driver.get(link);

            Thread.sleep(600);

            PageInfo scannedPage =
                    pageScanner.scanCurrentPage(
                            "page_" + pageIndex
                    );

            ModalScanner modalScanner =
                    new ModalScanner(driver);

            List<ModalInfo> modals =
                    modalScanner.scanPageModals(
                            "page_" + pageIndex
                    );

            scannedPage.setModals(modals);

            if (scannedPage.getPageTitle()
                    .equalsIgnoreCase("Error")) {

                continue;
            }

            boolean exists =
                    scannedPages.stream()
                            .anyMatch(p ->
                                    p.getPageUrl()
                                            .equalsIgnoreCase(
                                                    scannedPage.getPageUrl()
                                            )
                            );

            if (exists) {
                continue;
            }
            scannedPages.add(scannedPage);

            pageIndex++;
        }

        ManualGenerator manualGenerator =
                new ManualGenerator();

        manualGenerator.generateManualForPages(
                scannedPages,
                "Manual_Perdorimi_V2.docx"
        );

        AcceptanceTestGenerator acceptanceGenerator =
                new AcceptanceTestGenerator();

        acceptanceGenerator.generateAcceptanceTestsForPages(
                scannedPages,
                "Acceptance_Test_V2.docx"
        );

        driver.quit();

        long endTime = System.currentTimeMillis();
        long durationMillis = endTime - startTime;

        long seconds = durationMillis / 1000;
        long minutes = seconds / 60;
        long remainingSeconds = seconds % 60;
        System.out.println("Dokumentet u gjeneruan me sukses!");
        System.out.println("Koha totale e gjenerimit: "
                + minutes + " minuta dhe "
                + remainingSeconds + " sekonda.");

    }
}