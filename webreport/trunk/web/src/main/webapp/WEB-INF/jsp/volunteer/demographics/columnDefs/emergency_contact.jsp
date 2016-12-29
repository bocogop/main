<%@ include file="../../../shared/inc_header.jsp"%>

<script type="text/javascript">
	colDefs.push({
		"targets" : colDefs.length,
		"visible" : false,
		"orderable" : false,
		"data" : function(row, type, val, meta) {
			var s = ''
			if (row.emergencyContactName)
				s += 'Name: ' + row.emergencyContactName + '<br>'
			if (row.emergencyContactRelationship)
				s += 'Relationship: ' + row.emergencyContactRelationship
						+ '<br>'
			if (row.emergencyContactPhone)
				s += 'Phone: ' + row.emergencyContactPhone + '<br>'
			if (row.emergencyContactPhoneAlt)
				s += 'Phone Alt: ' + row.emergencyContactPhoneAlt + '<br>'
			if (type === 'export') {
				s = s.replace(/<br\s*[\/]?>/gi, "\n")
			}
			return s
		}
	})
</script>