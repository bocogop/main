$(function() {
	buildVolunteerDataComparePopup()
	buildNotificationsTable()
	refreshNotifications()
})

function buildVolunteerDataComparePopup() {
	$("#volunteerDataChangeCompareDiv").dialog({
		autoOpen : false,
		modal : true,
		width : 500,
		closeOnEscape : false,
		draggable : true,
		resizable : true,
		buttons : {
			'Close' : function() {
				$(this).dialog('close')
			}
		}
	})

	$("#volunteerDataChangeCompareDiv").show()
}

function compareVolunteerDataChanges(volId, fromVer, toVer) {
	$.ajax({
			url : ajaxHomePath + "/volunteer/history",
			data : {
				volunteerId : volId,
				versions : [fromVer, toVer]
			},
			type : "POST",
			dataType : 'json',
			error : commonAjaxErrorHandler,
			success : function(r) {
				var fromEntry = r[fromVer]
				var toEntry = r[toVer]
				
				var tbody = $("#volunteerDataChangeTable tbody")
				tbody.empty()
				
				var fields = {
					firstName : 'First Name',
					middleName : 'Middle Name',
					lastName : 'Last Name',
					suffix : 'Suffix',
					nickname : 'Nickname',
					gender : {
						label : 'Gender',
						valueGenerator : function(entry) {
							return entry.gender.name
						}
					},
					dateOfBirth : 'Date of Birth',
					addressLine1 : 'Address Line 1',
					addressLine2 : 'Address Line 2',
					city : 'City',
					state : {
						label : 'State',
						valueGenerator : function(entry) {
							return entry.state.name
						}
					},
					zip : 'Zip',
					phone : 'Phone',
					phoneAlt : 'Alternate Phone',
					phoneAlt2 : 'Alternate Phone 2',
					email : 'Email',
					emergencyContactName : 'Emergency Contact Name',
					emergencyContactRelationship : 'Emergency Contact Relationship',
					emergencyContactPhone : 'Emergency Contact Phone',
					emergencyContactPhoneAlt : 'Emergency Contact Alt Phone',
				}
				
				$.each(fields, function(key, val) {
					var fromVal = !fromEntry ? '(not found)' :
						val.valueGenerator ? val.valueGenerator(fromEntry) : fromEntry[key] || ''
					var toVal = !toEntry ? '(not found)' :
						val.valueGenerator ? val.valueGenerator(toEntry) : toEntry[key] || ''
					var label = val.label || val
					
					if (fromVal != toVal) {
						var tr = $("<tr></tr>")
						tr.append($("<td nowrap></td>").text(label))
						tr.append($("<td nowrap></td>").text(fromVal))
						tr.append($("<td nowrap></td>").text(toVal))
						tbody.append(tr)
					}
				})
				
				$("#volunteerDataChangeCompareDiv").dialog('open')
			}
		})
}

function buildNotificationsTable() {
	$('#notificationList').DataTable(
			{
				"columns" : [ {
					"render" : function(data, type, full, meta) {
						var s = ''
						if (type == 'display') {
							var severity = null
							if (full.severity.code == 'H') {
								severity = 'high'
							} else if (full.severity.code == 'M') {
								severity = 'medium'
							}
							if (severity != null) {
								s += '<img align="absmiddle" vspace="4" src="' + imgHomePath + '/decal-small-' + severity + '.png" alt="Decal signifying ' + severity + ' severity" tabindex="0" />&nbsp;'
							} else {
								s += '<img src="' + imgHomePath + '/spacer.gif" height="10" width="10" alt="Spacer image" />' 
							}
							s += full.severity.name
						} else {
							s += full.severity.name
						}
						return s
					}
				},
				{
					"render" : function(data, type, full, meta) {
						return full.name
					}
				}, {
					"render" : function(data, type, full, meta) {
						var s = ''
						
						s += '<div style="display:inline-block">'
						// s += '<ul class="notificationRefList">'
						if (full.referenceVolunteer) {
							s += 'Volunteer: <a class="appLink" href="' + homePath + '/volunteerEdit.htm?id='
								+ full.referenceVolunteer.id + '">' + escapeHTML(full.referenceVolunteer.displayName) + '</a><br>'
						}
						// s += '</ul>'
						s += '</div>'
							
						return s
					}
				}
				, {
					"render" : function(data, type, full, meta) {
						var theText = escapeHTML((full.originatingFacility) ? full.originatingFacility.displayName : '')
						if (type === 'filter') {
							return abbreviate(theText, 25)
						}
						
						return full.originatingFacility ? full.originatingFacility.displayName : ''
					},
					"visible" : isNationalAdmin
				}
				, {
					"render" : function(data, type, full, meta) {
						return full.beginDate
					}
				}
				, {
					"render" : function(data, type, full, meta) {
						return full.expirationDate
					}
				}
				, {
					"className": "dt-center",
					"render" : function(data, type, full, meta) {
						var options = ''
							
						var volReq = full['referenceVolunteerRequirement']
						if (volReq) {
							options += '<li><a href="javascript:showVolunteerRequirementPopup(' + volReq.id + ')">Edit Volunteer Requirement</a></li>'
						}
							
						$.each(["link", "link2", "link3"], function(index, item) {
							if (!full[item]) return
							
							if (full[item].code == 'A' && full.referenceAuditFromVersion != null && full.referenceAuditToVersion != null) {
								if (full.referenceVolunteer) {
									options += '<li><a href="javascript:compareVolunteerDataChanges(' + full.referenceVolunteer.id + ', '
										+ full.referenceAuditFromVersion + ', ' + full.referenceAuditToVersion + ')">View Data Changes</a></li>'
								} // else { ... for other types of objects - CPB
							}
							
							if (full[item].code == 'V' && full.referenceVolunteer) {
								options += '<li><a href="javascript:jumpToVolunteer(' + full.referenceVolunteer.id + ')">View Volunteer</a></li>'
							}
							
							if (full[item].code == 'D') {
								options += '<li><a href="' + homePath + '/manageDonationLog.htm">View E-Donations Received</a></li>'
							}
						})
						
						if (full.clearable) {
							options += '<li><a href="javascript:clearNotification(' + full.id + ')">Clear Notification</a></li>'
						}
						
						if (options == '') return ''
						
						var s = ''
						s += ' <div id="notificationActions' + full.uniqueIdentifier + '" class="wr-dropdown wr-dropdown-anchor-right" style="display: none">'
							+ '<ul class="wr-dropdown-menu" style="text-align:left">' + options + '</ul></div>'
						
						s += ' <a href="#" data-wr-dropdown="#notificationActions' + full.uniqueIdentifier
							+ '"><img src="' + imgHomePath + '/down.gif" border="0" /></a>'
						return s
					}
				}],
				"dom" : '<"top"f>rt<"bottom"><"clear">',
				"order" : [],
				"paging" : false,
				"scrollY" : "300px",
				"stateSave" : false,
				"stripeClasses" : [ 'odd' ]
			})
}

var volunteerRequirementMap = {}

function refreshNotifications() {
	volunteerRequirementMap = {}
	
	$.ajax({
		url : ajaxHomePath + "/notification",
		type : "POST",
		dataType : 'json',
		error : commonAjaxErrorHandler,
		success : function(r) {
			var hasNotifications = r.notifications.length > 0
			$("#notificationsMaxReached").toggle(r.hitMaxResults)
			$("#notificationListWrapper").toggle(hasNotifications)
			$("#noNotificationDiv").toggle(!hasNotifications)
			
			var table = $('#notificationList').DataTable()
			table.clear()
			
			if (hasNotifications) {
				$.each(r.notifications, function(index, notification) {
					var refVolReq = notification.referenceVolunteerRequirement
					if (refVolReq) volunteerRequirementMap[refVolReq.id] = refVolReq
					
					var row = table.row.add(notification)
					
					var tr = $('<tr valign="top"></tr>')
					var td = $('<td colspan="7"></td>').appendTo(tr)
					var ul = $('<ul class="notificationRefList"></ul>').appendTo(td)
					
					var hasChild = false
					/*
					childRow += '<ul class="notificationRefList">'
					if (notification.referenceVolunteer) {
						hasChild = true
						childRow += '<li>Volunteer: <a class="appLink" href="' + homePath + '/volunteerEdit.htm?id='
							+ notification.referenceVolunteer.id + '">' + escapeHTML(notification.referenceVolunteer.displayName) + '</a></li>'
					}
					childRow += '</ul>'
					*/
					
					hasChild = true
					var desc = notification.description
					if (notification.targetRole && notification.targetRole.id == nationalAdminRoleId)
						desc = 'National Admin Notification: ' + desc
					$('<li></li>').text(desc).appendTo(ul)
					
					if (hasChild)
						row.child(tr)
					row.show()
				})
			}
			
			rebuildTableFilters('notificationList')
			table.columns.adjust().draw()
			$(window).resize()
		}
	})
}

function clearNotification(notificationId) {
	confirmDialog('Are you sure you wish to clear this notification?', function() {
		$.ajax({
			url : ajaxHomePath + "/notification/clear",
			data : {
				notificationId : notificationId
			},
			type : "POST",
			dataType : 'json',
			error : commonAjaxErrorHandler,
			success : function(r) {
				refreshNotifications()
			}
		})
	})
	
}