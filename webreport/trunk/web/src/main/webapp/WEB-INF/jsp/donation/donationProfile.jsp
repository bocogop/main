<%@ include file="../shared/inc_header.jsp"%>

<jsp:include page="/WEB-INF/jsp/shared/inc_donorSearchPopup.jsp">
	<jsp:param name="uniqueDonorSearchPopupId" value="changeDonor" />
	<jsp:param name="mode" value="search" />
	<jsp:param name="resultCallbackMethod"
		value="changeDonorSelectedCallback" />
</jsp:include>
<jsp:include page="/WEB-INF/jsp/shared/inc_organizationSearchPopup.jsp">
	<jsp:param name="uniqueOrganizationSearchPopupId"
		value="donationSearch" />
	<jsp:param name="resultCallbackMethod"
		value="affiliateOrganizationSelectedCallback" />
	<jsp:param name="includeInactiveOption" value="true" />
	<jsp:param name="mode" value="search" />
</jsp:include>

<%@ include file="/WEB-INF/jsp/donation/donationDetail.jsp"%>

<c:if test="${FORM_READ_ONLY}">
	<%@ include file="../shared/inc_modifyToView.jsp"%>
</c:if>

<c:set var="organizationNotBranch"
	value="${command.donor.organization.scale == 'Organization'}" />

<script type="text/javascript">
	var isReadOnly = <c:out value="${FORM_READ_ONLY}" default="false"/>
	var donationSummaryPersistent = <c:out value="${command.donationSummary.persistent}" default="false"/>
	var donationSummaryId = ${command.donationSummary.persistent ? command.donationSummary.id
				: 'undefined'}
	var ackAddressFilled = ${command.ackAddressFilled}
	var ackDatePopulated = ${not empty command.donationSummary.acknowledgementDate &&  !(command.donationSummary.acknowledgementDate < command.donationSummary.donationDate) }
	var donorTypeIsIndividualOrOrg = ${command.donor.donorType.id == '1' || command.donor.donorType.id == '4' }
	
	var printReceipt = <c:out value="${printReceipt}" default="-1" />
	var printMemo = <c:out value="${printMemo}" default="-1" />
	var printThankYou = <c:out value="${printThankYou}" default="-1" />
	var printFormat = "<c:out value="${printFormat}" default="PDF" />"
	
	$(function() {
		initDonationProfile(printReceipt, printMemo, printThankYou, printFormat)
	})
</script>

<script type="text/javascript" src="${jsHome}/donationProfile.js"></script>

<style>
div.individualDisplayFields, div.volunteerDisplayFields, div.organizationDisplayFields
	{
	min-width: 680px;
	max-width: 850px;
}

div.donationInputFields {
	min-width: 680px;
	max-width: 1000px;
}
</style>

<%@ include file="inc_printDonationSummaryPopup.jsp"%>

<form:form method="post" action="${home}/donationSummarySubmit.htm"
	id="donationProfileForm">
	<c:if
		test="${not command.donor.volunteer.persistent && (command.donor.donorType.id == '1' || command.donor.donorType.id == '6')}">
		<%@ include file="inc_individual.jsp"%>
	</c:if>

	<c:if test="${command.donor.volunteer.persistent}">
		<%@ include file="inc_volunteer.jsp"%>
	</c:if>

	<c:if
		test="${command.donor.donorType.id == '4' && not empty command.donor.organization}">
		<%@ include file="inc_organization.jsp"%>
	</c:if>

	<c:if
		test="${command.donor.donorType.id == '2' || command.donor.donorType.id == '3' || command.donor.donorType.id == '5'}">
		<%@ include file="inc_otherTypes.jsp"%>
	</c:if>


	<div class="clearCenter donationInputFields">
		<fieldset>
			<legend>Donation</legend>

			<table width="100%">
				<tr>
					<td class='appFieldLabel' nowrap>Facility:</td>
					<td></td>
					<td><c:out
							value="${command.donationSummary.facility.displayName}" /></td>
					<td class='appFieldLabel' nowrap><label
						for='donationId'>Donation ID:</label></td>
					<td style="text-align: left"></td>
					<td><c:out value="${command.donationSummary.id}" default="(new)" /></td>
				</tr>
				<tr>
					<td class='appFieldLabel' nowrap><label
						for='donationDateInput'>Donation Date:<span
							class="invisibleRequiredFor508">*</span>
					</label></td>
					<td style="text-align: left"><span class='requdIndicator'>*</span></td>

					<td><app:input path="donationSummary.donationDate"
							id="donationDate" size="15" class="donationInput" /> <app:errors
							path="donationSummary.donationDate" cssClass="msg-error" /></td>

					<td class='appFieldLabel' nowrap><label
						for='donationTypeSelect'>Donation Type:<span
							class="invisibleRequiredFor508">*</span>
					</label></td>
					<td style="text-align: left"><span class='requdIndicator'>*</span></td>

					<td nowrap><app:select id="donationTypeSelect"
							path="donationSummary.donationType" itemLabel="donationType"
							itemValue="id" class="donationInput">
							<form:option value="-1" label="-- Select --" />
							<form:options items="${allDonationTypes}"
								itemLabel="donationType" itemValue="id" />
						</app:select> <app:errors path="donationSummary.donationType"
							cssClass="msg-error" /></td>
				</tr>

				<tr class="checkDonationType" style="display: none">
					<td class='appFieldLabel' nowrap><label for='checkNumInput'>Check
							Number:<span class="invisibleRequiredFor508">*</span>
					</label></td>
					<td style="text-align: left"><span class='requdIndicator'>*</span></td>

					<td><app:input path="donationSummary.checkNumber"
							id="checkNumber" class="donationInput" size="15" maxlength="15" />
						<app:errors path="donationSummary.checkNumber"
							cssClass="msg-error" /></td>

					<td class='appFieldLabel' nowrap>Check Date:</td>
					<td></td>
					<td><app:input path="donationSummary.checkDate" id="checkDate"
							class="donationInput" size="15" /> <app:errors
							path="donationSummary.checkDate" cssClass="msg-error" /></td>
				</tr>



				<tr class="creditCardDonationType" style="display: none">
					<td class='appFieldLabel' nowrap><label for='cardTypeSelect'>Card
							Type: <span class="invisibleRequiredFor508">*</span>
					</label></td>
					<td style="text-align: left"><span class='requdIndicator'>*</span></td>

					<td nowrap><app:select id="cardTypeSelect"
							path="donationSummary.stdCreditCardType" itemLabel="name"
							itemValue="id" class="donationInput">
							<form:option value="-1" label="-- Select --" />
							<form:options items="${allCreditCardTypes}" itemLabel="name"
								itemValue="id" />
						</app:select> <app:errors path="donationSummary.stdCreditCardType"
							cssClass="msg-error" /></td>

					<td class='appFieldLabel' nowrap>Confirmation Number:</td>
					<td></td>
					<td><app:input path="donationSummary.creditCardTransactionId"
							id="confirmatiomNumber" class="donationInput" size="20"
							maxlength="20" /> <app:errors
							path="donationSummary.creditCardTransactionId"
							cssClass="msg-error" /></td>
				</tr>

				<tr class="ePayDonationType" style="display: none">
					<td class='appFieldLabel' nowrap><label
						for='epayTrackingIdInput'>E-Pay Tracking Number:<span
							class="invisibleRequiredFor508">*</span>
					</label></td>
					<td style="text-align: left"><span class='requdIndicator'>*</span></td>

					<td><app:input path="donationSummary.epayTrackingID"
							id="epayTrackingId" class="donationInput" size="15"
							maxlength="15" /> <app:errors
							path="donationSummary.epayTrackingID" cssClass="msg-error" /></td>
				</tr>

			</table>

			<div id="persistentJQueryTable" style="display: none">
				<c:if test="${command.donationSummary.persistent}">
					<c:if test="${not FORM_READ_ONLY}">
						<div align="center" style="margin-top: 25px">
							<a class="buttonAnchor" id="createDonationButton"
								href="javascript:showDonationDetailsPopup()">Assign GPF</a>
						</div>
					</c:if>
					<div id="JQueryTable"
						style="max-width: 70%; margin: auto; margin-top: 10px; clear: both">

						<table class="formatTable" id="donationDetailsList" border="1"
							summary="List of Donations">
							<thead>
								<tr>
									<th>General Post Fund</th>
									<th>Donation Amount</th>
									<c:if test="${not FORM_READ_ONLY}">
										<th>Action</th>
									</c:if>
								</tr>
							</thead>
							<tfoot>
								<tr>
									<td align="right">Total:</td>
									<td align="right"></td>
									<c:if test="${not FORM_READ_ONLY}">
										<td></td>
									</c:if>
								</tr>
							</tfoot>
							<tbody>
							</tbody>
						</table>
					</div>
				</c:if>
			</div>
			<div id="notPersistentWithoutItemActivity">
				<c:if test="${not command.donationSummary.persistent}">
					<div class="hiddenGpfTableForItemAndActivity" id="normalTable"
						style="max-width: 70%; margin: auto; margin-top: 10px; clear: both; display: none">

						<table class="formatTable" id="donationDetailsForNewDonation"
							border="1" summary="List of Donations">
							<thead>
								<tr>
									<th>General Post Fund</th>
									<th>Donation Amount $</th>
								</tr>
							</thead>
							<tbody>
								<tr>
									<td><span class="invisibleRequiredFor508">*</span><span
										class="requdIndicator">*</span> <app:select id="gpf1"
											path="donationDetail1.donGenPostFund"
											itemLabel="generalPostFund" itemValue="id"
											class="donationInput">
											<form:option value="-1" label="--Select--" />
											<form:options items="${allDonGenPostFunds}"
												itemLabel="generalPostFund" itemValue="id" />
										</app:select> <app:errors path="donationDetail1.donGenPostFund"
											cssClass="msg-error" /></td>
									<td align="left" nowrap><span
										class="invisibleRequiredFor508">*</span><span
										class="requdIndicator">*</span> <app:input
											path="donationDetail1.donationValue" id="donationAmount1"
											class="currency donationInput" maxlength="13" />
								</tr>
								<tr>
									<td><span style="padding-left: 12px" /> <app:select
											id="gpf2" path="donationDetail2.donGenPostFund"
											itemLabel="generalPostFund" itemValue="id"
											class="donationInput">
											<form:option value="-1" label="--Select--" />
											<form:options items="${allDonGenPostFunds}"
												itemLabel="generalPostFund" itemValue="id" />
										</app:select> <app:errors path="donationDetail2.donGenPostFund"
											cssClass="msg-error" /></td>
									<td><span style="padding-left: 8px" /> <app:input
											path="donationDetail2.donationValue" id="donationAmount2"
											class="currency donationInput" maxlength="13" />
								</tr>
								<tr>
									<td><span style="padding-left: 12px" /> <app:select
											id="gpf3" path="donationDetail3.donGenPostFund"
											itemLabel="generalPostFund" itemValue="id"
											class="donationInput">
											<form:option value="-1" label="--Select--" />
											<form:options items="${allDonGenPostFunds}"
												itemLabel="generalPostFund" itemValue="id" />
										</app:select> <app:errors path="donationDetail3.donGenPostFund"
											cssClass="msg-error" /></td>
									<td><span style="padding-left: 8px" /> <app:input
											path="donationDetail3.donationValue" id="donationAmount3"
											class="currency donationInput" maxlength="13" />
								</tr>
							</tbody>
						</table>
					</div>
				</c:if>
			</div>

			<div class="gpfTableForItemAndActivity" id="normalTable2"
				style="max-width: 60%; margin: auto; margin-top: 20px; clear: both; display: none">
				<table class="formatTable" id="donationDetailsForNewDonation1"
					border="1" summary="List of Donations">
					<thead>
						<tr>
							<th>General Post Fund</th>
							<th>Donation Amount $</th>
						</tr>
					</thead>
					<tbody>
						<tr>
							<td><span style="padding-left: 12px" />None</td>
							<td align="left" nowrap><span
								class="invisibleRequiredFor508">*</span><span
								class="requdIndicator">*</span> <app:input
									path="donationDetail4.donationValue" id="donationAmount4"
									class="currency donationInput" maxlength="13" />
						</tr>
					</tbody>
				</table>
			</div>


			<div style="margin-top: 20px">
				<table width="100%">
					<tr>
						<td class="appFieldLabel fieldServiceReceipt"
							style="display: none">Field Service Receipt:</td>
						<td></td>
						<td><app:input path="donationSummary.fieldServiceReceipt"
								id="fieldServiceReceiptId"
								class="fieldServiceReceipt donationInput" style="display: none"
								size="20" maxlength="12" /> <app:errors
								path="donationSummary.fieldServiceReceipt" cssClass="msg-error" /></td>
						<td class="noServiceReceipt" style="display: none">
					</tr>
					<tr>
						<td class='appFieldLabel' nowrap>Acknowledgement Date:</td>
						<td></td>
						<td><app:input path="donationSummary.acknowledgementDate"
								id="acknowledgementDate" size="20" class="donationInput" /> <app:errors
								path="donationSummary.acknowledgementDate" cssClass="msg-error" /></td>

						<td class='appFieldLabel' nowrap><label
							for='donationReferenceSelect'>Reference:</label></td>
						<td><app:select id="donationReferenceSelect"
								path="donationSummary.donReference"
								itemLabel="donationReference" itemValue="id"
								class="donationInput">
								<form:option value="-1" label="    " />
								<form:options items="${allDonationReferences}"
									itemLabel="donationReference" itemValue="id" />
							</app:select> <app:errors path="donationSummary.donReference"
								cssClass="msg-error" /></td>
					</tr>
					<tr>
						<c:if test="${command.donor.donorType.id != '6'}">
							<td class='appFieldLabel' nowrap><label
								for='primaryPhoneInput'>Letter Salutation:<span
									class="invisibleRequiredFor508">*</span>
							</label></td>
							<td style="text-align: left"><span class='requdIndicator'>*</span></td>
							<td><app:input path="donationSummary.salutation"
									id="salutation" size="20" maxlength="50" class="donationInput" />
								<app:errors path="donationSummary.salutation"
									cssClass="msg-error" /></td>
						</c:if>

						<td class='appFieldLabel' nowrap>Designation:</td>
						<c:if test="${command.donor.donorType.id == '6'}">
							<td></td>
						</c:if>
						<td><app:input path="donationSummary.designation"
								id="designationId" size="20" maxlength="50"
								class="donationInput" /> <app:errors
								path="donationSummary.designation" cssClass="msg-error" /></td>
					</tr>
					<tr>
						<td><br /></td>
					</tr>
					<c:if test="${empty command.donor.organization}">
						<tr>
							<td class='appFieldLabel' nowrap><span
								style="padding-left: 30px">Affiliation:</span></td>
							<td></td>
							<td id="affiliationName" class="donationInput"><c:out
									value='${command.donationSummary.organization.displayName}'
									default='Not Applicable' /> <c:if
									test="${command.donationSummary.organization.inactive}">
									<span class="redText" style="font-weight: bold">(INACTIVE)</span>
								</c:if></td>
							<c:if test="${not FORM_READ_ONLY}">
								<td rowspan="2"><span
									style="padding-left: 10px; padding-right: 30px"><a
										class="buttonAnchor donationInput" id="searchOrgButton"
										href="javascript:popupOrganizationSearch('donationSearch')">Add
											Affiliation</a></span></td>
								<td><a class="buttonAnchor donationInput"
									id="deleteOrgButton" href="javascript:removeAffiliation()">Delete
										Affiliation</a></td>

							</c:if>
						</tr>
					</c:if>
				</table>
			</div>

			<div class="clearCenter" style="margin-top: 10px">
				<table>
					<tr>
						<td nowrap>Donation Description (used in acknowledgement)</td>
					</tr>
					<tr>
						<td><app:textarea path="donationSummary.donationDescription"
								id="donationDescription" rows="3" cols="80" maxlength="250"
								class="donationInput" /></td>
					</tr>
					<tr>
						<td nowrap>Additional Information</td>
					</tr>
					<tr>
						<td><app:textarea path="donationSummary.additionalComments"
								id="comments" rows="3" cols="80" maxlength="250"
								class="donationInput" /></td>
					</tr>
				</table>
			</div>
		</fieldset>
	</div>

	<div
		class="clearCenter donationInputFields hiddenFieldsForItemAndActivity"
		style="margin-top: 10px; display: none">
		<fieldset>
			<legend>In Memory Of</legend>
			<table>
				<tr>
					<td class='appFieldLabel'>In Memory Of:</td>
					<td><app:input path="donationSummary.inMemoryOf"
							id="inMemeoryOf" size="40" maxlength="50" class="donationInput" />
						<app:errors path="donationSummary.inMemoryOf" cssClass="msg-error" /></td>
				</tr>
				<tr>
					<td style="font-weight: bold">Family Contact</td>
					<td></td>
				</tr>
				<tr>
					<td class='appFieldLabel'>Name:</td>
					<td><app:input path="donationSummary.familyContact"
							id="familyContact" size="40" maxlength="50" class="donationInput" />
						<app:errors path="donationSummary.familyContact"
							cssClass="msg-error" /></td>
				</tr>
				<tr>
					<td class='appFieldLabel'>Street:</td>
					<td><app:input path="donationSummary.familyContactAddress"
							id="familyContactAddressId" size="40" maxlength="35"
							class="donationInput" /> <app:errors
							path="donationSummary.familyContactAddress" cssClass="msg-error" /></td>
				</tr>
				<tr>
					<td class='appFieldLabel'>City:</td>
					<td><app:input path="donationSummary.familyContactCity"
							id="familyContactCityId" size="20" maxlength="30"
							class="donationInput" /> <app:errors
							path="donationSummary.familyContactCity" cssClass="msg-error" /></td>
					<td width="5"><img alt="" src="${imgHome}/spacer.gif"
						width="5" height="1" /></td>
					<td class='appFieldLabel'><label for='stateSelect'>State:</label></td>
					<td nowrap><app:select id="stateSelect"
							path="donationSummary.familyContactState" itemLabel="name"
							itemValue="id" class="donationInput">
							<form:option value="-1" label="    " />
							<form:options items="${allStates}" itemLabel="name"
								itemValue="id" />
						</app:select> <app:errors path="donationSummary.familyContactState"
							cssClass="msg-error" /></td>
					<td width="5"><img alt="" src="${imgHome}/spacer.gif"
						width="5" height="1" /></td>
					<td class='appFieldLabel'>Zip:</td>
					<td><app:input path="donationSummary.familyContactZip"
							id="familyContactZipId" size="10" maxlength="10"
							class="donationInput" /> <app:errors
							path="donationSummary.familyContactZip" cssClass="msg-error" /></td>
				</tr>
			</table>
		</fieldset>

	</div>

	<div
		class="clearCenter donationInputFields hiddenFieldsForItemAndActivity"
		style="margin-top: 10px; display: none">
		<fieldset>
			<legend>CC Fields</legend>
			<table align="center">
				<tr>

					<td><app:input path="donationSummary.cc1"
							id="fieldServiceReceiptId1" size="16" maxlength="50"
							class="donationInput" /> <app:errors path="donationSummary.cc1"
							cssClass="msg-error" /></td>
					<td><app:input path="donationSummary.cc2"
							id="fieldServiceReceiptId2" size="16" maxlength="50"
							class="donationInput" /> <app:errors path="donationSummary.cc2"
							cssClass="msg-error" /></td>

					<td><app:input path="donationSummary.cc3"
							id="fieldServiceReceiptId3" size="16" maxlength="50"
							class="donationInput" /> <app:errors path="donationSummary.cc3"
							cssClass="msg-error" /></td>

					<td><app:input path="donationSummary.cc4"
							id="fieldServiceReceiptId4" size="16" maxlength="50"
							class="donationInput" /> <app:errors path="donationSummary.cc4"
							cssClass="msg-error" /></td>

					<td><app:input path="donationSummary.cc5"
							id="fieldServiceReceiptId5" size="16" maxlength="50"
							class="donationInput" /> <app:errors path="donationSummary.cc5"
							cssClass="msg-error" /></td>
				</tr>
			</table>
		</fieldset>
	</div>
	<div class="clearCenter">
		<c:if test="${not FORM_READ_ONLY}">
			<c:if test="${not command.donor.donorType.lookupType.legacy}">
				<a href="#" class="buttonAnchor" id="postPrintButton">Post &amp;
					Print</a>
			</c:if>
			<sec:authorize
				access="hasAnyAuthority('${PERMISSION_VOLUNTEER_READ},
					${PERMISSION_VOLUNTEER_CREATE}')">
				<a class="buttonAnchor" id="submitFormButton" href="#">Post</a>
			</sec:authorize>
		</c:if>
		<c:if
			test="${FORM_READ_ONLY and not command.donor.donorType.lookupType.legacy}">
			<c:if test="${command.donationSummary.persistent}">
				<a class="buttonAnchor" id="justPrintButton" tabIndex="0">Print</a>
			</c:if>
		</c:if>
		<a id="cancelOperationBtn" class="buttonAnchor"
			href="${current_breadcrumb}">Cancel</a>
	</div>

	<form:hidden id="orgIdInput" path="organizationId" value="" />
	<form:hidden id="printReceipt" path="printReceipt" value="false" />
	<form:hidden id="printMemo" path="printMemo" value="false" />
	<form:hidden id="printThankYou" path="printThankYou" value="false" />
	<form:hidden id="printFormat" path="printFormat" value="" />
</form:form>

