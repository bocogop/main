<%@ include file="../../../shared/inc_header.jsp"%>

<td class="noborder" title="Filter by Precinct"><select
	id="filterPrecinct" class="allParamInputs columnFilter"
	colIndex="${COL_INDEX_PRECINCT}">
		<option value="">(all)</option>
		<c:forEach items="${allPrecincts}" var="p">
		<option value="<c:out value="${p.id}" />"><c:out value="${p.name}" /></option>
		</c:forEach>
</select></td>