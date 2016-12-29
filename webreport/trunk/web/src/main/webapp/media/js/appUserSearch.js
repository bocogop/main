function popupAppUserSearch(uid) {
	$("#appUserSearchName" + uid).val('')
	$("#appUserSearchADName" + uid).val('')
	$("#appUserSearchNoResults" + uid).hide()
	$("#appUserSearchResultsTable" + uid).hide()
	$('#appUserSearchName' + uid).focus()
	$("#appUserSearchDialog" + uid).dialog('open')
}

function initAppUserSearchPopup(uid, includeLocalDB, includeLDAP) {
	var parms = {
		"dom" : '<"top"i><"tableClear"><"top2"pl>rt',
		"pagingType" : "full_numbers",
		"pageLength" : 10,
		"lengthMenu" : [ [ 10, -1 ], [ 10, "All" ] ],
		"stateSave" : false,
		"stripeClasses" : [ 'odd' ]
	}

	var theDataTable = $('#appUserSearchResultsList' + uid).DataTable(parms)

	$.each([ "appUserSearchName", "appUserSearchADName", "appUserSearchLink" ], function(index,
			value) {
		$("#" + value + uid).keypress(function(event) {
			if (event.which == 13) {
				submitAppUserSearchForm(uid, includeLocalDB, includeLDAP)
			}
		})
	})

	$("#appUserSearchLink" + uid).click(function(evt) {
		submitAppUserSearchForm(uid, includeLocalDB, includeLDAP)
	})

	$("#appUserSearchDialog" + uid).dialog({
		autoOpen : false,
		modal : true,
		width : 1000,
		height : 600,
		closeOnEscape : false,
		draggable : true,
		resizable : true,
		// zIndex: 200000,
		buttons : {
			'Cancel' : function() {
				$(this).dialog('close')
				window["appUserSearchPopupCancel" + uid]()
			}
		}
	})

	$("#appUserSearchDialog" + uid).show()
	/*
	 * Required to solve 508 issue not reading dialog box title. It is also
	 * required to disable dialog animation to enable this functionality
	 */
	$('#appUserSearchName' + uid).focus()
}

var appUserSearchResults = new Object()

function submitAppUserSearchForm(uid, includeLocalDB, includeLDAP) {
	var theName = $("#appUserSearchName" + uid).val()
	var theUsername = $("#appUserSearchADName" + uid).val()
	
	if ($.trim(theName) == '' && $.trim(theUsername) == '') {
		displayAttentionDialog("Please enter at least one piece of search criteria.")
		return
	}
	
	var submitFunc = function() {
		$.ajax({
				url : ajaxHomePath + "/appUser/find",
				type : "POST",
				dataType : 'json',
				data : {
					name : theName,
					activeDirectoryName : theUsername,
					includeLocalDB: includeLocalDB,
					includeLDAP: includeLDAP
				},
				error : commonAjaxErrorHandler,
				success : function(results) {
					$("#appUserSearchNoResults" + uid).hide()
					$("#appUserSearchResultsTable" + uid).hide()

					var resultMap = new Object()

					var table = $('#appUserSearchResultsList' + uid)
							.DataTable()
					table.clear()

					for (var i = 0; i < results.length; i++) {
						resultMap['' + results[i].username] = results[i]

						var appUserName = results[i].displayName;
						var appUserNameEscaped = escapeHTML(appUserName)
						var newRow = new Array()

						newRow[newRow.length] = '<a class="appLink" href="javascript:appUserSearchPopupItemSelected'
								+ uid
								+ '(\''
								+ results[i].username
								+ '\')">'
								+ appUserNameEscaped + '</a>'
						newRow[newRow.length] = escapeHTML(results[i].title)
						newRow[newRow.length] = escapeHTML(results[i].department)
						newRow[newRow.length] = escapeHTML(results[i].office)
						newRow[newRow.length] = escapeHTML(results[i].telephoneNumber)
						newRow[newRow.length] = escapeHTML(results[i].email)
						table.row.add(newRow)
					}

					appUserSearchResults[uid] = resultMap

					if (results.length > 0) {
						$("#appUserSearchResultsTable" + uid).show()

						/*
						 * Disabling the next few lines since we don't have
						 * filters for search results - CPB
						 */
						/* Reset all filters every time they do a search */
						// table.search('').columns().search('')
						// rebuildTableFilters('appUserSearchResultsList' + uid)
					} else {
						$("#appUserSearchNoResults" + uid).show()
					}
					table.draw()

					$('#appUserSearchName' + uid).focus()
				}
			})
	}
	
	if (!includeLDAP) {
		submitFunc()
		return
	}
	
	var tokens = theName.split(",")
	var lastNameHasWildcard = tokens[0].indexOf("*") != -1
	var firstNameHasWildcard = tokens.length >= 2 && tokens[1].indexOf("*") != -1
	if (tokens.length == 1 && lastNameHasWildcard) {
		confirmDialog('Searching just the last name with a wildcard may take a long time. Are you sure?',
				submitFunc)
	} else if (lastNameHasWildcard && firstNameHasWildcard){
		confirmDialog('Searching with wildcards on both the first and last names may take a long time. Are you sure?',
				submitFunc)
	} else {
		submitFunc()
	}
}