package com.github.saggcs.abebc.uttill;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.JsonTokenId;
import com.github.jochenw.afw.core.util.Holder;
import com.github.jochenw.afw.di.util.Exceptions;

public class JsonUtils {
	private static final JsonFactory jsonFactory = new JsonFactory();

	
	public static void write(Object pJsonObject, OutputStream pOs) {
		try (final JsonGenerator jgen = jsonFactory.createGenerator(pOs)) {
			writeValue(jgen, null, pJsonObject);
		} catch (Exception e) {
			throw Exceptions.show(e);
		}
	}

	protected static void writeValue(JsonGenerator pJgen, String pName, Object pValue) throws JsonGenerationException, IOException {
		if (pValue == null) {
			if (pName == null) {
				pJgen.writeNull();
			} else {
				pJgen.writeNullField(pName);
			}
		} else if (pValue instanceof Boolean) {
			final boolean b = ((Boolean) pValue).booleanValue();
			if (pName == null) {
				pJgen.writeBoolean(b);
			} else {
				pJgen.writeBooleanField(pName, b);
			}
		} else if (pValue instanceof String) {
			final String s = (String) pValue;
			if (pName == null) {
				pJgen.writeString(s);
			} else {
				pJgen.writeStringField(pName, s);
			}
		} else if (pValue instanceof BigDecimal) {
			final BigDecimal bd = (BigDecimal) pValue;
			if (pName == null) {
				pJgen.writeNumber(bd);
			} else {
				pJgen.writeNumberField(pName, bd);
			}
		} else if (pValue instanceof Long) {
			final long l = ((Long) pValue).longValue();
			if (pName == null) {
				pJgen.writeNumber(l);
			} else {
				pJgen.writeNumberField(pName, l);
			}
		} else if (pValue instanceof Integer) {
			final int i = ((Integer) pValue).intValue();
			if (pName == null) {
				pJgen.writeNumber(i);
			} else {
				pJgen.writeNumberField(pName, i);
			}
		} else if (pValue instanceof Double) {
			final double d = ((Double) pValue).doubleValue();
			if (pName == null) {
				pJgen.writeNumber(d);
			} else {
				pJgen.writeNumberField(pName, d);
			}
		} else if (pValue instanceof Float) {
			final float f = ((Float) pValue).floatValue();
			if (pName == null) {
				pJgen.writeNumber(f);
			} else {
				pJgen.writeNumberField(pName, ((Float) pValue).floatValue());
			}
		} else if (pValue.getClass().isArray()) {
			if (pName == null) {
				pJgen.writeStartArray();
			} else {
				pJgen.writeArrayFieldStart(pName);
			}
			final int len = Array.getLength(pValue);
			for (int i = 0;  i < len;  i++) {
				writeValue(pJgen, null, Array.get(pValue, i));
			}
			pJgen.writeEndArray();
		} else if (pValue instanceof List) {
			final List<?> list = (List<?>) pValue;
			if (pName == null) {
				pJgen.writeStartArray();
			} else {
				pJgen.writeArrayFieldStart(pName);
			}
			list.forEach((o) -> {
				try {
					writeValue(pJgen, null, o);
				} catch (Exception e) {
					throw Exceptions.show(e);
				}
			});
			pJgen.writeEndArray();
		} else if (pValue instanceof Map) {
			@SuppressWarnings("unchecked")
			final Map<String,Object> map = (Map<String,Object>) pValue;
			if (pName == null) {
				pJgen.writeStartObject();
			} else {
				pJgen.writeObjectFieldStart(pName);
			}
			map.forEach((k,v) -> {
				try {
					writeValue(pJgen, k, v);
				} catch (Exception e) {
					throw Exceptions.show(e);
				}
			});
			pJgen.writeEndObject();
		} else {
			throw new IllegalStateException("Invalid type for Json value: " + pValue.getClass().getName());
		}
	}

	public static <O> O parse(String pMapString) {
		return parse(new StringReader(pMapString));
	}

	public static <O> O parse(Reader pReader) {
		try (final JsonParser jp = jsonFactory.createParser(pReader)) {
			@SuppressWarnings("unchecked")
			final O o = (O) parse(jp, null);
			return o;
		} catch (Exception e) {
			throw Exceptions.show(e);
		}
	}

	public static <O> O parse(InputStream pIn) {
		try (final JsonParser jp = jsonFactory.createParser(pIn)) {
			@SuppressWarnings("unchecked")
			final O o = (O) parse(jp, null);
			return o;
		} catch (Exception e) {
			throw Exceptions.show(e);
		}
	}
	
	public static Object parse(JsonParser pParser, JsonToken pToken) throws JsonParseException, IOException {
		final JsonLocation loc;
		final JsonToken tok;
		if (pToken == null) {
			loc = pParser.getCurrentLocation();
			tok = pParser.nextToken();
		} else {
			loc = pParser.getTokenLocation();
			tok = pToken;
		}
		if (tok == null) {
			throw new IllegalStateException("Unexpected end of token stream at " + loc);
		}
		switch (tok.id()) {
		case JsonTokenId.ID_START_OBJECT:
			return parseMap(pParser);
		case JsonTokenId.ID_START_ARRAY:
			return parseArray(pParser);
		case JsonTokenId.ID_NUMBER_INT: {
			final String str = pParser.getText();
			try {
				return Integer.valueOf(Integer.parseInt(str));
			} catch (NumberFormatException e1) {
				try {
					return Long.valueOf(Long.parseLong(str));
				} catch (NumberFormatException e2) {
					try {
						return new BigInteger(str);
					} catch (NumberFormatException e3) {
						throw new IllegalStateException("Invalid integer number " + str + " at " + loc);
					}
				}
			}
		}
		case JsonTokenId.ID_NUMBER_FLOAT: {
			final String str = pParser.getText();
			try {
				return Float.valueOf(Float.parseFloat(str));
			} catch (NumberFormatException e1) {
				try {
					return Double.valueOf(Double.parseDouble(str));
				} catch (NumberFormatException e2) {
					try {
						return new BigDecimal(str);
					} catch (NumberFormatException e3) {
						throw new IllegalStateException("Invalid floating point number " + str + " at " + loc);
					}
				}
			}
		}
		case JsonTokenId.ID_FALSE:
			return Boolean.FALSE;
		case JsonTokenId.ID_TRUE:
			return Boolean.TRUE;
		case JsonTokenId.ID_NULL:
			return null;
		case JsonTokenId.ID_STRING:
			return pParser.getText();
		default:
			throw new IllegalStateException("Invalid token " + tok + " at " + loc);
		}
	}

	protected static List<Object> parseArray(JsonParser pParser)
			throws JsonParseException, IOException {
		final List<Object> list = new ArrayList<>();
		for (;;) {
			final JsonLocation loc = pParser.getCurrentLocation();
			final JsonToken tok = pParser.nextToken();
			if (tok == null) {
				throw new IllegalStateException("Unexpected end of token stream at " + loc);
			} else if (tok.id() == JsonTokenId.ID_END_ARRAY) {
				return list;
			} else {
				list.add(parse(pParser, tok));
			}
		}
	}

	protected static Map<String,Object> parseMap(JsonParser pParser)
				throws JsonParseException, IOException {
		final Map<String,Object> map = new HashMap<>();
		for (;;) {
			final JsonLocation loc = pParser.getCurrentLocation();
			final JsonToken tok = pParser.nextToken();
			if (tok == null) {
				throw new IllegalStateException("Unexpected end of token stream at " + loc);
			} else if (tok.id() == JsonTokenId.ID_END_OBJECT) {
				return map;
			} else if (tok.id() == JsonTokenId.ID_FIELD_NAME) {
				final String fieldName = pParser.getCurrentName();
				map.put(fieldName, parse(pParser, null));
			} else {
				throw new IllegalStateException("Unexpexted token " + tok + " at " + loc);
			}
		}
	}

	public static String asString(Map<String, Object> pJsonObject) {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		write(pJsonObject, baos);
		return new String(baos.toByteArray(), StandardCharsets.UTF_8);
	}
}
