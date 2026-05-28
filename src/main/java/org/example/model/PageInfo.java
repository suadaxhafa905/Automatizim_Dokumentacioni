


/*
PageInfo

Model që ruan informacion për një faqe:
-titullin
-URL
-screenshot
-listën e elementëve

Përfaqëson:
Një faqe të plotë të sistemit

Modeli i faqes. Mban: pageTitle, pageUrl, screenshotPath, elements.
 */

package org.example.model;

import java.util.ArrayList;
import java.util.List;

public class PageInfo {

    private String pageTitle;
    private String pageUrl;
    private String screenshotPath;
    private List<ElementInfo> elements;
    private String helpSectionText;
    private List<ModalInfo> modals;

    public PageInfo(
            String pageTitle,
            String pageUrl,
            String screenshotPath,
            List<ElementInfo> elements,
            String helpSectionText,
            List<ModalInfo> modals
    ) {
        this.pageTitle = pageTitle == null ? "" : pageTitle.trim();
        this.pageUrl = pageUrl == null ? "" : pageUrl.trim();
        this.screenshotPath = screenshotPath == null ? "" : screenshotPath.trim();
        this.elements = elements == null ? new ArrayList<>() : elements;
        this.helpSectionText = helpSectionText == null ? "" : helpSectionText.trim();
        this.modals = modals == null ? new ArrayList<>() : modals;
    }

    public String getPageTitle() {
        return pageTitle;
    }

    public String getPageUrl() {
        return pageUrl;
    }

    public String getScreenshotPath() {
        return screenshotPath;
    }

    public List<ElementInfo> getElements() {
        return elements;
    }

    public String getHelpSectionText() {
        return helpSectionText;
    }

    public List<ModalInfo> getModals() {
        return modals;
    }

    public void setModals(List<ModalInfo> modals) {
        this.modals = modals;
    }
}
