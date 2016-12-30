<%@ include file="../../../shared/inc_header.jsp"%>

<script type="text/javascript">
	colDefs.push({
		"targets" : colDefs.length,
		"orderable" : false,
		"data" : function(row, type, val, meta) {
			var s = ''
			if (row.phone)
				s += 'Phone: ' + row.phone + '<br>'
			if (row.phoneAlt)
				s += 'Alt Phone: ' + row.phoneAlt + '<br>'
			if (row.phoneAlt2)
				s += 'Alt 2 Phone: ' + row.phoneAlt2 + '<br>'
			if (row.email)
				s += 'Email: ' + row.email + '<br>'
			if (type === 'export') {
				s = s.replace(/<br\s*[\/]?>/gi, "\n")
			}
			return s
		}
	})
</script>