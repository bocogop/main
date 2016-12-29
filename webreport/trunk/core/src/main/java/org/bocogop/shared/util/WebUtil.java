package org.bocogop.shared.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.WebRequest;

import org.bocogop.shared.model.Role.RoleType;

public class WebUtil {
	private static final Logger log = LoggerFactory.getLogger(WebUtil.class);

	private static final Map<Class<?>, Map<String, String>> CONSTANTS = new HashMap<>();

	public static final String COMMAND_OBJ_NAME_ATTRIBUTE = "commandObjectName";

	/**
	 * Adds all public static final String fields in the specified class to the
	 * specified model.
	 * 
	 * @param clazz
	 * @param model
	 */
	public static void addClassConstantsToModel(Class<?> clazz, Map<String, Object> model) {
		addClassConstantsToModel(clazz, model, false);
	}

	/**
	 * Adds all public static final String fields in the specified class to the
	 * specified model.
	 * 
	 * @param clazz
	 * @param model
	 */
	public static void addClassConstantsToModel(Class<?> clazz, Map<String, Object> model,
			boolean includeClassNamePrefix) {
		Map<String, String> constants = registerAndRetrieveClassConstants(clazz);
		for (Map.Entry<String, String> entry : constants.entrySet()) {
			String name = entry.getKey();
			String value = entry.getValue();
			if (includeClassNamePrefix) {
				String classNamePrefix = getClassNamePrefix(clazz);
				name = classNamePrefix + name;
			}
			model.put(name, value);
		}
	}

	/**
	 * Adds all public static final String fields in the specified class to the
	 * specified request as attributes.
	 * 
	 * @param clazz
	 * @param request
	 */
	public static void addClassConstantsToRequest(Class<?> clazz, WebRequest request) {
		addClassConstantsToRequest(clazz, request, false);
	}

	/**
	 * Adds all public static final String fields in the specified class to the
	 * specified request as attributes.
	 * 
	 * @param clazz
	 * @param request
	 */
	public static void addClassConstantsToRequest(Class<?> clazz, WebRequest request, boolean includeClassNamePrefix) {
		Map<String, String> constants = registerAndRetrieveClassConstants(clazz);

		for (Map.Entry<String, String> entry : constants.entrySet()) {
			String name = entry.getKey();
			String value = entry.getValue();
			if (includeClassNamePrefix) {
				String classNamePrefix = getClassNamePrefix(clazz);
				name = classNamePrefix + name;
			}
			request.setAttribute(name, value, WebRequest.SCOPE_REQUEST);
		}
	}

	private static synchronized Map<String, String> registerAndRetrieveClassConstants(Class<?> clazz) {
		Map<String, String> constants = CONSTANTS.get(clazz);

		if (constants == null) {
			constants = registerStringClassConstants(clazz);
		}
		return constants;
	}

	private static Map<String, String> registerStringClassConstants(Class<?> clazz) {
		Map<String, String> map = CollectionUtil.getOrInsert(clazz, CONSTANTS, new HashMap<String, String>());
		for (Field field : clazz.getFields()) {
			int modifiers = field.getModifiers();
			if (!Modifier.isPublic(modifiers))
				continue;
			if (!Modifier.isStatic(modifiers))
				continue;
			if (!Modifier.isFinal(modifiers))
				continue;
			if (field.getType() != String.class)
				continue;
			try {
				map.put(field.getName(), (String) field.get(null));
			} catch (Exception e) {
				log.error("Couldn't reference the field " + field.getName() + " in " + clazz, e);
				continue;
			}
		}
		return map;
	}

	public static void addEnumToRequest(Class<? extends Enum<?>> enumClass, final HttpServletRequest request) {
		addEnumValues(enumClass, new CommonOperations() {
			@Override
			public void add(String key, Object value) {
				request.setAttribute(key, value);
			}
		});
	}

	public static void addEnumToModel(Class<? extends Enum<?>> enumClass, final Map<String, Object> model) {
		addEnumValues(enumClass, new CommonOperations() {
			@Override
			public void add(String key, Object value) {
				model.put(key, value);
			}
		});
	}

	private static void addEnumValues(Class<? extends Enum<?>> enumClass, CommonOperations ops) {
		String className = enumClass.getSimpleName();
		Enum<?>[] values = enumClass.getEnumConstants();
		ops.add("all" + StringUtils.capitalize(className) + "s", values);

		String sb = getClassNamePrefix(enumClass);
		for (Enum<?> value : values)
			ops.add(sb.toString() + value.name().toUpperCase(), value);
	}

	private static String getClassNamePrefix(Class<?> clazz) {
		String className = clazz.getSimpleName();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < className.length(); i++) {
			char c = className.charAt(i);
			if (i > 0 && Character.isUpperCase(c)
					&& (i == className.length() - 1 || !Character.isUpperCase(className.charAt(i + 1))))
				sb.append("_");
			sb.append(Character.toUpperCase(c));
		}
		sb.append("_");
		return sb.toString();
	}

	public static void main(String[] args) {
		Map<String, Object> test = new HashMap<>();
		addEnumToModel(RoleType.class, test);
		System.out.println(test);
	}

	private static interface CommonOperations {
		void add(String key, Object value);
	}

	public static void writeFileAttachmentToResponse(HttpServletResponse res, byte[] fileContent, String contentType,
			String filename) throws IOException {
		writeFileToResponse(res, fileContent, "attachment", contentType, filename);
	}

	public static void writeFileInlineToResponse(HttpServletResponse res, byte[] fileContent, String contentType,
			String filename) throws IOException {
		writeFileToResponse(res, fileContent, "inline", contentType, filename);
	}

	private static void writeFileToResponse(HttpServletResponse res, byte[] fileContent, String disposition,
			String contentType, String filename) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(fileContent.length);
		baos.write(fileContent);
		res.setContentType(contentType);
		res.addHeader("Content-Disposition", disposition + "; filename=" + filename);
		// res.addProperty(HttpHeaders.CACHE_CONTROL,
		// "max-age=3600, must-revalidate");
		res.setContentLength(baos.size());

		try (OutputStream out = res.getOutputStream()) {
			baos.writeTo(out);
			out.flush();
		}
	}

}
