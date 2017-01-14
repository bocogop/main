function popupVoterSearch(uid, voterListToShow, options) {
	var dialogEl = $("#voterSearchDialog" + uid)
	var mode = dialogEl.data('mode')
	
	options = $.extend({
		searchFirstNameStr : '',
		searchLastNameStr : '',
		searchVoterId : '',
		searchEmail : '',
		searchYOB : '',
		submitButtonStr : null
	}, options)
	
	// ---- Set defaults immediately
	
	$("#voterSearchFirstName" + uid).val(options.searchFirstNameStr)
	$("#voterSearchLastName" + uid).val(options.searchLastNameStr)
	$("#voterSearchVoterId" + uid).val(options.searchCode)
	$("#voterSearchEmail" + uid).val(options.searchEmail)
	$("#voterSearchYOB" + uid).val(options.searchYOB)
	$("#voterSearchNoResults" + uid).hide()
	$("#voterSearchResultsTable" + uid).hide()
	
	$('#voterSearchLastName' + uid).focus()
	
	$("#voterSearchDialog" + uid).dialog('open')
	
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
	
	if (mode != 'search') {
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
									+ voterNameEscaped + '</a>'
						} else {
							return voterNameEscaped
						}
					}
				},
				{
					"targets" : 1,
					"data" : function(row, type, val, meta) {
						var theText = escapeHTML(row.voterId)
						return theText
					}
				},
				{
					"targets" : 2,
					"data" : function(row, type, val, meta) {
						var theText = escapeHTML(row.precinct ? row.precinct.name : '(Unknown)')
						return theText
					}
				},
				{
					"targets" : 3,
					"data" : function(row, type, val, meta) {
						if (type === 'display') {
							return escapeHTML(row.birthYear) + "<br><nobr>(~" + row.ageApprox + " yrs)</nobr>"
						} else {
							return row.birthYear
						}
					}
				},
				{
					"targets" : 4,
					"data" : function(row, type, val, meta) {
						return escapeHTML(row.gender.name)
					}
				},
				{
					"targets" : 5,
					"data" : function(row, type, val, meta) {
						if (type === 'filter') {
							return row.statusActive ? 'Active' : 'Inactive'
						} else {
							return (row.statusActive ? 'Active' : 'Inactive') +
								(row.statusReason ? ' (' + row.statusReason + ')' : '')
						}
					}
				},
				{
					"targets" : 6,
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
	dialogEl.attr('title', 'Search for Voter')
	
	var buttonConfig = {}
	buttonConfig['Cancel'] = function() { $(this).dialog('close') }
	
	dialogEl.dialog({
		autoOpen : false,
		modal : false,
		width : 900,
		height : 600,
		closeOnEscape : true,
		draggable : true,
		resizable : true,
		buttons : buttonConfig
	})
	dialogEl.data('mode', mode)
	dialogEl.data('maxResults', maxResults)
	dialogEl.data('callbackMethod', callbackMethod)
	
	if (mode == 'search') {
		
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
	}
	
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
//				yob : yearOfBirth,
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
	                  "voterSearchVoterId" + uid,
	                  "voterSearchEmail" + uid,
	                  "voterSearchYOB" + uid])) {
		displayAttentionDialog('Please enter at least one piece of search criteria.')
		return
	}
	
	var dialogEl = $("#voterSearchDialog" + uid)

	$.ajax({
		url : ajaxHomePath + "/voterSearch/find",
		type : "POST",
		dataType : 'json',
		data : {
			firstName : $("#voterSearchFirstName" + uid).val(),
			lastName : $("#voterSearchLastName" + uid).val(),
			voterId : $("#voterSearchCode" + uid).val(),
			email : $("#voterSearchEmail" + uid).val(),
			birthYear : $("#voterSearchYOB" + uid).val()
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

	$('#voterSearchLastName' + uid).focus()
}


function getVoterDashedBoxEl(voter) {
	var addressHtml = voter.addressMultilineDisplay ? escapeHTML(voter.addressMultilineDisplay) : ""
	
	var phoneHtml = voter.finalPhone ? escapeHTML(voter.finalPhone) + '<br>' : "";
	var emailHtml = ""
	if (voter.finalEmail)
		emailHtml = escapeHTML(voter.finalEmail) + '<a href="mailto:'
				+ escapeHTML(voter.finalEmail)
				+ '"><img alt="Click to email '
				+ escapeHTML(voter.finalEmail) + '"' + 'src="' + imgHomePath
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