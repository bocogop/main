<%@ include file="../../../shared/inc_header.jsp"%>

<td class="noborder" title="Filter by Name" nowrap>
	<input id="filterName" type="text" size="20" class="allParamInputs columnFilter" colIndex="${COL_INDEX_NAME}" placeholder="Last [, First [Middle]]" />
	<img src="${imgHome}/question.png"
		onclick="javascript:displayAttentionDialog('Enter the last name, and optionally a first and middle name.&lt;p&gt; Values entered match on the beginning of each name; thus, \'Smi\' would match a voter named Smith, but not \'mit\'.')"
		title="Values entered match on the beginning of each word; thus, 'Smi' would match a voter named Smith, but not 'mit'." />
</td>