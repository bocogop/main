<%@ include file="../../shared/inc_header.jsp"%>

<tiles:importAttribute name="widgetId" scope="page" />

<%-- either "single" or "multiple" --%>
<tiles:importAttribute name="mode" scope="page" />

<%-- 'true' or 'false' for each --%>
<tiles:importAttribute name="includeLocal" scope="page" />
<tiles:importAttribute name="includeNational" scope="page" />
<tiles:importAttribute name="nacOrgsOnly" scope="page" ignore="true" />
<tiles:importAttribute name="includeBranches" scope="page" ignore="true" />

<%-- is preselected --%>
<tiles:importAttribute name="preselectAll" scope="page" ignore="true" />

<c:set var="multipleAttr" value='multiple="multiple"' />
<c:set var="multiple" value="true" />

<c:if test="${empty preselectAll}">
	<c:set var="preselectAll" value="" />
</c:if>

<c:if test="${empty nacOrgsOnly}">
	<c:set var="nacOrgsOnly" value="null" />
</c:if>

<c:if test="${empty includeBranches}">
	<c:set var="includeBranches" value="true" />
</c:if>

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
					url : ajaxHomePath + "/organization",
					data : $.extend({}, {
						includeLocal : ${includeLocal},
						includeNational : ${includeNational},
						includeBranches : ${includeBranches},
						nacOrgsOnly : ${nacOrgsOnly}
					}, params),
					type : "GET",
					dataType : 'json',
					error : commonAjaxErrorHandler,
					success : function(responseList) {
						$('#' + widgetId).empty()
						$.each(responseList, function(index, item) {
							$('#' + widgetId).append($('<option>', {
								value : item.id,
								text : item.displayName,
								selected : '${preselectAll}'
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