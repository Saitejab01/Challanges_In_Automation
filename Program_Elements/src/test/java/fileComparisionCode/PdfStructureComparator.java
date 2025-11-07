package fileComparisionCode;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import java.io.*;
import java.util.*;

public class PdfStructureComparator {

    public static void main(String[] args) {
        String staticPdfPath = "C:\\Users\\User\\Downloads\\static_invoice_template.pdf";
        String dynamicPdfPath = "C:\\Users\\User\\Downloads\\generated_invoice_valid.pdf";
        String reportPath = "C:\\Users\\User\\Downloads\\pdf_structure_report.txt";

        StringBuilder report = new StringBuilder();
        boolean structureMatch = true;

        try {
            // Extract text
            String staticText = extractTextFromPDF(staticPdfPath);
            String dynamicText = extractTextFromPDF(dynamicPdfPath);

            // Split by lines
            List<String> staticLines = cleanLines(staticText);
            List<String> dynamicLines = cleanLines(dynamicText);

            report.append("🔍 PDF Structure Comparison Report\n");
            report.append("===================================\n\n");

            report.append("📄 Static Template Lines: ").append(staticLines.size()).append("\n");
            report.append("📄 Dynamic Invoice Lines: ").append(dynamicLines.size()).append("\n\n");

            // Compare line by line up to shortest length
            int minLines = Math.min(staticLines.size(), dynamicLines.size());

            for (int i = 0; i < minLines; i++) {
                String sLine = staticLines.get(i);
                String dLine = dynamicLines.get(i);

                // Remove placeholders before comparing
                String sNormalized = sLine.replaceAll("\\[.*?\\]", "").trim();
                String dNormalized = dLine.trim();

                if (sNormalized.isEmpty()) continue;

                // Check if line label pattern matches (Invoice No:, Client Name:, etc.)
                if (sNormalized.matches(".*:.*")) {
                    String sLabel = sNormalized.split(":")[0].trim().toLowerCase();
                    String dLabel = dNormalized.contains(":") ? dNormalized.split(":")[0].trim().toLowerCase() : "";

                    if (!dLabel.equals(sLabel)) {
                        report.append("❌ Label mismatch at line ").append(i + 1).append("\n")
                              .append("   Expected: ").append(sNormalized).append("\n")
                              .append("   Found:    ").append(dNormalized).append("\n\n");
                        structureMatch = false;
                    } else {
                        report.append("✅ Label OK at line ").append(i + 1).append(": ").append(sLabel).append("\n");
                    }
                } else {
                    // For lines without colons, compare text type
                    if (dNormalized.length() < 2 && !sNormalized.isEmpty()) {
                        report.append("⚠️ Possible missing structure line at ").append(i + 1)
                              .append(": ").append(sNormalized).append("\n");
                        structureMatch = false;
                    }
                }
            }

            // Check for extra lines
            if (dynamicLines.size() > staticLines.size()) {
                report.append("\n⚠️ Extra lines in dynamic PDF (possible data or layout shift):\n");
                for (int j = staticLines.size(); j < dynamicLines.size(); j++) {
                    report.append("   + ").append(dynamicLines.get(j)).append("\n");
                }
            }

            report.append("\n------------------------------------------\n");
            if (structureMatch)
                report.append("✅ Structure Match: PDFs have consistent layout and labels.\n");
            else
                report.append("❌ Structure Differences Detected.\n");

            // Save report
            try (FileWriter fw = new FileWriter(reportPath)) {
                fw.write(report.toString());
            }

            System.out.println("✅ Structure comparison complete. Report saved at:\n" + reportPath);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =========== Utility Methods ===========

    private static String extractTextFromPDF(String filePath) throws IOException {
        try (PDDocument document = PDDocument.load(new File(filePath))) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    private static List<String> cleanLines(String text) {
        List<String> lines = new ArrayList<>();
        for (String line : text.split("\\r?\\n")) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) lines.add(trimmed);
        }
        return lines;
    }
}
