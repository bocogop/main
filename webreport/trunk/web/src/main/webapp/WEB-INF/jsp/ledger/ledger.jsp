<%@ include file="../shared/inc_header.jsp"%>

<c:if test="${FORM_READ_ONLY}">
	<%@ include file="../shared/inc_modifyToView.jsp"%>
</c:if>

<jsp:include page="../shared/inc_expenditureDetail.jsp">
	<jsp:param name="resultCallbackMethod"
		value="refreshGeneralLedgerTable" />
	<jsp:param name="getGPFBalanceMapFn" value="getGPFBalanceMap" />
</jsp:include>

<jsp:include page="../shared/inc_adjustmentDetail.jsp">
	<jsp:param name="resultCallbackMethod"
		value="refreshGeneralLedgerTable" />
	<jsp:param name="getGPFBalanceMapFn" value="getGPFBalanceMap" />
</jsp:include>

<script>
	var isReadOnly = ${FORM_READ_ONLY}
	var canCreateExpenditures = false
	var ledgerEntries = []
	
	var gpfBalances = {}
	
	<sec:authorize
	access="hasAnyAuthority('${PERMISSION_EDONATION_MANAGE}')">
		canCreateExpenditures = true
	</sec:authorize>

	$(function() {
		buildGeneralLedgerTable()
		
		if ("<c:out value="${dateRangeType}" />" != "") {
			$("#dateRangeType").val("<c:out value="${dateRangeType}" />")
		} else {
			$("#dateRangeType option:eq(0)").prop('selected', true)
		}
		
		if ("<c:out value="${specificFY}" />" != "") {
			$("#specificFY").val("<c:out value="${specificFY}" />")
		} else {
			$("#specificFY option:eq(0)").prop('selected', true)
		}
		
		$("#dateRangeType").change(function() {
			$(".specificFYInput").toggle("specificfy" == $(this).val())
		})
		$("#dateRangeType").trigger('change')
		
		refreshGeneralLedgerTable()
	})
	
	function getGPFBalanceMap(callback) {
		if (!$.isEmptyObject(gpfBalances))
			callback(gpfBalances)
	}
	
	function getStartAndEndDate() {
		var startDate = $("#dateRangeType option:selected").attr('startDate')
		var endDate = $("#dateRangeType option:selected").attr('endDate')
		
		if (dateRangeType == 'specificfy') {
			var selectedOpt = $("#specificFY option:selected")
			startDate = selectedOpt.attr('startDate')
			endDate = selectedOpt.attr('endDate')
		}
		
		return {
			startDate : startDate,
			endDate : endDate
		}
	}
	
	function buildGeneralLedgerTable() {
		var theDataTable = $('#generalLedgerList').DataTable({
			buttons: ['excel', {
				extend : 'pdfHtml5',
				orientation : 'landscape'
			}, 'print'],
			"columns" : [ {
		        	"render" : function(row, type, val, meta) {
		        		if (!val.gpf) return ''
		        		if (type === 'display')
							return '<a href="' + homePath + '/ledgerDaily.htm?donGenPostFundId=' + val.gpf.id + '&dateRangeType=' 
									+ $("#dateRangeType").val() + '&specificFY=' + $("#specificFY").val() + '" class="appLink"><nobr>'
									+ val.gpf.generalPostFund + '</nobr></a>'
						return val.gpf.generalPostFund
		        	}
				}, {
					"render" : function(row, type, val, meta) {
						if (type === 'display')
							return "$" + formatAndAddThousandSeparators(val.balance)
						return val.balance
					} 
				}, {
					"render" : function(row, type, val, meta) {
						if (type === 'display') {
							var x = getStartAndEndDate()	
						
							return '<a class="appLink" href="' + homePath + '/donationList.htm?listDonationsMode=timeperiodsearch&startDate='
								+ x.startDate + '&endDate=' + x.endDate + '">$' + formatAndAddThousandSeparators(val.donationTotal)
								+ '</a> <a href="javascript:popupDonorSearch(\'menuAdd\')"><img align="right" style="margin-right:5px" src="'
								+ imgHomePath + '/add.png" border="0" /></a>'
						}
						return val.donationTotal
					}
				}, {
					"render" : function(row, type, val, meta) {
						if (!val.gpf) return ''
						var dateRangeType = $("#dateRangeType").val()
						
						if (type === 'display') {
							var x = getStartAndEndDate()
							
							return '<a href="' + homePath + '/expenditureList.htm?donGenPostFundId=' + val.gpf.id + '&startDate='
									+ x.startDate + '&endDate=' + x.endDate + '" class="appLink">$' + formatAndAddThousandSeparators(val.expenditureTotal)
									+ '</a> <a href="javascript:newExpenditure(' + val.gpf.id + ')"><img align="right" style="margin-right:5px" src="'
									+ imgHomePath + '/add.png" border="0" /></a>'
						}
						return val.expenditureTotal
					}
				}, {
					"render" : function(row, type, val, meta) {
						if (type === 'display') {
							var x = getStartAndEndDate()
							
							return '<a href="' + homePath + '/ledgerAdjustmentList.htm?donGenPostFundId=' + val.gpf.id + '&startDate='
								+ x.startDate + '&endDate=' + x.endDate + '" class="appLink">$' + formatAndAddThousandSeparators(val.ledgerAdjustmentTotal)
								+ '</a> <a href="javascript:newAdjustment(' + val.gpf.id
								+ ')"><img align="right" style="margin-right:5px" src="' + imgHomePath + '/add.png" border="0" /></a>'
						}
						return val.ledgerAdjustmentTotal
					}
				}
			],
	    	"dom": '<"top"fBi>rt<"bottom"pl><"clear">',
	    	"lengthMenu" : [ [ 20, 50, -1 ],
	    	 				[ 20, 50, "All" ] ],
			"order": [],
	    	"pageLength": 20,
	    	"pagingType": "full_numbers",
	    	"stripeClasses" : [],
	    	"drawCallback" : function() {
	    		var totals = [0, 0, 0, 0]
	    		
	    		for (var i = 0; i < ledgerEntries.length; i++) {
	    			totals[0] += ledgerEntries[i].balance
	    			totals[1] += ledgerEntries[i].donationTotal
	    			totals[2] += ledgerEntries[i].expenditureTotal
	    			totals[3] += ledgerEntries[i].ledgerAdjustmentTotal
	    		}
	    		for (var i = 0; i < 4; i++)
					$("#totalRow td:eq(" + (i+1) + ")")
							.text('$'+ formatAndAddThousandSeparators(totals[i]));
			}
		})
	}
	
	function refreshGeneralLedgerTable() {
		var dateRangeType = $("#dateRangeType").val()
		var specificFY = $("#specificFY").val()
		
		window.history.replaceState({}, '', homePath + '/ledger.htm?dateRangeType=' + dateRangeType
				+ (dateRangeType == 'specificfy' ? '&specificFY=' + specificFY : ''))
		
		$.ajax({
			url : ajaxHomePath + '/ledger/summary',
			dataType : 'json',
			data : {
				"dateRangeType" : dateRangeType,
				"specificFY" : specificFY
			},
			error : commonAjaxErrorHandler,
			success : function(response) {
				ledgerEntries = response
				
				gpfBalances = {}
				for (var i = 0; i < response.length; i++)
					gpfBalances[response[i].gpf.id] = response[i].balance
				
				var table = $('#generalLedgerList').DataTable()
				table.clear()
				table.rows.add(response)
				table.draw()				
		    }
		})
	}
</script>

<style>
#generalLedgerList tr.serviceRow {
	background-color: #dddddd;
}

#generalLedgerList {
	border-collapse: collapse;
	min-width: 400px;
}

#generalLedgerList td {
	margin: 3px;
}
</style>

<div style="float: right; margin-right: 15px">
	<a class="appLink" href="${home}/ledgerAdjustmentList.htm">Manage
		Adjustments</a> <a class="appLink" href="${home}/ledgerAdjustmentList.htm"><img
		alt="Manage Adjustments" src="${imgHome}/right.gif" border="0"
		align="absmiddle" /></a>
</div>

<div class="clearCenter">
	<table>
		<tr>
			<td align="right">Date Range:</td>
			<td><select id="dateRangeType">
					<option value="fy"
						startDate="<wr:localDate date="${currentfy[0]}" pattern="${TWO_DIGIT_DATE_ONLY}" />"
						endDate="<wr:localDate date="${currentfy[1]}" pattern="${TWO_DIGIT_DATE_ONLY}" />">Current Fiscal Year</option>
					<option value="lastfy"
						startDate="<wr:localDate date="${lastfy[0]}" pattern="${TWO_DIGIT_DATE_ONLY}" />"
						endDate="<wr:localDate date="${lastfy[1]}" pattern="${TWO_DIGIT_DATE_ONLY}" />">Previous Fiscal Year</option>
					<option value="specificfy">Other Fiscal Year</option>
					<option value="month"
						startDate="<wr:localDate date="${month[0]}" pattern="${TWO_DIGIT_DATE_ONLY}" />"
						endDate="<wr:localDate date="${month[1]}" pattern="${TWO_DIGIT_DATE_ONLY}" />">Current Month</option>
					<option value="lastmonth"
						startDate="<wr:localDate date="${lastmonth[0]}" pattern="${TWO_DIGIT_DATE_ONLY}" />"
						endDate="<wr:localDate date="${lastmonth[1]}" pattern="${TWO_DIGIT_DATE_ONLY}" />">Previous Month</option>
			</select></td>
			<td width="30">&nbsp;</td>
			<td class="specificFYInput" style="display: none">
				<table>
					<tr>
						<td align="right">Fiscal Year:</td>
						<td><select id="specificFY"><c:forEach begin="0"
									end="${currentFY - 1995}" var="i">
									<c:set var="y">${currentFY - i}</c:set>
									
									<option value="${y}"
										startDate="<wr:localDate date="${fyDates[y][0]}" pattern="${TWO_DIGIT_DATE_ONLY}" />"
										endDate="<wr:localDate date="${fyDates[y][1]}" pattern="${TWO_DIGIT_DATE_ONLY}" />"><c:out value="${y}" /></option>
								</c:forEach></select></td>
					</tr>
				</table>
			</td>
			<td width="30" class="specificFYInput" style="display: none">&nbsp;</td>
			<td><a class="buttonAnchor"
				href="javascript:refreshGeneralLedgerTable()">Submit</a></td>
		</tr>
	</table>
</div>

<div id="generalLedgerWrapper" class="clearCenter"
	style="max-width: 75%">
	<fieldset>
		<legend>General Ledger</legend>

		<table class="formatTable" id="generalLedgerList" border="1"
			summary="General Ledger">
			<thead>
				<tr id="totalRow">
					<td class="noborder">TOTAL</td>
					<td class="noborder"></td>
					<td class="noborder"></td>
					<td class="noborder"></td>
					<td class="noborder"></td>
				</tr>
				<tr>
					<th>General Post Fund</th>
					<th>Current Balance</th>
					<th>Total Donations</th>
					<th>Total Expenditures</th>
					<th>Total Adjustments</th>
				</tr>
			</thead>
			<tbody>
			</tbody>
		</table>
	</fieldset>
</div>
