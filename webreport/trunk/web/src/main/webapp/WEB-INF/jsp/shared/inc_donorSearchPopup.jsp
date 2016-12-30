<%@ include file="../shared/inc_header.jsp"%>

<%--
	Expects the following params:
	
	- A "uniqueDonorSearchPopupId" param which should be a string of letters that
		uniquely identifies this donorSearchPopup among other donorSearchPopups on the same page
	- A "mode" param set to either "search", "add" or "duplicateCheck"
			If "search", search fields are shown and the callback is called as normal when the person clicks a result
			If "add", an "Add Donor" button will appear after the user has searched once, and the donor type will be
				hardcoded to Individual.
			If "duplicateCheck", an array of donor objects will be provided directly to the popup via the
				popupDonorSearch method.
				
		If "mode" is unspecified, it defaults to "search".
		
	- A "callbackMethod" param which specifies the name of a JavaScript method defined by the
		-including- page which the donor popup will activate when the user selects a donor. The
		required method signature looks like:
		function someMethodWithUniqueName(donorObj)
		
		The donorObj parameter sent to the callback method is a javascript object containing the
		properties
		{
			id : <the ID of the donor selected>,
			[... other attributes of the Donor class marked for JsonProperty]
		}
		
	To activate this popup, call the method popupDonorSearch(uid) and pass in the same
	uniqueDonorSearchPopupId String above.
--%>

<c:set var="uid">
	<c:out value="${fn:escapeXml(param.uniqueDonorSearchPopupId)}" />
</c:set>



<script type="text/javascript">
	$(function() {
		initDonorSearchPopup({
			uid : "${uid}",
			mode : "<c:out value="${empty param.mode ? 'search' : fn:escapeXml(param.mode)}" />",
			maxResults : <fmt:message key="donorSearch.maxResults" />,
			callbackMethod : <c:out value="${empty param.resultCallbackMethod ? 'null' : fn:escapeXml(param.resultCallbackMethod)}" />,
			addButtonCallbackMethod : <c:out value="${empty param.addButtonCallbackMethod ? 'null' : fn:escapeXml(param.addButtonCallbackMethod)}" />,
			showDisclaimer : <c:out value="${not empty fn:escapeXml(param.disclaimerText)}" />
		})
	})
</script>

<div id="donorSearchDialog${uid}" style="display: none"
	title="Search for Donor">
	<div align="center" id="donorSearchDisclaimer${uid}"
		style="display: none; font-weight: bold">
		<c:out value="${fn:escapeXml(param.disclaimerText)}" />
		<p />
	</div>
	
	<table align="center">
		<tr valign="top" class="donorSearchFields${uid}">
			<td>
				<table class="orgSearchToggleFields${uid}">
					<tr>
						<td align="right" nowrap><label for='donorSearchOrgName${uid}'>Organization Name:</label></td>
						<td><input size="20" type="text" id="donorSearchOrgName${uid}"
							value="" title="Type the organization name" /></td>
					</tr>
				</table>
				
				<table class="donorSearchToggleFields${uid}">
					<tr>
						<td align="right" nowrap><label for='donorSearchLastName${uid}'>Last Name:</label></td>
						<td><input size="15" type="text" id="donorSearchLastName${uid}"
							value="" title="Type the last name" /></td>
						<td align="right" nowrap><label for='donorSearchFirstName${uid}'>First Name:</label></td>
						<td><input size="10" type="text" id="donorSearchFirstName${uid}"
							value="" title="Type the first name" /></td>
					</tr>
					<tr>
						<td align="right"><label for='donorSearchEmail${uid}'>Email:</label></td>
						<td colspan="3"><input size="30" type="text" id="donorSearchEmail${uid}" value=""
							title="Type all or part of an email" /></td>
					</tr>
					<tr>
						<td align="right"><label for='donorSearchPhone${uid}'>Phone:</label></td>
						<td colspan="3"><input size="30" type="text" id="donorSearchPhone${uid}" value=""
							title="Type all or part of a phone" /></td>
					</tr>
				</table>
			</td>
			<td>
				<div class="donorSearchToggleFields${uid}">
					<table>
						<tr>
							<td align="right"><label for='donorSearchCity${uid}'>City:</label></td>
							<td><input type="text" id="donorSearchCity${uid}" value=""
								title="Type all or part of a city" /></td>
						</tr>
						<tr>
							<td align="right"><label for='donorSearchState${uid}'>State:</label></td>
							<td><select id="donorSearchState${uid}">
									<option value=""></option>
									<c:forEach items="${allStates}" var="state">
										<option value="${state.id}" stateCode="${state.postalName}"><c:out
												value="${state.name}" /></option>
									</c:forEach>
							</select></td>
						</tr>
						<tr>
							<td align="right"><label for='donorSearchZip${uid}'>Zip:</label></td>
						 	<td><input
								type="text" id="donorSearchZip${uid}" value="" size="10"
								title="Type all or part of a zip" /></td>
						</tr>
					</table>
				</div>
			</td>
			<td>
				<table style="margin-left: 20px">
					<tr>
						<td align="right"><label for='donorSearchType${uid}'>Donor
								Type:</label></td>
						<td><input type="radio" id="donorSearchTypeIndividual${uid}"
							name="donorSearchType${uid}" value="1" checked="checked">Individual
							<input type="radio" id="donorSearchTypeOrganization${uid}"
							name="donorSearchType${uid}" value="4" style="margin-left: 15px">Organization</td>
					</tr>
					<tr class="donorSearchToggleFields${uid}">
						<td align="right"><label for='donorSearchPrecinct${uid}'>Donated
								at:</label></td>
						<td><input type="radio" id="donorSearchPrecinctLocal${uid}"
							name="donorSearchPrecinct${uid}" value="L" checked="checked"><c:out value="${precinctContextName}" />
							<input type="radio" id="donorSearchPrecinctAll${uid}"
							name="donorSearchPrecinct${uid}" value="A"
							style="margin-left: 15px">Any Precinct</td>
					</tr>
				</table>
			</td>
		</tr>
		<tr align="center">
			<td colspan="3"><a
				class="buttonAnchor donorSearchLink${uid} donorSearchFields${uid}"
				tabIndex="0">Search</a> <a style="display: none"
				class="buttonAnchor donorAddLink${uid}" tabIndex="1">Add New
					Donor</a></td>
		</tr>
	</table>
	<div id="donorSearchNoResults${uid}" style="display: none">
		<table align="center" cellpadding="10">
			<tr>
				<td>Sorry, no donors were found that matched the specified
					criteria.</td>
			</tr>
		</table>
	</div>
	<div id="donorSearchMaxResults${uid}"
		 class="redText" style="display: none; padding: 10px;" align="center">Your search
		exceeded the maximum results. Please enter additional search criteria.
	</div>
	<div id="donorSearchResultsTable${uid}" style="display: none">
		<table id="donorSearchResultsList${uid}" class="stripe"
			summary="List of Donors">
			<thead>
				<tr id="donorSearch${uid}FilterRow">
					<td class="noborder">Filters:</td>
					<td class="noborder" title="Filter by Donor is Voter"></td>
					<td class="noborder" title="Filter by Organization Precinct"></td>
					<td class="noborder"></td>
					<td class="noborder"></td>
					<td class="noborder" title="Filter by Precinct"></td>
					<td class="noborder" title="Filter by Donation Date"></td>
					<td class="noborder"></td>
					<td class="noborder"></td>
				</tr>
				<tr>
					<th id="nameHeaderCol${uid}">Name</th>
					<th class="select-filter">Donor is Voter?</th>
					<th class="select-filter">Precinct</th>
					<th>Contact Name</th>
					<th>Contact Info</th>
					<th class="select-filter">Last Donation Precinct</th>
					<th class="select-filter">Last Donation Date</th>
					<th>Last Donation Type</th>
					<th>Last Donation Amount</th>
				</tr>
			</thead>
		</table>
	</div>
</div>
