<%@ include file="shared/inc_header.jsp"%>

<script type="text/javascript">
	var invalidEmailText = "<spring:message code="voter.error.email" />"
	var pleaseCorrectText = "<spring:message code="pleaseCorrect" />"
	
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

	<div class="clearCenter">
		<div class="leftHalf" style="max-width:${leftMaxWidth}px">
			<div class="blueDiv">
				<div class="label" style="width: 170px;"><spring:message code="voterInformation" /></div>
				<table>
					<tr>
						<td class='appFieldLabel' nowrap><spring:message
								code="iGoBy" />:</td>
						<td style="padding: 4px; text-align: center" width="1"><span
							class='requdIndicator'>*</span></td>
						<td><app:input id="nicknameInput" path="voter.nickname" /></td>
					</tr>
					<tr>
						<td class='appFieldLabel' nowrap><spring:message
								code="lastName" />:</td>
						<td style="padding: 4px; text-align: center" width="1"><span
							class='requdIndicator'>*</span></td>
						<td><input id="lastNameInput" disabled="true" value="<c:out value="${command.voter.lastName}" />" /></td>
					</tr>
					<tr>
						<td class='appFieldLabel' nowrap><spring:message
								code="firstName" />:</td>
						<td style="padding: 4px; text-align: center" width="1"><span
							class='requdIndicator'>*</span></td>
						<td><input id="firstNameInput" disabled="true" value="<c:out value="${command.voter.firstName}" />" /></td>
					</tr>
					<tr>
						<td class='appFieldLabel' nowrap><spring:message
								code="middleName" />:</td>
						<td></td>
						<td><input id="middleNameInput" disabled="true" value="<c:out value="${command.voter.middleName}" />" /></td>
					</tr>
					<tr>
						<td class='appFieldLabel' nowrap><spring:message
								code="suffix" />:</td>
						<td style="padding: 4px; text-align: center" width="1"><span
							class='requdIndicator'></span></td>
						<td><input id="suffixInput" disabled="true" value="<c:out value="${command.voter.suffix}" />" /></td>
					</tr>
					<tr>
						<td class='appFieldLabel'><spring:message code="gender" />:</td>
						<td style="padding: 4px; text-align: center" width="5%"><span
							class='requdIndicator'>*</span></td>
						<td><%--<app:select id="genderSelect" path="voter.gender">
								<form:option value="">-- Select --</form:option>
								<form:options items="${allGenders}" itemLabel="name"
									itemValue="id" />
							</app:select> --%>
							<input id="genderInput" disabled="true" value="<c:out value="${command.voter.gender.name}" />" /></td>
					</tr>
					<tr>
						<td class='appFieldLabel' nowrap><label for='birthYearInput'><spring:message
									code="yearOfBirth" />
						</label></td>
						<td style="padding: 4px; text-align: center" width="5%"><span
							class='requdIndicator'>*</span></td>
						<td nowrap><input size="10" id="birthYearInput" disabled="true" value="<c:out value="${command.voter.birthYear}" />" /> <app:errors
								path="voter.birthYear" cssClass="msg-error" element="div" /></td>
					</tr>
				</table>
			</div>
		</div>

		<div class="leftHalf"  style="max-width:${rightMaxWidth}px">
			<div class="blueDiv">
				<div class="label" style="width:190px;"><spring:message code="contactInformation" /></div>
				<table>
					<tr>
						<td class='appFieldLabel' nowrap><spring:message code="address" />:<span
							class="invisibleRequiredFor508">*</span></td>
						<td style="padding: 4px; text-align: center" width="1"><span
							class='requdIndicator'>*</span></td>
						<td><input id="addressInput" disabled="true" value="<c:out value="${command.voter.address}" />" /> <app:errors
								path="voter.address" cssClass="msg-error" element="div" /></td>
					</tr>
					<tr>
						<td class='appFieldLabel'><spring:message code="city" />:<span
							class="invisibleRequiredFor508">*</span></td>
						<td style="padding: 4px; text-align: center" width="1"><span
							class='requdIndicator'>*</span></td>
						<td><input id="cityInput" disabled="true" value="<c:out value="${command.voter.city}" />" /> <app:errors
								path="voter.city" cssClass="msg-error" element="div" /></td>
					</tr>
					<tr>
						<td class='appFieldLabel' nowrap><spring:message code="state" />:<span
							class="invisibleRequiredFor508">*</span></td>
						<td style="padding: 4px; text-align: center" width="1"><span
							class='requdIndicator'>*</span></td>
						<td nowrap><%--<app:select id="stateSelect"
								path="voter.state">
								<form:option value="">-- Select --</form:option>
								<form:options items="${allStates}" itemLabel="name"
									itemValue="code" />
							</app:select>--%>
							<input id="stateInput" disabled="true" value="<c:out value="${stateMap[command.voter.state].name}" />" />
							 <app:errors path="voter.state" cssClass="msg-error"
								element="div" /></td>
					</tr>
					<tr>
						<td class='appFieldLabel' nowrap><span
							style="margin-left: 20px"><spring:message code="zip" />:</span><span
							class="invisibleRequiredFor508">*</span></td>
						<td style="padding: 4px; text-align: center" width="1"><span
							class='requdIndicator'>*</span></td>
						<td><input size="8" id="zipInput" disabled="true" value="<c:out value="${command.voter.finalZip}" />" />
							<app:errors path="voter.zip" cssClass="msg-error"
								element="div" /></td>
					</tr>
					<tr>
						<td class='appFieldLabel'><spring:message code="email" />:</td>
						<td></td>
						<td><app:input id="volEmail" cssClass="emailInput"
								path="voter.userProvidedEmail" size="30" /> <app:errors path="voter.userProvidedEmail"
								cssClass="msg-error" element="div" /></td>
					</tr>
					<tr>
						<td class='appFieldLabel'><spring:message code="phone" />:</td>
						<td></td>
						<td><app:input cssClass="phoneextmask" path="voter.userProvidedPhone" />
							<app:errors path="voter.userProvidedPhone" cssClass="msg-error"
								element="div" /></td>
					</tr>
				</table>
			</div>
		</div>
	</div>
	<div class="clearCenter" style="margin-top:15px;margin-bottom:15px">
	<i><spring:message code="lockedFields" /> (<a href="https://www.sos.state.co.us/" target="_blank">https://www.sos.state.co.us/</a>)</i>
	</div>
	<div style="clear: both" align="center">
		<input id="submitButton" type="submit" value="<spring:message code="submit" />"
			class="alwaysEnabled" /> <a id="cancelOperationBtn"
			class="buttonAnchor" href="${home}/index.htm"><spring:message code="cancel" /></a>
	</div>
</form:form>