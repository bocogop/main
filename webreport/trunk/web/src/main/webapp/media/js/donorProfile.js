function initDonorEdit(printReceipt, printMemo, printThankYou, printFormat) {
	printIfNeeded(printReceipt, printMemo, printThankYou, printFormat)

	$("input[name='donor.donorType']:radio").on('change', function() {
		toggleDisplays($(this).val());
	})

	var theSelect = $('select', "#precinctFilter")
	if ($("option[value = '" + workingPrecinct + "']", theSelect).length > 0) {
		theSelect.val(workingPrecinct)
		theSelect.change()
	}
	toggleDisplays(commandDonorType);
}

function linkVoterSelectedCallback(voterObj) {
	$.ajax({
		url : ajaxHomePath + '/donor/donorLinkVoter',
		dataType : 'json',
		data : {
			donorId : donorId,
			voterId : voterObj.id
		},
		error : commonAjaxErrorHandler,
		success : function(response) {
			document.location.href = homePath + '/donorEdit.htm?id=' + escapeHTML(response.id)
		}
	})
}


function unlinkVoter() {
	confirmDialog('Are you sure you want to remove the voter link from this donor?', function() {
		$("#desiredIndividualType").val('individual');
		toggleDisplays('1')
		$("#cancelFormButton").attr('href', 'donorEdit.htm?id=' + donorId)
	})

}

function linkOrganizationSelectedCallback(orgObj) {
	$.ajax({
		url : ajaxHomePath + '/donor/donorLinkOrganization',
		dataType : 'json',
		data : {
			donorId : donorId,
			orgId : orgObj.id
		},
		error : commonAjaxErrorHandler,
		success : function(response) {
			document.location.href = homePath + '/donorEdit.htm?id=' + escapeHTML(response.id)
		}
	})
}

function linkOrganizationAddSelectedCallback() {
	document.location.href = homePath + "/organizationCreate.htm?fromPage=donor"
}

function toggleDisplays(selectdDonorTypeVal) {
	$('.individualInputFields').toggle(shouldShowIndividualSection(selectdDonorTypeVal))
	$('.voterDisplayFields').toggle(shouldShowVoterSection(selectdDonorTypeVal))
	$('.organizationDisplayFields').toggle(shouldShowOrgSection(selectdDonorTypeVal))
	$('.otherTypesDisplayFields').toggle(shouldShowOtherTypesSection(selectdDonorTypeVal))
	$('#submitButton').toggle(shouldShowIndividualSection(selectdDonorTypeVal))
}

function shouldShowIndividualSection(selectdDonorTypeVal) {
	if (!donorPersistent) {
		// If creating new record, display individual data if donor type
		// selected is individual
		return selectdDonorTypeVal == '1';
	} else {
		// If editing existing record, and the voter link had just been
		// unlinked, display individual
		if (donorTypeIsIndividual && $("#desiredIndividualType").val() == 'individual') {
			return true;
		} else {
			// otherwise if neither voter nor organization is linked,
			// display individual
			return displayIndividual;
		}
	}
}

function shouldShowVoterSection(selectdDonorTypeVal) {
	if (!donorPersistent) {
		// If creating new record, display just individual data
		return false;
	} else {
		// If editing existing record, and the voter link had just been
		// unlinked, display individual
		if (donorTypeIsIndividual && $("#desiredIndividualType").val() == 'individual') {
			return false;
		} else {
			// otherwise if donor type is dinvidual, and voter is linked, display voter
			return displayVoter
		}
	}
}

function shouldShowOrgSection(selectdDonorTypeVal) {
	return displayOrganization
}

function shouldShowOtherTypesSection(donorTypeId) {
	return (donorTypeId == '2' || donorTypeId == '3' || donorTypeId == '5')
}

function shouldShowSubmitButton(selectdDonorTypeVal) {
	if (!donorPersistent) {
		// If creating new record, display submbit button just for
		// individual type
		return selectdDonorTypeVal == '1'
	} else {
		// If editing existing record, and the voter link had just been
		// unlinked, display submit button
		if ($("#desiredIndividualType").val() == 'individual') {
			return true;
		} else {
			// otherwise if neither linked to voter nor organizationk,
			// allow to submit
			return displayIndividual
		}
	}
}

function deleteDonation(donationId, donorId) {
	confirmDialog('Are you sure you want to delete this donation?', function() {
		document.location.href = homePath + '/donationDelete.htm?donationSummaryId=' + donationId + '&donorId='
				+ donorId
	})
}

function submitForm(isEdit, donorId) {
	doubleClickSafeguard($("#submitButton"))

	var firstName = $("#firstNameInput").val()
	var lastName = $("#lastNameInput").val()
	var state = $("#stateSelect").val()
	
	if (isEdit && firstName == commandFirstName && lastName == commandLastName)
		return true

	if (!validate())
		return false;

	$("#submitButton").val('Checking duplicates...')
	$("#submitButton").prop('disabled', true)
	
	$.ajax({
		url : ajaxHomePath + '/donorDuplicateCheck',
		dataType : 'json',
		data : {
			firstName: firstName,
			lastName: lastName,
			state: state,
			excludeDonorId : isEdit ? donorId : null
		},
		error : commonAjaxErrorHandler,
		success : function(r) {
			if (r.length == 0) {
				$("#submitButton").val('Submitting form...')
				finalSubmit()
				return
			}
			
			$("#submitButton").val(isEdit ? 'Update' : 'Create')
			$("#submitButton").prop('disabled', false)				
			popupDonorSearch('donorProfCheckForDups', r, {
				submitButtonStr : isEdit ? 'Update Anyway' : 'Create Anyway'
			})
		}
	})
	
	return false
}

function finalSubmit() {
	$("#donorForm")[0].submit()
}

function validate() {
	var errors = new Array();

	// if donor type is individual and donor is not of type voter then last
	// name is required
	if ((!donorPersistent && $("input[name='donor.donorType']:checked").val() == '1')
			|| (donorPersistent && donorTypeIsIndividual)) {

		if ($.trim($("#lastNameInput").val()) == '') {
			errors.push("Last Name is required.")
		}
		if ($.trim($("#firstNameInput").val()) == '') {
			errors.push("First Name is required.")
		}
	}

	if (!donorPersistent) {
		if ($("input[name='donor.donorType']:checked").length == 0) {
			errors.push("Donor Type is required.")
		}
	}

	if (errors.length > 0) {
		displayAttentionDialog("Please correct the following errors: <ul><li>" + errors.join("</li><li>")
				+ "</li></ul>");
	}

	return errors.length == 0;
}

function mergeDonorSelectedCallback(donorObj, fullObj) {
	var hasDonor = (typeof donorObj !== 'undefined')
	var changedDonorId = hasDonor? donorObj.id : ''
	var targetDonorPrecinct = (fullObj.precinct !== null)? fullObj.precinct.displayName: ''
	var targetDonorDonationDate= fullObj.donationDate !== null? fullObj.donationDate : ''
	if(fromMergeDonor.name == donorObj.displayName) {
		displayAttentionDialog("The target donor cannot be the same as the source donor")
		return
	}
	
	if( donorObj.organization != null && donorObj.organization.inactive) {
		displayAttentionDialog("Target Donor Organization is inactive, please choose a different target donor")
		return
	}
		var msg = 'Please confirm you wish to perform the following merge operation:<p>'
			+ '<table>'
			+ '	<tr style="font-weight:bold" align="center"><td>Source Donor:</td><td width="30" rowspan="2">&nbsp;</td><td>Target Donor:</td></tr>'
			+ '	<tr><td>'
			+ '		<table cellpadding="3" border="1">'
			+ '			<tr><td align="right">Type:</td><td nowrap>' + fromMergeDonor.type + '</td></tr>'
			+ '			<tr><td align="right">Name:</td><td class="textWrap" style="max-width: 240px">' + fromMergeDonor.name + '</td></tr>'
			+ '			<tr><td align="right">Contact Info:</td><td>'+getContactInfoForDonorMerge(fromMergeDonor.phone,fromMergeDonor.email, fromMergeDonor.mutillineAddress) + '</td></tr>'
			+ '			<tr><td align="right" nowrap>Last Donation Precinct:</td><td>' + fromMergeDonor.lastDonationFacilty + '</td></tr>'
			+ '			<tr><td align="right" nowrap>Last Donation Date:</td><td>' + fromMergeDonor.lastDonationDate + '</td></tr>'
			+ '		</table>'
			+ '	</td><td>'
			+ '		<table cellpadding="3" border="1">'
			+ '			<tr><td align="right">Type:</td><td nowrap>' + donorObj.donorType.donorType + '</td></tr>'
			+ '			<tr><td align="right">Name:</td><td class="textWrap" style="max-width: 240px">' + donorObj.displayName + '</td></tr>'
			+ '			<tr><td align="right">Contact Info:</td><td>'+getContactInfoForDonorMerge(donorObj.phone,donorObj.email, donorObj.addressMultilineDisplay) + '</td></tr>'
			+ '			<tr><td align="right" nowrap>Last Donation Precinct:</td><td>' + targetDonorPrecinct + '</td></tr>'
			+ '			<tr><td align="right" nowrap>Last Donation Date:</td><td>' + targetDonorDonationDate + '</td></tr>'
			+ '		</table>'
			+ '	</td></tr>'
			+ '</table><p align="center"><span class="redText">Warning!!  Are you sure you want to merge this donor? <br>The Source Donor will be removed from the Donor file once any donations are transferred to the Target Donor.</span></p>'
			
		confirmDialog(msg, function() {
		$.ajax({
			url : ajaxHomePath + '/mergeDonor',
			dataType : 'json',
			data : {
				sourceDonorId : donorId,
				targetDonorId: changedDonorId,
			},
			error : commonAjaxErrorHandler,
			success : function() {
				document.location.href = homePath + "/donorEdit.htm?id=" + changedDonorId
			  }
		})
	}, {
		width: 1000,
		height: 450
	})
}

function getContactInfoForDonorMerge(phone, email, addressMultilineDisplay) {
	var addressHtml = addressMultilineDisplay ? escapeHTML(addressMultilineDisplay) : ""
			
	var phoneHtml = phone ? escapeHTML(phone) + '<br>' : "";
	var emailHtml = ""
	if (email)
		emailHtml = escapeHTML(email) + '<a href="mailto:'
				+ escapeHTML(email)
				+ '"><img alt="Click to email '
				+ escapeHTML(email) + '"' + 'src="' + imgHomePath
				+ '/envelope.jpg" height="14"'
				+ ' width="18" border="0" align="absmiddle"'
				+ ' style="padding-left: 4px; padding-right: 4px" /></a>'
		
	var theHtml =  convertNewlineToBR(addressHtml)
		+ '<br>'+ phoneHtml +  emailHtml 
	return theHtml
}

function convertNewlineToBR(str) {
	var newStr = str.replace(/[\r?\n]/g, '<br />')
	return newStr.replace(/--Newline--/g, '<br />'); 
}

function mergeDonorToAnoymous() {
	var msg = 'Please confirm you wish to perform the following merge operation:<p>'
		+ '<table>'
		+ '	<tr style="font-weight:bold" align="center"><td>Source Donor:</td><td width="30" rowspan="2">&nbsp;</td><td>Target Donor:</td></tr>'
		+ '	<tr><td>'
		+ '		<table cellpadding="3" border="1">'
		+ '			<tr><td align="right">Type:</td><td nowrap>' + fromMergeDonor.type + '</td></tr>'
		+ '			<tr><td align="right">Name:</td><td nowrap>' + fromMergeDonor.name + '</td></tr>'
		+ '			<tr><td align="right">Contact Info:</td><td>'+getContactInfoForDonorMerge(fromMergeDonor.phone,fromMergeDonor.email, fromMergeDonor.mutillineAddress) + '</td></tr>'
		+ '			<tr><td align="right" nowrap>Last Donation Precinct:</td><td>' + fromMergeDonor.lastDonationFacilty + '</td></tr>'
		+ '			<tr><td align="right" nowrap>Last Donation Date:</td><td>' + fromMergeDonor.lastDonationDate + '</td></tr>'
		+ '		</table>'
		+ '	</td><td>'
		+ '		<table cellpadding="3" border="1">'
		+ '			<tr><td align="right">Type:</td><td nowrap> Anonymous</td></tr>'
		+ '			<tr><td align="right">Name:</td><td nowrap></td></tr>'
		+ '			<tr><td align="right">Contact Info:</td><td></td></tr>'
		+ '			<tr><td align="right" nowrap>Last Donation Precinct:</td><td ></td></tr>'
		+ '			<tr><td align="right" nowrap>Last Donation Date:</td><td></td></tr>'
		+ '		</table>'
		+ '	</td></tr>'
		+ '</table><p align="center"><span class="redText">Warning!!  Are you sure you want to merge this donor? <br>The Source Donor will be removed from the Donor file once any donations are transferred to the Target Donor.</span></p>'
		
	confirmDialog(msg, function() {
	$.ajax({
		url : ajaxHomePath + '/mergeDonorToAnonymous',
		dataType : 'json',
		data : {
			sourceDonorId : donorId,
			},
		error : commonAjaxErrorHandler,
		success : function() {
			document.location.href = homePath + "/donorEdit.htm?id=0"
		  }
	})
}, {
	width: 1000,
	height: 450
})
}

