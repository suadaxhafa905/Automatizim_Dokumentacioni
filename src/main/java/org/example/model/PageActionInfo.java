package org.example.model;

/*

Model që ruan informacion për një veprim funksional në faqe:

emrin e butonit
tipin e veprimit
përshkrimin funksional.
 */
public class PageActionInfo {

    private String actionName;
    private String actionType;
    private String description;

    public PageActionInfo(
            String actionName,
            String actionType,
            String description
    ) {
        this.actionName = actionName;
        this.actionType = actionType;
        this.description = description;
    }

    public String getActionName() {
        return actionName;
    }

    public String getActionType() {
        return actionType;
    }

    public String getDescription() {
        return description;
    }

    public void setActionName(String actionName) {
        this.actionName = actionName;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {

        return "PageActionInfo{" +
                "actionName='" + actionName + '\'' +
                ", actionType='" + actionType + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}