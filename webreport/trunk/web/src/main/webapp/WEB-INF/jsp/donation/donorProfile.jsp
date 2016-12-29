<%@ include file="../shared/inc_header.jsp"%>

<c:if test="${FORM_READ_ONLY}">
	<%@ include file="../shared/inc_modifyToView.jsp"%>
</c:if>

<jsp:include page="/WEB-INF/jsp/shared/inc_volunteerSearchPopup.jsp">
	<jsp:param name="uniqueVolunteerSearchPopupId" value="linkToDonor" />
	<jsp:param name="resultCallbackMethod"
		value="linkVolunteerSelectedCallback" />
</jsp:include>

<jsp:include page="/WEB-INF/jsp/shared/inc_organizationSearchPopup.jsp">
	<jsp:param name="uniqueOrganizationSearchPopupId" value="linkToDonor" />
	<jsp:param name="resultCallbackMethod"
		value="linkOrganizationSelectedCallback" />
	<jsp:param name="includeInactiveOption" value="false" />
	<jsp:param name="mode" value="donorLink" />
	<jsp:param name="addButtonCallbackMethod"
		value="linkOrganizationAddSelectedCallback" />
</jsp:include>

<jsp:include page="/WEB-INF/jsp/shared/inc_donorSearchPopup.jsp">
	<jsp:param name="uniqueDonorSearchPopupId" value="mergeDonor" />
	<jsp:param name="mode" value="search" />
	<jsp:param name="resultCallbackMethod"
		value="mergeDonorSelectedCallback" />
</jsp:include>

<jsp:include page="/WEB-INF/jsp/shared/inc_donorSearchPopup.jsp">
	<jsp:param name="uniqueDonorSearchPopupId"
		value="donorProfCheckForDups" />
	<jsp:param name="mode" value="duplicateCheck" />
	<jsp:param name="resultCallbackMethod"
		value="menuDonorSelectedCallback" />
	<jsp:param name="disclaimerText"
		value="Please ensure the new donor does not already exist in the matches below:" />
	<jsp:param name="addButtonCallbackMethod" value="finalSubmit" />
</jsp:include>


<c:if test="${command.donor.persistent}">
	<script type="text/javascript">
		$(function() {
		    var theDataTable = $('#donationsList').DataTable({
		    	buttons : [{
					extend : 'excel',
					exportOptions: {
						columns : [0,1,2,3,4,5,6]
					}
				}, {
					extend : 'pdfHtml5',
					orientation : 'landscape',
					exportOptions: {
						columns : [0,1,2,3,4,5,6]
					}
				}, {
					extend : 'print',
					exportOptions: {
						columns : [0,1,2,3,4,5,6]
					}
				} ],
		    	"dom": '<"top"fBi>rt<"bottom"pl><"clear">',
		    	"lengthMenu" : [ [ 10, 50, -1 ],
		    	 				[ 10, 50, "All" ] ],
		    	"order": [[0,"desc"]],
		    	"pageLength": 10,
		    	"pagingType": "full_numbers",
		    	"stateSave": false
			})
		    
		    rebuildTableFilters('donationsList')
		})
	</script>

	<style>
#donationsList {
	border: thin solid;
}

#donationsList th {
	font-weight: bold;
	text-align: center;
	padding: 3px 18px 3px 10px;
	margin: 3px;
	background-color: #B9CFE6;
}

#donationsList td {
	text-align: left;
	padding: 3px 4px;
	margin: 3px 4px;
}

#donationsList #allRowsController {
	padding: 3px 4px;
	margin: 3px 4px;
}
</style>
</c:if>

<%@ include file="inc_printDonationSummaryPopup.jsp" %>

<script type="text/javascript">
	var donorId = ${command.donor.persistent ? command.donor.id : 'undefined'}
	var fromMergeDonor = {
			type: "<c:out value="${command.donor.donorType.donorType}" />",
			name: "<c:out value="${command.donor.displayName}" />",
			phone: "<c:out value="${command.donor.displayPhone}" />",
			email: "<c:out value="${command.donor.displayEmail}" />",
			mutillineAddress: "<c:out value="${command.mutillineAddressWithoutLineFeed}" />",
			lastDonationFacilty: "<c:out value="${lastDonationFacility}"/>",
			lastDonationDate: "<c:out value="${lastDonationDate}" />"
	}
	var commandFirstName = "<c:out value="${command.donor.firstName}" />"
	var commandLastName = "<c:out value="${command.donor.lastName}" />"

	var donorPersistent = ${command.donor.persistent}
	var commandDonorType = ${command.donor.donorType.id}
	var donorTypeIsIndividual = (commandDonorType == '1')
	var donorTypeIsOrg = (commandDonorType == '4')
	var displayIndividual = donorTypeIsIndividual && ${empty command.donor.volunteer.id && empty command.donor.organization.id}
	var displayOrganization = donorTypeIsOrg && ${not empty command.donor.organization.id}
	var displayVolunteer = donorTypeIsIndividual && ${not empty command.donor.volunteer.id}
	var orgSearchNameStr = "<c:out value="${donorSearchParams.orgName}" default="" />"
	
	var printReceipt = <c:out value="${printReceipt}" default="-1" />
	var printMemo = <c:out value="${printMemo}" default="-1" />
	var printThankYou = <c:out value="${printThankYou}" default="-1" />
	var printFormat = "<c:out value="${printFormat}" default="PDF" />"
	
	var workingFacility = "<c:out value="${facilityContextName}"/>"
	
	$(function() {
		initDonorEdit(printReceipt, printMemo, printThankYou, printFormat)
		
		if (!donorPersistent && donorTypeIsOrg) {
			popupOrganizationSearch('linkToDonor', orgSearchNameStr)
		}
	})
</script>
<script type="text/javascript" src="${jsHome}/donorProfile.js"></script>

<style>
.ui-autocomplete-category {
	font-weight: bold;
	padding: .2em .4em;
	margin: .8em 0 .2em;
	line-height: 1.5;
}

<%--div.donorInputFields, div.individualInputFields {
	min-width: 680px;
}

div.volunteerDisplayFields, div.organizationDisplayFields {
	min-width: 430px;
}
</style>

<style>--%>
div.donorInputFields, div.volunteerDisplayFields, div.organizationDisplayFields, div.otherTypesDisplayFields
	{
	min-width: 680px;
	max-width: 850px;
}
</style>


<c:set var="donorIdOrNull" value="${command.donor.id}" />
<c:if test="${empty donorIdOrNull}">
	<c:set var="donorIdOrNull" value="null" />
</c:if>

<form:form method="post" action="${home}/donorSubmit.htm" id="donorForm" 
	onsubmit="return submitForm(${command.donor.persistent}, ${donorIdOrNull});">

	<div class="clearCenter donorInputFields">
		<fieldset>
			<table>
				<tr>
					<td class='appFieldLabel'><label for='statusRadio'>Donor
							Type: <span class="invisibleRequiredFor508">*</span>
					</label></td>
					<c:if test="${not command.donor.persistent}">
						<td style="padding: 4px; text-align: left"><span
							class='requdIndicator'>*</span></td>
						<td style="text-align: left"><label><form:radiobutton
								id="donorTypeRadioId1" path="donor.donorType" value="1" />Individual</label>
							<label><form:radiobutton id="donorTypeRadioId2" path="donor.donorType"
								value="4" />Organization</label> <app:errors path="donor.donorType"
								cssClass="msg-error" /></td>
					</c:if>

					<c:if test="${command.donor.persistent}">
						<td></td>
						<td style="text-align: left"><c:out
								value="${command.donor.donorType.donorType}" /> <form:hidden
								path="desiredIndividualType" id="desiredIndividualType" />
					</c:if>
				</tr>
			</table>
		</fieldset>
	</div>

	<div class="clearCenter individualInputFields">
		<div class="clearCenter centerContent">
			<c:if test="${not FORM_READ_ONLY}">
				<a id="linkVolunteerButton" class="buttonAnchor"
					href="javascript:popupVolunteerSearch('linkToDonor')">Link
					Volunteer</a>
			<sec:authorize
				access="hasAuthority('${PERMISSION_MERGE_DONOR}')">
				<c:if test="${command.donor.persistent}">
				<span style="padding-left: 20px; padding-right: 20px"><a id="mergeDonorButton" class="buttonAnchor"
					href="javascript:popupDonorSearch('mergeDonor')">Merge Donor</a></span>
				<a id="makeAnonymousButton" class="buttonAnchor"
					href="javascript:mergeDonorToAnoymous()">Make
					Anonymous </a>
					</c:if>
				</sec:authorize>
			</c:if>
		</div>
		<p>
		<fieldset>
			<legend> Donor </legend>
			<table>
				<tr>
					<td class='appFieldLabel' nowrap><label for='lastNameInput'>Last
							Name: <span class="invisibleRequiredFor508">*</span>
					</label></td>
					<td style="padding: 4px; text-align: left" nowrap><span
						class='requdIndicator'>*</span></td>
					<td style="text-align: left" nowrap><app:input
						  id="lastNameInput" path="donor.lastName" size="25" maxLength="30" />
						<app:errors path="donor.lastName" cssClass="msg-error" /></td>
					<td class='appFieldLabel' nowrap><label for='suffixInput'>Suffix:</label></td>
					<td></td>
					<td text-align: left" nowrap><app:input id="suffixInput"
							path="donor.suffix" size="6" maxLength="10" /> <app:errors
							path="donor.suffix" cssClass="msg-error" /></td>
				</tr>
				<tr>
					<td class='appFieldLabel'><label for='prefixInput'>Prefix:</label></td>
					<td></td>
					<td text-align: left" nowrap><app:input id="prefixInput"
							path="donor.prefix" size="6" maxLength="10" /> <app:errors
							path="donor.prefix" cssClass="msg-error" /></td>

					<td class='appFieldLabel' nowrap><label for='firstNameInput'>First
							Name: <span class="invisibleRequiredFor508">* </span>
					</label></td>
					<td style="padding: 4px; text-align: left" nowrap><span
						class='requdIndicator'>*</span></td>
					<td text-align: left" nowrap><app:input id="firstNameInput"
							path="donor.firstName" size="20" maxLength="30" /> <app:errors
							path="donor.firstName" cssClass="msg-error" /></td>
					<td class='appFieldLabel' nowrap><label for='middleNameInput'>Middle
							Name/Initial:</label></td>
					<td style="text-align: left" nowrap><app:input
							id="middleNameInput" path="donor.middleName" size="10"
							maxLength="20" /> <app:errors path="donor.middleName"
							cssClass="msg-error" /></td>
				</tr>
			</table>

		</fieldset>
	</div>

	<div class="clearCenter individualInputFields">
		<fieldset>
			<legend> Mailing Address </legend>
			<div class="leftHalf">
				<table>
					<tr>
						<td class='appFieldLabel'><label for='address1Input'>Street
								Address 1:</label></td>
						<td></td>
						<td style="text-align: left"><app:input id="address1Input"
								path="donor.addressLine1" size="35" maxLength="35" /> <app:errors
								path="donor.addressLine1" cssClass="msg-error" /></td>
					</tr>
					<tr>
						<td class='appFieldLabel'><label for='address2Input'>Street
								Address 2:</label></td>
						<td></td>
						<td style="text-align: left"><app:input id="address2Input"
								path="donor.addressLine2" size="35" maxLength="35" /> <app:errors
								path="donor.addressLine2" cssClass="msg-error" /></td>
					</tr>

					<tr>
						<td class='appFieldLabel'><label for='cityInput'>City:</label></td>
						<td></td>
						<td style="text-align: left"><app:input id="cityInput"
								path="donor.city" size="30" maxLength="30" /> <app:errors
								path="donor.city" cssClass="msg-error" /></td>
					</tr>

					<tr>
						<td class='appFieldLabel'>State:</td>
						<td></td>
						<td nowrap><app:select id="stateSelect" path="donor.state"
								itemLabel="name" itemValue="id">
								<form:option value="-1" label="  " />
								<form:options items="${allStates}" itemLabel="name"
									itemValue="id" />
							</app:select> <app:errors path="donor.state" cssClass="msg-error" /></td>
					</tr>

					<tr>
						<td class='appFieldLabel'><label for='zipInput'>Zip
								Code:</label></td>
						<td></td>
						<td style="text-align: left"><app:input id="zipInput"
								path="donor.zip" size="10" maxLength="10" /> <app:errors
								path="donor.zip" cssClass="msg-error" /></td>
					</tr>
				</table>
			</div>
			<div class="rightHalf">
				<table>
					<tr>
						<td class='appFieldLabel'>Email:</td>
						<td></td>
						<td><app:input id="donorEmail" path="donor.email"
								cssClass="emailInput" size="35" maxLength="250" /> <c:if
								test="${command.donor.persistent}">
								<a href="javascript:emailInputContent('donorEmail')"><img
									alt='Click to email volunteer' src="${imgHome}/envelope.jpg"
									height="14" width="18" border="0" align="absmiddle"
									style="padding-left: 4px; padding-right: 4px" /></a></c:if>
								<app:errors path="donor.email" cssClass="msg-error" />
							</td>
					</tr>

					<tr>
						<td class='appFieldLabel'>Phone:</td>
						<td></td>
						<td><app:input id="donorPhone" cssClass="phoneextmask" path="donor.phone" />
							<app:errors path="donor.phone" cssClass="msg-error" /></td>
					</tr>
				</table>
			</div>
		</fieldset>
	</div>


	<%--- volunteer --%>
	<c:if test="${command.donor.volunteer.persistent}">
		<div class="clearCenter volunteerDisplayFields">
			<fieldset>
				<legend> Donor is Volunteer </legend>
				<div class="leftHalf">
					<table>
						<tr>
							<td class='appFieldLabel' nowrap><span
								style="padding-left: 30px">Name:</span></td>
							<td><c:out value="${command.donor.displayName}" /></td>
						</tr>
						<tr>
							<td class='appFieldLabel' nowrap>Email:</td>
							<td><c:out value="${command.donor.volunteer.email}" /> <c:if
									test="${not empty command.donor.volunteer.email}">
									<a href="mailto:${command.donor.volunteer.email}"><img
										alt='Click to email volunteer' src="${imgHome}/envelope.jpg"
										height="14" width="18" border="0" align="absmiddle"
										style="padding-left: 4px; padding-right: 4px" /></a>
								</c:if><br> <app:errors path="donor.email" cssClass="msg-error" /></td>
						</tr>
						<tr>
							<td class='appFieldLabel' nowrap>Phone:</td>
							<td><c:out value="${command.donor.volunteer.phone}" /></td>
						</tr>
					</table>
				</div>
				<div class="rightHalf">
					<table>
						<tr valign="top">
							<td class='appFieldLabel' nowrap>Address:</td>
							<td><span style="padding-right: 30px"><pre>
										<c:out
											value="${command.donor.volunteer.addressMultilineDisplay}" />
									</pre></span></td>
						</tr>
					</table>
				</div>
				<div class="clearCenter">
					<c:if test="${not FORM_READ_ONLY}">
						<td><a id="unlinkVolunteerButton" class="buttonAnchor"
							href="javascript:unlinkVolunteer()">Unlink Volunteer</a></td>
						<td><a id="editVolunteerButton" class="buttonAnchor"
							href="${home}/volunteerEdit.htm?id=${command.donor.volunteer.id}&fromPage=donor">Edit
								Volunteer</a></td>
						<sec:authorize
								access="hasAuthority('${PERMISSION_MERGE_DONOR}')">
						<td><a id="mergeDonorButton" class="buttonAnchor"
							href="javascript:popupDonorSearch('mergeDonor')">Merge Donor</a></td>
						<td><a id="makeAnonymousButton" class="buttonAnchor"
							href="javascript:mergeDonorToAnoymous()">Make Anonymous </a></td>
						</sec:authorize>
					</c:if>
				</div>

			</fieldset>
		</div>
	</c:if>
	<%-- end volunteer --%>

	<%--- organization --%>
	<div class="clearCenter organizationDisplayFields">


		<fieldset>
			<legend> Donor is Organization </legend>
			<div class="clearCenter centerContent">
				<c:if test="${not command.donor.organization.persistent}">
					<a id="linkOrganizationButton" class="buttonAnchor"
						href="javascript:popupOrganizationSearch('linkToDonor')">Link
						Organization</a>
				</c:if>
			</div>

			<c:if test="${command.donor.organization.persistent}">
				<div class="leftHalf">
					<table width="100%">
						<tr>
							<td class='appFieldLabel' nowrap>Facility:</td>
							<td style="text-align: left"><c:out value="${command.orgFacilityDisplay}" /></td>
						</tr>
						<tr>
							<td class='appFieldLabel' nowrap><span
								style="padding-left: 30px">Name:</span></td>
							<td><c:out value="${command.donor.displayName}" />
							<c:if test="${command.donor.organization.inactive}"><span class="redText" style="font-weight: bold">(INACTIVE)</span>
							</c:if></td>
						</tr>
						<tr>
							<td class='appFieldLabel' nowrap><span
								style="padding-left: 30px">Type:</span></td>
							<td><c:out value="${command.donor.organization.scale}" /></td>
						</tr>
						<tr>
							<td class='appFieldLabel' nowrap>Contact Name:</span></td>
							<td><c:out value="${command.donor.organization.contactName}" /></td>
						</tr>
						<tr>
							<td class='appFieldLabel' nowrap>Contact Title:</span></td>
							<td><c:out
									value="${command.donor.organization.contactTitle}" /></td>
						</tr>
						<tr>
							<td class='appFieldLabel' nowrap>Contact Phone:</td>
							<td><c:out value="${command.donor.organization.phone}" /></td>
						</tr>

						<tr>
							<td class='appFieldLabel' nowrap>Contact Email:</td>
							<td><c:out value="${command.donor.organization.email}" /> <c:if
									test="${not empty command.donor.organization.email}">
									<a href="mailto:${command.donor.organization.email}"><img
										alt='Click to email contact' src="${imgHome}/envelope.jpg"
										height="14" width="18" border="0" align="absmiddle"
										style="padding-left: 4px; padding-right: 4px" /></a>
								</c:if></td>
						</tr>
					</table>
				</div>
				<div class="rightHalf">
					<table>
						<tr>
						<tr valign="top">
							<td class='appFieldLabel' nowrap>Address:</td>
							<td>
							<c:if
								test="${not empty command.donor.organization.addressMultilineDisplay}">
								<td><pre><c:out value="${command.donor.organization.addressMultilineDisplay}" /></pre></span></td>
							</c:if>
							<c:if
								test="${empty command.donor.organization.addressMultilineDisplay}">
								<td>Not on File</td>
							</c:if>
							</td>		
						</tr>
						</tr>
					</table>
				</div>
				</c:if>
							
				<div class="clearCenter">
					<table width="100%">

						<tr>
							<c:if test="${not FORM_READ_ONLY}">
								<td><a id="editOrganizationButton" class="buttonAnchor"
									href="${home}/organizationEdit.htm?id=${command.donor.organization.id}&fromPage=donor">Edit
										Organization</a></td>
								<sec:authorize
									access="hasAuthority('${PERMISSION_MERGE_DONOR}')">
									<td><a id="mergeDonorButton" class="buttonAnchor"
										href="javascript:popupDonorSearch('mergeDonor')">Merge Donor</a></td>
									<td><a id="makeAnonymousButton" class="buttonAnchor"
										href="javascript:mergeDonorToAnoymous()">Make Anonymous </a></td>
								</sec:authorize>
							</c:if>
						</tr>
					</table>
				</div>
				

		</fieldset>
	</div>

	<%-- end organization --%>

	<%--- other types --%>
	<div class="clearCenter otherTypesDisplayFields">

		<fieldset>
			<legend> </legend>
				<div class="leftHalf">
					<table width="100%">
					
						<c:if test="${command.donor.donorType.id == 2}">
							<tr>
								<td class='appFieldLabel' nowrap><span
									style="padding-left: 30px">Organization Name:</span></td>
								<td><c:out value="${command.donor.organization.displayName}" /></td>
							</tr>
						</c:if>
						<c:if test="${command.donor.donorType.id == 3 || command.donor.donorType.id == 5}">
							<tr>
								<td class='appFieldLabel' nowrap><span
									style="padding-left: 30px">Other Group Name:</span></td>
								<td><c:out value="${command.donor.otherGroupName}" /></td>
							</tr>
						</c:if>
						<c:if test="${command.donor.donorType.id == 2 || command.donor.donorType.id == 3}">
							<tr>
								<td class='appFieldLabel' nowrap><span
									style="padding-left: 30px">Individual Name:</span></td>
								<td><c:out value="${command.donor.individualName}" /></td>
							</tr>
						</c:if>						
					</table>
				</div>
				<div class="rightHalf">
					<table>
						<tr>
						<tr valign="top">
							<td class='appFieldLabel' nowrap>Address:</td>
							<td>
							<c:if
								test="${not empty command.donor.organization.addressMultilineDisplay}">
								<td><pre><c:out value="${command.donor.organization.addressMultilineDisplay}" /></pre></span></td>
							</c:if>
							<c:if
								test="${empty command.donor.organization.addressMultilineDisplay}">
								<td>Not on File</td>
							</c:if>
							</td>		
						</tr>
						</tr>
					</table>
				<div class="clearCenter">
					<table width="100%">
						<tr>
							<sec:authorize
								access="hasAuthority('${PERMISSION_MERGE_DONOR}')">
								<td><a id="mergeDonorButton" class="buttonAnchor"
									href="javascript:popupDonorSearch('mergeDonor')">Merge Donor</a></td>
								<td><a id="makeAnonymousButton" class="buttonAnchor"
									href="javascript:mergeDonorToAnoymous()">Make Anonymous </a></td>
							</sec:authorize>
						</tr>
					</table>
				</div>
		</fieldset>
	</div>

	<div class="clearCenter centerContent">
		<c:if test="${not FORM_READ_ONLY}">
			<input id="submitButton" type="submit" value="Submit" class="alwaysEnabled" />
		</c:if>
		<a id="cancelFormButton" class="buttonAnchor keepEnabledForInactive"
			href="${current_breadcrumb}">Cancel</a>
	</div>
	</form:form>




	<p>
	<c:if test="${command.donor.persistent}">
		<div class="clearCenter donorInputFields">
			<fieldset>
				<legend>Donations</legend>

				<div align="center">
					<c:if test="${not FORM_READ_ONLY}">
						<a class="buttonAnchor" id="createButton"
							href="${home}/donationCreate.htm?donorId=${command.donor.id}&fromPage=donor">Add
							Donation</a>
					</c:if>
				</div>
				<table id="donationsList" class="stripe" summary="List of Donations">
					<thead>
						<tr>
							<td class="noborder" title="Filter by Donation Date"></td>
							<td class="noborder" id="facilityFilter" title="Filter by Facility"></td>
							<td class="noborder" title="Filter by Donation Type"></td>
							<td class="noborder"></td>
							<td class="noborder" title="Filter by Acknowledgement Date"></td>
							<td class="noborder"></td>
							<c:if test="${command.donor.donorType.id == '1'}">
								<td class="noborder"></td>
							</c:if>
							<td class="noborder"></td>
						</tr>

						<tr>
							<th class="select-filter"
								selectFilterSortFunction="reverseCompareDates">
								Date</th>
							<th class="select-filter">Facility</th>
							<th class="select-filter">Type</th>
							<th>Amount</th>
							<th class="select-filter">ACK Date</th>
							<th>Description</th>
							<c:if test="${command.donor.donorType.id == '1'}">
								<th>Affiliation</th>
							</c:if>
							<th>Action</th>
						</tr>
					</thead>
					<tbody>
						<c:forEach items="${donationMap}" var="entry">
							<c:set var="donation" value="${entry.key}" />
							<c:set var="donationEditable" value="${entry.value}" />

							<tr id="dn${donation.id}">
								<td nowrap><a class="appLink"
									href="${home}/donationEdit.htm?id=${donation.id}&fromPage=donor"> <c:out
											value="${donation.donationDate}" />
								</a></td>
								<td><c:out value="${donation.facility.displayName}" /></td>
								<td><c:out value="${donation.donationType.donationType}" /></td>
								<td style="text-align: right"><c:set var="totalValue"
										value="0" /> <c:forEach items="${donation.donationDetails}"
										var="donationDetail">
										<c:set var="totalValue"
											value="${totalValue + donationDetail.donationValue}" />
									</c:forEach> <fmt:formatNumber type="currency" maxFractionDigits="2"
										value="${totalValue}" /></td>
								<td style="text-align: center" nowrap><c:out
										value="${donation.acknowledgementDate}" /></td>
								<td><c:out value="${donation.donationDescription}" /></td>
								<c:if test="${command.donor.donorType.id == '1'}">
									<td><c:out value="${donation.organization.displayName}" /></td>
								</c:if>
								<td align="center">
									<c:if
										test="${donation.facility.id == facilityContextId and not command.donor.donorType.lookupType.legacy}">
										<a
											href="javascript:showPrintDonationSummaryDialog(${donation.id})"
											title="Print Donation Documents"><img
											src="${imgHome}/printer.gif" align="left" border="0"
											alt="Print Donation Documents" /></a>
									</c:if> 
									<c:if
										test="${not FORM_READ_ONLY and donationEditable and donation.facility.id == facilityContextId}">
										<a
											href="javascript:deleteDonation('${donation.id}', '${command.donor.id}')"
											title="Are you sure you want to delete this donation?"><img
											src="${imgHome}/permanently_delete_18x18.png" border="0"
											hspace="5" align="right" alt="Delete Donation" /></a>
									</c:if></td>
							</tr>
						</c:forEach>
					</tbody>
				</table>
			</fieldset>
		</div>


	</c:if>