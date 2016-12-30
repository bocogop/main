<%@ include file="../../../shared/inc_header.jsp"%>

<script type="text/javascript">
	colDefs.push({
		"targets" : colDefs.length,
		"visible" : false,
		"data" : function(row, type, val, meta) {
			return row.youth ? 'Youth' : 'Adult'
		}
	})
</script>