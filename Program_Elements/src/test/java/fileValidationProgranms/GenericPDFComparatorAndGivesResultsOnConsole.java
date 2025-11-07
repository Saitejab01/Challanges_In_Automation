package fileValidationProgranms;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import java.io.*;
import java.util.*;
import java.util.regex.*;

public class GenericPDFComparatorAndGivesResultsOnConsole {

    public static void main(String[] args) {
        String staticPdfPath = "C:\\Users\\User\\Desktop\\FileComparation\\static_invoice_template_v2.pdf";
        String dynamicPdfPath = "C:\\Users\\User\\Desktop\\FileComparation\\valid_dynamic_invoice_v2.pdf";

        try {
            comparePDFs(staticPdfPath, dynamicPdfPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Compares a static PDF template and a dynamic PDF file.
     * - Extracts text from both files.
     * - Iterates through static PDF lines.
     * - Matches placeholders and validates data format.
     * - Prints readable comparison results to the console.
     */
    public static void comparePDFs(String staticPdfPath, String dynamicPdfPath) throws Exception {
        boolean allMatch = true;

        // Extract text from both PDFs
        String staticText = extractTextFromPDF(staticPdfPath);
        String dynamicText = extractTextFromPDF(dynamicPdfPath);

        System.out.println("Comparing Static vs Dynamic PDF...\n");

        String[] staticLines = staticText.split("\\r?\\n");

        for (String staticLine : staticLines) {
            staticLine = staticLine.trim();
            if (staticLine.isEmpty()) continue;

            // Check if the line contains a placeholder like [CLIENT_NAME]
            Matcher placeholderMatcher = Pattern.compile("\\[(.*?)\\]").matcher(staticLine);

            if (placeholderMatcher.find()) {
                String placeholder = placeholderMatcher.group(1);
                String fieldLabel = staticLine.replaceAll("\\[.*?\\]", "").trim();

                // Detect validation pattern (e.g., EMAIL, PHONE, DATE, etc.)
                String regexPattern = detectPattern(placeholder);

                // Build a pattern to find the field in the dynamic PDF
                String searchRegex = Pattern.quote(fieldLabel) + "\\s*:?\\s*(.+)";
                Pattern pattern = Pattern.compile(searchRegex, Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(dynamicText);

                if (matcher.find() && matcher.groupCount() >= 1) {
                    String value = safeGroup(matcher, 1);
                    if (value.matches(regexPattern)) {
                        System.out.println("Match Found: " + fieldLabel + value);
                    } else {
                        System.out.println("Value Format Mismatch: " + fieldLabel + value);
                        allMatch = false;
                    }
                } else {
                    System.out.println("Field Missing in Dynamic PDF: " + fieldLabel);
                    allMatch = false;
                }
            } else {
                // Handle plain text (static text without placeholders)
                if (dynamicText.toLowerCase().contains(staticLine.toLowerCase())) {
                    System.out.println("Static Text Found: " + staticLine);
                } else {
                    System.out.println("Static Text Missing: " + staticLine);
                    allMatch = false;
                }
            }
        }

        System.out.println("\n------------------------------------------");
        if (allMatch)
            System.out.println("Result: SAME — PDFs match successfully (layout and data validation passed).");
        else
            System.out.println("Result: DIFFERENT — Differences found in structure or data.");
    }

    /**
     * Reads the text content of a PDF file using Apache PDFBox.
     * @param filePath the path to the PDF file
     * @return extracted text as a String
     */
    private static String extractTextFromPDF(String filePath) throws Exception {
        PDDocument doc = PDDocument.load(new File(filePath));
        PDFTextStripper stripper = new PDFTextStripper();
        String text = stripper.getText(doc);
        doc.close();
        return text;
    }

    /**
     * Detects which regex rule should apply for a placeholder.
     * Example: EMAIL → email format, PHONE → 10-digit number, etc.
     * Returns a general ".*" pattern if no specific rule matches.
     */
    private static String detectPattern(String placeholder) {
        Map<String, String> patterns = new HashMap<>();

        patterns.put("CLIENT_NAME", "^[A-Za-z\\.]{1,15}$");
        patterns.put("CLIENT_ADDRESS", "^[A-Za-z0-9\\s,./#@\\-]{1,50}$");
        patterns.put("EMAIL", "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
        patterns.put("PHONE", "^(\\+91[- ]?)?\\d{10}$");
        patterns.put("INVOICE_NUMBER", "^[A-Z]{5}[0-9]{4}[A-Z]{1}$");
        patterns.put("DATE", "^\\d{2}-\\d{2}-\\d{4}$");
        patterns.put("AMOUNT", "^\\d+(\\.\\d{1,2})?$");
        patterns.put("IFSC_CODE", "^[A-Z]{4}0[A-Z0-9]{6}$");
        patterns.put("ACCOUNT_NO", "^\\d{9,18}$");
        patterns.put("UPI_ID", "^[a-zA-Z0-9\\.\\-_]{2,256}@[a-zA-Z]{2,64}$");

        // Return custom regex if placeholder already has one
        if (placeholder.matches(".*\\{.*\\}.*"))
            return placeholder;

        // Find matching rule for known fields
        for (String key : patterns.keySet()) {
            if (placeholder.toUpperCase().contains(key))
                return patterns.get(key);
        }

        // Default to match any text
        return ".*";
    }

    /**
     * Safely extracts a regex group value and trims it.
     * Prevents NullPointerException if group is missing.
     */
    private static String safeGroup(Matcher m, int group) {
        try {
            if (m.groupCount() >= group && m.group(group) != null)
                return m.group(group).trim();
        } catch (Exception ignored) {}
        return "";
    }
}
