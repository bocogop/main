<%@ include file="../../../shared/inc_header.jsp"%>

<td class="noborder" title="Filter by State"><select
	id="filterState" class="allParamInputs columnFilter" colIndex="${COL_INDEX_STATE}" style="width:60px">
		<option value="">(all)</option>
		<c:forEach items="${allStates}" var="s">
			<option value="${s.id}"><c:out value="${s.name}" /></option>
		</c:forEach>
</select></td>