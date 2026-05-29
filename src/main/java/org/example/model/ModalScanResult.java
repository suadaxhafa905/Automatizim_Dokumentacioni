package org.example.model;


/*
Model ndihmës që përmban:

listën e modaleve
listën e veprimeve funksionale të faqes.
 */
import java.util.List;

public class ModalScanResult {

    private List<ModalInfo> modals;
    private List<PageActionInfo> pageActions;

    public ModalScanResult(
            List<ModalInfo> modals,
            List<PageActionInfo> pageActions
    ) {
        this.modals = modals;
        this.pageActions = pageActions;
    }

    public List<ModalInfo> getModals() {
        return modals;
    }

    public List<PageActionInfo> getPageActions() {
        return pageActions;
    }
}