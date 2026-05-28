package org.example.utils;

import org.example.model.ElementInfo;

/*
Filtron elementë të panevojshëm:

hidden fields
duplicate elements
elementë të menusë
elementë pa vlerë funksionale

Përmirëson cilësinë e dokumentimit.

 */


public class ElementFilter {

    public static boolean shouldInclude(
            ElementInfo element
    ) {

        String tag = safe(element.getTagName());
        String text = safe(element.getText());
        String type = safe(element.getType());
        String name = safe(element.getName());
        String placeholder = safe(element.getPlaceholder());
        String label = safe(element.getLabel());

        if (tag.isEmpty()) {
            return false;
        }

        if (text.isEmpty()
                && name.isEmpty()
                && placeholder.isEmpty()
                && label.isEmpty()) {
            return false;
        }

        if ("input".equalsIgnoreCase(tag)
                && "hidden".equalsIgnoreCase(type)) {
            return false;
        }

        if (isInvalidText(text)
                && isInvalidText(name)
                && isInvalidText(placeholder)
                && isInvalidText(label)) {
            return false;
        }

        if (("button".equalsIgnoreCase(tag)
                || "a".equalsIgnoreCase(tag))
                && isInvalidActionText(text)) {
            return false;
        }

        return true;
    }

    private static boolean isInvalidText(String value) {

        value = safe(value);

        if (value.isEmpty()) {
            return true;
        }

        String lower = value.toLowerCase();

        if (value.length() < 2) {
            return true;
        }

        if (value.matches("^[0-9\\s]+$")) {
            return true;
        }

        if (value.matches(".*\\d{4,}.*")) {
            return true;
        }

        if (lower.contains("soft & solution")
                || lower.contains("rows per page")
                || lower.contains("items per page")
                || lower.contains("pagination")
                || lower.contains("placeholder")
                || lower.contains("example")
                || lower.contains("javascript")) {
            return true;
        }

        if (lower.contains("e para")
                || lower.contains("e kaluara")
                || lower.contains("tjetra")
                || lower.contains("e fundit")) {
            return true;
        }

        if (lower.contains("999")
                || lower.contains("000")) {
            return true;
        }

        return lower.equals("ok")
                || lower.equals("yes")
                || lower.equals("no")
                || lower.equals("x")
                || lower.equals("--zgjidhni--")
                || lower.equals("zgjidh");
    }

    private static boolean isInvalidActionText(String value) {

        value = safe(value);

        if (value.isEmpty()) {
            return true;
        }

        String lower = value.toLowerCase();

        if (isInvalidText(value)) {
            return true;
        }

        return lower.contains("e para")
                || lower.contains("e kaluara")
                || lower.contains("tjetra")
                || lower.contains("e fundit");
    }

    private static String safe(String value) {

        return value == null
                ? ""
                : value.trim();
    }
}