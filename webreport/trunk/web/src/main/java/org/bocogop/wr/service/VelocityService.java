package org.bocogop.wr.service;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.tools.generic.DateTool;
import org.apache.velocity.tools.generic.FieldTool;
import org.bocogop.wr.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.ui.velocity.VelocityEngineUtils;

@Service
public class VelocityService {

	@Autowired
	private VelocityEngine velocityEngine;
	@Value("${email.templateEncoding}")
	private String emailTemplateEncoding;

	public String mergeTemplateIntoString(String templateName) {
		return mergeTemplateIntoString(templateName, new HashMap<String, Object>());
	}

	public String mergeTemplateIntoString(String templateName, Map<String, Object> model) {
		if (!model.containsKey("date"))
			model.put("date", new DateTool());
		if (!model.containsKey("field"))
			model.put("field", new FieldTool());
		if (!model.containsKey("DateUtil"))
			model.put("DateUtil", DateUtil.class);
		if (!model.containsKey("StringUtils"))
			model.put("StringUtils", StringUtils.class);
		
		String content = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, templateName,
				emailTemplateEncoding, model);

		/* remove the initial spaces for all non-blank lines */
		content = content.replaceAll("(?m)^[ \t]+", "");
		/* remove all blank lines except for those that start with # */
		content = content.replaceAll("(?m)^[ \t]*\r?\n", "");
		/* remove the initial # from any line that starts with one */
		content = content.replaceAll("(?m)^#", "");

		return content;
	}

}
