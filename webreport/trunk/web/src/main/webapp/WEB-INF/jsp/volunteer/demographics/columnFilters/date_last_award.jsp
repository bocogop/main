<%@ include file="../../../shared/inc_header.jsp"%>

<td class="noborder" nowrap><input id="filterDateLastAwardMonth"
	type="text" size="2" placeholder="mm" class="allParamInputs columnFilter"
	colIndex="${COL_INDEX_DATE_LAST_AWARD}"
	colValueFn="getDateLastAwardFilterVal" /> <input
	id="filterDateLastAwardYear" type="text" size="4" placeholder="yyyy"
	class="allParamInputs columnFilter" colIndex="${COL_INDEX_DATE_LAST_AWARD}"
	colValueFn="getDateLastAwardFilterVal" /></td>