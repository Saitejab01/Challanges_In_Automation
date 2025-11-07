package fileComparisionCode;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import java.io.*;
import java.util.*;
import java.util.regex.*;

public class FileComparisionNLP {
	static HashMap<String, String> patterns = new HashMap<String, String>();

    public static void main(String[] args) {
        String staticPdfPath = "C:\\Users\\User\\Desktop\\FileComparation\\STATICPATTERN.pdf";
        String dynamicPdfPath = "C:\\Users\\User\\Desktop\\FileComparation\\validPattern.pdf";
        try {
            boolean result = comparePDFs(staticPdfPath, dynamicPdfPath);
            if (result)
                System.out.println("✅ Same");
            else
                System.out.println("❌ Not Same");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean comparePDFs(String staticPdfPath, String dynamicPdfPath) throws Exception {
        boolean allMatch = true;

        String staticText = extractTextFromPDF(staticPdfPath);
        String dynamicText = extractTextFromPDF(dynamicPdfPath);

        String[] staticLines = staticText.split("\\r?\\n");

        for (String staticLine : staticLines) {
            staticLine = staticLine.trim();
            if (staticLine.isEmpty()) continue;

            Matcher placeholderMatcher = Pattern.compile("\\[(.*?)\\]").matcher(staticLine);
            if (placeholderMatcher.find()) {
                String placeholder = placeholderMatcher.group(1);
                String fieldLabel = staticLine.replaceAll("\\[.*?\\]", "").trim();
                String regexPattern = detectPattern(placeholder,patterns);

                String searchRegex = Pattern.quote(fieldLabel) + "\\s*:?\\s*(.+)";
                Pattern pattern = Pattern.compile(searchRegex, Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(dynamicText);

                if (matcher.find() && matcher.groupCount() >= 1) {
                    String value = safeGroup(matcher, 1);
                    if (!value.matches(regexPattern)) {
                        allMatch = false;
                    }
                } else {
                    allMatch = false;
                }
            } else {
                if (!dynamicText.toLowerCase().contains(staticLine.toLowerCase())) {
                    allMatch = false;
                }
            }
        }

        return allMatch;
    }

	private static String extractTextFromPDF(String filePath) throws Exception {
        PDDocument doc = PDDocument.load(new File(filePath));
        PDFTextStripper stripper = new PDFTextStripper();
        String text = stripper.getText(doc);
        doc.close();
        return text;
    }

    private static String detectPattern(String placeholder,HashMap<String, String> patterns) {
        if (placeholder.matches(".*\\{.*\\}.*"))
            return placeholder;

        for (String key : patterns.keySet()) {
            if (placeholder.toUpperCase().contains(key))
                return patterns.get(key);
        }
        return ".*";
    }

    private static String safeGroup(Matcher m, int group) {
        try {
            if (m.groupCount() >= group && m.group(group) != null)
                return m.group(group).trim();
        } catch (Exception ignored) {}
        return "";
    }
}
