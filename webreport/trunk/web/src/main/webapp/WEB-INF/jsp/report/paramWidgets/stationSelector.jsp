<%@ include file="../../shared/inc_header.jsp"%>

<tiles:importAttribute name="widgetId" scope="page" />
<tiles:importAttribute name="showAllFacilities" scope="page" ignore="true" />

<%-- Either 'single' or 'multiple' (defaults to multiple if unspecified) - CPB --%>
<tiles:importAttribute name="mode" scope="page" ignore="true" />

<c:if test="${empty showAllFacilities}">
	<c:set var="showAllFacilities" value="false" />
</c:if>

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
					url : ajaxHomePath + "/assignedFacilities",
					data : $.extend({}, {
						'showAllFacilities' : ${showAllFacilities}
					}, params),
					type : "GET",
					dataType : 'json',
					error : commonAjaxErrorHandler,
					success : function(responseList) {
						$('#' + widgetId).empty()
						if (${mode == 'single'}) {
							$('#' + widgetId).append($('<option>', {
									value : '',
									text : 'Please select...'
								}))
						}
						$.each(responseList, function(index, item) {
							$('#' + widgetId).append($('<option>', {
								value : item.id,
								text : item.displayName
							}))
						})

						var theWidget = $('#' + widgetId).multiselect({
							multiple : ${multiple},
							selectedText : function(numChecked, numTotal, checkedItems) {
								if (${multiple}) {
									return numChecked + ' of ' + numTotal + ' checked'
								} else {
									return $(checkedItems[0]).next().text()
								}
							},
							height: 350,
							minWidth : 450
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