<%@ include file="../shared/inc_header.jsp"%>

<c:if test="${FORM_READ_ONLY}">
	<%@ include file="../shared/inc_modifyToView.jsp"%>
</c:if>

<script type="text/javascript">
	var isReadOnly = <c:out value="${FORM_READ_ONLY}" />
	var iso8601EarliestAcceptableDateEntry = "${iso8601EarliestAcceptableDateEntry}"
	var assumePriorYearAfterMMDD = <c:out value="${assumePriorYearAfterMMDD}" />

	<c:if test="${not empty dateRequested}">
	var dateRequested = new Date('<wr:localDate date="${dateRequested}" />')
	</c:if>
	<c:if test="${empty dateRequested}">
	var dateRequested = null
	</c:if>
</script>

<script type="text/javascript" src="${jsHome}/timeEntryShared.js"></script>
<script type="text/javascript" src="${jsHome}/occasionalTimeEntry.js"></script>

<style>
.errorField {
	border: 2px solid red;
}

.customFieldError {
	color: #f13458;
	font-weight: bold;
}
</style>

<c:set var="leftBoxWidth" value="650" />
<c:set var="rightBoxWidth" value="800" />

<div class="clearCenter occasionalTimeEntryContainer">
	<fieldset>
		<legend>Enter Time</legend>
		<table align="center" width="100%">
			<tr>
				<td>Date: <input type="text" id="dateMaster"
					class="dateInput alwaysEnabled" /></td>
				<c:if test="${not FORM_READ_ONLY}">
					<td align="right" class="occasionalTimeEntryWrapper"><a
						title="Post All occasional time entries. Push Alt-1 while in worksheet to highlight this button."
						id="postAllButton" href="#" class="buttonAnchor">Post All <img
							src="${imgHome}/right.gif" border="0" align="absmiddle"></a></td>
				</c:if>
			</tr>
		</table>

		<div id="occasionalTimeEntryParentWrapper" style="display: none">
			<div id="occasionalTimeEntryForbidden"
				style="display: none; margin: 80px 40px;"></div>
			<div class="occasionalTimeEntryWrapper">
				<div id="missingFieldsNotice" class="clearCenter"
					class="redText" style="margin-top: 15px; display: none; font-weight: bold">Please
					enter a value for the fields highlighted:</div>
				<table id="occasionalTimeEntryList" class="stripe"
					summary="List of Occasional Time Entries">
					<thead>
						<tr>
							<th>Number<br>in Group
							</th>
							<th>Total<br>Group Hours
							</th>
							<th>Organization</th>
							<th>Assignment</th>
							<th>Comments</th>
							<th width="20px">Action</th>
						</tr>
					</thead>
					<tbody>
					</tbody>
				</table>
			</div>
			<div class="clearCenter" style="margin-top: 10px;">
				*Push Alt-1 to exit keyboard entry fields
			</div>
		</div>
	</fieldset>
</div>

<div class="clearCenter occasionalTimeEntryContainer">
	<fieldset>
		<legend id="occasionalTimeReportLegend">Occasional Time
			Report</legend>
		<table id="occasionalTimeReportList" class="stripe"
			summary="Report of Occasional Time">
			<thead>
				<tr>
					<td class="noborder" id="occasionalTimeReportDateFilter" width="40"></td>
					<td class="noborder" width="80"></td>
					<td class="noborder" width="80"></td>
					<td class="noborder" id="occasionalTimeReportOrganizationFilter"></td>
					<td class="noborder" id="occasionalTimeReportServiceFilter"></td>
					<td class="noborder" id="occasionalTimeReportRoleFilter"></td>
					<td class="noborder" id="occasionalTimeReportLocationFilter"></td>
					<td class="noborder"></td>
					<td class="noborder" width="40"></td>
				</tr>
				<tr>
					<th class="select-filter" width="40">Date</th>
					<th width="80">Number in Group</th>
					<th width="80">Total Group Hours</th>
					<th class="select-filter">Organization</th>
					<th class="select-filter">Service</th>
					<th class="select-filter">Role</th>
					<th class="select-filter">Location</th>
					<th>Comments</th>
					<th width="40">Action</th>
				</tr>
			</thead>
			<tbody>
			</tbody>
		</table>
	</fieldset>
</div>
</div>

<div id="editOccasionalWorkEntryDialog" style="display: none"
	title="Edit Occasional Time Entry">
	<div class="clearCenter">
		<table>
			<tr>
				<td align="right">Date:</td>
				<td width="10"><span class="requdIndicator" id="nameRequired">*</span></td>
				<td><input type="text" id="editOccasionalWorkEntryDate"
					class="dateInput" size="16" /></td>
			</tr>
			<tr>
				<td align="right">Number in Group:</td>
				<td width="10"><span class="requdIndicator" id="nameRequired">*</span></td>
				<td><input type="text"
					id="editOccasionalWorkEntryNumberInGroup" size="8" maxlength="5" /></td>
			</tr>
			<tr>
				<td align="right">Total Group Hours:</td>
				<td width="10"><span class="requdIndicator" id="nameRequired">*</span></td>
				<td><input type="text" id="editOccasionalWorkEntryHours"
					size="8" maxlength="7" /></td>
			</tr>
			<tr>
				<td align="right">Organization:</td>
				<td width="10"><span class="requdIndicator" id="nameRequired">*</span></td>
				<td><input type="text" id="editOccasionalWorkEntryOrganization"
					size="50" /><input type="hidden"
					id="editOccasionalWorkEntryOrganizationId" /></td>
			</tr>
			<tr>
				<td align="right">Assignment:</td>
				<td width="10"><span class="requdIndicator" id="nameRequired">*</span></td>
				<td><input type="text"
					id="editOccasionalWorkEntryBenefitingServiceRole" size="50" /><input
					type="hidden" id="editOccasionalWorkEntryBenefitingServiceRoleId" /></td>
			</tr>
			<tr>
				<td align="right">Comments:</td>
				<td width="10"></td>
				<td><input type="text" id="editOccasionalWorkEntryComments"
					size="50" maxlength="40" /></td>
			</tr>
		</table>
	</div>
</div>