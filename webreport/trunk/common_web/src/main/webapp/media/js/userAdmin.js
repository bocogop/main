function loadUserAdmin() {
	$("input:submit, a.buttonAnchor, a.submitAnchor, button").button()
	
	$(".userInput").change(function() {
		update(false)
	})
	
	$(".manageSelfOnly").toggle(!hasUMPermission)
	$(".manageAll").toggle(hasUMPermission)
	
	buildUserTable()
    buildUserFieldsDialog()
}

function buildUserTable() {
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
            zeroRecords: "", // Please search above by name or username.",
            search: "", // Search",
            searchPlaceholder: "Search by name or username..."
        },
        "processing": true,
        "scrollY" : 120,
        "serverSide": true
    })
}

//--------------------------------------- User popup

function buildUserFieldsDialog() {
	$("#userFieldsWrapper").dialog({
		autoOpen : false,
		modal : false,
		width : 740,
		height : 350,
		closeOnEscape : true,
		draggable : true,
		resizable : true,
		buttons : [
		           {
		               id: "userFieldsSubmit",
		               text: "Submit",
		               click: submitUserAddOrEdit
		           }, {
		               id: "userFieldsCancel",
		               text: "Cancel",
		               click: function() {
		   				$(this).dialog('close')
		   			}
		           }
		       ]
	})
}

function submitUserAddOrEdit() {
   doubleClickSafeguard($("#userPopupSubmit"))
	var userId = $("#userFieldsWrapper").data('userId')
	var isNew = (userId == '')
		
	var username = $("#userUsername").val()
	var firstName = $("#userFirstName").val()
	var lastName = $("#userLastName").val()
	var phone = $("#userPhone").val()
	var email = $("#userEmail").val()
	var description = $("#userDescription").val()
	var passwordReset = $("#userPasswordReset").val()
	var passwordResetConfirm = $("#userPasswordResetConfirm").val()
	
	if (isNew && $.trim(passwordReset) == '') {
		displayAttentionDialog('Please enter a password for this user.')
		return
	}
	
	if (passwordReset != '') {
		if (/\s/g.test(passwordReset) || passwordReset.length < 6) {
			displayAttentionDialog('Your password must be at least 6 characters and cannot contain spaces.')
			$("#userPasswordReset").val('')
			$("#userPasswordResetConfirm").val('')
			return
		}
		
		if (passwordResetConfirm !== passwordReset) {
			displayAttentionDialog('Your password reset values do not match. Please try again.')
			$("#userPasswordReset").val('')
			$("#userPasswordResetConfirm").val('')
			return
		}
	}
	
	var errors = new Array()
	
	if ($.trim(username) == '')
	   errors.push('Please enter the username.')
	if ($.trim(firstName) == '')
	   errors.push('Please enter the first name.')
	if ($.trim(lastName) == '')
		errors.push('Please enter the last name.')
	if (!validateEmail(email))
		errors.push('Please enter a valid email in the format "user@server.tld".')
		
	if (errors.length > 0) {
		displayAttentionDialog("Please correct the following errors: <ul><li>"
				+ errors.join("</li><li>") + "</li></ul>");
		return
	}
	
	$.ajax({
		url : ajaxHomePath + '/appUser/saveOrUpdate',
		dataType : 'json',
		data : {
			userId : userId,
			firstName : firstName,
			lastName : lastName,
			username : username,
			phone : phone,
			email : email,
			description : description,
			passwordReset : passwordReset,
			passwordResetConfirm : passwordResetConfirm
		},
		error : commonAjaxErrorHandler,
		success : function(result) {
			$("#userFieldsWrapper").dialog('close')
			selectUser(result)
		}
	})
}

function toggleResetFields(toggleVal) {
	$(".passwordResetDisplay").toggle(!toggleVal)
	$(".passwordResetRow").toggle(toggleVal)
}

function editUser() {
	popupUserAddOrEdit(currentUserId)
}

function popupUserAddOrEdit(userId) {
	$("#userFieldsWrapper").data('userId', userId || '')
	
	$("#userUsername").val(userId ? currentUser.username : '')
	$("#userFirstName").val(userId ? currentUser.firstName : '')
	$("#userLastName").val(userId ? currentUser.lastName : '')
	$("#userPhone").val(userId ? currentUser.phone || '' : '')
	$("#userEmail").val(userId ? currentUser.email || '' : '')
	$("#userDescription").val(userId ? currentUser.description || '' : '')
	$("#userPasswordReset").val('')
	$("#userPasswordResetConfirm").val('')
	
	toggleResetFields(userId == null)
	
	$("#userFieldsWrapper").dialog('open')
}

// --------------------------------------- Roles functions

var allRoles = []

function getAllRoles(callback) {
	if (allRoles.length == 0) {
		loadAllRoles(function() {
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

function loadAllRoles(callback) {
	alert('AJAX call not implemented/needed yet')
}

// --------------------------------------- Main user functions

var currentUser = null
var currentUserId = null

function refreshUser(includeRoles) {
	currentUser = null
	var userSelected = currentUserId != null
	
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
			userId : currentUserId,
			includeRoles : includeRoles
		},
		error : commonAjaxErrorHandler,
		success : setUserFields
	})
}

function setUserFields(resultMap) {
	var userResult = resultMap.user
	currentUser = userResult
	
	var updateRoles = resultMap.updateRoles
	
	$("#userID").text(userResult.username)
	$("#name").text(userResult.displayName)
	$("#phone").text(userResult.phone)
	
	var emailHtml = ''
	if (userResult.email && userResult.email != '') {
		emailHtml = escapeHTML(userResult.email) + '<a href="mailto:'
			+ escapeHTML(userResult.email)
			+ '"><img alt="Click to email '
			+ escapeHTML(userResult.email) + '"' + 'src="' + imgHomePath
			+ '/envelope.jpg" height="14"'
			+ ' width="18" border="0" align="absmiddle"'
			+ ' style="padding-left: 4px; padding-right: 4px" /></a>'
	}
	
	$("#email").html(emailHtml)
	$("#enabled").prop('checked', userResult.enabled);
	$("#enabledText").text(userResult.enabled ? 'Yes' : 'No');
	$("#description").text(userResult.description)
	
	if (userResult.timeZone) {
		$("#timeZoneSelect").val(userResult.timeZone.id)
	}
	
	if (updateRoles) {
		$("#roles").empty()
		$.each(userResult.rolesSorted, function(index, appUserRole) {
			var role = appUserRole.role
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

function selectUserWithFields(id, username, displayName) {
	selectUser({
		id: id,
		username: username,
		displayName: displayName
	})
}

function selectUser(fullUserObj) {
	var table = $('#userTable').DataTable()
	table.clear()
	table.draw()
	
	if (fullUserObj) {
		table.row.add(fullUserObj).draw()
		currentUserId = fullUserObj.id
	} else {
		currentUserId = null
		currentUser = null
	}
	
	refreshUser(true)
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
				appUserId : currentUserId
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
		$.each(userResult.rolesSorted, function(index, appUserRole) {
			var role = appUserRole.role
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
			userId : currentUserId,
			enabled: $("#enabled").is(':checked'),
			timezone: $("#timeZoneSelect").val(),
			roles: roles.join(),
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

// For making "Set Default" and "Customze..." buttons 508 Compliant
function doNothing() {
}