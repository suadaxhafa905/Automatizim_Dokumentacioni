package org.example.generator;

import org.example.model.ElementInfo;
import org.example.model.PageInfo;
import org.example.model.PageType;
import org.example.utils.PageTitleResolver;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/*

Krijon:

përmbledhjen automatike të faqes
një paragraf të shkurtër informues

bazuar në strukturën dhe elementët e saj.

Gjeneron një përmbledhje të shkurtër për secilën faqe duke përshkruar qëllimin dhe përdorimin e saj.
 */


public class PageSummaryGenerator {

    public String generate(PageInfo pageInfo, PageType pageType) {

        List<ElementInfo> elements = pageInfo.getElements();

        String automaticSummary =
                buildDetailedAutomaticSummary(pageInfo, pageType, elements);

        String helpText =
                formatHelpText(pageInfo.getHelpSectionText());

        if (isValidHelpSectionText(helpText)) {
            automaticSummary +=
                    "\n\nInformacion mbi funksionimin e faqes:\n"
                            + helpText;
        }

        return cleanOutput(automaticSummary);
    }

    private String buildDetailedAutomaticSummary(
            PageInfo pageInfo,
            PageType pageType,
            List<ElementInfo> elements
    ) {

        String pageName = PageTitleResolver.resolve(pageInfo);

        Set<String> fields = collectFields(elements);
        Set<String> requiredFields = collectRequiredFields(elements);
        Set<String> actions = collectActions(elements);
        Set<String> sections = collectSections(elements);
        Set<String> tableColumns = collectTableColumns(elements);

        boolean hasTable = hasTag(elements, "table");
        boolean hasModal = hasContainer(elements, "MODAL");
        boolean hasTabs = hasContainer(elements, "TAB");

        StringBuilder summary = new StringBuilder();

        summary.append("Faqja \"")
                .append(pageName)
                .append("\" ");

        summary.append(buildPagePurpose(pageType, pageName, elements));

        if (!sections.isEmpty()) {
            summary.append("Faqja është e organizuar në seksione funksionale si ")
                    .append(joinLimited(sections, 4))
                    .append(", duke ndihmuar përdoruesin të orientohet gjatë përdorimit të saj. ");
        }

        if (!fields.isEmpty()) {
            summary.append("Fushat kryesore të faqes përfshijnë ")
                    .append(joinLimited(fields, 6))
                    .append(". ");
        }

        if (!requiredFields.isEmpty()) {
            summary.append("Fushat ")
                    .append(joinLimited(requiredFields, 5))
                    .append(" janë të identifikuara si të detyrueshme për plotësim. ");
        }

        if (!actions.isEmpty()) {
            summary.append("Në këtë faqe mund të kryhen veprime si ")
                    .append(joinLimited(actions, 5))
                    .append(". ");
        }

        if (hasTable) {
            if (!tableColumns.isEmpty()) {
                summary.append("Të dhënat paraqiten në tabelë me kolona si ")
                        .append(joinLimited(tableColumns, 6))
                        .append(". ");
            } else {
                summary.append("Të dhënat paraqiten në formë tabelare për të lehtësuar kontrollin dhe administrimin e informacionit. ");
            }
        }

        if (hasTabs) {
            summary.append("Informacioni në faqe është i ndarë në tab-e, duke e bërë më të lehtë navigimin ndërmjet kategorive të të dhënave. ");
        }

        if (hasModal) {
            summary.append("Disa veprime mund të hapin dritare modale për plotësimin, konfirmimin ose përditësimin e informacionit. ");
        }

        return cleanOutput(summary.toString());
    }

    private String buildPagePurpose(
            PageType pageType,
            String pageName,
            List<ElementInfo> elements
    ) {

        String allText = collectAllText(elements).toLowerCase();
        String lowerPageName = pageName.toLowerCase();

        if (pageType == PageType.DASHBOARD) {
            return "paraqet një pamje përmbledhëse me tregues, statistika dhe informacione monitoruese të sistemit. ";
        }

        if (pageType == PageType.STATISTICS_PAGE) {
            return "përdoret për analizimin dhe paraqitjen e statistikave, raporteve dhe treguesve të sistemit. ";
        }

        if (pageType == PageType.WORKFLOW_PAGE) {
            return "përdoret për administrimin e proceseve, statuseve dhe rrjedhave të punës në sistem. ";
        }

        if (pageType == PageType.FORM_PAGE) {
            return "mundëson plotësimin, regjistrimin dhe ruajtjen e të dhënave përkatëse në sistem. ";
        }

        if (pageType == PageType.TABLE_PAGE) {
            return "shfaq të dhëna në formë tabelare dhe mundëson administrimin e informacionit të regjistruar në sistem. ";
        }

        if (pageType == PageType.SEARCH_PAGE) {

            if (allText.contains("student")) {
                return "përdoret për kërkimin, filtrimin dhe administrimin e informacionit të studentëve në sistem. ";
            }

            if (allText.contains("përdorues")
                    || allText.contains("perdorues")
                    || allText.contains("rol")) {
                return "përdoret për kërkimin dhe administrimin e përdoruesve, roleve ose të drejtave të aksesit në sistem. ";
            }

            if (allText.contains("departament")
                    || allText.contains("struktur")) {
                return "përdoret për kërkimin dhe administrimin e strukturave, njësive organizative ose departamenteve. ";
            }

            return "mundëson kërkimin, filtrimin dhe konsultimin e të dhënave në sistem. ";
        }

        if (allText.contains("raport")
                || allText.contains("shkarko")) {
            return "përdoret për gjenerimin, filtrimin dhe shkarkimin e raporteve ose të dhënave të sistemit. ";
        }

        if (allText.contains("student")) {
            return "përdoret për administrimin, kërkimin dhe konsultimin e të dhënave të studentëve. ";
        }

        if (allText.contains("nim")
                && lowerPageName.contains("nim")) {
            return "përdoret për administrimin, gjenerimin ose verifikimin e numrit të matrikullimit. ";
        }

        return "ofron funksionalitete për ndërveprim me të dhënat dhe proceset e sistemit. ";
    }

    private String formatHelpText(String text) {

        if (text == null || text.isBlank()) {
            return "";
        }

        String result = text.trim();

        result = result.replaceFirst(
                "(?i)^informacion mbi funksionimin( e faqes)?\\s*:?\\s*",
                ""
        ).trim();

        result = result.replaceAll(
                "(?i)\\binformacion mbi funksionimin( e faqes)?\\b\\s*:?",
                ""
        ).trim();

        result = result.replaceAll("(?i)2026©.*", "");

        result = result.replace("•", "\n• ");

        result = result.replaceAll("[ \\t]+", " ")
                .replaceAll("\\n[ \\t]+", "\\n")
                .replaceAll("\\n{3,}", "\\n\\n")
                .trim();

        return result;
    }

    private boolean isValidHelpSectionText(String text) {

        if (text == null || text.isBlank()) {
            return false;
        }

        String lower = text.toLowerCase();

        if (text.length() < 60) {
            return false;
        }

        if (text.length() > 3000) {
            return false;
        }

        if (lower.contains("soft & solution")
                || lower.contains("copyright")
                || lower.contains("dashboard")
                || lower.contains("konfigurime statistika")) {
            return false;
        }

        if (looksLikeUniversityList(text)) {
            return false;
        }

        return lower.contains("në këtë faqe")
                || lower.contains("ne kete faqe")
                || lower.contains("ju mund")
                || lower.contains("mund të")
                || lower.contains("mund te")
                || lower.contains("klikoni")
                || lower.contains("shtoni")
                || lower.contains("modifikoni")
                || lower.contains("fshini")
                || lower.contains("kërkoni")
                || lower.contains("kerkoni")
                || lower.contains("shikoni")
                || lower.contains("përdorni")
                || lower.contains("perdorni");
    }

    private boolean looksLikeUniversityList(String text) {

        String lower = text.toLowerCase();

        int count = 0;

        String[] indicators = {
                "universiteti",
                "akademia",
                "kolegji",
                "shkolla e lartë",
                "shkolla e larte",
                "institucioni privat",
                "epitech",
                "albanian university",
                "western balkans",
                "politeknik",
                "luarasi",
                "barleti"
        };

        for (String indicator : indicators) {
            if (lower.contains(indicator)) {
                count++;
            }
        }

        return count >= 4
                && !lower.contains("në këtë faqe")
                && !lower.contains("ne kete faqe")
                && !lower.contains("ju mund")
                && !lower.contains("mund të")
                && !lower.contains("mund te");
    }

    private Set<String> collectFields(List<ElementInfo> elements) {

        Set<String> fields = new LinkedHashSet<>();

        for (ElementInfo element : elements) {

            String tag = clean(element.getTagName()).toLowerCase();
            String type = clean(element.getType()).toLowerCase();

            if (!tag.equals("input")
                    && !tag.equals("textarea")
                    && !tag.equals("select")) {
                continue;
            }

            if (type.equals("hidden")
                    || type.equals("button")
                    || type.equals("submit")
                    || type.equals("reset")) {
                continue;
            }
/*
            String value = getElementDisplayName(element);

            if (isValidValue(value)) {
                fields.add(value);
            }

 */

            String value = getElementDisplayName(element);

            if (isValidValue(value)
                    && !isBadFieldName(value)) {

                fields.add(value);
            }
        }

        return fields;
    }

    private Set<String> collectRequiredFields(List<ElementInfo> elements) {

        Set<String> fields = new LinkedHashSet<>();

        for (ElementInfo element : elements) {

            if (!element.isRequired()) {
                continue;
            }

            String value = getElementDisplayName(element);

            if (isValidValue(value)) {
                fields.add(value);
            }
        }

        return fields;
    }

    private Set<String> collectActions(List<ElementInfo> elements) {

        Set<String> actions = new LinkedHashSet<>();

        for (ElementInfo element : elements) {

            if (!isActionElement(element)) {
                continue;
            }

            String actionType = clean(element.getActionType());
            String name = getElementDisplayName(element);

            if (!isValidValue(name)) {
                continue;
            }

            String lower = name.toLowerCase();

            if (lower.contains("e para")
                    || lower.contains("e fundit")
                    || lower.contains("tjetra")
                    || lower.contains("e kaluara")) {
                continue;
            }

            actions.add(buildActionPhrase(name, actionType));
        }

        return actions;
    }

    private boolean isActionElement(ElementInfo element) {

        String tag = clean(element.getTagName()).toLowerCase();
        String type = clean(element.getType()).toLowerCase();

        if (tag.equals("button") || tag.equals("a")) {
            return true;
        }

        return tag.equals("input")
                && (type.equals("button")
                || type.equals("submit")
                || type.equals("reset"));
    }

    private Set<String> collectSections(List<ElementInfo> elements) {

        Set<String> sections = new LinkedHashSet<>();

        for (ElementInfo element : elements) {

            String section = clean(element.getSection());

            if (isValidValue(section)
                    && !looksLikeUniversityList(section)) {
                sections.add(section);
            }
        }

        return sections;
    }

    private Set<String> collectTableColumns(List<ElementInfo> elements) {

        Set<String> columns = new LinkedHashSet<>();

        for (ElementInfo element : elements) {

            if (!"table".equalsIgnoreCase(element.getTagName())) {
                continue;
            }

            if (element.getTableColumns() == null) {
                continue;
            }

            for (String column : element.getTableColumns()) {
                if (isValidValue(column)) {
                    columns.add(column);
                }
            }
        }

        return columns;
    }

    private String buildActionPhrase(String name, String actionType) {

        if (actionType == null || actionType.isBlank()) {
            return "ekzekutimin e veprimit \"" + name + "\"";
        }

        switch (actionType) {

            case "SEARCH":
                return "kërkimin e të dhënave";

            case "FILTER":
                return "filtrimin e rezultateve";

            case "SAVE":
                return "ruajtjen e ndryshimeve";

            case "DELETE":
                return "fshirjen ose çaktivizimin e të dhënave";

            case "EDIT":
                return "modifikimin e të dhënave";

            case "IMPORT":
                return "importimin e të dhënave";

            case "EXPORT":
                return "eksportimin ose shkarkimin e të dhënave";

            case "VERIFY":
                return "verifikimin e informacionit";

            case "SEND":
                return "dërgimin e të dhënave";

            case "ADD":
                return "shtimin e të dhënave";

            default:
                return "ekzekutimin e veprimit \"" + name + "\"";
        }
    }

    private String getElementDisplayName(ElementInfo element) {

        /*
        String label = clean(element.getLabel());

        if (isValidValue(label)) {
            return label;
        }

         */

        String label = clean(element.getLabel());

        if (isValidValue(label)
                && !isBadFieldName(label)) {

            return label.replace(":", "").trim();
        }
/*
        String text = clean(element.getText());

        if (isValidValue(text)) {
            return text;
        }

 */

        String text = clean(element.getText());

        if (isValidValue(text)
                && !isBadFieldName(text)) {

            return text.replace(":", "").trim();
        }
/*
        String placeholder = clean(element.getPlaceholder());

        if (isValidValue(placeholder)) {
            return placeholder;
        }

 */

        String placeholder = clean(element.getPlaceholder());

        if (isValidValue(placeholder)
                && !isBadFieldName(placeholder)) {

            return placeholder.replace(":", "").trim();
        }


/*
        String name = clean(element.getName());

        if (isValidValue(name)) {
            return makeReadableName(name);
        }

 */

        String name = clean(element.getName());

        if (isValidValue(name)
                && !isBadFieldName(name)) {

            return makeReadableName(name);
        }

        return "";
    }

    private boolean hasTag(List<ElementInfo> elements, String tagName) {

        for (ElementInfo element : elements) {
            if (tagName.equalsIgnoreCase(element.getTagName())) {
                return true;
            }
        }

        return false;
    }

    private boolean hasContainer(List<ElementInfo> elements, String containerType) {

        for (ElementInfo element : elements) {
            if (containerType.equalsIgnoreCase(element.getContainerType())) {
                return true;
            }
        }

        return false;
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

        if (lower.endsWith("...")) {
            return false;
        }

        return true;
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

    private String collectAllText(List<ElementInfo> elements) {

        StringBuilder builder = new StringBuilder();

        for (ElementInfo element : elements) {
            builder.append(safe(element.getText())).append(" ");
            builder.append(safe(element.getPlaceholder())).append(" ");
            builder.append(safe(element.getLabel())).append(" ");
            builder.append(safe(element.getHelpText())).append(" ");
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

    private String cleanOutput(String text) {

        if (text == null) {
            return "";
        }

        return text
                .replaceAll("[ \\t]+", " ")
                .replaceAll("\\n{3,}", "\n\n")
                .replace(" .", ".")
                .replace(" ,", ",")
                .trim();
    }

    private String clean(String text) {

        return text == null
                ? ""
                : text.replace("\n", " ")
                .replace("\r", " ")
                .replaceAll("[ \\t]+", " ")
                .trim();
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean isBadFieldName(String value) {

        if (value == null) {
            return true;
        }

        String lower = value.toLowerCase().trim();

        return lower.contains("time select")
                || lower.contains("data aplications")
                || lower.contains("date applications")
                || lower.contains("search")
                || lower.equals("select")
                || lower.equals("dropdown")
                || lower.equals("input");
    }
}