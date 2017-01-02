package org.bocogop.shared.model.lookup;

public enum TemplateType {
	REFERENCE_DATA_LINKS("refDataLinks"), //
	FOOTER_CONTENT("footerContent"), //
	HOMEPAGE_CONTENT("homepageContent"), //
	HOMEPAGE_ANNOUNCEMENT("homepageAnnouncement"), //
	SYSTEM_NOTIFICATION("systemNotification");

	private String name;

	private TemplateType(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

}
