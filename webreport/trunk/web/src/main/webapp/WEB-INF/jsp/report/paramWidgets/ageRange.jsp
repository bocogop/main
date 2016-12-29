<%@ include file="../../shared/inc_header.jsp"%>

<tiles:importAttribute name="widgetId" scope="page" />
<tiles:importAttribute name="reportParamNameFrom" scope="page" />
<tiles:importAttribute name="reportParamNameTo" scope="page" />

<script type="text/javascript">
	(function() {
		registerWidget({
			getParameters : function() {
				return [ {
					displayName : "<c:out value="${widgetLabel}" /> From",
					paramName : "<c:out value="${reportParamNameFrom}" />",
					paramValue : $("#${widgetId}_from").val()
				}, {
					displayName : "<c:out value="${widgetLabel}" /> To",
					paramName : "<c:out value="${reportParamNameTo}" />",
					paramValue : $("#${widgetId}_to").val()
				} ]
			},
			changeEventSelectors : [ "#${widgetId}_from", "#${widgetId}_to" ],
			htmlValidationSelectors : [ "#${widgetId}_from", "#${widgetId}_to" ]
		})
	})()
</script>

<c:out value="${widgetLabel}" />
:
From <input type="number" id="${widgetId}_from" value="0" min="0" max="120" />
To <input type="number" id="${widgetId}_to" value="120" min="0" max="120" />