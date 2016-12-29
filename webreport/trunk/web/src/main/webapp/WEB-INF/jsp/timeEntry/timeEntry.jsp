<%@ include file="../shared/inc_header.jsp"%>

<c:if test="${FORM_READ_ONLY}">
	<%@ include file="../shared/inc_modifyToView.jsp"%>
</c:if>

<script type="text/javascript">
	var isReadOnly = <c:out value="${FORM_READ_ONLY}" />
	var iso8601EarliestAcceptableDateEntry = "${iso8601EarliestAcceptableDateEntry}"
	var assumePriorYearAfterMMDD = <c:out value="${assumePriorYearAfterMMDD}" />
	var volIdRequested = <c:out value="${volIdRequested}" default="null" />
</script>

<script type="text/javascript" src="${jsHome}/timeEntryShared.js"></script>
<script type="text/javascript" src="${jsHome}/timeEntry.js"></script>

<style>
.assignmentInput {
	max-width: 180px;
}

.organizationInput {
	max-width: 180px;
}

.errorField {
	border: 2px solid red;
}

.customFieldError {
	color: #f13458;
	font-weight: bold;
}

#timeReportOrganizationFilter select {
	max-width: 180px;
}
</style>

<c:set var="leftBoxWidth" value="700" />
<c:set var="rightBoxWidth" value="750" />

<div class="clearCenter timeEntryContainer">
	<div
		style="margin-right: 15px; max-width: ${leftBoxWidth}px; display: inline-block; vertical-align: top">
		<fieldset>
			<legend>Enter Time</legend>
			<table align="center">
				<tr>
					<td><input type="radio" name="timeEntryType" value="date"
						id="byDate" selected="selected" class="alwaysEnabled" /> By Date:</td>
					<td rowspan="2" width="15">&nbsp;</td>
					<td><input type="radio" name="timeEntryType" value="volunteer"
						id="byVolunteer" class="alwaysEnabled" /> By Volunteer:</td>
				</tr>
				<tr>
					<td><input type="text" id="dateMaster" class="dateInput alwaysEnabled" /></td>
					<td><input disabled="disabled" type="text" class="alwaysEnabled"
						id="volunteerMaster" /><input type="hidden"
						id="volunteerMasterId" /></td>
				</tr>
			</table>
			<div id="timeEntryForbidden"
				style="display: none; margin: 80px 40px;">Time entries can only be
				added for the current fiscal year and cannot be future dated.</div>
			<div id="timeEntryWrapper" style="display:none">
				<div id="missingFieldsNotice" class="clearCenter"
					style="margin-top: 15px; display: none; color: red; font-weight: bold">Please
					enter a value for the fields highlighted:</div>
				<table id="timeEntryList" class="stripe" style="margin-top: 30px"
					width="${leftBoxWidth-10}" style="max-width: ${leftBoxWidth-10}px"
					summary="List of Time Entries">
					<thead>
						<tr>
							<th width="25%">Volunteer</th>
							<th width="10%">Date</th>
							<th width="10%">Hours</th>
							<th width="25%">Assignment</th>
							<th width="25%">Organization</th>
							<th width="5%">Action</th>
						</tr>
					</thead>
					<tbody>
					</tbody>
				</table>
				<c:if test="${not FORM_READ_ONLY}">
				<div class="clearCenter" style="margin-top: 10px;"><a
					title="Post All time entries. Push Alt-1 while in worksheet to highlight this button."
					id="postAllButton" href="#" class="buttonAnchor"><nobr>Post All</nobr> <img
						src="${imgHome}/right.gif" border="0" align="absmiddle"></a>
				</div>
				</c:if>
				<div class="clearCenter" style="margin-top: 10px;">
					*Push Alt-1 to exit keyboard entry fields
				</div>
			</div>
		</fieldset>
	</div>

	<div
		style="margin-left: 15px; max-width: ${rightBoxWidth}px; display: inline-block; vertical-align: top">
		<fieldset>
			<legend id="timeReportLegend">Time Report</legend>
			<table id="timeReportList" width="${rightBoxWidth-10}"
				style="max-width: ${rightBoxWidth-10}px" class="stripe"
				summary="Report of Time">
				<thead>
					<tr>
						<td class="noborder">Filters:</td>
						<td class="noborder"></td>
						<td class="noborder"></td>
						<td class="noborder"></td>
						<td class="noborder"></td>
						<td class="noborder" id="timeReportOrganizationFilter"></td>
						<td class="noborder"></td>
					</tr>
					<tr>
						<th>Volunteer</th>
						<th>Date</th>
						<th>Hours</th>
						<th class="select-filter">Service Role</th>
						<th class="select-filter">Location</th>
						<th class="select-filter">Organization</th>
						<th>Action</th>
					</tr>
				</thead>
				<tbody>
				</tbody>
			</table>
		</fieldset>

		<div class="adjustedHoursWrapper" style="display: none">
			<a class="appLink" style="margin-top:10px" id="adjustedHoursShowHide" href="#showHideAdjHrs"
				rel="#adjustedHoursSlidingDiv">Edit Adjusted Hours </a>
			<p>
			<div id="adjustedHoursSlidingDiv" class="toggleDiv"
				style="display: none;">
				<fieldset>
					<legend id="adjustedHoursLegend">Adjusted Hours</legend>
					<table id="adjustedHoursList" width="${rightBoxWidth}"
						style="max-width: ${rightBoxWidth}px" class="stripe"
						summary="Report of Adjusted Hours">
						<thead>
							<tr>
								<td class="noborder">Filters:</td>
								<td class="noborder"></td>
								<td class="noborder"></td>
								<td class="noborder"></td>
							</tr>
							<tr>
								<th width="60">Date</th>
								<th width="60">Hours</th>
								<th>Comments</th>
								<th width="100">Created By</th>
							</tr>
						</thead>
						<tbody>
						</tbody>
					</table>
					<c:if test="${not FORM_READ_ONLY}">
					<div class="clearCenter">
						<a class="buttonAnchor" href="javascript:addAdjustedHoursEntry()">Add
							Entry</a>
					</div>
					</c:if>
				</fieldset>
			</div>
		</div>
	</div>
</div>

<div id="editWorkEntryDialog" style="display: none"
	title="Edit Time Entry">
	<div class="clearCenter">
		<table>
			<tr>
				<td align="right">Date:</td>
				<td width="10"><span class="requdIndicator" id="nameRequired">*</span></td>
				<td><input type="text" id="editWorkEntryDate" class="dateInput"
					size="16" /></td>
			</tr>
			<tr>
				<td align="right">Hours:</td>
				<td width="10"><span class="requdIndicator" id="nameRequired">*</span></td>
				<td><input type="text" id="editWorkEntryHours" size="6"
					maxlength="5" title="Enter hours as a decimal. For example, 5 hours and 15 minutes would be entered as 5.25. Field rounds to the nearest quarter hour." /></td>
			</tr>
			<tr>
				<td align="right">Assignment:</td>
				<td width="10"><span class="requdIndicator" id="nameRequired">*</span></td>
				<td><div id="editWorkEntryAssignmentWrapper"></div></td>
			</tr>
			<tr>
				<td align="right">Organization:</td>
				<td width="10"><span class="requdIndicator" id="nameRequired">*</span></td>
				<td><div id="editWorkEntryOrganizationWrapper"></div></td>
			</tr>
		</table>
	</div>
</div>

<div id="addAdjustedHoursEntryDialog" style="display: none"
	title="Add Time Adjustment">
	<div class="clearCenter">
		<table>
			<tr>
				<td align="right">Date:</td>
				<td width="10"><span class="requdIndicator" id="nameRequired">*</span></td>
				<td><input type="text" id="addAdjustedHoursDate" class="dateInput"
					size="16" /></td>
			</tr>
			<tr>
				<td align="right">Hours:</td>
				<td width="10"><span class="requdIndicator" id="nameRequired">*</span></td>
				<td><input type="text" id="addAdjustedHoursHours" size="10"
					maxlength="9" title="Enter hours as a decimal. For example, 5 hours and 15 minutes would be entered as 5.25. Field rounds to the nearest quarter hour." /></td>
			</tr>
			<tr>
				<td align="right">Comments:</td>
				<td width="10"><span class="requdIndicator" id="nameRequired">*</span></td>
				<td><textarea id="addAdjustedHoursComments" rows="3" cols="60" maxlength="250"></textarea></td>
			</tr>
		</table>
	</div>
</div>