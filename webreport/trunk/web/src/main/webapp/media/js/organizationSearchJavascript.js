function popupOrganizationSearch(uid, searchNameStr) {
	$("#organizationSearchName" + uid).val(searchNameStr)
	$("#organizationSearchAbbreviation" + uid).val('')
	$("#organizationSearchIncludeInactive" + uid).prop('checked', false)
	if(uid == 'donationSearch')
		$("#inactiveOptionRow" + uid).hide()
	$("#organizationSearchNoResults" + uid).hide()
	$("#organizationSearchResultsTable" + uid).hide()
	$('#organizationSearchName' + uid).focus()
	$("#organizationSearchDialog" + uid).dialog('open')
}

function organizationSearchPopupItemSelected(uid, organizationId) {
	var organizationObj = organizationSearchResults[uid]['' + organizationId]
	var theDialog = $("#organizationSearchDialog" + uid)
	theDialog.dialog('close')
	theDialog.data('callbackMethod')(organizationObj)
}

function initOrganizationSearchPopup(options) {
	var uid = options.uid
	var maxResults = options.maxResults
	var callbackMethod = options.callbackMethod
	var includeInactiveOption = options.includeInactiveOption
	var addButtonCallbackMethod = options.addButtonCallbackMethod
	var mode = options.mode
	
	var parms = {
		"columnDefs" : [
				{
					"targets" : 0,
					"data" : function(row, type, val, meta) {
						if (type === 'sort')
							return row.precinct ? row.precinct.displayName : '11111111'
						return row.precinct ? row.precinct.displayName : 'National'
						
					}
				},{
					"targets" : 1,
					"data" : function(row, type, val, meta) {
						var organizationNameEscaped = escapeHTML(row.displayName)
						if (type === 'display') {
							return '<a class="appLink" href="javascript:organizationSearchPopupItemSelected(\''
									+ uid + '\', ' + row.id + ')">'
									+ organizationNameEscaped + '</a>'
						} else {
							return organizationNameEscaped
						}
					}
				},{
					"targets" : 2,
					"data" : function(row, type, val, meta) {
						return row.scale
					}
				},{
					"targets" : 3,
					"data" : function(row, type, val, meta) {
						return escapeHTML(row.abbreviation)
					}
				}],
		"dom" : '<"top"fi>rt<"bottom"pl><"clear">',
		"pagingType" : "full_numbers",
		"pageLength" : 10,
		"lengthMenu" : [ [ 10, -1 ], [ 10, "All" ] ],
		"stateSave" : false
	}

	var theDataTable = $('#organizationSearchResultsList' + uid).DataTable(parms)

	var dialogEl = $("#organizationSearchDialog" + uid)
	
	dialogEl.dialog({
		autoOpen : false,
		modal : false,
		width : 800,
		height : 600,
		closeOnEscape : true,
		draggable : true,
		resizable : true,
		buttons : {
			'Cancel' :  function() { $(this).dialog('close') }
		}
	})
	dialogEl.data('maxResults', maxResults)
	dialogEl.data('callbackMethod', callbackMethod)
	
	var enterSubmit = function(event) {
		if (event.which == 13) {
			submitOrganizationSearchForm(uid, mode)
		}
	}
	$("#organizationSearchName" + uid).keypress(enterSubmit)
	$("#organizationSearchAbbreviation" + uid).keypress(enterSubmit)

	$(".organizationSearchLink" + uid).click(function(evt) {
		submitOrganizationSearchForm(uid, mode)
	})
	
	$("#inactiveOptionRow" + uid).toggle(includeInactiveOption)
	if (!includeInactiveOption)
		$("#organizationSearchIncludeInactive" + uid).attr('checked', false)
		
	if (shouldShowAddButton) {
		$(".organizationAddLink" + uid).click(addButtonCallbackMethod)
	}

	dialogEl.show()
}

var organizationSearchResults = new Object()

function submitOrganizationSearchForm(uid, mode) {
	if (allValsEmpty(["organizationSearchName" + uid,
	                  "organizationSearchAbbreviation" + uid])) {
		displayAttentionDialog('Please enter at least one piece of search criteria.')
		return
	}
	
	var dialogEl = $("#organizationSearchDialog" + uid)

	$.ajax({
		url : ajaxHomePath + "/findOrganizations",
		type : "POST",
		dataType : 'json',
		data : {
			name : $("#organizationSearchName" + uid).val(),
			abbreviation : $("#organizationSearchAbbreviation" + uid).val(),
			includeInactive : $("#organizationSearchIncludeInactive" + uid).is(
					':checked'),
			mode : mode
		},
		error : commonAjaxErrorHandler,
		success : function(results) {
			processOrganizationSearchResults(uid, results, mode)
		}
	})
}

function processOrganizationSearchResults(uid, results, mode) {
	var dialogEl = $("#organizationSearchDialog" + uid)
	
	$("#organizationSearchNoResults" + uid).hide()
	$("#organizationSearchResultsTable" + uid).hide()

	var resultMap = new Object()

	var table = $('#organizationSearchResultsList' + uid).DataTable()
	table.clear()

	for (var i = 0; i < results.length; i++) {
		resultMap['' + results[i].id] = results[i]
		table.row.add(results[i])
	}

	organizationSearchResults[uid] = resultMap

	$("#organizationSearchMaxResults" + uid).toggle(results.length == dialogEl.data('maxResults'))
	
	if (results.length > 0) {
		$("#organizationSearchResultsTable" + uid).show()

		table.search('').columns().search('')
		rebuildTableFilters('organizationSearchResultsList' + uid)
		
	} else {
		$("#organizationSearchNoResults" + uid).show()
	}
	table.draw()
	if (shouldShowAddButton)
		$(".organizationAddLink" + uid).show()
	
	$("#organizationSearch" + uid + "FilterRow select").val("(all)")
	$('#organizationSearchName' + uid).focus()
}