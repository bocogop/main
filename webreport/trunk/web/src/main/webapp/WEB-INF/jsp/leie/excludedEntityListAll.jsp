<%@ include file="../shared/inc_header.jsp"%>

<%-- Example to hide search filter, if they want that elsewhere - CPB
	<jsp:param name="dom" value='<"top"i>rt<"bottom"pl><"clear">' />
	--%>
<script type="text/javascript">
	$(function() {
		$("#excludedEntityList").DataTable({
			"ajax" : ajaxHomePath + "/excludedEntities/search",
			buttons: ['excel', {
				extend : 'pdfHtml5',
				orientation : 'landscape'
			}, 'print'],
			"columnDefs" : [ {
				"targets" : 0,
				"data" : function(row, type, val, meta) {
					return row.displayName
				}
			}, {
				"targets" : 1,
				"data" : function(row, type, val, meta) {
					return row.exclusionType ? row.exclusionType.ssa : ''
				}
			}, {
				"targets" : 2,
				"data" : function(row, type, val, meta) {
					return row.exclusionType ? row.exclusionType.code42Usc : ''
				}
			}, {
				"targets" : 3,
				"data" : function(row, type, val, meta) {
					return row.exclusionDate
				}
			}, {
				"targets" : 4,
				"data" : function(row, type, val, meta) {
					return row.exclusionType ? row.exclusionType.description : ''
				}
			}, {
				"targets" : 5,
				"data" : function(row, type, val, meta) {
					return getAddressDashedBoxEl(row).outerHTML()
				}
			}, {
				"targets" : 6,
				"data" : function(row, type, val, meta) {
					return row.dob
				}
			} ],
			"dom" : '<"top"fBi>rt<"bottom"pl><"clear">',
			"lengthMenu" : [ [ 5, 10, 100 ], [ 5, 10, 100 ] ],
			"pagingType" : "full_numbers",
			"pageLength" : 5,
			"serverSide" : true,
			"stateSave" : false
		})
	})

	function getAddressDashedBoxEl(ee) {
		var theHtml = '<table width="100%" class="addressBox">'
		theHtml += '<tr valign="top"><td nowrap width="99%">'

		var addressHtml = ""
		if (ee.addressMultilineDisplay)
			addressHtml = escapeHTML(ee.addressMultilineDisplay)

		theHtml += convertLinefeedToBR(addressHtml) + '</td></tr></table>'
		return getBoxEl(theHtml, false)
	}
</script>

<div style="float: left; margin-left:15px">
	<a href="${home}/excludedEntityList.htm"><img alt="Return to WR Excluded Entity Matches"
		src="${imgHome}/left.gif" border="0" align="absmiddle" /></a> <a
		href="${home}/excludedEntityList.htm">Back to WR Excluded
		Entities</a>
</div>

<div class="clearCenter" style="border: 1px gray;">
	LEIE Source data last updated: <b><wr:localDate
			date="${lastUpdatedDate}" pattern="${DATE_ONLY}" /></b>
</div>
<p />

<table summary="Format table" align="center" width="90%">
	<tr>
		<td align="center">
			<table id="excludedEntityList" class="stripe"
				summary="List of Excluded Entities" width="98%">
				<thead>
					<tr>
						<th>LEIE Name</th>
						<th class="select-filter">SSA</th>
						<th class="select-filter">42 USC Code</th>
						<th>Exclusion Date</th>
						<th>Description</th>
						<th>LEIE Address</th>
						<th>LEIE Date of Birth</th>
					</tr>
				</thead>
				<tbody>
				</tbody>	
			</table>
		</td>
	</tr>
</table>
