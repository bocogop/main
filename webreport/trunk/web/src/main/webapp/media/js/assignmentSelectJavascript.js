function popupAssignmentSelect(uid) {
	$("#assignmentSelectDialog" + uid).dialog('open')
	
	if ($("#assignmentSelectDialog" + uid).data('stationsPopulated')) {
		$("#assignmentPrecinctId").val(precinctContextId)
		$("#assignmentPrecinctId").multiselect("refresh")
	}
	
	refreshAssignmentsTable(uid)
}

function refreshAssignmentsTable(uid) {
	$.ajax({
		url : ajaxHomePath + "/findAvailableAssignments",
		type : "POST",
		dataType : 'json',
		data : {
			precinctId : $("#assignmentPrecinctId").val()
		},
		error : commonAjaxErrorHandler,
		success : function(results) {
			processAssignmentSelectResults(uid, results)
		}
	})
}

function assignmentSelectPopupItemSelected(uid, assignmentId) {
	var assignmentObj = assignmentSelectResults[uid]['' + assignmentId]
	var theDialog = $("#assignmentSelectDialog" + uid)
	theDialog.dialog('close')
	
	var callbackMethod = theDialog.data('callbackMethod')
	if (callbackMethod)
		callbackMethod(assignmentObj)
}

function initAssignmentSelectPopup(options) {
	var uid = options.uid
	var callbackMethod = options.callbackMethod
	var volEditPermission = options.voterEditPermission
	
	var parms = {
		"columnDefs" : [
				{
					"targets" : 0,
					"data" : function(row, type, val, meta) {
						var displayNameEscaped = ''
						if (row.benefitingService) {
							displayNameEscaped = row.benefitingService.name
							if (row.benefitingService.subdivision)
								displayNameEscaped += ' - ' + row.benefitingService.subdivision
						}
						displayNameEscaped = escapeHTML(displayNameEscaped)
						
						if (type === 'display') {
							return '<a class="appLink" href="javascript:assignmentSelectPopupItemSelected(\''
									+ uid + '\', ' + row.id + ')">'
									+ displayNameEscaped + '</a>'
						} else {
							return displayNameEscaped
						}
					}
				},{
					"targets" : 1,
					"data" : function(row, type, val, meta) {
						return escapeHTML(row.benefitingServiceRole ? row.benefitingServiceRole.name : '')
					}
				},
				{
					"targets" : 2,
					"data" : function(row, type, val, meta) {
						return escapeHTML(row.benefitingServiceRole ? row.benefitingServiceRole.locationDisplayName : '')
					}
				}],
		"dom" : '<"top"fi>rt<"bottom"pl><"clear">',
		"pagingType" : "full_numbers",
		"pageLength" : 10,
		"lengthMenu" : [ [ 10, -1 ], [ 10, "All" ] ],
		"stateSave" : false
	}

	var theDataTable = $('#assignmentSelectList' + uid).DataTable(parms)
	var dialogEl = $("#assignmentSelectDialog" + uid)
	
	var precinctEl = $("#assignmentPrecinctId")
	precinctEl.change(function() {
		refreshAssignmentsTable(uid)
	})
	precinctEl.multiselect({
		selectedText : function(numChecked, numTotal, checkedItems) {
			return abbreviate($(checkedItems[0]).next().text())
		},
		beforeopen: function(){
			if (dialogEl.data('stationsPopulated')) return
			var curVal = precinctEl.val()
			
			$.ajax({
				url : ajaxHomePath + "/getPrecinctsWithUserPermission",
				type : "POST",
				data : {
					permission : volEditPermission,
					activeStatus : true
				},
				dataType : 'json',
				error : commonAjaxErrorHandler,
				success : function(results) {
					precinctEl.empty()
					var newHtml = []
					$.each(results, function(index, item) {
						if (item.id == centralOfficeId) return
						
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
	
	dialogEl.dialog({
		autoOpen : false,
		modal : false,
		width : 800,
		height : 450,
		closeOnEscape : true,
		draggable : true,
		resizable : true,
		buttons : {
			'Cancel' :  function() { $(this).dialog('close') }
		}
	})
	dialogEl.data('callbackMethod', callbackMethod)
	dialogEl.show()
}

var assignmentSelectResults = new Object()

function processAssignmentSelectResults(uid, results) {
	var dialogEl = $("#assignmentSelectDialog" + uid)
	
	var resultMap = new Object()

	var table = $('#assignmentSelectList' + uid).DataTable()
	table.clear()

	for (var i = 0; i < results.length; i++) {
		resultMap['' + results[i].id] = results[i]
		table.row.add(results[i])
	}

	assignmentSelectResults[uid] = resultMap

	table.search('').columns().search('')
	rebuildTableFilters('assignmentSelectList' + uid)
	
	table.draw()
}