<%@ include file="../../shared/inc_header.jsp"%>

<tiles:importAttribute name="widgetId" scope="page" />
<tiles:importAttribute name="initialValue" scope="page" ignore="true" />

<script type="text/javascript">
	(function() {
		registerWidget({
			visible: false,
			getParameters : function() {
				return [ {
					displayName : "",
					paramName : "<c:out value="${widgetParamName}" />",
					paramValue : $("#${widgetId}").val()
				} ]
			}
		})
	})()
</script>

<input type="hidden" id="${widgetId}" value="${initialValue}" />