package org.bocogop.shared.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

public class StringUtil {

	public static final Pattern ZIP_CODE_PATTERN = Pattern.compile("^\\d{5}(?:[-\\s]\\d{4})?$");

	public static boolean anyBlank(String... strings) {
		for (String s : strings)
			if (StringUtils.isBlank(s))
				return true;
		return false;
	}

	public static boolean allBlank(String... strings) {
		for (String s : strings)
			if (StringUtils.isNotBlank(s))
				return false;
		return true;
	}

	public static boolean anyNotBlank(String... strings) {
		for (String s : strings)
			if (StringUtils.isNotBlank(s))
				return true;
		return false;
	}

	public static boolean allNotBlank(String... strings) {
		for (String s : strings)
			if (StringUtils.isBlank(s))
				return false;
		return true;
	}

	/**
	 * Returns a fixed array containing 3 name components:
	 * <p>
	 * <ol>
	 * <li>Last Name</li>
	 * <li>First Name</li>
	 * <li>Middle Name</li>
	 * </ol>
	 * 
	 * These components are extracted from the specified name parameter. The
	 * parameter can assume several formats:
	 * <ul>
	 * <li>"Last"</li>
	 * <li>"Last, First"</li>
	 * <li>"Last, First Middle"</li>
	 * </ul>
	 *
	 * The returned array will have empty Strings populated for any unspecified
	 * pieces of the name (as opposed to null entries).
	 * 
	 * @param name
	 *            The concatenated name
	 * @return The name components per the description
	 */
	public static String[] parseNameComponents(String name) {
		String[] tokens = name.split("[,\\s]+");
		String[] result = new String[3];
		result[0] = tokens[0];
		result[1] = (tokens.length >= 2) ? tokens[1] : "";
		result[2] = (tokens.length >= 3) ? tokens[2] : "";
		return result;
	}

	/**
	 * Returns a fixed array containing 3 name components:
	 * <p>
	 * <ol>
	 * <li>City</li>
	 * <li>State</li>
	 * <li>Zipcode</li>
	 * </ol>
	 * 
	 * These components are extracted from the specified location parameter. The
	 * parameter can assume several formats:
	 * <ul>
	 * <li>"City"</li>
	 * <li>"Zip"
	 * <li>"City, Zip"</li>
	 * <li>"City, State"</li>
	 * <li>"City, State Zip"</li>
	 * </ul>
	 *
	 * Zipcodes may be either in the format 12345 or 12345-6789.
	 * <p>
	 * 
	 * The returned array will have empty Strings populated for any unspecified
	 * pieces of the name (as opposed to null entries).
	 * 
	 * @param location
	 *            The concatenated location
	 * @return The location components per the description
	 * 
	 * @param location
	 * @return The location components per the description
	 * @throws IllegalArgumentException
	 *             If two or more commas are present in the input
	 */
	public static String[] parseLocationComponents(String location) throws IllegalArgumentException {
		String[] result = new String[] { "", "", "" };
		if (StringUtils.isBlank(location))
			return result;

		location = location.trim();

		String[] tokens = location.split(",");
		if (tokens.length > 2)
			throw new IllegalArgumentException("The specified location '" + location + "' is in an invalid format");

		if (tokens.length > 1) {
			/* City was specified */
			result[0] = tokens[0].trim();
			String[] stateAndZipTokens = tokens[1].split("[\\s]+");
			String lastToken = stateAndZipTokens[stateAndZipTokens.length - 1];
			if (ZIP_CODE_PATTERN.matcher(lastToken).matches()) {
				result[2] = lastToken.trim();
				result[1] = tokens[1].substring(0, tokens[1].length() - lastToken.length()).trim();
			} else {
				result[1] = tokens[1].trim();
			}
		} else {
			/* Only specified city and/or zip */
			if (ZIP_CODE_PATTERN.matcher(tokens[0]).matches()) {
				result[2] = tokens[0].trim();
			} else {
				result[0] = tokens[0].trim();
			}
		}

		return result;
	}

	public static void main(String[] args) {
		String[] tests = { "Littleton", "Littleton, CO", "Littleton, CO 80127", "Littleton, CO 801237-4530",
				"Littleton, CO 80127-4530", "Dairy Road, Littleton, CO" };

		for (String test : tests)
			System.out.println(Arrays.toString(parseLocationComponents(test)));
	}

	public static String getDisplayName(boolean lastFirst, String firstName, String middleName, String lastName,
			String suffix) {
		boolean hasLast = StringUtils.isNotEmpty(lastName);
		boolean hasFirst = StringUtils.isNotEmpty(firstName);

		if (!hasLast && !hasFirst)
			return "(unknown)";

		boolean hasMiddle = StringUtils.isNotEmpty(middleName);

		StringBuilder sb = new StringBuilder();
		if (lastFirst) {
			if (hasLast)
				sb.append(lastName);
			if (StringUtils.isNotEmpty(suffix))
				sb.append(" ").append(suffix);
			if (hasLast && hasFirst)
				sb.append(", ");
			if (hasFirst)
				sb.append(firstName);
			if (hasMiddle)
				sb.append(" ").append(middleName);
		} else {
			if (hasFirst)
				sb.append(firstName);
			if (hasMiddle)
				sb.append(" ").append(middleName);
			if (hasFirst && hasLast)
				sb.append(" ");
			if (hasLast)
				sb.append(lastName);
			if (StringUtils.isNotEmpty(suffix))
				sb.append(" ").append(suffix);
		}
		return sb.toString();
	}

	public static String getAddressDisplay(String addressLine1, String addressLine2, String addressLine3, String city,
			String state, String zip, String country, String separator) {
		List<String> items = new ArrayList<>(7);
		if (StringUtils.isNotBlank(addressLine1))
			items.add(addressLine1);
		if (StringUtils.isNotBlank(addressLine2))
			items.add(addressLine2);
		if (StringUtils.isNotBlank(addressLine3))
			items.add(addressLine3);

		boolean hasCity = StringUtils.isNotBlank(city);
		boolean hasState = StringUtils.isNotBlank(state);
		if (hasCity || hasState) {
			if (hasCity && hasState) {
				items.add(city + ", " + state);
			} else if (hasCity) {
				items.add(city);
			} else {
				items.add(state);
			}
		}
		if (StringUtils.isNotBlank(zip))
			items.add(zip);
		if (StringUtils.isNotBlank(country))
			items.add(country);
		return items.isEmpty() ? "" : StringUtils.join(items, separator);
	}

	public static String maskSSN(String ssn) {
		if (ssn == null)
			return null;
		return "XXX-XX-" + ssn.replace("-", "").substring(5);
	}

	public static String removeNonDigitsIfNecessary(String ssn) {
		if (ssn == null)
			return null;
		return ssn.replaceAll("\\D", "");
	}

	public static String getStackTrace(Throwable aThrowable) {
		final Writer result = new StringWriter();
		final PrintWriter printWriter = new PrintWriter(result);
		aThrowable.printStackTrace(printWriter);
		return result.toString();
	}

	public static String normalizeLineBreaks(String remarks) {
		return remarks.replace("\r\n", "\n");
	}

}
