package org.example.model;

import java.util.ArrayList;
import java.util.List;

/*
ElementInfo

Model që ruan informacion për një element të UI:
- tag
- text
- type
- id
- placeholder
- css selector
- label
- required
- table columns
- action type
- section
- help text
- container type

Përfaqëson:
- Një element të faqes web
*/

public class ElementInfo {

    private String tagName;
    private String text;
    private String type;
    private String name;
    private String placeholder;
    private String id;
    private String cssSelector;

    private String label;
    private boolean required;
    private List<String> tableColumns;
    private String actionType;
    private String section;
    private String helpText;
    private String containerType;

    public ElementInfo(
            String tagName,
            String text,
            String type,
            String name,
            String placeholder,
            String id,
            String cssSelector,
            String label,
            boolean required,
            List<String> tableColumns,
            String actionType,
            String section,
            String helpText,
            String containerType
    ) {

        this.tagName = safe(tagName);
        this.text = safe(text);
        this.type = safe(type);
        this.name = safe(name);
        this.placeholder = safe(placeholder);
        this.id = safe(id);
        this.cssSelector = safe(cssSelector);

        this.label = safe(label);
        this.required = required;

        this.tableColumns =
                tableColumns != null
                        ? tableColumns
                        : new ArrayList<>();

        this.actionType = safe(actionType);
        this.section = safe(section);
        this.helpText = safe(helpText);
        this.containerType = safe(containerType);
    }

    public String getTagName() {
        return tagName;
    }

    public String getText() {
        return text;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public String getId() {
        return id;
    }

    public String getCssSelector() {
        return cssSelector;
    }

    public String getLabel() {
        return label;
    }

    public boolean isRequired() {
        return required;
    }

    public List<String> getTableColumns() {
        return tableColumns;
    }

    public String getActionType() {
        return actionType;
    }

    public String getSection() {
        return section;
    }

    public String getHelpText() {
        return helpText;
    }

    public String getContainerType() {
        return containerType;
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}