function onPageLoad(isNew, isReadOnly, disableTerminationFields, anyTerminationFieldsSet) {
	if (isReadOnly)
		setPageTitleText('View Voter')
	
	$('.dateInput').each(function() {
		$(this).enableDatePicker({
			showOn : "button",
			buttonImage : imgHomePath + "/calendar.gif",
			buttonImageOnly : true
		})
		$(this).mask(twoDigitDateMask)
	})
	
	var defaultParms = {
		"paging" : false,
		"processing" : true,
		"scrollCollapse" : true,
    	"scrollY" : "110px"
	}
	
	if (isNew) {
		editName()
	} else {
		buildOrganizationsTable(defaultParms, isReadOnly)
		refreshOrganizations()
		
		buildAssignmentsTable(defaultParms, isReadOnly)
		refreshAssignments()
		
		buildRequirementsTable(defaultParms, isReadOnly)
		refreshRequirements()

		buildParkingStickersTable(defaultParms, isReadOnly)
		refreshParkingStickers()
		
		buildUniformsTable(defaultParms, isReadOnly)
		refreshUniforms()
		
		$("#primaryOrganization").change(function() {
			setPrimaryOrganization($(this).val())
		})
		
		$("#primaryPrecinct").change(function() {
			setPrimaryPrecinct($(this).val())
		})
		
	}
	
	// ----------- Meals fields
	
	var mealRemarksUpdated = function() {
		var theVal = $("#mealsEligibleInput").val()
		var show = $.trim(theVal) == '0'
		var theTextarea = $("#mealRemarks")
		theTextarea.attr('placeholder', show ? '*[Meal remarks - required]' : '[Meal remarks]')
		theTextarea.removeClass("placeholderRequired")
		if (show)
			theTextarea.addClass("placeholderRequired")
		
	}
	$("#mealsEligibleInput").change(mealRemarksUpdated)
	mealRemarksUpdated()
	
	// ----------- Terminated fields
	
	$('.terminatedFields').toggle(anyTerminationFieldsSet)
	$('.notTerminatedFields').toggle(!anyTerminationFieldsSet)
	
	var updateTerminationFields = function() {
		var terminatedWithCause = $("#terminatedWithCauseCheckbox").is(":checked")
		var hasTerminationDate = $("#terminationDate").val() != ''
		
		$("#terminationDateRequired").toggle(terminatedWithCause)
		
		var terminationRemarks = $("#terminationRemarks")
		terminationRemarks.attr('placeholder', hasTerminationDate || terminatedWithCause ? '*[Termination remarks - required]' : '[Termination remarks]')
		terminationRemarks.removeClass("placeholderRequired")
		if (hasTerminationDate || terminatedWithCause)
			terminationRemarks.addClass("placeholderRequired")
		
		if (disableTerminationFields) {
			$("#terminatedWithCauseCheckbox").attr('disabled', true)
			terminationRemarks.attr('disabled', true)
			$("#terminationDate").attr('disabled', true)
		}
	}
	$("#terminatedWithCauseCheckbox").change(updateTerminationFields)
	$("#terminationDate").change(updateTerminationFields)
	
	updateTerminationFields()
	
	// ----------- Awards fields
	
	$("#awardSelect").change(function() {
		$("#lastAwardHours").val(awardHoursMap[$(this).val()])
		if ($(this).val() == '')
			$("#dateOfLastAwardInput").val('')
	})
	
}

function buildAssignmentsTable(defaultParms, isReadOnly) {
	var assignmentCols = [ {
		"targets" : 0,
		"data" : function(row, type, val, meta) {
			var bs = row.benefitingService
			var s = bs.name + ($.trim(bs.subdivision) == '' ? '' : ' - ' + bs.subdivision)
			if (row.benefitingServiceRole)
				s += " - " + row.benefitingServiceRole.name
			if (type === 'filter') {
				return abbreviate(s, 20)
			}
			return s
		}
	}, {
		"targets" : 1,
		"data" : function(row, type, val, meta) {
			var s = row.benefitingService.precinctDisplayName
			if (type === 'filter') {
				return abbreviate(s, 20)
			}
			return s
		}
	}, {
		"targets" : 2,
		"data" : function(row, type, val, meta) {
			var s = row.benefitingServiceRole.locationDisplayName
			if (type === 'filter') {
				return abbreviate(s, 20)
			}
			return s
		}
	}, {
		"targets" : 3,
		"data" : function(row, type, val, meta) {
			if (type === 'display' && !isReadOnly) {
				var actions = '<nobr>'
				if (row.precinct && voterEditSites.search(row.precinct.rootPrecinctId) != -1) {
					if (row.active) {
						actions += '<a class="assnStatusLink" href="javascript:inactivateAssignment('
							+ row.id + ')"><img src="' + imgHomePath
							+ '/switch.png" border="0" hspace="5" align="center" alt="Inactivate Assignment" /></a>'
					} else {
						actions += '<a class="assnStatusLink" href="javascript:addOrReactivateVoterAssignment('
							+ row.id + ')"><img src="'+ imgHomePath
							+ '/switch.png" border="0" hspace="5" align="center" alt="Reactivate Assignment" /></a>'
					}
				} else {
					actions += '<img src="'+ imgHomePath + '/spacer.gif" height="21" width="21" hspace="5" align="center" alt="Spacer" />'
				}
				return actions + ' ' + (row.active ? 'Active' : 'Inactive') + '</nobr>'
			} else {
				return row.active ? 'Active' : 'Inactive'
			}
		}
	}]
	
	if (!isReadOnly) {
		assignmentCols[assignmentCols.length] = {
			"targets" : 4,
			"data" : function(row, type, val, meta) {
				var actions = '<div style="margin:0 auto; text-align:center; white-space:nowrap">'
				if ((row.precinct && voterEditSites.search(row.precinct.rootPrecinctId) != -1) && row.hours == 0) {
					actions += '<a class="assnStatusLink" href="javascript:deleteAssignment('
							+ row.id + ')"><img src="' + imgHomePath
							+ '/delete.gif" border="0" hspace="5" align="center" alt="Delete Assignment" /></a>'
				}
				actions += '</div>'
				return actions;
			},
			"sortable" : false
		}
	}
	
	$('#assignmentsTable').DataTable($.extend({}, defaultParms, {
		"columnDefs" : assignmentCols,
    	"dom": '<"top">rt<"bottom"l>',
    	"language" : {
			zeroRecords : "No Assignments",
			infoEmpty: "No assignments available",
			searchPlaceholder : "Search by name..."
		},
		"order" : [[3, 'asc'], [0, 'asc']]
	}))
}

function buildRequirementsTable(defaultParms, isReadOnly) {
	var table = $('#requirementsTable').DataTable($.extend({}, defaultParms, {
		"columns": [
		            { "orderable": false },
		            { "orderable": false },
		            { "orderable": false },
		            { "orderable": false },
		            { "orderable": false }
		          ],
    	"dom": '<"top">rt<"bottom"l>',
    	"language" : {
			zeroRecords : "No Requirements",
			infoEmpty: "No requirements available",
			searchPlaceholder : "Search by name..."
		},
		"order" : [],
		"scrollY" : "250px",
		"stripeClasses" : [ 'odd' ]
	}))
	
}

function buildOrganizationsTable(defaultParms, isReadOnly) {
	var orgCols = [{
		"targets" : 0,
		"data" : function(row, type, val, meta) {
			if (type === 'sort' || type === 'filter') return row.organization.displayName
			return '<a class="appLink" href="' + homePath + '/organizationEdit.htm?id=' + row.organization.id + '">' + row.organization.displayName + '</a>'
		}
	},{
		"targets" : 1,
		"data" : function(row, type, val, meta) {
			if (type === 'sort') {
				return row.organization.precinct ? row.organization.precinct.displayName : 'AAAAAAAAA'
			}
			return row.organization.precinct ? row.organization.precinct.displayName : 'National'
		}
	},{
		"targets" : 2,
		"data" : function(row, type, val, meta) {
			if (type === 'display' && !isReadOnly) {
				var actions = '<nobr>'
				if (row.active) {
					actions += '<a class="assnStatusLink" href="javascript:inactivateOrganization('
						+ row.id + ')"><img src="' + imgHomePath
						+ '/switch.png" border="0" hspace="5" align="center" alt="Inactivate Voter Organization" /></a>'
				} else {
					actions += '<a class="assnStatusLink" href="javascript:reactivateOrganization('
						+ row.organization.id + ')"><img src="'+ imgHomePath
						+ '/switch.png" border="0" hspace="5" align="center" alt="Reactivate Voter Organization" /></a>'
				}
				return actions + ' ' + (row.active ? 'Active' : 'Inactive') + '</nobr>'
			} else {
				return row.active ? 'Active' : 'Inactive'
			}
		}
	}]
	
	if (!isReadOnly) {
		orgCols[orgCols.length] = {
			"targets" : 3,
			"data" : function(row, type, val, meta) {
				var actions = '<div style="margin:0 auto; text-align:center">'
				
				if ((!row.organization.precinct || voterEditSites.search(row.organization.precinct.id) != -1) && row.hours == 0) {
					actions += '<a class="assnStatusLink" href="javascript:deleteOrganization('
							+ row.id + ')"><img src="'+ imgHomePath
							+ '/delete.gif" border="0" hspace="5" align="center" alt="Delete Voter Organization" /></a>'
				}
				actions += '</div>'
				return actions;
			},
			"sortable" : false
		}
	}
	
	$('#organizationsTable').DataTable($.extend({}, defaultParms, {
		"columnDefs" : orgCols,
    	"dom": '<"top">rt<"bottom"l>',
    	"language" : {
			zeroRecords : "No Organizations",
			searchPlaceholder : "Search by name..."
		},
		"stateSave" : false,
		"order" : [[2, 'asc'], [1, 'asc'], [0, 'asc']]
	}))
}

function buildParkingStickersTable(defaultParms, isReadOnly) {
	var parkingCols = [ { // precinct, sticker, license, expires
		"targets" : 0,
		"data" : function(row, type, val, meta) {
			return row.precinct ? row.precinct.displayName : ''
		}
	}, {
		"targets" : 1,
		"data" : function(row, type, val, meta) {
			return row.stickerNumber
		}
	}, {
		"targets" : 2,
		"data" : function(row, type, val, meta) {
			if (row.state == null && row.licensePlate == null) return "(unknown)"
			var stateVal = row.state ? row.state.name : ""
			return stateVal + " ["  + (row.licensePlate ? row.licensePlate : '(unknown)') + "]"
			
		}
	}]
	
	if (!isReadOnly) {
		parkingCols[parkingCols.length] = {
			"targets" : 3,
			"data" : function(row, type, val, meta) {
				var actions = '<div style="margin:0 auto; text-align:center">'
				
				if (row.precinct && voterEditSites.search(row.precinct.id) != -1) {
						actions += '<a href="javascript:showParkingStickerDetailsPopup('
							+ row.id + ')"><img src="' + imgHomePath
							+ '/edit-small.gif" border="0" hspace="5" align="center" alt="Show Parking Sticker Details Popup" /></a>'
					
						actions += '<a href="javascript:deleteParkingSticker('
								+ row.id + ')"><img src="' + imgHomePath
								+ '/delete.gif" border="0" hspace="5" align="center" alt="Delete Parking Sticker" /></a>'
				}
				actions += '</div>'
				return actions
			}
		}
	}
	
	$('#parkingStickersTable').DataTable($.extend({}, defaultParms, {
		"columnDefs" : parkingCols,
    	"dom": '<"top">rt<"bottom"l>',
    	"language" : {
			zeroRecords : "No parking stickers available",
			infoEmpty: "No parking stickers available",
			searchPlaceholder : "Search by name..."
		}
	}))
}

function buildUniformsTable(defaultParms, isReadOnly) {
	var uniformCols = [{ // shirt size, uniform, number of shirts
		"targets" : 0,
		"data" : function(row, type, val, meta) {
			return row.precinct ? row.precinct.displayName : ''
		}
	}, {
		"targets" : 1,
		"data" : function(row, type, val, meta) {
			return row.shirtSize ? row.shirtSize.name : ''
		}
	}, {
		"targets" : 2,
		"data" : function(row, type, val, meta) {
			return row.numberOfShirts
		}
	}]
	
	if (!isReadOnly) {
		uniformCols[uniformCols.length] = {
			"targets" : 3,
			"data" : function(row, type, val, meta) {
				var actions = '<div style="margin:0 auto; text-align:center">'
				
				if (row.precinct && voterEditSites.search(row.precinct.id) != -1) {
					actions += '<a href="javascript:showUniformDetailsPopup('
						+ row.id + ')"><img src="' + imgHomePath
						+ '/edit-small.gif" border="0" hspace="5" align="center" alt="Edit Uniform Details" /></a>'
					actions += '<a href="javascript:deleteUniform('
							+ row.id + ')"><img src="' + imgHomePath
							+ '/delete.gif" border="0" hspace="5" align="center" alt="Delete Uniform" /></a>'
				}
				
				actions += '</div>'
				return actions
			}
		}
	}
	
	$('#uniformsTable').DataTable($.extend({}, defaultParms, {
		"columnDefs" : uniformCols,
    	"dom": '<"top">rt<"bottom"l>',
    	"language" : {
			zeroRecords : "No Uniforms",
			infoEmpty: "No uniforms available",
			searchPlaceholder : "Search..."
		}
	}))
}

function editName() {
	$(".fixedNameFields").hide()
	$(".nameInputs").show()
}

function terminateVoter() {
	$('.terminatedFields').show()
	$('.notTerminatedFields').hide()
}

function refreshOrganizations() {
	$.ajax({
		url : ajaxHomePath + '/voterOrganizations',
		dataType : 'json',
		data : {
			voterId: voterId
		},
		error : commonAjaxErrorHandler,
		success : function(r) {
			var finalItems = []
			for (var i = 0; i < r.organizations.length; i++) {
				var org = r.organizations[i]
				finalItems.push($.extend({}, org, {
					hours : r.hoursByOrganization[org.organization.id] || 0
				}))
			}
			redrawWithRows('organizationsTable', finalItems)
			
			/*
			 * $("#organizationsTable_filter2").val('Active')
			 * $("#organizationsTable_filter2").change()
			 */
			
			var uniqueOrganizations = new SortedArray([])
			var organizations = []
			$(finalItems).each(function(index, item) {
				if (uniqueOrganizations.search(item.organization.id) != -1)
					return
				if (item.active) {
					organizations.push({
						id: item.organization.id,
						name: item.organization.displayName
					})
					uniqueOrganizations.insert(item.organization.id)
				}
			})
			if (r.primaryOrganization && uniqueOrganizations.search(r.primaryOrganization.id) == -1) {
				organizations.push({
					id: r.primaryOrganization.id,
					name: r.primaryOrganization.displayName
				})
			}
			
			var orgSelect = $('#primaryOrganization')
			var orgsAvail = organizations.length > 0
			orgSelect.toggle(orgsAvail)
			$('#noOrganizationsAvailable').toggle(!orgsAvail)
			orgSelect.empty()
			
			if (!r.primaryOrganization) {
				orgSelect.append($('<option>', {
						value : '',
						text : '(Unknown)',
						selected : true
					}))
			}
			
			$(organizations).each(function(index, item) {
				orgSelect.append($('<option>', {
					value : item.id,
					text : item.name,
					selected : r.primaryOrganization && item.id == r.primaryOrganization.id
				}))
			})
		}
	})
}

function deleteParkingSticker(parkingStickerId) {
	confirmDialog('Are you sure you want to delete this parking sticker?', function() {
		$.ajax({
			url : ajaxHomePath + "/voter/deleteParkingSticker",
			type : "POST",
			dataType : 'json',
			data : {
				parkingStickerId : parkingStickerId
			},
			error : commonAjaxErrorHandler,
			success : function(r) {
				refreshParkingStickers()
			}
		})
	})
}

function deleteUniform(uniformId) {
	confirmDialog('Are you sure you want to delete this uniform?', function() {
		$.ajax({
			url : ajaxHomePath + "/voter/deleteUniform",
			type : "POST",
			dataType : 'json',
			data : {
				uniformId : uniformId
			},
			error : commonAjaxErrorHandler,
			success : function(r) {
				refreshUniforms()
			}
		})
	})
}

function assignmentUpdateCompleteHandler(r) {
	if (r.voterStatusChanged) {
		$(".buttonAnchor").button("disable")
		$("#submitButton").button("disable")
		$(".assnStatusLink").on("click", function(e) { e.preventDefault(); })
		showSpinner('Please wait, updating voter status...', true)
		jumpToVoter(voterId)
		return
	}
	
	refreshAssignments()
	refreshRequirements()
}

function inactivateAssignment(voterAssignmentId) {
	confirmDialog('Are you sure you want to inactivate this assignment?', function() {
		$.ajax({
			url : ajaxHomePath + "/voter/inactivateVoterAssignment",
			type : "POST",
			dataType : 'json',
			data : {
				voterAssignmentId : voterAssignmentId
			},
			error : commonAjaxErrorHandler,
			success : assignmentUpdateCompleteHandler
		})
	})
}

function deleteAssignment(voterAssignmentId) {
	confirmDialog('Are you sure you want to delete this assignment?', function() {
		$.ajax({
			url : ajaxHomePath + "/voter/deleteVoterAssignment",
			type : "POST",
			dataType : 'json',
			data : {
				voterAssignmentId : voterAssignmentId
			},
			error : commonAjaxErrorHandler,
			success : assignmentUpdateCompleteHandler
		})
	})
}

function deleteRequirement(voterRequirementId) {
	confirmDialog('Are you sure you want to delete this requirement?', function() {
		$.ajax({
			url : ajaxHomePath + "/voter/deleteVoterRequirement",
			type : "POST",
			dataType : 'json',
			data : {
				voterRequirementId : voterRequirementId
			},
			error : commonAjaxErrorHandler,
			success : function(r) {
				/*
				 * if (r) { displayAttentionDialog('The primary precinct for
				 * this voter was inactivated; please choose another primary
				 * precinct.') }
				 */
				refreshRequirements()
			}
		})
	})
}

function organizationUpdateCompleteHandler(r) {
	if (r.voterStatusChanged) {
		document.location.href = homePath + '/voterEdit.htm?id=' + voterId
		return
	}
	
	if (r.primaryOrgInactivated) {
		displayAttentionDialog('The primary organization for this voter was inactivated; please choose another primary organization.')
	}
	refreshOrganizations()
}

function inactivateOrganization(voterOrganizationId) {
	confirmDialog('Are you sure you want to inactivate this organization?', function() {
		$.ajax({
			url : ajaxHomePath + "/voter/inactivateOrganization",
			type : "POST",
			dataType : 'json',
			data : {
				voterOrganizationId : voterOrganizationId
			},
			error : commonAjaxErrorHandler,
			success : organizationUpdateCompleteHandler
		})
	})
}

function deleteOrganization(voterOrganizationId) {
	confirmDialog('Are you sure you want to delete this organization?', function() {
		$.ajax({
			url : ajaxHomePath + "/voter/deleteOrganization",
			type : "POST",
			dataType : 'json',
			data : {
				voterOrganizationId : voterOrganizationId
			},
			error : commonAjaxErrorHandler,
			success : organizationUpdateCompleteHandler
		})
	})
}


function reactivateOrganization(theId) {
	addOrReactivateOrganization({ id : theId })
}

function addOrReactivateOrganization(organizationObj) {
	$.ajax({
		url : ajaxHomePath + "/voter/addOrganization",
		type : "POST",
		dataType : 'json',
		data : {
			voterId : voterId,
			organizationId : organizationObj.id
		},
		error : commonAjaxErrorHandler,
		success : organizationUpdateCompleteHandler
	})
}

function addOrReactivateVoterAssignment(assignmentObjOrId) {
	var paramData = {
		voterAssignmentId : assignmentObjOrId
	}
	
	if (assignmentObjOrId.benefitingServiceRole) {
		paramData = {
			voterId : voterId,
			benefitingServiceRoleId : assignmentObjOrId.benefitingServiceRole.id
		}
	}
	
	$.ajax({
		url : ajaxHomePath + "/voter/addOrReactivateVoterAssignment",
		type : "POST",
		dataType : 'json',
		data : paramData,
		error : commonAjaxErrorHandler,
		success : assignmentUpdateCompleteHandler
	})
}

function addOrReactivateVoterRequirement(requirementObjOrId) {
	var paramData = {
		voterRequirementId : requirementObjOrId
	}
	
	if (requirementObjOrId.requirement) {
	paramData = {
			voterId : voterId,
			requirementId : assignmentObjOrId.requirement.id
		}		
	}
	
	$.ajax({
		url : ajaxHomePath + "/voter/addOrReactivateVoterRequirement",
		type : "POST",
		dataType : 'json',
		data : paramData,
		error : commonAjaxErrorHandler,
		success : function(r) {
			refreshRequirements()
		}
	})
}

function setPrimaryOrganization(organizationId) {
	$.ajax({
		url : ajaxHomePath + "/voter/setPrimaryOrganization",
		type : "POST",
		dataType : 'json',
		data : {
			voterId : voterId,
			organizationId : organizationId
		},
		error : commonAjaxErrorHandler,
		success : function(result) {
			displayAttentionDialog('The primary organization was updated successfully.')
			refreshOrganizations()
		}
	})
}

function setPrimaryPrecinct(precinctId) {
	$.ajax({
		url : ajaxHomePath + "/voter/setPrimaryPrecinct",
		type : "POST",
		dataType : 'json',
		data : {
			voterId : voterId,
			precinctId : precinctId
		},
		error : commonAjaxErrorHandler,
		success : function(result) {
			displayAttentionDialog('The primary precinct was updated successfully.')
			refreshAssignments()
		}
	})
}

var allVoterAssignments = undefined
function refreshAssignments() {
	$.ajax({
		url : ajaxHomePath + '/voterAssignments',
		dataType : 'json',
		data : {
			voterId: voterId
		},
		error : commonAjaxErrorHandler,
		success : function(r) {
			var finalItems = []
			for (var i = 0; i < r.assignments.length; i++) {
				var assn = r.assignments[i]
				finalItems.push($.extend({}, assn, {
					hours : r.hoursByAssignment[assn.id] || 0
				}))
			}
			allVoterAssignments = finalItems
			redrawWithRows('assignmentsTable', finalItems)
			
			/*
			 * $("#assignmentsTable_filter3").val('Active')
			 * $("#assignmentsTable_filter3").change()
			 */
			
			var uniquePrecincts = new SortedArray([])
			var precincts = []
			if (r.primaryPrecinct) {
				precincts.push({
					id: r.primaryPrecinct.id,
					name: r.primaryPrecinct.displayName
				})
				uniquePrecincts.insert(r.primaryPrecinct.id)
			}
			$(r.assignments).each(function(index, item) {
				if (uniquePrecincts.search(item.precinct.rootPrecinctId) != -1)
					return
					
				precincts.push({
					id: item.precinct.rootPrecinctId,
					name: item.precinct.rootPrecinctDisplayName
				})
				uniquePrecincts.insert(item.precinct.rootPrecinctId)
			})
			
			var facSelect = $('#primaryPrecinct')
			var precinctsAvail = precincts.length > 0
			facSelect.toggle(precinctsAvail)
			$('#noPrecinctsAvailable').toggle(!precinctsAvail)
			facSelect.empty()
			
			if (!r.primaryPrecinct) {
				facSelect.append($('<option>', {
					value : '',
					text : '(Unknown)',
					selected : true
				}))
			}
			
			$(precincts).each(function(index, item) {
				facSelect.append($('<option>', {
					value : item.id,
					text : item.name,
					selected : r.primaryPrecinct && item.id == r.primaryPrecinct.id
				}))
			})
		}
	})
}

function hasActiveAssignmentAtPrecinct(precinctId) {
	if (typeof allVoterAssignments == 'undefined') {
		alert('There was an error retrieving / processing assignments for this voter')
		return
	}
	
	for (var i = 0; i < allVoterAssignments.length; i++) {
		var item = allVoterAssignments[i]
		if (('' + item.precinct.rootPrecinctId) == precinctId && item.active)
			return true
	}
	return false
}

voterRequirementData = {}
voterRequirementsByScope = null
voterAssignmentData = {}

function refreshRequirements() {
	var table = $('#requirementsTable').DataTable()
	table.clear()
	
	$.ajax({
		url : ajaxHomePath + '/voterRequirements',
		dataType : 'json',
		data : {
			voterId: voterId
		},
		error : commonAjaxErrorHandler,
		success : function(r) {
			
			voterRequirementData = {}
			$(r.allRequirements).each(function(index, item) {
				voterRequirementData[item.id] = item
			})
			
			voterAssignmentData = {}
			$(r.allVoterAssignments).each(function(index, item) {
				voterAssignmentData[item.id] = item
			})
			
			voterRequirementsByScope = r.requirementsByScope
			
			var rows = []
			if (r.requirementsByScope.globalAll.length > 0)
				table.row
					.add($('<tr class="groupRow"><td colspan="5">Global Requirements</td></tr>')[0])
					.child(getRequirementChildRows(r.requirementsByScope.globalAll)).show()
			if (r.requirementsByScope.precinctAll.length > 0)
				table.row
					.add($('<tr class="groupRow"><td colspan="5">Precinct Requirements</td></tr>')[0])
					.child(getRequirementChildRows(r.requirementsByScope.precinctAll)).show()
			
			$.each(r.requirementsByScope.byAssignment, function(volAssignmentId, volReqs) {
				table.row
					.add($('<tr class="groupRow"><td colspan="5">' + escapeHTML(voterAssignmentData[volAssignmentId].displayName) + '</td></tr>')[0])
					.child(getRequirementChildRows(volReqs)).show()
			})
			
			table.columns.adjust().draw()
			$(window).resize()
		}
	})
}

function getRequirementChildRows(volReqs) {
	var childRows = []
	for (var j = 0; volReqs && j < volReqs.length; j++) {
		var volReq = volReqs[j]
		var childRow = $('<tr valign="top" />')

		var canEdit = (volReq.requirement.precinctScope == null || voterEditSites.search(volReq.requirement.precinctScope.rootPrecinctId) != -1)
		
		var nameTd = ''
		if (canEdit) {
			nameTd = $('<td><ul><li><a class="appLink" href="javascript:showVoterRequirementPopup('
				+ volReq.id + ')"></a></li></ul></td>')
			nameTd.find('a').attr('title', volReq.comments).attr('tabindex', '0').text(volReq.requirement.name)
		} else {
			nameTd = $('<td><ul><li></li></ul></td>')
			nameTd.find('li').attr('title', volReq.comments).attr('tabindex', '0').text(volReq.requirement.name)
		}
		nameTd.appendTo(childRow)
		
		var appType = volReq.requirement.applicationType
		var appTypeDesc = appType == reqAppTypeAllVoters ? 'All Voters' :
			appType == reqAppTypeRoleType ? 'Role Type "' + volReq.requirement.roleType.name + '"' :
			appType == reqAppTypeSpecificRoles ? 'Specific Roles' : '(Unknown)'
		
		var precinct = volReq.requirement.precinctScope
		$('<td></td>').text(appTypeDesc + (precinct != null ? ' at ' + precinct.displayName : '')).appendTo(childRow)
		
		var hasDateType = (volReq.requirement.dateType != null)
		var hasReqDate = (volReq.requirementDate != null)
		
		var severity = null
		var isExpired = false
		var altText = ''
		if (volReq.status.id == requirementNotApplicableStatusId) {
			// noop
		} else {
			var daysDiff = null
			if (volReq.requirement.dateType.skipNotification == false) {
				reqDateObj = getDateFromMMDDYYYY(volReq.requirementDate)
				if (reqDateObj) {
					daysDiff = daysBetween(getTodayWithoutTime(), reqDateObj)
					isExpired = (daysDiff <= 0)
				}
			}
			
			if (volReq.status.id != requirementMetStatusId || isExpired) {
				severity = 'high'
				altText = 'This voter does not meet the requirement'
			} else if (daysDiff && volReq.requirement.daysNotification && daysDiff <= volReq.requirement.daysNotification) {
				severity = 'medium'
				altText = 'This requirement is within the expiration period'
			}
		}
		
		var s = ''
		s += hasDateType ? escapeHTML(volReq.requirement.dateType.name) : '&nbsp;'
		if (hasDateType && hasReqDate) s += ':<br>'
		s += (hasReqDate ? escapeHTML(volReq.requirementDate) : '&nbsp;')
		
		var dateTd = $('<td nowrap></td>').html(s)
		
		dateTd.appendTo(childRow)
		
		var statusText = isExpired ? 'Expired' : volReq.status ? escapeHTML(volReq.status.name) : ''
		var statusTd = $('<td nowrap></td>').append($('<span></span>').text(statusText))
		statusTd.appendTo(childRow)
		
		var iconTd = $('<td nowrap></td>').appendTo(childRow)
		if (severity != null) {
			$('<img align="absmiddle" src="' + imgHomePath + '/decal-small-' + severity + '.png" alt="' + altText + '" tabindex="0" /> ')
				.appendTo(iconTd)
		}
		
		childRows.push(childRow)
	}
	
	return childRows
}

var parkingStickerData = {}
function refreshParkingStickers() {
	$.ajax({
		url : ajaxHomePath + '/voterParkingStickers',
		dataType : 'json',
		data : {
			voterId: voterId
		},
		error : commonAjaxErrorHandler,
		success : function(r) {
			parkingStickerData = {}
			$(r).each(function(index, item) {
				parkingStickerData[item.id] = item
			})
			redrawWithRows('parkingStickersTable', r)
		}
	})
}

var uniformData = {}
function refreshUniforms() {
	$.ajax({
		url : ajaxHomePath + '/voterUniforms',
		dataType : 'json',
		data : {
			voterId: voterId
		},
		error : commonAjaxErrorHandler,
		success : function(r) {
			uniformData = {}
			$(r).each(function(index, item) {
				uniformData[item.id] = item
			})
			redrawWithRows('uniformsTable', r)
		}
	})
}

function redrawWithRows(tableId, rows) {
	var table = $('#' + tableId).DataTable()
	table.clear()
	table.rows.add(rows)
	rebuildTableFilters(tableId)
	table.draw()
}

function toggleFields(className, val) {
	$('.' + className).toggle(val)
	$('#show_' + className).toggle(!val)
	$('#hide_' + className).toggle(val)
}

function validate() {
	var allErrors = new Array()

	var firstName = $("#firstNameInput").val()
	var lastName = $("#lastNameInput").val()
	if ($.trim(firstName) == '' || $.trim(lastName) == '') {
		allErrors.push('Please enter both first name and last name.')
	}
	
	var dobVal = $.trim($("#dateOfBirthInput").val())
	if (dobVal == '' || !validateDate(dobVal)) {
		allErrors.push('Please enter a valid date of birth.')
	}
	
	var gender = $("#genderSelect").val()
	if (gender == '') {
		allErrors.push('Please enter the gender.')
	}
	
	var addressLine1 = $.trim($("#addressLine1").val())
	if ($.trim(addressLine1) == '') {
		allErrors.push('Please enter the first line of the address.')
	}
	
	var addressCity = $.trim($("#addressCity").val())
	if ($.trim(addressCity) == '') {
		allErrors.push('Please enter the address city.')
	}
	
	var addressState = $("#stateSelect").val()
	if (addressState == '') {
		allErrors.push('Please enter the address state.')
	}
	
	var addressZip = $.trim($("#addressZip").val())
	if ($.trim(addressZip) == '') {
		allErrors.push('Please enter the address zip code.')
	}
			
	if (allErrors.length > 0)
		displayAttentionDialog("Please correct the following errors: <ul><li>"
				+ allErrors.join("</li><li>") + "</li></ul>");

	return allErrors.length == 0
}

var leieMatches = null

function submitForm(isEdit, voterId) {
	doubleClickSafeguard($("#submitButton"))
	
	if (!validate())
		return false
	
	leieMatches = null
	
	var firstName = $("#firstNameInput").val()
	var lastName = $("#lastNameInput").val()
	var dobVal = $("#dateOfBirthInput").val()
	
	/*
	 * If all the params on this submission are the same as they were when we
	 * loaded the page (including LEIE override and whether we have a
	 * termination date), no need to search for duplicates or run LEIE check
	 * since the data didn't change. CPB
	 */
	if (isEdit && firstName == commandFirstName && lastName == commandLastName
			&& removeChars(dobVal, ['-', '/']) == getAsMMDDYYYY(commandDob))
		return true
	
	if (isEdit && $("#terminationDate").val() != '') {
		/*
		 * No need to check for duplicates or LEIE matches if we're terminating
		 * the guy anyway
		 */
		return true
	}
	
	$("#submitButton").val('Validating Information...')
	$("#submitButton").prop('disabled', true)
	
	$.ajax({
		url : ajaxHomePath + '/voter/preSubmitChecks',
		dataType : 'json',
		data : {
			firstName: firstName,
			lastName: lastName,
			dob: dobVal,
			voterId : isEdit ? voterId : null
		},
		error : commonAjaxErrorHandler,
		success : function(r) {
			var potentialDuplicates = r.potentialDuplicates
			leieMatches = r.leieMatches || []
			
			if (potentialDuplicates.length == 0) {
				$("#submitButton").val('Submitting form...')
				finalSubmit()
				return
			}
			
			$("#submitButton").val(isEdit ? 'Submit' : 'Create')
			$("#submitButton").prop('disabled', false)				
			popupVoterSearch('volProfCheckForDups', potentialDuplicates, {
				submitButtonStr : isEdit ? 'Update Anyway' : 'Create Anyway'
			})
		}
	})
	
	return false
}

function finalSubmit() {
	var submitForm = function() {
		$("#voterForm")[0].submit()
	}
	if (leieMatches != null && leieMatches.length > 0) {
		$("#submitButton").val('Create')
		$("#submitButton").prop('disabled', false)
		
		var firstMatch = leieMatches[0]
		
		var msg = 'LEIE match found, do you wish to continue?<p>'
			+ '<table style="max-width:460">'
			+ '	<tr><td>'
			+ '		<table cellpadding="3" border="1">'
			+ '			<tr><td align="right">Name:</td><td>' + firstMatch.displayName + '</td></tr>'
			+ '			<tr><td align="right" nowrap>Date of Birth:</td><td>' + firstMatch.dob + '</td></tr>'
			+ '			<tr><td align="right">General:</td><td>' + (firstMatch.general || '') + '</td></tr>'
			+ '			<tr><td align="right">Specialty:</td><td>' + (firstMatch.specialty || '') + '</td></tr>'
			+ '			<tr><td align="right">Address:</td><td>' + (firstMatch.addressMultilineDisplay || '') + '</td></tr>'
			+ '			<tr><td align="right" nowrap>Exclusion Date:</td><td>' + (firstMatch.exclusionDate || '') + '</td></tr>'
			+ '			<tr><td align="right" nowrap>Reinstate Date:</td><td>' + (firstMatch.reinstateDate || '') + '</td></tr>'
			+ '			<tr><td align="right" nowrap>Waiver Date:</td><td>' + (firstMatch.waiverDate || '') + '</td></tr>'
			+ '			<tr><td align="right" nowrap>Waiver State:</td><td>' + (firstMatch.waiverState || '') + '</td></tr>'
			+ '			<tr><td align="right" nowrap>Exclusion Type:</td><td>' + (firstMatch.exclusionType ? firstMatch.exclusionType.displayName : '') + '</td></tr>'
			+ '		</table>'
			+ '	</td></tr>'
			+ '</table>'
			
		confirmDialog(msg, submitForm, {
			width: 500,
			height: 500
		})
	} else {
		submitForm()
	}
}