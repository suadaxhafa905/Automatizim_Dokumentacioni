package org.example.model;

public class PageInteractionInfo {

    private String elementType;
    private String elementText;
    private String actionType;
    private String description;
    private String resultDescription;
    private String screenshotPath;
    private boolean modalOpened;
    private boolean pageChanged;

    public PageInteractionInfo(
            String elementType,
            String elementText,
            String actionType,
            String description,
            String resultDescription,
            String screenshotPath,
            boolean modalOpened,
            boolean pageChanged
    ) {
        this.elementType = elementType;
        this.elementText = elementText;
        this.actionType = actionType;
        this.description = description;
        this.resultDescription = resultDescription;
        this.screenshotPath = screenshotPath;
        this.modalOpened = modalOpened;
        this.pageChanged = pageChanged;
    }

    public String getElementType() {
        return elementType;
    }

    public String getElementText() {
        return elementText;
    }

    public String getActionType() {
        return actionType;
    }

    public String getDescription() {
        return description;
    }

    public String getResultDescription() {
        return resultDescription;
    }

    public String getScreenshotPath() {
        return screenshotPath;
    }

    public boolean isModalOpened() {
        return modalOpened;
    }

    public boolean isPageChanged() {
        return pageChanged;
    }
}