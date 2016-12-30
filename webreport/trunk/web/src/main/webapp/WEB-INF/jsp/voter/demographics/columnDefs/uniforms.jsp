<%@ include file="../../../shared/inc_header.jsp"%>

<script type="text/javascript">
	colDefs.push({
		"targets" : colDefs.length,
		"visible" : false,
		"orderable" : false,
		"data" : function(row, type, val, meta) {
			var getDisplay = function(a) {
				var size = a[0]
				var quantity = a[1]
				return size + " (" + quantity + ")"
			}

			if (type === 'export') {
				var s = ''
				for (var i = 0; i < row.combinedUniforms.length; i++) {
					if (i > 0)
						s += '\n'
					var a = row.combinedUniforms[i]
					s += getDisplay(a)
				}
				return s
			}

			var s = $('<ul></ul>')
			for (var i = 0; i < row.combinedUniforms.length; i++) {
				var a = row.combinedUniforms[i]
				s.append($('<li></li>').text(getDisplay(a)))
			}
			return s.outerHTML()
		}
	})
</script>