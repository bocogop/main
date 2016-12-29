<%@ include file="../../shared/inc_header.jsp"%>

<tiles:importAttribute name="widgetId" scope="page" />
<tiles:importAttribute name="textInputAttributes" scope="page"
	ignore="true" />
<tiles:importAttribute name="initialValue" scope="page" ignore="true" />

<script type="text/javascript">
	(function() {
		registerWidget({
			getParameters : function() {
				return [ {
					displayName : "<c:out value="${widgetLabel}" />",
					paramName : "<c:out value="${widgetParamName}" />",
					paramValue : $("#${widgetId}").val()
				} ]
			},
			changeEventSelectors : [ "#${widgetId}" ],
			htmlValidationSelectors : [ "#${widgetId}" ],
			customValidator : function() {
				var date = getWidget("${widgetId}").getParameterValue()
				var tokens = date.split("/");
				var month = tokens[0];
				if (parseInt(month) > 12 || parseInt(month) < 1)
					return 'Please enter a month between 1 and 12'
				
				return null
			}
		})
	})()
	
	$(function() {
		$("#${widgetId}").mask(twoDigitMonthYearMask, {
			autoclear : false
		})
	})

</script>

<c:out value="${widgetLabel}" />
:
<input type="text" id="${widgetId}" ${textInputAttributes}
	value="${initialValue}" />