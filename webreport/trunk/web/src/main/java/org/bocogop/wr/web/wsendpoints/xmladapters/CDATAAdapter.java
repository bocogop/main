package org.bocogop.wr.web.wsendpoints.xmladapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class CDATAAdapter extends XmlAdapter<String, String> {

	public static final String CDATA_PREFIX = "<![CDATA[";
	public static final String CDATA_SUFFIX = "]]>";

	@Override
	public String marshal(String v) throws Exception {
		return CDATA_PREFIX + v + CDATA_SUFFIX;
	}

	@Override
	public String unmarshal(String v) throws Exception {
		return v;
	}
}
