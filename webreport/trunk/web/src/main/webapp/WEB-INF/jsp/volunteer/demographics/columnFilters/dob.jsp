<%@ include file="../../../shared/inc_header.jsp"%>

<td class="noborder" title="Filter by Birth Month"><select
	id="filterBirthMonth" class="allParamInputs columnFilter"
	colIndex="${COL_INDEX_DOB}">
		<option value="">(all)</option>
		<c:forEach items="${allMonths}" var="m">
			<option value="${m.value}"><c:out value="${m.key}" />
				<c:out value="(${m.value})" />
			</option>
		</c:forEach>
</select></td>