<%@ include file="shared/inc_header.jsp"%>

<jsp:include page="/WEB-INF/jsp/shared/inc_appUserSearchPopup.jsp">
	<jsp:param name="uniqueSearchPopupId" value="mainUserAdmin" />
	<jsp:param name="resultCallbackMethod"
		value="newAppUserSelectedCallback" />
	<jsp:param name="includeLocalDB" value="false" />
	<jsp:param name="includeLDAP" value="true" />
</jsp:include>

<script type="text/javascript">
	var homePath = '${home}'
	var imgHomePath = '${imgHome}'
	var ajaxHomePath = '${ajaxHome}'
	var csrfParamName = '${_csrf.parameterName}'
	var csrfValue = '${_csrf.token}'

	var nationalAdminId = "${ROLE_TYPE_NATIONAL_ADMIN.id}"
	var centralOfficeFacilityId = "${VA_FACILITY_VALUE_CENTRAL_OFFICE.id}"
	
	var hasUMPermission = false
	var myUserId = <c:out value="${currentUser.id}" />
<sec:authorize access="hasAuthority('${PERMISSION_USER_MANAGER}')">
	hasUMPermission = true
</sec:authorize>
	
	$(function() {
		var csrfData = {}
		csrfData['${_csrf.parameterName}'] = '${_csrf.token}'
		$.ajaxSetup({
		  	cache:false,
		  	data: csrfData
		})
		
		if (!hasUMPermission) {
    		selectUserWithFields(${currentUser.id},
    				"<c:out value="${currentUser.username}" />",
    				"<c:out value="${currentUser.displayName}" />")
		}
	})
</script>

<script type="text/javascript" src="${jsHome}/userAdmin.js"></script>

<link type="text/css" rel="Stylesheet" href="${cssHome}/userAdmin.css" />

<div class="flex-container">
	<div class="fieldSetContainer" id="userAndDetailsDiv">
		<div class="manageAll">
			<fieldset>
				<legend>Select User</legend>
				<div style="text-align: center">
					<table id="userTable" class="display" cellspacing="0" width="100%">
						<thead>
							<tr>
								<th>Name</th>
							</tr>
						</thead>
					</table>
					<br> <a class="buttonAnchor leftIcon newUser"
						href="javascript:popupAppUserSearch('mainUserAdmin')">Add</a> <a
						class="buttonAnchor leftIcon deletePermanent"
						href="javascript:removeUser()">Remove</a>
				</div>
			</fieldset>
			<p />
		</div>
		<fieldset>
			<legend>User Details</legend>
			<div class="clearCenter pleaseSelect">Please select a
				user&#8230;</div>
			<div class="clearCenter userFields">
				<table align="center">
					<tr>
						<td align="right">User ID:</td>
						<td><span id="userID"></span></td>
					</tr>
					<tr>
						<td align="right">Name:</td>
						<td><span id="userName"></span></td>
					</tr>
					<tr>
						<td align="right">Phone:</td>
						<td><span id="userPhone"></span></td>
					</tr>
					<tr>
						<td align="right">Email:</td>
						<td><span id="userEmail"></span></td>
					</tr>
					<tr>
						<td align="right">Enabled:</td>
						<td align="left"><span class="manageAll"><input
								type="checkbox" id="userEnabled" class="userInput" /></span><span
							id="userEnabledText" class="manageSelfOnly"></span></td>
					</tr>
					<tr>
						<td align="right">Locked:</td>
						<td align="left"><span class="manageAll"><input
								type="checkbox" id="userLocked" class="userInput" /></span><span
							id="userLockedText" class="manageSelfOnly"></span></td>
					</tr>
					<tr id="userExpiredRow" style="display:none">
						<td align="right">Expired:</td>
						<td align="left"><span class="manageAll"><input
								type="checkbox" id="userExpired" class="userInput" /></span><span
							id="userExpiredText" class="manageSelfOnly">Yes</span></td>
					</tr>
					<tr>
						<td align="center" colspan="2">&nbsp;
							<p />Time Zone:
							<p /> <select class="userInput" id="timeZoneSelect">
								<c:forEach items="${allTimeZones}" var="entry">
									<option value="${entry.key.id}"><c:out
											value="${entry.value}" />
								</c:forEach>
						</select>
						</td>
					</tr>
				</table>
			</div>
		</fieldset>
	</div>

	<div class="fieldSetContainer" id="globalRolesAndStationsDiv">
		<fieldset>
			<legend>Facilities &amp; Global Roles</legend>
			<div class="clearCenter pleaseSelect">Please select a
				user&#8230;</div>
			<div class="clearCenter userFields">
				<table cellpadding="2" align="center">
					<tr align="center">
						<td class="manageAll">Roles Available:</td>
						<td class="manageAll">&nbsp;</td>
						<td>Roles Assigned:</td>
					</tr>
					<tr align="center" valign="middle">
						<td class="manageAll"><select size="10" id="available_roles"
							multiple></select></td>
						<td class="manageAll"><img src="${imgHome}/spacer.gif"
							height="5" alt="" /> <br /> <a class="buttonAnchor"
							href="javascript:moveItem('roles', 'addAll', true)">all&nbsp;-&gt;</a>
							<p /> <a class="buttonAnchor"
							href="javascript:moveItem('roles', 'add', true)">-&gt;</a>
							<p /> <a class="buttonAnchor"
							href="javascript:moveItem('roles', 'remove', true)">&lt;-</a>
							<p /> <a class="buttonAnchor"
							href="javascript:moveItem('roles', 'removeAll', true)">all&nbsp;&lt;-</a></td>
						<td><select name="globalRoles" size="10" id="roles"
							multiple="true"></select></td>
					</tr>
				</table>
				<p />

				<table cellpadding="2" align="center">
					<tr align="center">
						<td class="manageAll">Facilities Available:</td>
						<td class="manageAll">&nbsp;</td>
						<td aria-label="list of Assigned Facilities">Facilities Assigned:</td>
					</tr>
					<tr align="center" valign="middle">
						<td class="manageAll"><input id="available_stations_filter"
							size="14"
							title="Enter some text here to filter the Available Facility List"
							placeholder="[Filter Text]" />
							<p /> <select name="available_stations" id="available_stations"
							size="10" multiple></select></td>
						<td class="manageAll"><img src="${imgHome}/spacer.gif"
							height="5" alt="" />
							<p /> <a class="buttonAnchor"
							href="javascript:moveStations(true, true, false)">all&nbsp;-&gt;</a>
							<p /> <a class="buttonAnchor"
							href="javascript:moveStations(true)">-&gt;</a>
							<p /> <a class="buttonAnchor"
							href="javascript:moveStations(false)">&lt;-</a>
							<p /> <a class="buttonAnchor"
							href="javascript:moveStations(false, false, true)">all&nbsp;&lt;-</a></td>
						<td><input id="stations_filter" size="14"
							title="Enter some text here to filter the Facility List"
							placeholder="[Filter Text]" />
							<p /> <select name="stations" size="10" id="stations"
							multiple="true"></select></td>
					</tr>
					<tr>
						<td align="center" colspan="2" class="manageAll">Default
							Facility: <span class="defaultFacilityText"></span><input
							type="hidden" id="defaultFacilityId" value="" />
						</td>
						<td align="center"><a class="buttonAnchor"
							id="setDefaultLink" href="javascript:doNothing()">Set Default</a>
					</tr>
					<tr class="manageSelfOnly">
						<td colspan="2" class="manageAll">&nbsp;</td>
						<td align="center">Default Facility:<br> <span
							class="defaultFacilityText"></span></td>
					</tr>
				</table>
			</div>
		</fieldset>
	</div>
	<div class="fieldSetContainer" id="rolePermissionsTableDiv">
		<fieldset>
			<legend>Effective Roles</legend>
			<div class="clearCenter pleaseSelect">Please select a
				user&#8230;</div>
			<div class="clearCenter userFields effectiveRoleDiv">
				<table cellpadding="3" align="center" id="effectiveRoleTable"></table>
				<span class="manageAll">
					<p />
					<div id="customizeDiv" style="text-align: center; display: none">
						<a class="buttonAnchor" id="customize" href="javascript:doNothing()">Customize&#8230;</a>
					</div>
				</span>
			</div>
		</fieldset>
	</div>
</div>

<div id="customizeDialog" style="display: none"
	title="Customize Roles &amp; Facilities">
	<div align="center">Which roles should this user be assigned, and
		at which facilities?</div>
	<table cellpadding="2" align="center">
		<tr align="center">
			<td>Roles Available:</td>
			<td>&nbsp;</td>
			<td>Roles Assigned:</td>
		</tr>
		<tr align="center" valign="middle">
			<td><select size="10" id="available_cust_roles" multiple></select></td>
			<td><img src="${imgHome}/spacer.gif" height="5" alt="" /> <br /> <a
				class="buttonAnchor"
				href="javascript:moveItem('cust_roles', 'addAll')">all&nbsp;-&gt;</a>
				<p /> <a class="buttonAnchor"
				href="javascript:moveItem('cust_roles', 'add')">-&gt;</a>
				<p /> <a class="buttonAnchor"
				href="javascript:moveItem('cust_roles', 'remove')">&lt;-</a>
				<p /> <a class="buttonAnchor"
				href="javascript:moveItem('cust_roles', 'removeAll')">all&nbsp;&lt;-</a></td>
			<td><select name="custRoles" size="10" id="cust_roles"
				multiple="true"></select></td>
		</tr>
	</table>
	<p />

	<table cellpadding="2" align="center">
		<tr align="center">
			<td>Facilities Available:</td>
			<td>&nbsp;</td>
			<td>Facilities Assigned:</td>
		</tr>
		<tr align="center" valign="middle">
			<td><input id="available_cust_stations_filter" size="14"
				title="Enter some text here to filter the Available Facility List"
				placeholder="[Filter Text]" />
				<p /> <select name="available_cust_stations"
				id="available_cust_stations" size="10" multiple></select></td>
			<td><img src="${imgHome}/spacer.gif" height="5" alt="" /> <br /> <a
				class="buttonAnchor"
				href="javascript:moveItem('cust_stations', 'addAll')">all&nbsp;-&gt;</a>
				<p /> <a class="buttonAnchor"
				href="javascript:moveItem('cust_stations', 'add')">-&gt;</a>
				<p /> <a class="buttonAnchor"
				href="javascript:moveItem('cust_stations', 'remove')">&lt;-</a>
				<p /> <a class="buttonAnchor"
				href="javascript:moveItem('cust_stations', 'removeAll')">all&nbsp;&lt;-</a></td>
			<td><input id="cust_stations_filter" size="14"
				title="Enter some text here to filter the Facility List"
				placeholder="[Filter Text]" />
				<p /> <select name="cust_stations" size="10" id="cust_stations"
				multiple="true"></select></td>
		</tr>
	</table>
</div>