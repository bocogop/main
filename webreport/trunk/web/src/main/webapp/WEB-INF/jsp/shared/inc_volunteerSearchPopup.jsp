<%@ include file="../shared/inc_header.jsp"%>

<%--
	Expects the following params:
	
	- A "uniqueVolunteerSearchPopupId" param which should be a string of letters that
		uniquely identifies this volunteerSearchPopup among other volunteerSearchPopups on the same page
	- A "mode" param set to either "search", "add" or "duplicateCheck":
			If "search", search fields are shown and the callback is called as normal when the person clicks a result
			If "add", an "Add Volunteer" button will appear after the user has searched once, and the scope will be
				hardcoded to national.
			If "duplicateCheck", an array of volunteer objects will be provided directly to the popup via the
				popupVolunteerSearch method.
				
		If "mode" is unspecified, it defaults to "search".
		
	- A "callbackMethod" param which specifies the name of a JavaScript method defined by the
		-including- page which the volunteer popup will activate when the user selects a volunteer. The
		required method signature looks like:
		function someMethodWithUniqueName(volunteerObj)
		
		The volunteerObj parameter sent to the callback method is a javascript object containing the
		properties
		{
			id : <the ID of the volunteer selected>,
			[... other attributes of the Volunteer class marked for JsonProperty]
		}
		
	To activate this popup, call the method popupVolunteerSearch(uid) and pass in the same
	uniqueVolunteerSearchPopupId String above.
--%>

<c:set var="uid" value="${fn:escapeXml(param.uniqueVolunteerSearchPopupId)}" />

<script type="text/javascript">
	$(function() {
		initVolunteerSearchPopup({
			uid : "${uid}",
			mode : "<c:out value="${empty param.mode ? 'search' : fn:escapeXml(param.mode)}" />",
			maxResults : <fmt:message key="volunteerSearch.maxResults" />,
			callbackMethod : <c:out value="${empty param.resultCallbackMethod ? 'null' : fn:escapeXml(param.resultCallbackMethod)}" />,
			addButtonCallbackMethod : <c:out value="${empty param.addButtonCallbackMethod ? 'null' : fn:escapeXml(param.addButtonCallbackMethod)}" />,
			showDisclaimer : <c:out value="${not empty fn:escapeXml(param.disclaimerText)}" />
		})
	})
</script>

<div id="volunteerSearchDialog${uid}" style="display: none"
	title="Search for Volunteer">
	<div align="center" id="volSearchDisclaimer${uid}"
		style="display: none; font-weight: bold">
		<c:out value="${fn:escapeXml(param.disclaimerText)}" />
		<p />
	</div>

	<table align="center">
		<tr valign="top" class="volunteerSearchFields${uid}">
			<td>
				<table>
					<tr>
						<td align="right" nowrap><label for='volunteerSearchLastName${uid}'>Last Name:</label></td>
						<td><input size="15" type="text" id="volunteerSearchLastName${uid}"
							value="" title="Type the last name" /></td>
						<td align="right" nowrap><label for='volunteerSearchFirstName${uid}'>First Name:</label></td>
						<td><input size="10" type="text" id="volunteerSearchFirstName${uid}"
							value="" title="Type the first name" /></td>
					</tr>
					<tr>
						<td align="right"><label for='volunteerSearchCode${uid}'>Identifying
								Code:</label></td>
						<td><input type="text" id="volunteerSearchCode${uid}"
							value="" title="Type an identifying code" size="10" /></td>
						<td align="right"><label for='volunteerSearchDOB${uid}'>Date
								of Birth:</label></td>
						<td><input size="10" id="volunteerSearchDOB${uid}" /></td>
					</tr>
					<tr>
						<td align="right"><label for='volunteerSearchEmail${uid}'>Email:</label></td>
						<td colspan="3"><input type="text" size="32" id="volunteerSearchEmail${uid}"
							value="" title="Type all or part of an email" /></td>
					</tr>
				</table>
			</td>
			<td width="10">&nbsp;</td>
			<td>
				<table>
					<tr>
						<td align="right"><label for='volunteerSearchScope${uid}'>Scope:</label></td>
						<td><input type="radio" id="volunteerSearchScopeLocal${uid}"
							name="volunteerSearchScope${uid}" value="Local" checked="checked">Within
							Facility <input type="radio"
							id="volunteerSearchScopeNational${uid}"
							name="volunteerSearchScope${uid}" value="National"
							style="margin-left: 50px">National</td>
					</tr>
					<tr>
						<td></td>
						<td><div id="volunteerSearchFacilityWrapper${uid}">
								<select id="volunteerSearchScopeFacilityId${uid}">
									<c:if test="${not empty facilityContextId}">
										<option value="${facilityContextId}"
											selected="selected"><c:out
												value="${facilityContextName}" /></option>
									</c:if>
								</select>
							</div> <img src="${imgHome}/spacer.gif" height="1" width="400" alt="" /></td>
					</tr>
					<tr>
						<td align="right"></td>
						<td><input type="checkbox"
							id="volunteerSearchIncludeInactive${uid}" value="true" /> Include Inactive Volunteers</td>
					</tr>
				</table>
			</td>
		</tr>
		<tr align="center">
			<td colspan="3"><a style="margin-left: 20px"
				class="buttonAnchor volunteerSearchLink${uid} volunteerSearchFields${uid}"
				href="#">Search</a> <a style="display: none"
				class="buttonAnchor volunteerAddLink${uid}" href="#">Add New
					Volunteer</a></td>
		</tr>
	</table>
	<div id="volunteerSearchNoResults${uid}" style="display: none">
		<table align="center" cellpadding="10">
			<tr>
				<td>Sorry, no volunteers were found that matched the specified
					criteria.</td>
			</tr>
		</table>
	</div>
	<div id="volunteerSearchMaxResults${uid}"
		 class="redText" style="display: none; padding: 10px;" align="center">Your search
		exceeded the maximum results. Please enter additional search criteria.
	</div>
	<div id="volunteerSearchResultsTable${uid}" style="display: none">
		<table id="volunteerSearchResultsList${uid}" class="stripe"
			summary="List of Volunteers">
			<thead>
				<tr id="volunteerSearch${uid}FilterRow">
					<td class="noborder">Filters:</td>
					<td class="noborder" title="Filter by Primary Facility"></td>
					<td class="noborder"></td>
					<td class="noborder" title="Filter by Gender"></td>
					<td class="noborder" title="Filter by Status"></td>
					<td class="noborder"></td>
					<%--
					<td class="noborder"></td>
					 --%>
				</tr>
				<tr>
					<th width="40%" id="nameHeaderCol${uid}">Name</th>
					<th width="10%" class="select-filter">Primary Facility</th>
					<th width="10%">Date of Birth</th>
					<th width="5%" class="select-filter">Gender</th>
					<th width="10%" class="select-filter" nowrap>Status</th>
					<th width="20%">Contact Info</th>
					<%--
					<th width="5%" nowrap>Identifying<br>Code</th>
					--%>
				</tr>
			</thead>
		</table>
	</div>
</div>
