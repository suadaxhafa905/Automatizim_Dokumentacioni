package org.example;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.example.crawler.MenuCrawler;
import org.example.generator.AcceptanceTestGenerator;
import org.example.generator.ManualGenerator;
import org.example.model.ModalScanResult;
import org.example.model.PageInfo;
import org.example.scanner.ModalScanner;
import org.example.scanner.PageScanner;
import org.example.utils.LoginUtil;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import java.util.*;
public class Main {
    public static void main(String[] args) throws Exception {
        long startTime = System.currentTimeMillis();
        WebDriverManager.chromedriver().setup();
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
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
        prefs.put("profile.password_manager_leak_detection", false);
        prefs.put("profile.default_content_setting_values.notifications", 2);
        options.setExperimentalOption("prefs", prefs);
        WebDriver driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        LoginUtil.login(
                driver,
             "http://46.105.74.97:2340/auth/login",
          //      "http://46.105.74.97:2355/auth/logincredentials",
                "pergjegjes.njehsime",
                "Test@123"
        );
        Thread.sleep(1000);
        List<PageInfo> scannedPages = new ArrayList<>();
        PageScanner pageScanner = new PageScanner(driver);
        PageInfo dashboardPage =
                pageScanner.scanCurrentPage("dashboard_page");
        scannedPages.add(dashboardPage);
        MenuCrawler menuCrawler = new MenuCrawler(driver);
        Set<String> links = menuCrawler.collectInternalLinks();
        System.out.println("Faqet e gjetura:");
        Set<String> visitedUrls = new HashSet<>();
        Set<String> scannedPageTitles = new HashSet<>();
        visitedUrls.add(driver.getCurrentUrl());
        scannedPageTitles.add(
                normalizePageTitle(dashboardPage.getPageTitle())
        );
        int pageIndex = 2;
        for (String link : links) {
            try {
                System.out.println(link);
                if (visitedUrls.contains(link)) {
                    continue;
                }
                visitedUrls.add(link);
                driver.get(link);
                Thread.sleep(700);
                PageInfo scannedPage =
                        pageScanner.scanCurrentPage(
                                "page_" + pageIndex
                        );
                if (scannedPage == null) {
                    continue;
                }
                if (scannedPage.getPageTitle()
                        .equalsIgnoreCase("Error")) {
                    continue;
                }
                String normalizedTitle =
                        normalizePageTitle(
                                scannedPage.getPageTitle()
                        );
                if (scannedPageTitles.contains(normalizedTitle)) {
                    System.out.println(
                            "Faqja u anashkalua sepse ekziston: "
                                    + scannedPage.getPageTitle()
                    );
                    continue;
                }
                boolean existsByUrl =
                        scannedPages.stream()
                                .anyMatch(p ->
                                        p.getPageUrl()
                                                .equalsIgnoreCase(
                                                        scannedPage.getPageUrl()
                                                )
                                );
                if (existsByUrl) {
                    continue;
                }
                ModalScanner modalScanner =
                        new ModalScanner(driver);
                ModalScanResult modalResult =
                        modalScanner.scanPageModals(
                                "page_" + pageIndex
                        );
                scannedPage.setModals(
                        modalResult.getModals()
                );
                scannedPage.setPageActions(
                        modalResult.getPageActions()
                );
                scannedPages.add(scannedPage);
                scannedPageTitles.add(normalizedTitle);
                pageIndex++;
            } catch (Exception e) {
                System.out.println(
                        "Gabim gjatë skanimit të faqes: "
                                + link
                );
                e.printStackTrace();
            }
        }
        ManualGenerator manualGenerator =
                new ManualGenerator();
        manualGenerator.generateManualForPages(
                scannedPages,
                "Manual_Perdorimi_V5.docx"
        );
        AcceptanceTestGenerator acceptanceGenerator =
                new AcceptanceTestGenerator();
        acceptanceGenerator.generateAcceptanceTestsForPages(
                scannedPages,
                "Acceptance_Test_V5.docx"
        );
        driver.quit();
        long endTime = System.currentTimeMillis();
        long durationMillis = endTime - startTime;
        long seconds = durationMillis / 1000;
        long minutes = seconds / 60;
        long remainingSeconds = seconds % 60;
        System.out.println("Dokumentet u gjeneruan me sukses!");
        System.out.println(
                "Koha totale e gjenerimit: "
                        + minutes
                        + " minuta dhe "
                        + remainingSeconds
                        + " sekonda."
        );
    }
    private static String normalizePageTitle(String title) {
        if (title == null) {
            return "";
        }
        return title
                .toLowerCase()
                .replaceAll("\\(\\d+\\)", "")
                .replaceAll("[^a-zA-Z0-9ëËçÇ ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}