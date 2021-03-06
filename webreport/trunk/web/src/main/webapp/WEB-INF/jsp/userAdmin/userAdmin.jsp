<%@ include file="../shared/inc_header.jsp"%>

<script type="text/javascript">
	var homePath = '${home}'
	var imgHomePath = '${imgHome}'
	var ajaxHomePath = '${ajaxHome}'
	var csrfParamName = '${_csrf.parameterName}'
	var csrfValue = '${_csrf.token}'

	var hasUMPermission = false
	var myUserId = <c:out value="${currentUser.id}" />
	var requestedUserId = <c:out value="${appUser.id}" default="null" />
	
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
		
		loadUserAdmin()
		
		if (!hasUMPermission) {
    		selectUserWithFields(${currentUser.id},
    				"<c:out value="${currentUser.username}" />",
    				"<c:out value="${currentUser.displayName}" />")
		} else if (requestedUserId != null) {
			selectUserWithFields(requestedUserId,
					"<c:out value="${appUser.username}" />",
    				"<c:out value="${appUser.displayName}" />")
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
div.umDiv fieldset {
	min-height:240px;
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
						<td><span id="name"></span></td>
					</tr>
					<tr>
						<td align="right">Phone:</td>
						<td><span id="phone"></span></td>
					</tr>
					<tr>
						<td align="right">Email:</td>
						<td><span id="email"></span></td>
					</tr>
					<tr>
						<td align="right">Enabled:</td>
						<td align="left"><span class="manageAll"><input
								type="checkbox" id="enabled" class="userInput" /></span><span
							id="enabledText" class="manageSelfOnly"></span></td>
					</tr>
					<tr>
						<td align="right">Password:</td>
						<td align="left">[Protected]</td>
					</tr>
					<tr>
						<td align="right">Description:</td>
						<td><span id="description"></span></td>
					</tr>
					<tr style="display:none">
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
					maxlength="20" tabindex="1" /></td>
				<td width="10" rowspan="5">&nbsp;</td>
				<td align="center">Description / Notes:</td>
			</tr>
			<tr valign="top">
				<td class='appFieldLabel' nowrap>First Name:</td>
				<td style="padding: 4px; text-align: center" width="5%"><span
					class='requdIndicator'>*</span></td>
				<td><input type="text" id="userFirstName" size="20"
					maxlength="50" tabindex="2" /></td>
				<td rowspan="4"><textarea id="userDescription" rows="4"
						cols="40" tabindex="8"></textarea></td>
			</tr>
			<tr>
				<td class='appFieldLabel' nowrap>Last Name:</td>
				<td style="padding: 4px; text-align: center" width="5%"><span
					class='requdIndicator'>*</span></td>
				<td><input type="text" id="userLastName" size="20"
					maxlength="50" tabindex="3" /></td>
			</tr>
			<tr>
				<td class='appFieldLabel' nowrap>Phone:</td>
				<td style="padding: 4px; text-align: center" width="5%"></td>
				<td><input type="text" class="phoneextmask" id="userPhone"
					size="20" maxlength="30" tabindex="4" /></td>
			</tr>
			<tr>
				<td class='appFieldLabel' nowrap>Email:</td>
				<td style="padding: 4px; text-align: center" width="5%"></td>
				<td><input type="text" id="userEmail" size="20" maxlength="250" tabindex="5" /></td>
			</tr>
			<tr class="passwordResetDisplay">
				<td class='appFieldLabel' nowrap>Password:</td>
				<td style="padding: 4px; text-align: center" width="5%"></td>
				<td>Protected (<a href="javascript:toggleResetFields(true)">reset</a>)</td>
			</tr>
			<tr class="passwordResetRow" style="display:none">
				<td class='appFieldLabel' nowrap>Password Reset:</td>
				<td style="padding: 4px; text-align: center" width="5%"></td>
				<td><input type="password" id="userPasswordReset" size="20" maxlength="20" tabindex="6" /></td>
			</tr>
			<tr class="passwordResetRow" style="display:none">
				<td class='appFieldLabel' nowrap>Password Reset (confirm):</td>
				<td style="padding: 4px; text-align: center" width="5%"></td>
				<td><input type="password" id="userPasswordResetConfirm" size="20" maxlength="20" tabindex="7" /></td>
			</tr>
		</table>
	</div>
</div>