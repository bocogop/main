<%@ include file="../../../shared/inc_header.jsp"%>

<td class="noborder" nowrap><input id="filterLastVoteredDateMonth"
	type="text" size="2" placeholder="mm" class="allParamInputs columnFilter"
	colIndex="${COL_INDEX_LAST_VOTERED_DATE}" colValueFn="getLastVoteredDateFilterVal" />
	<input id="filterLastVoteredDateYear" type="text" size="4" placeholder="yyyy"
	class="allParamInputs columnFilter" colIndex="${COL_INDEX_LAST_VOTERED_DATE}"
	colValueFn="getLastVoteredDateFilterVal" /></td>
<%--
<td class="noborder" title="Filter by Last Votered Date"><select
	id="filterLastVolDate" class="columnFilter"
	colIndex="${COL_INDEX_LAST_VOTERED_DATE}">
		<option value="">(all)</option>
		<option value="never">Never</option>
</select></td> --%>