<%@ include file="../shared/inc_header.jsp"%>

<%--
	Expects the following params:
	
	- A "uniqueVoterSearchPopupId" param which should be a string of letters that
		uniquely identifies this voterSearchPopup among other voterSearchPopups on the same page
	- A "mode" param set to either "search", "add" or "duplicateCheck":
			If "search", search fields are shown and the callback is called as normal when the person clicks a result
			If "add", an "Add Voter" button will appear after the user has searched once, and the scope will be
				hardcoded to national.
			If "duplicateCheck", an array of voter objects will be provided directly to the popup via the
				popupVoterSearch method.
				
		If "mode" is unspecified, it defaults to "search".
		
	- A "callbackMethod" param which specifies the name of a JavaScript method defined by the
		-including- page which the voter popup will activate when the user selects a voter. The
		required method signature looks like:
		function someMethodWithUniqueName(voterObj)
		
		The voterObj parameter sent to the callback method is a javascript object containing the
		properties
		{
			id : <the ID of the voter selected>,
			[... other attributes of the Voter class marked for JsonProperty]
		}
		
	To activate this popup, call the method popupVoterSearch(uid) and pass in the same
	uniqueVoterSearchPopupId String above.
--%>

<c:set var="uid" value="${fn:escapeXml(param.uniqueVoterSearchPopupId)}" />

<script type="text/javascript">
	$(function() {
		initVoterSearchPopup({
			uid : "${uid}",
			mode : "<c:out value="${empty param.mode ? 'search' : fn:escapeXml(param.mode)}" />",
			maxResults : <fmt:message key="voterSearch.maxResults" />,
			callbackMethod : <c:out value="${empty param.resultCallbackMethod ? 'null' : fn:escapeXml(param.resultCallbackMethod)}" />,
			addButtonCallbackMethod : <c:out value="${empty param.addButtonCallbackMethod ? 'null' : fn:escapeXml(param.addButtonCallbackMethod)}" />,
			showDisclaimer : <c:out value="${not empty fn:escapeXml(param.disclaimerText)}" />
		})
	})
</script>

<div id="voterSearchDialog${uid}" style="display: none"
	title="Search for Voter">
	<div align="center" id="volSearchDisclaimer${uid}"
		style="display: none; font-weight: bold">
		<c:out value="${fn:escapeXml(param.disclaimerText)}" />
		<p />
	</div>

	<table align="center">
		<tr valign="top" class="voterSearchFields${uid}">
			<td>
				<table>
					<tr>
						<td align="right" nowrap><label for='voterSearchLastName${uid}'>Last Name:</label></td>
						<td><input size="15" type="text" id="voterSearchLastName${uid}"
							value="" title="Type the last name" /></td>
						<td align="right" nowrap><label for='voterSearchFirstName${uid}'>First Name:</label></td>
						<td><input size="10" type="text" id="voterSearchFirstName${uid}"
							value="" title="Type the first name" /></td>
					</tr>
					<tr>
						<td align="right"><label for='voterSearchCode${uid}'>Identifying
								Code:</label></td>
						<td><input type="text" id="voterSearchCode${uid}"
							value="" title="Type an identifying code" size="10" /></td>
						<td align="right"><label for='voterSearchDOB${uid}'>Date
								of Birth:</label></td>
						<td><input size="10" id="voterSearchDOB${uid}" /></td>
					</tr>
					<tr>
						<td align="right"><label for='voterSearchEmail${uid}'>Email:</label></td>
						<td colspan="3"><input type="text" size="32" id="voterSearchEmail${uid}"
							value="" title="Type all or part of an email" /></td>
					</tr>
				</table>
			</td>
			<td width="10">&nbsp;</td>
			<td>
				<table>
					<tr>
						<td align="right"><label for='voterSearchScope${uid}'>Scope:</label></td>
						<td><input type="radio" id="voterSearchScopeLocal${uid}"
							name="voterSearchScope${uid}" value="Local" checked="checked">Within
							Precinct <input type="radio"
							id="voterSearchScopeNational${uid}"
							name="voterSearchScope${uid}" value="National"
							style="margin-left: 50px">National</td>
					</tr>
					<tr>
						<td></td>
						<td><div id="voterSearchPrecinctWrapper${uid}">
								<select id="voterSearchScopePrecinctId${uid}">
									<c:if test="${not empty precinctContextId}">
										<option value="${precinctContextId}"
											selected="selected"><c:out
												value="${precinctContextName}" /></option>
									</c:if>
								</select>
							</div> <img src="${imgHome}/spacer.gif" height="1" width="400" alt="" /></td>
					</tr>
					<tr>
						<td align="right"></td>
						<td><input type="checkbox"
							id="voterSearchIncludeInactive${uid}" value="true" /> Include Inactive Voters</td>
					</tr>
				</table>
			</td>
		</tr>
		<tr align="center">
			<td colspan="3"><a style="margin-left: 20px"
				class="buttonAnchor voterSearchLink${uid} voterSearchFields${uid}"
				href="#">Search</a> <a style="display: none"
				class="buttonAnchor voterAddLink${uid}" href="#">Add New
					Voter</a></td>
		</tr>
	</table>
	<div id="voterSearchNoResults${uid}" style="display: none">
		<table align="center" cellpadding="10">
			<tr>
				<td>Sorry, no voters were found that matched the specified
					criteria.</td>
			</tr>
		</table>
	</div>
	<div id="voterSearchMaxResults${uid}"
		 class="redText" style="display: none; padding: 10px;" align="center">Your search
		exceeded the maximum results. Please enter additional search criteria.
	</div>
	<div id="voterSearchResultsTable${uid}" style="display: none">
		<table id="voterSearchResultsList${uid}" class="stripe"
			summary="List of Voters">
			<thead>
				<tr id="voterSearch${uid}FilterRow">
					<td class="noborder">Filters:</td>
					<td class="noborder" title="Filter by Primary Precinct"></td>
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
					<th width="10%" class="select-filter">Primary Precinct</th>
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
