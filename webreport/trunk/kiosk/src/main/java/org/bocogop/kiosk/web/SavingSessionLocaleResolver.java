package org.bocogop.kiosk.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bocogop.shared.service.voter.VoterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

public class SavingSessionLocaleResolver extends SessionLocaleResolver {

	@Autowired
	private VoterService voterService;

	@Override
	public void setLocaleContext(HttpServletRequest request, HttpServletResponse response,
			LocaleContext localeContext) {
		super.setLocaleContext(request, response, localeContext);
		// add this to support multiple languages - CPB
		
		// CoreUserDetails currentUser = SecurityUtil.getCurrentUser();
		// if (currentUser != null) {
		// Long voterId = currentUser.getId();
		// voterService.updatePreferredLanguage(voterId,
		// localeContext.getLocale().getLanguage());
		// }
	}

}
