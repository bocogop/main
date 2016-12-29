var siteOrganizations = []
var siteOrganizationsById = {}
var siteBenefitingServiceRoles = []
var siteBenefitingServiceRolesById = {}

/*
 * Array of { index, date, hours, numberInGroup, organizationId, benefitingServiceRoleId, comments }
 * (selectedLocationId = -1 for main facility)
 */
var occasionalTimeEntryItems = []
var occasionalTimeReportItems

$(function() {
	
	// ---------------------------------- build screen components
	
	$("#postAllButton").click(postAll)
	
	buildOccasionalTimeEntryTable()
	buildOccasionalTimeReportTable()
	occasionalWorkEntryPopupInit()
	prepareDateMaster()
	
	// ---------------------------------- init screen values
	
	var nowDateStr = getDateStrFromDate(new Date())
	for (var i = 0; i < 1; i++) {
		occasionalTimeEntryItems.push({
			index : i,
			date : nowDateStr,
			hours : null,
			numberInGroup : null,
			organizationId : null,
			benefitingServiceRoleId : null,
			comments : null
		})
	}
	
	if (dateRequested == null)
		$("#dateMaster").val(nowDateStr)
	
	clearAndRefreshOccasionalTimeEntryTable()
	refreshOccasionalTimeReportTable()
	
	preloadOrganizations()
	preloadBenefitingServiceRoles()
	
	$("#occasionalTimeEntryParentWrapper").toggle(!isReadOnly)
})

function preloadOrganizations() {
	$.ajax({
		url : ajaxHomePath + "/organization/quickSearch/currentFacility",
		success : function(quickSearchResponse) {
			$(quickSearchResponse.organizations).each(function(index, item) {
				siteOrganizations.push(item)
				siteOrganizationsById[item.id] = item
			})
		},
		error : commonAjaxErrorHandler
	})
}

function preloadBenefitingServiceRoles() {
	$.ajax({
		url : ajaxHomePath + "/benefitingServiceRole/quickSearch/currentFacility",
		success : function(quickSearchResponse) {
			$(quickSearchResponse.benefitingServiceRoles).each(function(index, item) {
				siteBenefitingServiceRoles.push(item)
				siteBenefitingServiceRolesById[item.id] = item
			})
		},
		error : commonAjaxErrorHandler
	})
}

function prepareDateMaster() {
	var changeFunc = function(el) {
		if (!validateDate(el.val())) return
		
		var theDate = getDateFromMMDDYYYY(el.val())
		if (!theDate)
			return
		
		var earliestPossible = getDateFromYYYYMMDD(iso8601EarliestAcceptableDateEntry)
		var latestPossible = new Date()
		var acceptable = theDate >= earliestPossible && theDate <= latestPossible
		$(".occasionalTimeEntryWrapper").toggle(acceptable)
		$("#occasionalTimeEntryForbidden").toggle(!acceptable)
		if (!acceptable) {
			$("#occasionalTimeEntryForbidden").text(theDate < earliestPossible ?
					"Occasional time entries can only be added for the current fiscal year." :
						"Occasional time entries cannot be future dated.")
		}
		
		if (acceptable)
			refreshOccasionalTimeEntryTable()
	}
	
	prepareDateInput(occasionalTimeEntryItems, $("#dateMaster"), function(dateText, el) {
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

function occasionalWorkEntryPopupInit() {
	prepareDateInput(occasionalTimeEntryItems, $("#editOccasionalWorkEntryDate"), function(dateText, el) {
		// var index = el.input.attr('index')
		// occasionalTimeEntryItems[index].date = getDateFromMMDDYYYY(dateText)
	})
	prepareHoursInput(occasionalTimeEntryItems, $("#editOccasionalWorkEntryHours"), false)
	
	var submitEditOccasionalWorkEntry = function() {
		var date = $("#editOccasionalWorkEntryDate").val()
		var hours = $("#editOccasionalWorkEntryHours").val()
		var numberInGroup = $("#editOccasionalWorkEntryNumberInGroup").val()
		var organizationId = $("#editOccasionalWorkEntryOrganizationId").val()
		var benefitingServiceRoleId = $("#editOccasionalWorkEntryBenefitingServiceRoleId").val()
		var comments = $("#editOccasionalWorkEntryComments").val()
		
		var errors = new Array()
		if (date == '')
			errors.push('Please enter the date.')
		
		if ($.trim(hours) == '') {
			errors.push('Please enter the hours.')
		} else if (hours < 0.25 || hours > 9999.75) {
			errors.push('Please enter a time value between 0.25 and 9999.75 hours.')
		}
		if (numberInGroup == '')
			errors.push('Please enter the number in the group.')
		if (organizationId == '')
			errors.push('Please select an organization.')
		if (benefitingServiceRoleId == '')
			errors.push('Please select an assignment.')
			
		if (errors.length > 0) {
			displayAttentionDialog("Please correct the following errors: <ul><li>"
					+ errors.join("</li><li>") + "</li></ul>");
			return
		}
		
		$.ajax({
			url : ajaxHomePath + '/occasionalTimeEntry/update',
			method: 'POST',
			dataType : 'json',
			data : {
				id : $("#editOccasionalWorkEntryDialog").data('occasionalWorkEntryId'),
				date : date,
				hours : hours,
				numberInGroup : numberInGroup,
				organizationId : organizationId,
				benefitingServiceRoleId : benefitingServiceRoleId,
				comments : comments
			},
			error : commonAjaxErrorHandler,
			success : function(response) {
				$("#editOccasionalWorkEntryDialog").dialog('close')
				refreshOccasionalTimeReportTable()
		    }
		})
	}
	
	var dialogEl = $("#editOccasionalWorkEntryDialog")
	dialogEl.dialog({
		autoOpen : false,
		modal : false,
		width : 600,
		height : 270,
		closeOnEscape : true,
		draggable : true,
		resizable : true,
		buttons : {
			'Submit' : {
				text : 'Submit',
				id : 'editOccasionalWorkEntrySubmit',
				click : function() {
					doubleClickSafeguard($("#editOccasionalWorkEntrySubmit"))
					submitEditOccasionalWorkEntry()
				}
			},
			'Cancel' : function() {
				$(this).dialog('close')
			}
		}
	})
	
	prepareOrganizationInput($("#editOccasionalWorkEntryOrganization"), function(mainEl) {
			return $("#editOccasionalWorkEntryOrganizationId")
		},{
			activeOnly : false
		})
	prepareBenefitingServiceRoleInput($("#editOccasionalWorkEntryBenefitingServiceRole"), function() {
			return $("#editOccasionalWorkEntryBenefitingServiceRoleId")
		},{
			activeOnly : false
		})
	$("#editOccasionalWorkEntryDialog").show()
}

function buildOccasionalTimeEntryTable() {
	var theTable = $('#occasionalTimeEntryList')
			.DataTable(
					{
						"columns" : [
								{
									"render" : function(row, type, val, meta) {
										if (type !== 'display') return ''
										
										return $('<input type="text" size="6" maxlength="5" class="numberInGroupInput" />') //
											.attr('index', val.index) //
											.attr('id', 'numberInGroupInputId' + val.index) //
											.attr('value', val.numberInGroup || '') //
											.outerHTML()
									},
									"sortable" : false
								},{
									"render" : function(row, type, val, meta) {
										if (type !== 'display') return ''
										
										return $('<input type="text" size="8" maxlength="7" class="hoursInput" />') //
											.attr('index', val.index) //
											.attr('id', 'hoursInputId' + val.index) //
											.attr('value', val.hours || '') //
											.outerHTML()
									},
									"sortable" : false
								},{
									"render" : function(row, type, val, meta) {
										var orgInput = $('<input type="text" size="25" class="organizationInput" />') //
											.attr('index', val.index) //
											.attr('id', 'organizationInput' + val.index)
											orgInput.attr('value', val.organizationId ? (buildOrganizationName(val.organizationId) || '') : '')
										var orgInputId = $('<input type="hidden" class="organizationInputId" />') //
											.attr('id', 'organizationInputId' + val.index) //
											.attr('value', val.organizationId || '')
										return $('<div></div>').append(orgInput).append(orgInputId).outerHTML()
									},
									"sortable" : false
								},{
									"render" : function(row, type, val, meta) {
										var bsrInput = $('<input type="text" size="40" class="benefitingServiceRoleInput" />') //
											.attr('index', val.index) //
											.attr('id', 'benefitingServiceRoleInput' + val.index)
										if (val.benefitingServiceRoleId)
											bsrInput.attr('value', buildServiceName(val.benefitingServiceRoleId) || '')
										var bsrInputId = $('<input type="hidden" class="benefitingServiceRoleInputId" />') //
											.attr('id', 'benefitingServiceRoleInputId' + val.index) //
											.attr('value', val.benefitingServiceRoleId || '')
										return $('<div></div>').append(bsrInput).append(bsrInputId).outerHTML()
									},
									"sortable" : false
								},{
									"render" : function(row, type, val, meta) {
										var orgInput = $('<input type="text" size="20" maxlength="40" class="commentsInput" />') //
											.attr('index', val.index) //
											.attr('id', 'commentsInput' + val.index)
											.attr('value', val.comments || '')
										return orgInput.outerHTML()
									},
									"sortable" : false
								},{
									"render" : function(row, type, val, meta) {
										if (type !== 'display') return ''
										if (isReadOnly) return ''
										
										var results = ''
										if (occasionalTimeEntryItems.length > 1) {
											results += '<a class="rowDeleteIcon" index="' + val.index + '" href="javascript:deleteInputRow(' + val.index + ')">'
												+ '<img src="' + imgHomePath + '/cross.png" border="0" align="absmiddle" alt="Delete Row"></a>'
										}
										if (meta.row == occasionalTimeEntryItems.length - 1)
											results += '<a style="margin-left:15px" href="javascript:addInputRow()"><img src="'
												+ imgHomePath + '/add.png" border="0" align="absmiddle" alt="Add New Row"></a>'
										return '<nobr>' + results + '</nobr>'
									},
									"sortable" : false
								}
								],
						"data" : occasionalTimeEntryItems,
						"dom" : '<"top">rt<"bottom"l><"clear">',
						"order" : [],
						"paging" : false
					})
	return theTable
}

function buildOccasionalTimeReportTable() {
	var theTable = $('#occasionalTimeReportList')
			.DataTable(
					{
						buttons : [{
							extend : 'excel',
							exportOptions: {
								columns : [0,1,2,3,4,5,6,7]
							}
						}, {
							extend : 'pdfHtml5',
							orientation : 'landscape',
							exportOptions: {
								columns : [0,1,2,3,4,5,6,7]
							}
						}, {
							extend : 'print',
							exportOptions: {
								columns : [0,1,2,3,4,5,6,7]
							}
						} ],
						"columns" : [
								{
									"render" : function(row, type, val, meta) {
										if (type === 'filter') {
											var d = getDateFromMMDDYYYY(val.dateWorked)
											return pad((d.getMonth() + 1), 2) + '/' + d.getFullYear()
										}
										return val.dateWorked
									}
								},{
									"render" : function(row, type, val, meta) {
										return '' + val.numberInGroup
									}
								},
								{
									"render" : function(row, type, val, meta) {
										return '' + val.hoursWorked
									}
								},
								{
									"render" : function(row, type, val, meta) {
										return val.organization ? val.organization.displayName : ''
									}
								},
								{
									"render" : function(row, type, val, meta) {
										return val.benefitingService ? val.benefitingService.name : ''
									}
								},
								{
									"render" : function(row, type, val, meta) {
										return val.benefitingServiceRole ? val.benefitingServiceRole.name : ''
									}
								},
								{
									"render" : function(row, type, val, meta) {
										return val.locationDisplayName
									}
								},
								{
									"render" : function(row, type, val, meta) {
										return val.comments
									}
								},
								{
									"render" : function(row, type, val, meta) {
										var actions = '<div style="margin:0 auto; text-align:center"><nobr>'
										var dateMaster = getDateFromMMDDYYYY($("#dateMaster").val())
										var rowDate = getDateFromMMDDYYYY(val.dateWorked)
										
										var showButtons
										if (dateMaster != null) {
											showButtons = dateMaster >= getDateFromYYYYMMDD(iso8601EarliestAcceptableDateEntry)
										} else if (rowDate != null) {
											showButtons = rowDate >= getDateFromYYYYMMDD(iso8601EarliestAcceptableDateEntry)
										}
										
										if (val.organization.active !== true)
											showButtons = false
										if (isReadOnly) showButtons = false
										
										if (showButtons) {
											actions += '<a href="javascript:occasionalWorkEntryEdit('
												+ val.id + ')"><img src="'+ imgHomePath
												+ '/edit-small.gif" alt="Edit Occasional Time Entry" border="0" hspace="5" align="center"/></a>'
											actions += '<a href="javascript:deleteOccasionalWorkEntry('
													+ val.id
													+ ')"><img src="'+ imgHomePath
													+ '/permanently_delete_18x18.png" border="0" hspace="5" align="center" alt="Delete Occasional Time Entry" /></a>'
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

function getAutocompleteSource(collection, searchFieldFunc, activeOnly) {
	return function(request, response) {
		var theVal = $.trim(request.term).toLowerCase()
		if (theVal == '')
			return
	
		var matches = new Array()
		var tokens = theVal.match(/[A-Za-z0-9'&]+/g);
	
		outer: for (var i = 0; i < collection.length && matches.length <= 10; i++) {
			var v = collection[i]
	
			if (activeOnly && !v.active)
				continue;
			
			for (var t = 0; t < tokens.length; t++) {
				var token = tokens[t]
				
				if (searchFieldFunc(v.id).toLowerCase().indexOf(token) == -1)
					continue outer;
			}
			
			matches.push({
				label : searchFieldFunc(v.id),
				value : searchFieldFunc(v.id),
				id : v.id
			})
		}
	
		response(matches)
	}
}

function prepareOrganizationInput($selector, getIdElementFunc, options) {
	var finalOpts = $.extend({}, {
		selectedCallback : null,
		elementClearedCallback : null,
		addlLabelText : '',
		activeOnly : true
	}, options)
	
	$selector.each(function() {
		$(this).attr('aria-label', 'Type some characters to search matching active organizations and branches' + finalOpts.addlLabelText)
		bindAlt1Keydown($(this))

		var that = $(this)
		
		$(this).autocomplete({
			minLength : 1,
			select: function(event, ui) {
				var orgId = ui.item.id
				var org = siteOrganizationsById[orgId]
				
				/*
				 * workaround for this function not setting the value quickly enough before the blur()
				 * below is activated - CPB
				 */ 
				that.val(buildOrganizationName(orgId))
				getIdElementFunc($(this)).val(orgId)
				
				if (finalOpts.selectedCallback)
					finalOpts.selectedCallback($(event.target), orgId)
			},
			source :  getAutocompleteSource(siteOrganizations, buildOrganizationName, finalOpts.activeOnly)
		})
		
		$(this).blur(function() {
			var orgIdEl = getIdElementFunc($(this))
			var orgId = orgIdEl.val()
			
			if (orgId == '' || $(this).val() != buildOrganizationName(orgId)) {
				orgIdEl.val('')
				$(this).val('')
				if (finalOpts.elementClearedCallback)
					finalOpts.elementClearedCallback($(this))
			}
		})
	})
}

function buildServiceName(vId) {
	if (!vId) return ''
	var v = siteBenefitingServiceRolesById[vId]
	return v.serviceName + ($.trim(v.serviceSubdivision) != '' ? ' - ' + v.serviceSubdivision : '') + ' - '
		+ v.name + ' @' + (v.locationName ? v.locationName : 'Main Facility') + (!v.active ? ' (inactive)' : '')
}

function buildOrganizationName(oId) {
	if (!oId) return ''
	var o = siteOrganizationsById[oId]
	return o.name + (!o.active ? ' (inactive)' : '')
}

function prepareCommentsInput($selector, inWorksheet, options) {
	var finalOpts = $.extend({}, {
		addlLabelText : ''
	}, options)
	
	$selector.each(function() {
		$(this).attr('aria-label', 'Enter the comments' + finalOpts.addlLabelText)
		bindAlt1Keydown($(this))
		
		$(this).blur(function() {
			var v = $(this).val()
			var index = $(this).attr('index')
			occasionalTimeEntryItems[index].comments = ($.trim(v) != '' ? v : null)
		})
	})
}

function prepareBenefitingServiceRoleInput($selector, getIdElementFunc, options) {
	var finalOpts = $.extend({}, {
		selectedCallback : null,
		elementClearedCallback : null,
		addlLabelText : '',
		activeOnly : true
	}, options)
	
	$selector.each(function() {
		$(this).attr('aria-label', 'Type some characters to search matching active benefiting service roles' + finalOpts.addlLabelText)
		bindAlt1Keydown($(this))
		
		var that = $(this)
		
		$(this).autocomplete({
			minLength : 1,
			select: function(event, ui) {
				var bsrId = ui.item.id
				var bsr = siteBenefitingServiceRolesById[bsrId]
				
				that.val(buildServiceName(bsrId))
				getIdElementFunc($(this)).val(bsrId)
				
				if (finalOpts.selectedCallback)
					finalOpts.selectedCallback($(event.target), bsrId)
			},
			source :  getAutocompleteSource(siteBenefitingServiceRoles, buildServiceName, finalOpts.activeOnly)
		})
		
		$(this).blur(function() {
			var bsrIdEl = getIdElementFunc($(this))
			var bsrId = bsrIdEl.val()
			
			if (bsrId == '' || $(this).val() != buildServiceName(bsrId)) {
				bsrIdEl.val('')
				$(this).val('')
				if (finalOpts.elementClearedCallback)
					finalOpts.elementClearedCallback($(this))
			}
		})
	})
}

function prepareNumberInGroupInput($selector) {
	$selector.each(function() {
		$(this).attr('aria-label', 'Enter the number in the group, or push Alt one to exit the worksheet')
		bindAlt1Keydown($(this))
		
		$(this).blur(function() {
			var v = $(this).val().replace(/[^0-9]/g, '')
			$(this).val(v)
			var index = $(this).attr('index')
			occasionalTimeEntryItems[index].numberInGroup = ($.trim(v) != '' ? v : null)
		})
	})
}

function clearAndRefreshOccasionalTimeEntryTable() {
	var numItems = occasionalTimeEntryItems.length
	occasionalTimeEntryItems.length = 0
	for (var i = 0; i < numItems; i++)
		addBlankOccasionalTimeEntryItem()
	clearAllErrors()
	refreshOccasionalTimeEntryTable()
	$("#occasionalTimeEntryList input").first().focus()
}

function refreshOccasionalTimeEntryTable() {
	var table = $("#occasionalTimeEntryList").DataTable()
	table.clear().rows.add(occasionalTimeEntryItems).draw()
	prepareHoursInput(occasionalTimeEntryItems, $(".hoursInput", "#occasionalTimeEntryList"), true)
	prepareDateInput(occasionalTimeEntryItems, $(".dateInput", "#occasionalTimeEntryList"), function(dateText, el) {
		var index = el.input.attr('index')
		occasionalTimeEntryItems[index].date = getDateFromMMDDYYYY(dateText)
	})
	prepareNumberInGroupInput($(".numberInGroupInput", "#occasionalTimeEntryList"))
	prepareOrganizationInput($(".organizationInput", "#occasionalTimeEntryList"),
		function(mainEl) {
			return $("#organizationInputId" + mainEl.attr('index'))
		}, {
			selectedCallback : function(mainEl, orgId) {
				occasionalTimeEntryItems[mainEl.attr('index')].organizationId = orgId
				// $("#benefitingServiceRoleInput" + mainEl.attr('index')).focus()
			},
			elementClearedCallback : function(mainEl) {
				occasionalTimeEntryItems[mainEl.attr('index')].organizationId = null
			},
			addlLabelText : ', or push Alt one to exit the worksheet'
		}
	)
	prepareBenefitingServiceRoleInput($(".benefitingServiceRoleInput", "#occasionalTimeEntryList"),
		function(mainEl) {
			return $("#benefitingServiceRoleInputId" + mainEl.attr('index'))
		}, {
			selectedCallback : function(mainEl, bsrId) {
				occasionalTimeEntryItems[mainEl.attr('index')].benefitingServiceRoleId = bsrId
			},
			elementClearedCallback : function(mainEl) {
				occasionalTimeEntryItems[mainEl.attr('index')].benefitingServiceRoleId = null
			},
			addlLabelText : ', or push Alt one to exit the worksheet'
		})
	prepareCommentsInput($(".commentsInput", "#occasionalTimeEntryList"), {
		addlLabelText : ', or push Alt one to exit the worksheet'
	})
	prepareRowDeleteIcon($(".rowDeleteIcon", "#occasionalTimeEntryList"))
	rebindAutoAddRowFn("#occasionalTimeEntryList")
}

function refreshOccasionalTimeReportTable(pageToRowWithDate) {
	$("#occasionalTimeReportLegend").text('Occasional Time Report')
	var occasionalTimeReportList = $("#occasionalTimeReportList").DataTable()
	
	occasionalTimeReportItems = new Object()
	$.ajax({
		url : ajaxHomePath + '/occasionalTimeEntry/timeReportByDate',
		dataType : 'json',
		error : commonAjaxErrorHandler,
		success : function(response) {
			for (var i = 0; i < response.length; i++)
				occasionalTimeReportItems[response[i].id] = response[i]
			
			occasionalTimeReportList.clear()
			occasionalTimeReportList.rows.add(response)
			occasionalTimeReportList.draw()
			rebuildTableFilters('occasionalTimeReportList')
			if (pageToRowWithDate) {
				pageToFirstMatchingRow(occasionalTimeReportList, function (idx, data, node) {
			        return getDateFromMMDDYYYY(data.dateWorked) <= pageToRowWithDate
			    })
			}
	    }
	})
}

function addBlankOccasionalTimeEntryItem() {
	var dateMaster = $("#dateMaster").val()
	
	occasionalTimeEntryItems.push({
		index : occasionalTimeEntryItems.length,
		date : dateMaster != '' ? getDateFromMMDDYYYY($("#dateMaster").val()) : null,
		hours : null,
		organizationId : null,
		benefitingServiceRoleId : null,
	})
}

function addInputRow() {
	addBlankOccasionalTimeEntryItem()
	refreshOccasionalTimeEntryTable()
	$("#occasionalTimeEntryList tr:last-child td:first-child:visible input").focus()
}

function deleteInputRow(index) {
	occasionalTimeEntryItems.splice(index, 1)
	for (var i = index; i < occasionalTimeEntryItems.length; i++)
		occasionalTimeEntryItems[i].index--
	refreshOccasionalTimeEntryTable()
	$("#occasionalTimeEntryList input[value='']:visible:first").focus()
}

function occasionalWorkEntryEdit(occasionalWorkEntryId) {
	var occasionalWorkEntry = occasionalTimeReportItems[occasionalWorkEntryId]
	$("#editOccasionalWorkEntryDialog").data('occasionalWorkEntryId', occasionalWorkEntryId)
	
	$("#editOccasionalWorkEntryDate").val(occasionalWorkEntry.dateWorked)
	$("#editOccasionalWorkEntryNumberInGroup").val(occasionalWorkEntry.numberInGroup)
	var paddedHours = getPaddedHoursStr(occasionalWorkEntry.hoursWorked)
	$("#editOccasionalWorkEntryHours").val(paddedHours)
	
	$("#editOccasionalWorkEntryOrganization").val(occasionalWorkEntry.organization.displayName)
	$("#editOccasionalWorkEntryOrganizationId").val(occasionalWorkEntry.organization.id)
	
	$("#editOccasionalWorkEntryBenefitingServiceRole").val(buildServiceName(occasionalWorkEntry.benefitingServiceRole.id))
	$("#editOccasionalWorkEntryBenefitingServiceRoleId").val(occasionalWorkEntry.benefitingServiceRole.id)
	
	$("#editOccasionalWorkEntryComments").val(occasionalWorkEntry.comments)
	
	// build autocompletes here for popup dialog
	$("#editOccasionalWorkEntryDialog").dialog('open')
}

function deleteOccasionalWorkEntry(occasionalWorkEntryId) {
	confirmDialog('Are you sure you want to delete this occasional time entry?', function() { 
		$.ajax({ 
			url : ajaxHomePath + '/occasionalTimeEntry/delete',
			dataType : 'json',
			data : {
				occasionalWorkEntryId: occasionalWorkEntryId
			},
			error : commonAjaxErrorHandler,
			success : refreshOccasionalTimeReportTable 
		})
	})
}

function postAll(evt) {
	if (!validate())
		return

	if (evt != null)
		doubleClickSafeguard($(evt.currentTarget))
	
	var dateMaster = $("#dateMaster").val()
	
	var params = {}
	var curIndex = 0
	for (var i = 0; i < occasionalTimeEntryItems.length; i++) {
		var item = occasionalTimeEntryItems[i]
		if (!isValidItem(item)) continue;
		
		params['date' + curIndex] = dateMaster
		params['numberInGroup' + curIndex] = item.numberInGroup
		params['hours' + curIndex] = item.hours
		params['organizationId' + curIndex] = item.organizationId
		params['benefitingServiceRoleId' + curIndex] = item.benefitingServiceRoleId
		params['comments' + curIndex] = item.comments
		curIndex++
	}
	params['numEntries'] = curIndex
	
	$.ajax({
		url : ajaxHomePath + "/occasionalTimeEntry/post",
		data : params,
		success : function(response) {
			clearAndRefreshOccasionalTimeEntryTable()
			refreshOccasionalTimeReportTable(getDateFromMMDDYYYY(dateMaster))
		},
		error : commonAjaxErrorHandler
	})
}

function isValidItem(item) {
	var anyInputsEntered = false
	anyInputsEntered |= item.benefitingServiceRoleId != null
	anyInputsEntered |= item.organizationId != null
	anyInputsEntered |= item.hours != null
	anyInputsEntered |= item.numberInGroup != null
	return anyInputsEntered
}

function clearAllErrors() {
	$(".customFieldError", "#occasionalTimeEntryList").remove()
	$(".errorField").removeClass('errorField')
	$("#missingFieldsNotice").hide()
}

function validate() {
	var validationPassed = true
	var errorFields = []
	
	clearAllErrors()
	var hasMissingFields = false
	var dateMaster = $("#dateMaster").val()
	
	if (dateMaster == '') {
		errorFields.push("#dateMaster")
		hasMissingFields = true
	}
	
	var numValidItems = 0
	for (var i = 0; i < occasionalTimeEntryItems.length; i++) {
		var item = occasionalTimeEntryItems[i]
		var index = item.index
		
		if (!isValidItem(item))
			continue;
		
		if (item.numberInGroup == null) {
			errorFields.push("#numberInGroupInputId" + index)
			hasMissingFields = true
		}
		
		if (item.hours == null) {
			errorFields.push("#hoursInputId" + index)
			hasMissingFields = true
		} else if (item.hours < 0.25 || item.hours > 9999.75) {
			validationPassed = false
			errorFields.push("#hoursInputId" + index)
			$("#hoursInputId" + index).after('<span class="customFieldError"><br>Enter a value between 0.25 and 9999.75</span>')
		}
		
		if (item.organizationId == null) {
			errorFields.push("#organizationInput" + index)
			hasMissingFields = true
		}
		
		if (item.benefitingServiceRoleId == null) {
			errorFields.push("#benefitingServiceRoleInput" + index)
			hasMissingFields = true
		}
		
		numValidItems++
	}
	
	if (errorFields.length == 0 && numValidItems == 0) {
		errorFields.push("#numberInGroupInputId0")
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