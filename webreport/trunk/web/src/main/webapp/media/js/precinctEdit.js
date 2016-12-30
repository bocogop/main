var precinctId = null
var canEditAllPrecincts = null
var numMealsUpdated = false
var donGenPostFundList = {}
var donationRefList = {}

function onPageLoad(precinctSelected, createNew, editPrecinctId, precinctSet, editAllPrecincts) {
	precinctId = editPrecinctId
	canEditAllPrecincts = editAllPrecincts
	
	if (canEditAllPrecincts) {
		$("#precinctHierarchyDiv").show()
		buildPrecinctListTable()
		refreshPrecinctListTable()
		filterPrecinctListTable()
	} else{
		$("#precinctName").prop('disabled', true)
		$("#precinctParentSelect").prop('disabled', true)
		$("#precinctVISN").prop('disabled', true)
		$("#precinctActive").prop('disabled', true)
	}
	
	buildLocationListTable()
	if (precinctSelected && !createNew)
		refreshLocations()
		
	buildKioskListTable()
	if (precinctSelected && !createNew)
		refreshKiosks()
	
	buildDonGenPostFundListTable()
	refreshDonGenPostFundList()
	buildDonReferenceListTable()
	refreshDonReferenceList()
	
	buildPrecinctPopup()
	buildLocationPopup()
	buildKioskPopup()
	buildDonGenPostFundPopup()
	buildDonReferencePopup()
	
	$("#precinctParentSelect").multiselect({
		selectedText : function(numChecked, numTotal, checkedItems) {
			return $(checkedItems[0]).next().text() // abbreviate($(checkedItems[0]).next().text())
		},
		height: 300,
		minWidth: 250,
		multiple : false
	}).multiselectfilter()
	
	$(".precinctControlledInput").toggle(!precinctSet)
	$(".precinctControlledValue").toggle(precinctSet)
	$(".persistentPrecinctOnlyFields").toggle(!createNew)
	
	$("input[name='precinct.stationParameters.numberOfMeals']:radio").on('change', function() {
		toggleTheMealFields($(this).val());
		commandNumMeals = $(this).val();
		numMealsUpdated = true;
	})
	if(!numMealsUpdated)
		toggleTheMealFields(commandNumMeals)
		
	var roundMealHours = function() {
		 if($(this).val().length > 0 && !isNaN($(this).val())) { 
			var x = Math.round(($(this).val() * 100) / 25.0) * .25;
			if($(this).val() > 0 && x == 0)
				x = 0.25
			$(this).val(x)
	 }
	}
	
	$("#meal1Duration").blur(roundMealHours)
	$("#meal2Duration").blur(roundMealHours)
	$("#meal3Duration").blur(roundMealHours)
	
	toggleAlternateLanguageText()
	$("#altLanguageSelect").change(toggleAlternateLanguageText)	
	
}

function buildPrecinctPopup() {
	$("#sdsPrecinctTable").DataTable({
        "ajax": ajaxHomePath + "/precinct/quickSearch",
        "columns": [
            { "data": "name",
            	"render": function (data, type, full, meta) {
	    	      return '<a href=\'javascript:linkSDSPrecinct(' + full.id + ')\'>' + full.displayName + '</a>'
	    	    }
            }
        ],
        "dom" : '<"top"f>rt<"bottom">',
        "language": {
            zeroRecords: "", // Please search above by name or station
								// number.",
            search: "", // Search",
            searchPlaceholder: "Search by name or precinct number..."
        },
        "paging" : false,
        "processing": false,
        "scrolling" : false,
        "serverSide": true
    })
   
	 $("#sdsPrecinctTableWrapper").dialog({
		autoOpen : false,
		modal : false,
		width : 550,
		height : 350,
		closeOnEscape : true,
		draggable : true,
		resizable : true,
		buttons : {
			'Cancel' : function() {
				$(this).dialog('close')
			}
		}
	})
}

function buildPrecinctListTable() {
	$('#precinctList').DataTable({
		buttons :  [ 'excel', {
				extend : 'pdfHtml5',
				orientation : 'portrait'
			}, 'print' ],
		columnDefs : [
				{
					"targets" : 0,
					"data" : function(row, type, val, meta) {
						/* No link for VISNs */
						if (row.depth == 0)
							return '<b>' + escapeHTML(row.item.displayName) + '</b>'
						
						if (type === 'display') {
							var activeClass = (precinctId == row.item.id) ? 'activePrecinctLink' : ''
							var t = '<a class="precinctLink ' + activeClass + '" id="precinctLink' + row.item.id + '" style="margin-left:' + (row.depth * 25)
									+ 'px" href="precinctEdit.htm?id=' + row.item.id + '">'
									+ escapeHTML(row.item.displayName) + '</a>'
							if (!row.item.active)
								t = '<span class="inactivePrecinct">' + t + ' (Inactive)</span>'
							
							return t
						}
						// used for 'filter', 'sort', 'type' and undefined
						return row.item.displayName
					}
				},
				{
					"targets" : 1,
					"data" : function(row, type, val, meta) {
						return row.item.active ? 'ActiveNode' : ''
					},
					"visible" : false
				}],
		dom : '<"top"fB>rt<"bottom"pl><"clear">',
		language: {
			emptyTable: "Please wait...",
            zeroRecords: "", // Please search above by name or station
								// number.",
            search: "", // Search",
            searchPlaceholder: "Name or station number..."
        },
		order : [],
		paging : false,
		scrollY : "500px",
		scrollCollapse : true
	})
	
	$("#showInactivePrecincts").change(filterPrecinctListTable)
}

function filterPrecinctListTable() {
	var scrollPos = $("#precinctHierarchyDiv .dataTables_scrollBody").scrollTop()
	var isChecked = $("#showInactivePrecincts").is(':checked')
	var table = $('#precinctList').DataTable()
    table.columns(1).search(isChecked ? '' : 'ActiveNode').draw()
    $("#precinctHierarchyDiv .dataTables_scrollBody").scrollTop(scrollPos)
}

function refreshPrecinctListTable() {
	var flatten = function(node, depth, result) {
		var precinctChildren = node.precinctChildren
		/* Let's save some space - CPB */
		delete node['precinctChildren']
		
		result.push({
			item : node,
			depth : depth
		})
		for (var i = 0; precinctChildren && i < precinctChildren.length; i++) {
			flatten(precinctChildren[i], depth + 1, result)
		}
	}

	$.ajax({
		url : ajaxHomePath + '/findPrecinctsForHierarchyDisplay',
		dataType : 'json',
		data : {},
		global: false,     // this makes sure ajaxStart is not triggered
		error : commonAjaxErrorHandler,
		success : function(r) {
			var flattenedResults = []
			for (var i = 0; i < r.length; i++) {
				flatten(r[i], 0, flattenedResults)
			}

			var table = $('#precinctList').DataTable()
			table.clear()
			table.rows.add(flattenedResults)
			rebuildTableFilters('precinctList')
			table.draw()
			
			if (precinctId)
				$('#precinctHierarchyDiv .dataTables_scrollBody').scrollTo($('a#precinctLink' + precinctId), 0, {
					offset: -25
				})
		}
	})
}

function buildLocationListTable() {
	var locationListCols = [{
		"targets" : 0,
		"data" : function(row, type, set, meta) {
			return row.name
		}
	}, {
		"targets" : 1,
		"data" : function(row, type, set, meta) {
			var addressHtml = getLocationAddressDashedBoxEl(row).outerHTML()
			return addressHtml
		}
	}, {
		"targets" : 2,
		"data" : function(row, type, set, meta) {
			var contactInfoHtml = getLocationContactDashedBoxEl(row).outerHTML()
			return contactInfoHtml
		}
	}, {
		"targets" : 3,
		"data" : function(row, type, set, meta) {
			return row.voterActiveCount + ' Active<br>' + row.voterTotalCount + ' Total'
		}
	}, {
		"targets" : 4,
		"data" : function(row, type, set, meta) {
			if (type === 'display') {
				var actions = '<nobr>'
				if (row.active) {
					actions += '<a href="javascript:inactivateLocation('
						+ row.id + ', ' + row.voterActiveCount + ')"><img src="' + imgHomePath
						+ '/switch.png" border="0" hspace="5" align="center" alt="Inactivate Location" /></a>'
				} else {
					actions += '<a href="javascript:reactivateLocation('
						+ row.id + ')"><img src="'+ imgHomePath
						+ '/switch.png" border="0" hspace="5" align="center" alt="Reactivate Location" /></a>'
				}
				return actions + ' ' + (row.active ? 'Active' : 'Inactive') + '</nobr>'
			} else {
				return row.active ? 'Active' : 'Inactive'
			}
		}
	}]
	
	// if we want to make screen read-only, change this - CPB
	if (true) {
		locationListCols[locationListCols.length] = {
			"targets" : 5,
			"data" : function(row, type, set, meta) {
				var actions = '<div style="margin:0 auto; text-align:center">'
				actions += '<a href="javascript:popupLocationEdit(' + row.id + ')"><img src="' + imgHomePath
					+ '/edit-small.gif" border="0" hspace="5" align="center" alt="Edit Location" /></a>'
				if (row.voterTotalCount == 0) {
				actions += '<a href="javascript:deleteLocation('
						+ row.id + ', ' + row.voterActiveCount + ')"><img src="' + imgHomePath
						+ '/delete.gif" border="0" hspace="5" align="center" alt="Delete Location" /></a>'
				}
				actions += '</div>'
				return actions;
			}
		}
	}
	    			
	$('#locationList').DataTable({
		"columnDefs" : locationListCols,
		"dom" : '<"top">rt<"bottom"pl><"clear">',
		"order" : [],
		"paging" : false,
		"scrollY" : "200px",
		"scrollCollapse" : true
	})
}

function buildKioskListTable() {
	var kioskListCols = [{
		"targets" : 0,
		"data" : function(row, type, set, meta) {
			return row.location
		}
	}, {
		"targets" : 1,
		"data" : function(row, type, set, meta) {
			return row.registered ? 'Yes' : 'No'
		}
	}, {
		"targets" : 2,
		"data" : function(row, type, set, meta) {
			return row.printerStatus ? 'Online' : '<span class="redText">Offline</span>'
		}
	}, {
		"targets" : 3,
		"data" : function(row, type, set, meta) {
			return row.printRequestCount
		}
	}]
	
	// if we want to make screen read-only, change this - CPB
	if (true) {
		kioskListCols[kioskListCols.length] = {
			"targets" : 4,
			"data" : function(row, type, set, meta) {
				var actions = '<div style="margin:0 auto; text-align:center">'
				actions += '<a href="javascript:popupKioskEdit(' + row.id + ')"><img src="' + imgHomePath
					+ '/edit-small.gif" border="0" hspace="5" align="center" alt="Edit Kiosk" /></a>'
				actions += '<a href="javascript:deleteKiosk('
						+ row.id + ')"><img src="' + imgHomePath
						+ '/delete.gif" border="0" hspace="5" align="center" alt="Delete Kiosk" /></a>'
				actions += '</div>'
				return actions;
			}
		}
	}
	    			
	$('#kioskList').DataTable({
		"columnDefs" : kioskListCols,
		"dom" : '<"top">rt<"bottom"pl><"clear">',
		"order" : [],
		"paging" : false,
		"scrollY" : "200px",
		"scrollCollapse" : true
	})
}

function submitLocationAddOrEdit() {
	   doubleClickSafeguard($("#locationPopupSubmit"))
	   
	var locationId = $("#locationFieldsWrapper").data('locationId')
	
	var name = $("#locationName").val()
	var addressLine1 = $("#locationAddressLine1").val()
	var city = $("#locationAddressCity").val()
	var state = $("#locationAddressState").val()
	var zip = $("#locationAddressZip").val()
	
	var errors = new Array()
	
	if ($.trim(name) == '')
		errors.push('Please enter the name.')
	if ($.trim(addressLine1) == '')
		errors.push('Please enter the address line 1.')
	if ($.trim(city) == '')
		errors.push('Please enter the city.')
	if ($.trim(state) == '')
		errors.push('Please select the state.')
	if ($.trim(zip) == '')
		errors.push('Please enter the zip code.')
	var contactPhone = $("#locationContactPhone").val()
	if ($.trim(contactPhone) != '' && !validatePhone(contactPhone))
		errors.push("Please enter a valid phone number.")
	var contactEmail = $("#locationContactEmail").val()
	if (!validateEmail(contactEmail))
		errors.push("Please enter a valid contact email in the format 'user@domain.tld'.")
	
	if (errors.length > 0) {
		displayAttentionDialog("Please correct the following errors: <ul><li>"
				+ errors.join("</li><li>") + "</li></ul>");
		return
	}
	
	$.ajax({
		url : ajaxHomePath + '/precinct/location/saveOrUpdate',
		dataType : 'json',
		data : {
			precinctId : precinctId,
			locationId : locationId,
			name : name,
			addressLine1 : addressLine1,
			addressLine2 : $("#locationAddressLine2").val(),
			city : city,
			state : state,
			zip : zip,
			contactName : $("#locationContactName").val(),
			contactRole : $("#locationContactRole").val(),
			contactPhone : $("#locationContactPhone").val(),
			contactEmail : contactEmail,
		},
		error : commonAjaxErrorHandler,
		success : function() {
			$("#locationFieldsWrapper").dialog('close')
			refreshLocations()
		}
	})
}

function submitKioskAddOrEdit() {
   doubleClickSafeguard($("#kioskPopupSubmit"))
	var kioskId = $("#kioskFieldsWrapper").data('kioskId')
	var location = $("#kioskLocation").val()
	var registered = $("#kioskRegistered").is(':checked')
	
	var errors = new Array()
	
	if ($.trim(location) == '')
		errors.push('Please enter the location.')
	
	if (errors.length > 0) {
		displayAttentionDialog("Please correct the following errors: <ul><li>"
				+ errors.join("</li><li>") + "</li></ul>");
		return
	}
	
	$.ajax({
		url : ajaxHomePath + '/precinct/kiosk/saveOrUpdate',
		dataType : 'json',
		data : {
			precinctId : precinctId,
			kioskId : kioskId,
			location : location,
			registered : registered
		},
		error : commonAjaxErrorHandler,
		success : function() {
			$("#kioskFieldsWrapper").dialog('close')
			refreshKiosks()
		}
	})
}

function buildLocationPopup() {
	$("#locationFieldsWrapper").dialog({
		autoOpen : false,
		modal : false,
		width : 700,
		height : 300,
		closeOnEscape : true,
		draggable : true,
		resizable : true,
		buttons : [
		           {
		               id: "locationPopupSubmit",
		               text: "Submit",
		               click: submitLocationAddOrEdit
		           },
		           {
		               id: "locationPopupCancel",
		               text: "Cancel",
		               click: function() {
		   				$(this).dialog('close')
		   			}
		           }
		       ]
	})
}

function buildKioskPopup() {
	$("#kioskFieldsWrapper").dialog({
		autoOpen : false,
		modal : false,
		width : 400,
		height : 200,
		closeOnEscape : true,
		draggable : true,
		resizable : true,
		buttons : [
		           {
		               id: "kioskPopupSubmit",
		               text: "Submit",
		               click: submitKioskAddOrEdit
		           },
		           {
		               id: "kioskPopupCancel",
		               text: "Cancel",
		               click: function() {
		   				$(this).dialog('close')
		   			}
		           }
		       ]
	})
}

function linkSDSPrecinct(precinctId) {
	$.ajax({
		url : ajaxHomePath + '/linkSDSPrecinctToPrecinct',
		dataType : 'json',
		data : {
			precinctId : precinctId,
			precinctId : precinctId
		},
		error : commonAjaxErrorHandler,
		success : function(r) {
			document.location.href = "precinctEdit.htm?id=" + precinctId
			showSpinner('', true)
		}
	})
}

function popupSDSPrecinctSelection() {
	$("#sdsPrecinctTableWrapper").dialog('open')
}

var allLocations = {}
function refreshLocations() {
	var table = $('#locationList').DataTable()
	table.clear()
	
	$.ajax({
		url : ajaxHomePath + '/precinct/location',
		dataType : 'json',
		data : {
			precinctId : precinctId,
			includeCounts : true
		},
		error : commonAjaxErrorHandler,
		success : function(r) {
			var locations = r.locations
			var countsMap = r.countsMap
			
			var rArray = new Array()
			
			allLocations = {}
			for (var i = 0; i < locations.length; i++) {
				var location = locations[i]
				allLocations['' + location.id] = location
				var x = countsMap[location.id] || [0, 0]
				rArray[rArray.length] = $.extend({}, location, {
					voterActiveCount : x[0],
					voterTotalCount : x[1]
				})
			}
			
			table.rows.add(rArray)
			rebuildTableFilters('locationList')
			table.draw()
			
			$("#locationList_filter4").val('Active')
			$("#locationList_filter4").change()
		}
	})
}

function popupLocationEdit(locationId) {
	var location = null
	if (locationId && allLocations[locationId])
		location = allLocations[locationId]
	$("#locationFieldsWrapper").data('locationId', locationId ? locationId : '')
	
	$("#locationName").val(location ? location.name : '')
	$("#locationAddressLine1").val(location ? location.addressLine1 : '')
	$("#locationAddressLine2").val(location ? location.addressLine2 : '')
	$("#locationAddressCity").val(location ? location.city : '')
	$("#locationAddressState").val(location && location.state ? location.state.id : '')
	$("#locationAddressZip").val(location ? location.zip : '')
	$("#locationContactName").val(location ? location.contactName : '')
	$("#locationContactRole").val(location ? location.contactRole : '')
	$("#locationContactPhone").val(location ? location.contactPhone : '')
	$("#locationContactEmail").val(location ? location.contactEmail : '')
	$("#locationFieldsWrapper").dialog('open')
}

function deleteLocation(locationId, activeVolProfiles) {
	var location = allLocations[locationId]
	
	confirmDialog('Are you sure you want to delete this location?', function() {
		$.ajax({
			url : ajaxHomePath + '/precinct/location/delete',
			dataType : 'json',
			data : {
				locationId : locationId
			},
			error : commonAjaxErrorHandler,
			success : refreshLocations
		})
	})
}

function inactivateLocation(locationId, activeVolProfiles) {
	var location = allLocations[locationId]
	var func = function() {
		$.ajax({
			url : ajaxHomePath + '/precinct/location/inactivate',
			dataType : 'json',
			data : {
				locationId : locationId
			},
			error : commonAjaxErrorHandler,
			success : refreshLocations
		})
	}
	
	if (activeVolProfiles > 0) {
		confirmDialog(location.name + ' is active in ' + activeVolProfiles + ' voter profile(s).' 
					+ ' Inactivating this location will also inactivate all associated voter assignments. Do you wish to continue?'
			, func)
	} else {
		func()
	}
}

function reactivateLocation(locationId) {
	$.ajax({
		url : ajaxHomePath + '/precinct/location/reactivate',
		dataType : 'json',
		data : {
			locationId : locationId
		},
		error : commonAjaxErrorHandler,
		success : refreshLocations
	})
}

var allKiosks = {}
function refreshKiosks() {
	var table = $('#kioskList').DataTable()
	table.clear()
	
	$.ajax({
		url : ajaxHomePath + '/precinct/kiosk',
		dataType : 'json',
		data : {
			precinctId : precinctId
		},
		error : commonAjaxErrorHandler,
		success : function(r) {
			var kiosks = r.kiosks
			var statusMap = r.kioskStatusMap
			var kioskPrintRequestCountMap = r.kioskPrintRequestCountMap
			var rArray = new Array()
			
			allKiosks = {}
			for (var i = 0; i < kiosks.length; i++) {
				var kiosk = kiosks[i]
				allKiosks['' + kiosk.id] = kiosk
				rArray[rArray.length] = $.extend({}, kiosk, {
					printerStatus : statusMap[kiosk.id],
					printRequestCount : kioskPrintRequestCountMap[kiosk.id]
				})
			}
			
			table.rows.add(rArray)
			rebuildTableFilters('kioskList')
			table.draw()
		}
	})
}

function popupKioskEdit(kioskId) {
	var kiosk = null
	if (kioskId && allKiosks[kioskId])
		kiosk = allKiosks[kioskId]
	$("#kioskFieldsWrapper").data('kioskId', kioskId ? kioskId : '')
	
	$("#kioskLocation").val(kiosk ? kiosk.location : '')
	$("#kioskRegistered").prop('checked', kiosk ? kiosk.registered : false)
	$("#kioskFieldsWrapper").dialog('open')
}

function deleteKiosk(kioskId) {
	var kiosk = allKiosks[kioskId]
	
	confirmDialog('Are you sure you want to delete this kiosk?<p>Deleting this entry will disable the kiosk currently configured for it.' +
			' If the kiosk is being moved, please rename it instead. The kiosk will need to be reconfigured by IT staff before it will work with a new entry.', function() {
		$.ajax({
			url : ajaxHomePath + '/precinct/kiosk/delete',
			dataType : 'json',
			data : {
				kioskId : kioskId
			},
			error : commonAjaxErrorHandler,
			success : refreshKiosks
		})
	}, {
		height : 250
	})
}

function unlinkSDSPrecinct() {
	$.ajax({
		url : ajaxHomePath + '/unlinkSDSPrecinctFromPrecinct',
		dataType : 'json',
		data : {
			precinctId : precinctId
		},
		error : commonAjaxErrorHandler,
		success : function(r) {
			document.location.href = "precinctEdit.htm?id=" + precinctId
			showSpinner('', true)
		}
	})
}

function submitForm(isPersistent) {
	if (!validate())
	    return false

	doubleClickSafeguard($("#submitButton"))
	return true
}

function getLocationAddressDashedBoxEl(location) {
	var theHtml = '<table width="100%" class="addressBox">'
	theHtml += '<tr valign="top">'
	var addressHtml = ""
	if (location.addressMultilineDisplay)
		addressHtml = escapeHTML(location.addressMultilineDisplay)
	theHtml += '<td width="1%" nowrap>' + convertLinefeedToBR(addressHtml)
			+ '</td></tr></table>'
	return getBoxEl(theHtml, false)
}

function getLocationContactDashedBoxEl(location) {
	var theHtml = '<table width="100%" class="addressBox">'
	theHtml += '<tr valign="top"><td nowrap width="99%">'
	
	if (location) {
		var nameHtml = ""
		if (location.contactName)
			nameHtml += escapeHTML(location.contactName) + '<br>'
		var roleHtml = ""
		if (location.contactRole)
			roleHtml += '<i>' + escapeHTML(location.contactRole) + '</i><br>'
		var phoneHtml = ""
		if (location.contactPhone)
			phoneHtml = escapeHTML(location.contactPhone) + '<br>'
		var emailHtml = ""
		if (location.contactEmail)
			emailHtml = escapeHTML(location.contactEmail) + '<a href="mailto:'
					+ escapeHTML(location.contactEmail)
					+ '"><img alt="Click to email '
					+ escapeHTML(location.contactEmail) + '"' + 'src="' + imgHomePath
					+ '/envelope.jpg" height="14"'
					+ ' width="18" border="0" align="absmiddle"'
					+ ' style="padding-left: 4px; padding-right: 4px" /></a>'

		theHtml += nameHtml + roleHtml + phoneHtml + emailHtml
	}
	theHtml += '</td></tr></table>'
	return getBoxEl(theHtml, false)
}

function toggleTheMealFields(numMeals) {
	$('.meal1').toggle(shouldShowMeal1(numMeals));
    $('.meal2').toggle(shouldShowMeal2(numMeals));
    $('.meal3').toggle(shouldShowMeal3(numMeals));
	
}

function shouldShowMeal1(numMeals) {
	return numMeals > '0'
}

function shouldShowMeal2(numMeals) {
	return numMeals > '1'
}
function shouldShowMeal3(numMeals) {
	return numMeals > '2'
}

function validate() {
	var errors = new Array()
	if(commandNumMeals > 0) {
		if ($('#mealPrice').val() == '')
			errors.push("Meal Price is required.")
	
		if ($('#mealPrice').val() != '' &&  !validateNumericWithoutCommas($('#mealPrice').val()))
			errors.push("Meal Price must be numeric.")
			
		if ($('#mealPrice').val() != '' &&  parseFloat($('#mealPrice').val()) > 99.99 )
			errors.push("The maximum Meal Price is 99.99.")
		
		if ($('#meal1Duration').val() == '')
			errors.push("Required Hours for 1 Meal is required.")
			
		if ($('#meal1Duration').val() != '' &&  (isNaN(parseFloat($('#meal1Duration').val())) || parseFloat($('#meal1Duration').val()) > 24 ) || (parseFloat($('#meal1Duration').val()) < 0.25 ) )
			errors.push("The Required Hours for 1 Meal must be numeric and between 0.25 and 24.")
	
	    if ($('#meal1CutoffTime').val() == '')
			errors.push("Cutoff Time for 1 Meal is required.")
		
		if ($('#meal1CutoffTime').val() != '' &&  (!validateInteger($('#meal1CutoffTime').val()) || $('#meal1CutoffTime').val().length != 4))
				errors.push("Cutoff Time for 1 Meal must be in Military Time (0000 - 2359).")
				
		if ($('#meal1CutoffTime').val() != '' &&  parseFloat($('#meal1CutoffTime').val()) > 2359 )
				errors.push("The maximum Cutoff Time (HHMM) for 1 Meal  is 2359.")
	}

	if(commandNumMeals > 1) {

		if ($('#meal2Duration').val() == '')
			errors.push("Required Hours for 2 Meals is required.")
			
		if ($('#meal2Duration').val() != '' &&  (isNaN(parseFloat($('#meal2Duration').val())) || parseFloat($('#meal2Duration').val()) > 24 ) || (parseFloat($('#meal2Duration').val()) < 0.25 ) )
			errors.push("The Required Hours for 2 Meals must be numeric and between 0.25 and 24.")

        if ($('#meal2Duration').val() != '' && !(parseFloat($('#meal2Duration').val()) >  parseFloat($('#meal1Duration').val())))
        	    errors.push("The required hours for 2 meals must be greater than hours required for 1 meal")
	
	    if ($('#meal2CutoffTime').val() == '')
			errors.push("Cutoff Time for 2 Meals is required.")
		
		if ($('#meal2CutoffTime').val() != '' &&  (!validateInteger($('#meal2CutoffTime').val()) || $('#meal2CutoffTime').val().length != 4))
				errors.push("Cutoff Time for 2 Meals must be in Military Time (0000 - 2359).")
				
		if ($('#meal2CutoffTime').val() != '' &&  parseFloat($('#meal2CutoffTime').val()) > 2359 )
				errors.push("The maximum Cutoff Time (HHMM) for 2 Meals  is 2359.")
				
		if ($('#meal2CutoffTime').val() != '' &&  (parseFloat($('#meal2CutoffTime').val()) >=  parseFloat($('#meal1CutoffTime').val())) )
				errors.push("The Cutoff Time (HHMM) for 2 Meals must be less than Cutoff Time for 1 meal.")
		
	}
	
	if(commandNumMeals > 2) {
			
		if ($('#meal3Duration').val() == '')
			errors.push("Required Hours for 3 Meals is required.")
			
		if ($('#meal3Duration').val() != '' &&  (isNaN(parseFloat($('#meal3Duration').val())) || parseFloat($('#meal3Duration').val()) > 24 ) || (parseFloat($('#meal3Duration').val()) < 0.25 ) )
			 errors.push("The Required Hours for 3 Meals must be numeric and between 0.25 and 24.")

        if ($('#meal3Duration').val() != '' && (parseFloat($('#meal3Duration').val()) <=  parseFloat($('#meal2Duration').val())))
        	    errors.push("The required hours for 3 meals must be greater than hours required for 2 meals")
	
	    if ($('#meal3CutoffTime').val() == '')
			errors.push("Cutoff Time for 3 Meals  is required.")
		
		if ($('#meal3CutoffTime').val() != '' &&  (!validateInteger($('#meal3CutoffTime').val()) || $('#meal3CutoffTime').val().length != 4))
				errors.push("Cutoff Time for 3 Meals must be in Military Time (0000 - 2359).")
				
		if ($('#meal3CutoffTime').val() != '' &&  parseFloat($('#meal3CutoffTime').val()) > 2359 )
				errors.push("The maximum Cutoff Time (HHMM) for 3 Meals  is 2359.")
				
		if ($('#meal3CutoffTime').val() != '' &&  (parseFloat($('#meal3CutoffTime').val()) >=  parseFloat($('#meal2CutoffTime').val())) )
				errors.push("The Cutoff Time (HHMM) for 3 Meals must be less than Cutoff Time for 2 meals.")
	}
	
	if ($('#valueAmount').val() != '' &&  (parseFloat($('#valueAmount').val()) > 9999999999.99 ))
		errors.push("The maximum of Set Value Amount is 9999999999.99.")
		
	if ($('#valueAmount').val() != ''
		&& !validateNumericWithoutCommas($('#valueAmount').val())) {
		displayAttentionDialog("Set Value Amount is invalid [format should be ## or ##.##].") 
		return
	}
	
	if ($('#precinctTimeZone').val() == '') {
		errors.push("Please select a time zone.")
	}
	
	if (errors.length > 0)
		displayAttentionDialog("Please correct the following errors: <ul><li>"
				+ errors.join("</li><li>") + "</li></ul>");

	return errors.length == 0
}

function refreshDonGenPostFundList() {
	var myPrecinctId = precinctId
	donGenPostFundList = {}
	$.ajax({
				url : ajaxHomePath + '/donGenPostFundList',
				dataType : 'json',
				data : {
					precinctId : myPrecinctId ? myPrecinctId : 0
				},
				error : commonAjaxErrorHandler,
				success : function(response) {
					var rArray = new Array()
																	
					for (var i = 0; i < response.length; i++) {
						donGenPostFundList[response[i].id] = response[i]
						rArray[rArray.length] = response[i] 
					}
					
					$(".confirm").easyconfirm()
					
					refreshDonGenPostFundListData(rArray)
					
				}
			})
}

function refreshDonReferenceList() {
	var myPrecinctId = precinctId
	donationRefList = {}
	$.ajax({
				url : ajaxHomePath + '/donReferenceList',
				dataType : 'json',
				data : {
					precinctId : myPrecinctId ? myPrecinctId : 0
				},
				error : commonAjaxErrorHandler,
				success : function(response) {
					var rArray = new Array()
																	
					for (var i = 0; i < response.length; i++) {
						donationRefList[response[i].id] = response[i]
						rArray[rArray.length] = response[i] 
					}
					
					$(".confirm").easyconfirm()
					
					refreshDonReferenceListData(rArray)
					
				}
			})
}


function refreshDonGenPostFundListData(r) {
	var table = $('#donGenPostFundList').DataTable()
	table.clear()
	table.rows.add(r)
	table.draw()
}

function refreshDonReferenceListData(r) {
	var table = $('#donationRefList').DataTable()
	table.clear()
	table.rows.add(r)
	table.draw()
}

function popupDonGenPostFundEdit(donGenPostFundId) {
	var donGenPostFund = null
	if (donGenPostFundId && donGenPostFundList[donGenPostFundId])
		donGenPostFund = donGenPostFundList[donGenPostFundId]
	$("#donGenPostFundFieldsWrapper").data('donGenPostFundId', donGenPostFundId ? donGenPostFundId : '')
	$("#donGenPostFundName").val(donGenPostFund ? donGenPostFund.generalPostFund : '')
	$("#donGenPostFundActive").prop('checked', !donGenPostFund || donGenPostFund.active)
	$("#donGenPostFundFieldsWrapper").dialog('open')
}

function buildDonGenPostFundPopup() {
	$("#donGenPostFundFieldsWrapper").dialog({
		autoOpen : false,
		modal : false,
		width : 700,
		height : 250,
		closeOnEscape : true,
		draggable : true,
		resizable : true,
		buttons : [
		           {
		               id: "donGenPostFundPopupSubmit",
		               text: "Submit",
		               click: submitDonGenPostFundAddOrEdit
		           },
		           {
		               id: "donGenPostFundPopupCancel",
		               text: "Cancel",
		               click: function() {
		   				$(this).dialog('close')
		   			}
		           }
		       ]
	})
}

function submitDonGenPostFundAddOrEdit() {
	   doubleClickSafeguard($("#donGenPostFundPopupSubmit"))
	   
	var donGenPostFundId = $("#donGenPostFundFieldsWrapper").data('donGenPostFundId')
	
	var genPostFund = $("#donGenPostFundName").val()
		
	var errors = new Array()  // TODO_SZ keep list just in case more fields
								// needed such as status
	
	if ($.trim(genPostFund) == '')
		errors.push('Please enter the General Post Fund.')
		
	if (errors.length > 0) {
		displayAttentionDialog("Please correct the following errors: <ul><li>"
				+ errors.join("</li><li>") + "</li></ul>");
		return
	}
	
	$.ajax({
		url : ajaxHomePath + '/precinct/donGenPostFund/saveOrUpdate',
		dataType : 'json',
		data : {
			precinctId : precinctId,
			genPostFundId : donGenPostFundId,
			genPostFund : genPostFund,
			active : $("#donGenPostFundActive").is(':checked')
		},
		error : commonAjaxErrorHandler,
		success : function() {
			$("#donGenPostFundFieldsWrapper").dialog('close')
			refreshDonGenPostFundList()
		}
	})
}

function popupDonReferenceEdit(donReferenceId) {
	var donReference = null
	if (donReferenceId && donationRefList[donReferenceId])
		donReference = donationRefList[donReferenceId]
	$("#donReferenceFieldsWrapper").data('donReferenceId', donReferenceId ? donReferenceId : '')
	$("#donReferenceName").val(donReference ? donReference.donationReference : '')
	$("#donReferenceActive").prop('checked', !donReference || donReference.active)
	$("#donReferenceFieldsWrapper").dialog('open')
}

function buildDonReferencePopup() {
	$("#donReferenceFieldsWrapper").dialog({
		autoOpen : false,
		modal : false,
		width : 700,
		height : 250,
		closeOnEscape : true,
		draggable : true,
		resizable : true,
		buttons : [
		           {
		               id: "donReferencePopupSubmit",
		               text: "Submit",
		               click: submitDonReferenceAddOrEdit
		           },
		           {
		               id: "donReferencePopupCancel",
		               text: "Cancel",
		               click: function() {
		   				$(this).dialog('close')
		   			}
		           }
		       ]
	})
}

function submitDonReferenceAddOrEdit() {
	   doubleClickSafeguard($("#donReferencePopupSubmit"))
	   
	var donReferenceId = $("#donReferenceFieldsWrapper").data('donReferenceId')
	
	var donRef = $("#donReferenceName").val()
		
	var errors = new Array()  // TODO_SZ keep list just in case more fields
								// needed such as status
	
	if ($.trim(donRef) == '')
		errors.push('Please enter the Reference.')
		
	if (errors.length > 0) {
		displayAttentionDialog("Please correct the following errors: <ul><li>"
				+ errors.join("</li><li>") + "</li></ul>");
		return
	}
	
	$.ajax({
		url : ajaxHomePath + '/precinct/donReference/saveOrUpdate',
		dataType : 'json',
		data : {
			precinctId : precinctId,
			donReferenceId : donReferenceId,
			donRef : donRef,
			active : $("#donReferenceActive").is(':checked')
		},
		error : commonAjaxErrorHandler,
		success : function() {
			$("#donReferenceFieldsWrapper").dialog('close')
			refreshDonReferenceList()
		}
	})
}

function buildDonGenPostFundListTable() {
	var donGenPostFundListCols = [{
		"targets" : 0,
		"data" : function(row, type, set, meta) {
			return row.generalPostFund
		}
	}, {
		"targets" : 1,
		"data" : function(row, type, set, meta) {
			if (type === 'display') {
				var actions = '<nobr>'
				if (!row.inactive) {
					actions += '<a href="javascript:inactivateDonGenPostFund('
						+ row.id + ')"><img src="' + imgHomePath
						+ '/switch.png" border="0" hspace="5" align="center" alt="Inactivate Donation General Post Fund" /></a>'
				} else {
					actions += '<a href="javascript:reactivateDonGenPostFund('
						+ row.id + ')"><img src="'+ imgHomePath
						+ '/switch.png" border="0" hspace="5" align="center" alt="Reactivate Donation General Post Fund" /></a>'
				}
				return actions + ' ' + (row.inactive ? 'Inactive' : 'Active') + '</nobr>'
			} else {
				return row.inactive ? 'Inactive' : 'Active'
			}
		}
	}]
	
	// if we want to make screen read-only, change this - CPB
	if (true) {
		donGenPostFundListCols[donGenPostFundListCols.length] = {
			"targets" : 2,
			"data" : function(row, type, set, meta) {
				var actions = '<div style="margin:0 auto; text-align:center">'
				actions += '<a href="javascript:popupDonGenPostFundEdit(' + row.id + ')"><img src="' + imgHomePath
					+ '/edit-small.gif" border="0" hspace="5" align="center" alt="Edit Donation General Post Fund" /></a>'
				actions += '<a href="javascript:deleteDonGenPostFund('
						+ row.id + ')"><img src="' + imgHomePath
						+ '/delete.gif" border="0" hspace="5" align="center" alt="Delete Donation General Post Fund" /></a>'
				actions += '</div>'
				return actions;
			}
		}
	}
	    			
	$('#donGenPostFundList').DataTable({
		"columnDefs" : donGenPostFundListCols,
		"dom": '<"top"fi>rt<"bottom"pl><"clear">',
		"stateSave" : false,
		"lengthMenu" : [ [ 10, 50, -1 ],
    	 				[ 10, 50, "All" ] ],
		"order": [],
    	"pageLength": 10,
    	"pagingType": "full_numbers"
	})
}

function buildDonReferenceListTable() {
	var donReferenceListCols = [{
		"targets" : 0,
		"data" : function(row, type, set, meta) {
			return row.donationReference
		}
	}, {
		"targets" : 1,
		"data" : function(row, type, set, meta) {
			if (type === 'display') {
				var actions = '<nobr>'
				if (!row.inactive) {
					actions += '<a href="javascript:inactivateDonReference('
						+ row.id + ')"><img src="' + imgHomePath
						+ '/switch.png" border="0" hspace="5" align="center" alt="Inactivate Donation General Post Fund" /></a>'
				} else {
					actions += '<a href="javascript:reactivateDonReference('
						+ row.id + ')"><img src="'+ imgHomePath
						+ '/switch.png" border="0" hspace="5" align="center" alt="Reactivate Donation General Post Fund" /></a>'
				}
				return actions + ' ' + (row.inactive ? 'Inactive' : 'Active') + '</nobr>'
			} else {
				return row.inactive ? 'Inactive' : 'Active'
			}
		}
	}]
	
	// if we want to make screen read-only, change this - CPB
	if (true) {
		donReferenceListCols[donReferenceListCols.length] = {
			"targets" : 2,
			"data" : function(row, type, set, meta) {
				var actions = '<div style="margin:0 auto; text-align:center">'
				actions += '<a href="javascript:popupDonReferenceEdit(' + row.id + ')"><img src="' + imgHomePath
					+ '/edit-small.gif" border="0" hspace="5" align="center" alt="Edit Donation Reference" /></a>'
				actions += '<a href="javascript:deleteDonReference('
						+ row.id + ')"><img src="' + imgHomePath
						+ '/delete.gif" border="0" hspace="5" align="center" alt="Delete Donation Reference" /></a>'
				actions += '</div>'
				return actions;
			}
		}
	}
	    			
	$('#donationRefList').DataTable({
		"columnDefs" : donReferenceListCols,
		"dom": '<"top"fi>rt<"bottom"pl><"clear">',
		"stateSave" : false,
		"lengthMenu" : [ [ 10, 50, -1 ],
    	 				[ 10, 50, "All" ] ],
		"order": [],
    	"pageLength": 10,
    	"pagingType": "full_numbers"
	})
}

function deleteDonGenPostFund(donGenPostFundId) {
	var  donGenPostFund = donGenPostFundList[donGenPostFundId]
	
	confirmDialog('Are you sure you want to delete this Donation General Post Fund?', function() {
		$.ajax({
			url : ajaxHomePath + '/precinct/donGenPostFund/delete',
			dataType : 'json',
			data : {
				donGenPostFundId : donGenPostFundId
			},
			error : commonAjaxErrorHandler,
			success: function() {refreshDonGenPostFundList()}
		})
	})
}


function deleteDonReference(donReferenceId) {
	var  donReference = donationRefList[donReferenceId]
	
	confirmDialog('Are you sure you want to delete this Donation Reference?', function() {
		$.ajax({
			url : ajaxHomePath + '/precinct/donReference/delete',
			dataType : 'json',
			data : {
				donReferenceId : donReferenceId
			},
			error : commonAjaxErrorHandler,
			success : function() {refreshDonReferenceList() }
		})
	})
}

function inactivateDonGenPostFund(donGenPostFundId) {
	var donGenPostFund = donGenPostFundList[donGenPostFundId]
	$.ajax({
			url : ajaxHomePath + '/precinct/donGenPostFund/inactivate',
			dataType : 'json',
			data : {
				donGenPostFundId : donGenPostFundId
			},
			error : commonAjaxErrorHandler,
			success : function() { refreshDonGenPostFundList() }
		})
}

function reactivateDonGenPostFund(donGenPostFundId) {
	$.ajax({
		url : ajaxHomePath + '/precinct/donGenPostFund/reactivate',
		dataType : 'json',
		data : {
			donGenPostFundId : donGenPostFundId
		},
		error : commonAjaxErrorHandler,
		success : function() { refreshDonGenPostFundList() }
	})
}

function inactivateDonReference(donReferenceId) {
	var donReference = donationRefList[donReferenceId]
	$.ajax({
			url : ajaxHomePath + '/precinct/donReference/inactivate',
			dataType : 'json',
			data : {
				donReferenceId : donReferenceId
			},
			error : commonAjaxErrorHandler,
			success : function() { refreshDonReferenceList() }
		})
}

function reactivateDonReference(donReferenceId) {
	$.ajax({
		url : ajaxHomePath + '/precinct/donReference/reactivate',
		dataType : 'json',
		data : {
			donReferenceId : donReferenceId
		},
		error : commonAjaxErrorHandler,
		success : function() { refreshDonReferenceList() }
	})
}

function toggleAlternateLanguageText() {
    alternateLanguage = $("#altLanguageSelect").val()
   	$(".alternateLangWelcomeText").toggle(alternateLanguage != '-1')
}




