package fileValidationProgranms;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class CompareJSONFiles {

    public static void main(String[] args) {
        // Step 1: Tell the program where the two JSON files are stored.
        // One file is the reference (static), the other is the file you want to check (dynamic).
        String staticJsonPath = "C:\\Users\\User\\Desktop\\jsonFiles\\static.json";
        String dynamicJsonPath = "C:\\Users\\User\\Desktop\\jsonFiles\\dynamic.json";

        try {
            // Step 2: Read and convert both JSON files into JSONObjects
            JSONObject staticJson = new JSONObject(new JSONTokener(new FileInputStream(staticJsonPath)));
            JSONObject dynamicJson = new JSONObject(new JSONTokener(new FileInputStream(dynamicJsonPath)));

            System.out.println("Comparing JSON structure (schema) of both files...\n");

            // Step 3: Create maps to store each JSON file’s structure
            Map<String, String> staticSchema = new LinkedHashMap<>();
            Map<String, String> dynamicSchema = new LinkedHashMap<>();

            // Step 4: Extract the structure (schema) from both JSONs
            extractSchema(staticJson, "", staticSchema);
            extractSchema(dynamicJson, "", dynamicSchema);

            // Step 5: Print out both schemas for visual comparison
            System.out.println("=== Structure of Static JSON ===");
            printSchema(staticSchema);
            System.out.println();
            System.out.println("=== Structure of Dynamic JSON ===");
            printSchema(dynamicSchema);

            // Step 6: Compare both schemas to check for missing or mismatched elements
            System.out.println();
            System.out.println("=== Comparing Both Structures ===");
            compareSchemas(staticSchema, dynamicSchema);

        } catch (FileNotFoundException e) {
            System.out.println("One of the JSON files was not found: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("There was an error while comparing the JSON files: " + e.getMessage());
        }
    }

    /**
     * Recursively extracts the "schema" of a JSON object or array.
     * The schema maps every nested key (like "user.name.first") to its data type.
     *
     * Example:
     * {
     *   "user": { "name": "John", "age": 25 },
     *   "skills": ["Java", "SQL"]
     * }
     *
     * becomes:
     * user.name : String
     * user.age  : Integer
     * skills[]  : String
     *
     * @param json The JSON object or array to analyze
     * @param path The current key path (e.g., "user.name")
     * @param schemaMap A map that stores key-path and data type pairs
     */
    public static void extractSchema(Object json, String path, Map<String, String> schemaMap) {
        if (json instanceof JSONObject) {
            // When the current object is a JSON object, go through each key
            JSONObject obj = (JSONObject) json;
            Set<String> keys = obj.keySet();
            for (String key : keys) {
                Object val = obj.get(key);
                // If this is the first level, use the key as is. Otherwise, build a full path like "user.name"
                String currentPath = path.isEmpty() ? key : path + "." + key;
                extractSchema(val, currentPath, schemaMap);
            }
        } else if (json instanceof JSONArray) {
            // When the current object is a JSON array, check its elements
            JSONArray arr = (JSONArray) json;

            // If array is empty, mark it as an empty array in schema
            if (arr.isEmpty()) {
                schemaMap.put(path + "[]", "array(empty)");
                return;
            }

            // Collect types of all elements to ensure they are consistent
            Map<String, String> combinedTypes = new LinkedHashMap<>();
            for (int i = 0; i < arr.length(); i++) {
                Object elem = arr.get(i);
                Map<String, String> elementSchema = new LinkedHashMap<>();
                extractSchema(elem, path + "[]", elementSchema);

                // Merge element types together
                for (Map.Entry<String, String> entry : elementSchema.entrySet()) {
                    String key = entry.getKey();
                    String type = entry.getValue();

                    // If two array elements have different data types, mark it as mixed
                    if (combinedTypes.containsKey(key) && !combinedTypes.get(key).equals(type)) {
                        combinedTypes.put(key, "Mixed(" + combinedTypes.get(key) + "/" + type + ")");
                    } else {
                        combinedTypes.putIfAbsent(key, type);
                    }
                }
            }

            // Add array information to the schema map
            schemaMap.putAll(combinedTypes);
        } else {
            // When we reach a simple value like a number or text, record its type (String, Integer, etc.)
            schemaMap.put(path, json == null ? "null" : json.getClass().getSimpleName());
        }
    }

    /**
     * Prints the schema (structure) in a readable format.
     * Each key and its data type will be printed like this:
     *
     * - user.name : String
     * - user.age  : Integer
     *
     * @param schema The schema map containing key-path and data type pairs
     */
    public static void printSchema(Map<String, String> schema) {
        for (Map.Entry<String, String> entry : schema.entrySet()) {
            System.out.println(" - " + entry.getKey() + " : " + entry.getValue());
        }
    }

    
    /**
     * Compares two JSON schemas (static vs dynamic) and prints the differences.
     *
     * Example:
     * If static JSON has "user.age : Integer"
     * but dynamic JSON has "user.age : String"
     * The output will say:
     * Type mismatch for key: user.age (Static: Integer, Dynamic: String)
     *
     * @param staticSchema Schema of the static (reference) JSON
     * @param dynamicSchema Schema of the dynamic (test) JSON
     */
    public static void compareSchemas(Map<String, String> staticSchema, Map<String, String> dynamicSchema) {
        boolean match = true;

        // Step 1: Check if any key in static JSON is missing or has a different type in dynamic JSON
        for (String key : staticSchema.keySet()) {
            if (!dynamicSchema.containsKey(key)) {
                System.out.println("Missing key in dynamic JSON: " + key);
                match = false;
            } else {
                String typeStatic = staticSchema.get(key);
                String typeDynamic = dynamicSchema.get(key);
                if (!typeStatic.equals(typeDynamic)) {
                    System.out.println("Type mismatch for key: " + key +
                            " (Static: " + typeStatic + ", Dynamic: " + typeDynamic + ")");
                    match = false;
                }
            }
        }

        // Step 2: Check for extra keys that are only present in the dynamic JSON
        for (String key : dynamicSchema.keySet()) {
            if (!staticSchema.containsKey(key)) {
                System.out.println("Extra key in dynamic JSON: " + key);
                match = false;
            }
        }

        // Step 3: Print final result
        if (match) {
            System.out.println("Both JSON structures match perfectly.");
        }
    }
}
