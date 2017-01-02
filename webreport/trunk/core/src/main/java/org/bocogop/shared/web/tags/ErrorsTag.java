package org.bocogop.shared.web.tags;

import java.util.Map;

import javax.servlet.jsp.JspException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.tags.form.TagWriter;

/**
 * Error Tag class
 * 
 */
public class ErrorsTag extends org.springframework.web.servlet.tags.form.ErrorsTag {

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(ErrorsTag.class);
	private static final long serialVersionUID = -364728090243513709L;

	private static final String TAB_INDEX_ATTRIBUTE = "tabIndex";
	private static final String DEFAULT_TAB_INDEX_VALUE = "0";

	@Override
	protected void writeOptionalAttributes(TagWriter tagWriter) throws JspException {
		super.writeOptionalAttributes(tagWriter);
		writeOptionalAttribute(tagWriter, TAB_INDEX_ATTRIBUTE, getTabIndex());
	}

	/**
	 * Sets the tabIndex to zero by default if it is not specified
	 * 
	 * @return tabIndex
	 */
	protected String getTabIndex() {
		/* allow an explicit tag attribute to override our introspected value */
		Object maxlengthObj = getValue(TAB_INDEX_ATTRIBUTE);

		if (maxlengthObj == null) {
			Map<String, Object> dynamicAttrs = getDynamicAttributes();
			if (dynamicAttrs != null)
				maxlengthObj = dynamicAttrs.get(TAB_INDEX_ATTRIBUTE);
		}

		if (maxlengthObj != null)
			return String.valueOf(maxlengthObj);

		return DEFAULT_TAB_INDEX_VALUE;
	}
}