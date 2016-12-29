<%@ include file="../shared/inc_header.jsp"%>

<script type="text/javascript" src="${jsHome}/facilityEdit.js"></script>
<script type="text/javascript">
    var commandNumMeals = <c:out value="${command.facility.stationParameters.numberOfMeals}" default="1"/>
	var canEditAllFacilities = false
	<sec:authorize access="hasAnyAuthority('${PERMISSION_FACILITY_EDIT_ALL}')">
		canEditAllFacilities = true
	</sec:authorize>
	var statusCheckCutoff = ${kioskStatusCheckCutoff}
	
	$(function() {
		onPageLoad(${not empty command.facility},
				${not empty command.facility and not command.facility.persistent},
				<c:out value="${command.facility.id}" default="-1" />,
				${not empty command.facility and not empty command.facility.vaFacility},
				canEditAllFacilities)
	})
</script>

<style>
div#facilityHierarchyDiv {
	min-width: 400px;
	max-width: 400px;
}

div#genPostFundDiv, div#donReferenceDiv {
	min-width: 375px;
	max-width: 375px;
}

<sec:authorize access="hasAnyAuthority('${PERMISSION_FACILITY_EDIT_ALL}')">
div#facilityInfoDiv {
	max-width: 800px;
}
</sec:authorize>

#sdsFacilityTableWrapper .dataTables_wrapper .dataTables_filter
	{
	float: none;
	text-align: center;
}

#sdsFacilityTableWrapper .dataTables_wrapper .dataTables_filter input {
	width: 250px;
}

.vaFacilityControlledInput, .vaFacilityControlledValue {
	display: none;
}

#locationName {
	font-weight: bold;
}

.inactiveFacility {
	color: #f13458;
}

.inactiveFacility a.facilityLink {
	color: #f13458;
}

a.facilityLink {
	color: black;
}

a.activeFacilityLink {
	background-color: #E8D2E7;
}

div#facilityHierarchyDiv input[type='search'] {
	width: 200px;
}

div.dataTables_filter input {
	width: 100px;
}

#kioskList div.DataTables_sort_wrapper {
	white-space: nowrap
}
</style>

<div class="clearCenter">
	<sec:authorize
		access="hasAnyAuthority('${PERMISSION_FACILITY_EDIT_ALL}')">
		<div id="facilityHierarchyDiv" class="leftHalf" style="visible: none">
			<fieldset>
				<legend>Facility Hierarchy</legend>
				<div align="center" style="margin-bottom: 10px">
					<input type="checkbox" id="showInactiveFacilities">Show
					Inactive
					<c:if test="${not FORM_READ_ONLY}">
						<a style="margin-left: 15px" class="buttonAnchor"
							id="createButton" href="facilityCreate.htm">Add Facility</a>
					</c:if>
				</div>
				<table id="facilityList" class="stripe" summary="List of Facilities">
					<thead>
						<tr>
							<th>Name</th>
						</tr>
					</thead>
					<tbody>
					</tbody>
				</table>
			</fieldset>
		</div>
	</sec:authorize>
	<div id="facilityInfoDiv" class="rightHalf">
		<c:if test="${not empty command.facility}">
			<form:form method="post" action="${home}/facilitySubmit.htm"
				id="facilityForm"
				onsubmit="return submitForm(${command.facility.persistent});">
				<fieldset>
					<legend>Facility Details</legend>
					<div class="clearCenter" style="margin-bottom: 15px">
						<table>
							<tr>
								<td class='appFieldLabel' nowrap>Facility Name <c:if
										test="${command.facility.linkedToVAFacility}">(Override)</c:if>:<span
									class="invisibleRequiredFor508">*</span></td>
								<td style="padding: 4px; text-align: center" width="5%"><span
									class='requdIndicator'>*</span></td>
								<td><app:input id="facilityName" path="facility.name"
										size="45" cssStyle="font-weight:bold;" /></td>
							</tr>
							<tr>
								<td colspan="2"></td>
								<td><app:errors path="facility.name" cssClass="msg-error" /></td>
							</tr>
							<c:if test="${command.facility.persistent}">
								<tr valign="top">
									<td class='appFieldLabel' nowrap>Official VA Facility:</td>
									<td></td>
									<td><c:if test="${empty command.facility.vaFacility}">(none) 
										<sec:authorize
												access="hasAnyAuthority('${PERMISSION_FACILITY_EDIT_ALL}')">
												<a href="javascript:popupSDSFacilitySelection()">[Link]</a>
											</sec:authorize>
										</c:if> <c:if test="${not empty command.facility.vaFacility}">
											<c:out value="${command.facility.vaFacility.displayName}" />
											<br>
											Type: <i><c:out
													value="${command.facility.vaFacility.facilityType.name}" /></i>
											<sec:authorize
												access="hasAnyAuthority('${PERMISSION_FACILITY_EDIT_ALL}')">
												<br>
												<a href="javascript:unlinkSDSFacility()">[Unlink]</a>
											</sec:authorize>
										</c:if> </a></td>
								</tr>
							</c:if>
							<tr>
								<td class='appFieldLabel'>Parent Facility:</td>
								<td></td>
								<td><app:select id="facilityParentSelect" path="parentId">
										<form:option value="" label="(none)" />
										<form:options items="${allFacilities}"
											itemLabel="displayNameAbbreviated" itemValue="id" />
									</app:select></td>
							</tr>
							<tr>
								<td colspan="2"></td>
								<td><app:errors path="parentId" cssClass="msg-error" /></td>
							</tr>
							<tr>
								<td class='appFieldLabel'>VISN:</td>
								<td></td>
								<td><app:select id="facilityVISN"
										cssClass="vaFacilityControlledInput"
										path="facility.administrativeUnit">
										<form:option value="" label="(none)" />
										<form:options items="${allVISNs}" itemLabel="displayName"
											itemValue="id" />
									</app:select> <span class="vaFacilityControlledValue"><c:out
											value="${command.facility.vaFacility.visn.displayName}"
											default="(none set)" /></span></td>
							</tr>
							<tr>
								<td colspan="2"></td>
								<td><app:errors path="facility.administrativeUnit"
										cssClass="msg-error" /></td>
							</tr>
						</table>
					</div>
					<div style="float: left">
						<table>
							<tr>
								<td class='appFieldLabel' nowrap>Address Line 1:<span
									class="invisibleRequiredFor508">*</span></td>
								<td style="padding: 4px; text-align: center" width="5%"><span
									class='requdIndicator'>*</span></td>
								<td><app:input
										cssClass="vaFacilityControlledInput"
										path="facility.addressLine1" /><span
									class="vaFacilityControlledValue"><c:out
											value="${command.facility.vaFacility.addressLine1}" /></span></td>
							</tr>
							<tr>
								<td colspan="2"></td>
								<td><app:errors path="facility.addressLine1"
										cssClass="msg-error" /></td>
							</tr>
							<tr>
								<td class='appFieldLabel' nowrap>Address Line 2:</td>
								<td></td>
								<td><app:input
										cssClass="vaFacilityControlledInput"
										path="facility.addressLine2" /><span
									class="vaFacilityControlledValue"><c:out
											value="${command.facility.vaFacility.addressLine2}" /></span></td>
							</tr>
							<tr>
								<td colspan="2"></td>
								<td><app:errors path="facility.addressLine2"
										cssClass="msg-error" /></td>
							</tr>
							<tr>
								<td class='appFieldLabel'>City:<span
									class="invisibleRequiredFor508">*</span></td>
								<td style="padding: 4px; text-align: center" width="5%"><span
									class='requdIndicator'>*</span></td>
								<td><app:input
										cssClass="vaFacilityControlledInput" path="facility.city" /><span
									class="vaFacilityControlledValue"><c:out
											value="${command.facility.vaFacility.city}" /></span></td>
							</tr>
							<tr>
								<td colspan="2"></td>
								<td><app:errors path="facility.city"
										cssClass="msg-error" /></td>
							</tr>
							<tr>
								<td class='appFieldLabel' nowrap>State:<span
									class="invisibleRequiredFor508">*</span></td>
								<td style="padding: 4px; text-align: center" width="5%"><span
									class='requdIndicator'>*</span></td>
								<td nowrap><app:select cssClass="vaFacilityControlledInput"
										id="stateSelect" path="facility.state">
										<form:option value="">-- Select --</form:option>
										<form:options items="${allStates}" itemLabel="name"
											itemValue="id" />
									</app:select><span class="vaFacilityControlledValue"><c:out
											value="${command.facility.vaFacility.state.name}" /></span></td>
							</tr>
							<tr>
								<td colspan="2"></td>
								<td><app:errors path="facility.state" cssClass="msg-error" /></td>
							</tr>
							<tr>
								<td class='appFieldLabel' nowrap><span
									style="margin-left: 20px">Zip:</span><span
									class="invisibleRequiredFor508">*</span></td>
								<td style="padding: 4px; text-align: center" width="5%"><span
									class='requdIndicator'>*</span></td>
								<td><app:input cssClass="vaFacilityControlledInput"
										path="facility.zip" size="10" /><span
									class="vaFacilityControlledValue"><c:out
											value="${command.facility.vaFacility.zip}" /></span></td>
							</tr>
							<tr>
								<td colspan="2"></td>
								<td><app:errors path="facility.zip" cssClass="msg-error" /></td>
							</tr>
						</table>
					</div>
					<div style="float: left;">
						<table>
							<tr>
								<td class='appFieldLabel' nowrap>Station Number:<span
									class="invisibleRequiredFor508">*</span></td>
								<td style="padding: 4px; text-align: center" width="5%"><span
									class='requdIndicator'>*</span></td>
								<td><app:input cssClass="vaFacilityControlledInput"
										path="facility.stationNumber" size="10" /><span
									class="vaFacilityControlledValue"><c:out
											value="${command.facility.vaFacility.stationNumber}" /></span></td>
							</tr>
							<tr>
								<td colspan="2"></td>
								<td><app:errors path="facility.stationNumber"
										cssClass="msg-error" /></td>
							</tr>
							<c:if test="${command.facility.persistent && command.facility.type.id != 2}">
								<tr>
									<td class='appFieldLabel'>Facility Type:</td>
									<td style="padding: 4px; text-align: center" width="5%"></td>
									<td><c:out value="${command.facility.type.description}" /></td>
								</tr>
							</c:if>
							<tr>
								<td colspan="2" align="right"><form:checkbox
										path="facility.active" id="facilityActive" /></td>
								<td>Is Active</td>
							</tr>
							<tr>
								<td colspan="2"></td>
								<td><app:errors path="facility.active" cssClass="msg-error" /></td>
							</tr>
							<tr>
								<td class='appFieldLabel' nowrap>Time Zone:<span
									class="invisibleRequiredFor508">*</span></td>
								<td style="padding: 4px; text-align: center" width="5%"><span
									class='requdIndicator'>*</span></td>
								<td><form:select
										path="facility.timeZone" id="facilityTimeZone">
										<option value="">Please select...</option>
										<c:forEach items="${allTimeZones}" var="entry" varStatus="loop">
											<form:option value="${entry.key.id}"><c:out value="${entry.value} - ${entry.key.id}" /></form:option>
											<c:if test="${loop.count == fn:length(prioritizedTimeZoneIds)}">
											<option value="">------------------------------------------------------</option>
											</c:if>
										</c:forEach>
										</form:select></td>
							</tr>
							<tr>
								<td colspan="2"></td>
								<td><app:errors path="facility.timeZone" cssClass="msg-error" /></td>
							</tr>
						</table>
					</div>
				</fieldset>

				<fieldset class="persistentFacilityOnlyFields" style="display: none">
					<legend>Physical Locations</legend>
					<div class="clearCenter" style="margin-bottom: 15px">
						<table id="locationList" class="stripe"
							summary="List of Physical Locations">
							<thead>
								<tr>
									<td width="30%" class="noborder">Filters:</td>
									<td width="20%" class="noborder"></td>
									<td width="20%" class="noborder"></td>
									<td width="10%" class="noborder"></td>
									<td width="10%" class="noborder" title="Filter by Status"></td>
									<c:if test="${not FORM_READ_ONLY}">
										<td width="10%" class="noborder"></td>
									</c:if>
								</tr>
								<tr>
									<th width="30%">Location Name</th>
									<th width="25%">Address</th>
									<th width="25%">Contact Info</th>
									<th width="10%">Volunteers</th>
									<th width="10%" class="select-filter">Status</th>
									<c:if test="${not FORM_READ_ONLY}">
										<th width="10%">Action</th>
									</c:if>
								</tr>
							</thead>
							<tbody>
							</tbody>
						</table>
						<div align="center" style="margin-top: 10px">
							<c:if test="${not FORM_READ_ONLY}">
								<a class="buttonAnchor" id="createLocationButton"
									href="javascript:popupLocationEdit()">Add Location</a>
							</c:if>
						</div>
					</div>
				</fieldset>
				<fieldset class="persistentFacilityOnlyFields" style="display: none">
					<legend>Kiosks</legend>
					
					<div style="margin-top: 5px">
						<table>
							<tr>
								<td style="padding-left: 10px; text-align: left">Default Language (English) - Welcome Text:</td>
							</tr>
							<tr>
								<td style="padding-left: 10px; text-align: left"><app:textarea
										path="facility.stationParameters.introductoryText"
										id="introductory" rows="3" cols="100" maxlength="240" /></td>
							</tr>
						</table>
					</div>
					
					<div style="margin-top: 5px;" class="alternateLangWelcomeText">
						<table>
							<tr>
								<td style="padding-left: 10px; text-align: left">Alternate Language (Spanish) - Welcome Text:</td>
							</tr>
							<tr>
								<td style="padding-left: 10px; text-align: left"><app:textarea
										path="facility.stationParameters.alternateLanguageIntroText"
										id="introductory" rows="3" cols="100" maxlength="240" /></td>
							</tr>
						</table>
					</div>
										
					
					<div class="clearCenter" style="margin-bottom: 15px;margin-top: 10px">
						<table id="kioskList" class="stripe"
							summary="List of Kiosks">
							<thead>
								<tr>
									<th width="35%" class="kioskHeader">Kiosk Location</th>
									<th width="5%" class="kioskHeader">Registered</th>
									<th width="35%" class="kioskHeader">Printer Status</th>
									<th width="20%" class="kioskHeader">Queued Print Jobs</th>
									<c:if test="${not FORM_READ_ONLY}">
										<th width="5%">Action</th>
									</c:if>
								</tr>
							</thead>
							<tbody>
							</tbody>
						</table>
						<div align="center" style="margin-top: 10px">
							<c:if test="${not FORM_READ_ONLY}">
								<a class="buttonAnchor" id="createKioskButton"
									href="javascript:popupKioskEdit()">Add Kiosk</a>
							</c:if>
						</div>
					</div>
					</fieldset>
				<fieldset class="mealTicketSettingFields">
					<legend>Meal Ticket Settings</legend>
					<%@ include file="inc_mealTicketSettings.jsp"%>
				</fieldset>
				<fieldset class="donationSettingFields">
					<legend>Donation Settings</legend>
					<%@ include file="inc_donationSettings.jsp"%>
				</fieldset>
			
				<div class="clearCenter">
					<input type="submit" value="Submit" id="submitButton" /> <a
						class="buttonAnchor" href="${current_breadcrumb}">Cancel</a>
				</div>
			</form:form>
		</c:if>
	</div>
</div>

<div id="sdsFacilityTableWrapper" style="display: none"
	title="Please search and select a VA Facility:">
	<table id="sdsFacilityTable" class="display" cellspacing="0"
		width="100%">
		<thead>
			<tr>
				<th>Name</th>
			</tr>
		</thead>
	</table>
</div>

<div id="locationFieldsWrapper" style="display: none"
	title="Location Details">
	<div class="clearCenter">
		<table>
			<tr>
				<td class='appFieldLabel' nowrap>Name:<span
					class="invisibleRequiredFor508">*</span></td>
				<td style="padding: 4px; text-align: center" width="5%"><span
					class='requdIndicator'>*</span></td>
				<td><input type="text" id="locationName" size="30" maxlength="80" /></td>
				<td><img alt="" src="${imgHome}/spacer.gif" height="1"
					width="25" /></td>
			</tr>
		</table>
	</div>
	<div class="leftHalf">
		<table>
			<tr>
				<td class='appFieldLabel' nowrap>Address Line 1:<span
					class="invisibleRequiredFor508">*</span></td>
				<td style="padding: 4px; text-align: center" width="5%"><span
					class='requdIndicator'>*</span></td>
				<td colspan="4"><input type="text" id="locationAddressLine1" maxlength="35" /></td>
			</tr>
			<tr>
				<td class='appFieldLabel' nowrap>Address Line 2:</td>
				<td></td>
				<td colspan="4"><input type="text" id="locationAddressLine2" maxlength="35" /></td>
			</tr>
			<tr>
				<td class='appFieldLabel'>City:</td>
				<td style="padding: 4px; text-align: center" width="5%"><span
					class='requdIndicator'>*</span></td>
				<td colspan="4"><input type="text" id="locationAddressCity" maxlength="30" /></td>
			</tr>
			<tr>
				<td class='appFieldLabel'>State:</td>
				<td style="padding: 4px; text-align: center" width="5%"><span
					class='requdIndicator'>*</span></td>
				<td nowrap><select id="locationAddressState">
						<option value="">-- Select --</option>
						<c:forEach items="${allStates}" var="state">
							<option value="${state.id}"><c:out
									value="${state.name}" /></option>
						</c:forEach>
				</select></td>
			</tr>
			<tr>
				<td class='appFieldLabel'><span style="margin-left: 20px">Zip:</span><span
					class="invisibleRequiredFor508">*</span></td>
				<td style="padding: 4px; text-align: center" width="5%"><span
					class='requdIndicator'>*</span></td>
				<td><input type="text" id="locationAddressZip" size="10"
					maxlength="10" /></td>
			</tr>
		</table>
	</div>
	<div class="rightHalf">
		<table>
			<tr>
				<td class='appFieldLabel' nowrap>Contact Name:</td>
				<td><input type="text" id="locationContactName" maxlength="50" /></td>
			</tr>
			<tr>
				<td class='appFieldLabel' nowrap>Contact Role:</td>
				<td><input type="text" id="locationContactRole" maxlength="50" /></td>
			</tr>
			<tr>
				<td class='appFieldLabel' nowrap>Contact Phone:</td>
				<td><input type="text" id="locationContactPhone"
					class="phoneextmask" maxlength="15" /></td>
			</tr>
			<tr>
				<td class='appFieldLabel' nowrap>Contact Email:</td>
				<td><input type="text" id="locationContactEmail" maxlength="250" /></td>
			</tr>
		</table>
	</div>
</div>

<div id="kioskFieldsWrapper" style="display: none"
	title="Kiosk Details">
	<div class="clearCenter">
		<table>
			<tr>
				<td class='appFieldLabel' nowrap>Location:<span
					class="invisibleRequiredFor508">*</span></td>
				<td style="padding: 4px; text-align: center" width="5%"><span
					class='requdIndicator'>*</span></td>
				<td><input type="text" id="kioskLocation" size="30" maxlength="50" /></td>
			</tr>
			<tr>
				<td align="right" colspan="2"><input type="checkbox" id="kioskRegistered" /></td>
				<td>Registered<span
					class="invisibleRequiredFor508">*</span></td>
			</tr>
		</table>
	</div>
</div>