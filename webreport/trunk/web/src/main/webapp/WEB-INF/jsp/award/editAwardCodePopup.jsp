<%@ include file="../shared/inc_header.jsp"%>

<script type="text/javascript">

$(function() {
	initEditAwardCodePopup() 
})

function initEditAwardCodePopup() {
	
	var dialogEl = $("#editAwardCodeDialog")
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
				id : 'editAwardCodeSubmit',
				click : function() {
					submitEditAwardCode()
				}
			},
			'Cancel' : function() {
				$(this).dialog('close')
			}
		}
	})
	
	$("#editAwardCodeDialog").show()
}


function submitEditAwardCode() {
	var awardCodeId = $("#editAwardCodeDialog").data('awardCodeId')
	var name = $("#editAwardCodeName").val()
	var code = $("#editAwardCodeVal").val()
	var requiredHours = $("#editAwardCodeRequiredHours").val()
	var awardHours = $("#editAwardCodeAwardHours").val()	
	var type = $("#editAwardCodeType").val()	
	
	if (!validateEditAwardCode())
		return false
	
	$.ajax({
		url : ajaxHomePath + '/awardCode/saveOrUpdate',
		method: 'POST',
		dataType : 'json',
		data : {
			awardCodeId : $("#editAwardCodeDialog").data('awardCodeId'),
			code : code,
			name : name,
			requiredHours : requiredHours,
			awardHours : awardHours, 
			type:	type,
			isActive : $("#editAwardCodeActive").is(":checked")
		},
		error : commonAjaxErrorHandler,
		success : function(response) {
			$("#editAwardCodeDialog").dialog('close')
			refreshAwardCodeTable()
	    }
	})
}

function showAwardCodePopup(awardCodeId) {
	
	var isDisabled = <c:out value="${FORM_READ_ONLY}" default="false" />
	
	var isNew = typeof awardCodeId == 'undefined'
	
	$("#editAwardCodeDialog").dialog('option', 'title', isNew ? 'Add New Award Code' : 'Edit Award Code')
	$("#editAwardCodeDialog").data('awardCodeId', awardCodeId || '')

	var awardCodeObj = isNew ? null : awardCodeMap[awardCodeId]
	$("#editAwardCodeName").val(isNew ? '' : awardCodeObj.name)
	$("#editAwardCodeVal").val(isNew ? '' : awardCodeObj.code)
	$("#editAwardCodeRequiredHours").val(isNew ? '' : awardCodeObj.hoursRequired)
	$("#editAwardCodeAwardHours").val(isNew ? '' : awardCodeObj.awardHours)
	$("#editAwardCodeType").val(isNew ? '-1' : awardCodeObj.type)
	$("#editAwardCodeActive").prop('checked', isNew ? true : !awardCodeObj.inactive)
	
	$(".awardCodePopupInputs").prop('disabled', isDisabled)
	$("#editAwardCodeDialog").dialog('open')
}

function validateEditAwardCode() {
	var allErrors = new Array()
	
	var codeVal = $.trim($("#editAwardCodeVal").val())
	if (codeVal == '') {
		allErrors.push('Please enter the Code.')
	}

	var nameVal = $.trim($("#editAwardCodeName").val())
	if (nameVal == '') {
		allErrors.push('Please enter the Name.')
	}

	var requiredHours = $.trim($("#editAwardCodeRequiredHours").val())
	if (requiredHours == '') {
		allErrors.push('Please enter the Required Hours.')
	} else if (!validateInteger(requiredHours)) {
			allErrors.push('Required Hours must be Numeric.')
	}

	var awardHours = $.trim($("#editAwardCodeAwardHours").val())
	if (awardHours == '') {
		allErrors.push('Please enter the Award Hours.')
	} else if (!validateInteger(awardHours)) {
			allErrors.push('Award Hours must be Numeric.')
	}
	
	var type = $("#editAwardCodeType").val()
	if (type == '-1') {
		allErrors.push('Please enter the Award Type.')
	}

	if (allErrors.length > 0)
		displayAttentionDialog("Please correct the following errors: <ul><li>"
				+ allErrors.join("</li><li>") + "</li></ul>", undefined, undefined, {
					height: 235
				});

	return allErrors.length == 0
}

</script>

<div id="editAwardCodeDialog" style="display: none"
	title="Edit Award Code">
	
	<div class="clearCenter awardCodePopupInputs"">
		<table>
			<tr>
				<td align="right"><label for="editAwardCodeVal">Code:
				<span class="invisibleRequiredFor508">*</span></label></td>
				<td style="padding: 4px; text-align: center" width="1"><span
									class='requdIndicator'>*</span></td>
				<td><input type="text" id="editAwardCodeVal" title="Code for the Award" size="2"
							maxLength="2"/></td>
			</tr>

			<tr>
				<td align="right"><label for="editAwardCodeName">Name:
				<span class="invisibleRequiredFor508">*</span></label></td>
				<td style="padding: 4px; text-align: center" width="1"><span
									class='requdIndicator'>*</span></td>
				<td><input type="text" id="editAwardCodeName" title="Name of Award" size="45"
							maxLength="45"/></td>
			</tr>
			
			<tr>
				<td align="right"><label for="editAwardCodeRequiredHours">Required Hours:
				<span class="invisibleRequiredFor508">*</span></label></td>
				<td style="padding: 4px; text-align: center" width="1"><span
									class='requdIndicator'>*</span></td>
				<td><input type="number" id="editAwardCodeRequiredHours" title="Hours required to be eligible for the award" size="6"
							maxLength="6"/></td> 
			</tr>
			
			<tr>
				<td align="right"><label for="editAwardCodeAwardHours">Award Hours:
				<span class="invisibleRequiredFor508">*</span></label></td>
				<td style="padding: 4px; text-align: center" width="1"><span
									class='requdIndicator'>*</span></td>
				<td><input type="number" id="editAwardCodeAwardHours" title="Actual hours required for the award" size="6"
							maxLength="6"/></td>
			</tr>

				<tr>
					<td class='appFieldLabel' nowrap><label for="editAwardCodeType">Type:
					<span class="invisibleRequiredFor508">*</span></label></td>
					<td style="padding: 4px; text-align: center" width="1"><span
									class='requdIndicator'>*</span></td>					<td><select id="editAwardCodeType" title="Select Type">
							<option value="-1">Please select...</option>
							<c:forEach items="${allAwardCodeTypes}" var="type">
								<option value="${type.fullCode}"><c:out
										value="${type.name}" /></option>
							</c:forEach>
					</select></td>
			</tr>
				<tr>
					<td></td>
					<td></td>
					<td><input type="checkbox"
						id="editAwardCodeActive" value="true"> Is Active</td>		
				</tr>
					
		</table>
	</div>
</div>