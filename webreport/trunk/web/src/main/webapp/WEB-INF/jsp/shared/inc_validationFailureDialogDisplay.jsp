<%@ include file="inc_header.jsp"%>

<%-- Required: JSTL variable defined "validationFailureException"

	Optional: JSTL variable "validationFailureIntroMessageCode" - HTML that will be
		prepended directly before the message generated from the above exception --%>

displayAttentionDialogForValidationFailure(
	"<c:if test="${not empty validationFailureIntroMessageCode}"
		><spring:message code="${validationFailureIntroMessageCode}" text="" htmlEscape="false"
	/></c:if><spring:message message="${validationFailureException}" htmlEscape="true" />",

<c:if test="${not empty validationFailureException.recommendationMessageKey}">
	"<spring:message code="${validationFailureException.recommendationMessageKey}"
		text="" htmlEscape="true" />"
</c:if>
<c:if test="${empty validationFailureException.recommendationMessageKey}">
	null
</c:if>
,

<c:if test="${not empty validationFailureException.ruleMessageKey}">
	"<spring:message code="${validationFailureException.ruleMessageKey}"
		text="" htmlEscape="true"/>"
</c:if>
<c:if test="${empty validationFailureException.ruleMessageKey}">
	null
</c:if>
)