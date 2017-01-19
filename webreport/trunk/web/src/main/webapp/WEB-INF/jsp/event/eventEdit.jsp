<%@ include file="../shared/inc_header.jsp"%>

<c:if test="${FORM_READ_ONLY}">
	<%@ include file="../shared/inc_modifyToView.jsp"%>
</c:if>

<script type="text/javascript">
	var isReadOnly = <c:out value="${FORM_READ_ONLY}" default="false"/>
	var eventId = <c:out value="${command.event.id}" default="null" />

	$(function() {
		buildParticipantTable()

		if (eventId != null)
			refreshParticipantTable()
	})
</script>

<script type="text/javascript" src="${jsHome}/eventProfile.js"></script>

<form:form method="post" action="${home}/eventSubmit.htm" id="eventForm">

	<div class="clearCenter">
		<div class="leftHalf">
			<fieldset>
				<legend>Basic Fields</legend>

				<table width="100%">
					<tr>
						<td class='appFieldLabel' nowrap>Date:</td>
						<td style="text-align: left"><span class='requdIndicator'>*</span></td>
						<td><app:input path="event.date" id="eventDate" size="10"
								cssClass="dateInput" /> <app:errors path="event.date"
								cssClass="msg-error" /></td>
					</tr>
					<tr>
						<td class='appFieldLabel' nowrap><label
							for='donationDateInput'>Name:</label></td>
						<td style="text-align: left"><span class='requdIndicator'>*</span></td>
						<td><app:input path="event.name" id="eventName" size="20" />
							<app:errors path="event.name" cssClass="msg-error" /></td>
					</tr>
				</table>
			</fieldset>
			<div class="clearCenter">
				<c:if test="${not FORM_READ_ONLY}">
					<input class="submitAnchor" type="submit" value="Submit"
						id="submitFormButton" />
				</c:if>
				<a id="cancelOperationBtn" class="buttonAnchor"
					href="javascript:cancelDonation();${current_breadcrumb}">Cancel</a>
			</div>
		</div>
		<div class="rightHalf">
			<fieldset>
				<legend>Participants</legend>
				<div class="clearCenter">
					<a id="addParticipantBtn" class="buttonAnchor"
						href="javascript:addParticipant()">Add Participant</a>
				</div>
				<table class="formatTable stripe" id="participantList" border="1"
					summary="Participant List">
					<thead>
						<tr>
							<th width="40%">Name</th>
							<th width="10%">Voter ID</th>
							<th width="10%">Precinct</th>
							<th width="10%">Status</th>
							<th width="20%">Contact Info</th>
							<th width="10%">Action</th>
						</tr>
					</thead>
				</table>
			</fieldset>
		</div>
	</div>

</form:form>