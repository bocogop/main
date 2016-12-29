<%@ include file="../../../shared/inc_header.jsp"%>

<script type="text/javascript">
	colDefs.push({
		"targets" : colDefs.length,
		"orderable" : false,
		"data" : function(row, type, val, meta) {
			if (type === 'export') {
				return row.addressMultilineDisplay || ''
			}

			var addressHtml = ""
			if (row.addressMultilineDisplay)
				addressHtml = convertLinefeedToBR(row.addressMultilineDisplay)
			return addressHtml
		}
	})
</script>