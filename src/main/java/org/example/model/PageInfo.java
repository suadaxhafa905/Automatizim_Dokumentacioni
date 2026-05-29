package org.example.model;

import java.util.ArrayList;
import java.util.List;

/*
Model kryesor i faqes. Ruan:

titullin
URL
screenshot-in
elementët
modals
veprimet funksionale
tekstin ndihmës.

 */

public class PageInfo {

    private String pageTitle;
    private String pageUrl;
    private String screenshotPath;
    private List<ElementInfo> elements;
    private String helpSectionText;
    private List<ModalInfo> modals;
    private List<PageActionInfo> pageActions;

    private List<PageInteractionInfo> interactions;

    public PageInfo(
            String pageTitle,
            String pageUrl,
            String screenshotPath,
            List<ElementInfo> elements,
            String helpSectionText,
            List<ModalInfo> modals,
            List<PageActionInfo> pageActions,

            List<PageInteractionInfo> interactions
    ) {
        this.pageTitle = pageTitle == null ? "" : pageTitle.trim();
        this.pageUrl = pageUrl == null ? "" : pageUrl.trim();
        this.screenshotPath = screenshotPath == null ? "" : screenshotPath.trim();
        this.elements = elements == null ? new ArrayList<>() : elements;
        this.helpSectionText = helpSectionText == null ? "" : helpSectionText.trim();
        this.modals = modals == null ? new ArrayList<>() : modals;
        this.pageActions = pageActions == null ? new ArrayList<>() : pageActions;

        this.interactions =
                interactions == null
                        ? new ArrayList<>()
                        : interactions;
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

    public List<PageActionInfo> getPageActions() {
        return pageActions;
    }

    public void setModals(List<ModalInfo> modals) {
        this.modals = modals == null ? new ArrayList<>() : modals;
    }

    public void setPageActions(List<PageActionInfo> pageActions) {
        this.pageActions = pageActions == null ? new ArrayList<>() : pageActions;
    }

    public List<PageInteractionInfo> getInteractions() {
        return interactions;
    }
}