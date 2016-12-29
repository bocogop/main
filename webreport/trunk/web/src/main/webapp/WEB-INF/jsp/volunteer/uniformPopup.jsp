<%@ include file="../shared/inc_header.jsp"%>

<script type="text/javascript">
$(function() {
	var submitUniformDetails = function() {
		var myUniformId = $("#uniformId").val()
		var uniformSize = $("#uniformSize").val()
		var uniformCount = $("#uniformCount").val()
		
		if ($.trim(uniformSize) == '') {
			displayAttentionDialog('Please enter the Size.')
			return
		}
		
		if ($.trim(uniformCount) == '') {
			displayAttentionDialog('Please enter the Number.')
			return
		}
	
		if (uniformCount && uniformCount < 1) {
			displayAttentionDialog('Please enter a uniform count greater than 0.')
			return
		}
		
		$.ajax({
			url : ajaxHomePath + '/volunteer/uniform/createOrUpdate',
			method: 'POST',
			dataType : 'json',
			data : {
				volunteerId : volunteerId,
				facilityId : $("#uniformFacilityId").val(),
				uniformId: myUniformId,
				size: uniformSize,
				count: uniformCount
			},
			error : commonAjaxErrorHandler,
			success : function(response) {
				$("#uniformDetailsDialog").dialog('close')
				refreshUniforms()
		    }
		})
	}
	
	var dialogEl = $("#uniformDetailsDialog")
	dialogEl.dialog({
		autoOpen : false,
		modal : false,
		width : 700,
		height : 300,
		closeOnEscape : true,
		draggable : true,
		resizable : true,
		buttons : {
			'Submit' : {
				text : 'Submit',
				id : 'uniformSubmit',
				click : function() {
					var facilityId = $("#uniformFacilityId").val()
					var facilityText = $("#uniformFacilityId option:selected").text();
					if (!hasActiveAssignmentAtFacility(facilityId)) {
						confirmDialog('This volunteer does not have an active assignment at ' + facilityText
								+ ', do you wish to continue?', submitUniformDetails)
					} else {
						submitUniformDetails()
					}
				}
			},
		    //'Submit' : submitUniformDetails,
			'Cancel' : function() {
				$(this).dialog('close')
			}
		}
	})
	
	var facilityEl = $("#uniformFacilityId")
	facilityEl.multiselect({
		selectedText : function(numChecked, numTotal, checkedItems) {
			return abbreviate($(checkedItems[0]).next().text())
		},
		beforeopen: function(){
			if (dialogEl.data('stationsPopulated')) return
			var curVal = facilityEl.val()
			
			$.ajax({
				url : ajaxHomePath + "/getFacilitiesWithUserPermission",
				type : "POST",
				data : {
					permission : "<c:out value="${PERMISSION_TYPE_VOLUNTEER_CREATE}" />",
					activeStatus : true
				},
				dataType : 'json',
				error : commonAjaxErrorHandler,
				success : function(results) {
					facilityEl.empty()
					var newHtml = []
					$.each(results, function(index, item) {
						if (item.id == centralOfficeId) return
						
						var selectedText = (item.id == curVal) ? ' selected="selected"' : ''
						newHtml.push('<option value="' + item.id + '"' + selectedText + '>' + item.displayName + '</option>')
					})
					facilityEl.html(newHtml.join(''))
					
					facilityEl.val(curVal)
					facilityEl.multiselect("refresh")
					dialogEl.data('stationsPopulated', true)
					
					facilityEl.multiselect("open")
				}
			})
			
			return false
	   },
		multiple : false,
		minWidth : 400
	}).multiselectfilter()
	
	$("#uniformDetailsDialog").show()
})

function showUniformDetailsPopup(uniformId) {
	var isDisabled = <c:out value="${FORM_READ_ONLY}" default="false" />
	
	var uniformObj = uniformId ? uniformData[uniformId] : null
	var hasUniformObj = uniformObj != null
	
	$("#uniformId").val(hasUniformObj ? uniformObj.id : '')
	if ($("#uniformDetailsDialog").data('stationsPopulated')) {
		$("#uniformFacilityId").val(hasUniformObj ? uniformObj.facility.id : "${facilityContextId}")
	} else {
		$("#uniformFacilityId").empty().append(
				$("<option />")
					.prop("selected", true)
					.attr("value", hasUniformObj ? uniformObj.facility.id : "${facilityContextId}")
					.text(hasUniformObj ? uniformObj.facility.displayName : "${facilityContextName}"))
	}
	$("#uniformFacilityId").multiselect("refresh")
	
	$("#uniformSize").val(hasUniformObj && uniformObj.shirtSize ? uniformObj.shirtSize.id : '')
	$("#uniformCount").val(hasUniformObj ? uniformObj.numberOfShirts : '')
	$(".uniformPopupInputs").prop('disabled', isDisabled)
	$("#uniformDetailsDialog").dialog('open')
}
</script>

<div id="uniformDetailsDialog" style="display: none"
	title="Uniform Details">
	<input type="hidden" id="uniformId" value="" />

	<div class="uniformInputFields">
		<fieldset>
			<legend>Uniform Information</legend>
			<table>
				<tr>
					<td class='appFieldLabel'>Facility:<span
						class="invisibleRequiredFor508">*</span></td>
					<td><span class='requdIndicator'>*</span></td>
					<td><div id="uniformFacilityWrapper">
								<select id="uniformFacilityId"></select>
							</div></td>
				</tr>
				<tr>
					<td class='appFieldLabel'>Size:</td>
					<td></td>
					<td><select id="uniformSize"
						class="uniformPopupInputs">
							<option value="">Please select...</option>
							<c:forEach items="${allShirtSizes}" var="shirtSize">
								<option value="${shirtSize.id}"><c:out
										value="${shirtSize.name}" /></option>
							</c:forEach>
					</select></td>
				</tr>
				<tr>
					<td class='appFieldLabel'>Number:</td>
					<td></td>
					<td><select id="uniformCount" class="uniformPopupInputs">
						<c:forEach begin="1" end="10" varStatus="loop">
							<option value="${loop.count}">${loop.count}</option>
						</c:forEach>
					</select></td>
				</tr>
			</table>
		</fieldset>
	</div>
</div>