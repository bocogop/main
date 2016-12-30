<%@ include file="../shared/inc_header.jsp"%>

<script type="text/javascript">

var roleTypeGeneralId = ${BENEFITING_SERVICE_ROLE_TYPE_VALUE_GENERAL.id}

$(function() {
	initEditServiceRolePopup(${precinctContextId})
})

function initEditServiceRolePopup(precinctId) {
	var submitEditServiceRole = function() {
		var isNew = dialogEl.data('benefitingServiceRoleId') == ''
		
		var name = $("#editServiceRoleName").val()
		
		var errors = new Array()
		if ($.trim(name) == '')
			errors.push('Please enter the name.')
		
		var contactPhone = $("#editServiceRoleContactPhone").val()
		if ($.trim(contactPhone) != '' && !validatePhone(contactPhone))
			errors.push("Please enter a valid phone number.")
		
		var contactEmail = $("#editServiceRoleContactEmail").val()
		if (!validateEmail(contactEmail))
			errors.push("Please enter a valid contact email in the format 'user@domain.tld'.")
		
		if (errors.length > 0) {
			displayAttentionDialog("Please correct the following errors: <ul><li>"
					+ errors.join("</li><li>") + "</li></ul>");
			return
		}
		
		var locationIds = null
		if (isNew) {
			locationIds = $("#newServiceRolePhysicalLocation").val()
			if (locationIds.length == 0) {
				displayAttentionDialog('Please select at least one location.')
				return
			}
		} else {
			locationIds = [$("#editServiceRolePhysicalLocation").val()]
		}
		
		$.ajax({
			url : ajaxHomePath + '/benefitingServiceRole/saveOrUpdate',
			method: 'POST',
			dataType : 'json',
			data : {
				benefitingServiceRoleId : dialogEl.data('benefitingServiceRoleId'),
				benefitingServiceId : dialogEl.data('benefitingServiceId'),
				precinctId : precinctId,
				locationId : locationIds,
				name : name,
				description : $("#editServiceRoleDescription").val(),
				contactName : $("#editServiceRoleContactName").val(),
				contactEmail : $("#editServiceRoleContactEmail").val(),
				contactPhone : $("#editServiceRoleContactPhone").val(),
				active : $("#editServiceRoleActive").is(":checked"),
				roleType : $("#editServiceRoleType").val()
			},
			error : commonAjaxErrorHandler,
			success : function(response) {
				$("#editServiceRoleDialog").dialog('close')
				refreshBenefitingServicesTable()
		    }
		})
	}
	
	var dialogEl = $("#editServiceRoleDialog")
	dialogEl.dialog({
		autoOpen : false,
		modal : false,
		width : 650,
		height : 360,
		closeOnEscape : true,
		draggable : true,
		resizable : true,
		buttons : {
			'Submit' : {
				text : 'Submit',
				id : 'editServiceRoleSubmit',
				click : function() {
					doubleClickSafeguard($("#editServiceRoleSubmit"))
					submitEditServiceRole()
				}
			},
			'Cancel' : function() {
				$(this).dialog('close')
			}
		}
	})
	dialogEl.data('precinctId', precinctId)
		
	var newLocationEl = $("#newServiceRolePhysicalLocation")
	newLocationEl.multiselect({
		selectedText : function(numChecked, numTotal, checkedItems) {
			if (numChecked > 1)
				return numChecked + ' of ' + numTotal + ' checked'
			return abbreviate($(checkedItems[0]).next().text())
		},
		beforeopen: function() {
			if (dialogEl.data('newStationsPopulated')) return
			getLocalPrecinctsForLocation($("#precinctId").val(), true, function(locations) {
				var curVal = newLocationEl.val()
				
				newLocationEl.empty()
				var newHtml = []
				newHtml.push('<option value="-1">Main Precinct</option>')
				
				$.each(locations, function(index, item) {
					var selectedText = (item.id == curVal) ? ' selected="selected"' : ''
					newHtml.push('<option value="' + item.id + '"' + selectedText + '>' + item.displayName + '</option>')
				})
				newLocationEl.html(newHtml.join(''))
				
				newLocationEl.val(curVal)
				newLocationEl.multiselect("refresh")
				dialogEl.data('newStationsPopulated', true)
				
				newLocationEl.multiselect("open")
			})
			return false
	   },
		multiple : true,
		minWidth : 300
	})

	var editLocationEl = $("#editServiceRolePhysicalLocation")
	editLocationEl.multiselect({
		selectedText : function(numChecked, numTotal, checkedItems) {
			return abbreviate($(checkedItems[0]).next().text())
		},
		beforeopen: function() {
			if (dialogEl.data('editStationsPopulated')) return
			getLocalPrecinctsForLocation($("#precinctId").val(), true, function(locations) {
				var curVal = editLocationEl.val()
				
				editLocationEl.empty()
				var newHtml = []
				newHtml.push('<option value="-1">Main Precinct</option>')
				
				$.each(locations, function(index, item) {
					var selectedText = (item.id == curVal) ? ' selected="selected"' : ''
					newHtml.push('<option value="' + item.id + '"' + selectedText + '>' + item.displayName + '</option>')
				})
				editLocationEl.html(newHtml.join(''))
				
				editLocationEl.val(curVal)
				editLocationEl.multiselect("refresh")
				dialogEl.data('editStationsPopulated', true)
				
				editLocationEl.multiselect("open")
			})
			return false
	   },
		multiple : false,
		minWidth : 300
	})
		
	$("#editServiceRoleDialog").show()
}

/* mode can be 'new' (signifying create new role under the specified benefitingServiceId) 
 * or 'convert' (signifying the benefiting service with the specified ID will be converted to a new role) */
function showEditServiceRolePopup(mode, benefitingServiceRoleId, benefitingServiceId) {
	var isNew = (mode == 'new')
	
	var benefitingService = isNew ? benefitingServiceMap[benefitingServiceId] : null 
	var benefitingServiceRole = isNew ? null : benefitingServiceRoleMap[benefitingServiceRoleId]
	
	$("#editServiceRoleDialog").dialog('option', 'title', isNew ? 'New Benefiting Service Role' : 
		'Edit Benefiting Service Role');
	$("#editServiceRoleDialog").data('benefitingServiceRoleId', benefitingServiceRoleId || '')
	$("#editServiceRoleDialog").data('benefitingServiceId', benefitingServiceId || '')
	$("#advancedServiceRoleOptionsLink").toggle(!isNew && benefitingServiceRole.scope == 'LOCAL')
	
	$("#editServiceRoleName").val(isNew ? '' : benefitingServiceRole.name)
	
	$("#editServiceRoleContactName").val(isNew ? '' : benefitingServiceRole.contactName)
	$("#editServiceRoleContactEmail").val(isNew ? '' : benefitingServiceRole.contactEmail)
	$("#editServiceRoleContactPhone").val(isNew ? '' : benefitingServiceRole.contactPhone)
	$("#editServiceRoleDescription").val(isNew ? '' : benefitingServiceRole.description)
	$("#editServiceRoleType").val(isNew || !benefitingServiceRole.roleType ? roleTypeGeneralId : benefitingServiceRole.roleType.id)
	
	$("#editServiceRoleActive").prop('checked', isNew ? true : !benefitingServiceRole.inactive)
	$("#editServiceRoleName").prop('disabled', isNew ? false : benefitingServiceRole.requiredAndReadOnly)
	$("#editServiceRoleActive").prop('disabled', isNew ? false : benefitingServiceRole.requiredAndReadOnly)
	$("#editServiceRoleType").prop('disabled', isNew ? false : benefitingServiceRole.requiredAndReadOnly)
	
	$("#editServiceRolePhysicalLocationDiv").toggle(!isNew)
	$("#newServiceRolePhysicalLocationDiv").toggle(isNew)
	
	if (isNew) {
		$("#editServiceRoleDialog").data('newStationsPopulated', false)
		$("#newServiceRolePhysicalLocation").val('-1')
		$("#newServiceRolePhysicalLocation").multiselect("refresh")
	} else {
		$("#editServiceRoleDialog").data('editStationsPopulated', false)
		$("#editServiceRolePhysicalLocation").empty().append($('<option value="' + benefitingServiceRole.locationId + '" selected>').text(benefitingServiceRole.locationDisplayName))
		$("#editServiceRolePhysicalLocation").multiselect("refresh")
	}
	
	$("#editServiceRoleDialog").dialog('open')
}

function mergeRole() {
	var benefitingServiceId = $("#editServiceRoleDialog").data('benefitingServiceId')
	var benefitingServiceRoleId = $("#editServiceRoleDialog").data('benefitingServiceRoleId')
	showMergeRolePopup(benefitingServiceRoleId, benefitingServiceId)
}
</script>

<div id="editServiceRoleDialog" style="display: none"
	title="Edit Benefiting Service Role">
	<div class="leftHalf">
		<table>
			<tr>
				<td align="right"><label for='editServiceRoleName'>Name:</label></td>
				<td width="10"><span class="requdIndicator" id="nameRequired">*</span></td>
				<td><input type="text" id="editServiceRoleName"
					title="Type Service Role Name" maxlength="50" /></td>
			</tr>
			<tr>
				<td align="right"><label for='editServiceRoleType'>Role
						Type:</label></td>
				<td width="10"><span class="requdIndicator" id="nameRequired">*</span></td>
				<td><select id="editServiceRoleType">
						<c:forEach items="${allBenefitingServiceRoleTypes}" var="type">
							<option value="${type.id}"><c:out value="${type.name}" /></option>
						</c:forEach>
				</select></td>
			</tr>
			<tr style="display: none">
				<td align="right"><input type="checkbox"
					id="editServiceRoleActive" value="true"></td>
				<td></td>
				<td>Is Active</td>
			</tr>
		</table>
	</div>
	<div class="rightHalf">
		<table>
			<tr>
				<td align="right"><label for='editServiceRoleContactName'>Supervisor
						Name:</label></td>
				<td><input type="text" id="editServiceRoleContactName"
					title="Type service Role contact name" maxlength="50" /></td>
			</tr>
			<tr>
				<td align="right"><label for='editServiceRoleContactEmail'>Supervisor
						Email:</label></td>
				<td><input type="text" id="editServiceRoleContactEmail"
					title="Type service role contact email" maxlength="250" /></td>
			</tr>
			<tr>
				<td align="right"><label for='editServiceRoleContactPhone'>Supervisor
						Phone:</label></td>
				<td><input type="text" id="editServiceRoleContactPhone"
					title="Type service role contact phone" maxlength="30"
					class="phoneextmask" /></td>
			</tr>
		</table>
	</div>
	<div class="clearCenter" style="padding-top: 10px; text-align: left"><label for='editServiceRoleDescription'>
		Description:<br></label>
		<textarea rows="4" cols="60" id="editServiceRoleDescription" title="Type service role description" 
			maxlength="4000"></textarea>
	</div>

	<div id="newServiceRolePhysicalLocationDiv" class="clearCenter"
		style="padding-top: 10px;"><label for='newServiceRolePhysicalLocation'>
		Physical Location: </label><select id="newServiceRolePhysicalLocation"
			multiple="multiple">
			<option value="-1" selected="selected">Main Precinct</option>
		</select>
	</div>

	<div id="editServiceRolePhysicalLocationDiv" class="clearCenter"
		style="padding-top: 10px;"><label for='editServiceRolePhysicalLocation'>
		Physical Location: </label><select id="editServiceRolePhysicalLocation">
			<option value="-1" selected="selected">Main Precinct</option>
		</select>
	</div>

	<div class="clearCenter" style="padding-top: 10px; text-align: left">
		<a href="#" id="advancedServiceRoleOptionsLink"
			data-jq-dropdown="#editServiceRoleAdvancedOptions">Advanced
			Options...</a>
	</div>
</div>

<div id="editServiceRoleAdvancedOptions"
	class="jq-dropdown" style="display: none">
	<ul class="jq-dropdown-menu">
		<li><a href="javascript:mergeRole()">Merge Role</a></li>
	</ul>
</div>

<%@ include file="mergeRolePopup.jsp"%>
