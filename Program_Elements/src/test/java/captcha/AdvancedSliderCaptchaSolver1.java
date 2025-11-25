package captcha;

import java.io.File;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.openqa.selenium.By;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;
import org.openqa.selenium.remote.RemoteWebDriver;

import io.github.bonigarcia.wdm.WebDriverManager;
import nu.pattern.OpenCV;

public class AdvancedSliderCaptchaSolver1 {

    public static String downloadImage(WebDriver driver, String id, String saveToPath) throws Exception {
        WebElement imgElement = driver.findElement(By.id(id));
        String imageSrc = imgElement.getAttribute("src");
        File destination = new File(saveToPath);
        if (imageSrc.startsWith("http")) {
            URL url = new URL(imageSrc);
            FileUtils.copyURLToFile(url, destination);
        } else if (imageSrc.startsWith("data:image")) {
            String base64Data = imageSrc.substring(imageSrc.indexOf(",") + 1);
            byte[] imageBytes = Base64.getDecoder().decode(base64Data);
            FileUtils.writeByteArrayToFile(destination, imageBytes);
        }
        return destination.getAbsolutePath();
    }

    private static double detectGapCenterX(Mat bg, Mat piece, Mat bgGray, Mat pieceGray) {
        try {
            Mat thresh = new Mat();
            Imgproc.threshold(bgGray, thresh, 230, 255, Imgproc.THRESH_BINARY);
            Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
            Imgproc.morphologyEx(thresh, thresh, Imgproc.MORPH_OPEN, kernel);

            List<MatOfPoint> contours = new ArrayList<>();
            Mat hierarchy = new Mat();
            Imgproc.findContours(thresh, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

            double pieceArea = pieceGray.rows() * pieceGray.cols();
            double bestScore = Double.NEGATIVE_INFINITY;
            Rect bestRect = null;

            for (MatOfPoint mp : contours) {
                Rect r = Imgproc.boundingRect(mp);
                double area = r.width * r.height;
                if (area < pieceArea * 0.05) continue;
                double areaRatio = Math.min(area / pieceArea, pieceArea / area);
                double widthRatio = Math.min((double) r.width / pieceGray.cols(), (double) pieceGray.cols() / r.width);
                double score = areaRatio * 0.6 + widthRatio * 0.4;
                if (score > bestScore) {
                    bestScore = score;
                    bestRect = r;
                }
            }

            if (bestRect != null) {
                return bestRect.x + bestRect.width / 2.0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Double.NaN;
    }

    private static class MatchResult {
        final double x;
        final double confidence;

        MatchResult(double x, double confidence) {
            this.x = x;
            this.confidence = confidence;
        }
    }

    private static MatchResult templateMatchX(Mat bgGray, Mat pieceGray) {
        Mat result = new Mat(
                bgGray.rows() - pieceGray.rows() + 1,
                bgGray.cols() - pieceGray.cols() + 1,
                CvType.CV_32FC1
        );
        Imgproc.matchTemplate(bgGray, pieceGray, result, Imgproc.TM_CCOEFF_NORMED);

        // Find max location manually without Core.MinMaxLocResult
        double maxVal = Double.NEGATIVE_INFINITY;
        Point maxLoc = new Point(0, 0);
        for (int y = 0; y < result.rows(); y++) {
            for (int x = 0; x < result.cols(); x++) {
                double[] val = result.get(y, x);
                if (val[0] > maxVal) {
                    maxVal = val[0];
                    maxLoc = new Point(x, y);
                }
            }
        }

        return new MatchResult(maxLoc.x + pieceGray.cols() / 2.0, maxVal);
    }

    public static void solveSliderCaptcha(WebDriver driver) throws Exception {
        driver.findElement(By.xpath("//button[.='Submit']")).click();
        Thread.sleep(2500);

        String bgPath = downloadImage(driver, "background-image", "BI.png");
        String piecePath = downloadImage(driver, "puzzle-piece", "PI.png");

        OpenCV.loadLocally(); // load OpenCV native

        Mat bg = Imgcodecs.imread(bgPath);
        Mat piece = Imgcodecs.imread(piecePath);

        if (bg.empty() || piece.empty()) {
            throw new RuntimeException("Failed to load images.");
        }

        Mat bgGray = new Mat();
        Mat pieceGray = new Mat();
        Imgproc.cvtColor(bg, bgGray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.cvtColor(piece, pieceGray, Imgproc.COLOR_BGR2GRAY);

        double maskX = detectGapCenterX(bg, piece, bgGray, pieceGray);
        MatchResult tm = templateMatchX(bgGray, pieceGray);

        double chosenX = !Double.isNaN(maskX) ? (tm.confidence >= 0.85 ? (tm.x + maskX) / 2.0 : maskX) : tm.x;

        WebElement bgDom = driver.findElement(By.id("background-image"));
        WebElement puzzleDom = driver.findElement(By.id("puzzle-piece"));
        WebElement sliderHandle = driver.findElement(By.id("slider-handle"));

        Rectangle bgRect = bgDom.getRect();
        double scale = (double) bgRect.width / bgGray.width();
        int targetX = bgRect.x + (int) Math.round(chosenX * scale);

        PointerInput mouse = new PointerInput(PointerInput.Kind.MOUSE, "mouse");
        RemoteWebDriver rwd = (RemoteWebDriver) driver;

        // Start dragging
        Sequence start = new Sequence(mouse, 0);
        start.addAction(mouse.createPointerMove(Duration.ofMillis(0), PointerInput.Origin.fromElement(sliderHandle), 0, 0));
        start.addAction(mouse.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        rwd.perform(Collections.singletonList(start));
        Thread.sleep(200);

        // Drag loop with adaptive slowdown
        for (int i = 0; i < 150; i++) {
            Rectangle pr = puzzleDom.getRect();
            int pc = pr.x + pr.width / 2;
            int remaining = targetX - pc;
            if (Math.abs(remaining) <= 1) break;

            int step = (int) Math.round(remaining * 0.5);
            if (Math.abs(step) > 15) step = remaining > 0 ? 15 : -15;
            if (Math.abs(step) < 1) step = remaining > 0 ? 1 : -1;
            if (Math.abs(step) > Math.abs(remaining)) step = remaining;

            Sequence move = new Sequence(mouse, i + 1);
            move.addAction(mouse.createPointerMove(Duration.ofMillis(20 + Math.abs(step)), PointerInput.Origin.pointer(), step, 0));
            rwd.perform(Collections.singletonList(move));
            Thread.sleep(15);
        }

        // Micro-adjust loop for pixel-perfect alignment
        for (int i = 0; i < 30; i++) {
            Rectangle pr = puzzleDom.getRect();
            int pc = pr.x + pr.width / 2;
            int remaining = targetX - pc;
            if (Math.abs(remaining) <= 0) break;

            int microStep = remaining > 0 ? 1 : -1;
            if (Math.abs(microStep) > Math.abs(remaining)) microStep = remaining;

            Sequence microMove = new Sequence(mouse, 1000 + i);
            microMove.addAction(mouse.createPointerMove(Duration.ofMillis(15), PointerInput.Origin.pointer(), microStep, 0));
            rwd.perform(Collections.singletonList(microMove));
            Thread.sleep(10);
        }

        // Release slider
        Sequence end = new Sequence(mouse, 2000);
        end.addAction(mouse.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        rwd.perform(Collections.singletonList(end));
        Thread.sleep(1000);
    }

    public static void main(String[] args) throws Exception {
        WebDriverManager.edgedriver().setup();
        WebDriver driver = new EdgeDriver();
        driver.manage().window().maximize();
        driver.get("https://slidercaptcha.com/?utm_source=chatgpt.com");
        try {
            solveSliderCaptcha(driver);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Thread.sleep(3000);
            driver.quit();
        }
    }
}
