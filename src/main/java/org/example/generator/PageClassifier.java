package org.example.generator;

import org.example.model.ElementInfo;
import org.example.model.PageInfo;
import org.example.model.PageType;

/*
PageClassifier
Klasifikon tipin e faqes:

-dashboard
-search page
-form page
-statistics page
-management page

bazuar në elementët që zbulohen në UI.
*/



public class PageClassifier {

    public PageType classify(
            PageInfo pageInfo
    ) {

        boolean hasTable = false;
        boolean hasSearch = false;
        boolean hasSave = false;
        boolean hasDashboard = false;
        boolean hasStatistics = false;
        boolean hasWorkflow = false;

        int inputCount = 0;

        String allText = "";

        for (ElementInfo element : pageInfo.getElements()) {

            String tag = safe(element.getTagName()).toLowerCase();
            String text = safe(element.getText()).toLowerCase();
            String placeholder = safe(element.getPlaceholder()).toLowerCase();

            String combined = text + " " + placeholder;

            allText += combined + " ";

            if ("table".equals(tag)) {
                hasTable = true;
            }

            if ("input".equals(tag)) {
                inputCount++;
            }

            if (combined.contains("kërko")
                    || combined.contains("search")) {
                hasSearch = true;
            }

            if (combined.contains("ruaj")
                    || combined.contains("save")) {
                hasSave = true;
            }

            if (combined.contains("dashboard")
                    || combined.contains("tregues")
                    || combined.contains("monitorimi")) {
                hasDashboard = true;
            }

            if (combined.contains("total")
                    || combined.contains("statistika")
                    || combined.contains("grafik")) {
                hasStatistics = true;
            }

            if (combined.contains("workflow")
                    || combined.contains("rrjedhë pune")
                    || combined.contains("hapat")) {
                hasWorkflow = true;
            }
        }

        if (hasDashboard) {
            return PageType.DASHBOARD;
        }

        if (hasStatistics) {
            return PageType.STATISTICS_PAGE;
        }

        if (hasWorkflow) {
            return PageType.WORKFLOW_PAGE;
        }

        if (hasSearch && hasTable) {
            return PageType.SEARCH_PAGE;
        }

        if (inputCount >= 4 && hasSave) {
            return PageType.FORM_PAGE;
        }

        if (hasTable) {
            return PageType.TABLE_PAGE;
        }

        return PageType.UNKNOWN;
    }

    private String safe(
            String value
    ) {

        return value == null
                ? ""
                : value.trim();
    }
}