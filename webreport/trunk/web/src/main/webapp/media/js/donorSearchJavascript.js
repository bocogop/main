function popupDonorSearch(uid, donorListToShow, options) {

	var dialogEl = $("#donorSearchDialog" + uid)
	var mode = dialogEl.data('mode')
	
	options = $.extend({
		searchNameStr : '',
		searchFirstNameStr : '',
		searchLastNameStr : '',
		searchState : '',
		donationLogId : '',
		
		submitButtonStr : null
	}, options)

	if ($.trim(options.searchNameStr) != '') {
		var tokens = options.searchNameStr.split(" ") 
		options.searchLastNameStr = tokens[tokens.length-1]
		options.searchFirstNameStr = tokens.length > 1 ? tokens[0] : ''
	}

	$("#donorSearchFirstName" + uid).val(options.searchFirstNameStr)
	$("#donorSearchLastName" + uid).val(options.searchLastNameStr)
	if ($.trim(options.searchState) != '') {
		$("#donorSearchState" + uid + " option[stateCode=" + options.searchState + "]").prop('selected',true)
	}
	$("#donorSearchOrgName" + uid).val(options.searchNameStr)  
	
	$("#donationLogId" + uid).val(options.donationLogId)
	
	$("#donorSearchNoResults" + uid).hide()
	$("#donorSearchResultsTable" + uid).hide()
	$('#donorSearchLastName' + uid).focus()
	$("#donorSearchDialog" + uid).dialog('open')
	
	
	// ------ customize submit button based on mode
	
	if (options.submitButtonStr != null) {
		if (mode == 'duplicateCheck') {
			$("#donorCreateOrUpdateAnywayButton" + uid).button( "option", "label", options.submitButtonStr );
		} // else ...
	}
	
	if (donorListToShow) {
		processDonorSearchResults(uid, donorListToShow)
	} /*else if ($.trim(options.searchFirstNameStr) != '' || $.trim(options.searchLastNameStr) != '') {
		submitDonorSearchForm(uid)
	}*/

}

function donorSearchPopupItemSelected(uid, donorId) {
	var donorObj = donorSearchResults[uid]['' + donorId].donor
	var fullObj = donorSearchResults[uid]['' + donorId]
	var donationLogId = $("#donationLogId" + uid).val()
	var theDialog = $("#donorSearchDialog" + uid)
	theDialog.dialog('close')
	theDialog.data('callbackMethod')(donorObj, fullObj, donationLogId)
}

function initDonorSearchPopup(options) {
	var mode = options.mode
	var uid = options.uid
	var maxResults = options.maxResults
	var callbackMethod = options.callbackMethod
	var addButtonCallbackMethod = options.addButtonCallbackMethod
	var showDisclaimer = options.showDisclaimer
	var donationLogId = options.donationLogId
	
	if (mode != 'add' && mode != 'search' && mode != 'duplicateCheck' && mode != 'addEDonation') {
		alert('Invalid mode for donor search popup with id "' + uid + '": ' + mode)
		return
	}
	
	var parms = {
		"columnDefs" : [
				{
					"targets" : 0,
					"data" : function(row, type, val, meta) {
						var donorNameEscaped = '';
						donorNameEscaped = escapeHTML(row.donor.displayName)
						
						if (type === 'display') {
							return '<a class="appLink" href="javascript:donorSearchPopupItemSelected(\''
									+ uid + '\', ' + row.donor.id 
									+ ')">'
									+ donorNameEscaped + '</a>'
						} else {
							return donorNameEscaped
						}
					}
				},
				{
					"targets" : 1,
					"data" : function(row, type, val, meta) {
						return row.donor.volunteer? 'Yes' : 'No'
					}
				},
				{
					"targets" : 2,
					"data" : function(row, type, val, meta) {
						var theText = escapeHTML((row.orgFacility) ? row.orgFacility.displayName : '')
						if (type === 'filter') {
							return abbreviate(theText, 25)
						}
						return theText
					}
				},
				{
					"targets" : 3,
					"data" : function(row, type, val, meta) {
						return escapeHTML(row.orgContactName)
					}
				},
				{
					"targets" : 4,
					"data" : function(row, type, val, meta) {
						var	contactInfoHtml = ''
						contactInfoHtml = getDonorDashedBoxEl(row.donor)
	
						return contactInfoHtml
					}
				}, 
				{
					"targets" : 5,
					"data" : function(row, type, val, meta) {
						var theText = escapeHTML(row.facility ? row.facility.displayName : '')
						if (type === 'filter') {
							return abbreviate(theText, 25)
						}
						return theText
					}
				},
				{
					"targets" : 6,
					"data" : function(row, type, val, meta) {
						if (type === 'display' || type === 'filter') {
							return escapeHTML(row.donationDate)
						} else {
							// sort
							return getAsYYYYMMDD(row.donationDate)
						}
					}
				},
				{
					"targets" : 7,
					"data" : function(row, type, val, meta) {
						return escapeHTML(row.donationType? row.donationType.donationType : '')
					}
				},
				{
					"targets" : 8,
					"data" : function(row, type, val, meta) {
						return escapeHTML(row.donationValue ? '$'+ formatAndAddThousandSeparators(row.donationValue): '');
						
					}
				}
			],
		"dom" : '<"top"fi>rt<"bottom"pl><"clear">',
		"pagingType" : "full_numbers",
		"pageLength" : 10,
		"lengthMenu" : [ [ 10, -1 ], [ 10, "All" ] ],
		"stateSave" : false
	}

	var theDataTable = $('#donorSearchResultsList' + uid).DataTable(parms)

	var dialogEl = $("#donorSearchDialog" + uid)
	
	var buttonConfig = {}
	if (mode == 'duplicateCheck') {
		buttonConfig['Create Anyway'] = {
				id : 'donorCreateOrUpdateAnywayButton' + uid,
				text : 'Create Anyway',
				click : function() {
					$(this).dialog('close')
					addButtonCallbackMethod()
				}
		}
	}
	buttonConfig['Cancel'] = function() { $(this).dialog('close') }
	
	dialogEl.dialog({
		autoOpen : false,
		modal : false,
		width : 1200,
		height : 750,
		closeOnEscape : true,
		draggable : true,
		resizable : true,
		buttons : buttonConfig
	})
	dialogEl.data('mode', mode)
	dialogEl.data('maxResults', maxResults)
	dialogEl.data('callbackMethod', callbackMethod)
	
	$("input[name=donorSearchType" + uid + "]").click(function(evt) {
		donorSearchToggleFields(uid)
	})

	if (mode == 'add' || mode == 'search' || mode == 'addEDonation') {
		$.each([ "#donorSearchFirstName" + uid, "#donorSearchLastName" + uid, "#donorSearchOrgName" + uid ], function(index,
				value) {
			$(value).keypress(function(event) {
				if (event.which == 13) {
					submitDonorSearchForm(uid)
				}
			})
		})
		
		$(".donorSearchLink" + uid).click(function(evt) {
			submitDonorSearchForm(uid)
		})
	}
	
	if (mode == 'add' || mode == 'addEDonation') {
		$("input[name=donorSearchFacility" + uid + "][value=" + 'A' + "]").prop("checked", true)
			
		$(".donorAddLink" + uid).click(function() {
				var isOrg = $("#donorSearchTypeOrganization" + uid).is(":checked") 
				var donationLogId = $("#donationLogId" + uid).val()
				addButtonCallbackMethod(isOrg ? "organization" : "individual", donationLogId)
		})

		$("#donorSearchEmail" + uid).prop('disabled', true)
		$("#donorSearchPhone" + uid).prop('disabled', true)
		$("#donorSearchCity" + uid).prop('disabled', true)
		$("#donorSearchZip" + uid).prop('disabled', true)
	}
	
	if (mode == 'duplicateCheck')
		$(".donorSearchFields" + uid).hide()
		
	if (showDisclaimer)
		$('#donorSearchDisclaimer' + uid).show()
	
	donorSearchToggleFields(uid)
	dialogEl.show()
}

function donorSearchToggleFields(uid) {
	var isIndividual = $("#donorSearchTypeIndividual" + uid).is(":checked") 
	$('.donorSearchToggleFields' + uid).toggle(isIndividual)
	$('.orgSearchToggleFields' + uid).toggle(!isIndividual)
	
	$("#donorSearchNoResults" + uid).hide()
	
	if (isIndividual) {
		$('#donorSearchLastName' + uid).focus()
	} else {
		$('#donorSearchOrgName' + uid).focus()
	}
}

var donorSearchResults = new Object()

function submitDonorSearchForm(uid) { 
	 
	if (allValsEmpty(["donorSearchFirstName" + uid,
	                  "donorSearchLastName" + uid,
	                  "donorSearchOrgName" + uid,
	                  "donorSearchCity" + uid,
	                  "donorSearchState" + uid,
	                  "donorSearchZip" + uid,
	                  "donorSearchEmail" + uid,
	                  "donorSearchPhone" + uid,
	                  "donorSearchFacility" + uid])) {
		displayAttentionDialog('Please enter at least one piece of search criteria.')
		return
	}
	
	$('#donorSearchDisclaimer' + uid).hide()
	var dialogEl = $("#donorSearchDialog" + uid)
	var theType = $(
			"input[type='radio'][name='donorSearchType" + uid
					+ "']:checked").val()
	var theFacility = $(
			"input[type='radio'][name='donorSearchFacility" + uid
					+ "']:checked").val()
				
	$.ajax({
		url : ajaxHomePath + "/findDonors",
		type : "POST",
		dataType : 'json',
		data : {
			donorType : theType,
			firstName : $("#donorSearchFirstName" + uid).val(),
			lastName : $("#donorSearchLastName" + uid).val(),
			orgName : $("#donorSearchOrgName" + uid).val(),
			city : $("#donorSearchCity" + uid).val(),  
			state : $("#donorSearchState" + uid).val(),  
			zip : $("#donorSearchZip" + uid).val(),
			email : $("#donorSearchEmail" + uid).val(),
			phone : $("#donorSearchPhone" + uid).val(),
			facilityScope : theFacility,
		},
		error : commonAjaxErrorHandler,
		success : function(results) {
			processDonorSearchResults(uid, results)
		}
	})
}

function processDonorSearchResults(uid, results) {
	var dialogEl = $("#donorSearchDialog" + uid)
	
	$("#donorSearchNoResults" + uid).hide()
	$("#donorSearchResultsTable" + uid).hide()

	var resultMap = new Object()

	var table = $('#donorSearchResultsList' + uid).DataTable()
	table.clear()

	for (var i = 0; i < results.length; i++) {
		resultMap['' + results[i].donor.id] = results[i]
		table.row.add(results[i])
	}

	donorSearchResults[uid] = resultMap

	$("#donorSearchMaxResults" + uid).toggle(results.length >= dialogEl.data('maxResults'))
	
	if (results.length > 0) {
		
		var isOrganization = $("#donorSearchTypeOrganization" + uid).is(":checked") 
		toggleResultsColumns(table, isOrganization)
		
		table.search('').columns().search('')
		rebuildTableFilters('donorSearchResultsList' + uid)
		
		$("#donorSearchResultsTable" + uid).show()

	} else {
		$("#donorSearchNoResults" + uid).show()
	}
	table.draw()

	var mode = dialogEl.data('mode')
	if (mode == 'add' || mode == 'addEDonation') {
		$(".donorAddLink" + uid).show()
	}
	
	$('#donorSearchLastName' + uid).focus()
}

function toggleResultsColumns(table, isOrg) {
    table.column(1).visible(!isOrg)
    table.column(2).visible(isOrg)
    table.column(3).visible(isOrg)
}

function getDonorDashedBoxEl(donor) {
	var el = getDashedBoxEl(donor.displayPhone, donor.displayEmail, donor.addressMultilineDisplay)
	return el ? el.outerHTML() : ''
}


function getDashedBoxEl(phone, email, addressMultilineDisplay) {
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
	
	var theHtml = '<table class="addressBox">'
		+ '<tr valign="top"><td nowrap>' + convertLinefeedToBR(addressHtml)
		+ '</td><td width="10">&nbsp;</td><td align="right" nowrap>'
		+ phoneHtml + emailHtml + '</td></tr></table>'
	return getBoxEl(theHtml, false)
}