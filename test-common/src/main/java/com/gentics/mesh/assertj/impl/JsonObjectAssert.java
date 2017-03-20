package com.gentics.mesh.assertj.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.assertj.core.api.AbstractAssert;

import com.gentics.mesh.util.UUIDUtil;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;

import io.vertx.core.json.JsonObject;

public class JsonObjectAssert extends AbstractAssert<JsonObjectAssert, JsonObject> {
	/**
	 * Key to check
	 */
	protected String key;

	public JsonObjectAssert(JsonObject actual) {
		super(actual, JsonObjectAssert.class);
	}

	public JsonObjectAssert key(String key) {
		this.key = key;
		return this;
	}

	public JsonObjectAssert matches(Object expected) {
		assertNotNull(descriptionText() + " cannot be matched without specifying key first", key);
		assertNotNull(descriptionText() + " JsonObject must not be null", actual);
		assertEquals(descriptionText() + " key " + key, expected, actual.getValue(key));
		return this;
	}

	public JsonObjectAssert has(String path, String value, String msg) {
		try {
			String actualValue = getByPath(path);
			assertEquals("Value for property on path {" + path + "} did notmatch: " + msg, value, actualValue);
		} catch (PathNotFoundException e) {
			fail("Could not find property for path {" + path + "} - Json is:\n--snip--\n" + actual.encodePrettily() + "\n--snap--\n" + msg);
		}
		return this;
	}

	/**
	 * Resolve the given JSON path to load the value.
	 *
	 * @param jsonPath
	 *            the JSON path
	 * @param <T>
	 *            expected return type
	 * @return list of objects matched by the given path
	 */
	private <T> T getByPath(String jsonPath) {
		return JsonPath.read(actual.toString(), jsonPath);
	}

	public JsonObjectAssert hasNullValue(String key) {
		assertTrue("The json object should contain a map entry for key {" + key + "}", actual.containsKey(key));
		assertNull("The json object for key {" + key + "} should be null", actual.getJsonObject(key));
		return this;
	}

	/**
	 * Assert that the json object complies to the assertions which are stored in the assertionsfile of the given name.
	 * 
	 * @param name
	 * @return
	 * @throws IOException
	 */
	public JsonObjectAssert compliesToAssertions(String name) throws IOException {
		Properties props = new Properties();
		String path = "/graphql/" + name + ".assertions";
		InputStream ins = getClass().getResourceAsStream(path);
		if (ins == null) {
			fail("Could not find assertionsfile {" + path + "}");
		}
		try {
			props.load(ins);
		} finally {
			ins.close();
		}
		for (Object key : props.keySet()) {
			String keyStr = (String) key;
			String valueStr = props.getProperty(keyStr);
			String[] assertionInfo = valueStr.split("##");
			String value = assertionInfo[0];

			String msg = "";
			if (assertionInfo.length > 1) {
				msg = assertionInfo[1];
			}
			if ("<not-null>".equals(value)) {
				pathIsNotNull(keyStr, msg);
			} else if ("<is-null>".equals(value)) {
				pathIsNull(keyStr, msg);
			} else if ("<is-uuid>".equals(value)) {
				pathIsUuid(keyStr, msg);
			} else {
				has(keyStr, value, msg);
			}
		}

		return this;
	}

	public JsonObjectAssert pathIsUuid(String path) {
		return pathIsUuid(path, null);
	}

	public JsonObjectAssert pathIsUuid(String path, String msg) {
		if (msg == null) {
			msg = "";
		}
		String value = JsonPath.read(actual.toString(), path);
		assertNotNull("Value on path {" + path + "} was null", value);
		assertTrue("The specified value {" + value + "} on path {" + path + "} was no uuid: " + msg, UUIDUtil.isUUID(value));
		return this;
	}

	public JsonObjectAssert pathIsNotNull(String path) {
		return pathIsNotNull(path, null);
	}

	public JsonObjectAssert pathIsNotNull(String path, String msg) {
		if (msg == null) {
			msg = "";
		}
		Object value = JsonPath.read(actual.toString(), path);
		assertNotNull("Value on the path {" + path + "} was expected to be non-null: " + msg, value);
		return this;
	}

	public JsonObjectAssert pathIsNull(String path) {
		return pathIsNull(path, null);
	}

	public JsonObjectAssert pathIsNull(String path, String msg) {
		if (msg == null) {
			msg = "";
		}
		Object value = JsonPath.read(actual.toString(), path);
		assertNull("Value on the path {" + path + "} was expected to be null but was {" + value + "}: " + msg, value);
		return this;
	}
}