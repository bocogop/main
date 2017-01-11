<%@ include file="../../../shared/inc_header.jsp"%>

<script type="text/javascript">
	colDefs.push({
		"targets" : colDefs.length,
		"orderable" : false,
		"data" : function(row, type, val, meta) {
			if (type === 'export') {
				var s = ''
				for (var i = 0; i < row.combinedAssignments.length; i++) {
					if (i > 0)
						s += '\n'
					var a = row.combinedAssignments[i]
					s += escapeHTML(a)
				}
				return s
			}

			var s = $('<ul></ul>')
			for (var i = 0; i < row.combinedAssignments.length; i++) {
				var a = row.combinedAssignments[i]
				s.append($('<li></li>').text(a))
			}
			return s.outerHTML()
		}
	})
</script>