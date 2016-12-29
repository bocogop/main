<%@ include file="shared/inc_header.jsp"%>

<script type="text/javascript">
	var isNationalAdmin = <c:out value="${currentUser.nationalAdmin}" />
</script>

<script type="text/javascript">
	function volunteerRequirementUpdatedCallback() {
		refreshNotifications()
	}
	function getVolunteerRequirementData() {
		return volunteerRequirementMap
	}
	function retrieveVolunteerRequirementsByScope(volunteerRequirementId, callbackFn) {
		$.ajax({
			url : ajaxHomePath + '/volunteerRequirements',
			method : 'POST',
			dataType : 'json',
			data : {
				volunteerRequirementId : volunteerRequirementId
			},
			error : commonAjaxErrorHandler,
			success : function(response) {
				var volunteerAssignmentData = {}
				$(response.allVolunteerAssignments).each(function(index, item) {
					volunteerAssignmentData[item.id] = item
				})
				callbackFn(response.requirementsByScope, volunteerAssignmentData)
			}
		})
	}
</script>
<%@ include file="volunteer/volunteerRequirementPopup.jsp"%>

<script type="text/javascript" src="${jsHome}/homeJavascript.js"></script>

<style>
ul.notificationRefList {
	margin-top: 2px;
	margin-bottom: 2px;
}
</style>

<c:if test="${empty facilityContextId}">
	<script type="text/javascript">
		alert('Your working facility configuration is invalid; please contact the national coordinator. You will be logged out until this is corrected.')
		document.location.href = '${home}/logout.htm'
	</script>
</c:if>

<c:set var="notificationWidth" value="900" />
<c:if test="${currentUser.nationalAdmin}">
	<c:set var="notificationWidth" value="1000" />
</c:if>

<div class="clearCenter">
	<div class="rightHalf">
		<div class="roundedRect"
			style="vertical-align: top; min-width: ${notificationWidth}px; max-width:${notificationWidth}px; min-height: 300px">
			<h3 style="text-align: center">
				<u>Notifications:</u>
			</h3>
			<p />
			<div id="notificationsMaxReached" align="center" class="redText"
				style="display: none">Only the first ${notificationMaxResults}
				notifications are shown.</div>
			<div id="notificationListWrapper">
				<table id="notificationList" class="stripe"
					summary="List of Notifications">
					<thead>
						<tr>
							<td title="Filter by Severity"></td>
							<td></td>
							<td></td>
							<td title="Filter by Originating Facility"></td>
							<td></td>
							<td></td>
						</tr>
						<tr>
							<th width="10%" class="select-filter">Severity</th>
							<th width="30%" class="select-filter">Name</th>
							<th width="40%">Details</th>
							<th width="10%" class="select-filter">Originating Facility</th>
							<th width="10%">Created<br>Date</th>
							<th width="10%">Removal<br>Date</th>
							<th width="10%">Actions</th>
						</tr>
					</thead>
					<tbody>
					</tbody>
				</table>
			</div>
			<div id="noNotificationDiv" align="center"
				style="display: none; margin-top: 100px">You have no
				notifications.</div>
		</div>
	</div>
</div>

${homepageContent}

<div id="volunteerDataChangeCompareDiv" style="display: none" title="Compare Volunteer Data Fields">
	<table id="volunteerDataChangeTable" align="center" border="1" cellpadding="4">
		<thead>
			<tr>
				<th>Field:</th>
				<th>Old Value:</th>
				<th>New Value:</th>
			</tr>
		</thead>
		<tbody>
		</tbody>
	</table>
</div>
