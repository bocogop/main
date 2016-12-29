<%@ include file="../../shared/inc_header.jsp"%>

<tiles:importAttribute name="widgetId" scope="page" />

<%-- either "single" or "multiple" --%>
<tiles:importAttribute name="mode" scope="page" />

<%-- 'true' or 'false' for each --%>
<tiles:importAttribute name="excludeGames" scope="page" />

<c:set var="multipleAttr" value='multiple="multiple"' />
<c:set var="multiple" value="true" />
<c:if test="${mode == 'single'}">
	<c:set var="multiple" value="false" />
	<c:set var="multipleAttr" value="" />
</c:if>

<script type="text/javascript">
	(function() {
		var widgetId = "<c:out value="${widgetId}" />"

		registerWidget({
			refresh : function(params, refreshCompleteCallback) {
				$.ajax({
					url : ajaxHomePath + "/benefitingServiceList",
					data : $.extend({}, {
						excludeGames : ${excludeGames}
					}, params),
					type : "GET",
					dataType : 'json',
					error : commonAjaxErrorHandler,
					success : function(responseList) {
						$('#' + widgetId).empty()
						$.each(responseList, function(index, item) {
							$('#' + widgetId).append($('<option>', {
								value : item.id,
								text : item.name + ($.trim(item.subdivision) == '' ? '' : " - " + item.subdivision)
							}))
						})

						$('#' + widgetId).multiselect({
							selectedText : function(numChecked, numTotal, checkedItems) {
								return numChecked + ' of ' + numTotal + ' checked'
							},
							height: 300,
							minWidth: 250,
							multiple : ${multiple}
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
<select ${multipleAttr} id="${widgetId}"></select>