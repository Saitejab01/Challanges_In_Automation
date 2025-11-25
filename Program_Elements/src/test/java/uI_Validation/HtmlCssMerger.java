package uI_Validation;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HtmlCssMerger {

    public static void main(String[] args) {
        String pageUrl = "https://www.primevideo.com/region/eu/offers/nonprimehomepage/ref=dv_web_force_root";  // 👉 Put your URL here
        String outputPath = "C:/Users/User/Desktop/combined_page.html";

        try {
            // Step 1 → Fetch HTML
            Document doc = Jsoup.connect(pageUrl)
                    .userAgent("Mozilla/5.0")
                    .timeout(10000)
                    .get();

            // Step 2 → Extract CSS links
            Elements cssLinks = doc.select("link[rel=stylesheet]");

            StringBuilder internalCss = new StringBuilder();

            // Step 3 → Fetch external CSS content
            for (Element link : cssLinks) {
                String cssUrl = link.absUrl("href");

                if (!cssUrl.isEmpty()) {
                    System.out.println("Downloading CSS: " + cssUrl);
                    internalCss.append("/* ---- CSS FROM: ").append(cssUrl).append(" ---- */\n");
                    internalCss.append(downloadCss(cssUrl)).append("\n\n");
                }
            }

            // Step 4 → Remove external CSS <link> tags
            cssLinks.remove();

            // Step 5 → Insert internal <style> block at end of <head>
            Element head = doc.head();
            head.append("<style>\n" + internalCss.toString() + "\n</style>");

            // Step 6 → Save merged HTML file to Desktop
            File outputFile = new File(outputPath);
            FileWriter writer = new FileWriter(outputFile);
            writer.write(doc.outerHtml());
            writer.close();

            System.out.println("Merged HTML saved to: " + outputPath);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // Helper: download CSS text
    private static String downloadCss(String cssUrl) {
        StringBuilder cssContent = new StringBuilder();

        try {
            URL url = new URL(cssUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;

            while ((line = br.readLine()) != null) {
                cssContent.append(line).append("\n");
            }

            br.close();
        } catch (Exception e) {
            cssContent.append("/* Failed to load CSS: ").append(cssUrl).append(" */\n");
        }

        return cssContent.toString();
    }
}
