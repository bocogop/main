<%@ include file="../../shared/inc_header.jsp"%>

<tiles:importAttribute name="widgetId" scope="page" />
<tiles:importAttribute name="textInputAttributes" scope="page" ignore="true" />
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
			htmlValidationSelectors : [ "#${widgetId}" ]
		})
	})()
	
	$(function() {
		$("#${widgetId}").enableDatePicker({
			showOn : "button",
			buttonImage : imgHomePath + "/calendar.gif",
			buttonImageOnly : true
		})
		$("#${widgetId}").mask(twoDigitDateMask, {
			autoclear : false
		})
	})
</script>

<c:out value="${widgetLabel}" />
:
<input type="text" id="${widgetId}" ${textInputAttributes} value="${initialValue}" />