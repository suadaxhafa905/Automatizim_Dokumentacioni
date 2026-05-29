package org.example.generator;

import org.apache.poi.xwpf.usermodel.*;
import org.example.model.ElementInfo;
import org.example.model.PageInfo;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.example.model.PageInteractionInfo;
/*
Gjeneron automatikisht:

-acceptance test cases
-hapat e testimit
-rezultatet e pritshme
bazuar në elementët e faqes

Merret me navigimin në menu dhe nënmenu të sistemit.
Klikon automatikisht menutë dhe mbledh URL-të e faqeve që do të dokumentohen ose testohen.

Gjeneron dokumentin e Acceptance Test. Krijon skenarë testimi,
hapa testimi dhe rezultate të pritshme bazuar në elementët e identifikuar në faqe.
 */
public class AcceptanceTestGenerator {

    public void generateAcceptanceTestsForPages(
            List<PageInfo> pages,
            String outputPath
    ) throws Exception {

        XWPFDocument document = new XWPFDocument();

        addTitle(document, "Acceptance Test");

        addParagraph(
                document,
                "Dokument i gjeneruar automatikisht për testimin e faqeve dhe funksionaliteteve të sistemit."
        );

        int chapterNumber = 1;

        for (PageInfo pageInfo : pages) {

            if (pageInfo.getPageTitle() == null
                    || pageInfo.getPageTitle().isBlank()) {
                continue;
            }

            addHeading(
                    document,
                    chapterNumber + ". " + clean(pageInfo.getPageTitle())
            );

            addIntro(document, pageInfo);

            TestCaseInfo testCase =
                    buildSinglePageTestCase(pageInfo);

            addTestCaseBlock(document, testCase);

            chapterNumber++;
        }

        FileOutputStream out =
                new FileOutputStream(outputPath);

        document.write(out);
        out.close();
        document.close();
    }

    public void generateAcceptanceTests(
            PageInfo pageInfo,
            String outputPath
    ) throws Exception {

        List<PageInfo> pages = new ArrayList<>();
        pages.add(pageInfo);

        generateAcceptanceTestsForPages(
                pages,
                outputPath
        );
    }

    private TestCaseInfo buildSinglePageTestCase(
            PageInfo pageInfo
    ) {

        List<TestStep> steps = new ArrayList<>();

        int step = 1;

        String pageTitle =
                clean(pageInfo.getPageTitle());

        steps.add(
                new TestStep(
                        step++,
                        "Hapni faqen \"" + pageTitle + "\".",
                        "Faqja shfaqet me sukses dhe elementët kryesorë janë të dukshëm."
                )
        );

        for (String field : getInputFields(pageInfo)) {

            steps.add(
                    new TestStep(
                            step++,
                            "Plotësoni fushën \"" + field + "\".",
                            "Fusha \"" + field + "\" shfaqet e plotësuar."
                    )
            );
        }

        for (String select : getSelectFields(pageInfo)) {

            steps.add(
                    new TestStep(
                            step++,
                            "Zgjidhni një vlerë nga lista \"" + select + "\".",
                            "Vlera e përzgjedhur shfaqet në listën përkatëse."
                    )
            );
        }

        for (String button : getButtons(pageInfo)) {

            steps.add(
                    new TestStep(
                            step++,
                            "Klikoni butonin \"" + button + "\".",
                            buildExpectedResultForButton(button)
                    )
            );
        }


        for (PageInteractionInfo interaction : pageInfo.getInteractions()) {

            if (interaction == null) {
                continue;
            }

            String action =
                    clean(interaction.getDescription());

            String result =
                    clean(interaction.getResultDescription());

            if (action.isBlank() || result.isBlank()) {
                continue;
            }

            steps.add(
                    new TestStep(
                            step++,
                            action,
                            result
                    )
            );
        }


        if (hasTables(pageInfo)) {

            steps.add(
                    new TestStep(
                            step++,
                            "Verifikoni tabelën e të dhënave të shfaqur në faqe.",
                            "Tabela shfaqet në mënyrë të rregullt dhe kolonat kryesore janë të dukshme."
                    )
            );
        }

        return new TestCaseInfo(
                "Moduli / Faqja – " + pageTitle,
                "Verifikimi i funksionaliteteve kryesore të faqes",
                steps
        );
    }

    private void addIntro(
            XWPFDocument document,
            PageInfo pageInfo
    ) {

        String title =
                clean(pageInfo.getPageTitle());

        addParagraph(
                document,
                "Ky seksion përshkruan skenarët e testimit pranues për faqen \"" + title + "\". " +
                        "Testimi verifikon shfaqjen e faqes, aksesueshmërinë e elementeve kryesore, " +
                        "plotësimin e fushave, përdorimin e butonave funksionalë dhe shfaqjen e rezultateve të pritshme."
        );
    }

    private void addTestCaseBlock(
            XWPFDocument document,
            TestCaseInfo testCase
    ) {

        addMetaTable(document, testCase);
        addStepsTable(document, testCase.steps);
        addIssuesTable(document);
        addEmptyParagraph(document);
    }

    private void addMetaTable(
            XWPFDocument document,
            TestCaseInfo testCase
    ) {

        XWPFTable table =
                document.createTable(2, 3);

        setTableWidth(table, 9000);

        table.getRow(0).getCell(0).setText("Emri");
        table.getRow(0).getCell(1).setText(testCase.componentName);
        table.getRow(0).getCell(2).setText("");

        table.getRow(1).getCell(0).setText("Kërkesa");
        table.getRow(1).getCell(1).setText(testCase.requirement);
        table.getRow(1).getCell(2).setText("");

        mergeCellsHorizontally(table, 0, 1, 2);
        mergeCellsHorizontally(table, 1, 1, 2);

        setCellWidth(table.getRow(0).getCell(0), 1200);
        setCellWidth(table.getRow(0).getCell(1), 7800);

        setCellWidth(table.getRow(1).getCell(0), 1200);
        setCellWidth(table.getRow(1).getCell(1), 7800);

        shadeRow(table.getRow(0));
        shadeRow(table.getRow(1));
    }

    private void addStepsTable(
            XWPFDocument document,
            List<TestStep> steps
    ) {

        XWPFTable table =
                document.createTable(1, 3);

        table.setWidth("100%");

        XWPFTableRow titleRow =
                table.getRow(0);

        titleRow.getCell(0)
                .setText("Përshkrimi i hapave të testit");

        titleRow.getCell(1).setText("");
        titleRow.getCell(2).setText("");

        mergeCellsHorizontally(table, 0, 0, 2);

        shadeRow(titleRow);

        XWPFTableRow header =
                table.createRow();

        ensureCells(header, 3);

        header.getCell(0).setText("Hapi");
        header.getCell(1).setText("Veprimi");
        header.getCell(2).setText("Rezultati i pritur");
        table.getRow(1).getCell(0).setWidth("1000");
        table.getRow(1).getCell(1).setWidth("5000");
        table.getRow(1).getCell(2).setWidth("5000");

        shadeRow(header);
        header.setHeight(500);

        for (TestStep step : steps) {

            XWPFTableRow row =
                    table.createRow();

            ensureCells(row, 3);

            row.getCell(0).setWidth("1000");
            row.getCell(1).setWidth("5000");
            row.getCell(2).setWidth("5000");

            row.getCell(0).setText(String.valueOf(step.stepNumber));
            row.getCell(1).setText(step.action);
            row.getCell(2).setText(step.expectedResult);
        }
    }

    private void addIssuesTable(
            XWPFDocument document
    ) {

        XWPFTable table =
                document.createTable(1, 3);

        table.setWidth("100%");

        XWPFTableRow titleRow =
                table.getRow(0);

        titleRow.getCell(0)
                .setText("Rezultatet e testimit – Probleme të identifikuara");

        titleRow.getCell(1).setText("");
        titleRow.getCell(2).setText("");

        mergeCellsHorizontally(table, 0, 0, 2);

        shadeRow(titleRow);

        XWPFTableRow header =
                table.createRow();

        ensureCells(header, 3);
        header.getCell(0).setWidth("1500");
        header.getCell(1).setWidth("3500");
        header.getCell(2).setWidth("5000");

        header.getCell(0).setText("Hapi #");
        header.getCell(1).setText("Klasifikimi");
        header.getCell(2).setText("Përshkrimi");

        shadeRow(header);
        header.setHeight(500);

        XWPFTableRow emptyRow =
                table.createRow();

        ensureCells(emptyRow, 3);

        emptyRow.getCell(0).setText("");
        emptyRow.getCell(1).setText("");
        emptyRow.getCell(2).setText("");
    }

    private boolean hasTables(
            PageInfo pageInfo
    ) {

        for (ElementInfo element : pageInfo.getElements()) {

            if ("table".equalsIgnoreCase(element.getTagName())) {
                return true;
            }
        }

        return false;
    }

    private List<String> getInputFields(
            PageInfo pageInfo
    ) {

        Set<String> fields =
                new LinkedHashSet<>();

        for (ElementInfo element : pageInfo.getElements()) {

            if ("input".equalsIgnoreCase(element.getTagName())
                    || "textarea".equalsIgnoreCase(element.getTagName())) {

                String value =
                        getBestValue(element);

                if (isValidValue(value)) {
                    fields.add(value);
                }
            }
        }

        return new ArrayList<>(fields);
    }

    private List<String> getSelectFields(
            PageInfo pageInfo
    ) {

        Set<String> fields =
                new LinkedHashSet<>();

        for (ElementInfo element : pageInfo.getElements()) {

            if ("select".equalsIgnoreCase(element.getTagName())) {

                String value =
                        getBestValue(element);

                if (isValidSelectValue(value)) {
                    fields.add(value);
                }
            }
        }

        return new ArrayList<>(fields);
    }

    private List<String> getButtons(
            PageInfo pageInfo
    ) {

        Set<String> buttons =
                new LinkedHashSet<>();

        for (ElementInfo element : pageInfo.getElements()) {

            if ("button".equalsIgnoreCase(element.getTagName())) {

                String value =
                        getBestValue(element);

                if (isValidButtonValue(value)) {
                    buttons.add(value);
                }
            }
        }

        return new ArrayList<>(buttons);
    }

    private String buildExpectedResultForButton(
            String button
    ) {

        String lower =
                button.toLowerCase();

        if (lower.contains("kërko")
                || lower.contains("kerko")) {
            return "Shfaqen rezultatet sipas kritereve të kërkimit.";
        }

        if (lower.contains("filtro")) {
            return "Të dhënat filtrohen sipas kritereve të vendosura.";
        }

        if (lower.contains("pastro")) {
            return "Filtrat ose vlerat e vendosura pastrohen.";
        }

        if (lower.contains("shto")) {
            return "Hapet ndërfaqja për shtimin e të dhënave të reja.";
        }

        if (lower.contains("ruaj")) {
            return "Të dhënat ruhen me sukses në sistem.";
        }

        if (lower.contains("fshi")) {
            return "Sistemi kërkon konfirmim ose fshin të dhënën përkatëse.";
        }

        if (lower.contains("modifiko")
                || lower.contains("ndrysho")
                || lower.contains("ndysho")) {
            return "Hapet forma për modifikimin e të dhënave ekzistuese.";
        }

        if (lower.contains("regjistro")) {
            return "Të dhënat regjistrohen me sukses në sistem.";
        }

        if (lower.contains("importo")) {
            return "Të dhënat importohen me sukses në sistem.";
        }

        if (lower.contains("shkarko")) {
            return "Raporti ose të dhënat shkarkohen me sukses.";
        }

        if (lower.contains("graf")) {
            return "Shfaqet grafiku përkatës me të dhënat e përzgjedhura.";
        }

        if (lower.contains("verifiko")) {
            return "Sistemi kryen verifikimin e të dhënave.";
        }

        if (lower.contains("dërgo")
                || lower.contains("dergo")) {
            return "Të dhënat dërgohen me sukses për përpunim.";
        }

        if (lower.contains("gjenero")) {
            return "Sistemi gjeneron të dhënën ose kodin përkatës.";
        }

        if (lower.contains("refuzo")) {
            return "Sistemi refuzon veprimin dhe shfaq rezultatin përkatës.";
        }

        if (lower.contains("aktivizo")
                || lower.contains("riaktivizo")) {
            return "Regjistrimi ose përdoruesi aktivizohet me sukses.";
        }

        if (lower.contains("pezullo")) {
            return "Regjistrimi ose përdoruesi pezullohet sipas veprimit të zgjedhur.";
        }

        if (lower.contains("çregjistro")
                || lower.contains("cregjistro")) {
            return "Regjistrimi çregjistrohet sipas veprimit të zgjedhur.";
        }

        if (lower.contains("diplomo")) {
            return "Studenti ose regjistrimi përkatës diplomohet në sistem.";
        }

        return "Veprimi ekzekutohet me sukses në sistem.";
    }

    private String getBestValue(
            ElementInfo element
    ) {

        String text =
                clean(element.getText());

        if (isValidValue(text)
                && !isFakePlaceholder(text)) {
            return normalize(text);
        }

        String placeholder =
                clean(element.getPlaceholder());

        if (isValidValue(placeholder)
                && !isFakePlaceholder(placeholder)) {
            return normalize(placeholder);
        }

        String name =
                clean(element.getName());

        if (isValidValue(name)
                && !isFakePlaceholder(name)) {
            return normalize(name);
        }

        return "";
    }

    private boolean isValidValue(
            String value
    ) {

        if (value == null || value.isBlank()) {
            return false;
        }

        value = clean(value);

        String lower =
                value.toLowerCase();

        if (value.length() < 2
                || value.length() > 80) {
            return false;
        }

        if (value.matches("\\d+")) {
            return false;
        }

        if (lower.equals("elementi")
                || lower.equals("kërko...")
                || lower.equals("kerko...")
                || lower.equals("search...")
                || lower.equals("filter...")
                || lower.equals("zgjidh")
                || lower.equals("--zgjidhni--")
                || lower.endsWith(":")) {
            return false;
        }

        if (lower.contains("zgjidhni")
                || lower.endsWith("...")
                || lower.startsWith("--")) {
            return false;
        }

        if (lower.contains("placeholder")
                || lower.contains("example")
                || lower.contains("enter")) {
            return false;
        }

        if (lower.contains("http")
                || lower.contains("www")) {
            return false;
        }

        return true;
    }

    private boolean isValidSelectValue(
            String value
    ) {

        if (!isValidValue(value)) {
            return false;
        }

        String lower =
                value.toLowerCase();

        if (lower.contains("të gjitha")
                && value.split("\\s+").length > 4) {
            return false;
        }

        return value.split("\\s+").length <= 8;
    }

    private boolean isValidButtonValue(
            String value
    ) {

        if (!isValidValue(value)) {
            return false;
        }

        String lower =
                value.toLowerCase();

        return !(lower.equals("x")
                || lower.equals("ok")
                || lower.equals("close")
                || lower.equals("mbyll"));
    }

    private boolean isFakePlaceholder(
            String text
    ) {

        if (text == null || text.isBlank()) {
            return true;
        }

        text = clean(text);

        String lower =
                text.toLowerCase();

        if (text.matches(".*\\d{4,}.*")) {
            return true;
        }

        if (text.matches(".*\\d.*\\d.*\\d.*")) {
            return true;
        }

        if (lower.contains("enter")) {
            return true;
        }

        if (lower.contains("placeholder")) {
            return true;
        }

        if (lower.contains("example")) {
            return true;
        }

        if (lower.matches("[a-z]\\d+[a-z]?")) {
            return true;
        }

        return lower.matches(".*[a-z]\\d{2,}.*");
    }

    private void addTitle(
            XWPFDocument document,
            String text
    ) {

        XWPFParagraph paragraph =
                document.createParagraph();

        paragraph.setAlignment(
                ParagraphAlignment.CENTER
        );

        XWPFRun run =
                paragraph.createRun();

        run.setBold(true);
        run.setFontSize(18);
        run.setText(text);
    }

    private void addHeading(
            XWPFDocument document,
            String text
    ) {

        XWPFParagraph paragraph =
                document.createParagraph();

        paragraph.setSpacingBefore(300);
        paragraph.setSpacingAfter(150);

        XWPFRun run =
                paragraph.createRun();

        run.setBold(true);
        run.setFontSize(14);
        run.setText(text);
    }

    private void addParagraph(
            XWPFDocument document,
            String text
    ) {

        XWPFParagraph paragraph =
                document.createParagraph();

        XWPFRun run =
                paragraph.createRun();

        run.setFontSize(11);
        run.setText(text);
    }

    private void addEmptyParagraph(
            XWPFDocument document
    ) {

        document.createParagraph();
    }

    private void shadeRow(
            XWPFTableRow row
    ) {

        for (XWPFTableCell cell : row.getTableCells()) {

            cell.setColor("D9D9D9");

            cell.setVerticalAlignment(
                    XWPFTableCell.XWPFVertAlign.CENTER
            );

            for (XWPFParagraph paragraph : cell.getParagraphs()) {

                for (XWPFRun run : paragraph.getRuns()) {
                    run.setBold(true);
                }
            }
        }
    }

    private String clean(
            String text
    ) {

        if (text == null) {
            return "";
        }

        return text
                .replace("\n", " ")
                .replace("\r", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String normalize(
            String text
    ) {

        if (text == null) {
            return "";
        }

        return clean(text)
                .replace("manulisht", "manualisht")
                .replace("Ndysho", "Ndrysho")
                .replace(" ne ", " në ");
    }

    private void ensureCells(
            XWPFTableRow row,
            int numberOfCells
    ) {

        while (row.getTableCells().size() < numberOfCells) {
            row.addNewTableCell();
        }
    }

    private void mergeCellsHorizontally(
            XWPFTable table,
            int row,
            int fromCell,
            int toCell
    ) {

        for (int cellIndex = fromCell; cellIndex <= toCell; cellIndex++) {

            if (cellIndex == fromCell) {

                table.getRow(row)
                        .getCell(cellIndex)
                        .getCTTc()
                        .addNewTcPr()
                        .addNewHMerge()
                        .setVal(org.openxmlformats.schemas.wordprocessingml.x2006.main.STMerge.RESTART);

            } else {

                table.getRow(row)
                        .getCell(cellIndex)
                        .getCTTc()
                        .addNewTcPr()
                        .addNewHMerge()
                        .setVal(org.openxmlformats.schemas.wordprocessingml.x2006.main.STMerge.CONTINUE);
            }
        }
    }

    private static class TestCaseInfo {

        String componentName;
        String requirement;
        List<TestStep> steps;

        TestCaseInfo(
                String componentName,
                String requirement,
                List<TestStep> steps
        ) {

            this.componentName = componentName;
            this.requirement = requirement;
            this.steps = steps;
        }
    }

    private static class TestStep {

        int stepNumber;
        String action;
        String expectedResult;

        TestStep(
                int stepNumber,
                String action,
                String expectedResult
        ) {

            this.stepNumber = stepNumber;
            this.action = action;
            this.expectedResult = expectedResult;
        }
    }

    private void setTableWidth(
            XWPFTable table,
            int width
    ) {

        table.getCTTbl()
                .getTblPr()
                .getTblW()
                .setW(java.math.BigInteger.valueOf(width));

        table.getCTTbl()
                .getTblPr()
                .getTblW()
                .setType(
                        org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblWidth.DXA
                );
    }

    private void setCellWidth(
            XWPFTableCell cell,
            int width
    ) {

        if (!cell.getCTTc().isSetTcPr()) {

            cell.getCTTc().addNewTcPr();
        }

        if (!cell.getCTTc().getTcPr().isSetTcW()) {

            cell.getCTTc()
                    .getTcPr()
                    .addNewTcW();
        }

        cell.getCTTc()
                .getTcPr()
                .getTcW()
                .setW(java.math.BigInteger.valueOf(width));

        cell.getCTTc()
                .getTcPr()
                .getTcW()
                .setType(
                        org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblWidth.DXA
                );
    }

}