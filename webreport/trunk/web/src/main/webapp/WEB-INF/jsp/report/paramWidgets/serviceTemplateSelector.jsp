<%@ include file="../../shared/inc_header.jsp"%>

<tiles:importAttribute name="widgetId" scope="page" />

<%-- either "single" or "multiple" --%>
<tiles:importAttribute name="mode" scope="page" />

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
					url : ajaxHomePath + "/serviceTemplateList",
					data : params,
					type : "GET",
					dataType : 'json',
					error : commonAjaxErrorHandler,
					success : function(responseList) {
						var theWidget = $('#' + widgetId)
						theWidget.empty()
						$.each(responseList, function(index, item) {
							theWidget.append($('<option>', {
								value : item.id,
								text : item.name
							}))
						})

						theWidget.change(function(evt) {
							var isChecked = theWidget.data('mostRecentWasChecked')
							var clickedVal = theWidget.data('mostRecentValue')
							
							if (clickedVal == -1) {
								theWidget.val('-1')
							} else {
								if (isChecked) {
									$("option[value='-1']", theWidget).prop('selected', false)
								} else {
									if (theWidget.val() == null) {
										theWidget.val('-1')
									}
								}
							}
							theWidget.data('mostRecentWasChecked', false)
							theWidget.data('mostRecentValue', false)
							theWidget.multiselect("refresh")
						})
						
						theWidget.multiselect({
							selectedText : function(numChecked, numTotal, checkedItems) {
								if (numChecked == 1)
									return $(checkedItems[0]).next().text()
								return numChecked + ' of ' + (numTotal) + ' checked'
							},
							height: 300,
							minWidth: 250,
							multiple : ${multiple},
							click: function(event, ui) {
								theWidget.data('mostRecentWasChecked', ui.checked)
								theWidget.data('mostRecentValue', ui.value)
							},
							checkAll: function() {
								theWidget.data('mostRecentWasChecked', false)
								theWidget.data('mostRecentValue', false)
								$("option[value='-1']", theWidget).prop('selected', false)
								theWidget.multiselect("refresh")
							},
							uncheckAll: function() {
								theWidget.data('mostRecentWasChecked', false)
								theWidget.data('mostRecentValue', false)
								$("option[value='-1']", theWidget).prop('selected', true)
								theWidget.multiselect("refresh")
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
<select ${multipleAttr} id="${widgetId}" style="display:none"></select>