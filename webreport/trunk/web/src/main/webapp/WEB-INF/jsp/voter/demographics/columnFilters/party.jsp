<%@ include file="../../../shared/inc_header.jsp"%>

<td class="noborder" title="Filter by Party"><select
	id="filterParty" class="allParamInputs columnFilter" colIndex="${COL_INDEX_PARTY}">
		<option value="">(all)</option>
		<c:forEach items="${allParties}" var="g">
			<option value="${g.id}"><c:out value="${g.name}" /></option>
		</c:forEach>
</select></td>