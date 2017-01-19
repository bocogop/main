<%@ include file="../shared/inc_header.jsp"%>

<c:if test="${FORM_READ_ONLY}">
	<%@ include file="../shared/inc_modifyToView.jsp"%>
</c:if>

<script>
	var isReadOnly = ${FORM_READ_ONLY}
	var users = []
	
	$(function() {
		buildUserTable()
		refreshUserTable()
	})
	
	function buildUserTable() {
		var theDataTable = $('#userList').DataTable({
			buttons: ['excel', {
				extend : 'pdfHtml5',
				orientation : 'landscape'
			}, 'print'],
			"columns" : [ {
					"render" : function(row, type, val, meta) {
						if (type === 'display') {
							return '<nobr><a class="appLink" href="' + homePath + '/userAdmin.htm?appUserId=' + val.id + '">' + escapeHTML(val.displayName) + '</a></nobr>'
						}
						return val.displayName
					} 
				}, {
					"render" : function(row, type, val, meta) {
						return val.username
					}
				}, {
					"render" : function(row, type, val, meta) {
						return val.phone
					}
				}, {
					"render" : function(row, type, val, meta) {
						if (type === 'display') {
							if (val.email && val.email != '') {
								return escapeHTML(val.email) + '<a href="mailto:'
									+ escapeHTML(val.email)
									+ '"><img alt="Click to email '
									+ escapeHTML(val.email) + '"' + 'src="' + imgHomePath
									+ '/envelope.jpg" height="14"'
									+ ' width="18" border="0" align="absmiddle"'
									+ ' style="padding-left: 4px; padding-right: 4px" /></a>'
							}
						}
						return val.email
					}
				}, {
					"render" : function(row, type, val, meta) {
						return val.enabled ? 'Yes' : 'No'
					}
				}, {
					"render" : function(row, type, val, meta) {
						var s = '<ul>'
						for (var i = 0; i < val.basicRoles.length; i++)
							s += '<li>' + val.basicRoles[i].name + '</li>'
						s += '</ul>'
						return s
					}
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
	
	function refreshUserTable() {
		$.ajax({
			url : ajaxHomePath + '/appUser/list',
			dataType : 'json',
			data : {
			},
			error : commonAjaxErrorHandler,
			success : function(response) {
				users = response

				var table = $('#userList').DataTable()
				table.clear()
				table.rows.add(response)
				table.draw()				
		    }
		})
	}
</script>

<style>
#userList {
	border-collapse: collapse;
	min-width: 400px;
}

#userList td {
	margin: 3px;
}
</style>


<div id="userWrapper" class="clearCenter"
	style="max-width: 75%">
	<fieldset>
		<legend>Users</legend>

		<table class="formatTable" id="userList" border="1"
			summary="User List">
			<thead>
				<tr>
					<th>Name</th>
					<th>Username</th>
					<th>Phone</th>
					<th>Email</th>
					<th>Enabled</th>
					<th>Roles</th>
				</tr>
			</thead>
			<tbody>
			</tbody>
		</table>
	</fieldset>
</div>
