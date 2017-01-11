<%@ include file="../../../shared/inc_header.jsp"%>

<td class="noborder" nowrap><input id="filterEntryDateMonth"
	type="text" size="2" placeholder="mm" class="allParamInputs columnFilter"
	colIndex="${COL_INDEX_ENTRY_DATE}" colValueFn="getEntryDateFilterVal" />
	<input id="filterEntryDateYear" type="text" size="4" placeholder="yyyy"
	class="allParamInputs columnFilter" colIndex="${COL_INDEX_ENTRY_DATE}"
	colValueFn="getEntryDateFilterVal" /></td>