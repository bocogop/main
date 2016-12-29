<%@ include file="../../../shared/inc_header.jsp"%>

<script type="text/javascript">
	colDefs.push({
		"targets" : colDefs.length,
		"data" : function(row, type, val, meta) {
			return defaultStr(row.dateOfBirth)
		}
	})
</script>