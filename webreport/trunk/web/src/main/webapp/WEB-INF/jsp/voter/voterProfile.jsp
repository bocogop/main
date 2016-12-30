<%@ include file="../shared/inc_header.jsp"%>

<c:if test="${FORM_READ_ONLY}">
	<%@ include file="../shared/inc_modifyToView.jsp"%>
</c:if>

<c:set var="terminationControlsStyle" value="" />
<c:if test="${enableTerminationControls}">
	<c:set var="terminationControlsStyle" value="alwaysEnabled" />
</c:if>

<jsp:include page="/WEB-INF/jsp/shared/inc_voterSearchPopup.jsp">
	<jsp:param name="uniqueVoterSearchPopupId"
		value="volProfCheckForDups" />
	<jsp:param name="mode" value="duplicateCheck" />
	<jsp:param name="resultCallbackMethod"
		value="menuVoterSelectedCallback" />
	<jsp:param name="disclaimerText"
		value="Please ensure the new voter does not already exist in the matches below:" />
	<jsp:param name="addButtonCallbackMethod" value="finalSubmit" />
</jsp:include>

<jsp:include page="/WEB-INF/jsp/shared/inc_assignmentSelectPopup.jsp">
	<jsp:param name="uniqueId" value="volProfileAdd" />
	<jsp:param name="resultCallbackMethod"
		value="addOrReactivateVoterAssignment" />
</jsp:include>

<jsp:include page="/WEB-INF/jsp/shared/inc_organizationSearchPopup.jsp">
	<jsp:param name="uniqueOrganizationSearchPopupId" value="volProfileAdd" />
	<jsp:param name="resultCallbackMethod"
		value="addOrReactivateOrganization" />
	<jsp:param name="includeInactiveOption" value="false" />
	<jsp:param name="mode" value="voterLink" />
</jsp:include>

<%@ include file="parkingStickerPopup.jsp"%>
<%@ include file="uniformPopup.jsp"%>

<script type="text/javascript">
	function voterRequirementUpdatedCallback() {
		refreshRequirements()
	}
	function getVoterRequirementData() {
		return voterRequirementData
	}
	function retrieveVoterRequirementsByScope(voterRequirementId, callbackFn) {
		/* We already pre-populate these on the page so just immediately callback here - CPB */
		callbackFn(voterRequirementsByScope, voterAssignmentData)
	}
</script>
<%@ include file="voterRequirementPopup.jsp"%> 

<c:set var="voterId" value="-1" />
<c:if test="${command.voter.persistent}">
	<c:set var="voterId" value="${command.voter.id}" />
</c:if>

<script type="text/javascript">
	var voterId = ${voterId}
	
	var reqAppTypeAllVoters = "<c:out value="${REQUIREMENT_APPLICATION_TYPE_ALL_VOTERS}" />"
	var reqAppTypeRoleType = "<c:out value="${REQUIREMENT_APPLICATION_TYPE_ROLE_TYPE}" />"
	var reqAppTypeSpecificRoles = "<c:out value="${REQUIREMENT_APPLICATION_TYPE_SPECIFIC_ROLES}" />"
	
	var commandFirstName = "<c:out value="${command.voter.firstName}" />"
	var commandLastName = "<c:out value="${command.voter.lastName}" />"
	var commandDob = "<c:out value="${command.voter.dateOfBirth}" />"
	
	var awardHoursMap = {}
	<c:forEach items="${allAwards}" var="award">
		awardHoursMap['${award.id}'] = ${award.awardHours}
	</c:forEach>
	
	var voterEditSites = new SortedArray([${voterEditSiteIds}]) 

	var requirementNotMetStatusId = "<c:out value="${REQUIREMENT_STATUS_VALUE_UNMET.id}" />"
	var requirementMetStatusId = "<c:out value="${REQUIREMENT_STATUS_VALUE_MET.id}" />"
	var requirementNotApplicableStatusId = "<c:out value="${REQUIREMENT_STATUS_VALUE_NOT_APPLICABLE.id}" />"
	
	var hasUnterminateWithCausePerm = false
	<sec:authorize access="hasAnyAuthority('${PERMISSION_VOTER_UNTERMINATE_BY_CAUSE}')">
		hasUnterminateWithCausePerm = true
	</sec:authorize>
	
	$(function() {
		onPageLoad(${not command.voter.persistent},
				${FORM_READ_ONLY},
				${command.voterTerminatedWithCause} && !hasUnterminateWithCausePerm,
				${not empty command.terminationDate
					or not empty command.terminationRemarks
					or command.voterTerminatedWithCause})
	})
</script>

<script type="text/javascript"
	src="${jsHome}/voterProfile/voterProfileJavascript.js"></script>

	
<spring:hasBindErrors name="command">
	<c:if test="${errors.hasFieldErrors('fullName')}">
		<script type="text/javascript">
			$(function() {
		   	 editName()
			})
		</script>
	</c:if>
</spring:hasBindErrors>

<c:set var="leftMaxWidth" value="750" />
<c:set var="rightMaxWidth" value="750" />

<style>
.voterInputFields, .voterRequirementFields {
	min-width: 400px;
}


.voterInputFields, .voterRequirementFields fieldset {
	margin: 15px;
}

.dateInput {
	width: 100px;
}

.subtableHeaderBox {
	margin-bottom: 5px;
}

.subtableHeaderBox:after {
	content: "";
	display: table;
	clear: both;
}

.basicsDiv {
	margin: 0px 20px 20px 20px;
}

.basicsDiv:after {
	content: "";
	display: table;
	clear: both;
}

.subtableHeader {
	font-weight: bold;
}

a.addAnchor {
	position: relative;
	top: -20px;
	margin-bottom: 8px;
}

div.voterInputFields div.dataTables_wrapper {
	top: -20px;
	margin-bottom: -20px;
}

.voterName {
	font-size: 12pt;
	font-weight: bold;
}

.nameInputs {
	display: none;
}

#requirementsTable .groupRow {
	/* font-weight:bold; */
	font-style:italic;
	border: 1px solid black;
}
#requirementsTable ul {
	margin-top:0px;
	margin-bottom:0px;
}

.basicsDiv .leftHalf {
	max-width: <c:out value="${leftMaxWidth}" />px;
}
.basicsDiv .rightHalf {
	max-width: <c:out value="${rightMaxWidth}" />px;
}
/* 1600 - scrollbar width of 17px - a couple for overlap */
@media ( max-width : 1581px) {
	div.leftHalf {
		float:none;
	}

	div.rightHalf {
		float: none;
	}
</style>

<c:if test="${FORM_READ_ONLY}">
<style>
a.addAnchor {
	visibility: hidden;
}
</style>
</c:if>

<c:set var="volIdOrNull" value="${command.voter.id}" />
<c:if test="${empty volIdOrNull}">
	<c:set var="volIdOrNull" value="null" />
</c:if>

<form:form method="post" action="${home}/voterSubmit.htm"
	id="voterForm"
	onsubmit="return submitForm(${command.voter.persistent}, ${volIdOrNull});">
	<div class="clearCenter basicsDiv">
		<div class="leftHalf">
			<table>
				<tr>
					<td class="fixedNameFields"><label for='lastNameInput'>Name:<span
							class="invisibleRequiredFor508">*</span>
					</label> <span class="voterName"><c:out
								value="${command.voter.displayName}" />
							<c:if test="${not empty command.titleStatus}">
								<span class="redText"><c:out value="${command.titleStatus}" /></span>
							</c:if></span> <a href="javascript:editName()"><img
							src="${imgHome}/edit-small.gif" align="absmiddle" border="0"
							alt="Show Voter Name fields" /></a></td>
					<td class="nameInputs">
						<table>
							<tr>
								<td class='appFieldLabel' nowrap>Last Name:<span
									class="invisibleRequiredFor508">*</span></td>
								<td style="padding: 4px; text-align: center" width="1"><span
									class='requdIndicator'>*</span></td>
								<td><app:input id="lastNameInput" path="voter.lastName" /></td>
								<td width="5"><img src="${imgHome}/spacer.gif" width="5"
									height="1" alt="" /></td>
								<td class='appFieldLabel' nowrap>First Name:<span
									class="invisibleRequiredFor508">*</span></td>
								<td style="padding: 4px; text-align: center" width="1"><span
									class='requdIndicator'>*</span></td>
								<td><app:input id="firstNameInput"
										path="voter.firstName" /></td>
							</tr>
							<tr>
								<td class='appFieldLabel' nowrap>Middle Name:</td>
								<td></td>
								<td><app:input id="middleNameInput"
										path="voter.middleName" /></td>
								<td width="5"><img src="${imgHome}/spacer.gif" width="5"
									height="1" alt="" /></td>
								<td class='appFieldLabel' nowrap>Suffix:</td>
								<td style="padding: 4px; text-align: center" width="1"><span
									class='requdIndicator'></span></td>
								<td><app:input id="suffixInput" path="voter.suffix" /></td>
							</tr>
							<c:if test="${not empty command.titleStatus}">
								<tr>
									<td colspan="7" align="center"><span
										class="redText" style="font-weight: bold"><c:out value="${command.titleStatus}" /></span></td>
								</tr>
							</c:if>
						</table>
					</td>
					<c:if test="${command.voter.persistent}">
						<td>
							<table>
								<tr>
									<td class='appFieldLabel' nowrap>Entry Date: </label></td>
									<td style="padding: 4px; text-align: center" width="1"></td>
									<td nowrap><wr:localDate
											date="${command.voter.entryDate}" pattern="${DATE_ONLY}" /></td>
								</tr>
								<tr>
									<td class='appFieldLabel' nowrap><img
										src="${imgHome}/spacer.gif" width="40" height="1"
										class="fixedNameFields" alt="" />Years Votering:</td>
									<td style="padding: 4px; text-align: center" width="1"><span
										class='requdIndicator'></span></td>
									<td><c:out value="${yearsVotering}" /></td>
								</tr>
							</table>
						</td>
					</c:if>
				</tr>
			</table>
		</div>
		<div class="rightHalf">
			<c:if test="${command.voter.persistent}">
				<table style="margin-left: 40px">
					<tr>
						<td class='appFieldLabel' nowrap><label for='primaryPrecinct'>Primary
								Precinct:<span class="invisibleRequiredFor508">*</span>
						</label></td>
						<td style="padding: 4px; text-align: center" width="5%"><span
							class='requdIndicator'>*</span></td>
						<td><select id="primaryPrecinct" style="display: none"></select>
							<span id="noPrecinctsAvailable" style="display: none">Please
								add at least one assignment.</span></td>
					</tr>
					<tr>
						<td class='appFieldLabel' nowrap><label for='primaryPrecinct'>Primary
								Organization:<span class="invisibleRequiredFor508">*</span>
						</label></td>
						<td style="padding: 4px; text-align: center" width="5%"><span
							class='requdIndicator'>*</span></td>
						<td><select id="primaryOrganization" style="display: none"></select>
							<span id="noOrganizationsAvailable" style="display: none">Please
								add at least one organization.</span></td>
					</tr>
				</table>
			</c:if>
		</div>
	</div>

	<c:if test="${command.voter.persistent}">
		<hr style="margin-bottom: 15px;">
	</c:if>

	<div class="clearCenter">
		<div class="leftHalf" style="max-width:${leftMaxWidth}px">
			<c:if test="${command.voter.persistent}">
				<div class="voterInputFields">
					<fieldset>
						<legend>Assignments</legend>
						<div align="right">
							<a class="addAnchor buttonAnchor"
								href="javascript:popupAssignmentSelect('volProfileAdd')">Assign</a>
						</div>
						<table class="assignmentFields stripe" id="assignmentsTable"
							summary="List of Assignments" width="${leftMaxWidth}" style="max-width:${leftMaxWidth}px">
							<thead>
								<tr>
									<td title="Filter by Name"></td>
									<td title="Filter by Precinct"></td>
									<td title="Filter by Physical Location"></td>
									<td title="Filter by Status"></td>
									<c:if test="${not FORM_READ_ONLY}">
										<td></td>
									</c:if>
								</tr>
								<tr>
									<th class="select-filter">Name</th>
									<th class="select-filter">Precinct</th>
									<th class="select-filter">Physical Location</th>
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
					
				<div class="voterRequirementFields">
					<fieldset>
						<legend>Requirements</legend>
						<table class="assignmentFields stripe" id="requirementsTable"
							summary="List of Requirements" width="${leftMaxWidth}" style="max-width:${leftMaxWidth}px">
							<thead>
								<tr>
									<th width="35%">Name</th>
									<th width="35%">Application Type</th>
									<th width="20%">Date</th>
									<th width="10%">Status</th>
									<th width="1%"></th>
								</tr>
							</thead>
							<tbody>
							</tbody>
						</table>
					</fieldset>
				</div>
			</c:if>

			<div class="voterInputFields">
				<fieldset>
					<legend>Master Record</legend>
					<div class="clearCenter">
						<div style="float: left">
							<table>
								<tr>
									<td class='appFieldLabel'>Nickname:</td>
									<td></td>
									<td><app:input path="voter.nickname" /> <app:errors
											path="voter.nickname" cssClass="msg-error" /></td>
								</tr>
								<tr>
									<td class='appFieldLabel' nowrap><label for='dobInput'>Date
											of Birth:<span class="invisibleRequiredFor508">*</span>
									</label></td>
									<td style="padding: 4px; text-align: center" width="5%"><span
										class='requdIndicator'>*</span></td>
									
									<td nowrap><app:input size="10" id="dateOfBirthInput"
											path="voter.dateOfBirth" cssClass="dateInput" /> 
											<c:if test="${command.voter.persistent}">
												<c:set var="ageStr" value="${command.voter.age}" />
												<c:if test="${command.voter.youth}">
													<c:set var="ageStr" value="${ageStr} - Youth" />
												</c:if>
												
											Age: <c:out
											value="${ageStr}" /></c:if><br> <app:errors
											path="voter.dateOfBirth" cssClass="msg-error" /></td>
								</tr>
								<tr>
									<td class='appFieldLabel' nowrap><label
										for='preferredLanguageInput'>Preferred Language:</label></td>
									<td></td>
									<td nowrap><app:select items="${allLanguages}"
											id="preferredLanguageInput"
											path="voter.preferredLanguage" itemLabel="name"
											itemValue="id" /> <app:errors
											path="voter.preferredLanguage" cssClass="msg-error" /></td>
								</tr>
							</table>
						</div>
						<div style="float: right">
							<table>
								<tr>
									<td class='appFieldLabel'>Gender:</td>
									<td style="padding: 4px; text-align: center" width="5%"><span
										class='requdIndicator'>*</span></td>
									<td><app:select id="genderSelect" path="voter.gender">
											<form:option value="">-- Select --</form:option>
											<form:options items="${allGenders}" itemLabel="name"
												itemValue="id" />
										</app:select></td>
								</tr>
								<tr>
									<td colspan="2"></td>
									<td><app:errors path="voter.gender"
											cssClass="msg-error" /></td>
								</tr>
								<tr>
									<td class='appFieldLabel'>Code:</td>
									<td></td>
									<td><c:out value="${command.voter.identifyingCode}" /></td>
								</tr>
								<tr>
									<td class='appFieldLabel' nowrap></td>
									<td></td>
									<td nowrap><form:checkbox path="voter.vaEmployee" /><label
										for='vaEmployeeInput'>Is VA Employee</label> <app:errors
											path="voter.vaEmployee" cssClass="msg-error" /></td>
								</tr>
							</table>
						</div>
					</div>
					<div class="clearCenter" style="margin-top: 8px">
						<app:textarea placeholder="[General voter remarks]"
							path="voter.remarks" id="generalRemarks" rows="3" cols="80" />
					</div>
				</fieldset>
			</div>
			<div class="voterInputFields">
				<fieldset>
					<legend>Contact Information</legend>
					<div style="float: left">
						<table>
							<tr>
								<td class='appFieldLabel' nowrap>Address Line 1:<span
									class="invisibleRequiredFor508">*</span></td>
								<td style="padding: 4px; text-align: center" width="5%"><span
									class='requdIndicator'>*</span></td>
								<td><app:input id="addressLine1"
										path="voter.addressLine1" /></td>
							</tr>
							<tr>
								<td colspan="2"></td>
								<td><app:errors path="voter.addressLine1"
										cssClass="msg-error" /></td>
							</tr>
							<tr>
								<td class='appFieldLabel' nowrap>Address Line 2:</td>
								<td></td>
								<td><app:input id="addressLine2"
										path="voter.addressLine2" /></td>
							</tr>
							<tr>
								<td colspan="2"></td>
								<td><app:errors path="voter.addressLine2"
										cssClass="msg-error" /></td>
							</tr>
							<tr>
								<td class='appFieldLabel'>City:<span
									class="invisibleRequiredFor508">*</span></td>
								<td style="padding: 4px; text-align: center" width="5%"><span
									class='requdIndicator'>*</span></td>
								<td><app:input id="addressCity" path="voter.city" /></td>
							</tr>
							<tr>
								<td colspan="2"></td>
								<td><app:errors path="voter.city" cssClass="msg-error" /></td>
							</tr>
							<tr>
								<td class='appFieldLabel' nowrap>State:<span
									class="invisibleRequiredFor508">*</span></td>
								<td style="padding: 4px; text-align: center" width="5%"><span
									class='requdIndicator'>*</span></td>
								<td nowrap><app:select id="stateSelect"
										path="voter.state">
										<form:option value="">-- Select --</form:option>
										<form:options items="${allStates}" itemLabel="name"
											itemValue="id" />
									</app:select></td>
							</tr>
							<tr>
								<td colspan="2"></td>
								<td><app:errors path="voter.state" cssClass="msg-error" /></td>
							</tr>
							<tr>
								<td class='appFieldLabel' nowrap><span
									style="margin-left: 20px">Zip:</span><span
									class="invisibleRequiredFor508">*</span></td>
								<td style="padding: 4px; text-align: center" width="5%"><span
									class='requdIndicator'>*</span></td>
								<td><app:input id="addressZip" path="voter.zip"
										size="8" /></td>
							</tr>
							<tr>
								<td colspan="2"></td>
								<td><app:errors path="voter.zip" cssClass="msg-error" /></td>
							</tr>
						</table>
					</div>
					<div style="float: left">
						<table>
							<tr>
								<td class='appFieldLabel'>Email:</td>
								<td><app:input id="volEmail" cssClass="emailInput"
										path="voter.email" /> <a
									href="javascript:emailInputContent('volEmail')"><img
										alt='Click to email voter' src="${imgHome}/envelope.jpg"
										height="14" width="18" border="0" align="absmiddle"
										style="padding-left: 4px; padding-right: 4px" /></a></td>
							</tr>
							<tr>
								<td colspan="2"><app:errors path="voter.email"
										cssClass="msg-error" /></td>
							</tr>
							<tr>
								<td class='appFieldLabel'>Phone:</td>
								<td><app:input cssClass="phoneextmask"
										path="voter.phone" /></td>
							</tr>
							<tr>
								<td colspan="2"><app:errors path="voter.phone"
										cssClass="msg-error" /></td>
							</tr>
							<tr>
								<td class='appFieldLabel' nowrap>Alt Phone 1:</td>
								<td><app:input cssClass="phoneextmask"
										path="voter.phoneAlt" /></td>
							</tr>
							<tr>
								<td colspan="2"><app:errors path="voter.phoneAlt"
										cssClass="msg-error" /></td>
							</tr>
							<tr>
								<td class='appFieldLabel' nowrap>Alt Phone 2:</td>
								<td><app:input cssClass="phoneextmask"
										path="voter.phoneAlt2" /></td>
							</tr>
							<tr>
								<td colspan="2"><app:errors path="voter.phoneAlt2"
										cssClass="msg-error" /></td>
							</tr>
						</table>
					</div>
				</fieldset>
			</div>
			
		</div>
		<div class="rightHalf" style="max-width:${rightMaxWidth}px">
			<c:if test="${command.voter.persistent}">
				<div class="voterInputFields">
					<fieldset>
						<legend> Organizations </legend>
						<div align="right">
							<a class="addAnchor buttonAnchor"
								href="javascript:popupOrganizationSearch('volProfileAdd')">Assign</a>
						</div>
						<table class="organizationFields stripe" id="organizationsTable"
							summary="List of Organizations"
							width="${rightMaxWidth}" style="max-width:${rightMaxWidth}px">
							<thead>
								<tr>
									<td width="200">Filters:</td>
									<td width="100"></td>
									<td width="50" title="Filter by Status"></td>
									<c:if test="${not FORM_READ_ONLY}">
										<td width="50"></td>
									</c:if>
								</tr>
								<tr>
									<th width="200">Name</th>
									<th width="100">Precinct</th>
									<th class="select-filter" width="50">Status</th>
									<c:if test="${not FORM_READ_ONLY}">
										<th width="50">Action</th>
									</c:if>
								</tr>
							</thead>
							<tbody>
							</tbody>
						</table>
					</fieldset>
				</div>
			</c:if>
			<div class="voterInputFields">
				<fieldset>
					<legend>Emergency Contact</legend>

					<div style="float: left">
						<table>
							<tr>
								<td class='appFieldLabel'>Name:</td>
								<td><app:input path="voter.emergencyContactName" /></td>
							</tr>
							<tr>
								<td colspan="2"><app:errors
										path="voter.emergencyContactName" cssClass="msg-error" /></td>
							</tr>
							<tr>
								<td class='appFieldLabel'>Relationship:</td>
								<td><app:input
										path="voter.emergencyContactRelationship" /></td>
								<td width="30">&nbsp;</td>
							</tr>
							<tr>
								<td colspan="2"><app:errors
										path="voter.emergencyContactRelationship"
										cssClass="msg-error" /></td>
							</tr>
						</table>
					</div>
					<div style="float: right">
						<table>
							<tr>
								<td class='appFieldLabel'>Phone:</td>
								<td><app:input path="voter.emergencyContactPhone"
										cssClass="phoneextmask" /></td>
							</tr>
							<tr>
								<td colspan="2"><app:errors
										path="voter.emergencyContactPhone" cssClass="msg-error" /></td>
							</tr>
							<tr>
								<td class='appFieldLabel' nowrap>Alt Phone:</td>
								<td><app:input path="voter.emergencyContactPhoneAlt"
										cssClass="phoneextmask" /></td>
							</tr>
							<tr>
								<td colspan="2"><app:errors
										path="voter.emergencyContactPhoneAlt" cssClass="msg-error" /></td>
							</tr>
						</table>
					</div>
				</fieldset>
			</div>
			<div class="voterInputFields">
				<c:set var="facMgmtDisabled" value="true" />
				<sec:authorize access="hasAuthority('${PERMISSION_UPDATE_VOL_PROFILE_PRECINCT_MGMT}')">
					<c:set var="facMgmtDisabled" value="false" />
				</sec:authorize>
				
				<fieldset>
					<legend>Precinct Management</legend>
					<span class='appFieldLabel'>Meals Eligible:</span>
					<app:select path="voter.mealsEligible" id="mealsEligibleInput" disabled="${facMgmtDisabled}">
						<c:forEach begin="0" end="3" varStatus="loop">
							<form:option value="${loop.index}">${loop.index}</form:option>
						</c:forEach>
					</app:select>
					
					<app:errors path="voter.mealsEligible" cssClass="msg-error" />
					<p />
					<app:textarea placeholder="*[Meal remarks - required]"
						class="placeholderRequired" path="voter.mealRemarks"
						id="mealRemarks" rows="3" cols="80" disabled="${facMgmtDisabled}" />
					<br />
					<app:errors path="voter.mealRemarks" cssClass="msg-error" />

					<c:if test="${command.voter.persistent}">
						<c:if test="${not empty command.voter.leieExclusionDate}">
							<div style="margin-bottom:10px">
								LEIE Exclusion Date: <wr:localDate date="${command.voter.leieExclusionDate}" /><br>
								<form:checkbox id="leieApprovalOverride" path="voter.leieApprovalOverride" disabled="${facMgmtDisabled}" /> LEIE False Positive
							</div>
						 </c:if>
						<div align="center" class="notTerminatedFields"
							style="display: none">
							This voter has not been terminated. 
							<c:if test="${not facMgmtDisabled}">
							<a
								style="margin-left: 25px" class="buttonAnchor"
								href="javascript:terminateVoter()">Terminate</a></c:if>
						</div>
						<table class="terminatedFields" style="display: none">
							<tr>
								<td width="90%"><span class='appFieldLabel'>Termination
										Date:</span> <span class="requdIndicator"
									id="terminationDateRequired">*</span> <app:input size="10"
										path="terminationDate" cssClass="dateInput ${terminationControlsStyle}"
										id="terminationDate" disabled="${facMgmtDisabled}" /> <app:errors
										path="terminationDate" cssClass="msg-error" /></td>
								<td nowrap><form:checkbox id="terminatedWithCauseCheckbox"
										path="voterTerminatedWithCause" cssClass="${terminationControlsStyle}"
										disabled="${facMgmtDisabled}" /> Terminated with Cause<br>
									<app:errors path="voterTerminatedWithCause"
										cssClass="msg-error" /></td>
							</tr>
							<tr>
								<td colspan="2"><app:textarea id="terminationRemarks" maxlength="1000"
										disabled="${facMgmtDisabled}"
										placeholder="[Termination remarks]"
										path="terminationRemarks" rows="3" cols="80" cssClass="${terminationControlsStyle}" /> <app:errors
										path="terminationRemarks" cssClass="msg-error" /></td>
							</tr>
						</table>
						
					</c:if>
				</fieldset>
			</div>

			<c:if test="${command.voter.persistent}">
				<div class="voterInputFields">
					<fieldset>
						<legend>Hours & Awards</legend>
						<div style="float: left">
							<table>
								<tr align="right">
									<td>Prior Year Hours:</td>
									<td><c:out value="${command.timeSummary.priorYearHours}" /></td>
								</tr>
								<tr align="right">
									<td>
									<c:set var="showLink" value="${command.voter.status.voterActive and voterHasActiveAssignmentAtCurrentPrecinct and voterHasActiveOrganizationAtCurrentPrecinct}" />
									<c:if test="${showLink}">
											<a
												href="${home}/timeEntry.htm?voterId=${command.voter.id}"
												class="appLink">Current Year Hours:</a>
										</c:if> <c:if test="${not showLink}">Current Year Hours:</c:if></td>
									<td><c:out value="${command.timeSummary.currentYearHours}" /></td>
								</tr>
								<tr align="right">
									<td>Total Hours:</td>
									<td><c:out value="${command.timeSummary.totalHours}" /></td>
								</tr>
								<c:if test="${command.timeSummary.adjustedHours > 0}">
									<tr align="right">
										<td>Adjusted Hours:</td>
										<td><c:out value="${command.timeSummary.adjustedHours}" /></td>
									</tr>
								</c:if>
							</table>
						</div>
						<div style="float: right; text-align: center; margin-left: 20px">
							<table>
								<tr>
									<td class='appFieldLabel' nowrap><label for='dobInput'>
											Last Award:<span class="invisibleRequiredFor508">*</span>
									</label></td>
									<td></td>
									<td align="left" nowrap><app:select id="awardSelect"
											path="voter.lastAward">
											<form:option value="">(none)</form:option>
											<form:options items="${allAwards}"
												itemLabel="displayNameAbbreviated" itemValue="id" />
										</app:select></td>
								</tr>
								<tr>
									<td class='appFieldLabel' nowrap><label for='dobInput'>
											Last Award Date:<span class="invisibleRequiredFor508">*</span>
									</label></td>
									<td></td>
									<td align="left" nowrap><app:input size="10"
											id="dateOfLastAwardInput" path="voter.lastAwardDate"
											cssClass="dateInput" /></td>
								</tr>
								<tr>
									<td colspan="2">&nbsp;</td>
									<td><app:errors path="voter.lastAwardDate"
											cssClass="msg-error" /></td>
								</tr>
								<%-- Hiding this for now unless business wants it re-added - CPB --%>
								<tr style="display: none">
									<td class='appFieldLabel' nowrap>Last Award Hours:</td>
									<td></td>
									<td align="left"><app:input type="number" min="0"
											style="width:80px" id="lastAwardHours"
											path="voter.lastAwardHours" /></td>
								</tr>
								<tr>
									<td class='appFieldLabel' nowrap>Last Date Votered:</td>
									<td></td>
									<td align="left"><wr:localDate
											date="${command.timeSummary.mostRecentWorkEntryDate}" /></td>
								</tr>
							</table>
						</div>
					</fieldset>
				</div>

				<div class="voterInputFields">
					<fieldset>
						<legend> Parking Stickers </legend>
						<div align="right">
							<a class="addAnchor buttonAnchor"
								href="javascript:showParkingStickerDetailsPopup()">Add</a>
						</div>
						<table class="parkingStickerFields stripe"
							id="parkingStickersTable" summary="List of Parking Stickers"
							style="max-width:${rightMaxWidth}px">
							<thead>
								<tr>
									<th width="240">Precinct</th>
									<th width="100">Sticker</th>
									<th width="100">License</th>
									<c:if test="${not FORM_READ_ONLY}">
										<th width="50">Action</th>
									</c:if>
								</tr>
							</thead>
							<tbody>
							</tbody>
						</table>
					</fieldset>
				</div>
				<div class="voterInputFields">
					<fieldset>
						<legend> Uniforms </legend>
						<div align="right">
							<a align="right" class="addAnchor buttonAnchor"
								href="javascript:showUniformDetailsPopup()">Add</a>
						</div>
						<table class="uniformFields stripe" id="uniformsTable"
							summary="List of Uniforms" style="max-width:${leftMaxWidth}px">
							<thead>
								<tr>
									<th>Precinct</th>
									<th>Size</th>
									<th>Number</th>
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
			</c:if>
		</div>

		<div style="clear: both" align="center">
			
				<input id="submitButton" type="submit" value="Submit" class="alwaysEnabled" />
			
			<a id="cancelOperationBtn" class="buttonAnchor"
				href="${current_breadcrumb}">Cancel</a>
		</div>
	</div>
</form:form>
