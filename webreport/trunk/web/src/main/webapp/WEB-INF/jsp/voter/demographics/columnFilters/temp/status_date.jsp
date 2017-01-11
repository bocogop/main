<%@ include file="../../../shared/inc_header.jsp"%>

<td class="noborder" nowrap><input id="filterStatusDateMonth"
	type="text" size="2" placeholder="mm" class="allParamInputs columnFilter"
	colIndex="${COL_INDEX_STATUS_DATE}" colValueFn="getStatusDateFilterVal" />
	<input id="filterStatusDateYear" type="text" size="4" placeholder="yyyy"
	class="allParamInputs columnFilter" colIndex="${COL_INDEX_STATUS_DATE}"
	colValueFn="getStatusDateFilterVal" /></td>