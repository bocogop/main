<%@ include file="../shared/inc_header.jsp"%>

<script type="text/javascript">

$(function() {
	initEditStaffTitlePopup() 
})

function initEditStaffTitlePopup() {
	
	var dialogEl = $("#editStaffTitleDialog")
	dialogEl.dialog({
		autoOpen : false,
		modal : false,
		width : 600,
		height : 300,
		closeOnEscape : true,
		draggable : true,
		resizable : true,
		buttons : {
			'Submit' : {
				text : 'Submit',
				id : 'editStaffTitleSubmit',
				click : function() {
					submitEditStaffTitle()
				}
			},
			'Cancel' : function() {
				$(this).dialog('close')
			}
		}
	})
	
	$("#editStaffTitleDialog").show()
}


function submitEditStaffTitle() {
	var staffTitleId = $("#editStaffTitleDialog").data('staffTitleId')
	var name = $("#editStaffTitleName").val()
	var description = $("#editStaffTitleDescription").val()
	
	if (!validateEditStaffTitle())
		return false
	
	$.ajax({
		url : ajaxHomePath + '/staffTitle/saveOrUpdate',
		method: 'POST',
		dataType : 'json',
		data : {
			staffTitleId : $("#editStaffTitleDialog").data('staffTitleId'),
			name : name,
			description : description,
			isChief:	$("input[type='radio'][name='staffTitleIsChief']:checked").val(),
			isChiefSupervisor:	$("input[type='radio'][name='staffTitleIsChiefSupervisor']:checked").val(),
			isActive : $("#editStaffTitleActive").is(":checked")
		},
		error : commonAjaxErrorHandler,
		success : function(response) {
			$("#editStaffTitleDialog").dialog('close')
			refreshStaffTitleTable()
	    }
	})
}

function showStaffTitlePopup(staffTitleId) {
	
	var isDisabled = <c:out value="${FORM_READ_ONLY}" default="false" />
	
	var isNew = typeof staffTitleId == 'undefined'
	
	$("#editStaffTitleDialog").dialog('option', 'title', isNew ? 'New Staff Title' : 'Edit Staff Title')
	$("#editStaffTitleDialog").data('staffTitleId', staffTitleId || '')

	var staffTitleObj = isNew ? null : staffTitleMap[staffTitleId]
	$("#editStaffTitleName").val(isNew ? '' : staffTitleObj.name)
	$("#editStaffTitleDescription").val(isNew ? '' : staffTitleObj.description)
	$("#editStaffTitleActive").prop('checked', isNew ? true : !staffTitleObj.inactive)
	$("input[name=staffTitleIsChief][value=" + (!isNew && staffTitleObj.chief ? "Yes" : "No")
	                              + "]").prop("checked", true)
	$("input[name=staffTitleIsChiefSupervisor][value=" + (!isNew && staffTitleObj.chiefSupervisor ? "Yes" : "No")
	                              + "]").prop("checked", true)
	
	$(".staffTitlePopupInputs").prop('disabled', isDisabled)
	$("#editStaffTitleDialog").dialog('open')
}

function validateEditStaffTitle() {
	var allErrors = new Array()
	
	var nameVal = $.trim($("#editStaffTitleName").val())
	if (nameVal == '') {
		allErrors.push('Please enter the Name.')
	}

	if (allErrors.length > 0)
		displayAttentionDialog("Please correct the following errors: <ul><li>"
				+ allErrors.join("</li><li>") + "</li></ul>");

	return allErrors.length == 0
}

</script>

<div id="editStaffTitleDialog" style="display: none"
	title="Edit StaffTitle">
	
	<div class="clearCenter staffTitlePopupInputs"">
		<table>
			<tr>
				<td align="right"><label for="editStaffTitleName">Name:
				<span class="invisibleRequiredFor508">*</span></label></td>
				<td style="padding: 4px; text-align: center" width="1"><span
									class='requdIndicator'>*</span></td>
				<td><input type="text" id="editStaffTitleName" title="Name of Staff Title" size="50"
							maxLength="50"/></td>
			</tr>
			<tr>
				<td align="right"><label for="editStaffTitleDescription">Description:
				</label></td>
				<td></td>
				<td>
				<textarea id="editStaffTitleDescription" title="Description of Staff Title" rows="3" cols="50"  maxlength="250"></textarea></td>
			</tr>
			
			<tr>
						<td class='appFieldLabel' nowrap>Is Chief?
						</td>
						<td></td>
						<td><nobr>
								<input type="radio" name="staffTitleIsChief"
									id="staffTitleIsChiefYes" value="Yes" />Yes <input
									type="radio" name="staffTitleIsChief"
									id="staffTitleIsChiefNo" value="No" />No
							</nobr></td>
					</tr>
				<tr>
					<td class='appFieldLabel' nowrap>Is Chief Supervisor?</td>
					<td></td>
						<td><nobr>
								<input type="radio" name="staffTitleIsChiefSupervisor"
									id="staffTitleIsChiefSupervisorYes" value="Yes" />Yes <input
									type="radio" name="staffTitleIsChiefSupervisor"
									id="staffTitleIsChiefSupervisorNo" value="No" />No
							</nobr></td>
				</tr>
				<tr>
					<td></td>
					<td></td>
					<td><input type="checkbox"
						id="editStaffTitleActive" value="true"> Is Active</td>		
				</tr>
					
		</table>
	</div>
</div>