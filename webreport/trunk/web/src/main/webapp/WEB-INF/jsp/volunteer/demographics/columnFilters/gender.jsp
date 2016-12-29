<%@ include file="../../../shared/inc_header.jsp"%>

<td class="noborder" title="Filter by Gender"><select
	id="filterGender" class="allParamInputs columnFilter" colIndex="${COL_INDEX_GENDER}">
		<option value="">(all)</option>
		<c:forEach items="${allGenders}" var="g">
			<option value="${g.id}"><c:out value="${g.name}" /></option>
		</c:forEach>
</select></td>