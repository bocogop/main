<%@ include file="../shared/inc_header.jsp"%>

<script type="text/javascript" src="${jsHome}/mealTicketList.js"></script>

<jsp:include page="/WEB-INF/jsp/shared/inc_volunteerSearchPopup.jsp">
	<jsp:param name="uniqueVolunteerSearchPopupId" value="addMealTicket" />
	<jsp:param name="resultCallbackMethod" value="addMealTicketForVolCallback" />
</jsp:include>

<style>
.ui-state-default a.tableHeaderLink, .ui-state-default a.tableHeaderLink:link,
	.ui-state-default a.tableHeaderLink:visited {
	text-decoration: underline;
	font-weight: normal;
}

table.dataTable thead th.tableHeaderLinkWrapper {
	font-weight: normal;
}
</style>


	<div align="left" style="margin-left: 15px">
		<b><label>Meal Tickets for: </label>
		<wr:localDate date="${mealDate}" pattern="${DATE_ONLY}"/>
	</div> 
<p>
<c:if test="${not FORM_READ_ONLY}">
	<div class="clearCenter">
		<fieldset>
		
				<table>
					<tr>
						<td class='appFieldLabel' nowrap>Volunteer Last Name:</td>
						<td><input type="text" id="volunteerLastNameInput" size="15"
							maxLength="30" /></td>
						<td class='appFieldLabel' nowrap>First Name:</td>
						<td><input type="text" id="volunteerFirstNameInput" size="15"
							maxLength="30" /></td>
						<td><a id="addVolunteerButton" class="buttonAnchor"
							href="javascript:addVolunteerMealTicket()">Search</a></td>	
					</tr>
					<tr>
						<td class='appFieldLabel' nowrap>Occasional Volunteer Last Name:</td>
						<td><input type="text" id="occasionalLastNameInput" size="15"
							maxLength="30" /></td>
						<td class='appFieldLabel' nowrap>First Name:</td>
						<td><input type="text" id="occasionalFirstNameInput" size="15"
							maxLength="30" /></td>
						<td><a id="addOccasionalVolButton" class="buttonAnchor"
							href="javascript:addOccasionalVolunteer()">Add</a></td>	
					</tr>
				</table>		
		</fieldset>
	</div>
</c:if>
<p>

<c:if test="${not empty command.mealTickets}">

	<div align="left" style="margin-left: 15px">
		<a id="printSelectedButton" class="buttonAnchor"
			href="javascript:printSelectedMealTickets()">Print Selected</a>
	</div>

	<table class="stripe" summary="Format table" align="center" width="98%">
		<tr>
			<td align="center">
				<table id="mealTicketSearchResultsList"
					summary="Daily Meal Tickets">
					<thead>
						<tr id="mealTicektSearchFilterRow">
							<td class="noborder"></td>
							<td class="noborder"></td>
							<td class="noborder"></td>
							<td class="noborder"></td>
							<td class="noborder" title="Filter by Printed"></td>
							<td class="noborder" title="Filter by Reprinted"></td>
							<c:if test="${not FORM_READ_ONLY}">
								<td class="noborder"></td>
							</c:if>
						</tr>
					<tr>
						<th align="center" nowrap class="tableHeaderLinkWrapper">Select<br>
								<a class="tableHeaderLink"
								href="javascript:setAllCheckboxes(true)">All</a> / <a
								class="tableHeaderLink"
								href="javascript:setAllCheckboxes(false)">None</a>
						</th>
						<th>Volunteer Name</th>
						<th>Occasional Volunteer</th>
						<th>Meal Ticket ID</th>
						<th class="select-filter">Printed</th>
						<th class="select-filter">Reprinted</th>
						<c:if test="${not FORM_READ_ONLY}">
							<th width="10%">Action</th>
						</c:if>
					</tr>
					</thead>

					<c:forEach var="mt" items="${command.mealTickets}">
						<tr>
							<td><input type="checkbox" name="mealTicketSelect" value="${mt.id}" />
							</td>
							<td><c:out value="${mt.volunteer.displayName}"/></td>
							<td><c:if test="${empty mt.volunteer}"><c:out value="${mt.displayName}"/></c:if></td>
							<td><c:out value="${mt.id}"/></td>
							<td><wr:zonedDateTime value="${mt.lastPrintedDate}" zoneId="${userTimeZone}"/></td>
							<td><c:if test="${mt.reprinted}">Yes</c:if> 
								<c:if test="${!mt.reprinted}">No</c:if>
							</td>
							<c:if test="${not FORM_READ_ONLY}">
								<td><c:if test="${empty mt.lastPrintedDate}"> <a href="javascript:deleteMealTicket('${mt.id}')"
												title="Are you sure you want to delete this meal ticket?"><img
												src="${imgHome}/permanently_delete_18x18.png" border="0"
												hspace="5" align="center" alt="Delete Meal Ticket" /></a>
												</c:if>
												</td>
							</c:if>
						</tr>
					</c:forEach>
				</table>
			</td>
		</tr>
	</table>
</c:if>
<c:if test="${empty command.mealTickets}">
	<table align="center" cellpadding="10">
		<tr>
			<td>Sorry, no meal ticket were found that matched the specified
				criteria.</td>
		</tr>
	</table>
</c:if>

