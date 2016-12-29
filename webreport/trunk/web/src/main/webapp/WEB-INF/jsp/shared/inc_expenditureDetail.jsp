<%@ include file="../shared/inc_header.jsp"%>

<c:set var="callbackMethod"
	value="${fn:escapeXml(param.resultCallbackMethod)}" />
<c:set var="getExpenditureMapFn"
	value="${fn:escapeXml(param.getExpenditureMapFn)}" />

<c:set var="getGPFBalanceMapFn"
	value="${fn:escapeXml(param.getGPFBalanceMapFn)}" />
<c:if test="${empty getGPFBalanceMapFn}">
	<c:set var="getGPFBalanceMapFn" value="defaultGetGPFBalanceMap" />
</c:if>

<jsp:include page="/WEB-INF/jsp/shared/inc_appUserSearchPopup.jsp">
	<jsp:param name="uniqueSearchPopupId" value="${uid}_expenditure" />
	<jsp:param name="resultCallbackMethod" value="ldapCallback" />
	<jsp:param name="includeLocalDB" value="true" />
	<jsp:param name="includeLDAP" value="true" />
</jsp:include>

<jsp:include page="/WEB-INF/jsp/shared/inc_donationSearchPopup.jsp">
	<jsp:param name="uid" value="expenditureDetail" />
	<jsp:param name="resultCallbackMethod"
		value="donationSelected" />
</jsp:include>

<script type="text/javascript">

var donations = []
function donationSelected(obj) {
	donations.push(obj)
	refreshDonationsTable()
}

function refreshDonationsTable() {
	var table = $('#expDonationTable').DataTable()
	table.clear()
	for (var i = 0; i < donations.length; i++)
		table.row.add(donations[i])
	// rebuildTableFilters('expDonationTable')
	table.draw()
}

function deleteDonation(index) {
	donations.splice(index, 1)
	refreshDonationsTable()
}

var sortedGPFs = []
var gpfNameMap = {}
<c:forEach items="${allGPFs}" var="gpf">
	sortedGPFs.push({
		name : "<c:out value="${gpf}" />",
		id : ${gpf.id}
	})
	gpfNameMap[${gpf.id}] = "<c:out value="${gpf}" />"
</c:forEach>

$(function() {
	$("#expDetailsDialog").dialog({
		autoOpen : false,
		modal : true,
		width : 650,
		height : 650,
		closeOnEscape : false,
		draggable : true,
		resizable : true,
		buttons : {
			'Submit' : {
				text : 'Submit',
				id : 'expenditureDetailDialogSubmitBtn',
				click : submitExpenditureDetails
			},
			'Cancel' : function() {
				$(this).dialog('close')
			},
		}
	})
	
	$("#expDetailsDialog").show()
	/*
	 * Required to solve 508 issue not reading dialog box title. It is also
	 * required to disable dialog animation to enable this functionality
	 */
	$('#expDetailsDialog').focus()
	
	$('#expDetailsDialog .dateInput').each(function() {
		$(this).enableDatePicker({
			showOn : "button",
			buttonImage : imgHomePath + "/calendar.gif",
			buttonImageOnly : true
		})
		$(this).mask(twoDigitDateMask)
	})
	
	var theDataTable = $('#expDonationTable').DataTable({
			"columns" : [ {
	        	"render" : function(row, type, val, meta) {
					return val.id
				}
			}, {
				"render" : function(row, type, val, meta) {
					return val.donor.displayName
				}
			}, {
				"render" : function(row, type, val, meta) {
					return val.donationDate
				}
			}, {
				"render" : function(row, type, val, meta) {
					var s = 'Total: $' + formatAndAddThousandSeparators(val.totalDonationAmount)
					+ ' <ul style="margin-top:2px;margin-bottom:2px">'
					for (var i = 0; i < val.donationDetails.length; i++) {
						var dd = val.donationDetails[i]
						s += '<li>GPF "' + (dd.donGenPostFund ? dd.donGenPostFund.generalPostFund : '(none)') + '": $' 
							+ formatAndAddThousandSeparators(dd.donationValue) + '</li>'
					}
					s += '</ul>'
					return s
				}
			}, {
				"render" : function(row, type, val, meta) {
					return val.donationDescription
				}
			}
			<c:if test="${not FORM_READ_ONLY}">
			, {
				"render" : function(row, type, val, meta) {
					var actions = '<div style="margin:0 auto; text-align:left"><nobr>'
	
					actions += '<a href="javascript:deleteDonation('
						+ meta.row + ')"><img src="'+ imgHomePath
						+ '/cross.png" alt="Detach donation from expenditure" border="0" align="center"/></a>'
					
					actions += '</nobr></div>'
					return actions
				}
			}
			</c:if>
		],
		"dom": '<"top"i>rt<"bottom"pl><"clear">',
		"order" : [],
		"paging" : false
	})
	
	refreshDonationsTable()
})

function showExpenditureDetailsPopup(expenditure, postShowCallback) {
	var doIt = function(gpfBalances) {
		var isDisabled = <c:out value="${FORM_READ_ONLY}" default="false" />
		var hasExpenditure = expenditure !== null
		$("#expId").val(hasExpenditure ? expenditure.id : '')
		
		$("#expTransactionId").val(hasExpenditure ? expenditure.transactionId : '')
		$("#expGPF").val(hasExpenditure && expenditure.donGenPostFund ? expenditure.donGenPostFund.id : '')
		
		$(".expPopupInput").prop('disabled', hasExpenditure && isDisabled)
		
		$("#expGPF").empty()
		$("#expGPF").append('<option value="">Please select...</option>')
		for (var i = 0; i < sortedGPFs.length; i++) {
			$("#expGPF").append(
					$('<option value="' + sortedGPFs[i].id + '"'
					+ (hasExpenditure && expenditure.donGenPostFund.id == sortedGPFs[i].id ? ' selected="selected"' : '')
					+ '></option>').text(sortedGPFs[i].name
					+ ' (balance $' + formatAndAddThousandSeparators(gpfBalances[sortedGPFs[i].id]) + ')'))
		}
		
		$("#expOriginatorUserName").val(hasExpenditure && expenditure.originator ? expenditure.originator.username : '')
		$("#expOriginatorDisplayName").text(hasExpenditure && expenditure.originator ? expenditure.originator.displayName : '')
		$("#expRequestDate").val(hasExpenditure ? expenditure.requestDate : '')
		$("#expAmount").val(hasExpenditure ? expenditure.amount : '')
		$("#expVendor").val(hasExpenditure ? expenditure.vendor : '')
		$("#expDescription").val(hasExpenditure ? expenditure.description : '')
		$("#expQuantity").val(hasExpenditure ? expenditure.quantity : '')
		if (hasExpenditure && expenditure.unit) {
			$("#expUnit option[code='" + expenditure.unit.code + "']").prop('selected', true)
		} else {
			$("#expUnit").val('-1')
		}
		$("#expUnitPrice").val(hasExpenditure ? expenditure.unitPrice : '')
		$("#expComments").val(hasExpenditure ? expenditure.comments : '')
		
		donations = hasExpenditure ? expenditure.donations : []
		refreshDonationsTable()
		
		$("#expDetailsDialog").dialog('open')
		if (postShowCallback)
			postShowCallback()
	}	
	
	${getGPFBalanceMapFn}(doIt)
}

function defaultGetGPFBalanceMap(callback) {
	$.ajax({
		url : ajaxHomePath + '/ledger/gpfBalances',
		method: 'POST',
		dataType : 'json',
		error : commonAjaxErrorHandler,
		success : function(response) {
			balanceMap = {}
			for (var i = 0; i < response.length; i++) {
				balanceMap[response[i].gpf.id] = response[i].balance
			}
			callback(balanceMap)
		}
	})
}

function ldapCallback(userObj) {
	$("#expOriginatorUserName").val(userObj.username)
	$("#expOriginatorDisplayName").text(userObj.displayName)
}

function editExpenditure(expId) {
<c:if test="${not empty getExpenditureMapFn}">
	var expenditureObj = ${getExpenditureMapFn}()[expId]
	showExpenditureDetailsPopup(expenditureObj)
</c:if>
<c:if test="${empty param.getExpenditureMapFn}">
	alert('missing getExpenditureMapFn')
</c:if>
}

function newExpenditure(gpfId) {
	showExpenditureDetailsPopup(null, function() {
		if (gpfId)
			$("#expGPF").val(gpfId)
	})
}

function validateExpenditure() {
	var errors = new Array()
	
	if ($("#expTransactionId").val() == '' && $("#expPurchaseOrderId").val() == '')
		errors.push('Either Transaction # or Purchase Order # is required.')
	if ($('#expRequestDate').val() == '' || !validateDate($('#expRequestDate').val()))
		errors.push("Request Date is invalid.");
	if ($("#expGPF").val() == '')
		errors.push('General Post Fund is required.')
	if ($("#expOriginatorUserName").val() == '')
		errors.push('Please select a Purchase Requester.')
		
	var quantity = $("#expQuantity").val()
	if (quantity != '' && (!validateNumericWithoutCommas(quantity) || quantity <= 0))
		errors.push('Please enter a valid positive numeric quantity.')
	
	var amount = $("#expAmount").val()
	if (amount == '' || !validateNumericWithoutCommas(amount) || amount <= 0 || amount > 9999999.99)
		errors.push('A valid positive decimal amount is required.')
		
	if ($("#expDescription").val() == '')
		errors.push('Description is required.')
	
	if (errors.length > 0)
		displayAttentionDialog("Please correct the following errors: <ul><li>" + errors.join("</li><li>")
				+ "</li></ul>", null, null, {
			height : 230,
			width : 460
		});

	return errors.length == 0
}
	
function submitExpenditureDetails() {
	if (!validateExpenditure())
		return
		
	var donationSummaryIds = []
	for (var i = 0; i < donations.length; i++)
		donationSummaryIds.push(donations[i].id)
	
	$.ajax({
		url : ajaxHomePath + '/expenditure/saveOrUpdate',
		method: 'POST',
		dataType : 'json',
		data : {
			id: $("#expId").val(),
			transactionId : $("#expTransactionId").val(),
			purchaseOrder : $("#expPurchaseOrderId").val(),
			donGenPostFundId : $("#expGPF").val(),
			originatorUserName : $("#expOriginatorUserName").val(),
			requestDate : $("#expRequestDate").val(),
			amount : $("#expAmount").val(),
			vendor : $("#expVendor").val(),
			description : $("#expDescription").val(),
			comments : $("#expComments").val(),
			quantity : $("#expQuantity").val(),
			unit : $("#expUnit").val() == '-1' ? null : $("#expUnit").val(),
			unitPrice : $("#expUnitPrice").val(),
			donationSummaryIds : donationSummaryIds
		},
		error : commonAjaxErrorHandler,
		success : function(response) {
			$("#expDetailsDialog").dialog('close')
			${callbackMethod}()
	    }
	})
}
</script>

<style>
td.indent {
	min-width: 25px;
}

td.subhead {
	font-weight: bold;
}
</style>

<div id="expDetailsDialog" style="display: none"
	title="Expenditure Details">
	<input type="hidden" id="expId" value="" />
	<div class="serviceExpenditureInputFields">
		<table>
			<tr>
				<td class='subhead' colspan="7">Payment Information</td>
			</tr>
			<tr>
				<td rowspan="2" class="indent">&nbsp;</td>
				<td class='appFieldLabel' nowrap><label for='expTransactionId'>Transaction
						#:</label></td>
				<td></td>
				<td><input size="15" id="expTransactionId"
					class="expPopupInput" maxlength="20" /></td>
				<td align="right"><label for='expPurchaseOrderId'>Purchase Order #:</label></td>
				<td></td>
				<td><input size="15" maxlength="20" id="expPurchaseOrderId"
					style="margin-left: 8px" class="expPopupInput" /></td>
			</tr>
			<tr>
				<td class='appFieldLabel' nowrap><label for='expRequestDate'>Expense
						Date:</label><span class="invisibleRequiredFor508">*</span></td>
				<td><span class='requdIndicator'>*</span></td>
				<td><input size="15" id="expRequestDate"
					class="dateInput expPopupInput" /></td>
				<td class='appFieldLabel' nowrap><label for='expAmount'>
						Amount:</label><span class="invisibleRequiredFor508">*</span></td>
				<td><span class='requdIndicator'>*</span></td>
				<td>$<input size="10" id="expAmount" class="expPopupInput" /></td>
			</tr>
			<tr>
				<td class='subhead' colspan="7">General Post Fund</td>
			<tr>
				<td rowspan="1" class="indent">&nbsp;</td>
				<td class='appFieldLabel' nowrap><label for='expGPF'>GPF:</label><span
					class="invisibleRequiredFor508">*</span></td>
				<td><span class='requdIndicator'>*</span></td>
				<td colspan="4"><select id="expGPF" class="expPopupInput"></select>
				</td>
			</tr>
			<tr>
				<td class='subhead' colspan="7">Request</td>
			</tr>
			<tr>
				<td rowspan="5" class="indent">&nbsp;</td>
				<td class='appFieldLabel' nowrap><label for='expOriginator'>Purchase
						Requester:</label><span class="invisibleRequiredFor508">*</span></td>
				<td><span class='requdIndicator'>*</span></td>
				<td colspan="4"><span id="expOriginatorDisplayName"></span> <input
					type="hidden" id="expOriginatorUserName" /> <a
					href="javascript:popupAppUserSearch('${uid}_expenditure')"
					class="buttonAnchor expPopupInput">Search</a></td>
			</tr>
			<tr>
				<td class='appFieldLabel' nowrap><label for='expDescription'>Description:</label><span
					class="invisibleRequiredFor508">*</span></td>
				<td><span class='requdIndicator'>*</span></td>
				<td colspan="4"><textarea rows="4" cols="40"
						id="expDescription" class="expPopupInput" maxlength="1000"></textarea></td>
			</tr>
			<tr>
				<td class='appFieldLabel' nowrap><label for='expVendor'>Vendor:</label></td>
				<td></td>
				<td colspan="4"><input size="43" maxlength="50" id="expVendor"
					class="expPopupInput" /></td>
			</tr>
			<tr>
				<td class='appFieldLabel' nowrap><label for='expQuantity'>Qty
						Purchased:</label></td>
				<td></td>
				<td colspan="4"><input size="5" maxlength="3" id="expQuantity"
					class="expPopupInput" /> <label for='expUnit'>Unit:</label> <select
					id="expUnit">
						<option value="-1"></option>
						<c:forEach items="${allUnitTypes}" var="unitType">
							<option value="${unitType}" code="${unitType.code}"><c:out
									value="${unitType.name}" /></option>
						</c:forEach>
				</select> <label for='expUnitPrice'>Unit Price:</label> $<input size="8"
					id="expUnitPrice" class="expPopupInput" /></td>
			</tr>
			<tr>
				<td class='appFieldLabel' nowrap><label for='expComments'>Comments:</label></td>
				<td></td>
				<td colspan="4"><textarea rows="4" cols="40" id="expComments"
						class="expPopupInput" maxlength="1000"></textarea></td>
			</tr>
			<tr>
				<td class='subhead' colspan="7">Donation(s)</td>
			</tr>
			<tr>
				<td rowspan="2" class="indent">&nbsp;</td>
				<td colspan="2"></td>
				<td colspan="4"><a href="javascript:popupDonationSearch('expenditureDetail')"
					class="buttonAnchor expPopupInput">Add Donation</a></td>
			</tr>
			<tr>
				<td colspan="6">
					<table class="formatTable" id="expDonationTable" border="1"
						summary="List of Attached Donations">
						<thead>
							<tr>
								<th>ID</th>
								<th>Donor</th>
								<th>Date</th>
								<th>Amount</th>
								<th>Description</th>
								<c:if test="${not FORM_READ_ONLY}">
									<th>Action</th>
								</c:if>
							</tr>
						</thead>
						<tbody>
						</tbody>
					</table>
				</td>
			</tr>
		</table>
	</div>
</div>