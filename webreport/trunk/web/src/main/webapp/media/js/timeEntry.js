var siteVoters = new Array()
var siteVotersById = new Object()
var lastTimeEntryTypeSelected

/*
 * Array of { index, voterId, date, selectedAssignmentId, selectedOrganizationId, hours }
 */
var timeEntryItems = []
var timeReportItems
var adjustedHoursItems

$(function() {
	
	// ---------------------------------- build screen components
	
	$("input[name='timeEntryType']")
		.click(timeEntryTypeClicked)
		.keypress(function(event) {
			if (event.which == $.ui.keyCode.ENTER) {
				$("#timeEntryList input").first().focus()
			}
		})

	$("#postAllButton").click(postAll)
	
	buildTimeEntryTable()
	buildTimeReportTable()
	buildAdjustedHoursTable()
	workEntryPopupInit()
	adjustedHoursPopupInit()
	prepareVoterMaster()
	prepareDateMaster()
	
	$('#adjustedHoursShowHide').showHide({
		speed : 500,
		easing : '',
		showText : 'View/Edit Adjusted Hours',
		hideText : 'Close Adjusted Hours',
		showCallback : refreshAdjustedHoursTable
	})
	
	// ---------------------------------- init screen values
	
	var nowDateStr = getDateStrFromDate(new Date())
	for (var i = 0; i < 7; i++) {
		timeEntryItems.push({
			index : i,
			voterId : null,
			date : nowDateStr,
			selectedAssignmentId : null,
			selectedOrganizationId : null,
			hours : null
		})
	}
	
	if (volIdRequested == null) {
		$("#byDate").prop('checked', true)
		$("#dateMaster").val(nowDateStr)
		lastTimeEntryTypeSelected = 'byDate'
	} else {
		$("#byVoter").prop('checked', true)
		lastTimeEntryTypeSelected = 'byVoter'
		$(".adjustedHoursWrapper").show()
	}
	
	resetAllAndRefreshReport(false)
	$("#timeEntryList input").first().focus()
	
	$.ajax({
		url : ajaxHomePath + "/voter/quickSearch/currentPrecinct",
		success : function(quickSearchResponse) {
			siteVoters = quickSearchResponse.voters
			siteVotersById = new Object()
			$(siteVoters).each(function(index, item) {
				siteVotersById[item.id] = item
			})
			if (volIdRequested) {
				 voterSelected(volIdRequested)
			}
		},
		error : commonAjaxErrorHandler
	})
	
	if (!isReadOnly) {
		$("#timeEntryWrapper").show()
	}
})

function getSiteVoter(volId) {
	var vol = siteVotersById[volId]
	if (vol == null) {
		alert('The current voter does not have an active assignment and active organization at the current precinct.\n\nYou will be returned to this voter\'s profile for evaluation.')
		jumpToVoter(volId)
	}
	return vol
}

function voterSelected(volId) {
	var vol = getSiteVoter(volId)
	
	$("#voterMasterId").val(volId)
	/*
	 * workaround for this function not setting the value quickly enough before the blur()
	 * below is activated - CPB
	 */ 
	$("#voterMaster").val(vol.name)
	
	for (var i = 0; i < timeEntryItems.length; i++)
		updateTimeEntryItemsForVoterSelected(i, volId)
	
	refreshTimeEntryTable()
	refreshTimeReportTable()
	refreshAdjustedHoursTable()
	$(".adjustedHoursWrapper").show()
	$("#timeEntryList input").first().focus()
}

function prepareVoterMaster() {
	$("#voterMaster").autocomplete({
		minLength : 1,
		
		select: function(event, ui) {
			var inputEl = $(event.target)
			var volId = ui.item.id
			voterSelected(volId)			
		},
		source : voterAutocompleteSource
	})
	
	$("#voterMaster").blur(function() {
		var volId = $("#voterMasterId").val()
		
		if (volId == '' || $(this).val() != getSiteVoter(volId).name) {
			$("#voterMaster").val('')
			$("#voterMasterId").val('')
			for (var i = 0; i < timeEntryItems.length; i++)
				updateTimeEntryItemsForVoterSelected(i, null)
			$(this).val('')
			$(".assignmentWrapper").empty()
			$(".adjustedHoursWrapper").hide()
		}
	})
}

function prepareDateMaster() {
	var changeFunc = function(el) {
		if (!validateDate(el.val())) return
		
		var theDate = getDateFromMMDDYYYY(el.val())
		if (!theDate) {
			return
		}
		var earliestPossible = getDateFromYYYYMMDD(iso8601EarliestAcceptableDateEntry)
		var latestPossible = new Date()
		var acceptable = theDate >= earliestPossible && theDate <= latestPossible
		$("#timeEntryForbidden").toggle(!acceptable && !isReadOnly)
		$("#timeEntryWrapper").toggle(acceptable && !isReadOnly)
		
		if (acceptable)
			refreshTimeEntryTable()
		refreshTimeReportTable()
	}
	
	prepareDateInput(timeEntryItems, $("#dateMaster"), function(dateText, el) {
		changeFunc(el.input)
	})
	$("#dateMaster").change(function() {
		changeFunc($(this))
	})
	$("#dateMaster").keydown(function(e) {
		var keyCode = e.keyCode || e.which
		if (keyCode == $.ui.keyCode.ENTER) {
			changeFunc($(this))
		}
	})
}

function workEntryPopupInit() {
	prepareDateInput(timeEntryItems, $("#editWorkEntryDate"), function(dateText, el) {
		// var index = el.input.attr('index')
		// timeEntryItems[index].date = getDateFromMMDDYYYY(dateText)
	})
	prepareHoursInput(timeEntryItems, $("#editWorkEntryHours"), false)
	
	var submitEditWorkEntry = function() {
		var date = $("#editWorkEntryDate").val()
		var hours = $("#editWorkEntryHours").val()
		var assignmentId = $("#editWorkEntryAssignmentId").val()
		var organizationId = $("#editWorkEntryOrganizationId").val()
		
		var errors = new Array()
		if (date == '')
			errors.push('Please enter the date.')
		if (hours == '')
			errors.push('Please enter the hours.')
		if (assignmentId == '')
			errors.push('Please select an assignment.')
		if (organizationId == '')
			errors.push('Please select an organization.')
		
		if (errors.length > 0) {
			displayAttentionDialog("Please correct the following errors: <ul><li>"
					+ errors.join("</li><li>") + "</li></ul>");
			return
		}
		
		$.ajax({
			url : ajaxHomePath + '/timeEntry/update',
			method: 'POST',
			dataType : 'json',
			data : {
				id : $("#editWorkEntryDialog").data('workEntryId'),
				date : date,
				hours : hours,
				assignmentId : assignmentId,
				organizationId : organizationId
			},
			error : commonAjaxErrorHandler,
			success : function(response) {
				$("#editWorkEntryDialog").dialog('close')
				refreshTimeReportTable()
		    }
		})
	}
	
	var dialogEl = $("#editWorkEntryDialog")
	dialogEl.dialog({
		autoOpen : false,
		modal : false,
		width : 700,
		height : 250,
		closeOnEscape : true,
		draggable : true,
		resizable : true,
		buttons : {
			'Submit' : {
				text : 'Submit',
				id : 'editWorkEntrySubmit',
				click : function() {
					doubleClickSafeguard($("#editWorkEntrySubmit"))
					submitEditWorkEntry()
				}
			},
			'Cancel' : function() {
				$(this).dialog('close')
			}
		}
	})
	
	$("#editWorkEntryDialog").show()
}

function adjustedHoursPopupInit() {
	prepareDateInput(timeEntryItems, $("#addAdjustedHoursDate"), function(dateText, el) {
		// var index = el.input.attr('index')
		// timeEntryItems[index].date = getDateFromMMDDYYYY(dateText)
	})
	prepareHoursInput(timeEntryItems, $("#addAdjustedHoursHours"), false, true)
	
	var submitAddAdjustedHours = function() {
		var date = $("#addAdjustedHoursDate").val()
		var hours = $("#addAdjustedHoursHours").val()
		var comments = $("#addAdjustedHoursComments").val()
		
		var errors = new Array()
		if (date == '')
			errors.push('Please enter the date.')
		if ($.trim(hours) == '') {
			errors.push('Please enter the hours.')
		} else if (hours < -99999.75 || hours > 99999.75) {
			errors.push('Please enter an "hours" value greater than -100,000 and less than 100,000.')
		} else if (hours == 0) {
			errors.push('Please enter a nonzero value for the hours.')
		}
		
		if ($.trim(comments) == '')
			errors.push('Please enter comments describing this entry.')
		
		if (errors.length > 0) {
			displayAttentionDialog("Please correct the following errors: <ul><li>"
					+ errors.join("</li><li>") + "</li></ul>");
			return
		}
		
		$.ajax({
			url : ajaxHomePath + '/adjustedHours/new',
			method: 'POST',
			dataType : 'json',
			data : {
				voterId : $("#voterMasterId").val(),
				date : date,
				hours : hours,
				comments : comments
			},
			error : commonAjaxErrorHandler,
			success : function(response) {
				$("#addAdjustedHoursEntryDialog").dialog('close')
				refreshAdjustedHoursTable()
		    }
		})
	}
	
	var dialogEl = $("#addAdjustedHoursEntryDialog")
	dialogEl.dialog({
		autoOpen : false,
		modal : false,
		width : 700,
		height : 220,
		closeOnEscape : true,
		draggable : true,
		resizable : true,
		buttons : {
			'Submit' : {
				text : 'Submit',
				id : 'addAdjustedHoursEntrySubmit',
				click : function() {
					doubleClickSafeguard($("#addAdjustedHoursEntrySubmit"))
					submitAddAdjustedHours()
				}
			},
			'Cancel' : function() {
				$(this).dialog('close')
			}
		}
	})
	
	$("#addAdjustedHoursEntryDialog").show()
}

function addAdjustedHoursEntry() {
	var date = $("#addAdjustedHoursDate").val('')
	var hours = $("#addAdjustedHoursHours").val('')
	var comments = $("#addAdjustedHoursComments").val('')
	$("#addAdjustedHoursEntryDialog").dialog('open')
}

function updateTimeEntryItemsForVoterSelected(index, volId) {
	timeEntryItems[index].voterId = volId
	if (volId == null) {
		timeEntryItems[index].selectedAssignmentId = null
		timeEntryItems[index].selectedOrganizationId = null
	} else {
		var vol = getSiteVoter(volId)
		
		if (vol.assignments.length == 1) {
			timeEntryItems[index].selectedAssignmentId = '' + vol.assignments[0].id
		} else {
			timeEntryItems[index].selectedAssignmentId = null
		}
		
		if (vol.organizations.length == 1) {
			timeEntryItems[index].selectedOrganizationId = '' + vol.organizations[0].id
		} else {
			timeEntryItems[index].selectedOrganizationId = null
		}
	}
}

function buildTimeEntryTable() {
	var theTable = $('#timeEntryList')
			.DataTable(
					{
						"columns" : [
								{
									"render" : function(row, type, val, meta) {
										if (type !== 'display') return ''
										
										var volInput = $('<input type="text" class="voterInput" />') //
											.attr('index', val.index) //
											.attr('id', 'voterInput' + val.index)
										volInput.attr('value', val.voterId ? (getSiteVoter(val.voterId).name || '') : '')
										var volInputId = $('<input type="hidden" class="voterInputId" />') //
											.attr('id', 'voterInputId' + val.index) //
											.attr('value', val.voterId || '')
										return $('<div></div>').append(volInput).append(volInputId).outerHTML()
									},
									"sortable" : false
								},{
									"render" : function(row, type, val, meta) {
										if (type !== 'display') return ''
										
										return '<nobr>' + $('<input type="text" class="dateInput" size="12">') //
											.attr('index', val.index) //
											.attr('id', 'dateInput' + val.index)
											.attr('value', val.date ? getDateStrFromDate(val.date) : '') //
											.outerHTML() + '</nobr>'
									},
									"sortable" : false
								},{
									"render" : function(row, type, val, meta) {
										if (type !== 'display') return ''
										
										return $('<input type="text" size="6" maxlength="5" class="hoursInput" />') //
											.attr('index', val.index) //
											.attr('id', 'hoursInputIndex' + val.index) //
											.attr('value', val.hours || '') //
											.attr('title', 'Enter hours as a decimal. For example, 5 hours and 15 minutes would be entered as 5.25. Field rounds to the nearest quarter hour.') //
											.outerHTML()
									},
									"sortable" : false
								},{
									"render" : function(row, type, val, meta) {
										if (type !== 'display') return ''
										
										var el = $('<div></div>').attr('id', 'assignmentWrapperIndex' + val.index)
										var dropdown = getAssignmentsDropdownEl(val.index)
										if (dropdown) el.append(dropdown)
										return el.outerHTML()
									},
									"sortable" : false
								},{
									"render" : function(row, type, val, meta) {
										if (type !== 'display') return ''
										
										var el = $('<div></div>').attr('id', 'organizationWrapperIndex' + val.index)
										var dropdown = getOrganizationsDropdownEl(val.index)
										if (dropdown) {
											el.append(dropdown)
										}
										return el.outerHTML()
									},
									"sortable" : false
								},{
									"render" : function(row, type, val, meta) {
										if (type !== 'display') return ''
										
										var results = ''
										if (timeEntryItems.length > 1) {
											results += '<a class="rowDeleteIcon" index="' + val.index + '" href="javascript:deleteInputRow(' + val.index + ')">'
												+ '<img src="' + imgHomePath + '/cross.png" border="0" align="absmiddle" alt="Delete Row"></a>'
										}
										if (meta.row == timeEntryItems.length - 1)
											results += '<a style="margin-left:15px" href="javascript:addInputRow()"><img src="'
												+ imgHomePath + '/add.png" border="0" align="absmiddle" alt="Add New Row"></a>'
										return '<nobr>' + results + '</nobr>'
									},
									"sortable" : false
								}
								],
						"data" : timeEntryItems,
						"dom" : '<"top">rt<"bottom"l><"clear">',
						"order" : [],
						"paging" : false,
						// "stripeClasses" : ['odd'],
					})
	return theTable
}

function buildTimeReportTable() {
	var theTable = $('#timeReportList')
			.DataTable(
					{
						buttons : [{
							extend : 'excel',
							exportOptions: {
								columns : [0,1,2,3,4,5]
							}
						}, {
							extend : 'pdfHtml5',
							orientation : 'landscape',
							exportOptions: {
								columns : [0,1,2,3,4,5]
							}
						}, {
							extend : 'print',
							exportOptions: {
								columns : [0,1,2,3,4,5]
							}
						} ],
						"columnDefs" : [
								{
									"targets" : 0,
									"data" : function(row, type, val, meta) {
										var v = row.voterAssignment.voter
										return v ? v.displayName : ''
									}
								},
								{
									"targets" : 1,
									"data" : function(row, type, val, meta) {
										if (type === 'sort')
											return getAsYYYYMMDD(row.dateWorked, '/')
										return row.dateWorked
									},
									"type" : "string"
								},
								{
									"targets" : 2,
									"data" : function(row, type, val, meta) {
										return '' + row.hoursWorked
									}
								},
								{
									"targets" : 3,
									"data" : function(row, type, val, meta) {
										if (type === 'filter') {
											return $.trim(row.voterAssignment.displayName.split('-')[0])
										}
										return row.voterAssignment.displayName
									}
								},
								{
									"targets" : 4,
									"data" : function(row) {
										return row.voterAssignment.locationDisplayName
									}
								},
								{
									"targets" : 5,
									"data" : function(row) {
										return row.organization ? row.organization.displayName : ''
									}
								},
								{
									"targets" : 6,
									"data" : function(row, type, val, meta) {
										var actions = '<div style="margin:0 auto; text-align:center"><nobr>'
										var dateMaster = getDateFromMMDDYYYY($("#dateMaster").val())
										var rowDate = getDateFromMMDDYYYY(row.dateWorked)
										
										var showButtons
										if (dateMaster != null) {
											showButtons = dateMaster >= getDateFromYYYYMMDD(iso8601EarliestAcceptableDateEntry)
										} else if (rowDate != null) {
											showButtons = rowDate >= getDateFromYYYYMMDD(iso8601EarliestAcceptableDateEntry)
										}
										
										if (row.organization.active !== true)
											showButtons = false
										if (isReadOnly) showButtons = false
										
										if (showButtons) {
											actions += '<a href="javascript:workEntryEdit('
												+ row.id + ', ' + row.voterAssignment.voter.id + ')"><img src="'+ imgHomePath
												+ '/edit-small.gif" alt="Edit Time Entry" border="0" hspace="5" align="center"/></a>'
											actions += '<a href="javascript:deleteWorkEntry('
													+ row.id
													+ ')"><img src="'+ imgHomePath
													+ '/permanently_delete_18x18.png" border="0" hspace="5" align="center" alt="Delete Time Entry" /></a>'
										}
										actions += '</nobr></div>'
										return actions
									}
								} ],
						"dom" : '<"top"fBi><"tableClear"><"top2"pl>rt<"clear">',
						"lengthMenu" : [ [ 20, 50, -1 ], [ 20, 50, "All" ] ],
						"order" : [],
						"pageLength" : 20,
						"pagingType" : "full_numbers"
					})

	return theTable
}

function buildAdjustedHoursTable() {
	var theTable = $('#adjustedHoursList')
			.DataTable(
					{
						"columnDefs" : [
								{
									"targets" : 0,
									"data" : function(row, type, val, meta) {
										if (type === 'type') return 'string'
										if (type === 'sort')
											return getAsYYYYMMDD(row.date, '/')
										return row.date
									}
								},
								{
									"targets" : 1,
									"data" : function(row, type, val, meta) {
										return row.hours
									}
								},
								{
									"targets" : 2,
									"data" : function(row, type, val, meta) {
										return row.description
									}
								},
								{
									"targets" : 3,
									"data" : function(row, type, val, meta) {
										return row.createdBy
									}
								}
								],
						"data" : timeEntryItems,
						"dom" : '<"top">rt<"bottom"l><"clear">',
						"order" : [],
						"paging" : false,
						// "stripeClasses" : ['odd'],
					})
	return theTable
}

function voterAutocompleteSource(request, response) {
	var theVal = $.trim(request.term).toLowerCase()
	if (theVal == '')
		return

	var matches = new Array()
	var tokens = theVal.match(/[A-Za-z0-9']+/g);

	outer: for (var i = 0; i < siteVoters.length && matches.length <= 10; i++) {
		var v = siteVoters[i]

		for (var t = 0; t < tokens.length; t++) {
			var token = tokens[t]

			if (v.name.toLowerCase().indexOf(token) == -1)
				continue outer;
		}
		
		matches.push({
			label : v.name + ' (DOB ' + v.dob + ')',
			value : v.name,
			id : v.id
		})
	}

	response(matches)
}

function prepareVoterInput($selector) {
	$selector.each(function() {
		var that = $(this)
		
		$(this).attr('aria-label', 'Type some characters to search matching active voters, or push Alt one to exit the worksheet')
		bindAlt1Keydown($(this))
		
		$(this).autocomplete({
			minLength : 1,
			select: function(event, ui) {
				var index = $(event.target).attr('index')
				var volId = ui.item.id
				var vol = getSiteVoter(volId)
				
				/*
				 * workaround for this function not setting the value quickly enough before the blur()
				 * below is activated - CPB
				 */ 
				that.val(vol.name)
				
				timeEntryItems[index].voterId = volId
				$("#voterInputId" + index).val(volId)
				
				updateTimeEntryItemsForVoterSelected(index, volId)
				
				$("#assignmentWrapperIndex" + index).empty()
				var el = getAssignmentsDropdownEl(index)
				if (el) {
					$("#assignmentWrapperIndex" + index).append(el)
					prepareAssignmentsDropdown($(".assignmentInput", "#assignmentWrapperIndex" + index))
				}
				
				$("#organizationWrapperIndex" + index).empty()
				var el = getOrganizationsDropdownEl(index)
				if (el) {
					$("#organizationWrapperIndex" + index).append(el)
					prepareOrganizationsDropdown($(".organizationInput", "#organizationWrapperIndex" + index))
				}
			},
			source : voterAutocompleteSource
		})
		
			$(this).blur(function() {
				var index = $(this).attr('index')
				var volId = $("#voterInputId" + index).val()
				
				if (volId == '' || $(this).val() != getSiteVoter(volId).name) {
					$("#voterInputId" + index).val('')
					timeEntryItems[index].voterId = null
					// timeEntryItems[index].voterId = null
					$(this).val('')
					$("#assignmentWrapperIndex" + index).empty()
					$("#organizationWrapperIndex" + index).empty()
				}
			})
		})
}

function getAssignmentsDropdownEl(index) {
	var volId = timeEntryItems[index].voterId
	var assignments = volId ? getSiteVoter(volId).assignments : null
	
	if (!assignments) return null
	
	var container = $("<div />")
	if (assignments.length == 1) {
		var assn = assignments[0]
		container.append($('<span class="singleAssignmentInput"></span>').text(assn.name))
	} else {
		var selectedAssignmentId = timeEntryItems[index].selectedAssignmentId
		var select = $('<select class="assignmentInput"></select>') //
			.attr('id', 'assignmentInputIndex' + index) //
			.attr('index', index)
		
		$("<option />").attr("value", "").text("Please select...").appendTo(select)
		for (var i = 0; i < assignments.length; i++) {
			var assn = assignments[i]
			var opt = $("<option />").attr("value", assn.id).text(assn.name)
			if (assn.id == selectedAssignmentId)
				opt.attr('selected', 'selected')
			opt.appendTo(select)
		}
		container.append(select)
	}
	
	return container
}

function getOrganizationsDropdownEl(index) {
	var volId = timeEntryItems[index].voterId
	var organizations = volId ? getSiteVoter(volId).organizations : null
	
	if (!organizations) return null
	
	var container = $("<div />")
	if (organizations.length == 1) {
		var org = organizations[0]
		container.append($('<span class="singleOrganizationInput"></span>').text(org.name))
	} else {
		var selectedOrganizationId = timeEntryItems[index].selectedOrganizationId
		var select = $('<select class="organizationInput"></select>') //
			.attr('id', 'organizationInputIndex' + index) //
			.attr('index', index)
		
		$("<option />").attr("value", "").text("Please select...").appendTo(select)
		for (var i = 0; i < organizations.length; i++) {
			var org = organizations[i]
			var opt = $("<option />").attr("value", org.id).text(org.name)
			if (org.id == selectedOrganizationId)
				opt.attr('selected', 'selected')
			opt.appendTo(select)
		}
		container.append(select)
	}
	
	return container
}

function prepareAssignmentsDropdown($selector) {
	$selector.each(function() {
		$(this).attr('aria-label', 'Select the voter assignment, or push Alt one to exit the worksheet')
		bindAlt1Keydown($(this))
		
		$(this).change(function() {
			var index = $(this).attr('index')
			
			var v = $(this).val()
			if (index) /*
							 * wouldn't exist if our hours input was outside the
							 * time entry table - CPB
							 */
				timeEntryItems[index].selectedAssignmentId = (v == '' ? null : v)
		})
	})
}

function prepareOrganizationsDropdown($selector) {
	$selector.each(function() {
		$(this).attr('aria-label', 'Select the organization, or push Alt one to exit the worksheet')
		bindAlt1Keydown($(this))
		$(this).change(function() {
			var index = $(this).attr('index')
			
			var v = $(this).val()
			if (index)
				timeEntryItems[index].selectedOrganizationId = (v == '' ? null : v)
		})
	})
}

function resetAllAndRefreshReport(clearMasterValues) {
	var isVoter = $("#byVoter").is(":checked")
	var isDate = $("#byDate").is(":checked")

	$("#voterMaster").prop('disabled', !isVoter)
	$("#dateMaster").prop('disabled', !isDate)
	
	if (clearMasterValues) {
		$("#voterMaster").val('')
		$("#voterMasterId").val('')
		$("#dateMaster").val('')
	}
	
	if (isVoter) {
		$("#voterMaster").focus()
	} else {
		$("#dateMaster").focus()
	}
	
	var timeEntryList = $("#timeEntryList").DataTable()
	timeEntryList.column(0).visible(!isVoter)
	timeEntryList.column(1).visible(!isDate)
	
	clearAndRefreshTimeEntryTable()
	
	$("#timeReportList").DataTable().clear().search('').columns().search('')
	rebuildTableFilters('timeReportList')
	
	refreshTimeReportTable()
}

function clearAndRefreshTimeEntryTable() {
	var numItems = timeEntryItems.length
	timeEntryItems.length = 0
	for (var i = 0; i < numItems; i++)
		addBlankTimeEntryItem()
	clearAllErrors()
	refreshTimeEntryTable()
}

function refreshTimeEntryTable() {
	var table = $("#timeEntryList").DataTable()
	table.clear().rows.add(timeEntryItems).draw()
	prepareAssignmentsDropdown($(".assignmentInput", "#timeEntryList"))
	prepareOrganizationsDropdown($(".organizationInput", "#timeEntryList"))
	prepareHoursInput(timeEntryItems, $(".hoursInput", "#timeEntryList"), true)
	prepareDateInput(timeEntryItems, $(".dateInput", "#timeEntryList"), function(dateText, el) {
		var index = el.input.attr('index')
		timeEntryItems[index].date = getDateFromMMDDYYYY(dateText)
	})
	prepareVoterInput($(".voterInput", "#timeEntryList"))
	prepareRowDeleteIcon($(".rowDeleteIcon", "#timeEntryList"))
	rebindAutoAddRowFn("#timeEntryList")
}

function refreshTimeReportTable(pageToRowWithDate) {
	$("#timeReportLegend").text('Time Report')
	var timeReportList = $("#timeReportList").DataTable()
	
	var isVoter = $("#byVoter").is(":checked")
	var isDate = $("#byDate").is(":checked")
	timeReportList.column(0).visible(!isVoter)
	timeReportList.column(1).visible(!isDate)
	
	var voterMasterId = $("#voterMasterId").val()
	var voterMasterName = $("#voterMaster").val()
	var dateMaster = $("#dateMaster").val()
	var isVoterMasterId = voterMasterId != ''
	var isDateMaster = dateMaster != ''
	if (!isVoterMasterId && !isDateMaster) {
		timeReportList.clear().draw()
		return
	}
	
	$("#timeReportLegend").html('Time Report for ' 
			+ (isVoter ? '<a class="appLink" href="javascript:jumpToVoter(' + voterMasterId + ')">'
					+ escapeHTML(voterMasterName) + '</a>' : dateMaster))
	
	timeReportItems = new Object()
	$.ajax({
		url : ajaxHomePath + '/timeEntry/timeReportBy' + (isVoter ? 'Voter' : 'Date'),
		dataType : 'json',
		data : {
			voterId : voterMasterId,
			date : dateMaster
		},
		error : commonAjaxErrorHandler,
		success : function(response) {
			for (var i = 0; i < response.length; i++) {
				timeReportItems[response[i].id] = response[i]
			}
			
			timeReportList.clear().search('').columns().search('')
			timeReportList.rows.add(response)
			timeReportList.draw()
			rebuildTableFilters('timeReportList')
			if (pageToRowWithDate) {
				pageToFirstMatchingRow(timeReportList, function (idx, data, node) {
			        return getDateFromMMDDYYYY(data.dateWorked) <= pageToRowWithDate
			    })
			}
	    }
	})
}

function refreshAdjustedHoursTable() {
	var adjustedHoursList = $("#adjustedHoursList").DataTable()
	
	var voterMasterName = $("#voterMaster").val()
	var voterMasterId = $("#voterMasterId").val()
	var isVoterMasterId = voterMasterId != ''
	if (!isVoterMasterId) {
		$("#adjustedHoursLegend").html('Adjusted Hours')
		adjustedHoursList.clear().draw()
		return
	}
	
	$("#adjustedHoursLegend").html('Adjusted Hours for ' 
			+ '<a class="appLink" href="javascript:jumpToVoter(' + voterMasterId + ')">'
			+ escapeHTML(voterMasterName) + '</a>' )
	
	adjustedHoursItems = new Object()
	$.ajax({
		url : ajaxHomePath + '/timeEntry/adjustedHours',
		dataType : 'json',
		data : {
			voterId : voterMasterId,
		},
		error : commonAjaxErrorHandler,
		success : function(response) {
			var adjustedHoursEntries = response.adjustedHoursEntries
			var usernameMap = response.usernameMap
			
			for (var i = 0; i < adjustedHoursEntries.length; i++) {
				$.extend(adjustedHoursEntries[i], {
					createdBy : usernameMap[adjustedHoursEntries[i].id]
				})
				adjustedHoursItems[adjustedHoursEntries[i].id] = adjustedHoursEntries[i]
			}
			
			adjustedHoursList.clear()
			adjustedHoursList.rows.add(adjustedHoursEntries)
			rebuildTableFilters('adjustedHoursList')
			adjustedHoursList.draw()
	    }
	})
}

function addBlankTimeEntryItem() {
	var voterMasterId = $("#voterMasterId").val()
	var dateMaster = $("#dateMaster").val()
	
	timeEntryItems.push({
		index : timeEntryItems.length,
		voterId : voterMasterId != '' ? voterMasterId : null,
		date : dateMaster != '' ? getDateFromMMDDYYYY($("#dateMaster").val()) : null,
		selectedAssignmentId : null,
		selectedOrganizationId : null,
		hours : null
	})
	
	if (voterMasterId != '')
		updateTimeEntryItemsForVoterSelected(timeEntryItems.length - 1, voterMasterId)
}

function addInputRow() {
	addBlankTimeEntryItem()
	refreshTimeEntryTable()
	$("#timeEntryList tr:last-child td:first-child:visible input").focus()
}

function timeEntryTypeClicked() {
	var idClicked = $(this).attr('id')
	if (idClicked === lastTimeEntryTypeSelected) return
	
	var that = $(this)
	
	var proceedFunc = function() {
		that.prop('checked', true)
		lastTimeEntryTypeSelected = idClicked
		var isByVoter = lastTimeEntryTypeSelected == 'byVoter'
		
		if (isByVoter) {
			$("#timeEntryForbidden").hide()
			if (!isReadOnly)
				$("#timeEntryWrapper").show()
		} else {
			$(".adjustedHoursWrapper").hide()
		}
		resetAllAndRefreshReport(true)
	}
	
	var hasVal = false
	$(".voterInput, .dateInput, .hoursInput, .assignmentInput", "#timeEntryList").each(function(index, item) {
		if ($(item).is(":visible") && $(item).val() != '') {
			hasVal = true
			return false
		}
	})
	
	if (hasVal) {
		confirmDialog('Your time entry values will be cleared. Proceed?', function() {
			proceedFunc()
		})
		return false
	} else {
		proceedFunc()
	}
}

function deleteInputRow(index) {
	timeEntryItems.splice(index, 1)
	for (var i = index; i < timeEntryItems.length; i++)
		timeEntryItems[i].index--
	refreshTimeEntryTable()
	$("#timeEntryList input[value='']:visible:first").focus()
}

function workEntryEdit(workEntryId, voterId) {
	var workEntry = timeReportItems[workEntryId]
	$("#editWorkEntryDialog").data('workEntryId', workEntryId)
	
	$("#editWorkEntryDate").val(workEntry.dateWorked)
	
	var paddedHours = getPaddedHoursStr(workEntry.hoursWorked)
	$("#editWorkEntryHours").val(paddedHours)
	
	$.ajax({
			url : ajaxHomePath + '/voter/quickSearch/individualPlusAssignmentsAndOrgs',
			method: 'POST',
			dataType : 'json',
			data : {
				voterId : voterId,
				onlyActiveAssignmentsAndOrgs : false
			},
			error : commonAjaxErrorHandler,
			success : function(response) {
				var container = $("#editWorkEntryAssignmentWrapper").empty()
				var selectedAssignmentId = workEntry.voterAssignment.id
				var select = $('<select id="editWorkEntryAssignmentId"></select>')
				$("<option />").attr("value", "").text("Please select...").appendTo(select)
				for (var i = 0; i < response.assignments.length; i++) {
					var assn = response.assignments[i]
					var opt = $("<option />").attr("value", assn.id).text(assn.name + (assn.active ? '' : ' (inactive)'))
					if (assn.id == selectedAssignmentId)
						opt.attr('selected', 'selected')
					opt.appendTo(select)
				}
				container.append(select)
				
				var orgContainer = $("#editWorkEntryOrganizationWrapper").empty()
				var selectedOrganizationId = workEntry.organization ? workEntry.organization.id : null
				var select = $('<select id="editWorkEntryOrganizationId"></select>')
				$("<option />").attr("value", "").text("Please select...").appendTo(select)
				for (var i = 0; i < response.organizations.length; i++) {
					var org = response.organizations[i]
					var opt = $("<option />").attr("value", org.id).text(org.name + (org.active ? '' : ' (inactive)'))
					if (org.id == selectedOrganizationId)
						opt.attr('selected', 'selected')
					opt.appendTo(select)
				}
				orgContainer.append(select)
		    }
		})
	
	$("#editWorkEntryDialog").dialog('open')
}

function deleteWorkEntry(workEntryId) {
	confirmDialog('Are you sure you want to delete this time entry?', function() { 
		$.ajax({ 
			url : ajaxHomePath + '/timeEntry/delete',
			dataType : 'json',
			data : {
				workEntryId: workEntryId
			},
			error : commonAjaxErrorHandler,
			success : refreshTimeReportTable 
		})
	})
}

function postAll(evt) {
	if (!validate())
		return

	if (evt != null)
		doubleClickSafeguard($(evt.currentTarget))
	
	var byVoter = $("#byVoter").is(":checked")
	var byDate = $("#byDate").is(":checked")
	var volMaster = $("#voterMasterId").val()
	var dateMaster = $("#dateMaster").val()
	
	var params = {}
	var curIndex = 0
	var maxDate = null
	
	for (var i = 0; i < timeEntryItems.length; i++) {
		var item = timeEntryItems[i]
		if (!isValidItem(item)) continue
		
		if (byVoter) {
			params['date' + curIndex] = getDateStrFromDate(item.date)
			if (maxDate == null || maxDate < item.date)
				maxDate = item.date
		} else if (byDate) {
			params['date' + curIndex] = dateMaster
			maxDate = dateMaster
		}
		params['assignmentId' + curIndex] = item.selectedAssignmentId
		params['organizationId' + curIndex] = item.selectedOrganizationId
		params['hours' + curIndex] = item.hours
		curIndex++
	}
	params['numEntries'] = curIndex
	
	$.ajax({
		url : ajaxHomePath + "/timeEntry/post",
		data : params,
		success : function(response) {
			clearAndRefreshTimeEntryTable()
			refreshTimeReportTable(maxDate)
		},
		error : commonAjaxErrorHandler
	})
}

function isValidItem(item) {
	var assignments = (item.voterId == null) ? [] : getSiteVoter(item.voterId).assignments
	var organizations = (item.voterId == null) ? [] : getSiteVoter(item.voterId).organizations
	
	var anyInputsEntered = $("#byDate").is(":checked") ? item.voterId != null : item.date != null
	anyInputsEntered |= assignments.length > 1 && item.selectedAssignmentId != null
	anyInputsEntered |= organizations.length > 1 && item.selectedOrganizationId != null
	anyInputsEntered |= item.hours != null
	return anyInputsEntered
}

function clearAllErrors() {
	$(".customFieldError", "#timeEntryList").remove()
	$(".errorField").removeClass('errorField')
	$("#missingFieldsNotice").hide()
}

function validate() {
	var validationPassed = true
	var errorFields = []
	
	var byVoter = $("#byVoter").is(":checked")
	var byDate = $("#byDate").is(":checked")
	
	clearAllErrors()
	
	var hasMissingFields = false
	
	var volMaster = $("#voterMaster").val()
	var volMasterId = $("#voterMasterId").val()
	var dateMaster = $("#dateMaster").val()
	
	if (byVoter && volMasterId == '') {
		errorFields.push("#voterMaster")
		hasMissingFields = true
	} else if (byDate && dateMaster == '') {
		errorFields.push("#dateMaster")
		hasMissingFields = true
	}
	
	var numValidItems = 0
	for (var i = 0; i < timeEntryItems.length; i++) {
		var item = timeEntryItems[i]
		var index = item.index
		
		if (!isValidItem(item))
			continue;
		
		if (byDate && item.voterId == null) {
			errorFields.push("#voterInput" + index)
			hasMissingFields = true
		}
		if (byVoter) {
			if (item.date == null) {
				errorFields.push("#dateInput" + index)
				hasMissingFields = true
			} else {
				var earliestAcceptable = getDateFromYYYYMMDD(iso8601EarliestAcceptableDateEntry)
				var latestAcceptable = new Date()
				if (item.date < earliestAcceptable || item.date > latestAcceptable) {
					errorFields.push("#dateInput" + index)
					$("#dateInput" + index).next().after('<div class="customFieldError" style="max-width:120px; white-space:pre-wrap;"><br>'
							+ ((item.date < earliestAcceptable) ? 'Enter a date within the current fiscal year' :
								'Cannot enter a future date.') + '</div>')
				}
			}
		}
		
		if (item.hours == null) {
			errorFields.push("#hoursInputIndex" + index)
			hasMissingFields = true
		} else if (item.hours < 0.25 || item.hours > 24.0) {
			validationPassed = false
			errorFields.push("#hoursInputIndex" + index)
			$("#hoursInputIndex" + index).after('<span class="customFieldError"><br>Enter a value between 0.25 and 24.0</span>')
		}
		
		if (item.selectedAssignmentId == null) {
			errorFields.push("#assignmentInputIndex" + index)
			hasMissingFields = true
		}
		
		if (item.selectedOrganizationId == null) {
			errorFields.push("#organizationInputIndex" + index)
			hasMissingFields = true
		}
		
		numValidItems++
	}
	
	if (errorFields.length == 0 && numValidItems == 0) {
		errorFields.push(byVoter ? "#dateInput0" : "#voterInput0")
		hasMissingFields = true
	}

	$("#missingFieldsNotice").toggle(hasMissingFields)
	
	if (errorFields.length > 0) {
		validationPassed = false
		for (var i = 0; i < errorFields.length; i++) {
			$(errorFields[i]).addClass('errorField')
		}
		
		$(errorFields[0]).focus()
	}
	
	return validationPassed
}