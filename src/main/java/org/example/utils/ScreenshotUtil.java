package org.example.utils;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;

import java.io.File;

public class ScreenshotUtil {

    public static String captureScreenshot(
            WebDriver driver,
            String screenshotName
    ) {

        try {

            JavascriptExecutor js =
                    (JavascriptExecutor) driver;

            driver.manage().window().setSize(
                    new Dimension(1600, 900)
            );

            js.executeScript("window.scrollTo(0, 0);");

            Thread.sleep(200);

            File src =
                    ((TakesScreenshot) driver)
                            .getScreenshotAs(OutputType.FILE);

            String folder =
                    "generated/screenshots/";

            new File(folder).mkdirs();

            String path =
                    folder + sanitizeFileName(screenshotName) + ".png";

            FileUtils.copyFile(
                    src,
                    new File(path)
            );

            return path;

        } catch (Exception e) {

            e.printStackTrace();
            return "";
        }
    }

    public static String captureElementScreenshot(
            WebElement element,
            String screenshotName
    ) {

        try {

            if (element == null) {
                return "";
            }

            File src =
                    element.getScreenshotAs(OutputType.FILE);

            String folder =
                    "generated/screenshots/";

            new File(folder).mkdirs();

            String path =
                    folder + sanitizeFileName(screenshotName) + ".png";

            FileUtils.copyFile(
                    src,
                    new File(path)
            );

            return path;

        } catch (Exception e) {

            e.printStackTrace();
            return "";
        }
    }

    private static String sanitizeFileName(String value) {

        if (value == null || value.isBlank()) {
            return "screenshot";
        }

        return value
                .replaceAll("[^a-zA-Z0-9._-]", "_")
                .replaceAll("_+", "_")
                .trim();
    }
}