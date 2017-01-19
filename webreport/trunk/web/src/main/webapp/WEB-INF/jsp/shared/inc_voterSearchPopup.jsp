<%@ include file="../shared/inc_header.jsp"%>

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
	<table align="center">
		<tr valign="top" class="voterSearchFields${uid}">
			<td>
				<table>
					<tr valign="top">
						<td align="right" nowrap><label for='voterSearchLastName${uid}'>Last Name:</label></td>
						<td><input size="15" type="text" id="voterSearchLastName${uid}"
							value="" title="Type the last name" /></td>
						<td rowspan="2" width="10">&nbsp;</td>
						<td align="right"><label for='voterSearchCode${uid}'>Voter ID:</label></td>
						<td><input type="text" id="voterSearchCode${uid}"
							value="" title="Type an identifying code" size="10" /></td>
						<td rowspan="2" width="10">&nbsp;</td>
						<td><label for='voterSearchEmail${uid}'>Email:</label></td>
					</tr>
					<tr>
						<td align="right" nowrap><label for='voterSearchFirstName${uid}'>First Name:</label></td>
						<td><input size="15" type="text" id="voterSearchFirstName${uid}"
							value="" title="Type the first name" /></td>
						<td align="right"><label for='voterSearchYOB${uid}'>Year
								of Birth:</label></td>
						<td><input size="10" maxlength="4" id="voterSearchYOB${uid}" /></td>
						<td><input type="text" size="32" id="voterSearchEmail${uid}"
							value="" title="Type all or part of an email" /></td>
					</tr>
				</table>
			</td>
		</tr>
		<tr align="center">
			<td colspan="3"><a style="margin-left: 20px"
				class="buttonAnchor voterSearchLink${uid} voterSearchFields${uid}"
				href="#">Search</a> </td>
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
					<td class="noborder"></td>
					<td class="noborder"></td>
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
					<th width="10%">Voter ID</th>
					<th width="10%" class="select-filter">Precinct</th>
					<th width="10%">Birth Year</th>
					<th width="5%" class="select-filter">Gender</th>
					<th width="10%" class="select-filter" nowrap>Status</th>
					<th width="20%">Contact Info</th>
				</tr>
			</thead>
		</table>
	</div>
</div>
