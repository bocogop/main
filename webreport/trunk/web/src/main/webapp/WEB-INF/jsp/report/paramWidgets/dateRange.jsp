<%@ include file="../../shared/inc_header.jsp"%>

<tiles:importAttribute name="widgetId" scope="page" />
<tiles:importAttribute name="reportParamNameBegin" scope="page" />
<tiles:importAttribute name="reportParamNameEnd" scope="page" />
<tiles:importAttribute name="initialBeginValue" scope="page"
	ignore="true" />
<tiles:importAttribute name="initialEndValue" scope="page" ignore="true" />
<tiles:importAttribute name="dateGapLimit" scope="page" ignore="true" />

<c:if test="${empty dateGapLimit}">
	<c:set var="dateGapLimit" value="null" />
</c:if>

<script type="text/javascript">
	(function() {
		registerWidget({
			getParameters : function() {
				return [ {
					displayName : "<c:out value="${widgetLabel}" /> From",
					paramName : "<c:out value="${reportParamNameBegin}" />",
					paramValue : $("#${widgetId}_begin").val()
				}, {
					displayName : "<c:out value="${widgetLabel}" /> To",
					paramName : "<c:out value="${reportParamNameEnd}" />",
					paramValue : $("#${widgetId}_end").val()
				} ]
			},
			changeEventSelectors : [ "#${widgetId}_begin", "#${widgetId}_end" ],
			htmlValidationSelectors : [ "#${widgetId}_begin",
					"#${widgetId}_end" ],
			internalValidator : function() {
				var beginDate = getDateFromMMDDYYYY($("#${widgetId}_begin")
						.val())
				var endDate = getDateFromMMDDYYYY($("#${widgetId}_end").val())
				if (${dateGapLimit} != null
						&& ((endDate - beginDate)/1000/60/60/24 > ${dateGapLimit}))
					return 'Begin and End date cannot span more than 12 months total'
				if (endDate < beginDate)
					return 'End Date cannot be before Begin Date'
				else
					return null
			}
		})
	})()

	$(function() {
		$([ "#${widgetId}_begin", "#${widgetId}_end" ]).each(
				function(index, o) {
					$(o).enableDatePicker({
						showOn : "button",
						buttonImage : imgHomePath + "/calendar.gif",
						buttonImageOnly : true
					})
					$(o).mask(twoDigitDateMask, {
						autoclear : false
					})
				})
	})
</script>

<c:out value="${widgetLabel}" />
: Begin
<input type="text" id="${widgetId}_begin" ${textInputAttributes}
	value="${initialBeginValue}" style="width: 100px" />
End
<input type="text" id="${widgetId}_end" ${textInputAttributes}
	value="${initialEndValue}" style="width: 100px" />