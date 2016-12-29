<%@ include file="inc_header.jsp"%>

<script type="text/javascript">
	$(function() {
	    var theDataTable = $('#${fn:escapeXml(param.dataTableId)}').DataTable({
	    	buttons: ['excel', {
				extend : 'pdfHtml5',
				orientation : 'landscape'
			}, 'print'],
			
	    	<c:if test="${not empty param.dom}">
	    		"dom": '${fn:escapeXml(param.dom)}',
	    	</c:if>
	    	<c:if test="${empty param.dom}">
	    		"dom": '<"top"fBi>rt<"bottom"pl><"clear">',
	    	</c:if>
	    		
	    	<c:if test="${not empty param.drawCallBackFn}">
	    	"drawCallback": ${fn:escapeXml(param.drawCallBackFn)},
	    	</c:if>
	    	"lengthMenu" : [ [ 10, 50, -1 ],
	    	 				[ 10, 50, "All" ] ],
	    	 <c:choose>
	    		 <c:when test="${not empty param.defaultSort}">
	    			"order":${fn:escapeXml(param.defaultSort)},
	    		 </c:when>
	   			<c:otherwise>
	   				"order": [],
	   			</c:otherwise>
   			</c:choose>
	    	"pageLength": 10,
	    	"pagingType": "full_numbers",
	    	"stateSave": false,
	    	<c:if test="${not empty fn:escapeXml(param.stripeClasses)}">
	    	"stripeClasses" : [${fn:escapeXml(param.stripeClasses)}],
	    	</c:if>
		})
	    
	    rebuildTableFilters('${fn:escapeXml(param.dataTableId)}')
	})
	
	<c:if test="${not empty fn:escapeXml(param.refreshDataMethod)}">
		function ${fn:escapeXml(param.refreshDataMethod)}(r) {
			var table = $('#${fn:escapeXml(param.dataTableId)}').DataTable()
			table.clear()
			table.rows.add(r)
	        table.draw()
			rebuildTableFilters('${fn:escapeXml(param.dataTableId)}')
		}
	</c:if>
</script>

<c:set var="bodyCellTextAlign" value="left" />
<c:if test="${not empty fn:escapeXml(param.bodyCellTextAlign)}">
	<c:set var="bodyCellTextAlign" value="${fn:escapeXml(param.bodyCellTextAlign)}" />
</c:if>

<style>
#${fn:escapeXml(param.dataTableId)} {
	border:thin solid;	
}

#${fn:escapeXml(param.dataTableId)} th {
	font-weight: bold;
	text-align: center;
	padding: 3px 18px 3px 10px;
	margin: 3px;
	background-color: #B9CFE6;
}

#${fn:escapeXml(param.dataTableId)} td {
	text-align: ${bodyCellTextAlign};
	padding: 3px 4px;
	margin: 3px 4px;
}

#${fn:escapeXml(param.dataTableId)} #allRowsController{
	padding: 3px 4px;
	margin: 3px 4px;
}
</style>