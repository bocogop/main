<%@ include file="../shared/inc_header.jsp"%>

<c:if test="${FORM_READ_ONLY}">
	<%@ include file="../shared/inc_modifyToView.jsp"%>
</c:if>

<jsp:include page="../shared/inc_expenditureDetail.jsp">
	<jsp:param name="resultCallbackMethod" value="refreshLedgerTable" />
</jsp:include>

<jsp:include page="../shared/inc_adjustmentDetail.jsp">
	<jsp:param name="resultCallbackMethod" value="refreshLedgerTable" />
</jsp:include>

<script>
	var isReadOnly = ${FORM_READ_ONLY}
	var canCreateExpenditures = false
	var ledgerEntries = []
	var startingBalance = 0
	
	<sec:authorize
	access="hasAnyAuthority('${PERMISSION_EDONATION_MANAGE}')">
		canCreateExpenditures = true
	</sec:authorize>

	$(function() {
		buildLedgerTable()
		
		$('.dateInput').each(function() {
			$(this).enableDatePicker({
				showOn : "button",
				buttonImage : imgHomePath + "/calendar.gif",
				buttonImageOnly : true
			})
			$(this).mask(twoDigitDateMask)
		})
		
		if ("<c:out value="${dateRangeType}" />" != "")
			$("#dateRangeType").val("<c:out value="${dateRangeType}" />")
		if ("<c:out value="${specificFY}" />" != "")
			$("#specificFY").val("<c:out value="${specificFY}" />")
		$("#dateRangeType").change(function() {
			$(".dateRangeDateInput").toggle($(this).val() == 'custom')
			$(".specificFYInput").toggle($(this).val() == 'specificfy')
		})
		$("#dateRangeType").trigger('change')
		
		refreshLedgerTable()
		
		setPageTitleText('Daily Ledger for GPF "<c:out value="${donGenPostFund.generalPostFund}" />"')
	})
	
	function buildLedgerTable() {
		var theDataTable = $('#ledgerList').DataTable({
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
						if (type === 'display')
							return '<a class="appLink" href="' + homePath + '/donationList.htm?listDonationsMode=timeperiodsearch&startDate='
									+ val.date + '&endDate=' + val.date + '">$'	+ formatAndAddThousandSeparators(val.donationTotal) + '</a>'
						return val.donationTotal
					} 
				}, {
					"render" : function(row, type, val, meta) {
						if (type === 'display')
							return '<a class="appLink" href="' + homePath + '/expenditureList.htm?startDate=' + val.date + '&endDate=' + val.date+ '">$'
								+ formatAndAddThousandSeparators(val.expenditureTotal) + '</a>'
						return val.expenditureTotal
					}
				}, {
					"render" : function(row, type, val, meta) {
						if (type === 'display')
							return '<a class="appLink" href="' + homePath + '/ledgerAdjustmentList.htm?startDate=' + val.date
								+ '&endDate=' + val.date + '">$' + formatAndAddThousandSeparators(val.ledgerAdjustmentTotal) + '</a>'
						return val.ledgerAdjustmentTotal
					}
				}, {
					"render" : function(row, type, val, meta) {
						if (type === 'display')
							return '$'
								+ formatAndAddThousandSeparators(val.finalBalance)
						return val.finalBalance
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
	    		var theTable = $('#ledgerList').DataTable()
	    		
	    		for (var i = 0; i < ledgerEntries.length; i++) {
	    			totals[0] += ledgerEntries[i].donationTotal
	    			totals[1] += ledgerEntries[i].expenditureTotal
	    			totals[2] += ledgerEntries[i].ledgerAdjustmentTotal
	    		}
	    		for (var i = 0; i < 3; i++)
					$("#totalRow td:eq(" + (i + 1) + ")")
							.text('$'+ formatAndAddThousandSeparators(totals[i]));
			}
		})
	}
	
	function refreshLedgerTable() {
		var id = ${donGenPostFund.id}
		var dateRangeType = $("#dateRangeType").val()
		var specificFY = $("#specificFY").val()
		var startDate = $("#startDate").val()
		var endDate = $("#endDate").val()
		
		if (dateRangeType == 'custom') {
			if (!validateDate(startDate)) {
				displayAttentionDialog('Please enter a valid start date.')
				return
			}
			if (!validateDate(endDate)) {
				displayAttentionDialog('Please enter a valid end date.')
				return
			}
		}
		
		window.history.replaceState({}, '', homePath + '/ledgerDaily.htm?donGenPostFundId=' + id + '&dateRangeType=' + dateRangeType
				+ (dateRangeType == 'specificfy' ? '&specificFY=' + specificFY : '')
				+ (dateRangeType == 'custom' ? '&startDate=' + startDate : '')
				+ (dateRangeType == 'custom' ? '&endDate=' + endDate : ''))
	
		$.ajax({
			url : ajaxHomePath + '/ledger/gpf',
			dataType : 'json',
			data : {
				"id": id,
				"dateRangeType" : dateRangeType,
				"specificFY" : specificFY,
				"startDate" : startDate,
				"endDate" : endDate
			},
			error : commonAjaxErrorHandler,
			success : function(response) {
				ledgerEntries = response.dailySummaries
				var balance = response.startingBalance
				
				for (var i = 0; i < response.dailySummaries.length; i++) {
					var ds = response.dailySummaries[i]
					balance += ds.donationTotal - ds.expenditureTotal + ds.ledgerAdjustmentTotal
					ds.finalBalance = balance
				}
				
				var table = $('#ledgerList').DataTable()
				table.clear()
				table.rows.add(response.dailySummaries)
				table.draw()				
		    }
		})
	}
</script>

<style>
#ledgerList tr.serviceRow {
	background-color: #dddddd;
}

#ledgerList {
	border-collapse: collapse;
	min-width: 400px;
}

#ledgerList td {
	margin: 3px;
}
</style>

<div class="clearCenter">
	<table>
		<tr valign="middle">
			<td align="right">Date Range:</td>
			<td><select id="dateRangeType">
					<option value="fy">Current Fiscal Year</option>
					<option value="lastfy">Previous Fiscal Year</option>
					<option value="specificfy">Other Fiscal Year</option>
					<option value="last6month">Last 6 Months</option>
					<option value="month">Current Month</option>
					<option value="lastmonth">Previous Month</option>
					<option value="custom">Custom</option>
			</select></td>
			<td width="30">&nbsp;</td>
			<td class="dateRangeDateInput" style="display:none">
				<table>
					<tr>
						<td align="right">Start Date:</td>
						<td><input id="startDate" size="12" type="text"
							class="dateInput" value="${startDate}" /></td>
					</tr>
					<tr>
						<td align="right">End Date:</td>
						<td><input id="endDate" size="12" type="text"
							class="dateInput" value="${endDate}" /></td>
					</tr>
				</table>
			</td>
			<td width="30" class="dateRangeDateInput" style="display:none">&nbsp;</td>
			<td class="specificFYInput" style="display: none">
				<table>
					<tr>
						<td align="right">Fiscal Year:</td>
						<td><select id="specificFY"><c:forEach begin="0"
									end="${currentFY - 1995}" var="i">
									<option value="${currentFY - i}"><c:out value="${currentFY - i}" /></option>
								</c:forEach></select></td>
					</tr>
				</table>
			</td>
			<td width="30" class="specificFYInput" style="display: none">&nbsp;</td>
			<td><a class="buttonAnchor"
				href="javascript:refreshLedgerTable()">Submit</a></td>
		</tr>
	</table>
</div>

<div id="ledgerWrapper" class="clearCenter" style="max-width: 75%">
	<fieldset>
		<legend>Daily Ledger</legend>

		<table class="formatTable" id="ledgerList" border="1"
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
					<th>Date</th>
					<th>Total Donations</th>
					<th>Total Expenditures</th>
					<th>Total Adjustments</th>
					<th>Ending Balance</th>
				</tr>
			</thead>
			<tbody>
			</tbody>
		</table>
	</fieldset>
</div>
