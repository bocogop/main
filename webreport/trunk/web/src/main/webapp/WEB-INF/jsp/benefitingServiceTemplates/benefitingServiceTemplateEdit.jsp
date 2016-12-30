<%@ include file="../shared/inc_header.jsp"%>

<%@ include file="editServiceTemplatePopup.jsp"%>
<%@ include file="editServiceRoleTemplatePopup.jsp"%>

<script type="text/javascript">
	$(function() {
		var isRole = function(r) {
			return typeof r.serviceRoleTemplates == 'undefined'
		}
		
		var theDataTable = $('#benefitingServiceTemplateList').DataTable({
			"columns" : [ {
		            "className":      'details-control',
		            "orderable":      false,
		            "data":           null,
		            "defaultContent": '<a href="#"><img class="showRowImg" alt="Show Row" src="' + imgHomePath + '/plus.gif" style="display:none" />' 
		            	+ '<img alt="Hide Row" class="hideRowImg" src="' + imgHomePath + '/minus.gif" /></a>',
		        }, {
		        	"render" : function(row, type, val, meta) {
						return val.name
					}
				}, {
					"render" : function(row, type, val, meta) {
						return val.subdivision
					}
				}, {
					"render" : function(row, type, val, meta) {
						return ''
					}
				}, {
					"render" : function(row, type, val, meta) {
						return val.gamesRelated ? 'Yes' : 'No'
					}
				}, {
					"className" : "dt-body-right",
					"render" : function(row, type, val, meta) {
						if (type === 'display') {
							return 'Active: ' + val.voterActiveCount
								+ '<br>Total: ' + val.voterTotalCount
						}
						return val.voterActiveCount
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
							actions = '<a href="javascript:reactivateBenefitingServiceTemplate('
								+ val.id + ')"><img alt="Reactivate Service Template" src="'+ imgHomePath
								+ '/switch.png" border="0" hspace="5" align="center"/></a>'
						} else {
							actions = '<a href="javascript:inactivateBenefitingServiceTemplate('
									+ val.id + ', ' + val.voterActiveCount + ')"><img alt="Inactivate Service Template" src="' + imgHomePath
									+ '/switch.png" border="0" hspace="5" align="center"/></a>'
						}
						return (type === 'display' ? actions + ' ': '') + (val.inactive ? 'Inactive' : 'Active')
					}
				}, {
					"render" : function(row, type, val, meta) {
						var actions = '<div style="margin:0 auto; text-align:left"><nobr>'

						actions += '<a href="javascript:showEditServiceTemplatePopup('
							+ val.id + ')"><img alt="Edit Service Template" src="'+ imgHomePath
							+ '/edit-small.gif" border="0" hspace="5" align="center"/></a>'
							
						if (val.voterTotalCount == 0 && val.occasionalHoursCount == 0) {
							actions += '<a href="javascript:deleteBenefitingServiceTemplate('
								+ val.id + ', ' + val.voterTotalCount + ')"><img alt="Delete Service Template" src="' + imgHomePath
								+ '/permanently_delete_18x18.png" border="0" hspace="5" align="center"/></a>'
						}
					
						actions += '<a href="javascript:showEditServiceRoleTemplatePopup(undefined, '
							+ val.id + ')"><img alt="Add Service Role Template" src="'+ imgHomePath
								+ '/btn_addUsers.png" border="0" hspace="5" align="center"/></a>'
						actions += '</nobr></div>'
						return actions
					}
				}
			],
			"createdRow": function(row, data, dataIndex) {
				if (!isRole(data)) {
			      $(row).addClass('serviceRow')
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
		
		$('#benefitingServiceTemplateList tbody').on('click', 'td.details-control', function() {
	        var tr = $(this).closest('tr')
	        var row = theDataTable.row(tr)
	 
	        if (row.child.isShown()) {
	            row.child.hide()
	            tr.find('.showRowImg').show()
	            tr.find('.hideRowImg').hide()
	        } else {
	            row.child.show()
	            tr.find('.showRowImg').hide()
	            tr.find('.hideRowImg').show()
	        }
		})
	    
		refreshBenefitingServiceTemplatesTable()
	})
	
	var benefitingServiceTemplateMap = null
	var benefitingServiceRoleTemplateMap = null
	var benefitingServiceTemplateList = null
	var countsMapForRoles = null
	var occasionalHoursMapForRoles = null
	
	function refreshBenefitingServiceTemplatesTable() {
		benefitingServiceTemplateMap = new Object()
		benefitingServiceRoleTemplateMap = new Object()
		
		$.ajax({
			url : ajaxHomePath + '/benefitingServiceTemplatesWithRoles',
			dataType : 'json',
			error : commonAjaxErrorHandler,
			success : function(response) {
				benefitingServiceTemplateList = response.benefitingServiceTemplates
				countsMapForRoles = response.voterCountsForRoles
				occasionalHoursMapForRoles = response.occasionalHoursForRoles
				
				var rArray = new Array()
				
				for (var i = 0; i < benefitingServiceTemplateList.length; i++) {
					var benefitingServiceTemplate = benefitingServiceTemplateList[i]
					benefitingServiceTemplateMap[benefitingServiceTemplate.id] = benefitingServiceTemplate
					
					var finalActiveCount = 0, finalTotalCount = 0, finalOccasionalHoursCount = 0
					for (var j = 0; j < benefitingServiceTemplate.serviceRoleTemplates.length; j++) {
						var benefitingServiceRoleTemplate = benefitingServiceTemplate.serviceRoleTemplates[j]
						var counts = countsMapForRoles[benefitingServiceRoleTemplate.id] || [0, 0]
						finalActiveCount += counts[0]
						finalTotalCount += counts[1]
						finalOccasionalHoursCount += occasionalHoursMapForRoles[benefitingServiceRoleTemplate.id] || 0
					}
					
					rArray[rArray.length] = $.extend({}, benefitingServiceTemplate, {
						voterActiveCount : finalActiveCount,
						voterTotalCount : finalTotalCount,
						occasionalHoursCount : finalOccasionalHoursCount
					})
				}

				var table = $('#benefitingServiceTemplateList').DataTable()
				table.clear()
				table.rows.add(rArray)
				table.rows().every(function(rowIndex, tableLoop, rowLoop) {
					var benefitingServiceTemplate = this.data()
					var childRows = []
					
					for (var j = 0; j < benefitingServiceTemplate.serviceRoleTemplates.length; j++) {
						var benefitingServiceRoleTemplate = benefitingServiceTemplate.serviceRoleTemplates[j]
						benefitingServiceRoleTemplateMap[benefitingServiceRoleTemplate.id] = benefitingServiceRoleTemplate
						var counts = countsMapForRoles[benefitingServiceRoleTemplate.id] || [0, 0]
						var occasionalHours = occasionalHoursMapForRoles[benefitingServiceRoleTemplate.id] || 0
						
						var childRow = $('<tr />')
						$("<td></td>").appendTo(childRow)
						$("<td></td>").appendTo(childRow)
						$('<td nowrap></td>').text(benefitingServiceRoleTemplate.name).appendTo(childRow)
						$('<td></td>').text(benefitingServiceRoleTemplate.roleType ? benefitingServiceRoleTemplate.roleType.name : '').appendTo(childRow)
						$('<td></td>').appendTo(childRow)
						$('<td align="right"></td>').html('Active: ' + counts[0] + '<br>Total: ' + counts[1]).appendTo(childRow)
						$('<td align="right"></td>').text(occasionalHours).appendTo(childRow)
						
						var activeTd = $('<td nowrap></td>')
						if (!benefitingServiceRoleTemplate.requiredAndReadOnly) {
							if (benefitingServiceRoleTemplate.inactive) {
								activeTd.html('<a href="javascript:reactivateBenefitingServiceRoleTemplate('
									+ benefitingServiceRoleTemplate.id + ')"><img src="'+ imgHomePath
									+ '/switch.png" border="0" hspace="5" /></a> Inactive')
							} else {
								activeTd.html('<a href="javascript:inactivateBenefitingServiceRoleTemplate('
										+ benefitingServiceRoleTemplate.id + ', ' + counts[0] + ')"><img src="' + imgHomePath
										+ '/switch.png" border="0" hspace="5" /></a> Active')
							}
						} else {
							activeTd.html('<img src="'+ imgHomePath
									+ '/spacer.gif" height="1" width="33" /> '
									+ (benefitingServiceRoleTemplate.inactive ? 'Inactive' : 'Active'))
						}
						activeTd.appendTo(childRow)
						
						var actions = '<td nowrap>'
						actions += '<a href="javascript:showEditServiceRoleTemplatePopup('
							+ benefitingServiceRoleTemplate.id + ', ' + benefitingServiceTemplate.id + ')"><img src="'+ imgHomePath
							+ '/edit-small.gif" border="0" hspace="5" align="center"/></a>'
						if (counts[1] == 0 && occasionalHours == 0) {
							actions += '<a href="javascript:deleteBenefitingServiceRoleTemplate('
								+ benefitingServiceRoleTemplate.id + ')"><img src="'+ imgHomePath
								+ '/permanently_delete_18x18.png" border="0" hspace="5" align="center"/></a>'
						}
						actions += '</td>'
						$(actions).appendTo(childRow)
						
						childRows.push(childRow)
					}
					this.child(childRows).show()
				})
				
				rebuildTableFilters('benefitingServiceTemplateList')
				table.draw()
				
				$("#benefitingServiceTemplateList_filter6").val('Active')
				$("#benefitingServiceTemplateList_filter6").change()
		    }
		})
	}
	
	function deleteBenefitingServiceTemplate(benefitingServiceTemplateId) {
		var fullObj = benefitingServiceTemplateMap[benefitingServiceTemplateId]
		var msg = 'Are you sure you want to delete "' + fullObj.name
			+ ($.trim(fullObj.subdivision) == '' ? '' : ' - ' + fullObj.subdivision) + '"?'
	    confirmDialog(msg,
                function() {
  					$.ajax({
					url : ajaxHomePath + '/benefitingServiceTemplate/deleteOrInactivate',
					dataType : 'json',
					data : {
						benefitingServiceTemplateId: benefitingServiceTemplateId
					},
					error : commonAjaxErrorHandler,
					success : refreshBenefitingServiceTemplatesTable
				})
        })
	}
	
	function inactivateBenefitingServiceTemplate(benefitingServiceTemplateId, volCount) {
		var fullObj = benefitingServiceTemplateMap[benefitingServiceTemplateId]
		var msg = 'Are you sure you want to inactivate "'
			+ fullObj.name + ($.trim(fullObj.subdivision) == '' ? '' : ' - ' + fullObj.subdivision) + '"?'
		if (volCount > 0)
			msg += ' <span class="redText" style="font-weight:bold"><p>There are ' + volCount
				+ ' associated voter assignment(s) that will also be inactivated and will not be'
				+ ' automatically reactivated if this template is reactivated!</span>'
	    confirmDialog(msg,
                function() {
  					$.ajax({
					url : ajaxHomePath + '/benefitingServiceTemplate/inactivate',
					dataType : 'json',
					data : {
						benefitingServiceTemplateId: benefitingServiceTemplateId
					},
					error : commonAjaxErrorHandler,
					success : refreshBenefitingServiceTemplatesTable
				})
        })
	}
	
	function reactivateBenefitingServiceTemplate(benefitingServiceTemplateId) {
		$.ajax({
			url : ajaxHomePath + '/benefitingServiceTemplate/reactivate',
			dataType : 'json',
			data : {
				benefitingServiceTemplateId: benefitingServiceTemplateId
			},
			error : commonAjaxErrorHandler,
			success : refreshBenefitingServiceTemplatesTable
		})
	}
	
	function reactivateBenefitingServiceRoleTemplate(benefitingServiceRoleTemplateId) {
		$.ajax({
			url : ajaxHomePath + '/benefitingServiceRoleTemplate/reactivate',
			dataType : 'json',
			data : {
				benefitingServiceRoleTemplateId: benefitingServiceRoleTemplateId
			},
			error : commonAjaxErrorHandler,
			success : refreshBenefitingServiceTemplatesTable
		})
	}
	
	function deleteBenefitingServiceRoleTemplate(benefitingServiceRoleTemplateId) {
		var fullObj = benefitingServiceRoleTemplateMap[benefitingServiceRoleTemplateId]
	    confirmDialog('Are you sure you want to delete "' + fullObj.name + '"?',
                function() {
  					$.ajax({
					url : ajaxHomePath + '/benefitingServiceRoleTemplate/deleteOrInactivate',
					dataType : 'json',
					data : {
						benefitingServiceRoleTemplateId: benefitingServiceRoleTemplateId
					},
					error : commonAjaxErrorHandler,
					success : refreshBenefitingServiceTemplatesTable
				})
        })
	}
	
	function inactivateBenefitingServiceRoleTemplate(benefitingServiceRoleTemplateId, volCount) {
		var fullObj = benefitingServiceRoleTemplateMap[benefitingServiceRoleTemplateId]
		
		var msg = 'Are you sure you want to inactivate "' + fullObj.name + '"?'
		if (volCount > 0)
			msg += ' <span class="redText" style="font-weight:bold"><p>There are ' + volCount
				+ ' associated voter assignment(s) that will also be inactivated and will not be'
				+ ' automatically reactivated if this role template is reactivated!</span>'
		
	    confirmDialog(msg,
                function() {
  					$.ajax({
					url : ajaxHomePath + '/benefitingServiceRoleTemplate/inactivate',
					dataType : 'json',
					data : {
						benefitingServiceRoleTemplateId: benefitingServiceRoleTemplateId
					},
					error : commonAjaxErrorHandler,
					success : refreshBenefitingServiceTemplatesTable
				})
        })
	}
</script>

<style>
table#benefitingServiceTemplateList tr.serviceRow {
	background-color: #dddddd;
}

table#benefitingServiceTemplateList {
	border-collapse: collapse;
}

table#benefitingServiceTemplateList td {
	margin: 3px;
}
</style>

<div class="clearCenter servicesContainer">
	<div align="center" style="margin-bottom: 15px">
		<table>
			<tr>
				<td><a class="buttonAnchor"
					href="javascript:showEditServiceTemplatePopup()">New
						Service Template</a></td>
		</table>
	</div>

	<table class="formatTable" id="benefitingServiceTemplateList"
		border="1"
		summary="List of Templates for Benefiting Services &amp; Roles">
		<thead>
			<tr>
				<td width="25" class="noborder"></td>
				<td class="noborder"></td>
				<td class="noborder"></td>
				<td class="noborder"></td>
				<td class="noborder" title="Filter by Games Status"></td>
				<td class="noborder"></td>
				<td class="noborder"></td>
				<td class="noborder" title="Filter by Status"></td>
				<td class="noborder"></td>
			</tr>
			<tr>
				<th width="25"></th>
				<th>Service Name</th>
				<th>Role</th>
				<th>Role Type</th>
				<th class="select-filter">Games Service</th>
				<th>Voters</th>
				<th>Occasional Hours</th>
				<th class="select-filter">Status</th>
				<th>Action</th>
			</tr>
		</thead>
		<tbody>
		</tbody>
	</table>
</div>