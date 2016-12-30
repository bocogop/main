<%@ include file="../shared/inc_header.jsp"%>

<%-- Example to hide search filter, if they want that elsewhere - CPB
	<jsp:param name="dom" value='<"top"i>rt<"bottom"pl><"clear">' />
	--%>
<jsp:include page="/WEB-INF/jsp/shared/inc_jqueryTable.jsp">
	<jsp:param name="dataTableId" value="excludedEntityList" />
</jsp:include>

<div style="float:right;margin-right:15px">
	<a href="${home}/excludedEntityViewAll.htm">View all LEIE data</a> <a href="${home}/excludedEntityList.htm"><img
		alt="View All LEIE Data" src="${imgHome}/right.gif" border="0" align="absmiddle" /></a>
</div>

<div class="clearCenter" style="border: 1px gray;text-align:center">
	LEIE source data last updated: <b><wr:localDate
			date="${lastUpdatedDate}" pattern="${DATE_ONLY}" /></b><br>
	LEIE process last executed: <b><wr:localDate
			date="${lastExecutedDate}" pattern="${DATE_ONLY}" /></b>
</div>
<p />

<c:if test="${not empty excludedEntities}">
	<table summary="Format table" align="center" width="97%">
		<tr>
			<td align="center">
				<table id="excludedEntityList" class="stripe"
					summary="List of Excluded Entities" width="100%">
					<thead>
						<tr id="excludedEntitySearchFilterRow">
							<td class="noborder">Filters:</td>
							<td class="noborder" title="Filter by Precinct"></td>
							<td class="noborder"></td>
							<td class="noborder" title="Filter by SSA"></td>
							<td class="noborder" title="Filter by 42USC code"></td>
							<td class="noborder"></td>
							<td class="noborder"></td>
							<td class="noborder"></td>
							<td class="noborder"></td>
							<td class="noborder"></td>
							<td class="noborder"></td>
						</tr>
						<tr>
							<th>Voter</th>
							<th class="select-filter">Precinct</th>
							<th>LEIE Name</th>
							<th class="select-filter">SSA</th>
							<th class="select-filter">42 USC Code</th>
							<th>Exclusion Date</th>
							<th>Description</th>
							<th>WR Address</th>
							<th>LEIE Address</th>
							<th>WR Date of Birth</th>
							<th>LEIE Date of Birth</th>
						</tr>
					</thead>

					<c:forEach var="eeMatch" items="${excludedEntities}">
						<c:set var="v" value="${eeMatch.voter}" />
						<c:set var="ee" value="${eeMatch.excludedEntity}" />

						<tr>
							<td><a class="appLink" href="${home}/voterEdit.htm?id=${v.id}"><c:out value="${v.displayName}" /></a></td>
							<td><c:out value="${v.primaryPrecinct.displayName}" /></td>
							<td><c:out value="${ee.displayName}" /></td>
							<td><c:out value="${ee.exclusionType.ssa}" /></td>
							<td><c:out value="${ee.exclusionType.code42Usc}" /></td>
							<td><wr:localDate date="${ee.exclusionDate}"
									pattern="${DATE_ONLY}" /></td>
							<td><c:out value="${ee.exclusionType.description}" /></td>
							<td><pre>
									<c:out value="${v.addressMultilineDisplay}" />
								</pre></td>
							<td><pre>
									<c:out value="${ee.addressMultilineDisplay}" />
								</pre></td>
							<td><wr:localDate date="${v.dateOfBirth}"
									pattern="${DATE_ONLY}" /></td>
							<td><wr:localDate date="${ee.dob}" pattern="${DATE_ONLY}" /></td>
						</tr>
					</c:forEach>
				</table>
			</td>
		</tr>
	</table>
</c:if>
<c:if test="${empty excludedEntities}">
	<c:set var="canViewAll" value="false" />
	<sec:authorize
		access="hasAnyAuthority('${PERMISSION_EXCLUDED_ENTITY_VIEW_ALL}')">
		<c:set var="canViewAll" value="true" />
	</sec:authorize>

	<table align="center" cellpadding="10">
		<tr>
			<td>No excluded entities were found <c:if test="${canViewAll}">
					at any precinct.
				 </c:if> <c:if test="${not canViewAll}">
				for precinct "<c:out value="${precinctContextName}" />".
				</c:if>
			</td>
		</tr>
	</table>
</c:if>