$(function() {
	$("input:submit, a.buttonAnchor, a.submitAnchor, button").button()
	
	initCustomizeDialog()
	$("#customize").click(showCustomizeDialog)
	
	$(".userInput").change(function() {
		update(false)
	})
	
	$("#setDefaultLink").click(function() {
		var optionsSelected = $('#stations option:selected')
		if (optionsSelected.length == 0 || optionsSelected.length > 1) {
			displayAttentionDialog("Please select one precinct to set as the default.")
			return
		}
		
		$("#defaultPrecinctId").val(optionsSelected[0].value)
		$('#stations option:selected').prop("selected", false)
		update(false)
	})
	
	$(".manageSelfOnly").toggle(!hasUMPermission)
	$(".manageAll").toggle(hasUMPermission)
	
	$("#userTable").DataTable({
        "ajax": ajaxHomePath + "/appUser/quickSearch",
        "columns": [
            { "data": "username",
            	"render": function (data, type, full, meta) {
	    	      return '<a href=\'javascript:selectUserWithFields('+ full.id + ', "' + full.username +
	    	      	'", "' + full.displayName + '")\'>' + full.displayName + ' (' + full.username + ')</a>'
	    	    }
            }
        ],
        "dom" : '<"top"f>rt<"bottom"l>',
        "language": {
            zeroRecords: "", //Please search above by name or VA username.",
            search: "", // Search",
            searchPlaceholder: "Search by name or VA username..."
        },
        "processing": true,
        "scrollY" : 140,
        "serverSide": true
    })    
})

// --------------------------------------- Roles and Precincts functions

var allRoles = []
var allPrecincts = []

function getAllRoles(callback) {
	if (allRoles.length == 0) {
		loadAllRolesAndPrecincts(function() {
			callback(allRoles)
		})
	} else {
		callback(allRoles)
	}
}

function setAllRolesIfNeeded(roleGenerator) {
	if (allRoles.length == 0) {
		allRoles = roleGenerator()
	}
}

function getAllPrecincts(callback) {
	if (allPrecincts.length == 0) {
		loadAllRolesAndPrecincts(function() {
			callback(allPrecincts)
		})
	} else {
		callback(allPrecincts)
	}
}

function setAllPrecinctsIfNeeded(precinctGenerator) {
	if (allPrecincts.length == 0) {
		allPrecincts = precinctGenerator()
	}
}

function loadAllRolesAndPrecincts(callback) {
	alert('AJAX call not implemented/needed yet')
}

//--------------------------------------- Main user functions

var currentUser = null

function refreshUser(includeRolesAndPrecincts) {
	var userSelected = currentUser != null
	$(".pleaseSelect").toggle(!userSelected)
	
	$(".manageSelfOnly").toggle(!hasUMPermission)
	$(".manageAll").toggle(hasUMPermission)
	
	if (!userSelected) {
		$(".userFields").css({visibility: "hidden"})
		return
	}
	
	$(".userFields").css({visibility: "visible"})
	
	$.ajax({
		url : ajaxHomePath + "/appUser",
		type : "GET",
		dataType : 'json',
		data : {
			userId : currentUser.id,
			includeRolesAndPrecincts : includeRolesAndPrecincts
		},
		error : commonAjaxErrorHandler,
		success : setUserFields
	})
}

function setUserFields(resultMap) {
	var userResult = resultMap.user
	var updateRoles = resultMap.updateRoles
	
	$("#userID").text(userResult.username)
	$("#userName").text(userResult.displayName)
	$("#userPhone").text(userResult.telephoneNumber)
	
	var emailHtml = ''
	if (userResult.email != '') {
		emailHtml = escapeHTML(userResult.email) + '<a href="mailto:'
			+ escapeHTML(userResult.email)
			+ '"><img alt="Click to email '
			+ escapeHTML(userResult.email) + '"' + 'src="' + imgHomePath
			+ '/envelope.jpg" height="14"'
			+ ' width="18" border="0" align="absmiddle"'
			+ ' style="padding-left: 4px; padding-right: 4px" /></a>'
	}
	
	$("#userEmail").html(emailHtml)
	$("#userEnabled").prop('checked', userResult.enabled);
	$("#userEnabledText").text(userResult.enabled ? 'Yes' : 'No');
	$("#userExpired").prop('checked', userResult.inactive)
	$("#userExpiredRow").toggle(userResult.inactive)
	$("#userLocked").prop('checked', userResult.accountLockDate != null);
	$("#userLockedText").text(userResult.accountLockDate != null ? 'Yes' : 'No');
	
	if (userResult.timeZone) {
		$("#timeZoneSelect").val(userResult.timeZone.id)
	}
	
	var defaultPrecinct = resultMap.defaultPrecinct
	if (defaultPrecinct) {
		$("#defaultPrecinctId").val(defaultPrecinct.id)
		$(".defaultPrecinctText").text(defaultPrecinct.displayName)
	} else {
		$("#defaultPrecinctId").val("")
		$(".defaultPrecinctText").text("(none set)")
	}
	
	if (updateRoles) {
		$("#roles").empty()
		$.each(userResult.globalRolesSorted, function(index, appUserGlobalRole) {
			var role = appUserGlobalRole.role
			$("#roles").append($('<option></option>') //
					.val(role.id) //
					.html(role.name))
		})
		
		$("#available_roles").empty()
		$.each(resultMap.availableRoles, function(index, role) {   
			$("#available_roles").append($('<option></option>') //
					.val(role.id) //
					.html(role.name))
		})
		
		cacheAllRoles(resultMap)
		
		$("#customizeDiv").show()
	}
}

function newAppUserSelectedCallback(appUserObj) {
	$.ajax({
		url : ajaxHomePath + "/appUser/add",
		type : "POST",
		dataType : 'json',
		data : {
			activeDirectoryName : appUserObj.username
		},
		error : commonAjaxErrorHandler,
		success : function(result) {
			selectUser(result)
		}
	})
}

function selectUserWithFields(id, username, displayName) {
	selectUser({
		id: id,
		username: username,
		displayName: displayName
	})
}

function selectUser(userObj) {
	var table = $('#userTable').DataTable()
	table.clear()
	table.draw()
	
	if (userObj) {
		table.row.add(userObj).draw()
	}
	currentUser = userObj
	refreshUser(true)
}

var precinctsToAdd = []
var precinctsToRemove = []

function moveStations(isAdd, addAll, removeAll) {
	precinctsToAdd = []
	$('#available_stations option').each(function(i) {
    	if (isAdd && (addAll || $(this).is(":selected")))
    		precinctsToAdd.push(this.value)
    })
	
	precinctsToRemove = []
	var removingDefaultPrecinct = false
	var currentDefaultPrecinct = $("#defaultPrecinctId").val()
    $('#stations option').each(function(i) {
    	if (!isAdd && (removeAll || $(this).is(":selected"))) {
    		precinctsToRemove.push(this.value)
    		if (this.value == currentDefaultPrecinct)
    			removingDefaultPrecinct = true
    	}
    })
    
    if (precinctsToAdd.length == 0 && precinctsToRemove.length == 0)
    	return
    
    if (removingDefaultPrecinct) {
    	confirmDialog("Are you sure you want to remove the default precinct?<p />Another default precinct will need to be selected.",
        	function() {
        		update(true)
        	})
    } else {
    	update(true)
    }
}

function removeUser() {
	var userSelected = currentUser != null
	if (!userSelected) return
	confirmDialog('Remove user "' + currentUser.displayName + '"?', function() {
		$.ajax({
			url : ajaxHomePath + "/appUser/remove",
			type : "GET",
			dataType : 'json',
			data : {
				appUserId : currentUser.id
			},
			error : commonAjaxErrorHandler,
			success : function(result) {
				if (!result) {
					displayAttentionDialog('Sorry, the user could not be deleted.')
				} else {
					displayAttentionDialog('The user "' + currentUser.displayName + '" was deleted successfully.')
					selectUser()
				}
			}
		})
	})
}

function cacheAllRoles(resultMap) {
	var userResult = resultMap.user
	setAllRolesIfNeeded(function() {
		var allRoles = new SortedArray([], function(a, b) {
			if (a === b) return 0
			if (a.name < b.name) return -1
			return 1
		})
		$.each(userResult.globalRolesSorted, function(index, appUserGlobalRole) {
			var role = appUserGlobalRole.role
			allRoles.insert({
				id: role.id,
				name: role.name
			})
		})
		$.each(resultMap.availableRoles, function(index, role) {   
			allRoles.insert({
				id: role.id,
				name: role.name
			})
		})
		return allRoles.array
	})
}

function update(updateRoles) {
	var roles = []
	if (updateRoles)
        $('#roles option').each(function(i) {
        	roles.push(this.value)
        })
    
	$.ajax({
		url : ajaxHomePath + "/appUser/update",
		type : "POST",
		dataType : 'json',
		data : {
			userId : currentUser.id,
			enabled: $("#userEnabled").is(':checked'),
			expired: $("#userExpired").is(':checked'),
			locked: $("#userLocked").is(':checked'),
			timezone: $("#timeZoneSelect").val(),
			globalRoles: roles.join(),
			updateRoles: updateRoles
		},
		error : function(jqXHR, textStatus, errorThrown) { 
			refreshUser(updateRoles)
			commonAjaxErrorHandler(jqXHR, textStatus, errorThrown)
		},
		success : function(resultMap) {
			refreshUser(updateRoles)
		}
	})
}

function initCustomizeDialog() {
	$("#customizeDialog").dialog({
		autoOpen: false,
		modal: true,
		show: 'slide',
		draggable: true,
		resizable:true,
		width: 600,
		buttons: {
			OK : function() {
				$(this).dialog('close')
				
				var roles = []
		        $('#cust_roles option').each(function(i) {
		        	roles.push(this.value)
		        })
		        
		        var precincts = []
		        $('#cust_stations option').each(function(i) {
		        	precincts.push(this.value)
		        })
				
				$.ajax({
					url : ajaxHomePath + "/appUser/customize",
					type : "POST",
					dataType : 'json',
					data : {
						userId : currentUser.id,
						roles: roles.join(),
						precincts: precincts.join()
					},
					error : function(jqXHR, textStatus, errorThrown) { 
						refreshUser(true)
						commonAjaxErrorHandler(jqXHR, textStatus, errorThrown)
					},
					success : function(resultMap) {
						refreshUser(true)
					}
				})
			},
			Cancel: function() {
				$(this).dialog('close')
			}
		}
	})
}

function showCustomizeDialog() {
	$("#available_cust_roles, #cust_roles, #available_cust_stations, #cust_stations").empty()
	getAllRoles(function(allRoles) {
		var newOptionHtml = []
		$.each(allRoles, function(index, r) {
			/*
			 * Don't let them customize the national admin - it's either global
			 * or none - CPB
			 */
			if (nationalAdminId == r.id) return
			
			newOptionHtml.push('<option value="' + r.id + '">' + 
					escapeHTML(r.name) + '</option>')
		})
		$("#available_cust_roles").html(newOptionHtml.join(''))
	})
	getAllPrecincts(function(allPrecincts) {
		var newOptionHtml = []
		$.each(allPrecincts, function(index, f) {
			/*
			 * Don't let them customize the central office - it's either global
			 * or none - CPB
			 */
			if (centralOfficePrecinctId == f.id) return
			
			newOptionHtml.push('<option value="' + f.id + '">' + 
					abbreviate(f.name) + '</option>')
		})
		$("#available_cust_stations").html(newOptionHtml.join(''))
	})
	rebuildSelectFilters(['available_cust_stations', 'cust_stations'])
	$("#customizeDialog").dialog("open")
}

function moveItem(itemName, moveType, runUpdate) {
	var isAdd = moveType == 'add' || moveType == 'addAll'
	var isAll = moveType == 'addAll' || moveType == 'removeAll'
	
	fromBox = document.getElementById(isAdd ? "available_"
			+ itemName : itemName)
	toBox = document.getElementById(isAdd ? itemName
			: "available_" + itemName)
	
	var newOptions = []
			
	var wasChanged = false
	for (var i = fromBox.options.length - 1; i >= 0; i--) {
		var o = fromBox.options[i]
		if (o.selected || isAll) {
			wasChanged = true
			newOptions.push(new Option(o.text, o.value))
			fromBox.options[i] = null
		}
	}
	
	for (var i = 0; i < newOptions.length; i++)
		toBox.options[toBox.options.length] = newOptions[i]
	
	if (runUpdate)
		update(true)
}

function rebuildSelectFilters(itemIdArray, filterListOptions) {
	$.each(itemIdArray, function(i, o) {
		var filter = new filterlist($('#' + o)[0], filterListOptions)
		
		$('#' + o + '_filter').unbind('keyup')
		$('#' + o + '_filter').bind('keyup', function() {
			filter.set($(this).val())
		})
		filter.set($('#' + o + '_filter').val())
	})
}

function abbreviate(str) {
	if (str.length > 35)
	    return str.substr(0, 20) + '...' + str.substr(str.length-10, str.length);
	return str;
}

//For making "Set Default" and "Customze..." buttons 508 Compliant 
function doNothing() {
	
}