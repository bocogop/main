<%@ include file="shared/inc_header.jsp"%>

<script type="text/javascript">
	$(function() {
		onPageLoad()
	})
</script>

<script type="text/javascript" src="${jsHome}/voterProfile.js"></script>

<c:set var="leftMaxWidth" value="750" />
<c:set var="rightMaxWidth" value="750" />

<style>
.errorField {
	border: 3px solid red;
}

.customFieldError {
	color: #f13458;
	font-weight: bold;
}

.singleAssignmentInput {
	text-wrap: none;
}

#timeEntryWrapper {
	display: inline-block;
	font-size: 14pt;
}

#timeEntryWrapper select {
	font-size: 13pt;
}

.buttonAnchor .ui-button-text {
	font-size: 12pt;
}

#timeEntryList div.DataTables_sort_wrapper {
	font-size: 12pt;
}

#timeEntryList tr {
	vertical-align: top;
}

.blueDiv {
	border: 2px solid #0033DD;
	border-radius: 15px;
	margin: 10px 20px;
	padding: 6px;
}

.blueDiv fieldset {
	border: none;
}

.blueDiv legend {
	margin-left: 20px;
	margin-top: -30px;
	background: white;
}

.blueDiv hr {
	border-top: dotted 2px;
	color: #f13458;
}

.opportunitiesDiv {
	min-width: 600px;
	max-width: 600px;
}

.bottomTitle {
	font-size: 14pt;
	line-height: 40px;
}

.userSummaryField {
	font-weight: bold;
}

.label {
	font-weight: bold;
	margin-top: -17px;
	margin-left: 20px;
	background: white;
	display: block;
	margin-bottom: 5px;
}
</style>

<form:form id="voterForm" name='voterForm' method="POST"
	action="${home}/voterSubmit.htm" onsubmit="return submitForm();">

	<div style="display: inline-block; float: left"> 
		<a id="viewProfileButton" class="buttonAnchor"
			href="${home}/postTime.htm"><img
			src="${imgHome}/big-left-arrow.png" hspace="5" align="absmiddle" />
			<spring:message code="timePosting" /></a>
	</div>
	<p>
	<div style="margin-left: 340px">
		<div class="leftHalf" style="max-width:${leftMaxWidth}px">
			<div class="blueDiv">
				<div class="label" style="width: 210px;">Voter Information</div>
				<table>
					<tr>
						<td class='appFieldLabel' nowrap><spring:message
								code="lastName" />:<span class="invisibleRequiredFor508">*</span></td>
						<td style="padding: 4px; text-align: center" width="1"><span
							class='requdIndicator'>*</span></td>
						<td><app:input id="lastNameInput" path="voter.lastName" /></td>
					</tr>
					<tr>
						<td class='appFieldLabel' nowrap><spring:message
								code="firstName" />:<span class="invisibleRequiredFor508">*</span></td>
						<td style="padding: 4px; text-align: center" width="1"><span
							class='requdIndicator'>*</span></td>
						<td><app:input id="firstNameInput" path="voter.firstName" /></td>
					</tr>
					<tr>
						<td class='appFieldLabel' nowrap><spring:message
								code="middleName" />:</td>
						<td></td>
						<td><app:input id="middleNameInput"
								path="voter.middleName" /></td>
					</tr>
					<tr>
						<td class='appFieldLabel' nowrap><spring:message
								code="suffix" />:</td>
						<td style="padding: 4px; text-align: center" width="1"><span
							class='requdIndicator'></span></td>
						<td><app:input id="suffixInput" path="voter.suffix" /></td>
					</tr>
					<tr>
						<td class='appFieldLabel'><spring:message code="nickName" />:</td>
						<td></td>
						<td><app:input path="voter.nickname" /> <app:errors
								path="voter.nickname" cssClass="msg-error" element="div" /></td>
					</tr>
					<tr>
						<td class='appFieldLabel'><spring:message code="gender" />:</td>
						<td style="padding: 4px; text-align: center" width="5%"><span
							class='requdIndicator'>*</span></td>
						<td><app:select id="genderSelect" path="voter.gender">
								<form:option value="">-- Select --</form:option>
								<form:options items="${allGenders}" itemLabel="name"
									itemValue="id" />
							</app:select></td>

					</tr>
					<tr>
						<td class='appFieldLabel' nowrap><label for='dobInput'><spring:message
									code="dateOfBirth" /><span class="invisibleRequiredFor508">*</span>
						</label></td>
						<td style="padding: 4px; text-align: center" width="5%"><span
							class='requdIndicator'>*</span></td>

						<td nowrap><app:input size="10" id="dateOfBirthInput"
								path="voter.dateOfBirth" cssClass="dateInput" /> <app:errors
								path="voter.dateOfBirth" cssClass="msg-error" element="div" /></td>
					</tr>
				</table>
			</div>
			<p>
			<div class="blueDiv">
				<div class="label" style="width:180px;">Emergency Contact</div>
				<table>
					<tr>
						<td class='appFieldLabel'>Name:</td>
						<td><app:input path="voter.emergencyContactName" /> <app:errors
								path="voter.emergencyContactName" cssClass="msg-error"
								element="div" /></td>
					</tr>
					<tr>
						<td class='appFieldLabel'>Relationship:</td>
						<td><app:input path="voter.emergencyContactRelationship" />
							<app:errors path="voter.emergencyContactRelationship"
								cssClass="msg-error" element="div" /></td>
					</tr>
					<tr>
						<td class='appFieldLabel'>Phone:</td>
						<td><app:input path="voter.emergencyContactPhone"
								cssClass="phoneextmask" /> <app:errors
								path="voter.emergencyContactPhone" cssClass="msg-error"
								element="div" /></td>
					</tr>
					<tr>
						<td class='appFieldLabel' nowrap>Alt Phone:</td>
						<td><app:input path="voter.emergencyContactPhoneAlt"
								cssClass="phoneextmask" /> <app:errors
								path="voter.emergencyContactPhoneAlt" cssClass="msg-error"
								element="div" /></td>
					</tr>
				</table>
			</div>
		</div>

		<div class="leftHalf"  style="max-width:${rightMaxWidth}px">
			<div class="blueDiv">
				<div class="label" style="width:190px;">Contact Information</div>
				<table>
					<tr>
						<td class='appFieldLabel' nowrap>Address Line 1:<span
							class="invisibleRequiredFor508">*</span></td>
						<td style="padding: 4px; text-align: center" width="1"><span
							class='requdIndicator'>*</span></td>
						<td><app:input id="addressLine1"
								path="voter.addressLine1" /> <app:errors
								path="voter.addressLine1" cssClass="msg-error" element="div" /></td>
					</tr>
					<tr>
						<td class='appFieldLabel' nowrap>Address Line 2:</td>
						<td></td>
						<td><app:input id="addressLine2"
								path="voter.addressLine2" /> <app:errors
								path="voter.addressLine2" cssClass="msg-error" element="div" /></td>
					</tr>
					<tr>
						<td class='appFieldLabel'>City:<span
							class="invisibleRequiredFor508">*</span></td>
						<td style="padding: 4px; text-align: center" width="1"><span
							class='requdIndicator'>*</span></td>
						<td><app:input id="addressCity" path="voter.city" /> <app:errors
								path="voter.city" cssClass="msg-error" element="div" /></td>
					</tr>
					<tr>
						<td class='appFieldLabel' nowrap>State:<span
							class="invisibleRequiredFor508">*</span></td>
						<td style="padding: 4px; text-align: center" width="1"><span
							class='requdIndicator'>*</span></td>
						<td nowrap><app:select id="stateSelect"
								path="voter.state">
								<form:option value="">-- Select --</form:option>
								<form:options items="${allStates}" itemLabel="name"
									itemValue="id" />
							</app:select> <app:errors path="voter.state" cssClass="msg-error"
								element="div" /></td>
					</tr>
					<tr>
						<td class='appFieldLabel' nowrap><span
							style="margin-left: 20px">Zip:</span><span
							class="invisibleRequiredFor508">*</span></td>
						<td style="padding: 4px; text-align: center" width="1"><span
							class='requdIndicator'>*</span></td>
						<td><app:input id="addressZip" path="voter.zip" size="8" />
							<app:errors path="voter.zip" cssClass="msg-error"
								element="div" /></td>
					</tr>
					<tr>
						<td class='appFieldLabel'>Email:</td>
						<td></td>
						<td><app:input id="volEmail" cssClass="emailInput"
								path="voter.email" size="30" /> <app:errors path="voter.email"
								cssClass="msg-error" element="div" /></td>
					</tr>
					<tr>
						<td class='appFieldLabel'>Phone:</td>
						<td></td>
						<td><app:input cssClass="phoneextmask" path="voter.phone" />
							<app:errors path="voter.phone" cssClass="msg-error"
								element="div" /></td>
					</tr>
					<tr>
						<td class='appFieldLabel' nowrap>Alt Phone 1:</td>
						<td></td>
						<td><app:input cssClass="phoneextmask"
								path="voter.phoneAlt" /> <app:errors
								path="voter.phoneAlt" cssClass="msg-error" element="div" /></td>
					</tr>
					<tr>
						<td class='appFieldLabel' nowrap>Alt Phone 2:</td>
						<td></td>
						<td><app:input cssClass="phoneextmask"
								path="voter.phoneAlt2" /> <app:errors
								path="voter.phoneAlt2" cssClass="msg-error" element="div" /></td>
					</tr>
				</table>
			</div>
		</div>
	</div>

	<div style="clear: both" align="center">
		<input id="submitButton" type="submit" value="Submit"
			class="alwaysEnabled" /> <a id="cancelOperationBtn"
			class="buttonAnchor" href="${home}/postTime.htm">Cancel</a>
	</div>
</form:form>