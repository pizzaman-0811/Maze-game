package persistence;

/**
 * Interface that represents writable behavior.
 * Classes that implement this interface can be converted to JSON format for saving.
 */
import org.json.JSONObject;

/**
 * EFFECTS: returns this as JSON object
 */
public interface Writable {
    JSONObject toJson();
}
