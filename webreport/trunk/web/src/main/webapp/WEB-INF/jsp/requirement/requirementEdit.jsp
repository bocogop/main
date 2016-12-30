<%@ include file="../shared/inc_header.jsp"%>

<c:if test="${FORM_READ_ONLY}">
	<%@ include file="../shared/inc_modifyToView.jsp"%>
</c:if>

<script type="text/javascript">
	var isReadOnly = ${FORM_READ_ONLY}
	var precinctId = ${precinctContextId}
	var newStatusId = ${REQUIREMENT_STATUS_VALUE_NEW.id}
	var metStatusId = ${REQUIREMENT_STATUS_VALUE_MET.id}
	var initialApplicationType = "<c:out value="${command.applicationType}" />"
	var isEdit = <c:out value="${command.edit}" />
	var isGlobal = <c:out value="${command.scope == REQUIREMENT_SCOPE_TYPE_GLOBAL}" />
	var fieldChanged = false
	var numRequirementTypes = <c:out value="${fn:length(allRequirementTypes)}" />
	
	var selectedRoles = new SortedArray([])
	<c:forEach items="${command.specificRoles}" var="r">
		selectedRoles.insert("${r.id}")
	</c:forEach>
		
	var selectedRoleTemplates = new SortedArray([])
	<c:forEach items="${command.specificRoleTemplates}" var="rt">
		selectedRoleTemplates.insert("${rt.id}")
	</c:forEach>
	
	var initialStatuses = new SortedArray([])
	<c:forEach items="${command.validStatuses}" var="status">
		initialStatuses.insert("${status.id}")
	</c:forEach>
		
	$(function() {
		initRequirementEdit()
	})
	
	var roleTableInitialized = false
	var roleTemplateTableInitialized = false
	
	function initRequirementEdit() {
		$("#reqStatus" + metStatusId + ", #reqStatus" + newStatusId).prop('disabled', true).prop('checked', true)
		
		var isRole = function(r) {
			return typeof r.benefitingServiceRoles == 'undefined'
		}
		
		var isRoleTemplate = function(r) {
			return typeof r.benefitingServiceRoleTemplates == 'undefined'
		}
		
		$('#roleList').DataTable({
	    	"columns" : [ {
		        	"render" : function(row, type, val, meta) {
						return val.name
					}
				}, {
		        	"render" : function(row, type, val, meta) {
		        		if (type === 'sort' || type === 'filter') {
							for (var j = 0; j < val.benefitingServiceRoles.length; j++) {
								var benefitingServiceRole = val.benefitingServiceRoles[j]
								if (selectedRoles.search("" + benefitingServiceRole.id) != -1)
									return type === 'sort' ? '1' : 'Yes'
							}
			        		return type === 'sort' ? '2' : 'No'
		        		} else {
		        			return ''
		        		}
		        	}
				
	   			}, {
					"render" : function(row, type, val, meta) {
						return val.subdivision
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
				}
			],
			"createdRow": function(row, data, dataIndex) {
			      $(row).addClass('serviceRow');
			},
	    	"dom": '<"top"fi>rt<"bottom"pl><"clear">',
	    	 "language" : {
	    		 "emptyTable" : "",
				"zeroRecords" : ""
			},
			"order": [],
	    	"paging" : false,
			"scrollY" : "250px",
			"scrollCollapse" : true,
	    	"stripeClasses" : [],
		})
		
		$('#roleTemplateList').DataTable({
	    	"columns" : [ {
		        	"render" : function(row, type, val, meta) {
						return val.name
					}
				}, {
		        	"render" : function(row, type, val, meta) {
		        		if (type === 'sort' || type === 'filter') {
							for (var j = 0; j < val.serviceRoleTemplates.length; j++) {
								var benefitingServiceRoleTemplate = val.serviceRoleTemplates[j]
								if (selectedRoleTemplates.search("" + benefitingServiceRoleTemplate.id) != -1)
									return type === 'sort' ? '1' : 'Yes'
							}
			        		return type === 'sort' ? '2' : 'No'
		        		} else {
		        			return ''
		        		}
						}
	   			}, {
					"render" : function(row, type, val, meta) {
						return val.subdivision
					}
				}, {
					"render" : function(row, type, val, meta) {
						return ''
					}
				}
			],
			"createdRow": function(row, data, dataIndex) {
			      $(row).addClass('serviceRow');
			},
	    	"dom": '<"top"fi>rt<"bottom"pl><"clear">',
	    	 "language" : {
	    		 "emptyTable" : "",
				"zeroRecords" : ""
			},
			"order": [],
	    	"paging" : false,
			"scrollY" : "250px",
			"scrollCollapse" : true,
	    	"stripeClasses" : [],
		})
	    
		$("#submitFormButton").click(submitForm)
		
		if (isEdit) {
			var initialTypeInputId = $(".requirementApplicationRadio:checked").attr('id')
			
			$(".requirementApplicationRadio").click(function() {
				var selectedValue = $(this).attr('value')
				if (selectedValue === initialApplicationType) return
				var that = $(this)
				
				confirmDialog('After changing the type, the form will be reset. Proceed?', function() {
					$("#requirementChangeNewType").val(selectedValue)
					$("#requirementChangeNewRoleType").val($("#roleTypeInput").val())
					that.prop('checked', true)
					showSpinner('Please wait, your changes are being saved...', true)
					$("#changeTypeForm").submit()
				}, {
					cancelCallback : function() {
						$("#" + initialTypeInputId).prop('checked', true)
					}
				})
				return false
			})
		}
		
		var appTypeChanged = function() {
			var newType = $("input.requirementApplicationRadio:checked").val()
			var isSpecificRoles = newType == "${REQUIREMENT_APPLICATION_TYPE_SPECIFIC_ROLES}"
			
			var showPrecinctSpecificRoles = isSpecificRoles && !isGlobal
			$("#roleListWrapper").toggle(showPrecinctSpecificRoles)
			if (!roleTableInitialized && showPrecinctSpecificRoles) {
				refreshRoleTable()
			}
			
			var showGlobalSpecificRoleTemplates = isSpecificRoles && isGlobal
			$("#roleTemplateListWrapper").toggle(showGlobalSpecificRoleTemplates)
			if (!roleTemplateTableInitialized && showGlobalSpecificRoleTemplates) {
				refreshRoleTemplateTable()
			}
		}
		$(".requirementApplicationRadio").change(appTypeChanged)
		appTypeChanged()
		
		var dateTypeChanged = function() {
			var isNA = $("#dateTypeInput").val() == "${REQUIREMENT_DATE_TYPE_VALUE_NOT_APPLICABLE.id}"
			$("#daysNotificationInput").toggle(!isNA)
			$("#daysNotificationNA").toggle(isNA)
			
			if (isNA)
				$("#daysNotificationInput").val('')
		}
		$("#dateTypeInput").change(dateTypeChanged)
		dateTypeChanged()
		
		var typeChanged = function() {
			var isStandard = $("#typeInput").val() == "${REQUIREMENT_TYPE_STANDARD}"
			if (!isStandard) {
				var theText = $("#typeInput option:selected").text()
				$("#nameInput").val(theText)
				$("#nameInputFixed").text(theText)
			}
			$("#nameInput").toggle(isStandard)
			$("#nameInputFixed").toggle(!isStandard)
		}
		$("#typeInput").change(typeChanged)
		typeChanged()
		
		$("#requirementTypeRow").toggle(numRequirementTypes > 1)
	}
	
	function refreshRoleTable() {
		$.ajax({
			url : ajaxHomePath + '/benefitingServicesWithRoles',
			dataType : 'json',
			data : {
				precinctId: precinctId,
				bypassCounts : true,
				activeStatus : true
			},
			error : commonAjaxErrorHandler,
			success : function(response) {
				var table = $('#roleList').DataTable()
				table.clear()
				table.rows.add(response.benefitingServices)
				
				table.rows().every(function(rowIndex, tableLoop, rowLoop) {
					var benefitingService = this.data()
					var childRows = []
					
					for (var j = 0; j < benefitingService.benefitingServiceRoles.length; j++) {
						var benefitingServiceRole = benefitingService.benefitingServiceRoles[j]
						if (benefitingServiceRole.inactive) continue;
						
						var childRow = $('<tr />')
						$('<td></td>').appendTo(childRow)
						
						var checkedStr = ''
						if (selectedRoles.search("" + benefitingServiceRole.id) != -1)
							checkedStr = ' checked="checked"'
						
						$('<td><input type="checkbox" name="roleSelectItems" value="' + benefitingServiceRole.id + '"' + checkedStr + ' />'
								+ '</td>').appendTo(childRow)
						$('<td></td>').text(benefitingServiceRole.name).appendTo(childRow)
						$('<td></td>').text(benefitingServiceRole.locationDisplayName).appendTo(childRow)
						$('<td></td>').text(benefitingServiceRole.roleType ? benefitingServiceRole.roleType.name : '').appendTo(childRow)
						
						childRows.push(childRow)
					}
					this.child(childRows).show()
				})
				
				rebuildTableFilters('roleList')
				table.columns.adjust().draw()
				$(window).resize()
				$("input[type='checkbox']", "#roleList").change(function() {
					selectedRoles.remove($(this).val())
					if ($(this).prop('checked'))
						selectedRoles.insert($(this).val())
					
					var parentTr = $(this).closest('tr').prevAll('.serviceRow:first')
					if (parentTr.length) {
						var r = table.row(parentTr)
						r.invalidate()
					}
				}) 
				roleTableInitialized = true
		    }
		})
	}
	
	function refreshRoleTemplateTable() {
		$.ajax({
			url : ajaxHomePath + '/benefitingServiceTemplatesWithRoles',
			dataType : 'json',
			data : {
				bypassCounts : true,
				activeStatus : true
			},
			error : commonAjaxErrorHandler,
			success : function(response) {
				var table = $('#roleTemplateList').DataTable()
				table.clear()
				table.rows.add(response.benefitingServiceTemplates)
				
				table.rows().every(function(rowIndex, tableLoop, rowLoop) {
					var benefitingServiceTemplate = this.data()
					var childRows = []
					
					for (var j = 0; j < benefitingServiceTemplate.serviceRoleTemplates.length; j++) {
						var benefitingServiceRoleTemplate = benefitingServiceTemplate.serviceRoleTemplates[j]
						if (benefitingServiceRoleTemplate.inactive) continue;
						
						var childRow = $('<tr />')
						$('<td></td>').appendTo(childRow)
						
						var checkedStr = ''
						if (selectedRoleTemplates.search("" + benefitingServiceRoleTemplate.id) != -1)
							checkedStr = ' checked="checked"'
						
						$('<td><input type="checkbox" name="roleTemplateSelectItems" value="' + benefitingServiceRoleTemplate.id + '"' + checkedStr + ' />'
								+ '</td>').appendTo(childRow)
						$('<td></td>').text(benefitingServiceRoleTemplate.name).appendTo(childRow)
						$('<td></td>').text(benefitingServiceRoleTemplate.roleType ? benefitingServiceRoleTemplate.roleType.name : '').appendTo(childRow)
						
						childRows.push(childRow)
					}
					this.child(childRows).show()
				})
				
				rebuildTableFilters('roleTemplateList')
				table.columns.adjust().draw()
				$(window).resize()
				$("input[type='checkbox']", "#roleTemplateList").change(function() {
					selectedRoleTemplates.remove($(this).val())
					if ($(this).prop('checked'))
						selectedRoleTemplates.insert($(this).val())
					
					var parentTr = $(this).closest('tr').prevAll('.serviceRow:first')
					if (parentTr.length) {
						var r = table.row(parentTr)
						r.invalidate()
					}
				}) 
				roleTemplateTableInitialized = true
		    }
		})
	}
	
	function submitForm(evt) {
		if (!validate())
			return
		
		var missingItem = false
		for (var i = 0; isEdit && !missingItem && i < initialStatuses.array.length; i++) {
			if ($("#reqStatus" + initialStatuses.array[i]).is(":checked") == false)
				missingItem = true
		}
		
		var completeSubmission = function() {
			if (evt != null)
				doubleClickSafeguard($(evt.currentTarget))
			$('#requirementForm').submit()
		}
		
		if (missingItem) {
			confirmDialog('All voters associated to this requirement with the statuses being removed will have their status reset to "New".',
					completeSubmission)
		} else {
			completeSubmission()
		}
	}

	function validate() {
		var errors = new Array()

		/*
		if ($('#serviceTitleInput').val() == '')
			errors.push("Service title is required.")
		
		if ($('#phoneId').val() == '')
			errors.push("Primary phone is required.")
		
		if ($('#phoneId').val() != '' && !validatePhone($('#phoneId').val()))
			errors.push("Primary phone number is invalid.")
		
		if ($('#mailStopId').val() == '')
			errors.push("Mail stop is required.")
		
		if ($('#secPhoneId').val() != '' && !validatePhone($('#secPhoneId').val()))
			errors.push("Alternate phone number is invalid.")
		
		if ($('#faxId').val() != '' && !validateFax($('#faxId').val()))
			errors.push("Fax number is invalid.")

		if ($('#operCostId').val() != '' && !validateNumeric($('#operCostId').val()))
			errors.push("Operating costs is invalid.")
		
		if ($('#roleCostId').val() != '' && !validateNumeric($('#roleCostId').val()))
			errors.push("Role costs is invalid.")
		*/
		
		if (errors.length > 0)
			displayAttentionDialog("Please correct the following errors: <ul><li>"
					+ errors.join("</li><li>") + "</li></ul>");

		return errors.length == 0
	}
</script>

<style>
table#roleList tr.serviceRow, table#roleTemplateList tr.serviceRow {
	background-color: #dddddd;
}
.statusRequiredCheckbox {
	font-weight:bold;
}
@media ( max-width : 1300px) {
	div.leftHalf {
		float:none;
	}

	div.rightHalf {
		float: none;
	}
</style>

<form:form method="post" action="${home}/requirementSubmit.htm"
	id="requirementForm">

	<div class="clearCenter requirementContainer">
		<div class="leftHalf" style="margin-right: 25px">
			<fieldset>
				<legend>Requirement Basics</legend>
				<table>
					<tr>
						<td class='appFieldLabel' nowrap><label
							for='nameInput'>Name:<span
								class="invisibleRequiredFor508">*</span>
						</label></td>
						<td style="text-align: left"><span class='requdIndicator'>*</span></td>
						<td><app:input size="50" id="nameInput" path="name" /> <app:errors
								path="name" cssClass="msg-error" />
								<span id="nameInputFixed" style="display:none"></span></td>
					</tr>
					<tr id="requirementTypeRow" style="display:none">
						<td class='appFieldLabel'><label for='typeInput'>
								Type:</label></td>
						<td></td>
						<td><app:select id="typeInput" title="Select Requirement Type"
								path="type">
								<form:options items="${allRequirementTypes}"
									itemLabel="name" />
							</app:select> <app:errors path="type" cssClass="msg-error" element="div" /></td>
					</tr>
					<tr>
						<td class='appFieldLabel' nowrap><label for='descriptionInput'>Description:<span
								class="invisibleRequiredFor508">*</span></label></td>
						<td style="text-align: left"><span class='requdIndicator'>*</span></td>
						<td><app:textarea path="description" id="descriptionInput"
								rows="4" cols="50" maxlength="250" /> <app:errors
								path="description" cssClass="msg-error" /></td>
					</tr>
					<tr>
						<td class='appFieldLabel' nowrap><label for='tmsCourseId'>TMS Course ID:</label></td>
						<td style="text-align: left"></td>
						<td><app:input size="20" id="tmsCourseId" path="tmsCourseId" /> <app:errors
								path="tmsCourseId" cssClass="msg-error" /></td>
					</tr>
					<tr>
						<td class='appFieldLabel'>Active:</td>
						<td></td>
						<td><form:radiobutton path="active" label="Yes" value="true" />
						<form:radiobutton path="active" label="No" value="false" />
						 <app:errors path="active" cssClass="msg-error" element="div" /></td>
					</tr>
					<tr>
						<td class='appFieldLabel'><label for='dateTypeInput'>Date
								Type:</label></td>
						<td></td>
						<td><app:select id="dateTypeInput" title="Select Date Type"
								path="dateType">
								<form:options items="${allRequirementDateTypes}"
									itemLabel="name" itemValue="id" />
							</app:select> <app:errors path="dateType" cssClass="msg-error" element="div" /></td>
					</tr>
					<tr>
						<td class='appFieldLabel'><label for='daysNotificationInput'>Notification Period:</label></td>
						<td></td>
						<td><app:select id="daysNotificationInput" title="Select Notification Period"
								path="daysNotification">
								<form:option value="" label="Not Applicable" />
								<form:option value="30" label="30 Days" />
								<form:option value="60" label="60 Days" />
								<form:option value="90" label="90 Days" />
							</app:select> <app:errors path="daysNotification" cssClass="msg-error" element="div" />
							<span id="daysNotificationNA" style="display:none">Not Applicable</span>
							</td>
					</tr>
					<tr valign="top">
						<td class='appFieldLabel'>Valid
								Statuses:
						</td>
						<td></td>
						<td><c:forEach items="${allRequirementStatuses}" var="status">
							<c:set var="cssClass" value="" />
							<c:if test="${status.id == REQUIREMENT_STATUS_VALUE_MET.id || status.id == REQUIREMENT_STATUS_VALUE_NEW.id}">
								<c:set var="cssClass" value="statusRequiredCheckbox" />
							</c:if>
							<c:set var="checkedText" value="${command.validStatuses.contains(status) ? 'checked' : ''}" />
							<span class="statusCheckbox ${cssClass}">
								<form:checkbox id="reqStatus${status.id}" path="validStatuses" label="${status.name}" value="${status.id}" checked="${checkedText}" /> <br />
							</span>
						</c:forEach>
						
						<app:errors path="validStatuses"
								cssClass="msg-error" element="div" /></td>
					</tr>
					<tr>
						<td align="center" colspan="3">Prevent Kiosk Time Posting if not "Met":
						<form:radiobutton path="preventTimeposting" label="Yes" value="true" />
						<form:radiobutton path="preventTimeposting" label="No" value="false" />
						 <app:errors path="preventTimeposting" cssClass="msg-error" element="div" /></td>
					</tr>
				</table>
			</fieldset>
		</div>
		<div class="rightHalf" style="margin-right: 25px; min-width:650px; max-width:650px;">
			<fieldset>
				<legend>Requirement Application</legend>

				<table>
					<tr>
						<td>Apply this requirement to:
							<div style="margin-left: 20px">
								<label><form:radiobutton id="allApplicationType"
										cssClass="requirementApplicationRadio" path="applicationType" value="${REQUIREMENT_APPLICATION_TYPE_ALL_VOTERS}" />All
									Voters</label><br> <label><form:radiobutton
										id="roleTypeApplicationType" path="applicationType" value="${REQUIREMENT_APPLICATION_TYPE_ROLE_TYPE}"
										cssClass="requirementApplicationRadio" />A Role Type</label>
								<form:select id="roleTypeInput" path="roleType"
									items="${allBenefitingServiceRoleTypes}" itemLabel="name"
									itemValue="id" />
								<br> <label><form:radiobutton
										id="roleApplicationType"
										cssClass="requirementApplicationRadio" path="applicationType"
										value="${REQUIREMENT_APPLICATION_TYPE_SPECIFIC_ROLES}" />Specific 
										<c:if test="${command.scope == REQUIREMENT_SCOPE_TYPE_PRECINCT}">
											Roles
										</c:if>
										<c:if test="${command.scope == REQUIREMENT_SCOPE_TYPE_GLOBAL}">
											Role Templates
										</c:if>
										</label>
							</div>
						</td>
						<td width="25">&nbsp;</td>
						<td></td>
					</tr>
				</table>
				
				<div id="roleListWrapper" style="display:none">
				<table id="roleList" class="stripe" summary="List of Roles" width="615">
					<thead>
						<tr>
							<td width="30%" class="noborder"></td>
							<td width="5%" class="noborder" title="Filter by Selected"></td>
							<td width="30%" class="noborder"></td>
							<td width="10%" class="noborder"
								title="Filter by Service Location"></td>
							<td width="7%" class="noborder"></td>
						</tr>
						<tr>
							<th width="30%">Service Name</th>
							<th width="5%" class="select-filter"></th>
							<th width="30%">Role</th>
							<th width="10%" class="select-filter">Location</th>
							<th width="7%">Type</th>
						</tr>
					</thead>
					<tbody>
					</tbody>
				</table>
				</div>
				
				<div id="roleTemplateListWrapper" style="display:none">
				<table id="roleTemplateList" class="stripe" summary="List of Role Templates" width="615">
					<thead>
						<tr>
							<td width="30%" class="noborder"></td>
							<td width="5%" class="noborder" title="Filter by Selected"></td>
							<td width="30%" class="noborder"></td>
							<td width="7%" class="noborder"></td>
						</tr>
						<tr>
							<th width="30%">Service Name</th>
							<th width="5%" class="select-filter"></th>
							<th width="30%">Role</th>
							<th width="7%">Type</th>
						</tr>
					</thead>
					<tbody>
					</tbody>
				</table>
				</div>
			</fieldset>
		</div>
	</div>
	<div class="clearCenter requirementContainer">
		<c:if test="${not FORM_READ_ONLY}">
			<a class="buttonAnchor" id="submitFormButton" tabIndex="0">Submit</a>
		</c:if>
		<a id="cancelOperationBtn" class="buttonAnchor"
			href="${current_breadcrumb}">Cancel</a>
	</div>
</form:form>

<form method="post" action="${home}/requirementChangeType.htm" id="changeTypeForm">
	<input type="hidden" name="requirementId" value="${command.requirementId}" />
	<input type="hidden" id="requirementChangeNewType" name="requirementChangeNewType" />
	<input type="hidden" id="requirementChangeNewRoleType" name="requirementChangeNewRoleType" />
	<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
</form>