package org.example.utils;

import org.example.model.PageInfo;

/*
Merr titullin final nga PageInfo.getPageTitle() dhe e validon.
Përdoret sidomos nga manuali që titujt e kapitujve dhe figurave të jenë korrektë.
 */
public class PageTitleResolver {

    public static String resolve(PageInfo pageInfo) {

        String title =
                clean(pageInfo.getPageTitle());

        if (isValidTitle(title)) {
            return title;
        }

        return "Faqja";
    }

    private static boolean isValidTitle(String text) {

        if (text == null || text.trim().isEmpty()) {
            return false;
        }

        text = clean(text);

        String lower =
                text.toLowerCase();

        if (text.length() < 3 || text.length() > 150) {
            return false;
        }

        if (lower.equals("smial")
                || lower.equals("faqja")
                || lower.equals("menu")
                || lower.equals("navigation")) {
            return false;
        }

        if (lower.contains("klikoni")
                || lower.contains("zgjidh")
                || lower.contains("plotësoni")
                || lower.contains("informacion mbi funksionimin")
                || lower.contains("dokument i gjeneruar")
                || lower.contains("duke treguar")) {
            return false;
        }

        if (text.matches("\\d+")
                || text.matches(".*\\d+%.*")) {
            return false;
        }

        return true;
    }

    private static String clean(String text) {

        if (text == null) {
            return "";
        }

        return text
                .replace("\n", " ")
                .replace("\r", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}