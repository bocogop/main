<%@ include file="../../../shared/inc_header.jsp"%>

<script type="text/javascript">
	colDefs.push({
		"targets" : colDefs.length,
		"orderable" : false,
		"data" : function(row, type, val, meta) {
			if (type === 'export') {
				return row.fullBallotAddressMultilineDisplay || ''
			}

			var addressHtml = ""
			if (row.fullBallotAddressMultilineDisplay)
				addressHtml = convertLinefeedToBR(row.fullBallotAddressMultilineDisplay)
			return addressHtml
		}
	})
</script>