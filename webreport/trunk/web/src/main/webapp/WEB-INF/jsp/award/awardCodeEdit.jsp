<%@ include file="../shared/inc_header.jsp"%>

<c:if test="${FORM_READ_ONLY}">
	<%@ include file="../shared/inc_modifyToView.jsp"%>
</c:if>

<%@ include file="editAwardCodePopup.jsp"%>


<script type="text/javascript">
	var isReadOnly = ${FORM_READ_ONLY}

	<sec:authorize
	access="hasAnyAuthority('${PERMISSION_AWARD_CODE_CREATE}')"> 
	</sec:authorize>

	$(function() {
		buildAwardCodeTable();
		refreshAwardCodeTable()
	})
	
	var awardCodeMap = new Array();
	
	function buildAwardCodeTable() {
		var theDataTable = $('#awardCodeList').DataTable({
			buttons: ['excel', {
				extend : 'pdfHtml5',
				orientation : 'landscape'
			}, 'print'],
				
			"columns" : [
				{
		        	"render" : function(row, type, val, meta) {
		    				return '<a href="javascript:showAwardCodePopup(' + val.id + ')">'+ 
		    				val.name + '</a>'
					}
				}, 
				{
					"render" : function(row, type, val, meta) {
						return val.code
					}
				}, 
				{
					"render" : function(row, type, val, meta) {
						return val.hoursRequired.toString()
					}
				}, {
					"render" : function(row, type, val, meta) {
						return val.awardHours.toString()
					}
				},  {
					"render" : function(row, type, val, meta) {
						return val.type
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

						actions += '<a href="javascript:deleteAwardCode('
								+ val.id + ')"><img alt="Delete Award code" src="'+ imgHomePath
								+ '/permanently_delete_18x18.png" border="0" hspace="5" align="center"/></a>'
						
						actions += '</nobr></div>'
						return actions
					}
				}
				</c:if>
			],
	    	"dom": '<"top"fBi>rt<"bottom"pl><"clear">',
	    	"lengthMenu" : [ [ 10, 50, -1 ],
	    	 				[ 10, 50, "All" ] ],
			"order": [],
	    	"pageLength": 10,
	    	"pagingType": "full_numbers",
	    	"stripeClasses" : [],
		})
		
	}
	
	function refreshAwardCodeTable() {
		awardCodeMap = new Object()
		
		$.ajax({
			url : ajaxHomePath + '/awardCodes',
			dataType : 'json',
			data : {
			},
			error : commonAjaxErrorHandler,
			success : function(response) {
				var awardCodes = response
				
				var rArray = new Array()
				
				for (var i = 0; i < awardCodes.length; i++) {
					var awardCode = awardCodes[i]
					awardCodeMap[awardCode.id] = awardCode
					rArray[rArray.length] = awardCode
				}

				var table = $('#awardCodeList').DataTable()
				table.clear()
				table.rows.add(rArray)
				rebuildTableFilters('awardCodeList')
				table.draw()
				
				$('#awardCodeList_filter5').val('Active')
				$('#awardCodeList_filter5').change()
		    }
		})
	}
	
	function deleteAwardCode(awardCodeId) {
		var fullObj = awardCodeMap[awardCodeId]
		var msg = 'Are you sure you want to delete "' + fullObj.name + '"?'
	    confirmDialog(msg,
                function() {
  					$.ajax({
					url : ajaxHomePath + '/awardCode/delete',
					dataType : 'json',
					data : {
						awardCodeId: awardCodeId
					},
					error : commonAjaxErrorHandler,
					success : refreshAwardCodeTable
				})
        })
	}
	
</script>

<style>
table#awardCodeList tr.serviceRow {
	background-color: #dddddd;
}

table#awardCodeList {
	border-collapse: collapse;
}

table#awardCodeList td {
	margin: 3px;
}

#awardCodeList {
	min-width: 400px;
}
</style>

<div id="awardCodeWrapper" class="clearCenter" style="max-width: 75%">
	<fieldset>
		<legend>Award Codes</legend>
		<div align="center" style="margin-bottom: 15px">
			<a class="buttonAnchor" href="javascript:showAwardCodePopup()">Add
				Award Code</a>
			</td>
		</div>
		<table class="formatTable" id="awardCodeList" border="1"
			summary="List of Award Codes">
			<thead>
				<tr>
					<td class="noborder" width="30%"></td>
					<td class="noborder" width="5%"></td>
					<td class="noborder" width="15%"></td>
					<td class="noborder" width="15%"></td>
					<td class="noborder" width="15%" title="Filter by Type"></td>
					<td class="noborder" width="7%" title="Filter by Status"></td>
					<c:if test="${not FORM_READ_ONLY}">
						<td class="noborder" width="7%"></td>
					</c:if>
				</tr>
				<tr>
					<th>Name</th>
					<th>Code</th>
					<th>Required Hours</th>
					<th>Award Hours</th>
					<th class="select-filter">Type</th>
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
