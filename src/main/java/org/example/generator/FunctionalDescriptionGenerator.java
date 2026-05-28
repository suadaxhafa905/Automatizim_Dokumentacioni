package org.example.generator;

import org.example.model.ElementInfo;
import org.example.model.PageInfo;
import org.example.model.PageType;
import org.example.utils.PageTitleResolver;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class FunctionalDescriptionGenerator {

    public String generate(PageInfo pageInfo) {

        List<ElementInfo> elements = pageInfo.getElements();

        PageClassifier classifier = new PageClassifier();
        PageType pageType = classifier.classify(pageInfo);

        StringBuilder description = new StringBuilder();

        description.append(
                buildDynamicIntro(pageInfo, pageType, elements)
        );

        appendSectionDescription(description, elements);
        appendFieldsDescription(description, elements);
        appendRequiredFieldsDescription(description, elements);
        appendActionDescription(description, elements);
        appendTableDescription(description, elements);
        appendContainerDescription(description, elements);

        return cleanOutput(description.toString());
    }

    private String buildDynamicIntro(
            PageInfo pageInfo,
            PageType pageType,
            List<ElementInfo> elements
    ) {

        String pageName = PageTitleResolver.resolve(pageInfo);
        String allText = collectText(elements).toLowerCase();
        String lowerPageName = pageName.toLowerCase();

        if (pageType == PageType.DASHBOARD) {
            return "Faqja \"" + pageName + "\" paraqet informacion përmbledhës, tregues monitorues dhe të dhëna kryesore të sistemit. ";
        }

        if (pageType == PageType.STATISTICS_PAGE
                || lowerPageName.contains("statistika")
                || allText.contains("statistika")) {
            return "Faqja \"" + pageName + "\" përdoret për analizimin vizual të të dhënave statistikore, filtrimin e rezultateve dhe monitorimin e treguesve kryesorë të sistemit. ";
        }

        if (pageType == PageType.WORKFLOW_PAGE
                || lowerPageName.contains("workflow")
                || lowerPageName.contains("rrjedhë pune")
                || lowerPageName.contains("rrjedhe pune")) {
            return "Faqja \"" + pageName + "\" përdoret për administrimin e proceseve, statuseve dhe rrjedhave të punës në sistem. ";
        }

        if (pageType == PageType.FORM_PAGE) {
            return "Faqja \"" + pageName + "\" mundëson plotësimin, regjistrimin dhe ruajtjen e të dhënave në sistem. ";
        }

        if (pageType == PageType.TABLE_PAGE) {
            return "Faqja \"" + pageName + "\" shfaq të dhënat e sistemit në formë tabelare dhe mundëson administrimin e tyre. ";
        }

        if (pageType == PageType.SEARCH_PAGE) {

            if (allText.contains("student")) {
                return "Faqja \"" + pageName + "\" mundëson kërkimin, filtrimin dhe administrimin e të dhënave të studentëve. ";
            }

            if (allText.contains("përdorues")
                    || allText.contains("perdorues")
                    || allText.contains("rol")) {
                return "Faqja \"" + pageName + "\" përdoret për kërkimin dhe administrimin e përdoruesve, roleve ose të drejtave të aksesit. ";
            }

            if (allText.contains("departament")
                    || allText.contains("struktur")) {
                return "Faqja \"" + pageName + "\" përdoret për kërkimin dhe administrimin e njësive organizative ose departamenteve. ";
            }

            return "Faqja \"" + pageName + "\" përmban funksionalitete kërkimi dhe filtrimi të të dhënave. ";
        }

        if (allText.contains("raport")
                || allText.contains("shkarko")
                || allText.contains("export")) {
            return "Faqja \"" + pageName + "\" përdoret për gjenerimin, filtrimin dhe shkarkimin e raporteve ose të dhënave. ";
        }

        if (allText.contains("student")) {
            return "Faqja \"" + pageName + "\" përdoret për administrimin, kërkimin dhe konsultimin e informacionit të studentëve në sistem. ";
        }

        if (allText.contains("nim") && lowerPageName.contains("nim")) {
            return "Faqja \"" + pageName + "\" përdoret për administrimin dhe verifikimin e numrit të matrikullimit. ";
        }

        return "Faqja \"" + pageName + "\" përdoret për ndërveprim me funksionalitetet përkatëse të sistemit. ";
    }

    private void appendSectionDescription(
            StringBuilder description,
            List<ElementInfo> elements
    ) {

        Set<String> sections = new LinkedHashSet<>();

        for (ElementInfo element : elements) {

            String section = clean(element.getSection());

            if (isValidValue(section) && !looksLikeNavigationOrList(section)) {
                sections.add(section);
            }
        }

        if (!sections.isEmpty()) {
            description.append("Faqja është e organizuar në seksione funksionale si ")
                    .append(joinLimited(sections, 5))
                    .append(", të cilat ndihmojnë përdoruesin të orientohet gjatë përdorimit të saj. ");
        }
    }

    private void appendFieldsDescription(
            StringBuilder description,
            List<ElementInfo> elements
    ) {

        Set<String> fields = new LinkedHashSet<>();

        for (ElementInfo element : elements) {

            if (!isFieldElement(element)) {
                continue;
            }

            String fieldName = getElementDisplayName(element);

            if (isValidValue(fieldName)
                    && !looksLikeNavigationOrList(fieldName)) {
                fields.add(fieldName);
            }
        }

        if (!fields.isEmpty()) {
            description.append("Fushat kryesore të identifikuara në këtë faqe janë: ")
                    .append(joinLimited(fields, 8))
                    .append(". ");
        }
    }

    private void appendRequiredFieldsDescription(
            StringBuilder description,
            List<ElementInfo> elements
    ) {

        Set<String> requiredFields = new LinkedHashSet<>();

        for (ElementInfo element : elements) {

            if (!element.isRequired()) {
                continue;
            }

            if (!isFieldElement(element)) {
                continue;
            }

            String fieldName = getElementDisplayName(element);

            if (isValidValue(fieldName)
                    && !looksLikeNavigationOrList(fieldName)) {
                requiredFields.add(fieldName);
            }
        }

        if (!requiredFields.isEmpty()) {
            description.append("Disa fusha janë të detyrueshme për t’u plotësuar përpara kryerjes së veprimit. ")
                    .append("Fushat e detyrueshme janë: ")
                    .append(joinLimited(requiredFields, 8))
                    .append(". ");
        }
    }

    private void appendActionDescription(
            StringBuilder description,
            List<ElementInfo> elements
    ) {

        Set<String> actions = new LinkedHashSet<>();

        for (ElementInfo element : elements) {

            if (!isRealActionElement(element)) {
                continue;
            }

            String actionType = clean(element.getActionType());
            String buttonName = getElementDisplayName(element);

            if (!isValidActionName(buttonName)) {
                continue;
            }

            if (looksLikeNavigationOrList(buttonName)) {
                continue;
            }

            actions.add(
                    buildActionSentence(buttonName, actionType)
            );
        }

        if (!actions.isEmpty()) {
            description.append("Veprimet kryesore që mund të kryhen në këtë faqe përfshijnë: ")
                    .append(String.join("; ", actions))
                    .append(". ");
        }
    }

    private boolean isFieldElement(ElementInfo element) {

        String tag = clean(element.getTagName()).toLowerCase();
        String type = clean(element.getType()).toLowerCase();

        if (!tag.equals("input")
                && !tag.equals("textarea")
                && !tag.equals("select")) {
            return false;
        }

        return !type.equals("hidden")
                && !type.equals("button")
                && !type.equals("submit")
                && !type.equals("reset");
    }

    private boolean isRealActionElement(ElementInfo element) {

        String tag = clean(element.getTagName()).toLowerCase();
        String type = clean(element.getType()).toLowerCase();
        String actionType = clean(element.getActionType());

        if (tag.equals("select") || tag.equals("textarea")) {
            return false;
        }

        if (tag.equals("input")) {
            return type.equals("button")
                    || type.equals("submit")
                    || type.equals("reset");
        }

        if (tag.equals("button")) {
            return true;
        }

        if (tag.equals("a")) {
            String text = getElementDisplayName(element);

            return isValidValue(text)
                    && !actionType.isBlank()
                    && !looksLikeFieldOrFilter(text);
        }

        return false;
    }

    private boolean isValidActionName(String value) {

        if (!isValidValue(value)) {
            return false;
        }

        String lower = value.toLowerCase();

        if (looksLikeFieldOrFilter(value)) {
            return false;
        }

        if (lower.contains("pyetja")
                || lower.contains("zgjidh")
                || lower.contains("select")
                || lower.contains("choose")
                || lower.contains("dropdown")
                || lower.contains("institucioni")
                || lower.contains("cikli")
                || lower.contains("programi")
                || lower.contains("data")
                || lower.contains("date")) {
            return false;
        }

        return true;
    }

    private boolean looksLikeFieldOrFilter(String value) {

        if (value == null || value.isBlank()) {
            return false;
        }

        String lower = value.toLowerCase().trim();

        return lower.endsWith(":")
                || lower.endsWith("...")
                || lower.startsWith("zgjidh")
                || lower.startsWith("select")
                || lower.contains("zgjidhni")
                || lower.contains("përzgjidh")
                || lower.contains("perzgjidh")
                || lower.contains("fusha")
                || lower.contains("kërko me")
                || lower.contains("kerko me")
                || lower.contains("kërkoni")
                || lower.contains("kerkoni")
                || lower.contains("cikli i studimit")
                || lower.contains("institucioni i arsimit")
                || lower.contains("programi i studimit");
    }

    private void appendTableDescription(
            StringBuilder description,
            List<ElementInfo> elements
    ) {

        for (ElementInfo element : elements) {

            String tag = clean(element.getTagName()).toLowerCase();

            if (!tag.equals("table")) {
                continue;
            }

            List<String> columns = element.getTableColumns();

            if (columns != null && !columns.isEmpty()) {

                Set<String> cleanColumns = new LinkedHashSet<>();

                for (String column : columns) {
                    if (isValidValue(column)
                            && !looksLikeNavigationOrList(column)) {
                        cleanColumns.add(column);
                    }
                }

                if (!cleanColumns.isEmpty()) {
                    description.append("Të dhënat shfaqen në tabelë me kolonat kryesore: ")
                            .append(joinLimited(cleanColumns, 8))
                            .append(". ");
                    return;
                }
            }

            description.append("Të dhënat e faqes shfaqen në formë tabelare për të lehtësuar leximin dhe administrimin e informacionit. ");
            return;
        }
    }

    private void appendContainerDescription(
            StringBuilder description,
            List<ElementInfo> elements
    ) {

        boolean hasModal = false;
        boolean hasTabs = false;
        boolean hasAccordion = false;

        for (ElementInfo element : elements) {

            String container = clean(element.getContainerType()).toUpperCase();

            if (container.equals("MODAL")) {
                hasModal = true;
            }

            if (container.equals("TAB")) {
                hasTabs = true;
            }

            if (container.equals("ACCORDION")) {
                hasAccordion = true;
            }
        }

        if (hasTabs) {
            description.append("Faqja përmban ndarje në tab-e, të cilat organizojnë informacionin sipas kategorive funksionale. ");
        }

        if (hasAccordion) {
            description.append("Disa informacione janë të organizuara në seksione të hapshme ose të mbyllshme për lehtësi navigimi. ");
        }

        if (hasModal) {
            description.append("Disa veprime mund të hapin dritare modale ku përdoruesi plotëson, ndryshon ose konfirmon të dhëna shtesë. ");
        }
    }

    private String buildActionSentence(
            String buttonName,
            String actionType
    ) {

        String lower = buttonName.toLowerCase();

        if (actionType == null || actionType.isBlank()) {

            if (lower.contains("shto") || lower.contains("add")) {
                actionType = "ADD";
            } else if (lower.contains("modifiko")
                    || lower.contains("ndrysho")
                    || lower.contains("edit")) {
                actionType = "EDIT";
            } else if (lower.contains("fshi")
                    || lower.contains("delete")
                    || lower.contains("remove")) {
                actionType = "DELETE";
            } else if (lower.contains("kërko")
                    || lower.contains("kerko")
                    || lower.contains("search")) {
                actionType = "SEARCH";
            } else if (lower.contains("filtro")
                    || lower.contains("filter")) {
                actionType = "FILTER";
            } else if (lower.contains("ruaj")
                    || lower.contains("save")) {
                actionType = "SAVE";
            } else if (lower.contains("import")) {
                actionType = "IMPORT";
            } else if (lower.contains("eksport")
                    || lower.contains("export")
                    || lower.contains("shkarko")
                    || lower.contains("download")) {
                actionType = "EXPORT";
            } else {
                actionType = "";
            }
        }

        switch (actionType) {

            case "SEARCH":
                return "butoni \"" + buttonName + "\" shfaq rezultatet sipas kritereve të kërkimit";

            case "FILTER":
                return "butoni \"" + buttonName + "\" filtron të dhënat sipas kritereve të vendosura";

            case "SAVE":
                return "butoni \"" + buttonName + "\" ruan të dhënat ose ndryshimet e kryera";

            case "DELETE":
                return "butoni \"" + buttonName + "\" fshin ose çaktivizon të dhënën përkatëse";

            case "EDIT":
                return "butoni \"" + buttonName + "\" hap ndërfaqen për modifikimin e të dhënave";

            case "IMPORT":
                return "butoni \"" + buttonName + "\" importon të dhëna në sistem";

            case "EXPORT":
                return "butoni \"" + buttonName + "\" eksporton ose shkarkon të dhënat";

            case "VERIFY":
                return "butoni \"" + buttonName + "\" verifikon informacionin e vendosur";

            case "SEND":
                return "butoni \"" + buttonName + "\" dërgon të dhënat ose kodin për përpunim";

            case "ADD":
                return "butoni \"" + buttonName + "\" shton të dhëna të reja në sistem";

            default:
                return "butoni \"" + buttonName + "\" ekzekuton veprimin përkatës në sistem";
        }
    }

    private String getElementDisplayName(ElementInfo element) {

        String label = clean(element.getLabel());

        if (isValidValue(label)) {
            return label;
        }

        String text = clean(element.getText());

        if (isValidValue(text)) {
            return text;
        }

        String placeholder = clean(element.getPlaceholder());

        if (isValidValue(placeholder)) {
            return placeholder;
        }

        String name = clean(element.getName());

        if (isValidValue(name)) {
            return makeReadableName(name);
        }

        return "";
    }

    private boolean isValidValue(String value) {

        if (value == null || value.isBlank()) {
            return false;
        }

        value = clean(value);
        String lower = value.toLowerCase();

        if (value.length() < 2 || value.length() > 120) {
            return false;
        }

        if (value.matches("\\d+")) {
            return false;
        }

        if (value.matches(".*\\d{4,}.*")) {
            return false;
        }

        if (isPaginationText(lower)) {
            return false;
        }

        if (lower.contains("tabela përmban")
                || lower.contains("sidebar")
                || lower.contains("menu")
                || lower.contains("placeholder")
                || lower.contains("example")
                || lower.contains("enter")
                || lower.contains("enter code")
                || lower.contains("fromtodate")
                || lower.contains("from to date")
                || lower.contains("totodate")
                || lower.contains("javascript")
                || lower.contains("pagination")
                || lower.contains("rows per page")
                || lower.contains("items per page")
                || lower.contains("informacion mbi funksionimin")) {
            return false;
        }

        if (lower.contains("999")
                || lower.contains("000")) {
            return false;
        }

        if (lower.equals("elementi")
                || lower.equals("kërko")
                || lower.equals("kerko")
                || lower.equals("zgjidh")
                || lower.equals("--zgjidhni--")
                || lower.equals("ok")
                || lower.equals("yes")
                || lower.equals("no")
                || lower.equals("x")) {
            return false;
        }

        return true;
    }

    private boolean looksLikeNavigationOrList(String text) {

        if (text == null || text.isBlank()) {
            return false;
        }

        String lower = text.toLowerCase();

        int count = 0;

        String[] indicators = {
                "universiteti",
                "akademia",
                "kolegji",
                "shkolla e lartë",
                "shkolla e larte",
                "studentët",
                "studentet",
                "konfigurime",
                "statistika",
                "dashboard",
                "departamentet",
                "struktura",
                "western balkans",
                "albanian university",
                "epitech"
        };

        for (String indicator : indicators) {
            if (lower.contains(indicator)) {
                count++;
            }
        }

        return count >= 4;
    }

    private boolean isPaginationText(String lower) {

        if (lower == null) {
            return false;
        }

        return lower.contains("e para")
                || lower.contains("e fundit")
                || lower.contains("tjetra")
                || lower.contains("e kaluara");
    }

    private String collectText(List<ElementInfo> elements) {

        StringBuilder builder = new StringBuilder();

        for (ElementInfo element : elements) {
            builder.append(safe(element.getText())).append(" ");
            builder.append(safe(element.getPlaceholder())).append(" ");
            builder.append(safe(element.getLabel())).append(" ");
            builder.append(safe(element.getHelpText())).append(" ");
        }

        return builder.toString();
    }

    private String joinLimited(Set<String> values, int limit) {

        StringBuilder builder = new StringBuilder();

        int count = 0;

        for (String value : values) {

            if (count >= limit) {
                break;
            }

            if (builder.length() > 0) {
                builder.append(", ");
            }

            builder.append(value);
            count++;
        }

        return builder.toString();
    }

    private String makeReadableName(String value) {

        if (value == null) {
            return "";
        }

        return value
                .replaceAll("([a-z])([A-Z])", "$1 $2")
                .replace("_", " ")
                .replace("-", " ")
                .trim();
    }

    private String cleanOutput(String value) {

        if (value == null) {
            return "";
        }

        return value
                .replaceAll("[ \\t]+", " ")
                .replace(" .", ".")
                .replace(" ,", ",")
                .trim();
    }

    private String clean(String value) {

        return value == null
                ? ""
                : value.replace("\n", " ")
                .replace("\r", " ")
                .replaceAll("[ \\t]+", " ")
                .trim();
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}