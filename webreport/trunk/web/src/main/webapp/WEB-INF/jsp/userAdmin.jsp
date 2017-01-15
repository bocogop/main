<%@ include file="shared/inc_header.jsp"%>

<script type="text/javascript">
	var homePath = '${home}'
	var imgHomePath = '${imgHome}'
	var ajaxHomePath = '${ajaxHome}'
	var csrfParamName = '${_csrf.parameterName}'
	var csrfValue = '${_csrf.token}'

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

<style>
div.umDiv {
	display: inline-block;
	vertical-align: top;
}
</style>

<div class="clearCenter">
	<div class="manageAll umDiv">
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
					href="javascript:popupUserAddOrEdit()">Add</a> <a
					class="buttonAnchor leftIcon deletePermanent"
					href="javascript:removeUser()">Remove</a>
			</div>
		</fieldset>
	</div>
	<div class="umDiv">
		<fieldset>
			<legend>User Details</legend>
			<div class="clearCenter pleaseSelect">Please select a
				user&#8230;</div>
			<div class="clearCenter userFields">
				<table align="center" width="75%">
					<tr valign="top">
						<td align="right">User ID:</td>
						<td><span id="userID"></span></td>
						<td rowspan="5" align="right"><a href="javascript:editUser()"><img src="${imgHome}/edit.png" border="0" /></a></td>
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
						<td align="center" colspan="3">&nbsp;
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
	<div class="umDiv">
		<fieldset>
			<legend>Roles</legend>
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
						<td><select name="roles" size="10" id="roles" multiple="true"></select></td>
					</tr>
				</table>
			</div>
		</fieldset>
	</div>
</div>
<div id="userFieldsWrapper" style="display: none" title="Add/Edit User">
	<div class="clearCenter">
		<table cellpadding="2">
			<tr>
				<td class='appFieldLabel' nowrap>Username:</td>
				<td style="padding: 4px; text-align: center" width="5%"><span
					class='requdIndicator'>*</span></td>
				<td><input type="text" id="userUsername" size="15"
					maxlength="20" /></td>
				<td width="10" rowspan="5">&nbsp;</td>
				<td align="center">Description / Notes:</td>
			</tr>
			<tr valign="top">
				<td class='appFieldLabel' nowrap>First Name:</td>
				<td style="padding: 4px; text-align: center" width="5%"><span
					class='requdIndicator'>*</span></td>
				<td><input type="text" id="userFirstName" size="20"
					maxlength="50" /></td>
				<td rowspan="4"><textarea id="userDescription" rows="4"
						cols="40"></textarea></td>
			</tr>
			<tr>
				<td class='appFieldLabel' nowrap>Last Name:</td>
				<td style="padding: 4px; text-align: center" width="5%"><span
					class='requdIndicator'>*</span></td>
				<td><input type="text" id="userLastName" size="20"
					maxlength="50" /></td>
			</tr>
			<tr>
				<td class='appFieldLabel' nowrap>Phone:</td>
				<td style="padding: 4px; text-align: center" width="5%"></td>
				<td><input type="text" class="phoneextmask" id="userPhone"
					size="20" maxlength="30" /></td>
			</tr>
			<tr>
				<td class='appFieldLabel' nowrap>Email:</td>
				<td style="padding: 4px; text-align: center" width="5%"></td>
				<td><input type="text" id="userEmail" size="20" maxlength="250" /></td>
			</tr>
		</table>
	</div>
</div>