<%@ include file="../../shared/inc_header.jsp"%>

<tiles:importAttribute name="widgetId" scope="page" />
<tiles:importAttribute name="checkboxInputAttributes" scope="page" ignore="true" /> 
<tiles:importAttribute name="inputType" scope="page" ignore="true" />

<c:if test="${empty inputType}">
	<c:set var="inputType" value="checkbox" />
</c:if>

<script type="text/javascript">
	(function() {
		registerWidget({
			getParameters : function() {
				return [ {
					displayName : "<c:out value="${widgetLabel}" />",
					paramName : "<c:out value="${widgetParamName}" />",
					paramValue : $("#${widgetId}").is(":checked") ? '1' : ['0','1']  
				} ]
			},
			changeEventSelectors : [ "#${widgetId}" ],
			htmlValidationSelectors : [ "#${widgetId}" ]
		})
	})()
</script>

<c:out value="${widgetLabel}" />
:
<input type="${inputType}" id="${widgetId}" ${checkboxInputAttributes} />