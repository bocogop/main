<%@ include file="../shared/inc_header.jsp"%>


<script type="text/javascript">
$(function() {
	$("#officialDetailsDialog").dialog({
		autoOpen : false,
		modal : true,
		width : 950,
		height : 650,
		closeOnEscape : false,
		draggable : true,
		resizable : true,
		buttons : {
			<c:if test="${not FORM_READ_ONLY}">
				'Submit' : {
					text : 'Submit',
					id : 'officialDetailsDialogSubmitButton',
					click : submitOfficialDetails
				},
			</c:if>
			'Cancel' : function() {
				$(this).dialog('close')
			}
		}
	})
	
	$("#officialDetailsDialog").show()
	/*
	 * Required to solve 508 issue not reading dialog box title. It is also
	 * required to disable dialog animation to enable this functionality
	 */
	$('#officialDetailsDialog').focus()
	
	$(['#officialVavsStartDate', '#officialVavsEndDate', '#officialNacMemberStartDate',
	   '#officialNacMemberEndDate']).each(function(index, o) {
		$(o).enableDatePicker({
			showOn : "button",
			buttonImage : imgHomePath + "/calendar.gif",
			buttonImageOnly : true
		}) 
		$(o).mask("${TWO_DIGIT_DATE_MASK}", {autoclear: false})
	})
})

function showOfficialDetailsPopup(officialId, organizationId) {
	$("#organizationId").val(organizationId);
	var isDisabled = <c:out value="${FORM_READ_ONLY}" default="false" />
	var hasOfficialId = (officialId != '' && typeof officialId !== 'undefined')
	var fullObj = hasOfficialId ? officialsList[officialId] : null
		
	$("#officialId").val(hasOfficialId ? officialId : '')
	$("#officialLastName").val(hasOfficialId ? fullObj.lastName : '')
	$("#officialSuffix").val(hasOfficialId ? fullObj.suffix : '')
	$("#officialSaluation").val(hasOfficialId ? fullObj.prefix : '')
	$("#officialFirstName").val(hasOfficialId ? fullObj.firstName : '')
	$("#officialMiddleName").val(hasOfficialId ? fullObj.middleName : '')
	$("#officialOrganizationTitle").val(hasOfficialId ? fullObj.title : '')
	$("#officialEmail").val(hasOfficialId ? fullObj.email : '')
	$("#officialStreetAddress").val(hasOfficialId ? fullObj.streetAddress : '')
	$("#officialCity").val(hasOfficialId ? fullObj.city : '')
	$("#officialStateSelect").val(hasOfficialId && fullObj.state ? fullObj.state.id : '')
	$("#officialZip").val(hasOfficialId ? fullObj.zip : '')
	$("#officialPhone").val(hasOfficialId ? fullObj.phone : '')
	$("#officialVavsTitle").val(hasOfficialId && fullObj.stdVAVSTitle ? fullObj.stdVAVSTitle.id : '')
	$("#officialVavsStartDate").val(hasOfficialId ? fullObj.vavsStartDate : '')
	$("#officialVavsEndDate").val(hasOfficialId ? fullObj.vavsEndDate : '')
	$("#officialNacMemberStartDate").val(hasOfficialId ? fullObj.nacStartDate : '')
	$("#officialNacMemberEndDate").val(hasOfficialId ? fullObj.nacEndDate : '')
	$("input[name=natCertifyingOfficial][value=" + (hasOfficialId && fullObj.certifyingOfficial ? "Yes" : "No")
	                              + "]").prop("checked", true)
	$("input[name=officialNacMember][value=" + (hasOfficialId && fullObj.nationalCommitteeMember ? "Yes" : "No")
	                              + "]").prop("checked", true)
	
	$("#officialDetailsDialog").dialog('open')
	
	if (isDisabled) {
		// When editing an inactive org, the record is readonly except for the Status field
		$('.nationalOfficialInputFields input', '#officialDetailsDialog').not(".keepEnabledForInactive").attr(
			'disabled', 'disabled')
		$('[id*=Date]', '#officialDetailsDialog').not(".keepEnabledForInactive").datepicker('disable')
			
		$('.nationalOfficialInputFields select', '#officialDetailsDialog').each(
				function() {
					var selectedVal = $(this).find("option:selected").val();
					if (selectedVal == "-1" || selectedVal == "") {
						$(this).replaceWith("None")
					} else {
						$(this).replaceWith(
								escapeHTML($(this).find("option:selected")
										.text()))
					}
				})
		$('.requdIndicator', '#officialDetailsDialog').remove()
	}
}

function submitOfficialDetails() {
    if (!validateNationalOffical()) return;

	/* If validations pass, submit to server - CPB */
	$.ajax({
		url : ajaxHomePath + '/nationalOfficalCreateOrUpdate',
		method: 'POST',
		dataType : 'json',
		data : {
			nationalOfficialId: $("#officialId").val(),
			organizationId: $("#organizationId").val(),
			lastName: $("#officialLastName").val(),
			firstName: $("#officialFirstName").val(),
			middleName: $("#officialMiddleName").val(),
			suffix:	$("#officialSuffix").val(),
			prefix:	$("#officialSaluation").val(),
			title: $("#officialOrganizationTitle").val(),
			certifyingOfficial:	$("input[type='radio'][name='natCertifyingOfficial']:checked").val(),
			email: $("#officialEmail").val(),
			streetAddress: $("#officialStreetAddress").val(),
			city: $("#officialCity").val(),
			state: $("#officialStateSelect").val(),
			zip: $("#officialZip").val(),
			phone: $("#officialPhone").val(),
			stdVAVSTitle: $("#officialVavsTitle").val(),
			vavsStartDate: $("#officialVavsStartDate").val(),
			vavsEndDate: $("#officialVavsEndDate").val(),
			nationalCommitteeMember: $("input[type='radio'][name='officialNacMember']:checked").val(),
			nacStartDate: $("#officialNacMemberStartDate").val(),
			nacEndDate: $("#officialNacMemberEndDate").val()
		},
		error : commonAjaxErrorHandler,
		success : function(response) {
			$("#officialDetailsDialog").dialog('close')
			/* generated by inc_jqueryTable.jsp, see below */
			refreshOfficialsTable()
	    }
	})
}

function validateNationalOffical() {
    var errors = new Array();

    /* Run some validations*/
	if ($("#organizationId").val() == '') {
		errors.push('Please select an organization first.')
	}
	
	if ($.trim($("#officialLastName").val()) == '') {
		errors.push("Last Name is required.")
	}
	
	if ($.trim($("#officialStreetAddress").val()) == '') {
		errors.push("Street Address is required.")
	}
	
	if ($.trim($("#officialCity").val()) == '') {
		errors.push("City is required.")
	}
	
	var myState = $("#officialStateSelect").val()
	if (myState == '') {
		errors.push('State is required.')
	}
	
	if ($.trim($("#officialZip").val()) == '') {
		errors.push("Zip Code is required.")
	}
	
	var myOfficialVavsTitle = $("#officialVavsTitle").val()
	if (myOfficialVavsTitle == '') {
		errors.push('VAVS Title is required.')
	}
			
	if ($('#officialVavsStartDate').val() != '' && !validateDate($('#officialVavsStartDate').val())) {
		errors.push("VAVS Appointment Date is invalid.");
	}

	if ($('#officialVavsEndDate').val() != '' && !validateDate($('#officialVavsEndDate').val())) {
		errors.push("VAVS Expiration Date is invalid.");
	}

	if ($('#officialNacMemberStartDate').val() != '' && !validateDate($('#officialNacMemberStartDate').val())) {
		errors.push("National Executive Committee Appointment Date is invalid.");
	}

	if ($('#officialNacMemberEndDate').val() != '' && !validateDate($('#officialNacMemberEndDate').val())) {
		errors.push("National Executive Committee Expiration Date is invalid.");
	}
	
	var theEmail = $("#officialEmail").val()
	if ($.trim(theEmail) != '' && !validateEmail(theEmail)) {
		errors.push('Please enter a valid email address.')
	}
	
	var thePhone = $("#officialPhone").val()
	if ($.trim(thePhone) != '' && !validatePhone(thePhone)) {
		errors.push('Please enter a valid phone number.')
	}

    if (errors.length > 0) {
        displayAttentionDialog("Please correct the following errors: <ul><li>"
                     + errors.join("</li><li>") + "</li></ul>");                   
    }

    return errors.length == 0;
}
</script>

<div id="officialDetailsDialog" style="display: none"
	title="National Official Detail">
	<input type="hidden" id="officialId" value="" /> <input type="hidden"
		id="organizationId" value="" />

	<div class="nationalOfficialInputFields officialDetailsDisplay">
		<fieldset>
			<legend>Name</legend>
			<div class="leftHalf">
				<table>
					<tr>
						<td class='appFieldLabel' nowrap>Prefix:</td>
						<td></td>
						<td><input type="text" id="officialSaluation" size="10"
							maxLength="10" /></td>
					</tr>
					<tr>
						<td class='appFieldLabel' nowrap>First Name:</td>
						<td></td>
						<td><input type="text" id="officialFirstName" size="30"
							maxLength="30" /></td>
					</tr>
					<tr>
						<td class='appFieldLabel' nowrap>Middle Name/Initial:</td>
						<td></td>
						<td><input type="text" id="officialMiddleName" size="30"
							maxLength="20" /></td>
					</tr>
					<tr>
						<td class='appFieldLabel' nowrap>Last Name:<span
							class="invisibleRequiredFor508">*</span></td>
						<td><span class='requdIndicator'>*</span></td>
						<td><input type="text" id="officialLastName" size="30"
							maxLength="30" /></td>
					</tr>
					<tr>
						<td class='appFieldLabel' nowrap>Suffix:</td>
						<td></td>
						<td><input type="text" id="officialSuffix" size="10"
							maxLength="10" /></td>
					</tr>
				</table>
			</div>
			<div class="rightHalf">
				<table>
					<tr>
						<td class='appFieldLabel' nowrap>Organization Title:</td>
						<td></td>
						<td><input type="text" id="officialOrganizationTitle"
							size="30" maxLength="30" /></td>
					</tr>
					<tr>
						<td class='appFieldLabel' nowrap>National Certifying
							Official?<span class="invisibleRequiredFor508">*</span>
						</td>
						<td><span class='requdIndicator'>*</span></td>
						<td><nobr>
								<input type="radio" name="natCertifyingOfficial"
									id="natCertifyingOfficialYes" value="Yes" />Yes <input
									type="radio" name="natCertifyingOfficial"
									id="natCertifyingOfficialNo" value="No" />No
							</nobr></td>
					</tr>
				</table>
			</div>
		</fieldset>

		<fieldset>
			<legend>Contact Information</legend>
			<div class="leftHalf">
				<table>
					<tr>
						<td class='appFieldLabel' nowrap>Street Address:<span
							class="invisibleRequiredFor508">*</span></td>
						<td><span class='requdIndicator'>*</span></td>
						<td><input type="text" id="officialStreetAddress" size="35"
							maxLength="35" /></td>
					</tr>
					<tr>
						<td class='appFieldLabel' nowrap>City:<span
							class="invisibleRequiredFor508">*</span></td>
						<td><span class='requdIndicator'>*</span></td>
						<td><input type="text" id="officialCity" size="30"
							maxLength="30" /></td>
					</tr>
					<tr>
						<td class='appFieldLabel' nowrap>State:<span
							class="invisibleRequiredFor508">*</span></td>
						<td><span class='requdIndicator'>*</span></td>
						<td><select id="officialStateSelect">
								<option value="">-- Select --</option>
								<c:forEach items="${allStates}" var="state">
									<option value="${state.id}"><c:out
											value="${state.name}" /></option>
								</c:forEach>
						</select></td>
					</tr>
					<tr>
						<td class='appFieldLabel' nowrap>Zip:<span
							class="invisibleRequiredFor508">*</span></td>
						<td><span class='requdIndicator'>*</span></td>
						<td><input type="text" id="officialZip" size="13"
							maxLength="10" /></td>
					</tr>
				</table>
			</div>
			<div class="rightHalf">
				<table>
					<tr>
						<td class='appFieldLabel' nowrap>Email Address:</td>
						<td></td>
						<td><input type="text" id="officialEmail" size="35"
							maxLength="255" /> <a
							href="javascript:emailInputContent('officialEmail')"><img
								alt='Click to email voter' src="${imgHome}/envelope.jpg"
								height="14" width="18" border="0" align="absmiddle"
								style="padding-left: 4px; padding-right: 4px" /></a></td>
					</tr>
					<tr>
						<td class='appFieldLabel' nowrap>Telephone Number:</td>
						<td></td>
						<td><input type="text" id="officialPhone" class="phoneextmask" /></td>
					</tr>
				</table>
			</div>
		</fieldset>

		<fieldset>
			<legend>VAVS Committee Information</legend>
			<div class="leftHalf"  style="padding-left:100px">
				<table>
					<tr>
						<td class='appFieldLabel' nowrap>VAVS Title:<span
							class="invisibleRequiredFor508">*</span></td>
						<td><span class='requdIndicator'>*</span></td>
						<td colspan="4"><select id="officialVavsTitle">
								<option value="">-- Select --</option>
								<c:forEach items="${allOfficialVAVSTitles}" var="title">
									<option value="${title.id}"><c:out
											value="${title.name}" /></option>
								</c:forEach>
						</select></td>
					</tr>
				</table>
			</div>
			<div class="rightHalf" style="padding-right:100px">
				<table>
					<tr>
						<td class='appFieldLabel' nowrap>Appointment Date:</td>
						<td><input size="13" id="officialVavsStartDate" /></td>
					</tr>
					<tr>
						<td class='appFieldLabel' nowrap>Expiration Date:</td>
						<td><input size="13" id="officialVavsEndDate" /></td>
					</tr>
				</table>
			</div>
		</fieldset>

		<fieldset>
			<legend>NAC Committee Information</legend>
			<div class="leftHalf"  style="padding-left:100px">
				<table>
					<tr>
						<td class='appFieldLabel' nowrap>National Executive Committee
							Member?<span class="invisibleRequiredFor508">*</span>
						</td>
						<td><span class='requdIndicator'>*</span></td>
						<td><nobr>
								<input type="radio" name="officialNacMember"
									id="officialNacMemberYes" value="Yes" />Yes <input type="radio"
									name="officialNacMember" id="officialNacMemberNo" value="No" />No
							</nobr></td>
					</tr>
				</table>
			</div>
			<div class="rightHalf" style="padding-right:100px">
				<table>
					<tr>
						<td class='appFieldLabel' nowrap>Appointment Date:</td>
						<td><input size="13" id="officialNacMemberStartDate" /></td>
					</tr>
					<tr>
						<td class='appFieldLabel' nowrap>Expiration Date:</td>
						<td><input size="13" id="officialNacMemberEndDate" /></td>
					</tr>
				</table>
			</div>
		</fieldset>
	</div>
</div>