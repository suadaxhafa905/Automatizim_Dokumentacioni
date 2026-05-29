package org.example.model;

import java.util.List;

/*
Model që ruan informacion për një modal:

titullin
screenshot-in
elementët e modalit
butonin që e hapi.
 */
public class ModalInfo {

    private String modalTitle;
    private String openedByButton;
    private String screenshotPath;
    private List<ElementInfo> elements;

    public ModalInfo(
            String modalTitle,
            String openedByButton,
            String screenshotPath,
            List<ElementInfo> elements
    ) {
        this.modalTitle = modalTitle == null ? "" : modalTitle.trim();
        this.openedByButton = openedByButton == null ? "" : openedByButton.trim();
        this.screenshotPath = screenshotPath == null ? "" : screenshotPath.trim();
        this.elements = elements;
    }

    public String getModalTitle() {
        return modalTitle;
    }

    public String getOpenedByButton() {
        return openedByButton;
    }

    public String getScreenshotPath() {
        return screenshotPath;
    }

    public List<ElementInfo> getElements() {
        return elements;
    }
}
