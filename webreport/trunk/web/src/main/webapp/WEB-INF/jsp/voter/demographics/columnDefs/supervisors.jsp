<%@ include file="../../../shared/inc_header.jsp"%>

<script type="text/javascript">
	colDefs.push({
		"targets" : colDefs.length,
		"visible" : false,
		"orderable" : false,
		"data" : function(row, type, val, meta) {
			var addedOne = false
			
			if (type === 'export') {
				var s = ''
				for (var i = 0; i < row.supervisors.length; i++) {
					if (i > 0) s += '\n'
					var a = row.supervisors[i]
					var tokens = a.split("|")
					if (tokens[1] == '') continue
					
					s += escapeHTML(tokens[1])
					if (row.supervisors.length > 1) s += ' (' + tokens[0] + ')'
					if (tokens[3] != '')
						s += ' - ' + tokens[3]
					if (tokens[2] != '')
						s += ' [' + tokens[2] + ']'
					addedOne = true
				}
				return addedOne ? s : ''
			}
			
			var s = $('<ul></ul>')
			for (var i = 0; i < row.supervisors.length; i++) {
				var a = row.supervisors[i]
				var tokens = a.split("|")
				if (tokens[1] == '') continue
				
				var name = tokens[2] == '' ? tokens[1] : $('<a class="appLink"></a>').attr('href', 'mailto:' + escapeHTML(tokens[2])).text(tokens[1]).outerHTML()
				var li = $('<li></li>')
				li.append(name)
				if (row.supervisors.length > 1) li.append(' (' + escapeHTML(tokens[0]) + ')')
				if (tokens[3] != '')
					li.append(' - ', '<nobr>' + escapeHTML(tokens[3]) + '</nobr>')
				s.append(li)
				addedOne = true
			}
			
			return addedOne ? s.outerHTML() : ''
		}
	})
</script>