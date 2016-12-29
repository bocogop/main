<%@ include file="../shared/inc_header.jsp"%>

<script type="text/javascript">
$(function() {
	initMergeRolePopup()
})

function initMergeRolePopup() {
	var isRole = function(r) {
		return typeof r.benefitingServiceRoles == 'undefined'
	}
	
	var submitMergeRole = function() {
		var selectedEl = $("input[name='mergeRoleSelectItem']:checked")
		var selectedVal = selectedEl.val()
		if (!selectedVal) {
			displayAttentionDialog('Please select a target role.')
			return
		}
	
		var fromId = dialogEl.data('fromBenefitingServiceId')
		var fromRoleId = dialogEl.data('fromBenefitingServiceRoleId')
		var fromBs = benefitingServiceMap[fromId]
		var fromBsr = benefitingServiceRoleMap[fromRoleId]
		var fromCounts = countsMapForRoles[fromRoleId] || [0, 0]
		var fromOccasionalHours = occasionalHoursMapForRoles[fromRoleId] || 0
		
		var toId = selectedEl.next().val()
		var toRoleId = selectedVal
		var toBs = benefitingServiceMap[toId]
		var toBsr = benefitingServiceRoleMap[toRoleId]
		var toCounts = countsMapForRoles[toRoleId] || [0, 0]
		var toOccasionalHours = occasionalHoursMapForRoles[toRoleId] || 0
		
		var msg = 'Please confirm you wish to perform the following merge operation:<p>'
			+ '<table>'
			+ '	<tr style="font-weight:bold" align="center"><td>From Role:</td><td width="30" rowspan="2">&nbsp;</td><td>To Role:</td></tr>'
			+ '	<tr><td>'
			+ '		<table cellpadding="3" border="1">'
			+ '			<tr><td align="right">Service:</td><td nowrap>' + fromBs.name + ($.trim(fromBs.subdivision) != '' ? ' - ' + fromBs.subdivision : '') + '</td></tr>'
			+ '			<tr><td align="right">Role:</td><td nowrap>' + fromBsr.name + '</td></tr>'
			+ '			<tr><td align="right">Volunteers:</td><td align="right">Active: ' + fromCounts[0] + '<br>Total: ' + fromCounts[1] + '</td></tr>'
			+ '			<tr><td align="right" nowrap>Occasional Hours:</td><td align="right">' + fromOccasionalHours + '</td></tr>'
			+ '		</table>'
			+ '	</td><td>'
			+ '		<table cellpadding="3" border="1">'
			+ '			<tr><td align="right">Service:</td><td nowrap>' + toBs.name + ($.trim(toBs.subdivision) != '' ? ' - ' + toBs.subdivision : '') + '</td></tr>'
			+ '			<tr><td align="right">Role:</td><td nowrap>' + toBsr.name + '</td></tr>'
			+ '			<tr><td align="right">Volunteers:</td><td align="right">Active: ' + toCounts[0] + '<br>Total: ' + toCounts[1] + '</td></tr>'
			+ '			<tr><td align="right" nowrap>Occasional Hours:</td><td align="right">' + toOccasionalHours + '</td></tr>'
			+ '		</table>'
			+ '	</td></tr>'
			+ '</table>'
			
		confirmDialog(msg, function() {
			$.ajax({
				url : ajaxHomePath + '/benefitingServiceRole/merge',
				method: 'POST',
				dataType : 'json',
				data : {
					fromBenefitingServiceRoleId : fromRoleId,
					toBenefitingServiceRoleId : selectedVal,
				},
				error : commonAjaxErrorHandler,
				success : function(response) {
					$("#mergeRoleDialog").dialog('close')
					$("#editServiceRoleDialog").dialog('close')
					refreshBenefitingServicesTable()
			    }
			})
		}, {
			width: 900,
			height: 300
		})
	}
	
	var dialogEl = $("#mergeRoleDialog")
	dialogEl.dialog({
		autoOpen : false,
		modal : false,
		width : 800,
		height : 700,
		closeOnEscape : true,
		draggable : true,
		resizable : true,
		buttons : {
			'Submit' : {
				text : 'Submit',
				id : 'mergeRoleSubmit',
				click : function() {
					doubleClickSafeguard($("#mergeRoleSubmit"))
					submitMergeRole()
				}
			},
			'Cancel' : function() {
				$(this).dialog('close')
			}
		}
	})
	
	var theDataTable = $('#mergeRoleList').DataTable({
    	"columns" : [ {
	        	"render" : function(row, type, val, meta) {
					return val.name
				}
			}, {
	        	"render" : function(row, type, val, meta) {
					return ''
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
					if (type === 'filter') {
						return abbreviate(val.locationDisplayName, 25)
					}
					return val.locationDisplayName
				}
			}, {
				"render" : function(row, type, val, meta) {
					return ''
				}
			}, {
				"render" : function(row, type, val, meta) {
					return val.inactive ? 'Inactive' : 'Active'
				}
			}
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
	
	$("#mergeRoleDialog").show()
}
	
function refreshMergeRoleTable() {
	var fromRoleId = $("#mergeRoleDialog").data('fromBenefitingServiceRoleId')
	
	var table = $('#mergeRoleList').DataTable()
	table.clear()
	table.rows.add(benefitingServiceList)
	
	table.rows().every(function(rowIndex, tableLoop, rowLoop) {
		var benefitingService = this.data()
		
		for (var j = 0; j < benefitingService.benefitingServiceRoles.length; j++) {
			var benefitingServiceRole = benefitingService.benefitingServiceRoles[j]
			benefitingServiceRoleMap[benefitingServiceRole.id] = benefitingServiceRole
		}
	})
	
	var fromRole = benefitingServiceRoleMap[fromRoleId]
	
	table.rows().every(function(rowIndex, tableLoop, rowLoop) {
		var benefitingService = this.data()
		var childRows = []
		
		for (var j = 0; j < benefitingService.benefitingServiceRoles.length; j++) {
			var benefitingServiceRole = benefitingService.benefitingServiceRoles[j]
			
			/* Don't allow a merge from an active role to an inactive role - CPB */
			if (!fromRole.inactive && benefitingServiceRole.inactive)
				continue;
			
			var childRow = $('<tr />')
			$('<td></td>').appendTo(childRow)
			if (fromRoleId == benefitingServiceRole.id) {
				$('<td></td>').appendTo(childRow)
			} else {
				$('<td><input type="radio" name="mergeRoleSelectItem" value="' + benefitingServiceRole.id + '" />'
						+ '<input type="hidden" value="' + benefitingService.id + '" /></td>').appendTo(childRow)
			}
			$('<td></td>').text(benefitingServiceRole.name).appendTo(childRow)
			$('<td></td>').text(benefitingServiceRole.scope == 'NATIONAL' ? 'National' : 'Local').appendTo(childRow)
			$('<td></td>').text(benefitingServiceRole.locationDisplayName).appendTo(childRow)
			$('<td></td>').text(benefitingServiceRole.roleType ? benefitingServiceRole.roleType.name : '').appendTo(childRow)
			$('<td></td>').text(benefitingServiceRole.inactive ? 'Inactive' : 'Active').appendTo(childRow)
			
			childRows.push(childRow)
		}
		this.child(childRows).show()
	})
	
	rebuildTableFilters('mergeRoleList')
	
	$("select", "#statusFilter").val('Active')
	$("select", "#statusFilter").change()
	
	table.draw()
}

function showMergeRolePopup(benefitingServiceRoleId, benefitingServiceId) {
	$("#mergeRoleDialog").data('fromBenefitingServiceId', benefitingServiceId)
	$("#mergeRoleDialog").data('fromBenefitingServiceRoleId', benefitingServiceRoleId)
	$("#mergeRoleDialog").dialog('open')
	refreshMergeRoleTable()
}
</script>

<style>
table#mergeRoleList tr.serviceRow {
	background-color: #dddddd;
}

table#mergeRoleList {
	border-collapse: collapse;
}

table#mergeRoleList td {
	margin: 3px;
}
</style>

<div id="mergeRoleDialog${uid}" style="display: none"
	title="Select a Target Role">
	<table class="formatTable" id="mergeRoleList" border="1"
		summary="List of Roles">
		<thead>
			<tr>
				<td width="30%" class="noborder"></td>
				<td width="5%" class="noborder"></td>
				<td width="30%" class="noborder"></td>
				<td width="10%" class="noborder" title="Filter by Service Scope"></td>
				<td width="10%" class="noborder" title="Filter by Service Location"></td>
				<td width="7%" class="noborder"></td>
				<td width="8%" class="noborder" id="statusFilter" title="Filter by Status"></td>
			</tr>
			<tr>
				<th>Service Name</th>
				<th></th>
				<th>Role</th>
				<th class="select-filter">Scope</th>
				<th class="select-filter">Location</th>
				<th>Type</th>
				<th class="select-filter">Status</th>
			</tr>
		</thead>
		<tbody>
		</tbody>
	</table>
</div>
