package org.bocogop.wr.web.tags;

import javax.servlet.jsp.JspException;

import org.apache.commons.lang3.StringUtils;
import org.bocogop.shared.model.lookup.AbstractLookup;
import org.springframework.web.servlet.tags.form.TagWriter;

/**
 * @author vhaisdbarryc
 */
public class SelectTag extends org.springframework.web.servlet.tags.form.SelectTag {
	private static final long serialVersionUID = 7795278323667003152L;

	private boolean addCssClass = false;

	@Override
	protected void writeDefaultAttributes(TagWriter tagWriter) throws JspException {
		Object actualValueObj = getBindStatus().getActualValue();
		if (actualValueObj instanceof AbstractLookup == false) {
			super.writeDefaultAttributes(tagWriter);
			return;
		}

		AbstractLookup<?, ?> lookupVal = (AbstractLookup<?, ?>) actualValueObj;
		if (lookupVal.isActive()) {
			super.writeDefaultAttributes(tagWriter);
			return;
		}

		tagWriter.writeOptionalAttributeValue("inactiveAppLookupName", lookupVal.getName());
		tagWriter.writeOptionalAttributeValue("inactiveAppLookupId", String.valueOf(lookupVal.getId()));
		addCssClass = true;

		super.writeDefaultAttributes(tagWriter);
	}

	/**
	 * Gets the appropriate CSS class to use based on the state of the current
	 * {@link org.springframework.web.servlet.support.BindStatus} object.
	 */
	@Override
	protected String resolveCssClass() throws JspException {
		String cssClass = super.resolveCssClass();
		if (addCssClass) {
			if (StringUtils.isNotEmpty(cssClass))
				cssClass += " ";
			cssClass += "inactiveAppSelect";
		}
		return cssClass;
	}

}
