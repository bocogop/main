<%@ include file="../shared/inc_header.jsp"%>

<%--
	Expects the following params:
	
	- A "uid" param which should be a string of letters that
		uniquely identifies this donationSearchPopup among other donationSearchPopups on the same page
	- A "callbackMethod" param which specifies the name of a JavaScript method defined by the
		-including- page which the donation popup will activate when the user selects a donation. The
		required method signature looks like:
		function someMethodWithUniqueName(donationObj)
		
		The donationObj parameter sent to the callback method is a javascript object containing the
		properties
		{
			id : <the ID of the donation selected>,
			[... other attributes of the Donation class marked for JsonProperty]
		}
		
	To activate this popup, call the method popupDonationSearch(uid) and pass in the same
	uid String above.
--%>

<c:set var="uid">
	<c:out value="${fn:escapeXml(param.uid)}" />
</c:set>

<script type="text/javascript">
	$(function() {
		initDonationSearchPopup({
			uid : "${uid}",
			maxResults : <fmt:message key="donationSummaryList.maxResults" />,
			callbackMethod : <c:out value="${empty param.resultCallbackMethod ? 'null' : fn:escapeXml(param.resultCallbackMethod)}" />
		})
	})
</script>

<div id="donationSearchDialog${uid}" style="display: none"
	title="Search for Donation">

	<table align="center">
		<tr valign="top" class="donationSearchFields${uid}">
			<td>
				<table>
					<tr>
						<td align="right">Begin Date:</td>
						<td><input size="12" id="donationSearchBeginDate${uid}"
							class="dateInput donationSearchInput${uid} donationSearchNonId${uid}" /></td>
					</tr>
					<tr>
						<td align="right">End Date:</td>
						<td><input size="12" id="donationSearchEndDate${uid}"
							class="dateInput donationSearchInput${uid} donationSearchNonId${uid}"" /></td>
					</tr>
				</table>
			</td>
			<td width="20" align="center">&nbsp;</td>
			<td>
				<table>
					<tr>
						<td align="right">Donor Name:</td>
						<td><input class="donationSearchInput${uid} donationSearchNonId${uid}" id="donationSearchDonorName${uid}" /></td>
					</tr>
					<tr>
						<td align="right"><label for='donationSearchDonorType${uid}'>Donor
								Type:</label></td>
						<td><select class="donationSearchNonId${uid}" id="donationSearchDonorType${uid}">
								<option value="">(any)</option>
								<c:forEach items="${allDonorTypes}" var="donorType">
									<option value="${donorType.id}"><c:out
											value="${donorType.donorType}" /></option>
								</c:forEach>
						</select></td>
					</tr>
				</table>
			</td>
			<td width="45" align="center"><b>Or</b></td>
			<td>
				Donation ID:<br>
				<input class="donationSearchInput${uid}" id="donationSearchDonationId${uid}" />
			</td>
		</tr>
		<tr align="center">
			<td colspan="5"><a
				class="buttonAnchor donationSearchLink${uid} donationSearchFields${uid}"
				tabIndex="0">Search</a></td>
		</tr>
	</table>
	<div id="donationSearchNoResults${uid}" style="display: none">
		<table align="center" cellpadding="10">
			<tr>
				<td>Sorry, no donations were found that matched the specified
					criteria.</td>
			</tr>
		</table>
	</div>
	<div id="donationSearchMaxResults${uid}" class="redText"
		style="display: none; padding: 10px;" align="center">Your search
		exceeded the maximum results. Please enter additional search criteria.
	</div>
	<div id="donationSearchResultsTable${uid}" style="display: none">
		<table id="donationSearchResultsList${uid}" class="stripe"
			summary="List of Donations">
			<thead>
				<tr>
					<th>Date</th>
					<th>Type</th>
					<th>Donor Type</th>
					<th>Donor Name</th>
					<th>Affiliation</th>
					<th>Org / Other Groups</th>
					<th>Description</th>
					<th>Value</th>
					<th>Id</th>
				</tr>
			</thead>
		</table>
	</div>
</div>
