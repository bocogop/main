<%@ include file="../../../shared/inc_header.jsp"%>

<td class="noborder" nowrap><input id="filterLastVolunteeredDateMonth"
	type="text" size="2" placeholder="mm" class="allParamInputs columnFilter"
	colIndex="${COL_INDEX_LAST_VOLUNTEERED_DATE}" colValueFn="getLastVolunteeredDateFilterVal" />
	<input id="filterLastVolunteeredDateYear" type="text" size="4" placeholder="yyyy"
	class="allParamInputs columnFilter" colIndex="${COL_INDEX_LAST_VOLUNTEERED_DATE}"
	colValueFn="getLastVolunteeredDateFilterVal" /></td>
<%--
<td class="noborder" title="Filter by Last Volunteered Date"><select
	id="filterLastVolDate" class="columnFilter"
	colIndex="${COL_INDEX_LAST_VOLUNTEERED_DATE}">
		<option value="">(all)</option>
		<option value="never">Never</option>
</select></td> --%>