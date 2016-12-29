<%@ include file="../shared/inc_header.jsp"%>

<jsp:include page="../shared/inc_expenditureDetail.jsp">
	<jsp:param name="resultCallbackMethod" value="refreshTable" />
	<jsp:param name="getExpenditureMapFn" value="getExpenditureMap" />
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

		var colDefs = [ {
			"render" : function(row, type, val, meta) {
				return val.donGenPostFund.generalPostFund
			}
		}, {
			"render" : function(row, type, val, meta) {
				if (type === 'display')
					return '<a class="appLink" href="javascript:editExpenditure(' + val.id + ')">' + val.requestDate + '</a>'
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
				return val.vendor
			}
		}, {
			"render" : function(row, type, val, meta) {
				var hasQuantity = (val.quantity != null)
				var hasUnit = (val.unit != null)
				var hasUnitPrice = (val.unitPrice != null)
				
				var s = ''
				if (hasQuantity)
					s += val.quantity
				if (hasUnit && val.unit.code != 'EA')
					s += ' ' + val.unit.name
				if (hasUnitPrice) {
					s += ' @ $' + formatAndAddThousandSeparators(val.unitPrice)
					if (hasQuantity && hasUnit && val.quantity > 1)
						s += '/' + val.unit.name
				}
				return s
			}
		}, {
			"render" : function(row, type, val, meta) {
				if (type === 'display') {
					var s = ''
					if ($.trim(val.originator.email) != '') {
						s += ' <a class="appLink" href="mailto:' + val.originator.email
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
		}, {
			"render" : function(row, type, val, meta) {
				return val.transactionId
			}
		}, {
			"render" : function(row, type, val, meta) {
				return val.purchaseOrderNumber
			}
		}, {
			"render" : function(row, type, val, meta) {
				var totalAmount = 0, totalDonations = 0
				if (val.donations && val.donations.length > 0) {
					totalDonations = val.donations.length
					if (totalDonations == 1) {
						return val.donations[0].donationDate + ' - $' + formatAndAddThousandSeparators(val.donations[0].totalDonationAmount)
					} else {
						var s = ''
						for (var i = 0; i < totalDonations; i++) {
							var donation = val.donations[i]
							s += '<li>' + donation.donationDate + ' - $' + formatAndAddThousandSeparators(donation.totalDonationAmount) + '</li>'
							totalAmount += donation.totalDonationAmount
						}
						
						return totalDonations + ' Donation(s), Total: $' + formatAndAddThousandSeparators(totalAmount)
							+ '<ul>' + s + '</ul>'
					}
				}
				
				return ''
			}
		} ] 
			
		<c:if test="${canDelete}">
		colDefs.push({
			"render" : function(row, type, val, meta) {
				var actions = '<div style="margin:0 auto; text-align:center; white-space:nowrap">'
					actions += '<a class="appLink" href="javascript:deleteExpenditure('
							+ val.id + ')"><img src="' + imgHomePath
							+ '/delete.gif" border="0" hspace="5" align="center" alt="Delete Expenditure" /></a>'
				actions += '</div>'
				return actions;
			}
		})
		</c:if>
		
		var theDataTable = $('#expenditureSearchResultsList').DataTable({
			buttons : [ {
				extend : 'excel'
			}, {
				extend : 'pdfHtml5',
				orientation : 'landscape'
			}, {
				extend : 'print'
			} ],

			"columns" : colDefs,
			"dom" : '<"top"fBi>rt<"bottom"><"clear">',
			"order" : [],
			"paging" : false,
			"stripeClasses" : [ 'odd' ]
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

	var expenditureMap = {}
	
	function refreshTable() {
		$.ajax({
			url : ajaxHomePath + '/expenditure/list',
			dataType : 'json',
			data : {
				"startDate" : $("#startDateInput").val(),
				"endDate" : $("#endDateInput").val(),
				"donGenPostFundId" : $("#generalPostFundSelect").val()
			},
			error : commonAjaxErrorHandler,
			success : function(response) {
				expenditureMap = {}
				for (var i = 0; i < response.length; i++)
					expenditureMap[response[i].id] = response[i]
				
				var table = $('#expenditureSearchResultsList').DataTable()
				table.clear()
				table.rows.add(response)
				
				table.rows().every(function(rowIndex, tableLoop, rowLoop) {
					var expenditure = this.data()
					var row = '<div style="margin-left:40px">'
					if (expenditure.description)
						row += '<i>Description:</i> ' + escapeHTML(expenditure.description)
					if (expenditure.description && expenditure.comments)
						row += '<br>'
					if (expenditure.comments)
						row += '<i>Comments:</i> ' + escapeHTML(expenditure.comments)
					row += '</div>'
					this.child(row).show()
				})
				
				table.draw()
				
				$("#listWrapper").toggle(response.length > 0)
				$("#noResultsDiv").toggle(response.length == 0)
				$("#maxResultsDiv").toggle(response.length == ${maxResults})
			}
		})
	}
	
	function getExpenditureMap() {
		return expenditureMap
	}
	
	function deleteExpenditure(id) {
		confirmDialog('Are you sure you want to delete this expenditure?', function() {
			$.ajax({
				url : ajaxHomePath + '/expenditure/delete',
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

#expenditureSearchResultsList thead th.tableHeaderLinkWrapper {
	font-weight: normal;
}
#expenditureSearchResultsList {
	border-collapse: collapse;
}
#expenditureSearchResultsList td {
    padding: 3px;
}
</style>

<div class="clearCenter" style="padding-top: 15px; display: none"
	id="maxResultsDiv">The maximum number of search results was met.
	Please add more restrictive criteria and search again.</div>
<div class="clearCenter" style="padding-top: 15px; display: none"
	id="noResultsDiv">
	Sorry, no expenditures were found that matched the specified criteria.
	</td>
</div>
<div class="clearCenter" id="listWrapper" style="display:none">
	<table id="expenditureSearchResultsList" class="stripe" border="1"
		summary="List of Expenditures">
		<thead>
			<tr>
				<th>General Post Fund</th>
				<th>Date</th>
				<th>Amount</th>
				<th>Vendor</th>
				<th>Quantity</th>
				<th>Purchase Requester</th>
				<th>Transaction #</th>
				<th>Purchase Order</th>
				<th>Donations</th>
				<c:if test="${canDelete}">
					<th>Action</th>
				</c:if>
			</tr>
		</thead>
		<tbody>
		</tbody>
	</table>
</div>