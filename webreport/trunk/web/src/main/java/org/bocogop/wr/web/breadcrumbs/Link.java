package org.bocogop.wr.web.breadcrumbs;

import java.io.Serializable;

public class Link implements Serializable {
	private static final long serialVersionUID = -3600276917920115323L;

	// ------------------------------------- Fields
	
	private String text;
	private String href;

	// ------------------------------------- Constructors

	public Link() {
	}

	public Link(String text, String href) {
		this.text = text;
		this.href = href;
	}

	// ------------------------------------- Accessor Methods

	/**
	 * @return the href
	 */
	public String getHref() {
		return href;
	}

	/**
	 * @param href
	 *            the href to set
	 */
	public void setHref(String href) {
		this.href = href;
	}

	/**
	 * @return the text
	 */
	public String getText() {
		return text;
	}

	/**
	 * @param text
	 *            the text to set
	 */
	public void setText(String text) {
		this.text = text;
	}

}
