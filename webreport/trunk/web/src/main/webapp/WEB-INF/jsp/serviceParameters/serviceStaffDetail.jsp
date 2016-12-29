<%@ include file="../shared/inc_header.jsp"%>

<jsp:include page="/WEB-INF/jsp/shared/inc_appUserSearchPopup.jsp">
	<jsp:param name="uniqueSearchPopupId" value="staffSearch" />
	<jsp:param name="resultCallbackMethod" value="ldapCallback" />
	<jsp:param name="cancelCallbackMethod" value="ldapCancel" />
	<jsp:param name="includeLocalDB" value="true" />
	<jsp:param name="includeLDAP" value="true" />
</jsp:include>

<jsp:include page="/WEB-INF/jsp/shared/inc_appUserSearchPopup.jsp">
	<jsp:param name="uniqueSearchPopupId" value="managerSearch" />
	<jsp:param name="resultCallbackMethod" value="managerSearchCallback" />
	<jsp:param name="includeLocalDB" value="true" />
	<jsp:param name="includeLDAP" value="true" />
</jsp:include>

<script type="text/javascript">
var activeStaffTitles = []
<c:forEach items="${allActiveStaffTitles}" var="t">
	activeStaffTitles.push({
		id : ${t.id},
		name : "<c:out value="${t.name}" />"
	})
</c:forEach>

$(function() {
	$("#staffDetailsDialog").dialog({
		autoOpen : false,
		modal : true,
		width : 800,
		height : 580,
		closeOnEscape : false,
		draggable : true,
		resizable : true,
		buttons : {
			'Submit' : {
				text : 'Submit',
				id : 'staffDetailDialogSubmitBtn',
				click : submitStaffDetails
			},
		    //'Submit' : submitStaffDetails,
			'Cancel' : function() {
				$(this).dialog('close')
			},
		}
	})
	
	$("#staffDetailsDialog").show()
	/*
	 * Required to solve 508 issue not reading dialog box title. It is also
	 * required to disable dialog animation to enable this functionality
	 */
	$('#staffDetailsDialog').focus()
	
	$(['#staffVavsStartDate', '#staffVavsEndDate', '#staffRetirementEligibleDate',
	   '#staffRetirementEstimateDate']).each(function(index, o) {
		$(o).enableDatePicker({
			showOn : "button",
			buttonImage : imgHomePath + "/calendar.gif",
			buttonImageOnly : true
		})
		$(o).mask("${TWO_DIGIT_DATE_MASK}", {autoclear: false})
	})
	
})

function ldapCallback(userObj) {
	$("#newStaffUserName").val(userObj.username)
	$("#staffLastName").text(userObj.lastName)
	$("#staffFirstName").text(userObj.firstName)
	$("#staffMiddleName").text(userObj.middleName)
	$("#staffTitle").text(userObj.title)
	$("#staffDepartment").text(userObj.department)
	$("#staffOffice").text(userObj.office)
	$("#staffTelephoneNumber").text(userObj.telephoneNumber)
	$("#staffEmail").text(userObj.email)
	$("#staffEmailLink").attr('href', 'mailto:' + userObj.email)
	$("#staffEmailLink").toggle($.trim(userObj.email) != '')
	$("#staffNickName").val('')
	$("#staffId").val('')
	$(".staffDetailsDisplay").show()
}

function ldapCancel() {
	$("#staffDetailsDialog").dialog('close')
}

function managerSearchCallback(userObj) {
	$("#chiefTitle").val(userObj.title)
	$("#chiefManager").val(userObj.displayName)
	$("#chiefUserName").val(userObj.username)
}

function showStaffDetailsPopup(staffId) {
	var isDisabled = <c:out value="${FORM_READ_ONLY}" default="false" />
	
	var hasStaffId = (typeof staffId !== 'undefined')
	
	$(".staffDetailsDisplay").toggle(hasStaffId)
	$("#staffSearchButton").toggle(!hasStaffId)
	
	var sel = $("#staffVavsRole")
	sel.empty()
	sel.append($('<option value="">Please select...</option>'))
	// populate initial list of active values
	for (var i = 0; i < activeStaffTitles.length; i++)
		sel.append($('<option value="' + activeStaffTitles[i].id + '"></option>').text(activeStaffTitles[i].name))
		
	if (hasStaffId) {
		 var fullObj = staffList[staffId]
		
		 if (fullObj.staffTitle.inactive)
		 	sel.append($('<option value="' + fullObj.staffTitle.id + '"></option>').text(fullObj.staffTitle.name + " (Inactive)"))
		 
		$("#staffId").val(staffId)
		$("#staffLastName").text(fullObj.userInfo.lastName)
		$("#staffFirstName").text(fullObj.userInfo.firstName)
		$("#staffMiddleName").text(fullObj.userInfo.middleName)
		$("#staffEmail").text(fullObj.userInfo.emailAddress)
		$("#staffEmailLink").attr('href', 'mailto:' + fullObj.userInfo.emailAddress)
		$("#staffEmailLink").toggle($.trim(fullObj.userInfo.emailAddress) != '')
		$("#staffEmailNotifications").prop('checked', fullObj.emailNotifications)
		$("#staffTitle").text(fullObj.userInfo.title)
		$("#staffDepartment").text(fullObj.userInfo.department)
		$("#staffOffice").text(fullObj.userInfo.office)
		$("#staffTelephoneNumber").text(fullObj.userInfo.phone)
		
		$("#staffNickName").val(fullObj.nickName)
		$("#staffNamePrefix").val(fullObj.namePrefix)
		$("#staffVavsRole").val(fullObj.staffTitle ? fullObj.staffTitle.id : '')
		$("#staffGrade").val(fullObj.grade)
		$("#staffVavsStartDate").val(fullObj.vavsStartDate)
		$("#staffVavsEndDate").val(fullObj.vavsEndDate)
		$("#staffRetirementEligibleDate").val(fullObj.retirementEligibleDate)
		$("#staffRetirementEstimateDate").val(fullObj.retirementEstimateDate)
		$("input[name=vavsLeadership][value=" + (fullObj.vavsLeadership ? "Yes" : "No")
		                              + "]").prop("checked", true)
		$("#staffComment").val(fullObj.comment)
		
		$.each(['staffNickName','staffNamePrefix','staffVavsRole','staffGrade','staffVavsStartDate','staffVavsEndDate',
		        'staffRetirementEligibleDate','staffRetirementEstimateDate','staffComment','vavsLeadershipYes',
		        'vavsLeadershipNo','staffDetailDialogSubmitBtn'], function(index, idVal) {
			var element = $("#" + idVal)
			element.prop('disabled', isDisabled)
		})
	}
	
	$("#staffDetailsDialog").dialog('open')
	if (!hasStaffId) {
		
		$("#staffLastName").text('')
		$("#staffFirstName").text('')
		$("#staffMiddleName").text('')
		$("#staffNickName").val('')
		$("#staffNamePrefix").val('')
		$("#staffVavsRole").val('')
		$("#staffGrade").val('')
		$("#staffVavsStartDate").val('')
		$("#staffVavsEndDate").val('')
		$("#staffRetirementEligibleDate").val('')
		$("#staffRetirementEstimateDate").val('') 
		$("#staffComment").val('') 
		$("input[name=vavsLeadership][value=No"
		                              + "]").prop("checked", true)
		popupAppUserSearch('staffSearch')
	}
}

function validateLocalDate() {
	var errors = new Array()
	
	if ($('#staffVavsStartDate').val() != '' && !validateDate($('#staffVavsStartDate').val())) {
		errors.push("VAVS Start Date is invalid.");
	}
	
	if ($('#staffVavsEndDate').val() != '' && !validateDate($('#staffVavsEndDate').val())) {
		errors.push("VAVS End Date is invalid.");
	}
	
	if ($('#staffRetirementEligibleDate').val() != '' && !validateDate($('#staffRetirementEligibleDate').val())) {
		errors.push("Date Eligible for Retirement is invalid.");
	}
		
	if ($('#staffRetirementEstimateDate').val() != '' && !validateDate($('#staffRetirementEstimateDate').val())) {
		errors.push("Estimated Date of Retirement is invalid.");
	}
	
	
	if (errors.length > 0)
		displayAttentionDialog("Please correct the following errors: <ul><li>" + errors.join("</li><li>") + "</li></ul>");

	return errors.length == 0
}
	
function submitStaffDetails() {
	var myServiceStaffId = $("#staffId").val()
	var myUsername = $("#newStaffUserName").val()
	
	/* Run some validations - CPB */
	if (myServiceStaffId == '' &&  myUsername == '') {
		displayAttentionDialog('Please search and select a staff user.')
		return
	}
	
	var myStaffVavsRole = $("#staffVavsRole").val()
	if (myStaffVavsRole == '') {
		displayAttentionDialog('VAVS role is required.')
		return
	}
	
	var myVavsLeadership = $("input[type='radio'][name='vavsLeadership']:checked").val()
	if (!myVavsLeadership) {
		displayAttentionDialog('Please select a VAVS leadership option.')
		return
	}
	
	if (!validateLocalDate())
		return
		
	/* If validations pass, submit to server - CPB */
	$.ajax({
		url : ajaxHomePath + '/voluntaryServiceStaffCreateOrUpdate',
		method: 'POST',
		dataType : 'json',
		data : {
			serviceStaffId: myServiceStaffId,
			newStaffUserName: myUsername,
			nickName: $("#staffNickName").val(),
			staffNamePrefix: $("#staffNamePrefix").val(),
			staffGrade: $("#staffGrade").val(),
			staffVavsRole: myStaffVavsRole,
			staffVavsStartDate: $("#staffVavsStartDate").val(),
			staffVavsEndDate: $("#staffVavsEndDate").val(),
			staffRetirementEligibleDate: $("#staffRetirementEligibleDate").val(),
			staffRetirementEstimateDate: $("#staffRetirementEstimateDate").val(),
			staffIsVavsLeadership: myVavsLeadership,
			staffComment: $("#staffComment").val(),
			staffEmailNotifications: $("#staffEmailNotifications").is(':checked')
		},
		error : commonAjaxErrorHandler,
		success : function(response) {
			$("#staffDetailsDialog").dialog('close')
			/* generated by inc_jqueryTable.jsp, see below */
			refreshStaffTable()
	    }
	})
}
</script>

<div id="staffDetailsDialog" style="display: none" title="Staff Details">
	<input type="hidden" id="staffId" value="" /> <input type="hidden"
		id="newStaffUserName" value="" />


	<div id="staffSearchButton"
		style="text-align: center; margin: 0 auto; display: none">
		<a class="buttonAnchor"
			href="javascript:popupAppUserSearch('staffSearch')">Search</a> <br>
		<td>Click "Search" to Search a Staff from LDAP Server.</td>
	</div>

	<div class="serviceStaffInputFields staffDetailsDisplay">
		<fieldset>
			<div class="leftHalf">
				<legend>Staff Information</legend>
				<table>
					<tr>
						<td class='appFieldLabel'>Last Name:</td>
						<td></td>
						<td><span class="textWrap" id="staffLastName"></span></td>
					</tr>
					<tr>
						<td class='appFieldLabel'>First Name:</td>
						<td></td>
						<td><span class="textWrap" id="staffFirstName"></span></td>
					</tr>
					<tr>
						<td class='appFieldLabel'>Middle Name/Initial:</td>
						<td></td>
						<td><span class="textWrap" id="staffMiddleName"></td>
					</tr>
					<tr>
						<td class='appFieldLabel'>Job Title:</td>
						<td></td>
						<td><span class="textWrap" id="staffTitle"></td>
					</tr>
					<tr>
						<td class='appFieldLabel'>Department:</td>
						<td></td>
						<td><span class="textWrap" id="staffDepartment"></td>
					</tr>
				</table>
			</div>
			<div class="rightHalf">
				<table>
					<tr>
						<td class='appFieldLabel' nowrap><label for='staffNickName'>Nickname:</label></td>
						<td></td>
						<td><input type="text" id="staffNickName"
							title="Type nickname" maxlength="20" /></td>
					</tr>
					<tr>
						<td class='appFieldLabel' nowrap><label for='staffNamePrefix'>Prefix:</label></td>
						<td></td>
						<td><input type="text" id="staffNamePrefix"
							title="Type prefix" maxlength="10" /></td>
					</tr>
					<tr>
						<td class='appFieldLabel' nowrap>Office:</td>
						<td></td>
						<td><span class="textWrap" id="staffOffice"></td>
					</tr>
					<tr>
						<td class='appFieldLabel' nowrap>Business Phone:</td>
						<td></td>
						<td><span class="textWrap" id="staffTelephoneNumber"></td>
					</tr>
					<tr>
						<td class='appFieldLabel' nowrap>Email Address:</td>
						<td></td>
						<td nowrap><span id="staffEmail"></span>&nbsp;<a
							id="staffEmailLink" style="display: none" href=""><img
								alt='Click to email volunteer' src="${imgHome}/envelope.jpg"
								height="14" width="18" border="0" align="absmiddle"
								style="padding-left: 4px; padding-right: 4px" /></a></td>
					</tr>
					<tr>
						<td align="right"><input type="checkbox" id="staffEmailNotifications" title="Email Notifications" /></td>
						<td></td>
						<td nowrap>Email Notifications</td>
					</tr>
				</table>
			</div>
		</fieldset>

		<fieldset class="staffDetailsDisplay">
			<legend>Voluntary Service</legend>
			<table>
				<tr>
					<td class='appFieldLabel' nowrap><label for='staffVavsRole'>VAVS
							Role:</label><span class="invisibleRequiredFor508">*</span></td>
					<td><span class='requdIndicator'>*</span></td>
					<td colspan="4"><select id="staffVavsRole">
							<option value="">Please select...</option>
							<c:forEach items="${allStaffTitles}" var="title">						
								<option 
								<c:if test="${title.inactive}"><c:out value="disabled" /></c:if>
								value="${title.id}"><c:out
										value="${title.name}" /></option>
										
							</c:forEach>
					</select></td>
				</tr>
				<tr>
					<td class='appFieldLabel' nowrap><label for='staffGrade'>Grade:</label></td>
					<td></td>
					<td><input type="text" id="staffGrade" value="" maxlength="6" /></td>
					<td width="15" rowspan="3">&nbsp;</td>
					<td class='appFieldLabel' nowrap>VAVS Leadership Board?<span
						class="invisibleRequiredFor508">*</span></td>
					<td><span class='requdIndicator'>*</span></td>
					<td><nobr>
							<input type="radio" name="vavsLeadership" id="vavsLeadershipYes"
								value="Yes" />Yes <input type="radio" name="vavsLeadership"
								id="vavsLeadershipNo" value="No" />No
						</nobr></td>
				</tr>
				<tr>
					<td class='appFieldLabel' nowrap><label
						for='staffVavsStartDate'>VAVS Start Date:</label></td>
					<td></td>
					<td><input size="15" id="staffVavsStartDate" /></td>
					<td class='appFieldLabel' nowrap><label
						for='staffRetirementEligibleDate'>Date Eligible for
							Retirement:</label></td>
					<td></td>
					<td><input size="15" id="staffRetirementEligibleDate" /></td>
				</tr>
				<tr>
					<td class='appFieldLabel' nowrap><label for='staffVavsEndDate'>VAVS
							End Date:</label></td>
					<td></td>
					<td><input size="15" id="staffVavsEndDate" /></td>
					<td class='appFieldLabel' nowrap><label
						for='staffRetirementEstimateDate'>Estimated Date of
							Retirement:</label></td>
					<td></td>
					<td><input size="15" id="staffRetirementEstimateDate" /></td>
				</tr>
			</table>
		</fieldset>
	</div>
	<div class="serviceStaffInputFields">
		<fieldset class="staffDetailsDisplay">
			<legend>Comments</legend>
			<textarea rows="5" cols="80" id="staffComment" maxlength="250"
				title="type comments"></textarea>
		</fieldset>
	</div>
</div>