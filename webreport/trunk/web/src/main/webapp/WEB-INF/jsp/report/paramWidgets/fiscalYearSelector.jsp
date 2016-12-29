<%@ include file="../../shared/inc_header.jsp"%>

<tiles:importAttribute name="widgetId" scope="page" />
<%-- either "single" or "multiple" --%>
<tiles:importAttribute name="mode" scope="page" />

<c:set var="multipleAttr" value='multiple="single"' />
<c:set var="multiple" value="false" />

<c:if test="${mode == 'single'}">
	<c:set var="multiple" value="false" />
	<c:set var="multipleAttr" value="" />

</c:if>

<script type="text/javascript">
	(function() {
		var widgetId = "<c:out value="${widgetId}" />"

		registerWidget({
			refresh : function(params, refreshCompleteCallback) {
				
				if (${multiple == 'true'}) {
					$('#' + widgetId).multiselect({
						selectedText : function(numChecked, numTotal, checkedItems) {
							return numChecked + ' of ' + numTotal + ' checked'
						},
						height: 300,
						minWidth: 250,
						multiple : ${multiple}
					}).multiselectfilter()
				}
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
<select ${multipleAttr} id="${widgetId}">
	<c:forEach items="${allFiscalYears}" var="fiscalYear">
		<option value="${fiscalYear}"><c:out value="${fiscalYear}" /></option>
	</c:forEach>
</select>
