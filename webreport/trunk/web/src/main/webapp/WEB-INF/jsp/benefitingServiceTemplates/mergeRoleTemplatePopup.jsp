<%@ include file="../shared/inc_header.jsp"%>

<script type="text/javascript">
$(function() {
	initMergeRoleTemplatePopup()
})

function initMergeRoleTemplatePopup() {
	var isRoleTemplate = function(r) {
		return typeof r.serviceRoleTemplates == 'undefined'
	}
	
	var submitMergeRoleTemplate = function() {
		var selectedEl = $("input[name='mergeRoleTemplateSelectItem']:checked")
		var selectedVal = selectedEl.val()
		if (!selectedVal) {
			displayAttentionDialog('Please select a target role template.')
			return
		}
		
		var fromTemplateId = dialogEl.data('fromBenefitingServiceTemplateId')
		var fromRoleTemplateId = dialogEl.data('fromBenefitingServiceRoleTemplateId')
		var fromBs = benefitingServiceTemplateMap[fromTemplateId]
		var fromBsr = benefitingServiceRoleTemplateMap[fromRoleTemplateId]
		var fromCounts = countsMapForRoles[fromRoleTemplateId] || [0, 0]
		var fromOccasionalHours = occasionalHoursMapForRoles[fromRoleTemplateId] || 0
		
		var toTemplateId = selectedEl.next().val()
		var toRoleTemplateId = selectedVal
		var toBs = benefitingServiceTemplateMap[toTemplateId]
		var toBsr = benefitingServiceRoleTemplateMap[toRoleTemplateId]
		var toCounts = countsMapForRoles[toRoleTemplateId] || [0, 0]
		var toOccasionalHours = occasionalHoursMapForRoles[toRoleTemplateId] || 0
		
		var msg = 'Please confirm you wish to perform the following merge operation:<p>'
			+ '<table>'
			+ '	<tr style="font-weight:bold" align="center"><td>From Role Template:</td><td width="30" rowspan="2">&nbsp;</td><td>To Role Template:</td></tr>'
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
			+ '</table><p align="center"><span class="redText">This operation may take a long time.<br>Please wait until the operation completes before refreshing the screen.</span></p>'
			
		confirmDialog(msg, function() {
			$.ajax({
				url : ajaxHomePath + '/benefitingServiceRoleTemplate/merge',
				method: 'POST',
				dataType : 'json',
				data : {
					fromBenefitingServiceRoleTemplateId : dialogEl.data('fromBenefitingServiceRoleTemplateId'),
					toBenefitingServiceRoleTemplateId : selectedVal,
				},
				error : commonAjaxErrorHandler,
				success : function(response) {
					$("#mergeRoleTemplateDialog").dialog('close')
					$("#editServiceRoleTemplateDialog").dialog('close')
					refreshBenefitingServiceTemplatesTable()
			    }
			})
		}, {
			width: 900,
			height: 350
		})
	}
	
	var dialogEl = $("#mergeRoleTemplateDialog")
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
				id : 'mergeRoleTemplateSubmit',
				click : function() {
					doubleClickSafeguard($("#mergeRoleTemplateSubmit"))
					submitMergeRoleTemplate()
				}
			},
			'Cancel' : function() {
				$(this).dialog('close')
			}
		}
	})
	
	var theDataTable = $('#mergeRoleTemplateList').DataTable({
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
					return val.inactive ? 'Inactive' : 'Active'
				}
			}
		],
		"createdRow": function(row, data, dataIndex) {
			if (!isRoleTemplate(data)) {
		      $(row).addClass('serviceTemplateRow');
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
	
	$("#mergeRoleTemplateDialog").show()
}
	
function refreshMergeRoleTemplateTable() {
	var fromRoleTemplateId = $("#mergeRoleTemplateDialog").data('fromBenefitingServiceRoleTemplateId')
	
	var table = $('#mergeRoleTemplateList').DataTable()
	table.clear()
	table.rows.add(benefitingServiceTemplateList)
	
	/* First, populate every item in benefitingServiceRoleTemplateMap. We need to do this first so we can look up the "from" role above. CPB */
	table.rows().every(function(rowIndex, tableLoop, rowLoop) {
		var benefitingServiceTemplate = this.data()
		
		for (var j = 0; j < benefitingServiceTemplate.serviceRoleTemplates.length; j++) {
			var benefitingServiceRoleTemplate = benefitingServiceTemplate.serviceRoleTemplates[j]
			benefitingServiceRoleTemplateMap[benefitingServiceRoleTemplate.id] = benefitingServiceRoleTemplate
		}
	})
	
	var fromRoleTemplate = benefitingServiceRoleTemplateMap[fromRoleTemplateId]
	
	/* Next, recreate table rows */
	table.rows().every(function(rowIndex, tableLoop, rowLoop) {
		var benefitingServiceTemplate = this.data()
		var childRows = []
		
		for (var j = 0; j < benefitingServiceTemplate.serviceRoleTemplates.length; j++) {
			var benefitingServiceRoleTemplate = benefitingServiceTemplate.serviceRoleTemplates[j]
			
			/* Don't allow a merge from an active role to an inactive role - CPB */
			if (!fromRoleTemplate.inactive && benefitingServiceRoleTemplate.inactive)
				continue;
			
			var childRow = $('<tr />')
			$('<td></td>').appendTo(childRow)
			if (fromRoleTemplateId == benefitingServiceRoleTemplate.id) {
				$('<td></td>').appendTo(childRow)
			} else {
				$('<td><input type="radio" name="mergeRoleTemplateSelectItem" value="' + benefitingServiceRoleTemplate.id + '" />'
						+ '<input type="hidden" value="' + benefitingServiceTemplate.id + '" /></td>').appendTo(childRow)
			}
			$('<td></td>').text(benefitingServiceRoleTemplate.name).appendTo(childRow)
			$('<td></td>').text(benefitingServiceRoleTemplate.inactive ? 'Inactive' : 'Active').appendTo(childRow)
			
			childRows.push(childRow)
		}
		this.child(childRows).show()
	})
	
	rebuildTableFilters('mergeRoleTemplateList')
	
	$("select", "#statusFilter").val('Active')
	$("select", "#statusFilter").change()
	
	table.draw()
}

function showMergeRoleTemplatePopup(benefitingServiceRoleTemplateId, benefitingServiceTemplateId) {
	$("#mergeRoleTemplateDialog").data('fromBenefitingServiceTemplateId', benefitingServiceTemplateId)
	$("#mergeRoleTemplateDialog").data('fromBenefitingServiceRoleTemplateId', benefitingServiceRoleTemplateId)
	$('#mergeRoleTemplateList').DataTable().clear().draw()
	
	$("#mergeRoleTemplateDialog").dialog('open')
	showSpinner('Loading templates, please wait...')
	setTimeout(function(){
		refreshMergeRoleTemplateTable()
		hideSpinner()
	},400)
}
</script>

<style>
table#mergeRoleTemplateList tr.serviceTemplateRow {
	background-color: #dddddd;
}

table#mergeRoleTemplateList {
	border-collapse: collapse;
}

table#mergeRoleTemplateList td {
	margin: 3px;
}
</style>

<div id="mergeRoleTemplateDialog${uid}" style="display: none"
	title="Select a Target Role Template">
	<table class="formatTable" id="mergeRoleTemplateList" border="1"
		summary="List of Role Templates">
		<thead>
			<tr>
				<td class="noborder" width="40%"></td>
				<td width="5%" class="noborder"></td>
				<td class="noborder" width="40%""></td>
				<td class="noborder" width="15%" id="statusFilter" title="Filter by Status"></td>
			</tr>
			<tr>
				<th>Service Template Name</th>
				<th></th>
				<th>Role Template</th>
				<th class="select-filter">Status</th>
			</tr>
		</thead>
		<tbody>
		</tbody>
	</table>
</div>
