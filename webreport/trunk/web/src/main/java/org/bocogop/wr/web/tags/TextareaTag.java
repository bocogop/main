package org.bocogop.wr.web.tags;

import java.util.Map;

import javax.servlet.jsp.JspException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.tags.form.TagWriter;

public class TextareaTag extends org.springframework.web.servlet.tags.form.TextareaTag {
	private static final Logger log = LoggerFactory.getLogger(TextareaTag.class);
	private static final long serialVersionUID = -364728090243513709L;

	private static final String MAXLENGTH_ATTRIBUTE = "maxlength";

	/*
	 * Not sure if the tag will try to retrieve this attribute more than once so
	 * caching it just to be on the safe side. Foregoing synchronization since
	 * re-population a second time wouldn't be a huge issue - CPB
	 */
	private boolean attemptedCacheLength = false;
	private String cachedLength = null;

	@Override
	protected void writeOptionalAttributes(TagWriter tagWriter) throws JspException {
		super.writeOptionalAttributes(tagWriter);
		writeOptionalAttribute(tagWriter, MAXLENGTH_ATTRIBUTE, getMaxlength());
	}

	protected String getMaxlength() {
		/* allow an explicit tag attribute to override our introspected value */
		Object maxlengthObj = getValue(MAXLENGTH_ATTRIBUTE);
		if (maxlengthObj == null) {
			Map<String, Object> dynamicAttrs = getDynamicAttributes();
			if (dynamicAttrs != null)
				maxlengthObj = dynamicAttrs.get(MAXLENGTH_ATTRIBUTE);
		}
		if (maxlengthObj != null)
			return String.valueOf(maxlengthObj);

		if (cachedLength != null)
			return cachedLength;

		if (!attemptedCacheLength)
			try {
				cachedLength = InputTag.getCachedLength(getBindStatus(), getRequestContext(), pageContext);
			} catch (JspException e) {
				log.error("Encountered an error:", e);
			} finally {
				attemptedCacheLength = true;
			}
		return cachedLength;
	}

}