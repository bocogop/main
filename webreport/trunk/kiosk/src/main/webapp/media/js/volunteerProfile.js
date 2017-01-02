function onPageLoad(isNew, isReadOnly, disableTerminationFields, anyTerminationFieldsSet) {
	$('.dateInput').each(function() {
		$(this).enableDatePicker({
			showOn : "button",
			buttonImage : imgHomePath + "/calendar.gif",
			buttonImageOnly : true
		})
		$(this).mask(twoDigitDateMask)
	})
}

function validate() {
	var allErrors = new Array()

	var firstName = $("#firstNameInput").val()
	var lastName = $("#lastNameInput").val()
	if ($.trim(firstName) == '' || $.trim(lastName) == '') {
		allErrors.push('Please enter both first name and last name.')
	}
	
	var dobVal = $.trim($("#dateOfBirthInput").val())
	if (dobVal == '' || !validateDate(dobVal)) {
		allErrors.push('Please enter a valid date of birth.')
	}
	
	var gender = $("#genderSelect").val()
	if (gender == '') {
		allErrors.push('Please enter the gender.')
	}
	
	var addressLine1 = $.trim($("#addressLine1").val())
	if ($.trim(addressLine1) == '') {
		allErrors.push('Please enter the first line of the address.')
	}
	
	var addressCity = $.trim($("#addressCity").val())
	if ($.trim(addressCity) == '') {
		allErrors.push('Please enter the address city.')
	}
	
	var addressState = $("#stateSelect").val()
	if (addressState == '') {
		allErrors.push('Please enter the address state.')
	}
	
	var addressZip = $.trim($("#addressZip").val())
	if ($.trim(addressZip) == '') {
		allErrors.push('Please enter the address zip code.')
	}
			
	if (allErrors.length > 0)
		displayAttentionDialog("Please correct the following errors: <ul><li>"
			+ allErrors.join("</li><li>") + "</li></ul>", undefined, undefined, {
				height: 330
			});

	return allErrors.length == 0
}


function submitForm() {
	doubleClickSafeguard($("#submitButton"))
	
	if (!validate())
		return false
		
	$("#voterForm")[0].submit()
}
