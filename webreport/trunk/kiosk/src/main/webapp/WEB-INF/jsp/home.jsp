<%@ include file="shared/inc_header.jsp"%>

<style>
.eventButton {
	display: inline-block;
	min-width: 250px;
	max-width: 250px;
	min-height: 250px;
	max-height: 250px;
	line-height: 290px;
	border: 3px solid #0033DD;
	border-radius: 15px;
	font-size: 16pt;
	text-align: center;
	margin:10px 20px;
	cursor:pointer;
}
.eventButton a {
	
}
.postTime {
  background: url('${imgHome}/clock.png');
}
.profile {
  background: url('${imgHome}/profile.png');
}
.opportunities {
	background: url('${imgHome}/volunteer.png');
}
.eventButton span {
	color:black;
	vertical-align: middle;
	text-shadow: white 0.1em 0.1em 0.1em;
}
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

<div style="width: 90vw">
	<c:if test="${not empty homepageAnnouncement}">
		<div class="notificationAlert notificationBox"
			style="margin-left: 10%; vertical-align: top; display: inline-block">
			<h1 style="text-align: center">
				<u>Announcements:</u>
				<p />
				${homepageAnnouncement}
			</h1>
		</div>
	</c:if>

</div>

<div class="clearCenter" style="margin-top:25px; margin-bottom:20px; text-align:center">${homepageContent}</div>

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
			<img align="left" src="${imgHome}/volunteer.png" height="80"
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
