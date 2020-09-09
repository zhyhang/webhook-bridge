package org.yanhuang.alerting.webhook.bridge.utils;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author zhyhang
 *
 */
public class JsonCodec {

	private static final ObjectMapper mapper = new ObjectMapper();

	static {
		mapper.setSerializationInclusion(Include.NON_NULL);
		mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	public static String toString(Object obj) {
		if (obj == null) {
			return null;
		}
		try {
			return mapper.writeValueAsString(obj);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static <T> T parse(String json, Class<T> cls) {
		if (json == null || json.trim().isEmpty()) {
			return null;
		}
		try {
			return mapper.readValue(json, cls);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static byte[] getBytes(Object obj) {
		try {
			return obj != null ? mapper.writeValueAsBytes(obj) : null;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static <T> T parseBytes(byte[] bs, Class<T> clazz) {
		try {
			return bs != null ? mapper.readValue(bs, clazz) : null;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
