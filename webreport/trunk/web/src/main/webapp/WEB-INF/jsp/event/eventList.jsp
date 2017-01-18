<%@ include file="../shared/inc_header.jsp"%>

<c:if test="${FORM_READ_ONLY}">
	<%@ include file="../shared/inc_modifyToView.jsp"%>
</c:if>

<script>
	var isReadOnly = ${FORM_READ_ONLY}
	var events = []
	
	var canEditEvents = false
	<sec:authorize
	access="hasAnyAuthority('${PERMISSION_EVENT_EDIT}')">
		canEditEvents = true
	</sec:authorize>

	$(function() {
		buildEventTable()
		refreshEventTable()
	})
	
	function buildEventTable() {
		var theDataTable = $('#eventList').DataTable({
			buttons: ['excel', {
				extend : 'pdfHtml5',
				orientation : 'landscape'
			}, 'print'],
			"columns" : [ {
					"render" : function(row, type, val, meta) {
						return val.date
					} 
				}, {
					"render" : function(row, type, val, meta) {
						if (type === 'display') {
							return '<a class="appLink" href="' + homePath + '/eventEdit.htm?id=' + val.id + '">' + escapeHTML(val.name) + '</a>'
						}
						return val.name
					}
				}, {
					"render" : function(row, type, val, meta) {
						return ''
					}
				}, {
					"render" : function(row, type, val, meta) {
						var s = '<nobr>'
						s += '<a href="javascript:deleteEvent(' + val.id + ')"><img src="' + imgHomePath + '/delete.gif" border="0" /></a>'
						s += '</nobr>'
						return s
					},
					"className" : "dt-center"
				}
			],
	    	"dom": '<"top"fBi>rt<"bottom"pl><"clear">',
	    	"lengthMenu" : [ [ 20, 50, -1 ],
	    	 				[ 20, 50, "All" ] ],
			"order": [],
	    	"pageLength": 20,
	    	"pagingType": "full_numbers",
	    	"stripeClasses" : []
		})
	}
	
	function refreshEventTable() {
		$.ajax({
			url : ajaxHomePath + '/event',
			dataType : 'json',
			data : {
			},
			error : commonAjaxErrorHandler,
			success : function(response) {
				events = response

				var table = $('#eventList').DataTable()
				table.clear()
				table.rows.add(response)
				table.draw()				
		    }
		})
	}
	
	function deleteEvent(eventId) {
		confirmDialog('Are you sure you want to delete this event?<p>This action is irreversible.', function() {
			$.ajax({
				url : ajaxHomePath + '/event/delete',
				dataType : 'json',
				data : {
					id : eventId
				},
				error : commonAjaxErrorHandler,
				success : refreshEventTable
			})
		})
	}
</script>

<style>
#eventList tr.serviceRow {
	background-color: #dddddd;
}

#eventList {
	border-collapse: collapse;
	min-width: 400px;
}

#eventList td {
	margin: 3px;
}
</style>


<div id="eventWrapper" class="clearCenter"
	style="max-width: 75%">
	<fieldset>
		<legend>Events</legend>

		<table class="formatTable" id="eventList" border="1"
			summary="General Ledger">
			<thead>
				<tr>
					<th width="60">Date</th>
					<th>Event</th>
					<th>Description</th>
					<th width="20">Action</th>
				</tr>
			</thead>
			<tbody>
			</tbody>
		</table>
	</fieldset>
</div>
