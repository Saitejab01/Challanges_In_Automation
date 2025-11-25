package captcha;
import java.io.File;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
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
public class AutomationOfSliderCaptcha {
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
                double centerX = bestRect.x + bestRect.width / 2.0;
                return centerX;
            }
        } catch (Exception e) {
        }
        return Double.NaN;
    }
    private static class MatchResult {
        final double x;
        final double confidence;
        MatchResult(double x, double confidence) { this.x = x; this.confidence = confidence; }
    }
    private static MatchResult templateMatchX(Mat bgGray, Mat pieceGray) {
        Mat result = new Mat(
                bgGray.rows() - pieceGray.rows() + 1,
                bgGray.cols() - pieceGray.cols() + 1,
                CvType.CV_32FC1
        );
        Imgproc.matchTemplate(bgGray, pieceGray, result, Imgproc.TM_CCOEFF_NORMED);
        Core.MinMaxLocResult mmr = Core.minMaxLoc(result);
        double matchX = mmr.maxLoc.x;
        double confidence = mmr.maxVal;
        return new MatchResult(matchX + (pieceGray.cols() / 2.0), confidence);
    }
    public static void solveSliderCaptcha(WebDriver driver) throws Exception {
        driver.findElement(By.xpath("//button[.='Submit']")).click();
        Thread.sleep(2500);
        String bgPath = downloadImage(driver, "background-image", "BI.png");
        String piecePath = downloadImage(driver, "puzzle-piece", "PI.png");
        OpenCV.loadLocally();

        Mat bg = Imgcodecs.imread(bgPath);
        Mat piece = Imgcodecs.imread(piecePath);

        if (bg == null || bg.empty() || piece == null || piece.empty()) {
            throw new RuntimeException("Failed to load images from disk.");
        }

        Mat bgGray = new Mat();
        Mat pieceGray = new Mat();
        Imgproc.cvtColor(bg, bgGray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.cvtColor(piece, pieceGray, Imgproc.COLOR_BGR2GRAY);

        double gapCenterFromMask = detectGapCenterX(bg, piece, bgGray, pieceGray);

        MatchResult tm = templateMatchX(bgGray, pieceGray);

        double chosenGapCenterPx;
        double tmConfidence = tm.confidence;
        boolean maskOK = !Double.isNaN(gapCenterFromMask);

        if (maskOK) {
            if (tmConfidence >= 0.85) {
                chosenGapCenterPx = (tm.x + gapCenterFromMask) / 2.0;
            } else {
                chosenGapCenterPx = gapCenterFromMask;
            }
        } else {
            chosenGapCenterPx = tm.x;
        }
        WebElement bgDom = driver.findElement(By.id("background-image"));
        WebElement puzzleDom = driver.findElement(By.id("puzzle-piece"));
        WebElement sliderHandle = driver.findElement(By.id("slider-handle"));
        Rectangle bgRect = bgDom.getRect();
        int bgStartX = bgRect.x;
        int bgWidthOnScreen = bgRect.width;
        double imageWidthPx = (double) bgGray.width();
        double scale = bgWidthOnScreen / imageWidthPx;

        int targetScreenX = bgStartX + (int) Math.round(chosenGapCenterPx * scale);
        Rectangle pieceRect = puzzleDom.getRect();
        int pieceCenterScreenX = pieceRect.x + pieceRect.width / 2;
        int remainingInitial = targetScreenX - pieceCenterScreenX;
        System.out.println("chosenGapCenterPx (img px): " + chosenGapCenterPx);
        System.out.println("tmConfidence: " + tmConfidence);
        System.out.println("bgStartX: " + bgStartX + ", scale: " + scale);
        System.out.println("targetScreenX: " + targetScreenX);
        System.out.println("pieceCenterScreenX: " + pieceCenterScreenX + ", remainingInitial: " + remainingInitial);

        PointerInput mouse = new PointerInput(PointerInput.Kind.MOUSE, "mouse");
        RemoteWebDriver rwd = (RemoteWebDriver) driver;

        Sequence start = new Sequence(mouse, 0);
        start.addAction(mouse.createPointerMove(Duration.ofMillis(0),
                PointerInput.Origin.fromElement(sliderHandle), 0, 0));
        start.addAction(mouse.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        rwd.perform(Collections.singletonList(start));
        int maxIterations = 120;
        int stableCount = 0;
        int lastAbsRemaining = Integer.MAX_VALUE;
        int sign = remainingInitial >= 0 ? 1 : -1;

        for (int iter = 0; iter < maxIterations; iter++) {
            Rectangle currentPieceRect = puzzleDom.getRect();
            int pieceCenter = currentPieceRect.x + currentPieceRect.width / 2;
            int remaining = targetScreenX - pieceCenter;

            int absRemaining = Math.abs(remaining);

            if (absRemaining <= 2) {
                stableCount++;
                if (stableCount >= 4) {
                    System.out.println("Aligned with tolerance after iter " + iter + " rem=" + remaining);
                    break;
                }
            } else {
                stableCount = 0;
            }
            int desiredSign = remaining >= 0 ? 1 : -1;
            if (desiredSign != sign) {
                int microStep = sign * 1;
                Sequence move = new Sequence(mouse, 1);
                move.addAction(mouse.createPointerMove(
                        Duration.ofMillis(40),
                        PointerInput.Origin.pointer(),
                        microStep,
                        0
                ));
                rwd.perform(Collections.singletonList(move));
                Thread.sleep(45);
                continue;
            }
            int step = (int) Math.round(remaining * 0.55);
            if (Math.abs(step) > 20) step = (step > 0) ? 20 : -20;
            if (Math.abs(step) < 1) step = (remaining > 0) ? 1 : -1;
            if (Math.abs(step) > Math.abs(remaining)) step = remaining;

            Sequence move = new Sequence(mouse, 1);
            move.addAction(mouse.createPointerMove(
                    Duration.ofMillis(35 + Math.min(60, Math.abs(step) * 2)),
                    PointerInput.Origin.pointer(),
                    step,
                    0
            ));
            rwd.perform(Collections.singletonList(move));

            Thread.sleep(50);

            if (absRemaining >= lastAbsRemaining) {
                int micro = (remaining > 0) ? 1 : -1;
                Sequence microMove = new Sequence(mouse, 2);
                microMove.addAction(mouse.createPointerMove(
                        Duration.ofMillis(20),
                        PointerInput.Origin.pointer(),
                        micro,
                        0
                ));
                rwd.perform(Collections.singletonList(microMove));
                Thread.sleep(40);
            }

            lastAbsRemaining = Math.abs(remaining);
        }
        for (int finalIter = 0; finalIter < 20; finalIter++) {
            Rectangle pr = puzzleDom.getRect();
            int pc = pr.x + pr.width / 2;
            int rem = targetScreenX - pc;
            if (Math.abs(rem) <= 1) break;
            int micro = rem > 0 ? 1 : -1;
            Sequence microMove = new Sequence(mouse, 10 + finalIter);
            microMove.addAction(mouse.createPointerMove(Duration.ofMillis(20),
                    PointerInput.Origin.pointer(),
                    micro,
                    0));
            rwd.perform(Collections.singletonList(microMove));
            Thread.sleep(30);
        }

        Sequence end = new Sequence(mouse, 1000);
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
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            Thread.sleep(3000);
            driver.quit();
        }
    }
}