<%@ include file="../../../shared/inc_header.jsp"%>

<script type="text/javascript">
	colDefs.push({
		"targets" : colDefs.length,
		"data" : function(row, type, val, meta) {
			if (type === 'display') {
				return '<a href="' + homePath + '/volunteerEdit.htm?id='
						+ row.id + '" class="appLink">'
						+ escapeHTML(row.displayName) + '</a>'
			}
			return defaultStr(row.displayName)
		}
	})
</script>