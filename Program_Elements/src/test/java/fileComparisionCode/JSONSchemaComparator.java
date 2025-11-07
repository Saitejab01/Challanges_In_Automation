package fileComparisionCode;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Set;

public class JSONSchemaComparator {

    public static void main(String[] args) {
        // Give full paths for your static and dynamic JSON files
        String staticJsonPath = "C:\\Users\\User\\Desktop\\jsonFiles\\static.json";
        String dynamicJsonPath = "C:\\Users\\User\\Desktop\\jsonFiles\\dynamic.json";

        try {
            JSONObject staticJson = new JSONObject(new JSONTokener(new FileInputStream(staticJsonPath)));
            JSONObject dynamicJson = new JSONObject(new JSONTokener(new FileInputStream(dynamicJsonPath)));

            System.out.println("🔍 Comparing JSON schemas...\n");

            boolean result = compareJsonSchemas(staticJson, dynamicJson, "");

            if (result) {
                System.out.println("✅ Both JSON schemas match perfectly!");
            }

        } catch (FileNotFoundException e) {
            System.out.println("❌ JSON file not found: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("⚠️ Error while comparing JSONs: " + e.getMessage());
        }
    }

    // Recursive method to compare keys of two JSON objects
    public static boolean compareJsonSchemas(JSONObject staticJson, JSONObject dynamicJson, String path) {
        boolean isMatch = true;
        Set<String> staticKeys = staticJson.keySet();
        Set<String> dynamicKeys = dynamicJson.keySet();

        // Check for missing keys in dynamic JSON
        for (String key : staticKeys) {
            String currentPath = path.isEmpty() ? key : path + "." + key;
            if (!dynamicKeys.contains(key)) {
                System.out.println("❌ Missing key in dynamic JSON: " + currentPath);
                isMatch = false;
            } else {
                Object staticVal = staticJson.get(key);
                Object dynamicVal = dynamicJson.get(key);

                // Recurse into nested structures
                if (staticVal instanceof JSONObject && dynamicVal instanceof JSONObject) {
                    boolean nestedMatch = compareJsonSchemas((JSONObject) staticVal, (JSONObject) dynamicVal, currentPath);
                    if (!nestedMatch) isMatch = false;
                } else if (staticVal instanceof JSONArray && dynamicVal instanceof JSONArray) {
                    boolean nestedMatch = compareJsonArrays((JSONArray) staticVal, (JSONArray) dynamicVal, currentPath);
                    if (!nestedMatch) isMatch = false;
                }
            }
        }

        // Check for extra keys in dynamic JSON
        for (String key : dynamicKeys) {
            String currentPath = path.isEmpty() ? key : path + "." + key;
            if (!staticKeys.contains(key)) {
                System.out.println("⚠️ Extra key in dynamic JSON: " + currentPath);
                isMatch = false;
            }
        }

        return isMatch;
    }

    // Compare array schemas (check type and internal structure)
    public static boolean compareJsonArrays(JSONArray staticArr, JSONArray dynamicArr, String path) {
        if (staticArr.isEmpty() || dynamicArr.isEmpty()) return true;

        Object staticElem = staticArr.get(0);
        Object dynamicElem = dynamicArr.get(0);
        boolean isMatch = true;

        if (staticElem instanceof JSONObject && dynamicElem instanceof JSONObject) {
            boolean nestedMatch = compareJsonSchemas((JSONObject) staticElem, (JSONObject) dynamicElem, path + "[]");
            if (!nestedMatch) isMatch = false;
        } else if (staticElem.getClass() != dynamicElem.getClass()) {
            System.out.println("❌ Type mismatch at " + path + "[]");
            isMatch = false;
        }

        return isMatch;
    }
}
