package challangesByKarthick;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.util.*;

public class ChatBotAutomationWAY2 {

    // Clean + normalize text
    private static String clean(String text) {
        return text.toLowerCase()
                .replaceAll("[^a-zA-Z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    // Tokenize with Lucene and remove stopwords
    private static List<String> tokenize(String text) throws IOException {
        Analyzer analyzer = new EnglishAnalyzer();
        TokenStream stream = analyzer.tokenStream(null, text);
        CharTermAttribute attr = stream.addAttribute(CharTermAttribute.class);

        List<String> tokens = new ArrayList<>();

        stream.reset();
        while (stream.incrementToken()) {
            tokens.add(attr.toString());
        }
        stream.end();
        stream.close();

        return tokens;
    }

    // Convert tokens into term frequency (TF) vector
    private static Map<String, Double> getTF(List<String> tokens) {
        Map<String, Double> tf = new HashMap<>();
        for (String token : tokens) {
            tf.put(token, tf.getOrDefault(token, 0.0) + 1.0);
        }
        return tf;
    }

    // Cosine similarity (0–1)
    private static double cosine(Map<String, Double> v1, Map<String, Double> v2) {
        Set<String> allWords = new HashSet<>();
        allWords.addAll(v1.keySet());
        allWords.addAll(v2.keySet());

        double dot = 0.0, mag1 = 0.0, mag2 = 0.0;

        for (String w : allWords) {
            double a = v1.getOrDefault(w, 0.0);
            double b = v2.getOrDefault(w, 0.0);

            dot += a * b;
            mag1 += a * a;
            mag2 += b * b;
        }

        if (mag1 == 0 || mag2 == 0) return 0.0;
        return dot / (Math.sqrt(mag1) * Math.sqrt(mag2));
    }

    // ===============================
    // ⭐ PUBLIC METHOD TO CALL ⭐
    // ===============================
    public static double getAccuracy(String response1, String response2) {
        try {
            String r1 = clean(response1);
            String r2 = clean(response2);

            List<String> t1 = tokenize(r1);
            List<String> t2 = tokenize(r2);

            Map<String, Double> tf1 = getTF(t1);
            Map<String, Double> tf2 = getTF(t2);

            double cosineValue = cosine(tf1, tf2);

            return cosineValue * 100; // Accuracy %
        }
        catch (Exception e) {
            e.printStackTrace();
            return 0.0;
        }
    }
    
    public static void main(String[] args) {
    	
    	String text3 = "Machine Learning is a subset of Artificial Intelligence that enables systems to learn from data and improve their performance over time without being explicitly programmed. It involves algorithms that identify patterns and make predictions based on input data.";

    	String text4 = "ML, or Machine Learning, is part of AI which allows computers to learn from information and enhance their capabilities over time automatically. It uses algorithms to detect patterns and predict outcomes from the given data.";

    	double accuracy = getAccuracy(text3, text4);
    	System.out.println("Content Accuracy = " + accuracy + "%");
	}
}
