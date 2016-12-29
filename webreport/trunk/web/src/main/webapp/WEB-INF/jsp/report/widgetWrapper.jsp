<%@ include file="../shared/inc_header.jsp"%>

<tiles:importAttribute name="widgetId" scope="page" />
<tiles:importAttribute name="reportParamName" scope="page" ignore="true" />
<tiles:importAttribute name="displayLabel" scope="page" ignore="true" />
<tiles:importAttribute name="initialVisibility" scope="page"
	ignore="true" />
<tiles:importAttribute name="submitAsReportParameters" scope="page"
	ignore="true" />
<tiles:importAttribute name="bypassAsReportParameters" scope="page"
	ignore="true" />
<tiles:importAttribute name="htmlValidationFailureMessageProvider"
	scope="page" ignore="true" />

<%-- Boilerplate for all widgets with a single parameter; if multiple params, they can import 
whatever tiles attribute they want themselves and set up their getParameters() method accordingly - CPB  --%>
<c:set var="widgetParamName" value="${widgetId}" scope="request" />
<c:if test="${not empty reportParamName}">
	<c:set var="widgetParamName" value="${reportParamName}" scope="request" />
</c:if>

<c:set var="widgetLabel" value="${widgetId}" scope="request" />
<c:if test="${not empty displayLabel}">
	<c:set var="widgetLabel" value="${displayLabel}" scope="request" />
</c:if>

<c:set var="visible" value="true" />
<c:if test="${initialVisibility == 'hidden'}">
	<c:set var="visible" value="false" />
</c:if>

<script type="text/javascript">
	latestReportParameterBaseConfig = {
		id : "<c:out value="${widgetId}" />",
		submitAsReportParam : <c:out value="${submitAsReportParameters}" default="true" />,
		bypassAsReportParam : <c:out value="${bypassAsReportParameters}" default="false" />,
		visible : ${visible},
		htmlValidationSelectors : [],
		htmlValidationFailureMessageProvider : <c:out value="${htmlValidationFailureMessageProvider}" default="null" />,
		customValidator : null,
		internalValidator : $.noop
	}
</script>

<div class="widgetWrapper" id="widget_${widgetId}_div"
	style="display: none">
	<tiles:insertAttribute name="widget" />
</div>