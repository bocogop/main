<%@ include file="shared/inc_header.jsp"%>

<script type="text/javascript">
	var pleaseWaitText = '<spring:message code="pleaseWait" />'
	var primaryVolOrganizationId = <c:out value="${primaryVolOrganization.id}" default="-1" />
	var greaterThan24HoursErrorText = "<spring:message code="greaterThan24HoursErrorText" />"
	var greaterThan0MinutesText = "<spring:message code="greaterThan0MinutesText" />"
	var uniqueAssignmentsAndOrgsText = "<spring:message code="uniqueAssignmentsAndOrgsText" />"
</script>

<script type="text/javascript" src="${jsHome}/postTime.js"></script>

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

.blueDiv {
	border: 2px solid #0033DD;
	border-radius: 15px;
	margin: 10px 20px;
	min-height: 200px;
	padding: 6px;
}

.profileDiv {
	min-width: 550px;
	max-width: 550px;
}

.opportunitiesDiv {
	min-width: 600px;
	max-width: 600px;
}

.bottomTitle {
	font-size: 14pt;
	line-height: 40px;
}

.blueDiv hr {
	border-top: dotted 2px;
	color: #f13458;
}

.userSummaryField {
	font-weight: bold;
}
</style>

<div class="clearCenter timeEntryContainer">
	<div class="blueDiv" id="timeEntryWrapper">
		<div id="missingFieldsNotice" class="clearCenter"
			class="redText" style="margin-top: 10px; display: none; font-weight: bold">
			<spring:message code="enterFieldsHighlighted" />
			:
		</div>
		<table id="timeEntryList" class="stripe" style="margin-top: 10px"
			width="1200" style="max-width: 1200px" summary="List of Time Entries">
			<thead>
				<tr>
					<th width="1"><spring:message code="hours" /></th>
					<th width="1"><spring:message code="minutes" /></th>
					<th><spring:message code="assignment" /></th>
					<th><spring:message code="organization" /></th>
				</tr>
			</thead>
			<tbody>
			</tbody>
		</table>

		<div class="clearCenter" style="margin-top: 10px;">
			<a title="<spring:message code="postAllTimeEntries" />"
				id="postAllButton" href="#" class="buttonAnchor"><nobr>
					<spring:message code="postAll" />
				</nobr> <img src="${imgHome}/big-right-arrow.png" border="0"
				style="margin-left: 10px" align="absmiddle"></a>
		</div>
	</div>
	<div id="timeEntryUnmetRequirements" class="clearCenter redText"
		style="display: none">
		<spring:message code="unmetRequirements" />
		<ul id="globalOrFacilityUnmetRequirements"></ul>
	</div>
	<div id="timeEntryNoAssignments" class="clearCenter"
		style="display: none">
		<spring:message code="noActiveAssignments" />
	</div>
	<div id="timeEntryNoOrganizations" class="clearCenter"
		style="display: none">
		<spring:message code="noActiveOrganizations" />
	</div>
</div>
<div class="clearCenter timeEntryContainer" style="margin-top: 10px;">
	<div class="leftHalf">
		<div class="blueDiv profileDiv">
			<table width="100%">
				<tr valign="top">
					<td width="1"><img align="left" src="${imgHome}/profile.png"
						height="80" style="margin-right: 20px" /></td>
					<td><span class="bottomTitle"><spring:message
								code="userSummary" /></span>
						<div style="display: inline-block; float: right">
							<a id="viewProfileButton" class="buttonAnchor" href="${home}/voterEdit.htm"><img
								src="${imgHome}/big-left-arrow.png" hspace="5" align="absmiddle" />
								<spring:message code="viewFullProfile" /></a>
						</div>
						<hr></td>
				</tr>
			</table>

			<table>
				<tr>
					<td align="right" class="userSummaryField"><spring:message
							code="name" />:</td>
					<td><c:out value="${voter.displayName}" /></td>
				</tr>
				<tr>
					<td align="right" class="userSummaryField" nowrap><spring:message
							code="hoursThisYear" />:</td>
					<td><span id="yearHours"></span></td>
				</tr>
				<tr>
					<td align="right" class="userSummaryField" nowrap><spring:message
							code="lifetimeHours" />:</td>
					<td><span id="totalHours"></span></td>
				</tr>
				<%--
				<c:if test="${not empty voter.lastAward}">
					<tr valign="top" id="currentAwardRow">
						<td align="right" class="userSummaryField" nowrap><spring:message code="currentAward" />:</td>
						<td><span id="currentAward"><c:out value="${voter.lastAward.displayName}" /><img src="${imgHome}/star-48.png" height="16" />
							<c:if test="${not empty voter.lastAwardDate}"> on <vss:localDate date="${voter.lastAwardDate}" /></c:if>
						</span></td>
					</tr>
				</c:if>
				 --%>

				<tr>
					<td align="right" class="userSummaryField" nowrap><spring:message
							code="phoneNumber" />:</td>
					<td><c:if test="${not empty voter.phone}">
							<c:out value="${voter.phone}" />
						</c:if> <c:if test="${empty voter.phone}"><span class="redText">(none)</span></c:if></td>
				</tr>

				<tr>
					<td align="right" class="userSummaryField" nowrap><spring:message
							code="email" />:</td>
					<td><c:if test="${not empty voter.email}">
						<c:out value="${voter.email}" />
					</c:if> <c:if test="${empty voter.email}"><span class="redText">(none)</span></c:if></td>
				</tr>
					<tr valign="top">
						<td align="right" class="userSummaryField" nowrap><spring:message
								code="address" />:</td>
						<td>
						<c:if test="${not empty voter.addressMultilineDisplay}">
							<pre>
								<c:out value="${voter.addressMultilineDisplay}" />
							</pre>
						</c:if>
						<c:if test="${empty voter.addressMultilineDisplay}">
							<span class="redText">(none)</span>
						</c:if>
						</td>
					</tr>
			</table>
		</div>
	</div>

	<div class="rightHalf">
		<div class="blueDiv opportunitiesDiv">
			<img align="left" src="${imgHome}/voter.png" height="80"
				style="margin-right: 20px" /> <span class="bottomTitle"><spring:message
					code="opportunities" /></span>
			<div style="display: inline-block; float: right">
				<a id="viewAllOpportunitiesButton" class="buttonAnchor" href="#"><img
					src="${imgHome}/big-left-arrow.png" hspace="5" align="absmiddle" />
					<spring:message code="viewAllOpportunities" /></a>
			</div>
			<hr>

			<div align="center" style="margin-top: 25px">
				<spring:message code="noOpportunities" />
			</div>
		</div>
	</div>


</div>