<%@ include file="../../shared/inc_header.jsp"%>

<tiles:importAttribute name="widgetId" scope="page" />

<script type="text/javascript">
	(function() {
		var widgetId = "<c:out value="${widgetId}" />"

		registerWidget({
			refresh : function(params, refreshCompleteCallback) {
				$.ajax({
					url : ajaxHomePath + "/assignedVISNs",
					data : $.extend({}, {
						// add here as needed - CPB
					}, params),
					type : "GET",
					dataType : 'json',
					error : commonAjaxErrorHandler,
					success : function(responseList) {
						$('#' + widgetId).empty()
						$.each(responseList, function(index, item) {
							$('#' + widgetId).append($('<option>', {
								value : item.number,
								text : item.displayName
							}))
						})

						$('#' + widgetId).multiselect({
							selectedText : function(numChecked, numTotal, checkedItems) {
								return numChecked + ' of ' + numTotal + ' checked'
							}
						}).multiselectfilter()
						
						refreshCompleteCallback()
					}
				})
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
<select multiple="multiple" id="${widgetId}"></select>