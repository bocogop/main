package org.bocogop.wr.util;

public class ValidationUtil {

	/*
	 * Phone Number formats: (nnn)nnn-nnnn; nnnnnnnnnn; nnn-nnn-nnnn ^\\(? : May
	 * start with an option "(" . (\\d{3}) : Followed by 3 digits. \\)? : May
	 * have an optional ")" [- ]? : May have an optional "-" after the first 3
	 * digits or after optional ) character. (\\d{3}) : Followed by 3 digits. [-
	 * ]? : May have another optional "-" after numeric digits. (\\d{4}): ends
	 * with four digits. \\x? : May contain an optional "x" ([0-9]*$ : May have
	 * one or more digits.
	 * 
	 * Examples: Matches following phone numbers: (123)456-7890, 123-456-7890,
	 * 1234567890, (123)-456-7890
	 * 
	 * @Pattern(regexp =
	 * "^\\(?(\\d{3})\\)?[- ]?(\\d{3})[-]?(\\d{4})(\\x?\\d{1,5}?)$", message =
	 * "Invalid phone number")
	 */
	// private static final String PHONE_REGEX =
	// "^\\(?(\\d{3})\\)?( )?(\\d{3})( |-)?(\\d{4})( )?(x)?(\\d{1,5})?$";
	// Loosening up the regex validation to allow either () around area code or
	// have area code separated by - from the rest of the
	// phone number
	public static final String PHONE_REGEX = "^\\(?(\\d{3})\\)?[ -]?(\\d{3})[ -]?(\\d{4})[ ]?(x)?(\\d{1,5})?$";

	// checks if fax number entered is valid
	public static final String FAX_REGEX = "^\\(?(\\d{3})\\)?[ -]?(\\d{3})[ -]?(\\d{4})[ ]?$";
	
	// checks if email entered is valid
	public static final String EMAIL_REGEX  = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
}