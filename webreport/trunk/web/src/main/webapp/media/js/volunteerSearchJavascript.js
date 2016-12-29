function popupVolunteerSearch(uid, volunteerListToShow, options) {
	var dialogEl = $("#volunteerSearchDialog" + uid)
	var mode = dialogEl.data('mode')
	
	options = $.extend({
		searchFirstNameStr : '',
		searchLastNameStr : '',
		searchCode : '',
		searchEmail : '',
		searchDOB : '',
		submitButtonStr : null
	}, options)
	
	// ---- Set defaults immediately
	
	if (mode == 'add') {
		$('#volunteerSearchScopeNational' + uid).click()
		$('input[type=radio][name="volunteerSearchScope' + uid + '"]').prop('disabled', true)
		$("#volunteerSearchIncludeInactive" + uid).prop('checked', true)
		$("#volunteerSearchIncludeInactive" + uid).prop('disabled', true)
	} else {
		$('#volunteerSearchScopeLocal' + uid).click()
		$('#volunteerSearchIncludeInactive' + uid).prop('checked', false)
	}
	
	$("#volunteerSearchFirstName" + uid).val(options.searchFirstNameStr)
	$("#volunteerSearchLastName" + uid).val(options.searchLastNameStr)
	$("#volunteerSearchCode" + uid).val(options.searchCode)
	$("#volunteerSearchEmail" + uid).val(options.searchEmail)
	$("#volunteerSearchDOB" + uid).val(options.searchDOB)
	$("#volunteerSearchNoResults" + uid).hide()
	$("#volunteerSearchResultsTable" + uid).hide()
	
	$('#volunteerSearchLastName' + uid).focus()
	
	$("#volunteerSearchDialog" + uid).dialog('open')
	
	// ------ customize submit button based on mode
	
	if (options.submitButtonStr != null) {
		if (mode == 'duplicateCheck') {
			$("#createOrUpdateAnywayButton" + uid).button( "option", "label", options.submitButtonStr );
		} // else ...
	}
	
	// ------ preload search results if specified; otherwise, check for previous search in session on server
	
	if (volunteerListToShow) {
		processVolunteerSearchResults(uid, volunteerListToShow)
	} else if ($.trim(options.searchFirstNameStr) != '' || $.trim(options.searchLastNameStr) != '') {
		submitVolunteerSearchForm(uid)
	} else if (mode == 'search') {
		restorePreviousSearch(uid)
	}
}

function volunteerSearchPopupItemSelected(uid, volunteerId) {
	var volunteerObj = volunteerSearchResults[uid]['' + volunteerId]
	var theDialog = $("#volunteerSearchDialog" + uid)
	theDialog.dialog('close')
	theDialog.data('callbackMethod')(volunteerObj)
}

function initVolunteerSearchPopup(options) {
	var mode = options.mode
	var uid = options.uid
	var maxResults = options.maxResults
	var callbackMethod = options.callbackMethod
	var addButtonCallbackMethod = options.addButtonCallbackMethod
	var showDisclaimer = options.showDisclaimer
	
	if (mode != 'add' && mode != 'search' && mode != 'duplicateCheck') {
		alert('Invalid mode for volunteer search popup with id "' + uid + '": ' + mode)
		return
	}
	
	var parms = {
		"columnDefs" : [
				{
					"targets" : 0,
					"data" : function(row, type, val, meta) {
						var volunteerNameEscaped = escapeHTML(row.displayName)
						if (type === 'display') {
							return '<a class="appLink" href="javascript:volunteerSearchPopupItemSelected(\''
									+ uid + '\', ' + row.id + ')">'
									+ volunteerNameEscaped + '</a><br><i>Code:</i> '
									+ (row.identifyingCode ? escapeHTML(row.identifyingCode) : '(none)')
						} else {
							return volunteerNameEscaped
						}
					}
				},
				{
					"targets" : 1,
					"data" : function(row, type, val, meta) {
						var theText = escapeHTML(row.primaryFacility ? row.primaryFacility.displayName : '(Unknown)')
						if (type === 'filter') {
							return abbreviate(theText, 25)
						}
						return theText
					}
				},
				{
					"targets" : 2,
					"data" : function(row, type, val, meta) {
						if (type === 'display') {
							return escapeHTML(row.dateOfBirth)
						} else {
							return getAsYYYYMMDD(row.dateOfBirth)
						}
					}
				},
				{
					"targets" : 3,
					"data" : function(row, type, val, meta) {
						return escapeHTML(row.gender.name)
					}
				},
				{
					"targets" : 4,
					"data" : function(row, type, val, meta) {
						if (type === 'sort') {
							return (row.status.sortOrder * 100000000) + getAsYYYYMMDD(row.statusDate)
						} else if (type === 'filter') {
							return row.status.name
						} else {
							return row.status.name + (row.status.volunteerActive == false ? " as of " +
									 row.statusDate : '')
						}
					}
				},
				{
					"targets" : 5,
					"data" : function(row, type, val, meta) {
						var contactInfoHtml = getVolunteerDashedBoxEl(row).outerHTML()
						return contactInfoHtml
					}
				}/*, {
					"targets" : 6,
					"data" : function(row, type, val, meta) {
						return escapeHTML(row.identifyingCode)
					}
				}*/ ],
		"dom" : '<"top"fi>rt<"bottom"pl><"clear">',
		"pagingType" : "full_numbers",
		"pageLength" : 10,
		"lengthMenu" : [ [ 10, -1 ], [ 10, "All" ] ],
		"stateSave" : false
	}

	var theDataTable = $('#volunteerSearchResultsList' + uid).DataTable(parms)
	
	var dialogEl = $("#volunteerSearchDialog" + uid)
	if (mode == 'add') {
		dialogEl.attr('title', 'Add New Volunteer')
	} else {
		dialogEl.attr('title', 'Search for Volunteer')
	}
	
	var buttonConfig = {}
	if (mode == 'duplicateCheck')
		buttonConfig['Create Anyway'] = {
			id : 'createOrUpdateAnywayButton' + uid,
			text : 'Create Anyway',
			click : function() {
				$(this).dialog('close')
				addButtonCallbackMethod()
			}
		}
	buttonConfig['Cancel'] = function() { $(this).dialog('close') }
	
	dialogEl.dialog({
		autoOpen : false,
		modal : false,
		width : 1200,
		height : 600,
		closeOnEscape : true,
		draggable : true,
		resizable : true,
		buttons : buttonConfig
	})
	dialogEl.data('stationsPopulated', false)
	dialogEl.data('mode', mode)
	dialogEl.data('maxResults', maxResults)
	dialogEl.data('callbackMethod', callbackMethod)
	
	if (mode == 'add' || mode == 'search') {
		
		$.each([ "#volunteerSearchFirstName" + uid, "#volunteerSearchLastName" + uid ], function(index,
				value) {
			$(value).keypress(function(event) {
				if (event.which == 13) {
					submitVolunteerSearchForm(uid)
				}
			})
		})
		
		$(".volunteerSearchLink" + uid).click(function(evt) {
			submitVolunteerSearchForm(uid)
		})
		
		$('#volunteerSearchScopeLocal' + uid).click(function() {
			$("#volunteerSearchFacilityWrapper" + uid).show()
		})
		$('#volunteerSearchScopeNational' + uid).click(function() {
			$("#volunteerSearchFacilityWrapper" + uid).hide()
		})
		
		$("#volunteerSearchDOB" + uid).enableDatePicker({
			showOn : "button",
			buttonImage : imgHomePath + "/calendar.gif",
			buttonImageOnly : true
		})
		$("#volunteerSearchDOB" + uid).mask(twoDigitDateMask, {
			autoclear : false
		})
		
		var facilityEl = $("#volunteerSearchScopeFacilityId" + uid)
		facilityEl.multiselect({
			selectedText : function(numChecked, numTotal, checkedItems) {
				return abbreviate($(checkedItems[0]).next().text())
			},
			beforeopen: function(){
				if (dialogEl.data('stationsPopulated')) return
				var curVal = facilityEl.val()
				
				$.ajax({
					url : ajaxHomePath + "/volunteerSearch/facilities",
					type : "POST",
					dataType : 'json',
					error : commonAjaxErrorHandler,
					success : function(results) {
						facilityEl.empty()
						var newHtml = []
						$.each(results, function(index, item) {
							var selectedText = (item.id == curVal) ? ' selected="selected"' : ''
							newHtml.push('<option value="' + item.id + '"' + selectedText + '>' + item.displayName + '</option>')
						})
						facilityEl.html(newHtml.join(''))
						
						facilityEl.val(curVal)
						facilityEl.multiselect("refresh")
						dialogEl.data('stationsPopulated', true)
						
						facilityEl.multiselect("open")
					}
				})
				
				return false
		   },
			multiple : false,
			minWidth : 400
		}).multiselectfilter()
	}
	
	if (mode == 'add') {
		$('#volunteerSearchScopeNational' + uid).click()
		$('input[type=radio][name="volunteerSearchScope' + uid + '"]').prop('disabled', true)
		$("#volunteerSearchIncludeInactive" + uid).prop('checked', true)
		$("#volunteerSearchIncludeInactive" + uid).prop('disabled', true)
		$(".volunteerAddLink" + uid).click(addButtonCallbackMethod)
	} else if (mode == 'duplicateCheck') {
		$(".volunteerSearchFields" + uid).hide()
	}
	
	if (showDisclaimer)
		$('#volSearchDisclaimer' + uid).show()
		
	dialogEl.show()
}

var volunteerSearchResults = new Object()

function restorePreviousSearch(uid) {
	$.ajax({
		url : ajaxHomePath + "/volunteerSearch/mostRecent",
		type : "POST",
		dataType : 'json',
		error : commonAjaxErrorHandler,
		success : function(results) {
			if (results != null && results.volunteers.length > 0) {
				
				// restore input values
				
//				firstName : $("#volunteerSearchFirstName" + uid).val(),
//				lastName : $("#volunteerSearchLastName" + uid).val(),
//				code : $("#volunteerSearchCode" + uid).val(),
//				email : $("#volunteerSearchEmail" + uid).val(),
//				dob : dateOfBirth,
//				scope : theScope,
//				facilityId : $("#volunteerSearchScopeFacilityId" + uid).val(),
//				includeInactive : $("#volunteerSearchIncludeInactive" + uid).is(
//						':checked')
				
				processVolunteerSearchResults(uid, results.volunteers)
			}
		}
	})
}

function submitVolunteerSearchForm(uid) {
	if (allValsEmpty(["volunteerSearchFirstName" + uid,
	                  "volunteerSearchLastName" + uid,
	                  "volunteerSearchCode" + uid,
	                  "volunteerSearchEmail" + uid,
	                  "volunteerSearchDOB" + uid])) {
		displayAttentionDialog('Please enter at least one piece of search criteria.')
		return
	}
	
	$('#volSearchDisclaimer' + uid).hide()
	var dialogEl = $("#volunteerSearchDialog" + uid)
	var theScope = $(
			"input[type='radio'][name='volunteerSearchScope" + uid
					+ "']:checked").val()

	if (theScope != 'National' && !$("#volunteerSearchScopeFacilityId" + uid).val()) {
		displayAttentionDialog('Please select a facility.')
		return
	}
	
	var dateOfBirth = $("#volunteerSearchDOB" + uid).val()
	if ($.trim(dateOfBirth) != '' && !validateDate(dateOfBirth)) {
		displayAttentionDialog('Please enter a valid date of birth.')
		return
	}
					
	$.ajax({
		url : ajaxHomePath + "/volunteerSearch/find",
		type : "POST",
		dataType : 'json',
		data : {
			firstName : $("#volunteerSearchFirstName" + uid).val(),
			lastName : $("#volunteerSearchLastName" + uid).val(),
			code : $("#volunteerSearchCode" + uid).val(),
			email : $("#volunteerSearchEmail" + uid).val(),
			dob : dateOfBirth,
			scope : theScope,
			facilityId : $("#volunteerSearchScopeFacilityId" + uid).val(),
			includeInactive : $("#volunteerSearchIncludeInactive" + uid).is(
					':checked')
		},
		error : commonAjaxErrorHandler,
		success : function(results) {
			processVolunteerSearchResults(uid, results)
		}
	})
}

function processVolunteerSearchResults(uid, results) {
	var dialogEl = $("#volunteerSearchDialog" + uid)
	
	$("#volunteerSearchNoResults" + uid).hide()
	$("#volunteerSearchResultsTable" + uid).hide()

	var resultMap = new Object()

	var table = $('#volunteerSearchResultsList' + uid).DataTable()
	table.clear()

	for (var i = 0; i < results.length; i++) {
		resultMap['' + results[i].id] = results[i]
		table.row.add(results[i])
	}

	volunteerSearchResults[uid] = resultMap

	$("#volunteerSearchMaxResults" + uid).toggle(results.length == dialogEl.data('maxResults'))
	
	if (results.length > 0) {
		$("#volunteerSearchResultsTable" + uid).show()

		table.search('').columns().search('')
		rebuildTableFilters('volunteerSearchResultsList' + uid)
	} else {
		$("#volunteerSearchNoResults" + uid).show()
	}
	table.draw()

	var mode = dialogEl.data('mode')
	if (mode == 'add') {
		$(".volunteerAddLink" + uid).show()
	}
	
	$('#volunteerSearchLastName' + uid).focus()
}


function getVolunteerDashedBoxEl(volunteer) {
	var addressHtml = volunteer.addressMultilineDisplay ? escapeHTML(volunteer.addressMultilineDisplay) : ""
	
	var phoneHtml = volunteer.phone ? escapeHTML(volunteer.phone) + '<br>' : "";
	var emailHtml = ""
	if (volunteer.email)
		emailHtml = escapeHTML(volunteer.email) + '<a href="mailto:'
				+ escapeHTML(volunteer.email)
				+ '"><img alt="Click to email '
				+ escapeHTML(volunteer.email) + '"' + 'src="' + imgHomePath
				+ '/envelope.jpg" height="14"'
				+ ' width="18" border="0" align="absmiddle"'
				+ ' style="padding-left: 4px; padding-right: 4px" /></a>'
				
	var theHtml = '<table width="100%" class="addressBox">'
		+ '<tr valign="top"><td width="1%" nowrap>' + convertLinefeedToBR(addressHtml)
		+ '</td><td width="10">&nbsp;</td><td align="right" nowrap width="99%">'
		+ phoneHtml + emailHtml + '</td></tr></table>'
	var boxEl = getBoxEl(theHtml, false)
	boxEl.css('width', '100%')
	return boxEl
}