package capchaAutomation;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.time.Duration;
import java.util.Base64;
import javax.imageio.ImageIO;
import org.json.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;

public class CaptchaOCR {

    public static void main(String[] args) throws Exception {
        // 1️⃣  Launch Chrome and open page
        WebDriver driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(20));
        driver.get("https://www.careinsurance.com/get-policy-list.html");

        // Click to load captcha (if necessary)
        driver.findElement(By.xpath("//label[text()='Email ID']/preceding-sibling::label")).click();
        Thread.sleep(2000);

        // 2️⃣  Get Base64 image link from page
        JavascriptExecutor js = (JavascriptExecutor) driver;
        String base64Src = (String) js.executeScript(
                "return document.querySelector('#to-append-captcha > img').getAttribute('src');");
        System.out.println("Captured Base64 Image Source:\n" + base64Src);

        //driver.quit();

        // 3️⃣  Extract base64 data (remove prefix like 'data:image/png;base64,')
        String base64Data = base64Src.split(",")[1];

        // 4️⃣  Decode Base64 → BufferedImage
        byte[] imageBytes = Base64.getDecoder().decode(base64Data);
        BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageBytes));

        // Save original for debugging
        String originalPath = "C:\\Users\\User\\Downloads\\CaptchaOriginal.png";
        ImageIO.write(originalImage, "png", new File(originalPath));
        System.out.println("Original image saved: " + originalPath);

        // 5️⃣  Crop tightly around text
        BufferedImage cropped = cropAroundText(originalImage);

        // Save cropped image
        String croppedPath = "C:\\Users\\User\\Downloads\\CroppedCaptcha.png";
        ImageIO.write(cropped, "png", new File(croppedPath));
        System.out.println("Cropped image saved: " + croppedPath);

        // 6️⃣  Send to OCR.space
        String apiKey = "helloworld"; // Replace with your OCR.space key
        String extractedText = sendToOCRSpace(cropped, apiKey);

        System.out.println("\n Extracted Text: " + extractedText);
    }

    // 🔹 Detect text area and crop image automatically
    private static BufferedImage cropAroundText(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();

        int minX = width, minY = height, maxX = 0, maxY = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = img.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                int gray = (r + g + b) / 3;

                // Keep text areas (non-white pixels)
                if (gray < 250) {
                    if (x < minX) minX = x;
                    if (y < minY) minY = y;
                    if (x > maxX) maxX = x;
                    if (y > maxY) maxY = y;
                }
            }
        }

        // Avoid empty crop if text not found
        if (maxX <= minX || maxY <= minY) {
            System.out.println("⚠️ No significant text found. Using original image.");
            return img;
        }

        return img.getSubimage(minX, minY, maxX - minX + 1, maxY - minY + 1);
    }

    // 🔹 Send BufferedImage to OCR.space and extract text
    private static String sendToOCRSpace(BufferedImage image, String apiKey) throws Exception {
        String boundary = Long.toHexString(System.currentTimeMillis());
        String LINE_FEED = "\r\n";

        URL url = new URL("https://api.ocr.space/parse/image");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("apikey", apiKey);
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

        OutputStream output = conn.getOutputStream();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, "UTF-8"), true);

        // Send image as multipart/form-data
        writer.append("--" + boundary).append(LINE_FEED);
        writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"captcha.png\"").append(LINE_FEED);
        writer.append("Content-Type: image/png").append(LINE_FEED);
        writer.append(LINE_FEED).flush();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        baos.flush();
        byte[] imageBytes = baos.toByteArray();
        output.write(imageBytes);
        output.flush();
        baos.close();

        writer.append(LINE_FEED).flush();
        writer.append("--" + boundary + "--").append(LINE_FEED);
        writer.close();

        // Read response
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String response = in.lines().reduce("", (a, b) -> a + b);
        in.close();

        System.out.println("Raw OCR Response: " + response);

        JSONObject json = new JSONObject(response);
        if (!json.isNull("ParsedResults")) {
            return json.getJSONArray("ParsedResults")
                       .getJSONObject(0)
                       .getString("ParsedText").trim();
        } else {
            System.out.println(" No ParsedResults found.");
            return "";
        }
    }
}