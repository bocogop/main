<%@ include file="../shared/inc_header.jsp"%>

<script type="text/javascript">
$(function() {
	initEditServiceTemplatePopup()
})

function initEditServiceTemplatePopup() {
	var submitEditServiceTemplate = function() {
		var name = $("#editServiceTemplateName").val()
		
		var errors = new Array()
		if ($.trim(name) == '')
			errors.push('Please enter the name.')

		if (errors.length > 0) {
			displayAttentionDialog("Please correct the following errors: <ul><li>"
					+ errors.join("</li><li>") + "</li></ul>");
			return
		}
		
		$.ajax({
			url : ajaxHomePath + '/benefitingServiceTemplate/saveOrUpdate',
			method: 'POST',
			dataType : 'json',
			data : {
				benefitingServiceTemplateId : dialogEl.data('benefitingServiceTemplateId'),
				name : name,
				subdivision : $("#editServiceTemplateSubdivision").val(),
				abbreviation : $("#editServiceTemplateAbbreviation").val(),
				active : $("#editServiceTemplateActive").is(":checked"),
				gamesRelated : $("#editServiceTemplateGamesRelated").is(":checked"),
			},
			error : commonAjaxErrorHandler,
			success : function(response) {
				$("#editServiceTemplateDialog").dialog('close')
				refreshBenefitingServiceTemplatesTable()
		    }
		})
	}
	
	var dialogEl = $("#editServiceTemplateDialog")
	dialogEl.dialog({
		autoOpen : false,
		modal : false,
		width : 300,
		height : 270,
		closeOnEscape : true,
		draggable : true,
		resizable : true,
		buttons : {
			'Submit' : {
				text : 'Submit',
				id : 'editServiceTemplateSubmit',
				click : function() {
					submitEditServiceTemplate()
				}
			},
			'Cancel' : function() {
				$(this).dialog('close')
			}
		}
	})
	
	$("#editServiceTemplateDialog").show()
}

function showEditServiceTemplatePopup(benefitingServiceTemplateId) {
	var isNew = typeof benefitingServiceTemplateId == 'undefined'
	
	$("#editServiceTemplateDialog").dialog('option', 'title', isNew ? 'New Benefiting Service Template' : 'Edit Benefiting Service Template');
	$("#editServiceTemplateDialog").data('benefitingServiceTemplateId', benefitingServiceTemplateId || '')
	//$("#advancedServiceTemplateOptionsLink").toggle(!isNew)
	
	var benefitingServiceTemplate = isNew ? null : benefitingServiceTemplateMap[benefitingServiceTemplateId]
	$("#editServiceTemplateName").val(isNew ? '' : benefitingServiceTemplate.name)
	$("#editServiceTemplateSubdivision").val(isNew ? '' : benefitingServiceTemplate.subdivision)
	$("#editServiceTemplateAbbreviation").val(isNew ? '' : benefitingServiceTemplate.abbreviation)
	$("#editServiceTemplateActive").prop('checked', isNew ? true : !benefitingServiceTemplate.inactive)
	$("#editServiceTemplateGamesRelated").prop('checked', isNew ? false : benefitingServiceTemplate.gamesRelated)
	
	$("#subdivisionRow").toggle($("#editServiceTemplateSubdivision").val() != '')
	
	$("#editServiceTemplateDialog").dialog('open')
}
</script>

<div id="editServiceTemplateDialog" style="display: none"
	title="Edit Benefiting Service Template">
	<div>
		<table>
			<tr>
				<td align="right"><label for='editServiceTemplateName'>Name:</label></td>
				<td width="10"><span class="requdIndicator"
									id="nameRequired">*</span></td>
				<td><input type="text" id="editServiceTemplateName" title="Type service template name" maxlength="35" /></td>
			</tr>
			<tr id="subdivisionRow">
				<td align="right"><label for='editServiceTemplateSubdivision'>Subdivision:</label></td>
				<td></td>
				<td><input type="text" id="editServiceTemplateSubdivision" title="Type service template subdivision" 
					maxlength="30" /></td>
			</tr>
			<tr>
				<td align="right"><label for='editServiceTemplateAbbreviation'>Abbreviation:</label></td>
				<td></td>
				<td><input type="text" id="editServiceTemplateAbbreviation" title="Type service template abbreviation" 
					maxlength="7" /></td>
			</tr>
			<tr>
				<td align="right"><input type="checkbox"
					id="editServiceTemplateGamesRelated" value="true"></td>
				<td></td>
				<td>Games Service</td>
			</tr>
			<tr style="display:none">
				<td align="right"><input type="checkbox"
					id="editServiceTemplateActive" value="true"></td>
				<td></td>
				<td>Is Active</td>
			</tr>
		</table>
	</div>
	<%--
	<div style="padding-top: 10px">
		<a href="#" id="advancedServiceTemplateOptionsLink"
			data-jq-dropdown="#editServiceTemplateAdvancedOptions">Advanced
			Options...</a>
	</div>
	 --%>
</div>
<%--
<div id="editServiceTemplateAdvancedOptions"
	class="jq-dropdown" style="display: none">
	<ul class="jq-dropdown-menu">
		<li><a href="#">Convert Service to Role</a></li>
		<li><a href="#">Merge Roles</a></li>
	</ul>
</div>
--%>