<%@ include file="../shared/inc_header.jsp"%>

<c:if test="${FORM_READ_ONLY}">
	<%@ include file="../shared/inc_modifyToView.jsp"%>
</c:if>

<%@ include file="linkServicesAndRolesPopup.jsp"%>
<%@ include file="editServicePopup.jsp"%>
<%@ include file="editServiceRolePopup.jsp"%>

<script type="text/javascript">
	var isReadOnly = ${FORM_READ_ONLY}
	
	$(function() {
		var isRole = function(r) {
			return typeof r.benefitingServiceRoles == 'undefined'
		}
		
		var theDataTable = $('#benefitingServiceList').DataTable({
			"columns" : [ {
		        	"render" : function(row, type, val, meta) {
						return val.name
					}
				}, {
					"render" : function(row, type, val, meta) {
						return val.subdivision
					}
				}, {
					"render" : function(row, type, val, meta) {
						return val.scope == 'NATIONAL' ? 'National' : 'Local'
					}
				}, {
					"render" : function(row, type, val, meta) {
						/*if (type === 'filter') {
							return abbreviate(val.locationDisplayName, 25)
						}
						return val.locationDisplayName
						*/
						return ''
					}
				}, {
					"render" : function(row, type, val, meta) {
						return ''
					}
				}, {
					"render" : function(row, type, val, meta) {
						var s = ''
						if (type === 'display') {
							s += (val.contactName || '') + '<br>'
							if ($.trim(val.contactEmail) != '')
								s += '<nobr>' + val.contactEmail + ' <a href="mailto:' + val.contactEmail
									+ '"><img alt="Click to email contact" src="' + imgHomePath
									+ '/envelope.jpg" height="14" width="18" border="0" align="absmiddle"'
									+ ' style="padding-left: 4px; padding-right: 4px" /></a></nobr><br>'
							s += '<nobr>' + (val.contactPhone || '') + '</nobr>'
						} else {
							s = val.contactName + ' ' + val.contactEmail + ' ' + val.contactPhone
						}
						
						return s
					}
				}, {
					"render" : function(row, type, val, meta) {
						return val.gamesRelated ? 'Yes' : 'No'
					}
				}, {
					"className" : "dt-body-right",
					"render" : function(row, type, val, meta) {
						if (type === 'display') {
							return 'Active: ' + val.volunteerActiveCount
								+ '<br>Total: ' + val.volunteerTotalCount
						}
						return val.volunteerActiveCount
					}
				}, {
					"className" : "dt-body-right",
					"render" : function(row, type, val, meta) {
						return val.occasionalHoursCount
					}
				}, {
					"render" : function(row, type, val, meta) {
							var actions = ''
							if (val.inactive) {
								actions = '<a href="javascript:reactivateBenefitingService('
									+ val.id + ')"><img src="'+ imgHomePath
									+ '/switch.png" alt="Reactivate Service" border="0" hspace="5" align="center"/></a>'
							} else {
								actions = '<a href="javascript:inactivateBenefitingService('
										+ val.id + ', ' + val.volunteerActiveCount + ')"><img src="' + imgHomePath
										+ '/switch.png" alt="Inactivate Service" border="0" hspace="5" align="center"/></a>'
							}
							return (type === 'display' ? actions + ' ': '') + (val.inactive ? 'Inactive' : 'Active')
					}
				}
				<c:if test="${not FORM_READ_ONLY}">
				, {
					"render" : function(row, type, val, meta) {
						var actions = '<div style="margin:0 auto; text-align:left"><nobr>'

						actions += '<a href="javascript:showEditServicePopup('
							+ val.id + ')"><img src="'+ imgHomePath
							+ '/edit-small.gif" alt="Edit Service" border="0" hspace="5" align="center"/></a>'
						
						if (val.volunteerTotalCount == 0 && val.occasionalHoursCount == 0) {
							actions += '<a href="javascript:deleteBenefitingService('
								+ val.id + ', ' + val.volunteerTotalCount + ')"><img src="' + imgHomePath
								+ '/permanently_delete_18x18.png" alt="Delete Service" border="0" hspace="5" align="center"/></a>'
						}
							
						if (val.scope != 'NATIONAL') {
							actions += '<a href="javascript:showEditServiceRolePopup(\'new\', undefined, '
								+ val.id + ')"><img src="'+ imgHomePath
								+ '/btn_addUsers.png" alt="Add Custom Local Service" border="0" hspace="5" align="center"/></a>'
						} else {
							actions += '<a href="javascript:showLinkDetailsPopup('
									+ val.id + ')"><img src="'+ imgHomePath
									+ '/btn_addUsers.png" alt="Add National Service or Create Custom Service" border="0" hspace="5" align="center"/></a>'
						}
						actions += '</nobr></div>'
						return actions
					}
				}
				</c:if>
			],
			"createdRow": function(row, data, dataIndex) {
				if (!isRole(data)) {
			      $(row).addClass('serviceRow');
			    }
			},
	    	"dom": '<"top"fi>rt<"bottom"pl><"clear">',
	    	"lengthMenu" : [ [ 10, 50, -1 ],
	    	 				[ 10, 50, "All" ] ],
			"order": [],
	    	"pageLength": 10,
	    	"pagingType": "full_numbers",
	    	"stripeClasses" : [],
		})
		
		refreshBenefitingServicesTable()
		$("#facilityId").change(refreshBenefitingServicesTable)
		
		buildFacilitySelect()
	})
	
	/* cache map of facilityID to array of location objects { id : id, displayName : displayName } */
	var localFacilityMap = {}
	function getLocalFacilitiesForLocation(facilityId, activeStatus, callback) {
		var locations = localFacilityMap[facilityId]
		if (typeof locations == 'undefined') {
			$.ajax({
				url : ajaxHomePath + "/facility/location",
				type : "POST",
				data : {
					facilityId : facilityId,
					activeStatus : activeStatus
				},
				dataType : 'json',
				error : commonAjaxErrorHandler,
				success : function(results) {
					localFacilityMap[facilityId] = results.locations
					callback(results.locations)
				}
			})
		} else {
			callback(locations)
		}
	}
	
	var benefitingServiceMap = null
	var benefitingServiceRoleMap = null
	var benefitingServiceList = null
	var countsMapForRoles = null
	var occasionalHoursMapForRoles = null
	
	function refreshBenefitingServicesTable() {
		benefitingServiceMap = new Object()
		benefitingServiceRoleMap = new Object()
		
		$.ajax({
			url : ajaxHomePath + '/benefitingServicesWithRoles',
			dataType : 'json',
			data : {
				facilityId: $("#facilityId").val()
			},
			error : commonAjaxErrorHandler,
			success : function(response) {
				benefitingServiceList = response.benefitingServices
				
				countsMapForRoles = response.volunteerCountsForRoles
				occasionalHoursMapForRoles = response.occasionalHoursForRoles
				
				var rArray = new Array()
				
				for (var i = 0; i < benefitingServiceList.length; i++) {
					var benefitingService = benefitingServiceList[i]
					benefitingServiceMap[benefitingService.id] = benefitingService
					
					var finalActiveCount = 0, finalTotalCount = 0, finalOccasionalHoursCount = 0
					for (var j = 0; j < benefitingService.benefitingServiceRoles.length; j++) {
						var benefitingServiceRole = benefitingService.benefitingServiceRoles[j]
						var counts = countsMapForRoles[benefitingServiceRole.id] || [0, 0]
						finalActiveCount += counts[0]
						finalTotalCount += counts[1]
						finalOccasionalHoursCount += occasionalHoursMapForRoles[benefitingServiceRole.id] || 0
					}
					
					rArray[rArray.length] = $.extend({}, benefitingService, {
						volunteerActiveCount : finalActiveCount,
						volunteerTotalCount : finalTotalCount,
						occasionalHoursCount : finalOccasionalHoursCount
					})
				}

				var table = $('#benefitingServiceList').DataTable()
				table.clear()
				table.rows.add(rArray)
				table.rows().every(function(rowIndex, tableLoop, rowLoop) {
					var benefitingService = this.data()
					var childRows = []
					
					for (var j = 0; j < benefitingService.benefitingServiceRoles.length; j++) {
						var benefitingServiceRole = benefitingService.benefitingServiceRoles[j]
						benefitingServiceRoleMap[benefitingServiceRole.id] = benefitingServiceRole
						var counts = countsMapForRoles[benefitingServiceRole.id] || [0, 0]
						var occasionalHours = occasionalHoursMapForRoles[benefitingServiceRole.id] || 0
							
						var childRow = $('<tr />')
						$('<td></td>').appendTo(childRow)
						
						var nameTd = $('<td></td>').text(benefitingServiceRole.name)
						if (benefitingServiceRole.requiredAndReadOnly) nameTd.attr('style', 'font-weight:bold')
						nameTd.appendTo(childRow)
						
						$('<td></td>').text(benefitingServiceRole.scope == 'NATIONAL' ? 'National' : 'Local').appendTo(childRow)
						$('<td></td>').text(benefitingServiceRole.locationDisplayName).appendTo(childRow)
						$('<td></td>').text(benefitingServiceRole.roleType ? benefitingServiceRole.roleType.name : '').appendTo(childRow)
						
						var contactInfo = escapeHTML(benefitingServiceRole.contactName || '')
						if ($.trim(benefitingServiceRole.contactEmail) != '')
							contactInfo += '<br>' + escapeHTML(benefitingServiceRole.contactEmail) + ' <a href="mailto:' + benefitingServiceRole.contactEmail
									+ '"><img alt="Click to email contact" src="' + imgHomePath
									+ '/envelope.jpg" height="14" width="18" border="0" align="absmiddle"'
									+ ' style="padding-left: 4px; padding-right: 4px" /></a>'
						contactInfo += '<br>' + escapeHTML(benefitingServiceRole.contactPhone || '')
						
						$('<td nowrap></td>').html(contactInfo).appendTo(childRow)
						$('<td></td>').appendTo(childRow)
						
						$('<td align="right"></td>').html('Active: ' + counts[0] + '<br>Total: ' + counts[1]).appendTo(childRow)
						$('<td align="right"></td>').text(occasionalHours).appendTo(childRow)
						
						var activeTd = $('<td nowrap></td>')
						if (!benefitingServiceRole.requiredAndReadOnly) {
							if (benefitingServiceRole.inactive) {
								activeTd.html('<a href="javascript:reactivateBenefitingServiceRole('
									+ benefitingServiceRole.id + ')"><img src="'+ imgHomePath
									+ '/switch.png" alt="Reactivate Service Role" border="0" hspace="5" /></a> Inactive')
							} else {
								activeTd.html('<a href="javascript:inactivateBenefitingServiceRole('
										+ benefitingServiceRole.id + ', ' + counts[0] + ')"><img src="' + imgHomePath
										+ '/switch.png" alt="Inactivate Service Role" border="0" hspace="5" /></a> Active')
							}
						} else {
							activeTd.html('<img src="'+ imgHomePath
									+ '/spacer.gif" alt="" height="1" width="33" /> '
									+ (benefitingServiceRole.inactive ? 'Inactive' : 'Active'))
						}
						activeTd.appendTo(childRow)
						
						var actions = '<td nowrap>'
						actions += '<a href="javascript:showEditServiceRolePopup(\'edit\', '
							+ benefitingServiceRole.id + ', ' + benefitingService.id + ')"><img alt="Edit Service Role" src="'+ imgHomePath
							+ '/edit-small.gif" border="0" hspace="5" align="center"/></a>'
						
						if (counts[1] == 0 && occasionalHours == 0 && !benefitingServiceRole.requiredAndReadOnly) {
							actions += '<a href="javascript:deleteBenefitingServiceRole('
								+ benefitingServiceRole.id + ')"><img alt="Delete Service Role" src="'+ imgHomePath
								+ '/permanently_delete_18x18.png" border="0" hspace="5" align="center"/></a>'
						}
						
						actions += '</td>'
						$(actions).appendTo(childRow)
						childRows.push(childRow)
					}
					this.child(childRows).show()
				})
				
				rebuildTableFilters('benefitingServiceList')
				table.draw()
				
				$("#benefitingServiceList_filter9").val('Active')
				$("#benefitingServiceList_filter9").change()
		    }
		})
	}
	
	function deleteBenefitingService(benefitingServiceId) {
		var fullObj = benefitingServiceMap[benefitingServiceId]
		var msg = 'Are you sure you want to delete "'
			+ fullObj.name + ($.trim(fullObj.subdivision) == '' ? '' : ' - ' + fullObj.subdivision) + '"?'
	    confirmDialog(msg,
                function() {
  					$.ajax({
					url : ajaxHomePath + '/benefitingService/delete',
					dataType : 'json',
					data : {
						benefitingServiceId: benefitingServiceId
					},
					error : commonAjaxErrorHandler,
					success : refreshBenefitingServicesTable
				})
        })
	}
	
	function inactivateBenefitingService(benefitingServiceId, volCount) {
		var fullObj = benefitingServiceMap[benefitingServiceId]
		var msg = 'Are you sure you want to inactivate "'
			+ fullObj.name + ($.trim(fullObj.subdivision) == '' ? '' : ' - ' + fullObj.subdivision) + '"?'
		if (volCount > 0)
			msg += ' <span class="redText" style="font-weight:bold"><p>There are ' + volCount
				+ ' associated volunteer assignment(s) that will also be inactivated and will not be'
				+ ' automatically reactivated if this role is reactivated!</span>'
	    confirmDialog(msg,
                function() {
  					$.ajax({
					url : ajaxHomePath + '/benefitingService/inactivate',
					dataType : 'json',
					data : {
						benefitingServiceId: benefitingServiceId
					},
					error : commonAjaxErrorHandler,
					success : refreshBenefitingServicesTable
				})
        })
	}
	
	function reactivateBenefitingService(benefitingServiceId) {
		$.ajax({
			url : ajaxHomePath + '/benefitingService/reactivate',
			dataType : 'json',
			data : {
				benefitingServiceId: benefitingServiceId
			},
			error : commonAjaxErrorHandler,
			success : refreshBenefitingServicesTable
		})
	}
	
	function reactivateBenefitingServiceRole(benefitingServiceRoleId) {
		$.ajax({
			url : ajaxHomePath + '/benefitingServiceRole/reactivate',
			dataType : 'json',
			data : {
				benefitingServiceRoleId: benefitingServiceRoleId
			},
			error : commonAjaxErrorHandler,
			success : refreshBenefitingServicesTable
		})
	}
	
	function deleteBenefitingServiceRole(benefitingServiceRoleId, volCount) {
		var fullObj = benefitingServiceRoleMap[benefitingServiceRoleId]
	    confirmDialog('Are you sure you want to delete "' + fullObj.name + '"?',
                function() {
  					$.ajax({
					url : ajaxHomePath + '/benefitingServiceRole/delete',
					dataType : 'json',
					data : {
						benefitingServiceRoleId: benefitingServiceRoleId
					},
					error : commonAjaxErrorHandler,
					success : refreshBenefitingServicesTable
				})
        })
	}
	
	function inactivateBenefitingServiceRole(benefitingServiceRoleId, volCount) {
		var fullObj = benefitingServiceRoleMap[benefitingServiceRoleId]
		var msg = 'Are you sure you want to inactivate "' + fullObj.name + '"?'
		if (volCount > 0)
			msg += ' <span class="redText" style="font-weight:bold"><p>There are ' + volCount
				+ ' associated volunteer assignment(s) that will also be inactivated and will not be'
				+ ' automatically reactivated if this role is reactivated!</span>'
	    confirmDialog(msg,
                function() {
  					$.ajax({
					url : ajaxHomePath + '/benefitingServiceRole/inactivate',
					dataType : 'json',
					data : {
						benefitingServiceRoleId: benefitingServiceRoleId
					},
					error : commonAjaxErrorHandler,
					success : refreshBenefitingServicesTable
				})
        })
	}
	
	var facilitiesPopulated = false
	
	function buildFacilitySelect() {
		var facilityEl = $("#facilityId")
		facilityEl.multiselect({
			selectedText : function(numChecked, numTotal, checkedItems) {
				return abbreviate($(checkedItems[0]).next().text())
			},
			beforeopen: function(){
				if (facilitiesPopulated) return
				var curVal = facilityEl.val()
				
				$.ajax({
					url : ajaxHomePath + "/getManageServiceStations",
					type : "POST",
					dataType : 'json',
					error : commonAjaxErrorHandler,
					success : function(results) {
						facilityEl.empty()
						var newHtml = []
						$.each(results, function(index, item) {
							var selectedText = (item.id == curVal) ? ' selected="selected"' : ''
							newHtml.push('<option value="' + item.id + '"' + selectedText + '>' + item.displayName + '</option>')
						})
						facilityEl.html(newHtml.join(''))
						
						facilityEl.val(curVal)
						facilityEl.multiselect("refresh")
						facilitiesPopulated = true
						
						facilityEl.multiselect("open")
					}
				})
				
				return false
		   },
			multiple : false,
			minWidth : 400
		}).multiselectfilter()
	}
</script>

<style>
table#benefitingServiceList tr.serviceRow {
	background-color: #dddddd;
}
table#benefitingServiceList {
	border-collapse: collapse;
}
table#benefitingServiceList td {
	margin: 3px;
}
</style>

<div class="clearCenter servicesContainer">
	<div align="center" style="margin-bottom:15px">
		<table>
			<tr>
				<td><a class="buttonAnchor"
					href="javascript:showLinkDetailsPopup()" style="margin-right:60px">Add Service</a></td>
				<td>Facility:</td>
				<td><select id="facilityId">
						<c:if test="${not empty facilityContextId}">
							<option value="${facilityContextId}" selected="selected"><c:out
									value="${facilityContextName}" /></option>
						</c:if>
				</select></td>
		</table>
	</div>

	<table class="formatTable" id="benefitingServiceList" border="1"
		summary="List of Benefiting Services &amp; roles">
		<thead>
			<tr>
				<td class="noborder"></td>
				<td class="noborder"></td>
				<td class="noborder" title="Filter by Service Scope"></td>
				<td class="noborder"></td>
				<td class="noborder"></td>
				<td class="noborder"></td>
				<td class="noborder" title="Filter by Games Status"></td>
				<td class="noborder"></td>
				<td class="noborder"></td>
				<td class="noborder" title="Filter by Status"></td>
				<c:if test="${not FORM_READ_ONLY}">
					<td class="noborder"></td>
				</c:if>
			</tr>
			<tr>
				<th>Service Name</th>
				<th>Role</th>
				<th class="select-filter">Scope</th>
				<th>Location</th>
				<th>Type</th>
				<th>Contact</th>
				<th class="select-filter">Games Service</th>
				<th>Volunteers</th>
				<th>Occasional<br>Hours</th>
				<th class="select-filter">Status</th>
				<c:if test="${not FORM_READ_ONLY}">
					<th>Action</th>
				</c:if>
			</tr>
		</thead>
		<tbody>
		</tbody>
	</table>
</div>