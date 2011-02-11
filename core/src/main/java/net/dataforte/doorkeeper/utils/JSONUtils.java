package net.dataforte.doorkeeper.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONUtils {

	public static Map<String, ?> json2map(String s) throws JSONException {
		JSONObject json = new JSONObject(s);
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		for (@SuppressWarnings("unchecked")
		Iterator<String> it = json.keys(); it.hasNext();) {
			String key = it.next();
			Object value = json.get(key);
			if (value.getClass() == String.class) {
				map.put(key, value);
			} else if (value.getClass() == JSONArray.class) {
				List<String> l = new ArrayList<String>();
				JSONArray a = (JSONArray) value;
				for (int i = 0; i < a.length(); i++) {
					l.add(a.getString(i));
				}
				map.put(key, l);
			}
		}
		return map;
	}
}
