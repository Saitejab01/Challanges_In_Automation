package fileValidationProgranms;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import java.io.*;
import java.util.*;
import java.util.regex.*;

public class GenericPDFComparatorAndStoreResultInTxtFile {

    public static void main(String[] args) {
        String staticPdfPath = "C:\\Users\\User\\Desktop\\FileComparation\\STATICPATTERN.pdf";
        String dynamicPdfPath = "C:\\Users\\User\\Desktop\\FileComparation\\ValidPattern.pdf";
        String reportPath = "C:\\Users\\User\\Downloads\\pdf_structure_report.txt";

        try {
            comparePDFs(staticPdfPath, dynamicPdfPath, reportPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void comparePDFs(String staticPdfPath, String dynamicPdfPath, String reportPath) throws Exception {
        StringBuilder report = new StringBuilder();
        boolean allMatch = true;

        // Extract text from both PDFs
        String staticText = extractTextFromPDF(staticPdfPath);
        String dynamicText = extractTextFromPDF(dynamicPdfPath);

        report.append("🔍 Generic PDF Comparison (Regex-based)\n\n");

        // Split into lines and iterate through static template
        String[] staticLines = staticText.split("\\r?\\n");

        for (String staticLine : staticLines) {
            staticLine = staticLine.trim();
            if (staticLine.isEmpty()) continue;

            // Detect placeholders or patterns
            Matcher placeholderMatcher = Pattern.compile("\\[(.*?)\\]").matcher(staticLine);
            if (placeholderMatcher.find()) {
                String placeholder = placeholderMatcher.group(1);
                String fieldLabel = staticLine.replaceAll("\\[.*?\\]", "").trim();

                // Check if placeholder contains a regex-like pattern (e.g., [A-Z]{5}[0-9]{4}[A-Z]{1})
                String regexPattern = detectPattern(placeholder);

                // Build a dynamic pattern to search the value in the dynamic PDF
                String searchRegex = Pattern.quote(fieldLabel) + "\\s*:?\\s*(.+)";
                Pattern pattern = Pattern.compile(searchRegex, Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(dynamicText);

                if (matcher.find() && matcher.groupCount() >= 1) {
                    String value = safeGroup(matcher, 1);
                    if (value.matches(regexPattern)) {
                        report.append("✅ ").append(fieldLabel)
                              .append(" matches pattern ").append(regexPattern)
                              .append(" → ").append(value).append("\n");
                    } else {
                        report.append("❌ ").append(fieldLabel)
                              .append(" mismatch → ").append(value)
                              .append("  (expected pattern ").append(regexPattern).append(")\n");
                        allMatch = false;
                    }
                } else {
                    report.append("⚠️ No match for field: ").append(fieldLabel).append("\n");
                    allMatch = false;
                }
            } else {
                // Static text should match exactly
                if (dynamicText.toLowerCase().contains(staticLine.toLowerCase())) {
                    report.append("✅ Static text found: ").append(staticLine).append("\n");
                } else {
                    report.append("❌ Static text missing: ").append(staticLine).append("\n");
                    allMatch = false;
                }
            }
        }

        report.append("\n------------------------------------------\n");
        if (allMatch)
            report.append("✅ PDFs match successfully (layout & data validation passed).\n");
        else
            report.append("❌ Differences found — check details above.\n");

        try (FileWriter fw = new FileWriter(reportPath)) {
            fw.write(report.toString());
        }

        System.out.println("✅ Comparison complete. Report saved at:\n" + reportPath);
    }

    // Extract PDF text
    private static String extractTextFromPDF(String filePath) throws Exception {
        PDDocument doc = PDDocument.load(new File(filePath));
        PDFTextStripper stripper = new PDFTextStripper();
        String text = stripper.getText(doc);
        doc.close();
        return text;
    }

    // Detects regex pattern type from placeholder
    private static String detectPattern(String placeholder) {
        // Predefined generic patterns
        Map<String, String> patterns = new HashMap<>();
//        patterns.put("CLIENT_NAME", "^[A-Za-z\\.]{1,15}$");
//        patterns.put("CLIENT_ADDRESS", "^[A-Za-z0-9\\s,./#@\\-]{1,50}$");
//        patterns.put("EMAIL", "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
//        patterns.put("PHONE", "^(\\+91[- ]?)?\\d{10}$");
//        patterns.put("INVOICE_NUMBER", "^[A-Z]{5}[0-9]{4}[A-Z]{1}$");
//        patterns.put("DATE", "^\\d{2}-\\d{2}-\\d{4}$");
//        patterns.put("AMOUNT", "^\\d+(\\.\\d{1,2})?$");
//        patterns.put("IFSC_CODE", "^[A-Z]{4}0[A-Z0-9]{6}$");
//        patterns.put("ACCOUNT_NO", "^\\d{9,18}$");
//        patterns.put("UPI_ID", "^[a-zA-Z0-9\\.\\-_]{2,256}@[a-zA-Z]{2,64}$");

        patterns.put("NAME", "^[A-Za-z\\.\\s]{1,20}$");
        patterns.put("AGE", "^(1[89]|[2-9][0-9])$");
        patterns.put("COMPANY_NAME", "^[A-Za-z\\s]{2,30}$");
        patterns.put("ROLE", "^[A-Za-z\\s]{2,30}$");
        patterns.put("SALARY", "^\\d{4,6}$");
        // If placeholder looks like a regex itself, return it directly
//        if (placeholder.matches(".*\\{.*\\}.*"))
//            return placeholder;

        // Otherwise, check by name
        for (String key : patterns.keySet()) {
            if (placeholder.toUpperCase().contains(key))
                return patterns.get(key);
        }
        return ".*"; // Default: any value
    }

    private static String safeGroup(Matcher m, int group) {
        try {
            if (m.groupCount() >= group && m.group(group) != null)
                return m.group(group).trim();
        } catch (Exception ignored) {}
        return "";
    }
}
