<%@ include file="inc_header.jsp"%>

<c:set var="useSpacerIfNoEmail" value="${param.useSpacerIfNoEmail}" />
<c:if test="${empty useSpacerIfNoEmail}">
	<c:set var="useSpacerIfNoEmail" value="false" />
</c:if>

<%--
	Simple JSP fragment which generates an envelope link (with a "mailto:" href) and an
	associated javascript method which shows or hides this envelope link.	
	
	Expected params:
		"uid" - a unique ID amongst all other instances of this include on the main page.
		"emailAddress" - the email to link to. If this is empty, then by default a blank
			spacer image will be shown; otherwise, an envelope link will be shown.
	
	A JavaScript method "setEmailLink<uid>Visibility(isShown)" will be generated which can
	be called by the parent page. Pass true or false in as a param.
--%>

<c:if test="${not empty fn:escapeXml(param.emailAddress) or useSpacerIfNoEmail}">
	<c:set var="linkDisplay" value="" />
	<c:set var="spacerDisplay" value="none" />
	<c:if test="${empty fn:escapeXml(param.emailAddress)}">
		<c:set var="linkDisplay" value="none" />
		<c:set var="spacerDisplay" value="" />	
	</c:if>
	
	<span>
			<a id="${fn:escapeXml(param.uid)}EmailLinkAnchor" style="display:${linkDisplay}"
				href="mailto:<c:out value="${fn:escapeXml(param.emailAddress)}" />"><img alt='Click to email <c:out value="${fn:escapeXml(param.emailAddress)}" />'
				src="${imgHome}/envelope.jpg" height="14"
				width="18" border="0" align="absmiddle"
				style="padding-left: 4px; padding-right: 4px" /></a>
			<img alt="" class="${spacerClass}" src="${imgHome}/spacer.gif" height="14"
				width="18" style="display:${spacerDisplay}; padding-left: 4px; padding-right: 4px" />
	</span>
</c:if>