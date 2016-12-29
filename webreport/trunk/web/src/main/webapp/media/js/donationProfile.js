var valChanged = false

function initDonationProfile(printReceipt, printMemo, printThankYou, printFormat) {
	printIfNeeded(printReceipt, printMemo, printThankYou, printFormat)
	
	var unorderableCols = [ 0 ]
	if (!isReadOnly)
		unorderableCols.push(2)
	
	$('#donationDetailsList').DataTable({
		"dom" : '<"top">rt<"bottom"><"clear">',
		"order": [],
		"stateSave" : false,
		"columnDefs" : [ 
	        { className: "dt-body-right dt-foot-right", "targets": [ 1 ], orderable: false },
            { className: "dt-body-left", "targets": unorderableCols , orderable: false}
         ],
		"footerCallback" : function(row, data, start, end,
				display) {
			var api = this.api();
			// Remove the formatting to get integer data for
			// summation
			var intVal = function(i) {
				return typeof i === 'string' ? i.replace(
						/[\$,]/g, '') * 1
						: typeof i === 'number' ? i : 0;
			};
			// Total over all pages
			total = api.column(1).data().reduce(
					function(a, b) {
						return intVal(a) + intVal(b);
					}, 0);
			// Update footer
			// Occasionally the total sum is not right for
			// cents. The following line just a temporary
			// fix
			total = Math.round(total*100)/100; 
			$(api.column(1).footer())
					.html('$'+ formatAndAddThousandSeparators(total));
		}
	})
	// resort the list
	
	$('#donationDetailsForNewDonation').DataTable({
		"dom" : '<"top">rt<"bottom"><"clear">',
		"order": [],
		"stateSave" : false,
		"columnDefs" : [{
			 targets: [0,1],
		     orderable: false
	    }]
	})
			
	$('#donationDetailsForNewDonation1').DataTable({
		"dom" : '<"top">rt<"bottom"><"clear">',
		"order": [],
		"stateSave" : false,
		"columnDefs": [{
			 targets: [0,1],
		     orderable: false
	    }]
	})

	var dateFields = ['#donationDate', '#checkDate']
	if (!ackDatePopulated)
		dateFields.push('#acknowledgementDate')
	
	$(dateFields).each(
		function(index, o) {
			$(o).enableDatePicker({
				showOn : "button",
				buttonImage : imgHomePath + "/calendar.gif",
				buttonImageOnly : true
			})
			$(o).mask(twoDigitDateMask, {
				autoclear : false
			})
		})

	
		
	$("#donationTypeSelect").change(toggleTheFields)	
	$("#submitFormButton").click(submitForm)
	$("#postPrintButton").click(postAndPrint)
	$("#justPrintButton").click(justPrint)
	
	toggleTheFields()
	
	
	$(".donationInput").change(function() {
		valChanged = true
		$("#changeDonorButton").button("option", "disabled", valChanged)
	})
	
	//fix the textarea change listener issue for IE
    $(document).on('input propertychange', "textarea[name='donationSummary.donationDescription']", function () {
    	valChanged = true
		$("#changeDonorButton").button("option", "disabled", valChanged)
     })

    $(document).on('input propertychange', "textarea[name='donationSummary.additionalComments']", function () {
        valChanged = true
		$("#changeDonorButton").button("option", "disabled", valChanged)
    })

	showAckAddressFieldsIfPresent()
	
	$("#acknowledgementDate").prop('disabled', ackDatePopulated)
	
}

var donationTypeVal 

function toggleTheFields() {
    donationTypeVal = $("#donationTypeSelect").val()
    toggleDisplays(donationTypeVal)
    if (donationSummaryPersistent &&  hasDonationDetailWithRealGPF &&
    		(donationTypeVal == '3' // item
    				|| donationTypeVal == '4' // activity
    		)) {
    	displayAttentionDialog('Please remove all GPF entries whose fund is not "None" before saving.')
    }
}

function toggleDisplays(donationTypeVal) {
	$('.checkDonationType').toggle(shouldShowCheckFields(donationTypeVal));	
	$('.creditCardDonationType').toggle(shouldShowCreditCardFields(donationTypeVal));	
	$('.ePayDonationType').toggle(shouldShowEDonationFields(donationTypeVal));	
	$('.fieldServiceReceipt').toggle(shouldShowReceiptField(donationTypeVal));
	$('.noServiceReceipt').toggle(!shouldShowReceiptField(donationTypeVal));
	$('.hiddenFieldsForItemAndActivity').toggle(!shouldShowFieldsForItemAndActivity(donationTypeVal));
	$('.hiddenGpfTableForItemAndActivity').toggle(!shouldShowFieldsForItemAndActivity(donationTypeVal));
	$('.gpfTableForItemAndActivity').toggle(shouldShowItemActivityOnlyTable(donationTypeVal));
	$('#persistentJQueryTable').toggle(!shouldShowItemActivityOnlyTable(donationTypeVal));
}


function shouldShowCheckFields(donationTypeVal) {
	return donationTypeVal == '2'
}

function shouldShowCreditCardFields(donationTypeVal) {
	return 	donationTypeVal == '6'
}

function shouldShowEDonationFields(donationTypeVal) {
	return 	donationTypeVal == '5'
}

function shouldShowReceiptField(donationTypeVal) {
	if (donationTypeVal == '2' || donationTypeVal == '1' || donationTypeVal == '6')
		return true;
	else
		return false;	
}

function shouldShowFieldsForItemAndActivity(donationTypeVal) {
	if (donationTypeVal == '3' || donationTypeVal == '4' )
		return true;
	else
		return false;	
}

function shouldShowItemActivityOnlyTable(donationTypeVal) {
	return !((donationTypeVal != '3' &&  donationTypeVal != '4') ||  hasDonationDetailWithRealGPF )
}

function showAckAddressFieldsIfPresent() {
	if (ackAddressFilled)
		editAckAddress()
}
	
function refreshDonationDetailsData(r) {
	var table = $('#donationDetailsList').DataTable()
	table.clear()
	table.rows.add(r)
	table.draw()
}

function submitForm(evt) {
	if (!validate())
	    return false

	if (evt != null)
		doubleClickSafeguard($(evt.currentTarget),10000)
	$('#donationProfileForm').submit()
	return true
}

function deleteDonationDetail(donationDetailId) {
	var fullObj = donationDetailsList[donationDetailId]
	confirmDialog('Are you sure you want to delete this entry for ["'
			+ fullObj.donGenPostFund.generalPostFund + '"]?', function() {
		$.ajax({
			url : ajaxHomePath + '/deleteDonationDetail',
			dataType : 'json',
			data : {
				donationDetailId : donationDetailId
			},
			error : commonAjaxErrorHandler,
			success : function() {
				/* generated by inc_jqueryTable.jsp */
				refreshDonationDetails()
			}
		})
	})
}

function changeDonorSelectedCallback(donorObj) {
	var hasDonor = (typeof donorObj !== 'undefined')
	var changedDonorId = hasDonor? donorObj.id : ''
		
	confirmDialog('Are you sure you want to move this donation to the new Donor?',
			function() {
		$.ajax({
			url : ajaxHomePath + '/changeDonor',
			dataType : 'json',
			data : {
				donationSummaryId : donationSummaryId,
				donorId: changedDonorId
			},
			error : commonAjaxErrorHandler,
			success : function() {
				document.location.href = homePath + "/donationEdit.htm?id=" + donationSummaryId
			}
		})
	})
}

	
function affiliateOrganizationSelectedCallback(orgObj) {
	$("#orgIdInput").val(orgObj.id)
	$("#affiliationName").text(orgObj.displayName)
	valChanged = true
	$("#changeDonorButton").button("option", "disabled", valChanged)
}

function removeAffiliation() {
	$("#orgIdInput").val('')
	$("#affiliationName").text('Not Applicable')
	valChanged = true
	$("#changeDonorButton").button("option", "disabled", valChanged)
}



function editAckAddress() {
	$(".fixedAckAddressFields").hide()
	$(".ackAddressInputs").show()
}	

function hideAckAddress() {
	$(".fixedAckAddressFields").show()
	$(".ackAddressInputs").hide()
}	


function validate() {
	var errors = new Array()

	if ($('#donationDate').val() != ''
			&& !validateDate($('#donationDate').val())) {
		errors.push("Donation Date is invalid.")
	}

	if ($('#checkDate').val() != '' && !validateDate($('#checkDate').val())) {
		errors.push("Check Date is invalid.")
	}

	if ($('#donationDate').val() == '')
		errors.push("Donation Date is required.")
				
	if ($('#donationTypeSelect').val() == '-1')
		errors.push("Donation Type is required.")
		
	if ($("#donationTypeSelect").val() == '2') {
		if ($('#checkNumber').val() == '')
			errors.push("Check Number is required.")

		if ($('#checkNumber').val() != '' &&  !validateNumericWithoutCommas($('#checkNumber').val()))
			errors.push("Check Number needs to be numeric.")
	}
	
	if ($("#donationTypeSelect").val() == '6') {
		if ($('#cardTypeSelect').val() == '-1')
			errors.push("Card Type is required.")
	}
	
	if ($("#donationTypeSelect").val() == '5') {
		if ($('#epayTrackingId').val() == '' )
			errors.push("E-Pay Tracking Number is required.")
	}
	
			
	if (!donationSummaryPersistent && $('#donationTypeSelect').val() != '3' && $('#donationTypeSelect').val() != '4') {
			
		if ($('#gpf1').val() == '-1')
			errors.push("General Post Fund is required.")
			
		if ($('#donationAmount1').val() == '')
			errors.push("Donation Amount is required.")
			
		if ($('#donationAmount1').val() != '' && $('#donationAmount1').val() > 9999999999.99 ) 
			errors.push("The maximum donation amount is 9999999999.99.")
				
		if ($('#donationAmount1').val() != ''
			&& !validateNumericWithoutCommas($('#donationAmount1').val())) 
			errors.push("Donation Amount is invalid [format should be ########## or ##########.##].") 
	
			
		if ($('#gpf2').val() != '-1' && $('#donationAmount2').val() == '')
			errors.push("Donation Amount is required if General Post Fund is selected.")
			
		if ($('#gpf2').val() == '-1' && $('#donationAmount2').val() != '')
			errors.push("General Post Fund required if Donation Amount is given.")
			
		if ($('#gpf3').val() != '-1' && $('#donationAmount3').val() == '')
			errors.push("Donation Amount is required if General Post Fund is selected.")
			
		if ($('#gpf3').val() == '-1' && $('#donationAmount3').val() != '')
			errors.push("General Post Fund required if Donation Amount is given.")
			
		if ($('#donationAmount2').val() != '' && $('#donationAmount2').val() > 9999999999.99 ) {
			errors.push("The maximum donation amount is 9999999999.99.")
		}
		
		if ($('#donationAmount2').val() != ''
			&& !validateNumericWithoutCommas($('#donationAmount2').val())) {
			errors.push("Donation Amount is invalid [format should be ########## or ##########.##].") 
		}
		if ($('#donationAmount3').val() != '' && $('#donationAmount3').val() > 9999999999.99 ) {
			errors.push("The maximum donation amount is 9999999999.99.")
		}
		
		if ($('#donationAmount3').val() != ''
			&& !validateNumericWithoutCommas($('#donationAmount3').val())) {
			errors.push("Donation Amount is invalid [format should be ########## or ##########.##].") 
		}
	}
	
	if ($('#donationTypeSelect').val() == '3' || $('#donationTypeSelect').val() == '4') {
		
		if ($('#donationAmount4').val() == '')
			errors.push("Donation Amount is required.")
			
		if ($('#donationAmount4').val() != ''
			&& !validateNumericWithoutCommas($('#donationAmount4').val())) {
			errors.push("Donation Amount is invalid [format should be ########## or ##########.##].") 
		}
		
		if ($('#donationAmount4').val() != '' && $('#donationAmount4').val() > 9999999999.99 ) {
			errors.push("The maximum donation amount is 9999999999.99.")
		}
	}
	
	if(donorTypeIsIndividualOrOrg && $('#salutation').val() == '') {
		errors.push("Letter Salutation is required")
    }
	
	if ($('#inMemeoryOf').val() == '' &&  $('#familyContact').val() != '') {
		errors.push("In Memory Of is required if Family Contact is entered")
	}
	
	if (errors.length > 0)
		displayAttentionDialog("Please correct the following errors: <ul><li>"
				+ errors.join("</li><li>") + "</li></ul>");

	return errors.length == 0
}

function justPrint() {
	showPrintDonationSummaryDialog(donationSummaryId)
}

function postAndPrint() {
	if (!validate())
		return
	
	showPrintDonationSummaryDialogWithCallback(function(isReceipt, isMemo, isThankYou, format) {
		$("#printReceipt").val(isReceipt ? 'true' : 'false')
		$("#printMemo").val(isMemo ? 'true' : 'false')
		$("#printThankYou").val(isThankYou ? 'true' : 'false')
		$("#printFormat").val(format)

		$('#donationProfileForm').submit()
	})
}