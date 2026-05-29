package org.example.generator;

import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.*;
import org.example.model.ElementInfo;
import org.example.model.ModalInfo;
import org.example.model.PageActionInfo;
import org.example.model.PageInfo;
import org.example.model.PageType;
import org.example.utils.PageTitleResolver;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.example.model.PageInteractionInfo;

/*
Gjeneron manualin final .docx:

kapitujt
screenshot-et
figurat
përshkrimet
udhëzimet e përdorimit

Është klasa kryesore e dokumentimit.

Gjeneron manualin e përdorimit në format Word. Shton:

titujt e kapitujve
screenshot-et
përshkrimet funksionale
udhëzimet e përdorimit
modals dhe veprimet funksionale.
 */
public class ManualGenerator {

    private final SmartSentenceGenerator sentenceGenerator =
            new SmartSentenceGenerator();



    public void generateManualForPages(List<PageInfo> pages, String outputPath) throws Exception {

        XWPFDocument document = new XWPFDocument();

        addTitle(document, "Manual Përdorimi");
        addParagraph(document, "Dokument i gjeneruar automatikisht për faqet dhe funksionalitetet e sistemit.");

        int pageCounter = 1;
        int figureCounter = 1;

        for (PageInfo pageInfo : pages) {

            String pageTitle = PageTitleResolver.resolve(pageInfo);

            PageClassifier classifier = new PageClassifier();
            PageType pageType = classifier.classify(pageInfo);

            PageSummaryGenerator summaryGenerator = new PageSummaryGenerator();
            FunctionalDescriptionGenerator functionalGenerator = new FunctionalDescriptionGenerator();

            addHeading(document, pageCounter + ". " + pageTitle);
            addHeading(document, pageCounter + ".1 Përshkrimi i faqes");

            addParagraph(document, summaryGenerator.generate(pageInfo, pageType));

           // addImage(document, pageInfo.getScreenshotPath());
           // addFigureCaption(document, figureCounter++, pageTitle);

            addImage(document, pageInfo.getScreenshotPath());
            addFigureCaption(document, figureCounter++, pageTitle);
            addFigureDescription(document, pageTitle);

            addHeading(document, pageCounter + ".2 Përshkrimi i funksionaliteteve");
            addParagraph(document, functionalGenerator.generate(pageInfo));

            addStructuredUsageInstructions(document, pageInfo.getElements());

            int sectionCounter = 3;

            /*
            if (hasValidPageActions(pageInfo)) {
                addPageActionsSection(document, pageInfo, pageCounter, sectionCounter);
                sectionCounter++;
            }

            if (pageInfo.getModals() != null && !pageInfo.getModals().isEmpty()) {
                addModalSections(document, pageInfo, pageCounter, sectionCounter);
            }

             */
            if (hasValidPageActions(pageInfo)) {
                addPageActionsSection(document, pageInfo, pageCounter, sectionCounter);
                sectionCounter++;
            }

            if (pageInfo.getInteractions() != null
                    && !pageInfo.getInteractions().isEmpty()) {

                addHeading(
                        document,
                        pageCounter + "." + sectionCounter + " Ndërveprimet me elementët e faqes"
                );

                addParagraph(
                        document,
                        "Sistemi identifikon dhe ekzekuton automatikisht ndërveprimet funksionale të elementëve të faqes."
                );

/*
                for (PageInteractionInfo interaction : pageInfo.getInteractions()) {

                    addParagraph(
                            document,
                            "- "
                                    + interaction.getDescription()
                                    + " "
                                    + interaction.getResultDescription()
                    );

                    if (interaction.getScreenshotPath() != null
                            && !interaction.getScreenshotPath().isBlank()) {

                        addImage(document, interaction.getScreenshotPath());
                    }
                }

 */

                for (PageInteractionInfo interaction : pageInfo.getInteractions()) {

                    if ("DETAIL_BUTTON".equalsIgnoreCase(interaction.getElementType())) {
                        addDetailButtonInteraction(document, interaction);
                        continue;
                    }

                    addParagraph(
                            document,
                            "- "
                                    + interaction.getDescription()
                                    + " "
                                    + interaction.getResultDescription()
                    );

                    /*
                    if (interaction.getScreenshotPath() != null
                            && !interaction.getScreenshotPath().isBlank()) {

                        addImage(document, interaction.getScreenshotPath());
                    }

                     */

                    if (interaction.getScreenshotPath() != null
                            && !interaction.getScreenshotPath().isBlank()) {

                        addImage(document, interaction.getScreenshotPath());

                        addFigureCaption(
                                document,
                                figureCounter++,
                                buildInteractionFigureTitle(interaction)
                        );

                        addFigureDescription(
                                document,
                                buildInteractionFigureTitle(interaction)
                        );
                    }


                }


                sectionCounter++;
            }

            if (pageInfo.getModals() != null && !pageInfo.getModals().isEmpty()) {
                figureCounter = addModalSections(
                        document,
                        pageInfo,
                        pageCounter,
                        sectionCounter,
                        figureCounter
                );
            }


            pageCounter++;
        }

        FileOutputStream out = new FileOutputStream(outputPath);
        document.write(out);
        out.close();
        document.close();
    }

    private int addModalSections(
            XWPFDocument document,
            PageInfo pageInfo,
            int pageCounter,
            int sectionCounter,
            int figureCounter
    ) {

        if (pageInfo.getModals() == null || pageInfo.getModals().isEmpty()) {
            return figureCounter;
        }

        addHeading(document, pageCounter + "." + sectionCounter + " Dritaret modale të faqes");

        int modalCounter = 1;

        for (ModalInfo modal : pageInfo.getModals()) {

            String modalTitle = cleanTitle(modal.getModalTitle());

            if (modalTitle.isBlank()) {
                modalTitle = "Dritare modale";
            }

            addSubHeading(
                    document,
                    pageCounter + "." + sectionCounter + "." + modalCounter + " " + modalTitle
            );

            if (modal.getOpenedByButton() != null
                    && !modal.getOpenedByButton().trim().isEmpty()) {

                addParagraph(
                        document,
                        "Kjo dritare modale hapet pas klikimit të butonit \""
                                + cleanTitle(modal.getOpenedByButton())
                                + "\"."
                );
            }

            if (modal.getScreenshotPath() != null
                    && !modal.getScreenshotPath().trim().isEmpty()) {

                addImage(document, modal.getScreenshotPath());

                addFigureCaption(
                        document,
                        figureCounter++,
                        "Dritare modale - " + modalTitle
                );

                addFigureDescription(
                        document,
                        "Dritarja modale " + modalTitle
                );
            }

            if (modal.getElements() != null && !modal.getElements().isEmpty()) {
                addParagraph(
                        document,
                        "Elementet dhe veprimet kryesore të kësaj dritareje modale janë:"
                );

                addStructuredUsageInstructions(document, modal.getElements());
            }

            modalCounter++;
        }

        return figureCounter;
    }

    private boolean hasValidPageActions(PageInfo pageInfo) {

        if (pageInfo.getPageActions() == null || pageInfo.getPageActions().isEmpty()) {
            return false;
        }

        for (PageActionInfo action : pageInfo.getPageActions()) {
            if (isValidPageAction(action)) {
                return true;
            }
        }

        return false;
    }

    private void addPageActionsSection(XWPFDocument document, PageInfo pageInfo, int pageCounter, int sectionCounter) {

        if (!hasValidPageActions(pageInfo)) {
            return;
        }

        addHeading(document, pageCounter + "." + sectionCounter + " Veprime funksionale të faqes");
        addParagraph(document, "Faqja përmban veprimet funksionale të mëposhtme:");

        Set<String> added = new LinkedHashSet<>();

        for (PageActionInfo action : pageInfo.getPageActions()) {

            if (!isValidPageAction(action)) {
                continue;
            }

            String description = cleanTitle(action.getDescription());

            if (!added.add(description.toLowerCase())) {
                continue;
            }

            addParagraph(document, "- " + description);
        }
    }

    private boolean isValidPageAction(PageActionInfo action) {

        if (action == null) {
            return false;
        }

        String name = cleanTitle(action.getActionName());
        String description = cleanTitle(action.getDescription());

        if (description.isBlank()) {
            return false;
        }

        if (!isValidInstructionValue(name)) {
            return false;
        }

        if (isTableColumnName(name)) {
            return false;
        }

        return isValidButtonInstruction(name);
    }

    private void addTitle(XWPFDocument document, String text) {

        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setAlignment(ParagraphAlignment.CENTER);

        XWPFRun run = paragraph.createRun();
        run.setBold(true);
        run.setFontSize(18);
        run.setText(text);
    }

    private void addHeading(XWPFDocument document, String text) {

        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setSpacingBefore(250);
        paragraph.setSpacingAfter(120);

        XWPFRun run = paragraph.createRun();
        run.setBold(true);
        run.setFontSize(14);
        run.setText(text);
    }

    private void addSubHeading(XWPFDocument document, String text) {

        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setSpacingBefore(120);
        paragraph.setSpacingAfter(60);

        XWPFRun run = paragraph.createRun();
        run.setBold(true);
        run.setFontSize(11);
        run.setText(text);
    }

    private void addParagraph(XWPFDocument document, String text) {

        if (text == null || text.trim().isEmpty()) {
            return;
        }

        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setSpacingAfter(120);

        XWPFRun run = paragraph.createRun();
        run.setFontSize(11);

        String[] lines = text.split("\n");

        for (int i = 0; i < lines.length; i++) {
            run.setText(lines[i]);

            if (i < lines.length - 1) {
                run.addBreak();
            }
        }
    }

    private void addImage(XWPFDocument document, String imagePath) {

        if (imagePath == null || imagePath.trim().isEmpty()) {
            return;
        }

        try {

            File imageFile = new File(imagePath);

            if (!imageFile.exists()) {
                return;
            }

            BufferedImage image = ImageIO.read(imageFile);

            if (image == null) {
                return;
            }

            int originalWidth = image.getWidth();
            int originalHeight = image.getHeight();

            int maxWidth = 430;
            int maxHeight = 600;

            double widthRatio = (double) maxWidth / originalWidth;
            double heightRatio = (double) maxHeight / originalHeight;

            double ratio = Math.min(widthRatio, heightRatio);
            ratio = Math.min(ratio, 1.0);

            int finalWidth = (int) (originalWidth * ratio);
            int finalHeight = (int) (originalHeight * ratio);

            XWPFParagraph paragraph = document.createParagraph();
            paragraph.setAlignment(ParagraphAlignment.CENTER);
            paragraph.setSpacingBefore(120);
            paragraph.setSpacingAfter(80);

            XWPFRun run = paragraph.createRun();

            try (FileInputStream fis = new FileInputStream(imageFile)) {
                run.addPicture(
                        fis,
                        getPictureType(imageFile.getName()),
                        imageFile.getName(),
                        Units.toEMU(finalWidth),
                        Units.toEMU(finalHeight)
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getPictureType(String fileName) {

        String lowerName = fileName.toLowerCase();

        if (lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg")) {
            return XWPFDocument.PICTURE_TYPE_JPEG;
        }

        if (lowerName.endsWith(".gif")) {
            return XWPFDocument.PICTURE_TYPE_GIF;
        }

        if (lowerName.endsWith(".bmp")) {
            return XWPFDocument.PICTURE_TYPE_BMP;
        }

        return XWPFDocument.PICTURE_TYPE_PNG;
    }

    private void addFigureCaption(XWPFDocument document, int figureNumber, String title) {

        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setAlignment(ParagraphAlignment.CENTER);
        paragraph.setSpacingAfter(180);

        XWPFRun run = paragraph.createRun();
        run.setItalic(true);
        run.setFontSize(10);
        run.setText("Figura " + figureNumber + ". " + cleanTitle(title));
    }

    private void addStructuredUsageInstructions(XWPFDocument document, List<ElementInfo> elements) {

        boolean hasInputs = hasInputs(elements);
        boolean hasSelects = hasSelects(elements);
        boolean hasButtons = hasButtons(elements);

        if (!hasInputs && !hasSelects && !hasButtons) {
            return;
        }

        addParagraph(document, "Përdoruesi mund të kryejë veprimet e mëposhtme:");

        if (hasInputs) {
            addSubHeading(document, "Plotësimi i fushave");
            addInputInstructions(document, elements);
        }

        if (hasSelects) {
            addSubHeading(document, "Përzgjedhja e vlerave");
            addSelectInstructions(document, elements);
        }

        if (hasButtons) {
            addSubHeading(document, "Kryerja e veprimeve");
            addButtonInstructions(document, elements);
        }
    }

    private void addInputInstructions(XWPFDocument document, List<ElementInfo> elements) {

        Set<String> added = new LinkedHashSet<>();

        for (ElementInfo element : elements) {

            String tag = safe(element.getTagName());
            String type = safe(element.getType());

            if (!"input".equalsIgnoreCase(tag) && !"textarea".equalsIgnoreCase(tag)) {
                continue;
            }

            if ("hidden".equalsIgnoreCase(type)
                    || "button".equalsIgnoreCase(type)
                    || "submit".equalsIgnoreCase(type)
                    || "reset".equalsIgnoreCase(type)) {
                continue;
            }

            String value = getBestValue(element);

            if (!isValidInstructionValue(value) || isTableColumnName(value)) {
                continue;
            }

            if (!added.add(value.toLowerCase())) {
                continue;
            }

            if ("password".equalsIgnoreCase(type)) {
                addParagraph(document, "- Plotësoni fushën e fjalëkalimit me kredencialin përkatës.");
            } else {
                addParagraph(
                        document,
                        "- Plotësoni fushën \""
                                + value
                                + "\" për të vendosur ose filtruar informacionin përkatës."
                );
            }
        }
    }

    private void addSelectInstructions(XWPFDocument document, List<ElementInfo> elements) {

        Set<String> added = new LinkedHashSet<>();

        for (ElementInfo element : elements) {

            if (!"select".equalsIgnoreCase(element.getTagName())) {
                continue;
            }

            String value = cleanSelectValue(getBestValue(element));

            if (!isValidInstructionValue(value) || isTableColumnName(value)) {
                continue;
            }

            if (!added.add(value.toLowerCase())) {
                continue;
            }

            addParagraph(
                    document,
                    "- Zgjidhni një vlerë nga lista \""
                            + value
                            + "\" për të përcaktuar kriterin përkatës."
            );
        }
    }

    private void addButtonInstructions(XWPFDocument document, List<ElementInfo> elements) {

        Set<String> added = new LinkedHashSet<>();

        for (ElementInfo element : elements) {

            if (!isRealButton(element)) {
                continue;
            }

            String value = getBestValue(element);

            if (!isValidInstructionValue(value)) {
                continue;
            }

            if (isTableColumnName(value)) {
                continue;
            }

            if (!isValidButtonInstruction(value)) {
                continue;
            }

            if (!added.add(value.toLowerCase())) {
                continue;
            }

            addParagraph(document, "- " + buildButtonAction(value));
        }
    }

    private String buildButtonAction(String value) {

        String lower = value.toLowerCase().trim();

        if (lower.contains("kërko") || lower.contains("kerko") || lower.contains("search")) {
            return "Klikoni butonin \"" + value + "\" për të shfaqur rezultatet sipas kritereve të kërkimit.";
        }

        if (lower.contains("filtro") || lower.contains("filter")) {
            return "Klikoni butonin \"" + value + "\" për të filtruar të dhënat e shfaqura.";
        }

        if (lower.contains("shto") || lower.contains("add")) {
            String object = extractObjectName(value);
            return "Klikoni butonin \"" + value + "\" për të shtuar " + object + " në sistem.";
        }

        if (lower.contains("ruaj") || lower.contains("save")) {
            return "Klikoni butonin \"" + value + "\" për të ruajtur ndryshimet e kryera.";
        }

        if (lower.contains("pastro") || lower.contains("clear") || lower.contains("reset")) {
            return "Klikoni butonin \"" + value + "\" për të pastruar filtrat ose vlerat e vendosura.";
        }

        if (lower.contains("eksporto") || lower.contains("export")) {
            return "Klikoni butonin \"" + value + "\" për të eksportuar të dhënat e faqes.";
        }

        if (lower.contains("importo") || lower.contains("import")) {
            return "Klikoni butonin \"" + value + "\" për të importuar të dhëna në sistem.";
        }

        if (lower.contains("gjenero") || lower.contains("generate")) {
            return "Klikoni butonin \"" + value + "\" për të gjeneruar informacionin ose kodin përkatës.";
        }

        if (lower.contains("fshi") || lower.contains("delete") || lower.contains("remove")) {
            return "Klikoni butonin \"" + value + "\" për të fshirë të dhënën përkatëse.";
        }

        if (lower.contains("shkarko") || lower.contains("download")) {
            return "Klikoni butonin \"" + value + "\" për të shkarkuar raportin me të dhënat e shfaqura.";
        }

        if (lower.contains("modifiko") || lower.contains("ndrysho") || lower.contains("edit")) {
            return "Klikoni butonin \"" + value + "\" për të modifikuar të dhënat ekzistuese.";
        }

        if (lower.contains("verifiko") || lower.contains("verify")) {
            return "Klikoni butonin \"" + value + "\" për të verifikuar informacionin e vendosur.";
        }

        if (lower.contains("dërgo") || lower.contains("dergo") || lower.contains("send")) {
            return "Klikoni butonin \"" + value + "\" për të dërguar të dhënat ose kodin për përpunim.";
        }

        if (lower.contains("aktivizo") || lower.contains("activate") || lower.contains("riaktivizo")) {
            return "Klikoni butonin \"" + value + "\" për të aktivizuar funksionalitetin ose përdoruesin përkatës.";
        }

        if (lower.contains("çaktivizo")
                || lower.contains("caktivizo")
                || lower.contains("joaktiv")
                || lower.contains("deactivate")) {
            return "Klikoni butonin \"" + value + "\" për të çaktivizuar funksionalitetin ose përdoruesin përkatës.";
        }

        return "Klikoni butonin \"" + value + "\" për të ekzekutuar funksionalitetin përkatës në sistem.";
    }

    private boolean hasInputs(List<ElementInfo> elements) {

        for (ElementInfo element : elements) {

            String tag = safe(element.getTagName());
            String type = safe(element.getType());
            String value = getBestValue(element);

            if (("input".equalsIgnoreCase(tag) || "textarea".equalsIgnoreCase(tag))
                    && !"hidden".equalsIgnoreCase(type)
                    && !"button".equalsIgnoreCase(type)
                    && !"submit".equalsIgnoreCase(type)
                    && !"reset".equalsIgnoreCase(type)
                    && isValidInstructionValue(value)
                    && !isTableColumnName(value)) {
                return true;
            }
        }

        return false;
    }

    private boolean hasSelects(List<ElementInfo> elements) {

        for (ElementInfo element : elements) {

            String value = getBestValue(element);

            if ("select".equalsIgnoreCase(element.getTagName())
                    && isValidInstructionValue(value)
                    && !isTableColumnName(value)) {
                return true;
            }
        }

        return false;
    }

    private boolean hasButtons(List<ElementInfo> elements) {

        for (ElementInfo element : elements) {

            String value = getBestValue(element);

            if (isRealButton(element)
                    && isValidInstructionValue(value)
                    && !isTableColumnName(value)
                    && isValidButtonInstruction(value)) {
                return true;
            }
        }

        return false;
    }

    private boolean isRealButton(ElementInfo element) {

        String tag = safe(element.getTagName()).toLowerCase();
        String type = safe(element.getType()).toLowerCase();

        if ("button".equals(tag)) {
            return true;
        }

        if ("input".equals(tag)) {
            return "button".equals(type)
                    || "submit".equals(type)
                    || "reset".equals(type);
        }

        return "a".equals(tag);
    }

    private boolean isValidButtonInstruction(String value) {

        if (value == null || value.isBlank()) {
            return false;
        }

        String lower = value.toLowerCase().trim();

        if (isTableColumnName(value)) {
            return false;
        }

        return !(lower.endsWith(":")
                || lower.endsWith("...")
                || lower.contains("*")
                || lower.contains("zgjidh")
                || lower.contains("select")
                || lower.contains("choose")
                || lower.contains("pyetja")
                || lower.contains("cikli i studimit")
                || lower.contains("institucioni i arsimit")
                || lower.contains("programi i studimit")
                || lower.contains("kërko me")
                || lower.contains("kerko me")
                || lower.contains("data e fillimit")
                || lower.contains("data e përfundimit")
                || lower.contains("data e perfundimit")
                || lower.contains("emërtimi")
                || lower.contains("emertimi")
                || lower.contains("përshkrimi")
                || lower.contains("pershkrimi"));
    }

    private boolean isTableColumnName(String value) {

        if (value == null || value.isBlank()) {
            return true;
        }

        String lower = value.toLowerCase().trim();

        return lower.equals("nr")
                || lower.equals("roli")
                || lower.equals("përdoruesi")
                || lower.equals("perdoruesi")
                || lower.equals("emri")
                || lower.equals("atësi")
                || lower.equals("atesi")
                || lower.equals("mbiemri")
                || lower.equals("nid")
                || lower.equals("nim")
                || lower.equals("fakultetet")
                || lower.equals("krijuar më")
                || lower.equals("krijuar me")
                || lower.equals("data e fillimit")
                || lower.equals("data e përfundimit")
                || lower.equals("data e perfundimit")
                || lower.equals("statusi")
                || lower.equals("përshkrimi")
                || lower.equals("pershkrimi")
                || lower.equals("gjinia")
                || lower.equals("datëlindja")
                || lower.equals("datelindja")
                || lower.equals("programi")
                || lower.equals("universiteti")
                || lower.equals("lloji")
                || lower.equals("fakulteti")
                || lower.equals("departamenti")
                || lower.equals("data")
                || lower.equals("viti")
                || lower.equals("veprime")
                || lower.equals("opsione")
                || lower.equals("aksione");
    }

    private String getBestValue(ElementInfo element) {

        String label = safe(element.getLabel());

        if (isValidInstructionValue(label)) {
            return label;
        }

        String text = safe(element.getText());

        if (isValidInstructionValue(text)) {
            return text;
        }

        String placeholder = safe(element.getPlaceholder());

        if (isValidInstructionValue(placeholder)) {
            return placeholder;
        }

        String name = safe(element.getName());

        if (isValidInstructionValue(name)) {
            return makeReadableName(name);
        }

        return "";
    }

    private boolean isValidInstructionValue(String value) {

        if (value == null || value.trim().isEmpty()) {
            return false;
        }

        value = cleanTitle(value);

        String lower = value.toLowerCase();

        if (value.length() < 2 || value.length() > 90) {
            return false;
        }

        if (lower.equals("elementi")
                || lower.equals("0")
                || lower.equals("ok")
                || lower.equals("yes")
                || lower.equals("no")
                || lower.equals("x")
                || lower.equals("zgjidh")
                || lower.equals("--zgjidhni--")) {
            return false;
        }

        if (lower.contains("tabela përmban")
                || lower.contains("sidebar")
                || lower.contains("menu")
                || lower.contains("informacion mbi funksionimin")
                || lower.contains("pagination")
                || lower.contains("rows per page")
                || lower.contains("items per page")
                || lower.contains("placeholder")
                || lower.contains("example")
                || lower.contains("enter code")
                || lower.contains("fromtodate")
                || lower.contains("from to date")
                || lower.contains("totodate")
                || lower.contains("javascript")) {
            return false;
        }

        if (lower.contains("e para")
                || lower.contains("e kaluara")
                || lower.contains("tjetra")
                || lower.contains("e fundit")) {
            return false;
        }

        if (lower.contains("999") || lower.contains("000")) {
            return false;
        }

        if (lower.matches("\\d+")) {
            return false;
        }

        return !lower.matches(".*\\d{4,}.*");
    }

    private String extractObjectName(String value) {

        String lower = value.toLowerCase();

        lower = lower
                .replace("shto", "")
                .replace("add", "")
                .trim();

        if (lower.isBlank()) {
            return "një rekord të ri";
        }

        return lower;
    }

    private String cleanSelectValue(String value) {

        if (value == null) {
            return "";
        }

        String result =
                value.replace("\n", " ")
                        .replace("\r", " ")
                        .replaceAll("[ \\t]+", " ")
                        .trim();

        if (result.length() > 80) {
            return result.substring(0, 80).trim();
        }

        return result;
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

    private String cleanTitle(String text) {

        if (text == null) {
            return "";
        }

        return text.replace("\n", " ")
                .replace("\r", " ")
                .replaceAll("[ \\t]+", " ")
                .trim();
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private void addDetailButtonInteraction(
            XWPFDocument document,
            PageInteractionInfo interaction
    ) {

        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setSpacingAfter(120);

        XWPFRun dashRun = paragraph.createRun();
        dashRun.setFontSize(11);
        dashRun.setText("- Butoni me simbol ");

        if (interaction.getScreenshotPath() != null
                && !interaction.getScreenshotPath().isBlank()) {

            try {
                File imageFile = new File(interaction.getScreenshotPath());

                if (imageFile.exists()) {
                    try (FileInputStream fis = new FileInputStream(imageFile)) {
                        XWPFRun imageRun = paragraph.createRun();

                        imageRun.addPicture(
                                fis,
                                getPictureType(imageFile.getName()),
                                imageFile.getName(),
                                Units.toEMU(22),
                                Units.toEMU(22)
                        );
                    }
                }

            } catch (Exception ignored) {
            }
        }

        XWPFRun textRun = paragraph.createRun();
        textRun.setFontSize(11);
        textRun.setText(
                " "
                        + lowerFirstLetter(interaction.getDescription())
                        + " "
                        + interaction.getResultDescription()
        );
    }

    private String lowerFirstLetter(String text) {

        if (text == null || text.isBlank()) {
            return "";
        }

        text = text.trim();

        return text.substring(0, 1).toLowerCase()
                + text.substring(1);
    }

    private void addFigureDescription(XWPFDocument document, String title) {

        if (title == null || title.trim().isEmpty()) {
            return;
        }

        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setSpacingAfter(160);

        XWPFRun run = paragraph.createRun();
        run.setFontSize(10);
        run.setItalic(true);

        run.setText(
                "Përshkrim: Figura paraqet pamjen vizuale të seksionit \""
                        + cleanTitle(title)
                        + "\", duke ndihmuar përdoruesin të identifikojë elementët kryesorë të faqes dhe funksionalitetet përkatëse."
        );
    }

    private String buildInteractionFigureTitle(PageInteractionInfo interaction) {

        if (interaction == null) {
            return "Ndërveprim me elementin e faqes";
        }

        String type = safe(interaction.getElementType());
        String text = safe(interaction.getElementText());

        if ("TABLE_ROW".equalsIgnoreCase(type)) {
            return "Hapja e detajeve nga rreshti i tabelës";
        }

        if ("DROPDOWN".equalsIgnoreCase(type)) {
            return "Hapja e listës " + text;
        }

        if ("BUTTON".equalsIgnoreCase(type)) {
            return "Klikimi i butonit " + text;
        }

        if (!text.isBlank()) {
            return "Ndërveprim me elementin " + text;
        }

        return "Ndërveprim me elementin e faqes";
    }




}