<%@ include file="../shared/inc_header.jsp"%>

<script type="text/javascript">

var roleTypeGeneralId = ${BENEFITING_SERVICE_ROLE_TYPE_VALUE_GENERAL.id}

$(function() {
	initEditServiceRoleTemplatePopup()
})

function initEditServiceRoleTemplatePopup() {
	var submitEditServiceRoleTemplate = function() {
		var name = $("#editServiceRoleTemplateName").val()
		
		var errors = new Array()
		if ($.trim(name) == '')
			errors.push('Please enter the name.')
		
		if (errors.length > 0) {
			displayAttentionDialog("Please correct the following errors: <ul><li>"
					+ errors.join("</li><li>") + "</li></ul>");
			return
		}
		
		$.ajax({
			url : ajaxHomePath + '/benefitingServiceRoleTemplate/saveOrUpdate',
			method: 'POST',
			dataType : 'json',
			data : {
				benefitingServiceRoleTemplateId : dialogEl.data('benefitingServiceRoleTemplateId'),
				benefitingServiceTemplateId : dialogEl.data('benefitingServiceTemplateId'),
				name : name,
				active : $("#editServiceRoleTemplateActive").is(":checked"),
				roleType : $("#editServiceRoleTemplateType").val()
			},
			error : commonAjaxErrorHandler,
			success : function(response) {
				$("#editServiceRoleTemplateDialog").dialog('close')
				refreshBenefitingServiceTemplatesTable()
		    }
		})
	}
	
	var dialogEl = $("#editServiceRoleTemplateDialog")
	dialogEl.dialog({
		autoOpen : false,
		modal : false,
		width : 360,
		height : 250,
		closeOnEscape : true,
		draggable : true,
		resizable : true,
		buttons : {
			'Submit' : {
				text : 'Submit',
				id : 'editServiceRoleTemplateSubmit',
				click : function() {
					doubleClickSafeguard($("#editServiceRoleTemplateSubmit"))
					submitEditServiceRoleTemplate()
				}
			},
			'Cancel' : function() {
				$(this).dialog('close')
			}
		}
	})
	
	$("#editServiceRoleTemplateDialog").show()
}

function showEditServiceRoleTemplatePopup(benefitingServiceRoleTemplateId, benefitingServiceId) {
	var isNew = typeof benefitingServiceRoleTemplateId == 'undefined'
	
	$("#editServiceRoleTemplateDialog").dialog('option', 'title', isNew ? 'New Benefiting Service Role Template' : 'Edit Benefiting Service Role Template');
	$("#editServiceRoleTemplateDialog").data('benefitingServiceRoleTemplateId', benefitingServiceRoleTemplateId || '')
	$("#editServiceRoleTemplateDialog").data('benefitingServiceTemplateId', benefitingServiceId || '')
	$("#advancedServiceRoleTemplateOptionsLink").toggle(!isNew)
	
	var benefitingServiceRoleTemplate = isNew ? null : benefitingServiceRoleTemplateMap[benefitingServiceRoleTemplateId]
	$("#editServiceRoleTemplateName").val(isNew ? '' : benefitingServiceRoleTemplate.name)
	$("#editServiceRoleTemplateType").val(isNew || !benefitingServiceRoleTemplate.roleType ? roleTypeGeneralId : benefitingServiceRoleTemplate.roleType.id)
	$("#editServiceRoleTemplateActive").prop('checked', isNew ? true : !benefitingServiceRoleTemplate.inactive)
	
	$("#editServiceRoleTemplateName").prop('disabled', isNew ? false : benefitingServiceRoleTemplate.requiredAndReadOnly)
	$("#editServiceRoleTemplateActive").prop('disabled', isNew ? false : benefitingServiceRoleTemplate.requiredAndReadOnly)
	$("#editServiceRoleTemplateType").prop('disabled', isNew ? false : benefitingServiceRoleTemplate.requiredAndReadOnly)
	
	$("#editServiceRoleTemplateDialog").dialog('open')
}

function mergeRoleTemplate() {
	var benefitingServiceTemplateId = $("#editServiceRoleTemplateDialog").data('benefitingServiceTemplateId')
	var benefitingServiceRoleTemplateId = $("#editServiceRoleTemplateDialog").data('benefitingServiceRoleTemplateId')
	showMergeRoleTemplatePopup(benefitingServiceRoleTemplateId, benefitingServiceTemplateId)
}
</script>

<div id="editServiceRoleTemplateDialog" style="display: none"
	title="Edit Benefiting Service Role Template">
	<div>
		<table>
			<tr>
				<td align="right"><label for='editServiceRoleTemplateName'>Name:</label></td>
				<td><span class="requdIndicator"
									id="nameRequired">*</span></td>
				<td><input type="text" id="editServiceRoleTemplateName" title="Type Name" maxlength="50" /></td>
			</tr>
			<tr>
				<td align="right"><label for='editServiceRoleTemplateType'>Role Type:</label></td>
				<td><span class="requdIndicator"
									id="roleTypeRequired">*</span></td>
				<td><select id="editServiceRoleTemplateType">
				<c:forEach items="${allBenefitingServiceRoleTypes}" var="type">
					<option value="${type.id}"><c:out value="${type.name}" /></option>
				</c:forEach>
				</select></td>
			</tr>
			<tr style="display:none">
				<td align="right"><input type="checkbox" id="editServiceRoleTemplateActive" value="true"></td>
				<td>Is Active</td>
			</tr>
		</table>
	</div>
	<div style="padding-top:10px">
		<a href="#" id="advancedServiceRoleTemplateOptionsLink"
			data-jq-dropdown="#editServiceRoleTemplateAdvancedOptions">Advanced
			Options...</a>
	</div>
</div>

<div id="editServiceRoleTemplateAdvancedOptions"
	class="jq-dropdown" style="display: none">
	<ul class="jq-dropdown-menu">
		<li><a href="javascript:mergeRoleTemplate()">Merge Role Template</a></li>
	</ul>
</div>

<%@ include file="mergeRoleTemplatePopup.jsp"%>
