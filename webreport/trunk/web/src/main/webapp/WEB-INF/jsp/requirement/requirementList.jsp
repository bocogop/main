<%@ include file="../shared/inc_header.jsp"%>

<c:if test="${FORM_READ_ONLY}">
	<%@ include file="../shared/inc_modifyToView.jsp"%>
</c:if>

<script type="text/javascript">
	var isReadOnly = ${FORM_READ_ONLY}
	var scope = null;
	
	<sec:authorize
	access="hasAuthority('${PERMISSION_REQUIREMENTS_LOCAL_MANAGE}')">
		scope = 'local'
	</sec:authorize>
	<sec:authorize
	access="hasAuthority('${PERMISSION_REQUIREMENTS_GLOBAL_MANAGE}')">
		scope = 'global'
	</sec:authorize>

	$(function() {
		buildRequirementsTable('local')
		buildRequirementsTable('global')
		
		$("#requirementFacilitySelectWrapper").toggle(scope != 'local')
		$("input[name=requirementFacility][value=" + (scope == 'global' ? "G" : "F")+ "]").prop("checked", true)

		var scopeListener = function() {
			scope = $("#requirementFacilityLocal").is(":checked") ? 'local' : 'global'
			$("#requirementFacilityLocalWrapper").toggle(scope == 'local')
			$("#requirementFacilityGlobalWrapper").toggle(scope != 'local')
		}
		
		$("#requirementFacilityLocal").click(scopeListener)
		$("#requirementFacilityGlobal").click(scopeListener)
		scopeListener()
		
		refreshRequirementsTable('local')
		refreshRequirementsTable('global')
	})
	
	var requirementMap = new Array()
	
	function buildRequirementsTable(scope) {
		var theDataTable = $("#requirement" + capitalize(scope) + "List").DataTable({
			buttons: ['excel', {
				extend : 'pdfHtml5',
				orientation : 'landscape'
			}, 'print'],
			"columns" : [ {
		        	"render" : function(row, type, val, meta) {
		        		return escapeHTML(val.name)
					}
				}, {
					"render" : function(row, type, val, meta) {
						return val.description
					}
				}, {
					"render" : function(row, type, val, meta) {
						return val.applicationType == '${REQUIREMENT_APPLICATION_TYPE_ALL_VOLUNTEERS}' ? 'All Volunteers' :
							val.applicationType == '${REQUIREMENT_APPLICATION_TYPE_ROLE_TYPE}' ? 'Role Type "' + val.roleType.name + '"' :
							val.applicationType == '${REQUIREMENT_APPLICATION_TYPE_SPECIFIC_ROLES}' ? 'Specific Roles' :
								'(Unknown)'
					}
				}, {
					"render" : function(row, type, val, meta) {
						return val.dateType ? val.dateType.name : ''
					}
				}, {
					"render" : function(row, type, val, meta) {
						var actions = ''
						if (val.inactive) {
							actions = '<a href="javascript:reactivateRequirement('
								+ val.id + ', ' + (scope == 'local') + ')"><img src="'+ imgHomePath
								+ '/switch.png" alt="Reactivate Requirement" border="0" hspace="5" align="center"/></a>'
						} else {
							actions = '<a href="javascript:inactivateRequirement('
									+ val.id + ', ' + (scope == 'local') + ')"><img src="' + imgHomePath
									+ '/switch.png" alt="Inactivate Requirement" border="0" hspace="5" align="center"/></a>'
						}
						return '<nobr>' + 
							(type === 'display' && !isReadOnly ? actions + ' ' : '') +
							(val.inactive ? 'Inactive' : 'Active') +
							'</nobr>'
					}
				}
				<c:if test="${not FORM_READ_ONLY}">
				, {
					"render" : function(row, type, val, meta) {
						var actions = '<div style="margin:0 auto; text-align:left"><nobr>'
						actions += '<a href="' + homePath + '/requirementEdit.htm?id='
							+ val.id + '"><img alt="Edit Requirement" src="'+ imgHomePath
							+ '/edit-small.gif" border="0" hspace="5" align="center"/></a>'
						actions += '<a href="javascript:deleteRequirement('
							+ val.id + ', ' + (scope == 'local') + ')"><img alt="Delete Requirement" src="'+ imgHomePath
							+ '/permanently_delete_18x18.png" border="0" hspace="5" align="center" /></a>'
						actions += '</nobr></div>'
						return actions
					}
				}
				</c:if>
			],
	    	"dom": '<"top"fBi>rt<"bottom"pl><"clear">',
	    	"lengthMenu" : [ [ 20, 50, -1 ],
	    	 				[ 20, 50, "All" ] ],
			"order": [],
	    	"pageLength": 20,
	    	"pagingType": "full_numbers",
	    	"stripeClasses" : [],
		})
	}
	
	function refreshRequirementsTable(scope) {
		requirementMap[scope] = new Object()
		
		$.ajax({
			url : ajaxHomePath + '/requirements/' + scope,
			dataType : 'json',
			data : {
			},
			error : commonAjaxErrorHandler,
			success : function(response) {
				var requirements = response
				
				var rArray = new Array()
				
				for (var i = 0; i < requirements.length; i++) {
					var requirement = requirements[i]
					requirementMap[scope][requirement.id] = requirement
					rArray[rArray.length] = requirement
				}

				var tableName = 'requirement' + capitalize(scope) + 'List'
				var table = $('#' + tableName).DataTable()
				table.clear()
				table.rows.add(rArray)
				rebuildTableFilters(tableName)
				table.draw()
				
				$('#' + tableName +"_filter4").val('Active')
				$('#' + tableName + "_filter4").change()
		    }
		})
	}
	
	function inactivateRequirement(requirementId, scopeIsLocal) {
		$.ajax({
			url : ajaxHomePath + '/requirement/inactivate',
			dataType : 'json',
			data : {
				id: requirementId
			},
			error : commonAjaxErrorHandler,
			success : function() {
				refreshRequirementsTable(scopeIsLocal ? 'local' : 'global') 
			}
        })
	}
	
	function reactivateRequirement(requirementId, scopeIsLocal) {
		$.ajax({
			url : ajaxHomePath + '/requirement/reactivate',
			dataType : 'json',
			data : {
				id: requirementId
			},
			error : commonAjaxErrorHandler,
			success : function() {
				refreshRequirementsTable(scopeIsLocal ? 'local' : 'global') 
			}
		})
	}
	
	function deleteRequirement(requirementId, scopeIsLocal) {
		$.ajax({
			url : ajaxHomePath + '/requirement/deleteCheck',
			dataType : 'json',
			data : {
				id: requirementId
			},
			error : commonAjaxErrorHandler,
			success : function(volCount) {
				var msg = 'Are you sure you want to delete this requirement?'
				if (volCount > 0)
					msg += ' <span class="redText" style="font-weight:bold"><p>There are ' + volCount
						+ ' volunteer(s) associated with this requirement, whose statuses will be permanently lost!</span>'
			    confirmDialog(msg,
		                function() {
		  					$.ajax({
							url : ajaxHomePath + '/requirement/delete',
							dataType : 'json',
							data : {
								id: requirementId
							},
							error : commonAjaxErrorHandler,
							success : function() {
								refreshRequirementsTable(scopeIsLocal ? 'local' : 'global') 
							}
						})
		        })
			}
		})
	}
	
	function capitalize(s) {
		return s.charAt(0).toUpperCase() + s.slice(1)
	}
</script>

<style>
table#requirementGlobalList tr.serviceRow, table#requirementLocalList tr.serviceRow
{
	background-color: #dddddd;
}

table#requirementGlobalList, table#requirementLocalList {
	border-collapse: collapse;
}

table#requirementGlobalList td, table#requirementLocalList td {
	margin: 3px;
}

table#requirementGlobalList {
	min-width: 600px;
}
.requirementFacilityGlobalWrapper, .requirementFacilityLocalWrapper {
	min-width: 600px;
}
</style>

<div id="requirementFacilitySelectWrapper" class="clearCenter">
		<table>
			<tr>
				<td align="right"><label for='requirementFacility'>Facility:</label></td>
				<td><input type="radio" id="requirementFacilityGlobal"
					name="requirementFacility" value="G" checked="checked">National <input type="radio"
					id="requirementFacilityLocal" name="requirementFacility" value="F">
					<c:out value="${facilityContextName}" /></td>
			</tr>
		</table>
	</div>

<p>
<div id="requirementFacilityGlobalWrapper" class="clearCenter" style="max-width:75%">
 	<fieldset>
		<legend>National Requirements</legend>
	 	<div align="center" style="margin-bottom:15px">
	 		<table>
				<tr>
					<td><a class="buttonAnchor"
						href="${home}/requirementCreate.htm?scope=${REQUIREMENT_SCOPE_TYPE_GLOBAL}" style="margin-right:60px">Add Requirement</a></td>
					</tr>
			</table>
		</div>
		<table class="formatTable" id="requirementGlobalList" border="1"
			summary="List of Requirements">
			<thead>
				<tr>
					<td class="noborder" width="25%"></td>
					<td class="noborder" width="50%"></td>
					<td class="noborder" width="10%" title="Filter by Application Type"></td>
					<td class="noborder" width="10%" title="Filter by Date Type"></td>
					<td class="noborder" width="10%" title="Filter by Status"></td>
					 <c:if test="${not FORM_READ_ONLY}">
						<td class="noborder"></td>
					</c:if>
				</tr>
				<tr>
					<th>Name</th>
					<th>Description</th>
					<th class="select-filter">Application Type</th>
					<th class="select-filter">Date Type</th>
					<th class="select-filter">Status</th>
					<c:if test="${not FORM_READ_ONLY}">
						<th>Action</th>
					</c:if>
				</tr>
			</thead>
			<tbody>
			</tbody>
		</table>
	</fieldset>
</div>

<div id="requirementFacilityLocalWrapper" class="clearCenter" style="max-width:75%">
<fieldset>
	<legend>Facility Requirements</legend>
	<div align="center" style="margin-bottom:15px"> 
		<table>
			<tr>
				<td><a class="buttonAnchor"
					href="${home}/requirementCreate.htm?scope=${REQUIREMENT_SCOPE_TYPE_FACILITY}" style="margin-right:60px">Add Requirement</a></td>
			</tr>
		</table>
	</div>
	<table class="formatTable" id="requirementLocalList" border="1"
		summary="List of Requirements">
		<thead>
			<tr>
				<td class="noborder" width="25%"></td>
				<td class="noborder" width="50%"></td>
				<td class="noborder" width="10%" title="Filter by Application Type"></td>
				<td class="noborder" width="10%" title="Filter by Date Type"></td>
				<td class="noborder" width="10%" title="Filter by Status"></td>
				 <c:if test="${not FORM_READ_ONLY}">
					<td class="noborder"></td>
				</c:if>
			</tr>
			<tr>
				<th>Name</th>
				<th>Description</th>
				<th>Application Type</th>
				<th>Date Type</th>
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