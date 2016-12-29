<%@ include file="../shared/inc_header.jsp"%>

<jsp:include page="../shared/inc_adjustmentDetail.jsp">
	<jsp:param name="resultCallbackMethod" value="refreshTable" />
	<jsp:param name="getLedgerAdjustmentMapFn" value="getLedgerAdjustmentMap" />
</jsp:include>

<script type="text/javascript">
	$(function() {
		$('#paramDiv .dateInput').each(function() {
			$(this).enableDatePicker({
				showOn : "button",
				buttonImage : imgHomePath + "/calendar.gif",
				buttonImageOnly : true
			})
			$(this).mask(twoDigitDateMask)
		})

		var colArray = [ {
			"render" : function(row, type, val, meta) {
				return val.donGenPostFund.generalPostFund
			}
		}, {
			"render" : function(row, type, val, meta) {
				if (type === 'display')
					return '<a href="javascript:editAdjustment(' + val.id + ')">' + val.requestDate + '</a>'
				return val.requestDate
			}
		}, {
			"render" : function(row, type, val, meta) {
				if (type == 'display')
					return '$' + formatAndAddThousandSeparators(val.amount)
				return val.amount
			}
		}, {
			"render" : function(row, type, val, meta) {
				return val.justification
			}
		}, {
			"render" : function(row, type, val, meta) {
				if (type === 'display') {
					var s = ''
					if ($.trim(val.originator.email) != '') {
						s += ' <a href="mailto:' + val.originator.email
							+ '">' + (val.originator.displayName || '') + '</a>'
					} else {
						s += (val.originator.displayName || '')
					}
					if (val.originator.telephoneNumber) {
						s += '<br><nobr>' + (val.originator.telephoneNumber || '') + '</nobr>'
					}
					return s
				}
				
				return val.originator.displayName + ' ' + val.originator.telephoneNumber + ' ' + val.originator.email
			}
		} ]
		<c:if test="${canDelete}">
		colArray.push({
			"render" : function(row, type, val, meta) {
				var actions = '<div style="margin:0 auto; text-align:center; white-space:nowrap">'
					actions += '<a href="javascript:deleteLedgerAdjustment('
							+ val.id + ')"><img src="' + imgHomePath
							+ '/delete.gif" border="0" hspace="5" align="center" alt="Delete Ledger Adjustment" /></a>'
				actions += '</div>'
				return actions;
			}
		})	
		</c:if>
		
		var theDataTable = $('#ledgerAdjustmentSearchResultsList').DataTable({
			buttons : [ {
				extend : 'excel'
			}, {
				extend : 'pdfHtml5',
				orientation : 'landscape'
			}, {
				extend : 'print'
			} ],

			"columns" : colArray,
			"dom" : '<"top"fBi>rt<"bottom"><"clear">',
			"order" : [],
			"paging" : false
		})
		
		$("#generalPostFundSelect").val(${donGenPostFundId})
		$("#startDateInput").val('${startDate}')
		$("#endDateInput").val('${endDate}')

		$("#submitButton").click(refreshTable)
		refreshTable()
	})

	function validateForm() {
		$("input").prop("disabled", false)
		showSpinner(null, true)
		return true
	}

	var ledgerAdjustmentMap = {}
	
	function refreshTable() {
		$.ajax({
			url : ajaxHomePath + '/ledgerAdjustment/list',
			dataType : 'json',
			data : {
				"startDate" : $("#startDateInput").val(),
				"endDate" : $("#endDateInput").val(),
				"donGenPostFundId" : $("#generalPostFundSelect").val()
			},
			error : commonAjaxErrorHandler,
			success : function(response) {
				ledgerAdjustmentMap = {}
				for (var i = 0; i < response.length; i++)
					ledgerAdjustmentMap[response[i].id] = response[i]
				
				var table = $('#ledgerAdjustmentSearchResultsList').DataTable()
				table.clear()
				table.rows.add(response)
				
				table.draw()
				
				$("#listWrapper").toggle(response.length > 0)
				$("#noResultsDiv").toggle(response.length == 0)
				$("#maxResultsDiv").toggle(response.length == ${maxResults})
			}
		})
	}
	
	function getLedgerAdjustmentMap() {
		return ledgerAdjustmentMap
	}
	
	function deleteLedgerAdjustment(id) {
		confirmDialog('Are you sure you want to delete this ledger adjustment?', function() {
			$.ajax({
				url : ajaxHomePath + '/ledgerAdjustment/delete',
				dataType : 'json',
				data : {
					"id" : id
				},
				error : commonAjaxErrorHandler,
				success : refreshTable
			})
		})
	}
</script>

<div class="clearCenter" id="paramDiv">
	<table>
		<tr>
			<td align="right">Start Date:</td>
			<td><input size="12" id="startDateInput" class="dateInput" /></td>
			<td width="40" rowspan="2">&nbsp;</td>
			<td align="right">General Post Fund:</td>
			<td><select id="generalPostFundSelect">
					<option value="-1">(any)</option>
					<c:forEach items="${allGPFs}" var="gpf">
						<option value="${gpf.id}"><c:out
								value="${gpf.generalPostFund}" /></option>
					</c:forEach>
			</select></td>
		</tr>
		<tr>
			<td align="right">End Date:</td>
			<td><input size="12" id="endDateInput" class="dateInput" /></td>
			<td></td>
			<td></td>
		</tr>
	</table>

	<div class="clearCenter" style="padding-top: 15px">
		<a id="submitButton" class="buttonAnchor" href="#">Submit</a> <a
			id="cancelOperationBtn" class="buttonAnchor"
			href="${current_breadcrumb}">Cancel</a>
	</div>
</div>

<style>
.ui-state-default a.tableHeaderLink, .ui-state-default a.tableHeaderLink:link,
	.ui-state-default a.tableHeaderLink:visited {
	text-decoration: underline;
	font-weight: normal;
}

#ledgerAdjustmentSearchResultsList thead th.tableHeaderLinkWrapper {
	font-weight: normal;
}
#ledgerAdjustmentSearchResultsList {
	border-collapse: collapse;
}
#ledgerAdjustmentSearchResultsList td {
    padding: 3px;
}
</style>

<div class="clearCenter" style="padding-top: 15px; display: none"
	id="maxResultsDiv">The maximum number of search results was met.
	Please add more restrictive criteria and search again.</div>
<div class="clearCenter" style="padding-top: 15px; display: none"
	id="noResultsDiv">
	Sorry, no ledger adjustments were found that matched the specified criteria.
	</td>
</div>
<div class="clearCenter" id="listWrapper" style="display:none">
	<table id="ledgerAdjustmentSearchResultsList" class="stripe" border="1"
		summary="List of Ledger Adjustments">
		<thead>
			<tr>
				<th>General Post Fund</th>
				<th>Date</th>
				<th>Amount</th>
				<th>Justification</th>
				<th>Requester</th>
				<c:if test="${canDelete}">
					<th>Actions</th>
				</c:if>
			</tr>
		</thead>
		<tbody>
		</tbody>
	</table>
</div>