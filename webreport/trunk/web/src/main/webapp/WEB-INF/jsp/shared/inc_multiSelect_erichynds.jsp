<%@ include file="../shared/inc_header.jsp"%>

<%-- Escaping XML here for good practice since we use the raw EL expressions later --%>
<c:set var="reportParamId" value="${fn:escapeXml(param.reportParamId)}" />
<c:set var="reportParamPrompt"
	value="${fn:escapeXml(param.reportParamPrompt)}" />
<c:set var="quickSearchPrompt"
	value="${fn:escapeXml(param.quickSearchPrompt)}" />
<c:set var="selectListPopulatorURI"
	value="${fn:escapeXml(param.selectListPopulatorURI)}" />
<c:set var="selectedParamLabel"
	value="${fn:escapeXml(param.selectedParamLabel)}" />

<div id='${reportParamId}_container_div'
	class="ssrsParametersContainerDiv">
	<div id='${reportParamId}_selectContainer'>
		<label for='${reportParamId}'>${reportParamPrompt}:</label> <select
			class="ssrsParameters" multiple="multiple" id="${reportParamId}"
			name="${reportParamId}">
		</select>
		<div id='${reportParamId}_selectedItems'></div>
	</div>
</div>

<script type="text/javascript">
	$(function() {
		function setSelectListItems(responseList) {
			$.each(responseList, function(i, itemInResponseList) {
				$('#${reportParamId}').append($('<option>', {
					value : itemInResponseList.id,
					text : itemInResponseList.displayName
				}))
			})

			enableMultiSelect()
		}

		function refresh() {
			$.ajax({
				url : ajaxHomePath + "${selectListPopulatorURI}",
				type : "GET",
				dataType : 'json',
				error : commonAjaxErrorHandler,
				success : setSelectListItems
			})
		}

		refresh()

		function resetSelectedOptionsDiv() {
			$('#${reportParamId}_selectedItems').html('')
			$('#${reportParamId}_selectedItems').css('height', '0px');
		}

		function setItemsToSelectedOptionsDiv(selectedTextValue) {
			$('#${reportParamId}_selectedItems').html(selectedTextValue)
			$('#${reportParamId}_selectedItems').css('overflow-y', 'auto')
			$('#${reportParamId}_selectedItems').css('margin-top', '20px')
			$('#${reportParamId}_selectedItems').css('height', '100px')
		}

		function enableMultiSelect() {
			$('#${reportParamId}').multiselect({
				selectedText : function(numChecked, numTotal, checkedItems) {
					var selectedTextValue = '${selectedParamLabel}:'

					$.each(checkedItems, function() {
						selectedTextValue = selectedTextValue + '<br/>' + $(this).attr('title')
					})

					resetSelectedOptionsDiv()
					setItemsToSelectedOptionsDiv(selectedTextValue);

					return numChecked + ' of ' + numTotal + ' checked'
				},
				uncheckAll : function() {
					resetSelectedOptionsDiv()
				},
				click : function(event, ui) {
					if ($(this).multiselect("widget").find("input:checked").length == 0) {
						resetSelectedOptionsDiv()
					}
				}
			}).multiselectfilter()
		}
	})
</script>