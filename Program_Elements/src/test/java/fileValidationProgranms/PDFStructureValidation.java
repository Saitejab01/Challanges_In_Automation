package fileValidationProgranms;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import java.io.*;

/**
 * This program compares the structure (layout and headers) of two PDF files.
 * One PDF is a static "template" and the other is a "generated" or "dynamic" file.
 *
 * It does NOT check actual values — only verifies that all the required
 * text, labels, and headers appear in the correct structure.
 *
 * Example:
 * Static PDF (Template):
 *   Company Name: [YOUR COMPANY NAME]
 *   Address: [ADDRESS LINE]
 *
 * Dynamic PDF:
 *   Company Name: SaiTech Solutions
 *   Address: Hyderabad
 *
 * Output:
 *   Field/Header Found: Company Name:
 *   Field/Header Found: Address:
 *   Same: All headers and structure matched.
 */
public class PDFStructureValidation {

    public static void main(String[] args) {
        // Step 1: Provide full paths to the static and dynamic PDF files.
        // The "static" file is the original format or design.
        // The "dynamic" file is the generated invoice or document you want to verify.
        String staticPdfPath = "C:/Users/User/Downloads/static_invoice_template.pdf";
        String dynamicPdfPath = "C:/Users/User/Downloads/generated_invoice_valid.pdf";

        try {
            // Step 2: Compare both PDFs and store the result.
            boolean result = compareStructure(staticPdfPath, dynamicPdfPath);

            // Step 3: Print the final outcome based on comparison results.
            if (result)
                System.out.println("\nSame: All headers and structure matched.");
            else
                System.out.println("\nNot Same: Some structure headers are missing or mismatched.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method compares the structure of a static and dynamic PDF.
     * It checks whether all field labels and headers from the static template
     * are present in the dynamic PDF.
     *
     * Example:
     * If the static file has:
     *   "Company Name: [YOUR COMPANY NAME]"
     * and the dynamic file has:
     *   "Company Name: SaiTech Solutions"
     * Then this method will confirm that the label "Company Name:" exists.
     *
     * @param staticPdfPath  Full path to the static (template) PDF
     * @param dynamicPdfPath Full path to the dynamic (generated) PDF
     * @return true if structure matches, false otherwise
     * @throws Exception If PDF cannot be read or processed
     */
    public static boolean compareStructure(String staticPdfPath, String dynamicPdfPath) throws Exception {
        boolean allMatch = true;

        // Step 1: Extract text from both PDF files.
        String staticText = extractTextFromPDF(staticPdfPath);
        String dynamicText = extractTextFromPDF(dynamicPdfPath);

        // Step 2: Split static file into lines so we can compare one by one.
        String[] staticLines = staticText.split("\\r?\\n");

        // Step 3: Simplify the dynamic text (remove extra spaces and make lowercase)
        // This makes matching more reliable even if formatting is slightly different.
        dynamicText = dynamicText.replaceAll("\\s+", " ").toLowerCase();

        System.out.println("Comparing Static vs Dynamic Structure...\n");

        // Step 4: Loop through each line of the static file.
        for (String line : staticLines) {
            line = line.trim(); // remove leading/trailing spaces
            if (line.isEmpty()) continue; // skip blank lines

            // Step 5: Ignore pure numeric lines (like table row numbers 1, 2, 3, etc.)
            if (line.matches("^\\d+$")) {
                continue;
            }

            // Step 6: Detect lines that contain placeholders (like [CLIENT_NAME])
            if (line.contains("[")) {
                // Remove placeholders to get the  label only (e.g., "Company Name:")
                String fieldLabel = line.replaceAll("\\[.*?\\]", "").trim();

                // Check if this label appears in the dynamic PDF
                if (!dynamicText.contains(fieldLabel.toLowerCase())) {
                    System.out.println("Missing Field/Header: " + fieldLabel);
                    allMatch = false;
                } else {
                    System.out.println("Field/Header Found: " + fieldLabel);
                }
            } else {
                // Step 7: If the line doesn’t contain placeholders,
                // it’s static text or a table header (like "S.No Description Quantity Unit Price Total")
                if (!dynamicText.contains(line.toLowerCase())) {
                    System.out.println("Static Text Missing: " + line);
                    allMatch = false;
                } else {
                    System.out.println("Static Text Found: " + line);
                }
            }
        }

        // Step 8: Return true if everything matched, false if any structure is missing
        return allMatch;
    }

    /**
     * This method extracts all readable text from a given PDF file.
     * It uses Apache PDFBox to open and read the file.
     *
     * Example:
     * PDF content:
     *   Company Name: SaiTech Solutions
     *   Address: Hyderabad
     *
     * After extraction, it returns the text:
     *   "Company Name: SaiTech Solutions\nAddress: Hyderabad"
     *
     * @param filePath Full path of the PDF file to extract text from
     * @return All the text content of the PDF as a single string
     * @throws Exception If PDF cannot be loaded or read
     */
    private static String extractTextFromPDF(String filePath) throws Exception {
        // Step 1: Open the PDF file from the given path.
        PDDocument doc = PDDocument.load(new File(filePath));

        // Step 2: Create a PDFTextStripper to extract text content.
        PDFTextStripper stripper = new PDFTextStripper();

        // Step 3: Extract all text from the PDF.
        String text = stripper.getText(doc);

        // Step 4: Close the document to release memory.
        doc.close();

        // Step 5: Return the extracted text as a plain string.
        return text;
    }
}
