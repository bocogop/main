<%@ include file="../shared/inc_header.jsp"%>

<script type="text/javascript">
$(function() {
	initLinkServicesAndRolesPopup(${precinctContextId})
})

var linkServicesArray = new SortedArray([])
var linkRolesArray = new SortedArray([])

function initLinkServicesAndRolesPopup(workingPrecinctId) {
	var hasBenefitingServiceId = function() {
		return $("#linkDialog").data('benefitingServiceId') != ''
	}
	
	var theDataTable = $('#linkServicesAndRolesTable').DataTable({
		"columns" : [ {
	            "className":      'details-control',
	            "orderable":      false,
	            "data":           null,
	            	"render" : function(row, type, val, meta) {
	            		var bs = hasBenefitingServiceId()
	            		return '<a href="#"><img class="showRowImg" alt="Show Row" src="' + imgHomePath + '/plus.gif"' + (bs ? ' style="display:none"' : '') + ' />' 
		            		+ '<img class="hideRowImg" alt="Hide Row" src="' + imgHomePath + '/minus.gif"' + (bs ? '' : ' style="display:none"') + ' /></a>'
	            	}
	        },{
				"render" : function(row, type, val, meta) {
					if (type === 'display') {
						return '<input type="checkbox" templateId="' + val.serviceTemplate.id
							+ '" id="serviceTemplate' + val.serviceTemplate.id
							+ '" class="serviceTemplateCheckbox" name="linkServiceTemplateIds" value="'
							+ val.serviceTemplate.id + '"' + (hasBenefitingServiceId() ? ' style="display:none"' : '') + ' />' + val.serviceTemplate.name
							+ ($.trim(val.serviceTemplate.subdivision) != '' ? ' - ' + val.serviceTemplate.subdivision : '')
					} else {
						return val.serviceTemplate.name
					}
				}
			}, {
				"render" : function(row, type, val, meta) {
					return val.serviceTemplate.gamesRelated ? 'Yes' : 'No'
				}
			}, {
				"render" : function(row, type, val, meta) {
					return val.serviceTemplate.abbreviation
				}
			}
		],
    	"dom": '<"top"fi>rt<"bottom"><"clear">',
		"order": [],
    	"paging" : false,
	})
	
	$('#linkServicesAndRolesTable tbody').on('click', 'td.details-control', function() {
        var tr = $(this).closest('tr')
        var row = theDataTable.row(tr)
 		
        if (row.child.isShown()) {
            row.child.hide()
            tr.find('.showRowImg').show()
            tr.find('.hideRowImg').hide()
        } else {
        	row.child.show()
        	
        	var roleCheckboxes = tr.next().find('.serviceTemplateRoleCheckbox')
        	roleCheckboxes.each(function() {
        		$(this).prop('checked', linkRolesArray.search($(this).val()) != -1)
        	})
        	
        	tr.find('.serviceTemplateCheckbox').each(syncServiceCheckbox)
            roleCheckboxes.each(syncRoleCheckbox)
            tr.find('.showRowImg').hide()
            tr.find('.hideRowImg').show()
        }
	})
    
	var submitLinkServiceAndRole = function() {
		if (linkServicesArray.array.length == 0) {
			displayAttentionDialog('Please select at least one service to add.')
			return
		}
		
		var locationId = $("#linkServicesAndRolesPhysicalLocation").val()
		if (locationId.length == 0) {
			displayAttentionDialog('Please select at least one location.')
			return
		}
		
		$.ajax({
			url : ajaxHomePath + '/benefitingService/linkServicesAndRoles',
			method: 'POST',
			dataType : 'json',
			data : {
				precinctId : $("#precinctId").val(),
				locationId : locationId,
				csrfParamName : csrfValue,
				newServices : linkServicesArray.array,
				newRoles : linkRolesArray.array
			},
			error : commonAjaxErrorHandler,
			success : function(response) {
				$("#linkDialog").dialog('close')
				refreshBenefitingServicesTable()
		    }
		})
	}
	
	var dialogEl = $("#linkDialog")
	dialogEl.dialog({
		autoOpen : false,
		modal : false,
		width : 800,
		height : 600,
		closeOnEscape : true,
		draggable : true,
		resizable : true,
		buttons : {
			'Add Local' : {
				text : 'Add Local Service',
				id : 'addCustomServiceButton',
				click : function() {
					$(this).dialog('close')
					
					if (!hasBenefitingServiceId()) {
						showEditServicePopup()
					} else {
						showEditServiceRolePopup('new', undefined, $("#linkDialog").data('benefitingServiceId'))
					}
				}
			},
			'Submit' : {
				text : 'Submit',
				id : 'linkSubmit',
				click : function() {
					doubleClickSafeguard($("#linkSubmit"))
					submitLinkServiceAndRole()
				}
			},
			'Cancel' : function() {
				$(this).dialog('close')
			}
		}
	})
	
	var locationEl = $("#linkServicesAndRolesPhysicalLocation")
	locationEl.multiselect({
		selectedText : function(numChecked, numTotal, checkedItems) {
			if (numChecked > 1)
				return numChecked + ' of ' + numTotal + ' checked'
			return abbreviate($(checkedItems[0]).next().text())
		},
		beforeopen: function(){
			if (dialogEl.data('stationsPopulated')) return
			var curVal = locationEl.val()
			
			getLocalPrecinctsForLocation($("#precinctId").val(), true, function(locations) {
				locationEl.empty()
				var newHtml = []
				newHtml.push('<option value="-1" selected="selected">Main Precinct</option>')
				
				$.each(locations, function(index, item) {
					var selectedText = (item.id == curVal) ? ' selected="selected"' : ''
					newHtml.push('<option value="' + item.id + '"' + selectedText + '>' + item.displayName + '</option>')
				})
				locationEl.html(newHtml.join(''))
				
				locationEl.val(curVal)
				locationEl.multiselect("refresh")
				dialogEl.data('stationsPopulated', true)
				
				locationEl.multiselect("open")
			})
			
			return false
	   },
		multiple : true,
		minWidth : 300
	})
	
	$("#linkDialog").show()
}

function syncServiceCheckbox() {
	changeService($(this), $(this).is(":checked"), true)
}

function syncRoleCheckbox() {
	changeRoles($(this), $(this).is(":checked"), true)
}

function changeService($checkbox, isChecked, forceEvent) {
	var wasChecked = $checkbox.is(":checked")
	$checkbox.prop('checked', isChecked)
	var val = $checkbox.val()
	
	if (isChecked) {
		if (linkServicesArray.search(val) == -1)
			linkServicesArray.insert(val)
	} else {
		linkServicesArray.remove(val)
	}
	
	if (!isChecked && (forceEvent || wasChecked)) {
		// uncheck all children
		changeRoles($(".st" + $checkbox.attr("templateId")), false)
	} else if (isChecked && (forceEvent || !wasChecked)) {
		// check required children
		changeRoles($(".st" + $checkbox.attr("templateId") + ".requiredAndReadOnly"), true)
	}
}

function changeRoles($checkboxes, isChecked, forceEvent) {
	$checkboxes.each(function() {
		var $checkbox = $(this)
		var wasChecked = $checkbox.is(":checked")
		$checkbox.prop('checked', isChecked)
		var val = $checkbox.val()
		
		if (isChecked) {
			if (linkRolesArray.search(val) == -1)
				linkRolesArray.insert(val)
		} else {
			linkRolesArray.remove(val)
		}
		
		if (isChecked && (forceEvent || !wasChecked)) {
			// ensure parent is checked
			changeService($("#serviceTemplate" + $checkbox.attr("parentTemplateId")), true)	
		}
	})
}

function refreshLinkServicesAndRolesTable() {
		var benefitingServiceId = $("#linkDialog").data('benefitingServiceId')
		
		var format = function(d) {
			var s = '<table><tr valign="top"><td align="right"><div style="padding-left:90px;"><i>Available Roles:</i></div></td><td>' +
				'<table cellpadding="5" cellspacing="0" border="0">'
				// + '<tr><td><b>Available Service Roles:</b></td></tr>'
			for (var i = 0; i < d.serviceRoleTemplates.length; i++) {
				var serviceRoleTemplate = d.serviceRoleTemplates[i]
				var isRequiredAndReadOnly = serviceRoleTemplate.requiredAndReadOnly && benefitingServiceId == ''
				
				var classes = 'serviceTemplateRoleCheckbox st' + d.serviceTemplate.id
				if (isRequiredAndReadOnly)
					classes += ' requiredAndReadOnly'
				
				var disabledText = isRequiredAndReadOnly ? " disabled" : ""
				
				s += '<tr><td><input type="checkbox" parentTemplateId="' + d.serviceTemplate.id + '" class="'
					+ classes + '" name="linkRoleTemplateIds" value="' + serviceRoleTemplate.id + '"' + disabledText
					+ ' />' + serviceRoleTemplate.name + '</td></tr>'
			}
		    s += '</table></td></tr></table>'
		    return s
		}
		
		$.ajax({
			url : ajaxHomePath + '/benefitingServiceAndRoleTemplates/assignable',
			dataType : 'json',
			data : {
				precinctId: $("#precinctId").val(),
				benefitingServiceId : benefitingServiceId,
				skipRequiredAndReadOnlyRoles : false
			},
			error : commonAjaxErrorHandler,
			success : function(response) {
				var table = $('#linkServicesAndRolesTable').DataTable()
				table.clear()
				table.rows.add(response)
				table.rows().every(function() {
					$(this.node()).find(".serviceTemplateCheckbox").change(syncServiceCheckbox)
					
					var childRow = $(format(this.data()))
					childRow.find(".serviceTemplateRoleCheckbox").change(syncRoleCheckbox)
					
					var child = this.child(childRow)
					if ($("#linkDialog").data('benefitingServiceId') != '') child.show()
				})
				rebuildTableFilters('linkServicesAndRolesTable')
				table.draw()
				
				$("#allRolesUsedMessage").toggle(response.length == 0)
		    }
		})
	}

function showLinkDetailsPopup(optionalBenefitingServiceId) {
	$("#linkDialog").data('benefitingServiceId', optionalBenefitingServiceId || '')
	$("#addCustomServiceButton span").text(optionalBenefitingServiceId ? 'Add Local Role' : 'Add Local Service')
	
	$("#linkServicesAndRolesPhysicalLocation").val('-1')
	$("#linkServicesAndRolesPhysicalLocation").multiselect("refresh")
	
	var theTable = $('#linkServicesAndRolesTable').DataTable()
	theTable.search('').columns().search('').clear().draw()
	$("#linkDialog").dialog('open')
	refreshLinkServicesAndRolesTable(optionalBenefitingServiceId)
}
</script>

<style>
#addCustomServiceButton {
	margin-right: 460px;
}
</style>

<div id="linkDialog" style="display: none"
	title="Add National Benefiting Services and Roles">
	<form id="linkForm">
		<div>
			<div id="linkServicesAndRolesPhysicalLocationDiv" class="clearCenter"
				style="padding-top: 10px; padding-bottom: 10px">
				Physical Locations: <select id="linkServicesAndRolesPhysicalLocation" multiple="multiple">
					<option value="-1" selected="selected">Main Precinct</option>
				</select>
			</div>
			<table class="formatTable" id="linkServicesAndRolesTable">
				<thead>
					<tr>
						<td width="25"></td>
						<td width="370" class="noborder" title="Filter by Service Name"></td>
						<td width="100" class="noborder" title="Filter by Games"></td>
						<td width="160" class="noborder"></td>
					</tr>
					<tr>
						<th width="25"></th>
						<th width="370">Service &amp; Roles</th>
						<th width="100" class="select-filter">Games</th>
						<th width="160">Abbreviation</th>
					</tr>
				</thead>
				<tbody>
				</tbody>
			</table>
			<div id="allRolesUsedMessage"
				style="text-align: center; padding-top: 60px; display: none">All
				national roles have already been used for this service.</div>
		</div>
	</form>
</div>