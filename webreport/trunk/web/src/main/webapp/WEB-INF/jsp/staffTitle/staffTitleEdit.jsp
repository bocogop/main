<%@ include file="../shared/inc_header.jsp"%>

<c:if test="${FORM_READ_ONLY}">
	<%@ include file="../shared/inc_modifyToView.jsp"%>
</c:if>

<%@ include file="editStaffTitlePopup.jsp"%>

<script type="text/javascript">
	var isReadOnly = ${FORM_READ_ONLY}

	<sec:authorize
	access="hasAnyAuthority('${PERMISSION_STAFF_TITLE_CREATE}')"> 
	</sec:authorize>

	$(function() {
		buildStaffTitleTable();
		refreshStaffTitleTable()
	})
	
	var staffTitleMap = new Array();
	
	function buildStaffTitleTable() {
		var theDataTable = $('#staffTitleList').DataTable({
			buttons: ['excel', {
				extend : 'pdfHtml5',
				orientation : 'landscape'
			}, 'print'],
			"columns" : [ {
		        	"render" : function(row, type, val, meta) {
		        		return '<a href="javascript:showStaffTitlePopup(' + val.id + ')">'+ 
		    			escapeHTML(val.name) + '</a>'
					}
				}, 
				{
					"render" : function(row, type, val, meta) {
						return val.description
					}
				}, {
					"render" : function(row, type, val, meta) {
							return val.chief? 'Yes' : 'No'
					}
				},  {
					"render" : function(row, type, val, meta) {
						return val.chiefSupervisor? 'Yes' : 'No'
					}
				}, {
					"render" : function(row, type, val, meta) {
						return val.inactive ? 'Inactive' : 'Active'
					}
				}
				<c:if test="${not FORM_READ_ONLY}">
				, {
					"render" : function(row, type, val, meta) {
						var actions = '<div style="margin:0 auto; text-align:left"><nobr>'

						/*actions += '<a href="javascript:showStaffTitlePopup('
							+ val.id + ')"><img alt="Update" src="'+ imgHomePath
							+ '/edit-small.gif" border="0" hspace="5" align="center"/></a>'*/
							
						actions += '<a href="javascript:deleteStaffTitle('
								+ val.id + ')"><img alt="Delete StaffTitle" src="'+ imgHomePath
								+ '/permanently_delete_18x18.png" border="0" hspace="5" align="center"/></a>'
						
						actions += '</nobr></div>'
						return actions
					}
				}
				</c:if>
			],
	    	"dom": '<"top"fBi>rt<"bottom"pl><"clear">',
	    	"lengthMenu" : [ [ 20, 50, -1 ],
	    	 				[ 20, 50, "All" ] ],
			"order": [],
	    	"pageLength": 20,
	    	"pagingType": "full_numbers",
	    	"stripeClasses" : [],
		})
		
	}
	
	function refreshStaffTitleTable() {
		staffTitleMap = new Object()
		
		$.ajax({
			url : ajaxHomePath + '/staffTitles',
			dataType : 'json',
			data : {
			},
			error : commonAjaxErrorHandler,
			success : function(response) {
				var staffTitles = response
				
				var rArray = new Array()
				
				for (var i = 0; i < staffTitles.length; i++) {
					var staffTitle = staffTitles[i]
					staffTitleMap[staffTitle.id] = staffTitle
					rArray[rArray.length] = staffTitle
				}

				var table = $('#staffTitleList').DataTable()
				table.clear()
				table.rows.add(rArray)
				rebuildTableFilters('staffTitleList')
				table.draw()
				
				$('#staffTitleList_filter4').val('Active')
				$('#staffTitleList_filter4').change()
		    }
		})
	}
	
	function deleteStaffTitle(staffTitleId) {
		var fullObj = staffTitleMap[staffTitleId]
		var msg = 'Are you sure you want to delete "' + fullObj.name + '"?'
	    confirmDialog(msg,
                function() {
  					$.ajax({
					url : ajaxHomePath + '/staffTitle/delete',
					dataType : 'json',
					data : {
						staffTitleId: staffTitleId
					},
					error : commonAjaxErrorHandler,
					success : refreshStaffTitleTable
				})
        })
	}
	
</script>

<style>
table#staffTitleList tr.serviceRow
{
	background-color: #dddddd;
}

table#staffTitleList {
	border-collapse: collapse;
}

table#staffTitleList td {
	margin: 3px;
}

.#staffTitleList {
	min-width: 400px;
}
</style>


<div id="staffTitleWrapper" class="clearCenter" style="max-width:75%">
 	<fieldset>
		<legend>Staff Titles</legend>
 	<div align="center" style="margin-bottom:15px">
 		<table>
			<tr>
				<td><a class="buttonAnchor"
					href="javascript:showStaffTitlePopup()" style="margin-right:60px">Add Staff Title</a></td>
				</tr>
		</table>
	</div>
	<table class="formatTable" id="staffTitleList" border="1"
		summary="List of Staff Titles">
		<thead>
			<tr>
				<td class="noborder" width="25%"></td>
				<td class="noborder" width="50%"></td>
				<td class="noborder" width="7%"  title="Filter by Chief"></td>
				<td class="noborder" width="7%" title="Filter by Chief Supervisor"></td>
				<td class="noborder" width="7%" title="Filter by Status"></td>
			 	<c:if test="${not FORM_READ_ONLY}">
					<td class="noborder" width="7%" ></td>
				</c:if>
			</tr>
			<tr>
				<th>Name</th>
				<th>Description</th>
				<th class="select-filter">Is Chief</th>
				<th class="select-filter">Is Chief Supervisor</th>
				<th class="select-filter">Status</th>
				<c:if test="${not FORM_READ_ONLY}">
					<th>Action</th>
				</c:if>
			</tr>
		</thead>
		<tbody>
		</tbody>
	</table>
			</fieldset>
	
</div>
