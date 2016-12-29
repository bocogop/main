<%@ include file="../../shared/inc_header.jsp"%>

<tiles:importAttribute name="widgetId" scope="page" />

<%-- either "single" or "multiple" --%>
<tiles:importAttribute name="mode" scope="page" />

<c:set var="multipleAttr" value='multiple="multiple"' />
<c:set var="multiple" value="true" />
<c:set var="selected" value="selected" />
<c:if test="${mode == 'single'}">
	<c:set var="multiple" value="false" />
	<c:set var="multipleAttr" value="" />
	<c:set var="selected" value="" />
</c:if>

<script type="text/javascript">
	(function() {
		var widgetId = "<c:out value="${widgetId}" />"

		registerWidget({
			refresh : function(params, refreshCompleteCallback) {
				$.ajax({
					url : ajaxHomePath + "/organizationTypeList",
					data : params,
					type : "GET",
					dataType : 'json',
					error : commonAjaxErrorHandler,
					success : function(responseList) {
						$('#' + widgetId).empty()
												
						$('#' + widgetId).append($('<option>', {
							value : '-1',
							text : 'Missing Org Type',
							selected : '${selected}'
						}))
								
						$.each(responseList, function(index, item) {
							$('#' + widgetId).append($('<option>', {
								value : item.id,
								text : item.name,
								selected : '${selected}'
							}))
						})
					

						$('#' + widgetId).multiselect({
							selectedText : function(numChecked, numTotal, checkedItems) {
								if (numChecked == 1) {
									return $(checkedItems[0]).next().text()
								} else if (numChecked == numTotal) {
									return "(all)"
								}
								return numChecked + ' of ' + numTotal + ' checked'
							},
							height: 200,
							multiple : ${multiple}
						})
						
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

<c:out value="${widgetLabel}" />:
<select ${multipleAttr} id="${widgetId}"></select>