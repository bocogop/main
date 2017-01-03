function popupVoterSearch(uid, voterListToShow, options) {
	var dialogEl = $("#voterSearchDialog" + uid)
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
		$('#voterSearchScopeNational' + uid).click()
		$('input[type=radio][name="voterSearchScope' + uid + '"]').prop('disabled', true)
		$("#voterSearchIncludeInactive" + uid).prop('checked', true)
		$("#voterSearchIncludeInactive" + uid).prop('disabled', true)
	} else {
		$('#voterSearchScopeLocal' + uid).click()
		$('#voterSearchIncludeInactive' + uid).prop('checked', false)
	}
	
	$("#voterSearchFirstName" + uid).val(options.searchFirstNameStr)
	$("#voterSearchLastName" + uid).val(options.searchLastNameStr)
	$("#voterSearchCode" + uid).val(options.searchCode)
	$("#voterSearchEmail" + uid).val(options.searchEmail)
	$("#voterSearchDOB" + uid).val(options.searchDOB)
	$("#voterSearchNoResults" + uid).hide()
	$("#voterSearchResultsTable" + uid).hide()
	
	$('#voterSearchLastName' + uid).focus()
	
	$("#voterSearchDialog" + uid).dialog('open')
	
	// ------ customize submit button based on mode
	
	if (options.submitButtonStr != null) {
		if (mode == 'duplicateCheck') {
			$("#createOrUpdateAnywayButton" + uid).button( "option", "label", options.submitButtonStr );
		} // else ...
	}
	
	// ------ preload search results if specified; otherwise, check for previous search in session on server
	
	if (voterListToShow) {
		processVoterSearchResults(uid, voterListToShow)
	} else if ($.trim(options.searchFirstNameStr) != '' || $.trim(options.searchLastNameStr) != '') {
		submitVoterSearchForm(uid)
	} else if (mode == 'search') {
		restorePreviousSearch(uid)
	}
}

function voterSearchPopupItemSelected(uid, voterId) {
	var voterObj = voterSearchResults[uid]['' + voterId]
	var theDialog = $("#voterSearchDialog" + uid)
	theDialog.dialog('close')
	theDialog.data('callbackMethod')(voterObj)
}

function initVoterSearchPopup(options) {
	var mode = options.mode
	var uid = options.uid
	var maxResults = options.maxResults
	var callbackMethod = options.callbackMethod
	var addButtonCallbackMethod = options.addButtonCallbackMethod
	var showDisclaimer = options.showDisclaimer
	
	if (mode != 'add' && mode != 'search' && mode != 'duplicateCheck') {
		alert('Invalid mode for voter search popup with id "' + uid + '": ' + mode)
		return
	}
	
	var parms = {
		"columnDefs" : [
				{
					"targets" : 0,
					"data" : function(row, type, val, meta) {
						var voterNameEscaped = escapeHTML(row.displayName)
						if (type === 'display') {
							return '<a class="appLink" href="javascript:voterSearchPopupItemSelected(\''
									+ uid + '\', ' + row.id + ')">'
									+ voterNameEscaped + '</a><br><i>Code:</i> '
									+ (row.identifyingCode ? escapeHTML(row.identifyingCode) : '(none)')
						} else {
							return voterNameEscaped
						}
					}
				},
				{
					"targets" : 1,
					"data" : function(row, type, val, meta) {
						var theText = escapeHTML(row.primaryPrecinct ? row.primaryPrecinct.displayName : '(Unknown)')
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
							return row.status.name + (row.status.voterActive == false ? " as of " +
									 row.statusDate : '')
						}
					}
				},
				{
					"targets" : 5,
					"data" : function(row, type, val, meta) {
						var contactInfoHtml = getVoterDashedBoxEl(row).outerHTML()
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

	var theDataTable = $('#voterSearchResultsList' + uid).DataTable(parms)
	
	var dialogEl = $("#voterSearchDialog" + uid)
	if (mode == 'add') {
		dialogEl.attr('title', 'Add New Voter')
	} else {
		dialogEl.attr('title', 'Search for Voter')
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
		
		$.each([ "#voterSearchFirstName" + uid, "#voterSearchLastName" + uid ], function(index,
				value) {
			$(value).keypress(function(event) {
				if (event.which == 13) {
					submitVoterSearchForm(uid)
				}
			})
		})
		
		$(".voterSearchLink" + uid).click(function(evt) {
			submitVoterSearchForm(uid)
		})
		
		$('#voterSearchScopeLocal' + uid).click(function() {
			$("#voterSearchPrecinctWrapper" + uid).show()
		})
		$('#voterSearchScopeNational' + uid).click(function() {
			$("#voterSearchPrecinctWrapper" + uid).hide()
		})
		
		$("#voterSearchDOB" + uid).enableDatePicker({
			showOn : "button",
			buttonImage : imgHomePath + "/calendar.gif",
			buttonImageOnly : true
		})
		$("#voterSearchDOB" + uid).mask(twoDigitDateMask, {
			autoclear : false
		})
		
		var precinctEl = $("#voterSearchScopePrecinctId" + uid)
		precinctEl.multiselect({
			selectedText : function(numChecked, numTotal, checkedItems) {
				return abbreviate($(checkedItems[0]).next().text())
			},
			beforeopen: function(){
				if (dialogEl.data('stationsPopulated')) return
				var curVal = precinctEl.val()
				
				$.ajax({
					url : ajaxHomePath + "/voterSearch/precincts",
					type : "POST",
					dataType : 'json',
					error : commonAjaxErrorHandler,
					success : function(results) {
						precinctEl.empty()
						var newHtml = []
						$.each(results, function(index, item) {
							var selectedText = (item.id == curVal) ? ' selected="selected"' : ''
							newHtml.push('<option value="' + item.id + '"' + selectedText + '>' + item.displayName + '</option>')
						})
						precinctEl.html(newHtml.join(''))
						
						precinctEl.val(curVal)
						precinctEl.multiselect("refresh")
						dialogEl.data('stationsPopulated', true)
						
						precinctEl.multiselect("open")
					}
				})
				
				return false
		   },
			multiple : false,
			minWidth : 400
		}).multiselectfilter()
	}
	
	if (mode == 'add') {
		$('#voterSearchScopeNational' + uid).click()
		$('input[type=radio][name="voterSearchScope' + uid + '"]').prop('disabled', true)
		$("#voterSearchIncludeInactive" + uid).prop('checked', true)
		$("#voterSearchIncludeInactive" + uid).prop('disabled', true)
		$(".voterAddLink" + uid).click(addButtonCallbackMethod)
	} else if (mode == 'duplicateCheck') {
		$(".voterSearchFields" + uid).hide()
	}
	
	if (showDisclaimer)
		$('#volSearchDisclaimer' + uid).show()
		
	dialogEl.show()
}

var voterSearchResults = new Object()

function restorePreviousSearch(uid) {
	$.ajax({
		url : ajaxHomePath + "/voterSearch/mostRecent",
		type : "POST",
		dataType : 'json',
		error : commonAjaxErrorHandler,
		success : function(results) {
			if (results != null && results.voters.length > 0) {
				
				// restore input values
				
//				firstName : $("#voterSearchFirstName" + uid).val(),
//				lastName : $("#voterSearchLastName" + uid).val(),
//				code : $("#voterSearchCode" + uid).val(),
//				email : $("#voterSearchEmail" + uid).val(),
//				dob : dateOfBirth,
//				scope : theScope,
//				precinctId : $("#voterSearchScopePrecinctId" + uid).val(),
//				includeInactive : $("#voterSearchIncludeInactive" + uid).is(
//						':checked')
				
				processVoterSearchResults(uid, results.voters)
			}
		}
	})
}

function submitVoterSearchForm(uid) {
	if (allValsEmpty(["voterSearchFirstName" + uid,
	                  "voterSearchLastName" + uid,
	                  "voterSearchCode" + uid,
	                  "voterSearchEmail" + uid,
	                  "voterSearchDOB" + uid])) {
		displayAttentionDialog('Please enter at least one piece of search criteria.')
		return
	}
	
	$('#volSearchDisclaimer' + uid).hide()
	var dialogEl = $("#voterSearchDialog" + uid)
	var theScope = $(
			"input[type='radio'][name='voterSearchScope" + uid
					+ "']:checked").val()

	if (theScope != 'National' && !$("#voterSearchScopePrecinctId" + uid).val()) {
		displayAttentionDialog('Please select a precinct.')
		return
	}
	
	var dateOfBirth = $("#voterSearchDOB" + uid).val()
	if ($.trim(dateOfBirth) != '' && !validateDate(dateOfBirth)) {
		displayAttentionDialog('Please enter a valid date of birth.')
		return
	}
					
	$.ajax({
		url : ajaxHomePath + "/voterSearch/find",
		type : "POST",
		dataType : 'json',
		data : {
			firstName : $("#voterSearchFirstName" + uid).val(),
			lastName : $("#voterSearchLastName" + uid).val(),
			code : $("#voterSearchCode" + uid).val(),
			email : $("#voterSearchEmail" + uid).val(),
			dob : dateOfBirth,
			scope : theScope,
			precinctId : $("#voterSearchScopePrecinctId" + uid).val(),
			includeInactive : $("#voterSearchIncludeInactive" + uid).is(
					':checked')
		},
		error : commonAjaxErrorHandler,
		success : function(results) {
			processVoterSearchResults(uid, results)
		}
	})
}

function processVoterSearchResults(uid, results) {
	var dialogEl = $("#voterSearchDialog" + uid)
	
	$("#voterSearchNoResults" + uid).hide()
	$("#voterSearchResultsTable" + uid).hide()

	var resultMap = new Object()

	var table = $('#voterSearchResultsList' + uid).DataTable()
	table.clear()

	for (var i = 0; i < results.length; i++) {
		resultMap['' + results[i].id] = results[i]
		table.row.add(results[i])
	}

	voterSearchResults[uid] = resultMap

	$("#voterSearchMaxResults" + uid).toggle(results.length == dialogEl.data('maxResults'))
	
	if (results.length > 0) {
		$("#voterSearchResultsTable" + uid).show()

		table.search('').columns().search('')
		rebuildTableFilters('voterSearchResultsList' + uid)
	} else {
		$("#voterSearchNoResults" + uid).show()
	}
	table.draw()

	var mode = dialogEl.data('mode')
	if (mode == 'add') {
		$(".voterAddLink" + uid).show()
	}
	
	$('#voterSearchLastName' + uid).focus()
}


function getVoterDashedBoxEl(voter) {
	var addressHtml = voter.addressMultilineDisplay ? escapeHTML(voter.addressMultilineDisplay) : ""
	
	var phoneHtml = voter.phone ? escapeHTML(voter.phone) + '<br>' : "";
	var emailHtml = ""
	if (voter.email)
		emailHtml = escapeHTML(voter.email) + '<a href="mailto:'
				+ escapeHTML(voter.email)
				+ '"><img alt="Click to email '
				+ escapeHTML(voter.email) + '"' + 'src="' + imgHomePath
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