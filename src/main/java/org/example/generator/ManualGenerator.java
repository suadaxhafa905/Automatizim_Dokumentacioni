package org.example.generator;

import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.*;
import org.example.model.ElementInfo;
import org.example.model.ModalInfo;
import org.example.model.PageInfo;
import org.example.model.PageType;
import org.example.utils.PageTitleResolver;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


/*

Gjeneron manualin final .docx:

kapitujt
screenshot-et
figurat
përshkrimet
udhëzimet e përdorimit

Është klasa kryesore e dokumentimit.

 */

public class ManualGenerator {

    public void generateManualForPages(
            List<PageInfo> pages,
            String outputPath
    ) throws Exception {

        XWPFDocument document = new XWPFDocument();

        addTitle(document, "Manual Përdorimi");

        addParagraph(
                document,
                "Dokument i gjeneruar automatikisht për faqet dhe funksionalitetet e sistemit."
        );

        int pageCounter = 1;

        for (PageInfo pageInfo : pages) {

            String pageTitle = PageTitleResolver.resolve(pageInfo);

            PageClassifier classifier = new PageClassifier();
            PageType pageType = classifier.classify(pageInfo);

            PageSummaryGenerator summaryGenerator = new PageSummaryGenerator();
            FunctionalDescriptionGenerator functionalGenerator = new FunctionalDescriptionGenerator();

            addHeading(document, pageCounter + ". " + pageTitle);

            addHeading(document, pageCounter + ".1 Përshkrimi i faqes");

            addParagraph(
                    document,
                    summaryGenerator.generate(pageInfo, pageType)
            );

            addImage(document, pageInfo.getScreenshotPath());

            addFigureCaption(document, pageCounter, pageTitle);

            addHeading(document, pageCounter + ".2 Përshkrimi i funksionaliteteve");

            addParagraph(
                    document,
                    functionalGenerator.generate(pageInfo)
            );

            addStructuredUsageInstructions(
                    document,
                    pageInfo.getElements(),
                    pageCounter + ".3"
            );

            addModalSections(
                    document,
                    pageInfo,
                    pageCounter
            );

            pageCounter++;
        }

        FileOutputStream out = new FileOutputStream(outputPath);
        document.write(out);
        out.close();
        document.close();
    }

    private void addModalSections(
            XWPFDocument document,
            PageInfo pageInfo,
            int pageCounter
    ) {

        if (pageInfo.getModals() == null || pageInfo.getModals().isEmpty()) {
            return;
        }

        addHeading(
                document,
                pageCounter + ".4 Dritaret modale të faqes"
        );

        int modalCounter = 1;

        for (ModalInfo modal : pageInfo.getModals()) {

            String modalTitle = cleanTitle(modal.getModalTitle());

            if (modalTitle.isBlank()) {
                modalTitle = "Dritare modale";
            }

            addSubHeading(
                    document,
                    pageCounter + ".4." + modalCounter + " " + modalTitle
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
                        pageCounter,
                        "Modal - " + modalTitle
                );
            }

            if (modal.getElements() != null && !modal.getElements().isEmpty()) {

                addParagraph(
                        document,
                        "Elementet dhe veprimet kryesore të kësaj dritareje modale janë:"
                );

                addStructuredUsageInstructions(
                        document,
                        modal.getElements(),
                        pageCounter + ".4." + modalCounter
                );
            }

            modalCounter++;
        }
    }

    private void addTitle(
            XWPFDocument document,
            String text
    ) {

        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setAlignment(ParagraphAlignment.CENTER);

        XWPFRun run = paragraph.createRun();

        run.setBold(true);
        run.setFontSize(18);
        run.setText(text);
    }

    private void addHeading(
            XWPFDocument document,
            String text
    ) {

        XWPFParagraph paragraph = document.createParagraph();

        paragraph.setSpacingBefore(250);
        paragraph.setSpacingAfter(120);

        XWPFRun run = paragraph.createRun();

        run.setBold(true);
        run.setFontSize(14);
        run.setText(text);
    }

    private void addSubHeading(
            XWPFDocument document,
            String text
    ) {

        XWPFParagraph paragraph = document.createParagraph();

        paragraph.setSpacingBefore(120);
        paragraph.setSpacingAfter(60);

        XWPFRun run = paragraph.createRun();

        run.setBold(true);
        run.setFontSize(11);
        run.setText(text);
    }

    private void addParagraph(
            XWPFDocument document,
            String text
    ) {

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

    private void addImage(
            XWPFDocument document,
            String imagePath
    ) {

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

    private void addFigureCaption(
            XWPFDocument document,
            int figureNumber,
            String title
    ) {

        XWPFParagraph paragraph = document.createParagraph();

        paragraph.setAlignment(ParagraphAlignment.CENTER);
        paragraph.setSpacingAfter(180);

        XWPFRun run = paragraph.createRun();

        run.setItalic(true);
        run.setFontSize(10);
        run.setText(
                "Figura "
                        + figureNumber
                        + ". "
                        + cleanTitle(title)
        );
    }

    private void addStructuredUsageInstructions(
            XWPFDocument document,
            List<ElementInfo> elements,
            String sectionNumber
    ) {

        boolean hasInputs = hasInputs(elements);
        boolean hasSelects = hasSelects(elements);
        boolean hasButtons = hasButtons(elements);

        if (!hasInputs && !hasSelects && !hasButtons) {
            return;
        }

        addHeading(
                document,
                sectionNumber + " Udhëzime përdorimi"
        );

        addParagraph(
                document,
                "Përdoruesi mund të kryejë veprimet e mëposhtme:"
        );

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

    private void addInputInstructions(
            XWPFDocument document,
            List<ElementInfo> elements
    ) {

        Set<String> added = new LinkedHashSet<>();

        for (ElementInfo element : elements) {

            String tag = safe(element.getTagName());
            String type = safe(element.getType());

            if (!"input".equalsIgnoreCase(tag)
                    && !"textarea".equalsIgnoreCase(tag)) {
                continue;
            }

            if ("hidden".equalsIgnoreCase(type)
                    || "button".equalsIgnoreCase(type)
                    || "submit".equalsIgnoreCase(type)
                    || "reset".equalsIgnoreCase(type)) {
                continue;
            }

            String value = getBestValue(element);

            if (!isValidInstructionValue(value)) {
                continue;
            }

            if (!added.add(value.toLowerCase())) {
                continue;
            }

            if ("password".equalsIgnoreCase(type)) {

                addParagraph(
                        document,
                        "- Plotësoni fushën e fjalëkalimit me kredencialin përkatës."
                );

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

    private void addSelectInstructions(
            XWPFDocument document,
            List<ElementInfo> elements
    ) {

        Set<String> added = new LinkedHashSet<>();

        for (ElementInfo element : elements) {

            if (!"select".equalsIgnoreCase(element.getTagName())) {
                continue;
            }

            String value = cleanSelectValue(getBestValue(element));

            if (!isValidInstructionValue(value)) {
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

    private void addButtonInstructions(
            XWPFDocument document,
            List<ElementInfo> elements
    ) {

        Set<String> added = new LinkedHashSet<>();

        for (ElementInfo element : elements) {

            if (!isRealButton(element)) {
                continue;
            }

            String value = getBestValue(element);

            if (!isValidInstructionValue(value)) {
                continue;
            }

            if (!isValidButtonInstruction(value)) {
                continue;
            }

            if (!added.add(value.toLowerCase())) {
                continue;
            }

            addParagraph(
                    document,
                    "- "
                            + buildButtonAction(value)
            );
        }
    }

    private String buildButtonAction(
            String value
    ) {

        String lower = value.toLowerCase().trim();

        if (lower.contains("kërko")
                || lower.contains("kerko")
                || lower.contains("search")) {

            return "Klikoni butonin \""
                    + value
                    + "\" për të shfaqur rezultatet sipas kritereve të kërkimit.";
        }

        if (lower.contains("filtro")
                || lower.contains("filter")) {

            return "Klikoni butonin \""
                    + value
                    + "\" për të filtruar të dhënat e shfaqura.";
        }

        if (lower.contains("shto")
                || lower.contains("add")) {

            String object = extractObjectName(value);

            return "Klikoni butonin \""
                    + value
                    + "\" për të shtuar "
                    + object
                    + " në sistem.";
        }

        if (lower.contains("ruaj")
                || lower.contains("save")) {

            return "Klikoni butonin \""
                    + value
                    + "\" për të ruajtur ndryshimet e kryera.";
        }

        if (lower.contains("pastro")
                || lower.contains("clear")
                || lower.contains("reset")) {

            return "Klikoni butonin \""
                    + value
                    + "\" për të pastruar filtrat ose vlerat e vendosura.";
        }

        if (lower.contains("eksporto")
                || lower.contains("export")) {

            return "Klikoni butonin \""
                    + value
                    + "\" për të eksportuar të dhënat e faqes.";
        }

        if (lower.contains("importo")
                || lower.contains("import")) {

            return "Klikoni butonin \""
                    + value
                    + "\" për të importuar të dhëna në sistem.";
        }

        if (lower.contains("gjenero")
                || lower.contains("generate")) {

            return "Klikoni butonin \""
                    + value
                    + "\" për të gjeneruar informacionin ose kodin përkatës.";
        }

        if (lower.contains("fshi")
                || lower.contains("delete")
                || lower.contains("remove")) {

            return "Klikoni butonin \""
                    + value
                    + "\" për të fshirë të dhënën përkatëse.";
        }

        if (lower.contains("shkarko")
                || lower.contains("download")) {

            return "Klikoni butonin \""
                    + value
                    + "\" për të shkarkuar raportin me të dhënat e shfaqura.";
        }

        if (lower.contains("modifiko")
                || lower.contains("ndrysho")
                || lower.contains("edit")) {

            return "Klikoni butonin \""
                    + value
                    + "\" për të modifikuar të dhënat ekzistuese.";
        }

        if (lower.contains("verifiko")
                || lower.contains("verify")) {

            return "Klikoni butonin \""
                    + value
                    + "\" për të verifikuar informacionin e vendosur.";
        }

        if (lower.contains("dërgo")
                || lower.contains("dergo")
                || lower.contains("send")) {

            return "Klikoni butonin \""
                    + value
                    + "\" për të dërguar të dhënat ose kodin për përpunim.";
        }

        if (lower.contains("aktivizo")
                || lower.contains("activate")) {

            return "Klikoni butonin \""
                    + value
                    + "\" për të aktivizuar funksionalitetin ose përdoruesin përkatës.";
        }

        if (lower.contains("çaktivizo")
                || lower.contains("caktivizo")
                || lower.contains("deactivate")) {

            return "Klikoni butonin \""
                    + value
                    + "\" për të çaktivizuar funksionalitetin ose përdoruesin përkatës.";
        }

        return "Klikoni butonin \""
                + value
                + "\" për të ekzekutuar funksionalitetin përkatës në sistem.";
    }

    private boolean hasInputs(List<ElementInfo> elements) {

        for (ElementInfo element : elements) {

            String tag = safe(element.getTagName());
            String type = safe(element.getType());
            String value = getBestValue(element);

            if (("input".equalsIgnoreCase(tag)
                    || "textarea".equalsIgnoreCase(tag))
                    && !"hidden".equalsIgnoreCase(type)
                    && !"button".equalsIgnoreCase(type)
                    && !"submit".equalsIgnoreCase(type)
                    && !"reset".equalsIgnoreCase(type)
                    && isValidInstructionValue(value)) {
                return true;
            }
        }

        return false;
    }

    private boolean hasSelects(List<ElementInfo> elements) {

        for (ElementInfo element : elements) {

            String value = getBestValue(element);

            if ("select".equalsIgnoreCase(element.getTagName())
                    && isValidInstructionValue(value)) {
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

        String lower = value.toLowerCase();

        if (lower.endsWith(":")
                || lower.endsWith("...")
                || lower.contains("zgjidh")
                || lower.contains("select")
                || lower.contains("choose")
                || lower.contains("pyetja")
                || lower.contains("cikli i studimit")
                || lower.contains("institucioni i arsimit")
                || lower.contains("programi i studimit")
                || lower.contains("kërko me")
                || lower.contains("kerko me")) {
            return false;
        }

        return true;
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

        if (lower.contains("999")
                || lower.contains("000")) {
            return false;
        }

        if (lower.matches("\\d+")) {
            return false;
        }

        if (lower.matches(".*\\d{4,}.*")) {
            return false;
        }

        return true;
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

        return value == null
                ? ""
                : value.trim();
    }
}