<%@ include file="../shared/inc_header.jsp"%>

<c:set var="callbackMethod"
	value="${fn:escapeXml(param.resultCallbackMethod)}" />
<c:set var="getAdjustmentMapFn"
	value="${fn:escapeXml(param.getAdjustmentMapFn)}" />

<c:set var="getGPFBalanceMapFn"
	value="${fn:escapeXml(param.getGPFBalanceMapFn)}" />
<c:if test="${empty getGPFBalanceMapFn}">
	<c:set var="getGPFBalanceMapFn"
		value="defaultGetGPFBalanceMapForAdjustment" />
</c:if>

<script type="text/javascript">

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
	$("#adjDetailsDialog").dialog({
		autoOpen : false,
		modal : true,
		width : 650,
		height : 350,
		closeOnEscape : false,
		draggable : true,
		resizable : true,
		buttons : {
			'Submit' : {
				text : 'Submit',
				id : 'adjustmentDetailDialogSubmitBtn',
				click : submitAdjustmentDetails
			},
			'Cancel' : function() {
				$(this).dialog('close')
			},
		}
	})
	
	$("#adjDetailsDialog").show()
	/*
	 * Required to solve 508 issue not reading dialog box title. It is also
	 * required to disable dialog animation to enable this functionality
	 */
	$('#adjDetailsDialog').focus()
	
	$('#adjDetailsDialog .dateInput').each(function() {
		$(this).enableDatePicker({
			showOn : "button",
			buttonImage : imgHomePath + "/calendar.gif",
			buttonImageOnly : true
		})
		$(this).mask(twoDigitDateMask)
	})
})

function showAdjustmentDetailsPopup(adjustment, postShowCallback) {
	var doIt = function(gpfBalances) {
		var isDisabled = <c:out value="${FORM_READ_ONLY}" default="false" />
		var hasAdjustment = adjustment !== null
		$("#adjId").val(hasAdjustment ? adjustment.id : '')
		
		$("#adjGPF").val(hasAdjustment && adjustment.donGenPostFund ? adjustment.donGenPostFund.id : '')
		
		$(".adjPopupInput").prop('disabled', hasAdjustment && isDisabled)
		
		$("#adjGPF").empty()
		$("#adjGPF").append('<option value="">Please select...</option>')
		for (var i = 0; i < sortedGPFs.length; i++) {
			$("#adjGPF").append(
					$('<option value="' + sortedGPFs[i].id + '"'
					+ (hasAdjustment && adjustment.donGenPostFund.id == sortedGPFs[i].id ? ' selected="selected"' : '')
					+ '></option>').text(sortedGPFs[i].name
					+ ' (balance $' + formatAndAddThousandSeparators(gpfBalances[sortedGPFs[i].id]) + ')'))
		}
		
		$("#adjOriginatorUserName").val(hasAdjustment && adjustment.originator ? adjustment.originator.username : '')
		$("#adjOriginatorDisplayName").text(hasAdjustment && adjustment.originator ? adjustment.originator.displayName : '')
		$("#requesterRow").toggle(hasAdjustment)
		
		$("#adjRequestDate").val(hasAdjustment ? adjustment.requestDate : getDateAsMMDDYYYY(new Date()))
		$("#adjAmount").val(hasAdjustment ? adjustment.amount : '')
		$("#adjJustification").val(hasAdjustment ? adjustment.justification : '')
		
		$("#adjDetailsDialog").dialog('open')
		if (postShowCallback)
			postShowCallback()
	}	
	
	${getGPFBalanceMapFn}(doIt)
}

function defaultGetGPFBalanceMapForAdjustment(callback) {
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

function editAdjustment(adjId) {
<c:if test="${not empty param.getLedgerAdjustmentMapFn}">
	var adjustmentObj = ${param.getLedgerAdjustmentMapFn}()[adjId]
	showAdjustmentDetailsPopup(adjustmentObj)
</c:if>
<c:if test="${empty param.getLedgerAdjustmentMapFn}">
	alert('missing param getLedgerAdjustmentMapFn')
</c:if>
}

function newAdjustment(gpfId) {
	showAdjustmentDetailsPopup(null, function() {
		if (gpfId)
			$("#adjGPF").val(gpfId)
	})
}

function validateAdjustment() {
	var errors = new Array()
	
	if ($('#adjRequestDate').val() == '' || !validateDate($('#adjRequestDate').val()))
		errors.push("Request Date is invalid.");
	
	var amount = $("#adjAmount").val()
	if (amount == '' || !validateNumericWithoutCommas(amount, true) || amount > 9999999.99)
		errors.push('A valid decimal amount is required.')
		
	if ($("#adjJustification").val() == '')
		errors.push('Justification is required.')
	
	if (errors.length > 0)
		displayAttentionDialog("Please correct the following errors: <ul><li>" + errors.join("</li><li>")
				+ "</li></ul>", null, null, {
			height : 230,
			width : 460
		});

	return errors.length == 0
}
	
function submitAdjustmentDetails() {
	if (!validateAdjustment())
		return
		
	$.ajax({
		url : ajaxHomePath + '/ledgerAdjustment/saveOrUpdate',
		method: 'POST',
		dataType : 'json',
		data : {
			id: $("#adjId").val(),
			requestDate : $("#adjRequestDate").val(),
			donGenPostFundId : $("#adjGPF").val(),
			amount : $("#adjAmount").val(),
			justification : $("#adjJustification").val()
		},
		error : commonAjaxErrorHandler,
		success : function(response) {
			$("#adjDetailsDialog").dialog('close')
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

<div id="adjDetailsDialog" style="display: none"
	title="Adjustment Details">
	<input type="hidden" id="adjId" value="" />
	<div class="serviceAdjustmentInputFields">
		<table>
			<tr>
				<td class='subhead' colspan="7">Adjustment Information</td>
			</tr>
			<tr>
				<td rowspan="1" class="indent">&nbsp;</td>
				<td class='appFieldLabel' nowrap><label for='adjRequestDate'>Request
						Date:</label><span class="invisibleRequiredFor508">*</span></td>
				<td><span class='requdIndicator'>*</span></td>
				<td><input size="15" id="adjRequestDate"
					class="dateInput adjPopupInput" /></td>
				<td class='appFieldLabel' nowrap><label for='adjAmount'>
						Amount:</label><span class="invisibleRequiredFor508">*</span></td>
				<td><span class='requdIndicator'>*</span></td>
				<td>$<input size="10" id="adjAmount" class="adjPopupInput" /></td>
			</tr>
			<tr>
				<td class='subhead' colspan="7">General Post Fund</td>
			</tr>
			<tr>
				<td rowspan="1" class="indent">&nbsp;</td>
				<td class='appFieldLabel' nowrap><label for='adjGPF'>GPF:</label><span
					class="invisibleRequiredFor508">*</span></td>
				<td><span class='requdIndicator'>*</span></td>
				<td colspan="4"><select id="adjGPF" class="adjPopupInput"></select>
				</td>
			</tr>
			<tr>
				<td class='subhead' colspan="7">Request</td>
			</tr>
			<tr id="requesterRow">
				<td class="indent">&nbsp;</td>
				<td class='appFieldLabel' nowrap><label for='adjOriginator'>
						Requester:</label></td>
				<td></td>
				<td colspan="4"><span id="adjOriginatorDisplayName"></span> <input
					type="hidden" id="adjOriginatorUserName" /></td>
			</tr>
			<tr>
				<td class="indent">&nbsp;</td>
				<td class='appFieldLabel' nowrap><label for='adjComments'>Justification:</label><span
					class="invisibleRequiredFor508">*</span></td>
				<td><span class='requdIndicator'>*</span></td>
				<td colspan="4"><textarea rows="4" cols="40"
						id="adjJustification" class="adjPopupInput" maxlength="1000"></textarea></td>
			</tr>
		</table>
	</div>
</div>