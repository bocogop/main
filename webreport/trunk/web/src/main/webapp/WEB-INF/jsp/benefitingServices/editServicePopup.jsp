<%@ include file="../shared/inc_header.jsp"%>

<script type="text/javascript">
$(function() {
	initEditServicePopup(${precinctContextId})
})

function initEditServicePopup(precinctId) {
	var submitEditService = function() {
		var name = $("#editServiceName").val()
		
		var errors = new Array()
		if ($.trim(name) == '')
			errors.push('Please enter the name.')

		if (errors.length > 0) {
			displayAttentionDialog("Please correct the following errors: <ul><li>"
					+ errors.join("</li><li>") + "</li></ul>");
			return
		}
		
		// $("#editServicePhysicalLocation").val(),
		// see https://bugs.jquery.com/ticket/13097
		var locationIds = $('#editServicePhysicalLocation option:selected').map(function(i,v) {
			    return this.value;
			}).get()
		
		if (locationIds.length == 0) {
			displayAttentionDialog('Please select at least one location.')
			return
		}
		
		$.ajax({
			url : ajaxHomePath + '/benefitingService/saveOrUpdate',
			method: 'POST',
			dataType : 'json',
			data : {
				benefitingServiceId : dialogEl.data('benefitingServiceId'),
				precinctId : $("#precinctId").val(),
				locationId : locationIds,
				name : name,
				subdivision : $("#editServiceSubdivision").val(),
				abbreviation : $("#editServiceAbbreviation").val(),
				active : $("#editServiceActive").is(":checked"),
				gamesRelated : $("#editServiceGamesRelated").is(":checked")
			},
			error : commonAjaxErrorHandler,
			success : function(response) {
				$("#editServiceDialog").dialog('close')
				refreshBenefitingServicesTable()
		    }
		})
	}
	
	var dialogEl = $("#editServiceDialog")
	dialogEl.dialog({
		autoOpen : false,
		modal : false,
		width : 500,
		height : 260,
		closeOnEscape : true,
		draggable : true,
		resizable : true,
		buttons : {
			'Submit' : {
				text : 'Submit',
				id : 'editServiceSubmit',
				click : function() {
					doubleClickSafeguard($("#editServiceSubmit"))
					submitEditService()
				}
			},
			'Cancel' : function() {
				$(this).dialog('close')
			}
		}
	})
	
	dialogEl.data('precinctId', precinctId)
	
	var locationEl = $("#editServicePhysicalLocation")
	locationEl.multiselect({
		selectedText : function(numChecked, numTotal, checkedItems) {
			if (numChecked > 1)
				return numChecked + ' of ' + numTotal + ' checked'
			return abbreviate($(checkedItems[0]).next().text())
		},
		beforeopen: function(){
			if (dialogEl.data('stationsPopulated')) return
			var curVal = locationEl.val()
			
			getLocalPrecinctsForLocation($("#precinctId").val(), true, function(locations) {
				locationEl.empty()
				var newHtml = []
				newHtml.push('<option value="-1" selected="selected">Main Precinct</option>')
				
				$.each(locations, function(index, item) {
					var selectedText = (item.id == curVal) ? ' selected="selected"' : ''
					newHtml.push('<option value="' + item.id + '"' + selectedText + '>' + item.displayName + '</option>')
				})
				locationEl.html(newHtml.join(''))
				
				locationEl.val(curVal)
				locationEl.multiselect("refresh")
				dialogEl.data('stationsPopulated', true)
				
				locationEl.multiselect("open")
			})
			
			return false
	   },
		multiple : true,
		minWidth : 300
	})
	
	$("#editServiceDialog").show()
}

function showEditServicePopup(benefitingServiceId) {
	var isNew = typeof benefitingServiceId == 'undefined'
	
	$("#editServiceDialog").dialog('option', 'title', isNew ? 'New Benefiting Service' : 'Edit Benefiting Service');
	$("#editServiceDialog").data('benefitingServiceId', benefitingServiceId || '')
	
	var benefitingService = isNew ? null : benefitingServiceMap[benefitingServiceId]
	var editingNationalService = (benefitingService && benefitingService.scope == 'NATIONAL')
	
	$("#editServiceName").val(isNew ? '' : benefitingService.name)
	$("#editServiceName").prop('disabled', editingNationalService)
	
	$("#editServiceSubdivision").val(isNew ? '' : benefitingService.subdivision)
	$("#editServiceSubdivision").prop('disabled', editingNationalService)
	$("#subdivisionRow").toggle($("#editServiceSubdivision").val() != '')
	
	$("#editServiceAbbreviation").val(isNew ? '' : benefitingService.abbreviation)
	$("#editServiceAbbreviation").prop('disabled', editingNationalService)
	
	$("#editServiceGamesRelated").prop('checked', isNew ? false : benefitingService.gamesRelated)
	$("#editServiceGamesRelated").prop('disabled', editingNationalService)
	
	$("#editServiceActive").prop('checked', isNew ? true : !benefitingService.inactive)
	$("#editServiceActiveRow").toggle(isNew)
	
	$("#editServicePhysicalLocationDiv").toggle(isNew)
	$("#editServicePhysicalLocation").val('-1')
	$("#editServicePhysicalLocation").multiselect("refresh")
	
	$("#editServiceDialog").dialog('open')
}
</script>

<div id="editServiceDialog" style="display: none"
	title="Edit Benefiting Service">
	<div class="clearCenter">
		<table>
			<tr>
				<td align="right"><label for='editServiceName'>Name:</label></td>
				<td width="10"><span class="requdIndicator" id="nameRequired">*</span></td>
				<td><input type="text" id="editServiceName"
					title="Type service name" maxlength="35" size="30" /></td>
			</tr>
			<tr id="subdivisionRow">
				<td align="right"><label for='editServiceSubdivision'>Subdivision:</label></td>
				<td></td>
				<td><input type="text" id="editServiceSubdivision"
					title="Type service subdivision" maxlength="30" size="30" /></td>
			</tr>
			<tr>
				<td align="right"><label for='editServiceAbbreviation'>Abbreviation:</label></td>
				<td></td>
				<td><input type="text" id="editServiceAbbreviation"
					title="Type service abbreviation" maxlength="7" size="12" /></td>
			</tr>
			<tr id="editServiceActiveRow" style="display: none">
				<td align="right"></td>
				<td></td>
				<td><input type="checkbox" id="editServiceActive" value="true">
					Is Active</td>
			</tr>
			<tr>
				<td align="right"></td>
				<td></td>
				<td><input type="checkbox" id="editServiceGamesRelated"
					value="true"> Games Service</td>
			</tr>
		</table>
	</div>
	<div id="editServicePhysicalLocationDiv" class="clearCenter"
		style="padding-top: 10px;"><label for='editServicePhysicalLocation'>
		Physical Location: </label><select id="editServicePhysicalLocation"
			multiple="multiple">
			<option value="-1" selected="selected">Main Precinct</option>
		</select>
	</div>
</div>