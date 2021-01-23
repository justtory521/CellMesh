package jp.eskmemorial.cellmesh.lib.data;

import androidx.room.TypeConverter;

import org.json.JSONArray;
import org.json.JSONException;

public class SignalLogSpeedTestResultTypeConverter {

    private static final String NULL_KEYWORD = "null";

    @TypeConverter
    public static JSONArray toJSONArray(String str) {
        if (str == NULL_KEYWORD) {
            return null;
        }
        try {
            return new JSONArray(str);
        } catch (JSONException e) {
        }
        return null;
    }

    @TypeConverter
    public static String toString(JSONArray jsonArray) {
        if (jsonArray == null) {
            return NULL_KEYWORD;
        }
        return jsonArray.toString();
    }
}
