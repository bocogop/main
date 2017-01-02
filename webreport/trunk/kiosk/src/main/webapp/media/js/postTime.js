/*
 * Array of { index, hours, minutes, selectedAssignmentId, selectedOrganizationId, fixedOrganizationId }
 */
var originalEntries = []
var timeEntryItems = []

var volAssignments = []
var volAssignmentsById = {}

var volOrganizations = []
var volOrganizationsById = {}
var activeVolOrganizations = []

var unmetRequirements = {}

$(function() {
	$("#postAllButton").click(postAll)
	buildTimeEntryTable()
	loadVoterInfo()
	
	$("#viewAllOpportunitiesButton").button('option', 'disabled', true)
	//$("#viewProfileButton").button('option', 'disabled', true)
})

function loadVoterInfo() {
	volAssignments = []
	volAssignmentsById = {}
	volOrganizations = []
	unmetRequirements = {}
	
	$.ajax({
		url : ajaxHomePath + '/postTime/assignmentsAndOrgs',
		dataType : 'json',
		error : commonAjaxErrorHandler,
		success : function(response) {
			volAssignments = response.assignments
			for (var i = 0; i < volAssignments.length; i++) {
				var va = volAssignments[i]
				volAssignmentsById[va.id] = va
			}
			
			volOrganizations = response.organizations
			for (var i = 0; i < volOrganizations.length; i++) {
				var vo = volOrganizations[i]
				volOrganizationsById[vo.id] = vo
				if (vo.active)
					activeVolOrganizations.push(vo)
			}
			
			unmetRequirements = response.unmetRequirements

			originalEntries = response.todayEntries
			clearAndRefreshTimeEntryTable(response.todayEntries)
			
			var ts = response.timeSummary
			$("#yearHours").text(ts.currentYearHours)
			$("#totalHours").text(ts.totalHours)
			
			$("#timeEntryList input").first().focus()
	    }
	})
}

function buildTimeEntryTable() {
	var theTable = $('#timeEntryList')
			.DataTable(
					{
						"columns" : [
								{
									"render" : function(row, type, val, meta) {
										if (type !== 'display') return ''
										
										var el = $('<div></div>').attr('id', 'hoursWrapperIndex' + val.index)
										el.append(getHoursDropdownEl(val.index))
										return el.outerHTML()
									},
									"sortable" : false
								},{
									"render" : function(row, type, val, meta) {
										if (type !== 'display') return ''
										
										var el = $('<div></div>').attr('id', 'minutesWrapperIndex' + val.index)
										el.append(getMinutesDropdownEl(val.index))
										return el.outerHTML()
									},
									"sortable" : false
								},{
									"render" : function(row, type, val, meta) {
										if (type !== 'display') return ''
										
										var el = $('<div></div>').attr('id', 'assignmentWrapperIndex' + val.index)
										// el.append(getAssignmentsDropdownEl(val.index))
										var assn = volAssignmentsById[val.selectedAssignmentId]
										el.append($('<span class="singleAssignmentInput"></span>').text(assn.displayName
												+ ' - ' + assn.locationDisplayName))
										el.append($('<input type="hidden" id="assignmentInputIndex' + val.index + '" value="' + assn.id + '"/>'))
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
								}],
						"data" : timeEntryItems,
						"dom" : '<"top">rt<"bottom"l><"clear">',
						"language": {
							zeroRecords: pleaseWaitText,
							// number.",
							search: "", // Search",
							searchPlaceholder: ""
						},
						"order" : [],
						"paging" : false,
						
						// "stripeClasses" : ['odd'],
					})
	return theTable
}

function getHoursDropdownEl(index) {
	var hours = timeEntryItems[index].hours
	
	var container = $("<div />")
	var select = $('<select class="hoursInput"></select>') //
		.attr('id', 'hoursInputIndex' + index) //
		.attr('index', index)
	
	for (var i = 0; i <= 24; i++) {
		var opt = $("<option />").attr("value", i).text(i)
		if (i == hours)
			opt.attr('selected', 'selected')
		opt.appendTo(select)
	}
	container.append(select)
	
	return container
}

function getMinutesDropdownEl(index) {
	var minutes = timeEntryItems[index].minutes
	
	var container = $("<div />")
	var select = $('<select class="minutesInput"></select>') //
		.attr('id', 'minutesInputIndex' + index) //
		.attr('index', index)
	
	for (var i = 0; i < 4; i++) {
		var opt = $("<option />").attr("value", i * 15).text(i * 15)
		if (i * 15 == minutes)
			opt.attr('selected', 'selected')
		opt.appendTo(select)
	}
	container.append(select)
	
	return container
}

function getAssignmentsDropdownEl(index) {
	if (!volAssignments) return null
	
	var container = $("<div />")
	if (volAssignments.length == 1) {
		var volAssn = volAssignments[0]
		container.append($('<span class="singleAssignmentInput"></span>').text(volAssn.displayName
				+ ' - ' + volAssn.locationDisplayName))
	} else {
		var selectedAssignmentId = timeEntryItems[index].selectedAssignmentId
		var select = $('<select class="assignmentInput"></select>') //
			.attr('id', 'assignmentInputIndex' + index) //
			.attr('index', index)
		
		$("<option />").attr("value", "").text("Please select...").appendTo(select)
		for (var i = 0; i < volAssignments.length; i++) {
			var volAssn = volAssignments[i]
			var opt = $("<option />").attr("value", volAssn.id).text(volAssn.displayName + ' - ' + volAssn.locationDisplayName)
			if (volAssn.id == selectedAssignmentId)
				opt.attr('selected', 'selected')
			opt.appendTo(select)
		}
		container.append(select)
	}
	
	return container
}

function getOrganizationsDropdownEl(index) {
	if (!volOrganizations || index >= timeEntryItems.length) return null
	
	var thisTe = timeEntryItems[index]
	var selectedVolOrganizationId = thisTe.selectedOrganizationId
	
	var availableVolOrgs = []
	if (thisTe.fixedOrganizationId) {
		availableVolOrgs.push(volOrganizationsById[thisTe.fixedOrganizationId])
	} else {
		for (var i = 0; i < volOrganizations.length; i++)
			if (volOrganizations[i].active || volOrganizations[i].id == selectedVolOrganizationId)
				availableVolOrgs.push(volOrganizations[i])
	}
	
	var container = $("<div style='white-space: nowrap;' />")
	var addedOne = false
	
	if (availableVolOrgs.length == 1) {
		var volOrg = availableVolOrgs[0]
		container.append($('<span class="singleOrganizationInput"></span>').text(volOrg.organization.displayName))
		thisTe.selectedOrganizationId = volOrg.id
		addedOne = true
	} else {
		var select = $('<select class="organizationInput"></select>') //
			.attr('id', 'organizationInputIndex' + index) //
			.attr('index', index)
		
		$("<option />").attr("value", "").text("Please select...").appendTo(select)
		for (var i = 0; i < availableVolOrgs.length; i++) {
			var volOrg = availableVolOrgs[i]
			if (volOrg.active || volOrg.id == selectedVolOrganizationId) {
				var opt = $("<option />").attr("value", volOrg.id).text(volOrg.organization.displayName)
				if (volOrg.id == selectedVolOrganizationId)
					opt.attr('selected', 'selected')
				opt.appendTo(select)
				addedOne = true
			}
		}
		container.append(select)
		if (index == timeEntryItems.length - 1 || timeEntryItems[index + 1].selectedAssignmentId != thisTe.selectedAssignmentId)
			container.append('&nbsp;&nbsp;<a href="javascript:split(' + index + ')">Split Time</a>')
	}
	
	return addedOne ? container : null
}

function prepareAssignmentsDropdown($selector) {
	$selector.each(function() {
		$(this).attr('aria-label', 'Select the voter assignment')
		$(this).change(function() {
			var index = $(this).attr('index')
			var v = $(this).val()
			timeEntryItems[index].selectedAssignmentId = (v == '' ? null : v)
		})
	})
}

function prepareOrganizationsDropdown($selector) {
	$selector.each(function() {
		$(this).attr('aria-label', 'Select the organization')
		$(this).change(function() {
			var index = $(this).attr('index')
			var v = $(this).val()
			timeEntryItems[index].selectedOrganizationId = (v == '' ? null : v)
		})
	})
}

function prepareHoursDropdown($selector) {
	$selector.each(function() {
		$(this).attr('aria-label', 'Select the hours')
		$(this).change(function() {
			var index = $(this).attr('index')
			var v = $(this).val()
			timeEntryItems[index].hours = (v == '' ? 0 : v)
		})
	})
}

function prepareMinutesDropdown($selector) {
	$selector.each(function() {
		$(this).attr('aria-label', 'Select the minutes')
		$(this).change(function() {
			var index = $(this).attr('index')
			var v = $(this).val()
			timeEntryItems[index].minutes = (v == '' ? 0 : v)
		})
	})
}

function split(index) {
	var volAssignmentId = timeEntryItems[index].selectedAssignmentId
	
	var replacementItems = []
	
	var usedVolOrgIds = new SortedArray([])
	
	/* Gather all used items into the new split array */
	for (var i = 0; i < timeEntryItems.length; i++) {
		var te = timeEntryItems[i]
		if (te.selectedAssignmentId != volAssignmentId)
			continue;
		
		if (te.selectedOrganizationId != '') {
			if (usedVolOrgIds.search('' + te.selectedOrganizationId) == -1) {
				usedVolOrgIds.insert('' + te.selectedOrganizationId)
				var newTe = jQuery.extend({}, te, {
					fixedOrganizationId : te.selectedOrganizationId
				})
				replacementItems.push(newTe)
			}
		}
		
		te.remove = true
	}
	
	/* Append any unused items to the new split array */
	for (var i = 0; i < activeVolOrganizations.length; i++) {
		var o = activeVolOrganizations[i]
		
		if (usedVolOrgIds.search('' + o.id) == -1) {
			replacementItems.push({
				hours : 0,
				minutes : 0,
				selectedAssignmentId : volAssignmentId,
				selectedOrganizationId : o.id,
				fixedOrganizationId : o.id
			})
		}
	}
	
	/* Add new items to original index location in list */
	timeEntryItems.splice.apply(timeEntryItems, [index, 0].concat(replacementItems))
	
	/* Remove original items flagged for removal */
	for (var i = timeEntryItems.length - 1; i >= 0; i--)
		if (timeEntryItems[i].remove)
			timeEntryItems.splice(i, 1)
	
	/* Reset indices */
	for (var i = 0; i < timeEntryItems.length; i++)
		timeEntryItems[i].index = i
	
	refreshTimeEntryTable()
}

function clearAndRefreshTimeEntryTable(initialWorkEntries) {
	timeEntryItems = []
	
	var unusedAssignments = new SortedArray([])
	for (var i = 0; i < volAssignments.length; i++) {
		if (!volAssignments[i].active)
			continue
		unusedAssignments.insert(volAssignments[i].id)
	}

	for (var i = 0; initialWorkEntries && i < initialWorkEntries.length; i++) {
		var we = initialWorkEntries[i]
		
		var selectedOrganizationId = null
		for (var j = 0; j < volOrganizations.length; j++)
			if (volOrganizations[j].organization.id == we.organization.id) {
				selectedOrganizationId = volOrganizations[j].id
				break;
			}
		
		timeEntryItems.push({
			index : i,
			hours : we.fullHours,
			minutes : we.fullMinutes,
			selectedAssignmentId : we.voterAssignment.id,
			selectedOrganizationId : selectedOrganizationId,
			fixedOrganizationId : null
		})
		unusedAssignments.remove(we.voterAssignment.id)
	}
	
	for (var i = 0; i < unusedAssignments.array.length; i++) {
		timeEntryItems.push({
			index : timeEntryItems.length,
			hours : 0,
			minutes : 0,
			selectedAssignmentId : unusedAssignments.array[i],
			selectedOrganizationId : (primaryVolOrganizationId == -1 ? null : primaryVolOrganizationId),
			fixedOrganizationId : null
		})
	}
		
		/*addBlankTimeEntryItem()
		while (timeEntryItems.length < 4)
			addBlankTimeEntryItem()
		*/
	
	clearAllErrors()
	refreshTimeEntryTable()
}

function refreshTimeEntryTable() {
	var hasGlobalOrFacilityUnmetRequirements = (unmetRequirements.globalAll.length > 0 || unmetRequirements.facilityAll.length > 0)
	var hasAssignments = (timeEntryItems.length > 0)
	var hasOrgs = activeVolOrganizations.length > 0 || (hasAssignments && timeEntryItems[0].selectedOrganizationId != null)
	
	if (hasGlobalOrFacilityUnmetRequirements) {
		$("#globalOrFacilityUnmetRequirements").empty()
		$.each(unmetRequirements.globalAll, function(index, item) {
			$("#globalOrFacilityUnmetRequirements").append($("<li></li>").text(item.requirement.name))
		})
		$.each(unmetRequirements.facilityAll, function(index, item) {
			$("#globalOrFacilityUnmetRequirements").append($("<li></li>").text(item.requirement.name))
		})
	}
	$("#timeEntryUnmetRequirements").toggle(hasGlobalOrFacilityUnmetRequirements)
	$("#timeEntryNoAssignments").toggle(!hasGlobalOrFacilityUnmetRequirements && !hasAssignments)
	$("#timeEntryNoOrganizations").toggle(!hasGlobalOrFacilityUnmetRequirements && hasAssignments && !hasOrgs)
	$("#timeEntryWrapper").toggle(!hasGlobalOrFacilityUnmetRequirements && hasAssignments && hasOrgs)
	
	if (!hasGlobalOrFacilityUnmetRequirements && hasAssignments && hasOrgs) {
		var table = $("#timeEntryList").DataTable()
		table.clear().rows.add(timeEntryItems).draw()
		prepareHoursDropdown($(".hoursInput", "#timeEntryList"))
		prepareMinutesDropdown($(".minutesInput", "#timeEntryList"))
		prepareAssignmentsDropdown($(".assignmentInput", "#timeEntryList"))
		prepareOrganizationsDropdown($(".organizationInput", "#timeEntryList"))
		
		var disabledAssignmentIds = new SortedArray([])
		table.rows().every(function(rowIndex, tableLoop, rowLoop) {
			var data = this.data()
			var failingReqs = unmetRequirements.byAssignment[data.selectedAssignmentId]
			if (failingReqs) {
				$("select", this.node()).prop('disabled', true)
				$("a", this.node()).remove()
				if (disabledAssignmentIds.search(data.selectedAssignmentId) != -1) {
					// we've already notified the user about this assignment, don't do it a second time
					return
				}
				disabledAssignmentIds.insert(data.selectedAssignmentId)
				
				var childRow = $('<tr><td></td></tr>')
				var td = $('<td colspan="3"><span class="redText" style="text-style:italic">Sorry, this assignment is unavailable due to unmet requirements. Please see VAVS staff.</span></td>').appendTo(childRow)
				var ul = $('<ul class="redText"></ul>').appendTo(td)
				$.each(failingReqs, function(index, item) {
					ul.append($("<li></li>").text(item.requirement.name))
				})
				this.child(childRow).show()
			}
		})
	}
}

function addBlankTimeEntryItem() {
	timeEntryItems.push({
		index : timeEntryItems.length,
		hours : 0,
		minutes : 0,
		selectedAssignmentId : null,
		selectedOrganizationId : null
	})
}

function postAll(evt) {
	if (!validate())
		return

	if (evt != null)
		doubleClickSafeguard($(evt.currentTarget))
	
	var params = {}
	var curIndex = 0
	var maxDate = null
	
	for (var i = 0; i < timeEntryItems.length; i++) {
		var item = timeEntryItems[i]
		if (!isValidItem(item)) continue;
		
		params['hours' + curIndex] = item.hours
		params['minutes' + curIndex] = item.minutes
		params['assignmentId' + curIndex] = item.selectedAssignmentId
		params['organizationId' + curIndex] = item.selectedOrganizationId
		
		curIndex++
	}
	params['numEntries'] = curIndex
	
	$.ajax({
		url : ajaxHomePath + "/postTime/submit",
		data : params,
		success : function(response) {
			document.location.href = homePath + '/mealTicketPrint.htm'
			showSpinner('', true)
		},
		error : commonAjaxErrorHandler
	})
}

function isValidItem(item) {
	var anyInputsEntered = false
	anyInputsEntered |= ((item.hours && item.hours > 0) || (item.minutes && item.minutes > 0))
	// anyInputsEntered |= assignments.length > 1 && item.selectedAssignmentId != null
	// anyInputsEntered |= volOrganizations.length > 1 && item.selectedOrganizationId != null
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
	
	clearAllErrors()
	
	var hasMissingFields = false
	
	var numValidItems = 0
	var validItems = []
	
	for (var i = 0; i < timeEntryItems.length; i++) {
		var item = timeEntryItems[i]
		var index = item.index
		
		if (!isValidItem(item))
			continue;
		
		validItems.push(item)
		
		if (item.hours == 24 && item.minutes != 0) {
			errorFields.push("#minutesInputIndex" + index)
			$("#minutesInputIndex" + index).after('<span class="customFieldError"><br>' + greaterThan24HoursErrorText + '</span>')
		}
		
		if (item.hours == 0 && item.minutes == 0) {
			errorFields.push("#hoursInputIndex" + index)
			$("#hoursInputIndex" + index).after('<span class="customFieldError"><br>' + greaterThan0MinutesText + '</span>')
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
	
	if (errorFields.length == 0 && numValidItems == 0 && originalEntries.length == 0) {
		errorFields.push("#hoursInputIndex0")
		hasMissingFields = true
	}

	/* Ensure unique valid items*/
	outer: //
	for (var i = 0; i < validItems.length; i++) {
		var item1 = validItems[i]
		for (var j = i+1; j < validItems.length; j++) {
			var item2 = validItems[j]
			if (item1.selectedAssignmentId && item1.selectedAssignmentId == item2.selectedAssignmentId
					&& item1.selectedOrganizationId && item1.selectedOrganizationId == item2.selectedOrganizationId) {
				errorFields.push("#assignmentInputIndex" + item2.index)
				$("#assignmentInputIndex" + item2.index).after('<span class="customFieldError"><br>' + uniqueAssignmentsAndOrgsText + '</span>')
				break outer;
			}
		}
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