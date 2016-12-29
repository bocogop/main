<%@ include file="../shared/inc_header.jsp"%>

<%-- Example to hide search filter, if they want that elsewhere - CPB
	<jsp:param name="dom" value='<"top"i>rt<"bottom"pl><"clear">' />
	--%>
<jsp:include page="/WEB-INF/jsp/shared/inc_jqueryTable.jsp">
	<jsp:param name="dataTableId" value="organizationSearchResultsList" />
</jsp:include>

<script type="text/javascript">
	$(function() {

		$('select', '#statusFilter').val('Active')
		$('select', '#statusFilter').change()
	})
</script>

<div align="center">
	<sec:authorize
		access="hasAnyAuthority('${PERMISSION_ORG_CODE_NATIONAL_CREATE}, 
										 ${PERMISSION_ORG_CODE_LOCAL_CREATE}')">
		<a class="buttonAnchor" id="createButton"
			href="${home}/organizationCreate.htm">Create Organization</a>
	</sec:authorize>
</div>

<c:if test="${not empty command.organizations}">
	<table summary="Format table" align="center" width="98%">
		<tr>
			<td align="center">
				<table id="organizationSearchResultsList" class="stripe"
					summary="List of Organizations">
					<thead>
						<tr id="organizationSearchFilterRow">
							<td class="noborder" title="Filter by Facility">Filters:</td>
							<td class="noborder"></td>
							<td class="noborder"></td>
							<td class="noborder" title="Filter by Type"></td>
							<td class="noborder" title="Filter by NAC"></td>
							<td class="noborder" title="Filter by AJR Month"></td>
							<td class="noborder" title="Filter by NAC Executive Member"></td>
							<td class="noborder" title="Filter by NAC Membership Status"></td>
							<td class="noborder" id="statusFilter" title="Filter by Status"></td>
						</tr>
						<tr>
							<th class="select-filter">Facility</th>
							<th>Name</th>
							<th>Abbreviation</th>
							<th class="select-filter">Type</th>
							<th class="select-filter">NAC Member</th>
							<th class="select-filter">AJR Month</th>
							<th class="select-filter">NAC Executive Member</th>
							<th class="select-filter">NAC Status</th>
							<th class="select-filter">Status</th>
						</tr>
					</thead>

					<c:forEach var="org" items="${command.organizations}">
						<c:set var="isOrgNotBranch" value="${org.scale == 'Organization'}" />
						<tr>
							<td><c:if test="${not empty org.facility}">
									<c:out value="${org.facility.displayName}" />
								</c:if> <c:if test="${org.scope == 'NATIONAL'}">
									<c:out value="National" />
								</c:if></td>
							<td><a class="appLink"
								href="${home}/organizationEdit.htm?id=${org.id}"><c:out
										value="${org.displayName}" /></a></td>
							<td><c:out value="${org.abbreviation}" /></td>
							<td><c:if test="${isOrgNotBranch}">
									<c:out value="${org.type.name}" />
								</c:if></td>
							<td><c:if test="${isOrgNotBranch && org.onNationalAdvisoryCommittee}">Yes</c:if>
								<c:if test="${isOrgNotBranch && !org.onNationalAdvisoryCommittee}">No</c:if></td>
							<td><c:if test="${isOrgNotBranch}"><c:if test="${not empty org.annualJointReviewMonth}"><fmt:formatNumber minIntegerDigits="2" value="${org.annualJointReviewMonth}" /> - <c:out value="${org.AJRMonthName}" /></c:if></c:if></td>
							<td><c:if test="${isOrgNotBranch && org.nacExecutiveMember}">Yes</c:if> <c:if
									test="${isOrgNotBranch && !org.nacExecutiveMember}">No</c:if></td>
							<td><c:if test="${isOrgNotBranch}"><c:out
									value="${org.nacMembershipStatus.membershipStatus}" /></c:if></td>
							<td><c:if test="${org.active}">
									<c:out value="Active" />
								</c:if> <c:if test="${!org.active}">
									<c:out value="Inactive" />
								</c:if></td>
						</tr>
					</c:forEach>
				</table>
			</td>
		</tr>
	</table>
</c:if>
<c:if test="${empty command.organizations}">
	<table align="center" cellpadding="10">
		<tr>
			<td>Sorry, no organizations were found that matched the
				specified criteria.</td>
		</tr>
	</table>
</c:if>

