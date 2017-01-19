function buildParticipantTable() {
	var theDataTable = $('#participantList')
			.DataTable(
					{
						"columnDefs" : [
								{
									"targets" : 0,
									"data" : function(row, type, val, meta) {
										var voterNameEscaped = escapeHTML(row.voter.displayName)
										if (type === 'display') {
											return '<a class="appLink" href="'
													+ homePath
													+ '/voterEdit.htm?id=' + row.voter.id + '">'
													+ voterNameEscaped
													+ '</a>'
										} else {
											return voterNameEscaped
										}
									}
								},
								{
									"targets" : 1,
									"data" : function(row, type, val, meta) {
										var theText = escapeHTML(row.voter.voterId)
										return theText
									}
								},
								{
									"targets" : 2,
									"data" : function(row, type, val, meta) {
										var theText = escapeHTML(row.voter.precinct ? row.voter.precinct.name
												: '(Unknown)')
										return theText
									}
								},
								{
									"targets" : 3,
									"data" : function(row, type, val, meta) {
										if (type === 'filter') {
											return row.voter.statusActive ? 'Active'
													: 'Inactive'
										} else {
											return (row.voter.statusActive ? 'Active'
													: 'Inactive')
													+ (row.voter.statusReason ? ' ('
															+ row.voter.statusReason
															+ ')'
															: '')
										}
									}
								},
								{
									"targets" : 4,
									"data" : function(row, type, val, meta) {
										var contactInfoHtml = getVoterDashedBoxEl(
												row.voter).outerHTML()
										return contactInfoHtml
									}
								},
								{
									"targets" : 5,
									"data" : function(row, type, val, meta) {
										var s = '<nobr>'
										s += '<a href="javascript:deleteParticipant('
												+ row.id
												+ ')"><img src="'
												+ imgHomePath
												+ '/delete.gif" border="0" /></a>'
										s += '</nobr>'
										return s
									},
									"className" : "dt-center"
								} ],
						"dom" : '<"top"fi>rt<"bottom"pl><"clear">',
						"pagingType" : "full_numbers",
						"pageLength" : 10,
						"lengthMenu" : [ [ 10, -1 ], [ 10, "All" ] ],
						"stateSave" : false
					})
}

var participations = []

function refreshParticipantTable() {
	$.ajax({
		url : ajaxHomePath + '/event/participation',
		dataType : 'json',
		data : {
			eventId : eventId
		},
		error : commonAjaxErrorHandler,
		success : function(response) {
			participations = response

			var table = $('#participantList').DataTable()
			table.clear()
			table.rows.add(response)
			table.draw()				
	    }
	})
}