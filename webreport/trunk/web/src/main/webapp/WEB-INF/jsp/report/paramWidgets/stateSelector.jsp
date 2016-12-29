<%@ include file="../../shared/inc_header.jsp"%>

<tiles:importAttribute name="widgetId" scope="page" />

<script type="text/javascript">
	(function() {
		var widgetId = "<c:out value="${widgetId}" />"

		registerWidget({
			refresh : function(params, refreshCompleteCallback) {
				$('#' + widgetId).multiselect({
					selectedText : function(numChecked, numTotal, checkedItems) {
						return numChecked + ' of ' + numTotal + ' checked'
					}
				}).multiselectfilter()
				
				refreshCompleteCallback()
			},
			getParameters : function() {
				var theVal = $("#" + widgetId).val()
				return [ {
					displayName : "<c:out value="${widgetLabel}" />",
					paramName : "<c:out value="${widgetParamName}" />",
					paramValue : theVal || ''
				} ]
			},
			changeEventSelectors : [ "#" + widgetId ]
		})
	})()
</script>

<c:out value="${widgetLabel}" />
<p />
<select multiple="multiple" id="${widgetId}">
	<c:forEach items="${allStates}" var="state">
		<option value="${state.id}"><c:out value="${state.displayName}" /></option>
	</c:forEach>
</select>