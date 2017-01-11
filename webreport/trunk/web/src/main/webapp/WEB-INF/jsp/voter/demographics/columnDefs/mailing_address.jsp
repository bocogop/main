<%@ include file="../../../shared/inc_header.jsp"%>

<script type="text/javascript">
	colDefs.push({
		"targets" : colDefs.length,
		"orderable" : false,
		"data" : function(row, type, val, meta) {
			if (type === 'export') {
				return row.fullMailingAddressMultilineDisplay || ''
			}

			var addressHtml = ""
			if (row.fullMailingAddressMultilineDisplay)
				addressHtml = convertLinefeedToBR(row.fullMailingAddressMultilineDisplay)
			return addressHtml
		}
	})
</script>