<%@ include file="../shared/inc_header.jsp"%>

<%--
	Requires the following methods in the parent page:
	
	function volunteerRequirementUpdatedCallback()
		called after the volunteer requirement is successfully updated
		
	function (map) getVolunteerRequirementData()
		returns a map of volunteer requirement IDs to volunteer requirement objects
	function (void) retrieveVolunteerRequirementsByScope(volunteerRequirementId, callbackFunction)
		a function that retrieves the complete set of volunteer requirements for the volunteer
		with the specified volunteerRequirementId, and calls the callbackFunction when complete
 --%>

<script type="text/javascript">
var allRequirementStatuses = []
<c:forEach items="${allRequirementStatuses}" var="status">
	allRequirementStatuses.push({
		id : ${status.id},
		name : "<c:out value="${status.name}" />"
	})
</c:forEach>

$(function() {
	var submitVolunteerRequirement = function() {
		var myVolunteerRequirementId = $("#volunteerRequirementId").val()
		
		if (!validateVolunteerRequirement())
			return false

		$.ajax({
			url : ajaxHomePath + '/volunteer/volunteerRequirement/update',
			method: 'POST',
			dataType : 'json',
			data : {
				volunteerRequirementId: myVolunteerRequirementId,
				requirementDate: $("#volunteerRequirementDate").val(),
				status: $("#volunteerRequirementStatus").val(),
				comments: $("#volunteerRequirementComments").val()
			},
			error : commonAjaxErrorHandler,
			success : function(response) {
				$("#volunteerRequirementDialog").dialog('close')
				volunteerRequirementUpdatedCallback()
		    }
		})
	}
	
	$("#volunteerRequirementDate").enableDatePicker({
		showOn : "button",
		buttonImage : imgHomePath + "/calendar.gif",
		buttonImageOnly : true
	})
	$("#volunteerRequirementDate").mask(twoDigitDateMask)
	
	var dialogEl = $("#volunteerRequirementDialog")
	dialogEl.dialog({
		autoOpen : false,
		modal : false,
		width : 750,
		height : 400,
		closeOnEscape : true,
		draggable : true,
		resizable : true,
		buttons : {
			'Submit' : {
				text : 'Submit',
				id : 'volunteerRequirementSubmit',
				click : function() {
					submitVolunteerRequirement()
				}
			},
			'Cancel' : function() {
				$(this).dialog('close')
			}
		}
	})
	
	$("#volunteerRequirementDialog").show()
})

function validateVolunteerRequirement() {
	var allErrors = new Array()
	
	var dateVal = $('#volunteerRequirementDate').val()
	if (dateVal != '' && !validateDate(dateVal)) {
		allErrors.push('Please enter a valid date.')
	}

	if (allErrors.length > 0)
		displayAttentionDialog("Please correct the following errors: <ul><li>"
				+ allErrors.join("</li><li>") + "</li></ul>");

	return allErrors.length == 0
}

function showVolunteerRequirementPopup(volunteerRequirementId) {
	var volReq = getVolunteerRequirementData()[volunteerRequirementId]
	
	$("#volunteerRequirementId").val(volReq.id)

	$("#volunteerRequirementFacilitySpan").text(volReq.requirement.facilityScope ? volReq.requirement.facilityScope.displayName : 'National')
	$("#volunteerRequirementNameSpan").text(volReq.requirement.name)
	$("#volunteerRequirementTypeSpan").text(volReq.requirement.type.name)
	
	var dateLocked = (volReq.requirement.type.code == 'N')
	var statusLocked = (volReq.requirement.type.code == 'N')
	
	$("#volunteerRequirementDate").prop('disabled', dateLocked)
	$("#volunteerRequirementStatus").prop('disabled', statusLocked)
	
	var dateTypeText = volReq.requirement.dateType ? volReq.requirement.dateType.name : ''
	$("#volunteerRequirementDateTypeSpan").text(dateTypeText)
	var isDateTypeNotApplicable = (dateTypeText == '${DateValueNotApplicable.name}')
	$("#volunteerRequirementDateRow").toggle(!isDateTypeNotApplicable)
	if (isDateTypeNotApplicable) {
		$("#volunteerRequirementDate").val('')
	} else {
		// Date should not be editable if date type is not applicable
		$("#volunteerRequirementDate").val(volReq.requirementDate)
	}
	
	$("#volunteerRequirementTMSCourseIDSpan").text(volReq.requirement.tmsCourseId || "(none)")
	$("#volunteerRequirementPreventTimePostingSpan").text(volReq.requirement.preventTimeposting ? 'Yes' : 'No')
	
	var curVal = volReq.status ? volReq.status.id : -1
	var statusEl = $("#volunteerRequirementStatus")
	statusEl.empty()
	var foundCurrentStatus = false
	var newHtml = []
	$.each(volReq.requirement.basicAvailableStatuses, function(index, item) {
		var selectedText = (item.id == curVal) ? ' selected="selected"' : ''
		if (item.id == curVal) foundCurrentStatus = true
		newHtml.push('<option value="' + item.id + '"' + selectedText + '>' + item.name + '</option>')
	})
	if (!foundCurrentStatus)
		newHtml.push('<option value="' + volReq.status.id + '" selected="selected">' + volReq.status.name
				+ ' (no longer applicable)</option>')
	statusEl.html(newHtml.join(''))
	statusEl.val(curVal)
	
	$("#volunteerRequirementComments").val(volReq.comments)
	$("#volunteerRequirementOtherRoles").empty()
	
	$("#volunteerRequirementDialog").dialog('open')
	
	retrieveVolunteerRequirementsByScope(volunteerRequirementId, function(volunteerRequirementsByScope, volunteerAssignmentData) {
		var applicableAssignments = []
		for (var i = 0; i < volunteerRequirementsByScope.globalAll.length; i++) {
			if (volunteerRequirementsByScope.globalAll[i].id == volunteerRequirementId) {
				applicableAssignments.push($("<li>Global Requirement</li>"))
				break
			}
		}
		for (var i = 0; i < volunteerRequirementsByScope.facilityAll.length; i++) {
			if (volunteerRequirementsByScope.facilityAll[i].id == volunteerRequirementId) {
				applicableAssignments.push($("<li>Facility Requirement</li>"))
				break
			}
		}
		$.each(volunteerRequirementsByScope.byAssignment, function(volAssignmentId, volReqs) {
			for (var i = 0; volReqs && i < volReqs.length; i++) {
				if (volReqs[i].id == volunteerRequirementId) {
					applicableAssignments.push($("<li></li>").text(volunteerAssignmentData[volAssignmentId].displayName))
					break
				}
			}
		})
		$("#volunteerRequirementOtherRoles").append(applicableAssignments)
	})
}
</script>

<div id="volunteerRequirementDialog" style="display: none"
	title="Volunteer Requirement Details">
	<input type="hidden" id="volunteerRequirementId" value="" />

	<div class="clearCenter volunteerRequirementInputFields">
		<div class="leftHalf" style="max-width:350">
			<table style="max-width:350px">
				<tr>
					<td class='appFieldLabel'>Facility:</td>
					<td><span id="volunteerRequirementFacilitySpan"></span></td>
				</tr>
				<tr>
					<td class='appFieldLabel'>Name:</td>
					<td><span id="volunteerRequirementNameSpan"></span></td>
				</tr>
				<tr>
					<td class='appFieldLabel'>Type:</td>
					<td><span id="volunteerRequirementTypeSpan"></span></td>
				</tr>
				<tr>
					<td class='appFieldLabel'>TMS Course ID:</td>
					<td><span id="volunteerRequirementTMSCourseIDSpan"></span></td>
				</tr>
				<tr valign="bottom">
					<td class='appFieldLabel'>Prevent Kiosk Time Posting<br>
						if not "Met":
					</td>
					<td><span id="volunteerRequirementPreventTimePostingSpan"></span></td>
				</tr>

			</table>
		</div>
		<div class="rightHalf" style="margin-left: 20px">
			<table>
				<tr>
					<td class='appFieldLabel'>Date Type:</td>
					<td></td>
					<td><span id="volunteerRequirementDateTypeSpan"></span></td>
				</tr>
				<tr id="volunteerRequirementDateRow">
					<td align="right">Date:</td>
					<td width="10"></td>
					<td><input type="text" id="volunteerRequirementDate"
						size="13" /></td>
				</tr>
				<tr>
					<td class='appFieldLabel' nowrap>Status:</td>
					<td></td>
					<td colspan="4"><select id="volunteerRequirementStatus"></select></td>
				</tr>
			</table>
			<p>This requirement applies to the following roles:
			<ul id="volunteerRequirementOtherRoles" style="max-width:275px">
			</ul>
		</div>
	</div>
	<div class="clearCenter volunteerRequirementInputFields">
		<table>
			<tr>
				<td>Comments:<br> <textarea rows="4" cols="55"
						id="volunteerRequirementComments" maxlength="2000"></textarea></td>
			</tr>
		</table>
	</div>
</div>