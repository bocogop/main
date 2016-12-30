<%@ include file="../shared/inc_header.jsp"%>

<%--
	Expects the following params:
	
	- A "uniqueOrganizationSearchPopupId" param which should be a string of letters that
		uniquely identifies this organizationSearchPopup among other organizationSearchPopups on the same page
	- A "mode" param set to either "search", "add" or "duplicateCheck":
			If "search", search fields are shown and the callback is called as normal when the person clicks a result
			If "voterLink", only local orgs will be shown for precincts where the user also has the Create
				Voter permission
			If "donorLink"...
				
		If "mode" is unspecified, it defaults to "search".
	- A "includeInactiveOption" param, true/false, that specifies whether the checkbox for including inactive
		organizations is available to the user. If this is false or unspecified, the checkbox will be hidden
		and only active organizations will be shown.
	- A "callbackMethod" param which specifies the name of a JavaScript method defined by the
		-including- page which the organization popup will activate when the user selects a organization. The
		required method signature looks like:
		function someMethodWithUniqueName(organizationObj)
		
		The organizationObj parameter sent to the callback method is a javascript object containing the
		properties
		{
			id : <the ID of the organization selected>,
			[... other attributes of the Organization class marked for JsonProperty]
		}
		
	To activate this popup, call the method popupOrganizationSearch(uid) and pass in the same
	uniqueOrganizationSearchPopupId String above.
--%>

<c:set var="uid" value="${fn:escapeXml(param.uniqueOrganizationSearchPopupId)}" />

<script type="text/javascript">
	$(function() {
		initOrganizationSearchPopup({
			uid : "${uid}",
			maxResults : <fmt:message key="organizationSearch.maxResults" />,
			callbackMethod : <c:out value="${empty param.resultCallbackMethod ? 'null' : fn:escapeXml(param.resultCallbackMethod)}" />,
			addButtonCallbackMethod : <c:out value="${empty param.addButtonCallbackMethod ? 'null' : fn:escapeXml(param.addButtonCallbackMethod)}" />,
			includeInactiveOption : <c:out value="${empty param.includeInactiveOption ? 'false' : fn:escapeXml(param.includeInactiveOption)}" />,
			mode : "<c:out value="${empty param.mode ? 'null' : fn:escapeXml(param.mode)}" />"
		})
	})
	
	var shouldShowAddButton = ${not empty param.addButtonCallbackMethod}
	
</script>

<div id="organizationSearchDialog${uid}" style="display: none"
	title="Search for Organization">
	<table align="center">
		<tr valign="top" class="organizationSearchFields${uid}">
			<td>
				<table>
					<tr>
						<td align="right"><label for='organizationSearchName${uid}'>Name:</label></td>
						<td><input type="text" id="organizationSearchName${uid}"
							value="" title="Type an Organization name" /></td>
					</tr>
					<tr>
						<td align="right"><label
							for='organizationSearchAbbreviation${uid}'>Abbreviation:</label></td>
						<td><input type="text"
							id="organizationSearchAbbreviation${uid}" value=""
							title="Type an Organization abbreviation" /></td>
					</tr>
					
				</table>
			</td>
			<td>
				<table>
					<tr id="inactiveOptionRow${uid}" style="display: none">
						<td align="right"><input type="checkbox"
							id="organizationSearchIncludeInactive${uid}" value="true" /></td>
						<td>Include Inactive Organizations</td>
					</tr>
				</table>
			</td>
		</tr>
		<tr align="center">
			<td colspan="2"><a href="#" style="margin-left: 20px"
				class="buttonAnchor organizationSearchLink${uid} organizationSearchFields${uid}"
				>Search</a> <span style="padding-left: 10px; padding-right: 10px"><a style="display: none"
				class="buttonAnchor organizationAddLink${uid}" tabIndex="1">Create 
					Organization</a></td>
		</tr>
	</table>
	<div id="organizationSearchNoResults${uid}" style="display: none">
		<table align="center" cellpadding="10">
			<tr>
				<td>Sorry, no organizations were found that matched the
					specified criteria.</td>
			</tr>
		</table>
	</div>
	<div id="organizationSearchMaxResults${uid}"
		 class="redText" style="display: none; padding: 10px;" align="center">Your search
		exceeded the maximum results. Please enter additional search criteria.
	</div>
	<div id="organizationSearchResultsTable${uid}" style="display: none">
		<table id="organizationSearchResultsList${uid}" class="stripe"
			summary="List of Organizations">
			<thead>
				<tr id="organizationSearch${uid}FilterRow">
					<td width="30%" class="noborder"></td>
					<td width="50%" class="noborder"></td>
					<td width="10%" class="noborder"></td>
					<td width="10%" class="noborder"></td>
				</tr>
				<tr>
					<th width="30%" id="precinctHeaderCol${uid}" class="select-filter">Precinct</th>
					<th width="50%">Name</th>
					<th width="10%" class="select-filter">Type</th>
					<th width="10%">Abbreviation</th>
				</tr>
			</thead>
		</table>
	</div>
</div>
