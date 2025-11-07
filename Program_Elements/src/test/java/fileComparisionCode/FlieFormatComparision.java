package fileComparisionCode;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import java.io.*;
import java.util.*;
import java.util.regex.*;

public class FlieFormatComparision {

    public static void main(String[] args) {
        String staticPdfPath = "C:\\Users\\User\\Downloads\\static_invoice_template.pdf";
        String dynamicPdfPath = "C:\\Users\\User\\Downloads\\generated_invoice_valid.pdf";
        String reportPath = "C:\\Users\\User\\Downloads\\invoice_comparison_report.txt";

        StringBuilder report = new StringBuilder();
        boolean allMatch = true;

        try {
            // Extract text from PDFs
            String staticText = extractTextFromPDF(staticPdfPath);
            String dynamicText = extractTextFromPDF(dynamicPdfPath);

            List<String> staticLines = Arrays.asList(staticText.split("\\r?\\n"));
            List<String> dynamicLines = Arrays.asList(dynamicText.split("\\r?\\n"));

            report.append("🔍 Comparing Static vs Dynamic Invoice (improved matching)...\n\n");

            for (int i = 0; i < staticLines.size(); i++) {
                String s = staticLines.get(i).trim();
                if (s.isEmpty()) continue;

                // find matching line ignoring order
                String d = findMatchingLine(dynamicLines, s);

                if (d == null) {
                    report.append("⚠️ Line missing in dynamic invoice: ").append(s).append("\n");
                    allMatch = false;
                    continue;
                }

                // Compare placeholders and dynamic lines
                if (s.contains("[YOUR COMPANY NAME]")) allMatch &= checkText(report, d, "SaiTech Solutions", "COMPANY NAME", i);
                else if (s.contains("[ADDRESS LINE 1")) allMatch &= checkPattern(report, d, "Address:?\\s*(.+)", "[A-Za-z ,]+", "ADDRESS", i);
                else if (s.contains("[EMAIL@DOMAIN.COM]")) allMatch &= checkPattern(report, d, "Email:?\\s*(.+)", "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$", "EMAIL", i);
                else if (s.contains("[PHONE NUMBER]")) allMatch &= checkPattern(report, d, "Phone:?\\s*(.+)", "^[+0-9\\- ]+$", "PHONE", i);
                else if (s.contains("[CLIENT NAME]")) allMatch &= checkPattern(report, d, "Client Name:?\\s*(.+)", "[A-Za-z ]+", "CLIENT NAME", i);
                else if (s.contains("[CLIENT COMPANY NAME]")) allMatch &= checkPattern(report, d, "(Company|Company Name):?\\s*(.+)", "[A-Za-z0-9&,. ]+", "CLIENT COMPANY", i);
                else if (s.contains("[CLIENT ADDRESS]")) allMatch &= checkPattern(report, d, "Address:?\\s*(.+)", "[A-Za-z ,]+", "CLIENT ADDRESS", i);
                else if (s.contains("[INVOICE_NUMBER]")) allMatch &= checkPattern(report, d, "Invoice No:?\\s*(.+)", "[A-Z]{5}[0-9]{4}[A-Z]{1}", "INVOICE NUMBER", i);
                else if (s.contains("[INVOICE_DATE]")) allMatch &= checkPattern(report, d, "Invoice Date:?\\s*(.+)", "\\d{2}-\\d{2}-\\d{4}", "INVOICE DATE", i);
                else if (s.contains("[DUE_DATE]")) allMatch &= checkPattern(report, d, "Due Date:?\\s*(.+)", "\\d{2}-\\d{2}-\\d{4}", "DUE DATE", i);
                else if (s.matches("\\d+ \\[ITEM_.*")) allMatch &= checkPattern(report, d, ".*", "\\d+\\s+.*\\s+\\d+\\s+\\d+(\\.\\d{1,2})?\\s+\\d+(\\.\\d{1,2})?", "ITEM ROW", i);
                else if (s.contains("[SUBTOTAL]")) allMatch &= checkPattern(report, d, "Subtotal:?\\s*(.+)", "\\d+(\\.\\d{1,2})?", "SUBTOTAL", i);
                else if (s.contains("[TAX_VALUE]")) allMatch &= checkPattern(report, d, "Tax.*:?\\s*(.+)", "\\d+(\\.\\d{1,2})?", "TAX VALUE", i);
                else if (s.contains("[TOTAL_AMOUNT]")) allMatch &= checkPattern(report, d, "Total Amount Due:?\\s*(.+)", "\\d+(\\.\\d{1,2})?", "TOTAL AMOUNT", i);
                else if (s.contains("[BANK_NAME]")) allMatch &= checkPattern(report, d, "Bank Name:?\\s*(.+)", "[A-Za-z ]+", "BANK NAME", i);
                else if (s.contains("[ACCOUNT_NO]")) allMatch &= checkPattern(report, d, "Account Number:?\\s*(.+)", "\\d{9,18}", "ACCOUNT NO", i);
                else if (s.contains("[IFSC_CODE]")) allMatch &= checkPattern(report, d, "IFSC Code:?\\s*(.+)", "[A-Z]{4}0[A-Z0-9]{6}", "IFSC CODE", i);
                else if (s.contains("[UPI_ID]")) allMatch &= checkPattern(report, d, "UPI ID:?\\s*(.+)", "[a-zA-Z0-9\\.\\-_]{2,256}@[a-zA-Z]{2,64}", "UPI ID", i);
                else if (s.contains("[ADDITIONAL_NOTES]")) allMatch &= checkText(report, d, "Thank you for your business!", "NOTES", i);
                else if (s.contains("Authorized Signature")) allMatch &= checkText(report, d, "Authorized Signature", "SIGNATURE", i);
                else if (s.contains("_________________________")) allMatch &= d.contains("_");
            }

            report.append("\n------------------------------------------\n");
            if (allMatch) report.append("✅ PDFs match successfully (layout & data validation passed).\n");
            else report.append("❌ Differences found — check details above.\n");

            try (FileWriter fw = new FileWriter(reportPath)) {
                fw.write(report.toString());
            }

            System.out.println("✅ Comparison complete. Report saved at:\n" + reportPath);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ===== Utility Methods =====

    public static String extractTextFromPDF(String filePath) throws Exception {
        PDDocument doc = PDDocument.load(new File(filePath));
        PDFTextStripper stripper = new PDFTextStripper();
        String text = stripper.getText(doc);
        doc.close();
        return text;
    }

    private static String findMatchingLine(List<String> dynamicLines, String staticLine) {
        String label = staticLine.replaceAll("\\[.*?\\]", "").trim().split(":")[0];
        for (String dyn : dynamicLines) {
            if (dyn.trim().toLowerCase().contains(label.toLowerCase())) {
                return dyn.trim();
            }
        }
        return null;
    }

    private static boolean checkText(StringBuilder report, String line, String expected, String field, int i) {
        if (line.toLowerCase().contains(expected.toLowerCase())) {
            report.append("✅ ").append(field).append(" OK at line ").append(i + 1).append(": ").append(expected).append("\n");
            return true;
        } else {
            report.append("❌ ").append(field).append(" mismatch at line ").append(i + 1).append(": ").append(line).append("\n");
            return false;
        }
    }

    private static boolean checkPattern(StringBuilder report, String line, String prefixRegex, String valueRegex, String field, int i) {
        try {
            Pattern p = Pattern.compile(prefixRegex);
            Matcher m = p.matcher(line);
            if (m.find()) {
                String val = m.group(m.groupCount()).trim();
                if (val.matches(valueRegex)) {
                    report.append("✅ ").append(field).append(" valid at line ").append(i + 1).append(": ").append(val).append("\n");
                    return true;
                } else {
                    report.append("❌ Invalid ").append(field).append(" at line ").append(i + 1).append(": ").append(val).append("\n");
                    return false;
                }
            } else {
                report.append("⚠️ No match for ").append(field).append(" at line ").append(i + 1).append(": ").append(line).append("\n");
                return false;
            }
        } catch (Exception e) {
            report.append("⚠️ Regex error for ").append(field).append(" at line ").append(i + 1).append(": ").append(e.getMessage()).append("\n");
            return false;
        }
    }
}
