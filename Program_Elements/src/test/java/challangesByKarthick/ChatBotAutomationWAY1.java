package challangesByKarthick;

import info.debatty.java.stringsimilarity.Cosine;
import info.debatty.java.stringsimilarity.Jaccard;

import java.time.Duration;
import java.util.*;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.edge.EdgeDriver;

public class ChatBotAutomationWAY1 {

    private static final Cosine cosine = new Cosine();
    private static final Jaccard jaccard = new Jaccard();

    // MAIN METHOD FOR AUTOMATION SCRIPTS
    public static void main(String[] args) {
//		WebDriver driver = new EdgeDriver();
//		driver.manage().window().maximize();
//		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
    	String resp1="AI (Artificial Intelligence) is the field of computer science that focuses on creating machines that can think, learn, and make decisions similar to humans.\r\n"
    			+ "\r\n"
    			+ "In simple terms:\r\n"
    			+ "\r\n"
    			+ "AI = Making computers smart.\r\n"
    			+ "\r\n"
    			+ "What AI can do:\r\n"
    			+ "\r\n"
    			+ "Understand language\r\n"
    			+ "\r\n"
    			+ "Recognize images or faces\r\n"
    			+ "\r\n"
    			+ "Learn from data\r\n"
    			+ "\r\n"
    			+ "Solve problems\r\n"
    			+ "\r\n"
    			+ "Make predictions\r\n"
    			+ "\r\n"
    			+ "Chat like a human (e.g., ChatGPT)\r\n"
    			+ "\r\n"
    			+ "Types of AI:\r\n"
    			+ "\r\n"
    			+ "Narrow AI – designed for specific tasks (Siri, Google Assistant, chatbots).\r\n"
    			+ "\r\n"
    			+ "General AI – hypothetical future AI that can think like a human in every task.\r\n"
    			+ "\r\n"
    			+ "Superintelligent AI – a level beyond human intelligence (not yet real).\r\n"
    			+ "\r\n"
    			+ "Real-world examples:\r\n"
    			+ "\r\n"
    			+ "Chatbots\r\n"
    			+ "\r\n"
    			+ "Self-driving cars\r\n"
    			+ "\r\n"
    			+ "Recommendation systems (Netflix, YouTube)\r\n"
    			+ "\r\n"
    			+ "Fraud detection\r\n"
    			+ "\r\n"
    			+ "Medical diagnosis\r\n"
    			+ "\r\n"
    			+ "If you want, I can also explain AI for an interview answer or short definition.";
    	String resp2 = "AI (Artificial Intelligence) is a branch of computer science that focuses on creating machines or software that can think, learn, and make decisions like humans.\r\n"
    			+ "\r\n"
    			+ "🔍 Simple Definition\r\n"
    			+ "\r\n"
    			+ "AI = Computer systems that can understand, learn, and solve problems the way humans do.\r\n"
    			+ "\r\n"
    			+ "💡 What AI Can Do\r\n"
    			+ "\r\n"
    			+ "Understand language (NLP)\r\n"
    			+ "\r\n"
    			+ "Recognize images & objects\r\n"
    			+ "\r\n"
    			+ "Learn from data (Machine Learning)\r\n"
    			+ "\r\n"
    			+ "Make predictions\r\n"
    			+ "\r\n"
    			+ "Plan and make decisions\r\n"
    			+ "\r\n"
    			+ "Talk and respond like a human (Chatbots)\r\n"
    			+ "\r\n"
    			+ "🧠 How AI Works\r\n"
    			+ "\r\n"
    			+ "AI learns patterns from data → uses those patterns to give predictions or decisions.\r\n"
    			+ "\r\n"
    			+ "🔥 Examples in Real Life\r\n"
    			+ "\r\n"
    			+ "ChatGPT\r\n"
    			+ "\r\n"
    			+ "Google Assistant / Siri\r\n"
    			+ "\r\n"
    			+ "Self-driving cars\r\n"
    			+ "\r\n"
    			+ "YouTube / Netflix recommendations\r\n"
    			+ "\r\n"
    			+ "Spam detection in email\r\n"
    			+ "\r\n"
    			+ "If you want, I can also give:\r\n"
    			+ "✔ 10-word answer\r\n"
    			+ "✔ Interview answer\r\n"
    			+ "✔ One-line definition\r\n"
    			+ "✔ Technical definition\r\n"
    			+ "✔ Real-time examples for automation engineers";
    	System.out.println(getContentAccuracy(resp1,resp2));
		
	}
    public static double getContentAccuracy(String text1, String text2) {

        double cosineScore = cosineSimilarity(text1, text2);
        double jaccardScore = jaccardSimilarity(text1, text2);
        double keywordScore = keywordAccuracy(text1, text2);

        // Weighted average → most weight to content meaning
        return (cosineScore * 0.5) + (jaccardScore * 0.25) + (keywordScore * 0.25);
    }

    // ------------------ PART 1: Cosine (semantic) ------------------
    private static double cosineSimilarity(String a, String b) {
        return cosine.similarity(clean(a), clean(b)) * 100;
    }

    // ------------------ PART 2: Jaccard (unique content) ------------------
    private static double jaccardSimilarity(String a, String b) {
        return jaccard.similarity(clean(a), clean(b)) * 100;
    }

    // ------------------ PART 3: Keyword overlap ------------------
    private static double keywordAccuracy(String a, String b) {
        Set<String> wordsA = new HashSet<>(Arrays.asList(clean(a).split(" ")));
        Set<String> wordsB = new HashSet<>(Arrays.asList(clean(b).split(" ")));

        int match = 0;
        for (String w : wordsA) {
            if (wordsB.contains(w)) match++;
        }

        return (double) match / wordsA.size() * 100;
    }

    // ------------------ CLEAN TEXT: remove grammar ------------------
    private static String clean(String input) {
        return input
                .toLowerCase()
                .replaceAll("[^a-z0-9 ]", "")   // remove grammar, punctuation
                .replaceAll("\\s+", " ")
                .trim();
    }
}

