<%@ include file="../../../shared/inc_header.jsp"%>

<script type="text/javascript">
	colDefs.push({
		"targets" : colDefs.length,
		"visible" : false,
		"orderable" : false,
		"data" : function(row, type, val, meta) {
			var getDisplay = function(a) {
				var sticker = a[0]
				var state = a[1]
				var license = a[2]
				var s = sticker
				if (state != null || license != null) {
					s += ' ['
					if (state != null)
						s += state
					if (state != null && license != null)
						s += ' - '
					if (license != null)
						s += '"' + license + '"'
					s += ']'
				}
				return s
			}

			if (type === 'export') {
				var s = ''
				for (var i = 0; i < row.combinedParkingStickers.length; i++) {
					if (i > 0)
						s += '\n'
					var a = row.combinedParkingStickers[i]
					s += getDisplay(a)
				}
				return s
			}

			var s = $('<ul></ul>')
			for (var i = 0; i < row.combinedParkingStickers.length; i++) {
				var a = row.combinedParkingStickers[i]
				s.append($('<li></li>').text(getDisplay(a)))
			}
			return s.outerHTML()
		}
	})
</script>