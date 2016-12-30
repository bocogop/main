<%@ include file="../shared/inc_header.jsp"%>

<script type="text/javascript">
$(function() {
	$("#printDonationSummaryPopup").dialog({
		autoOpen : false,
		modal : false,
		width : 300,
		height : 230,
		closeOnEscape : true,
		draggable : true,
		resizable : true,
		buttons : {
			'Submit' : function() {
				var isReceipt = $("#isPrintReceipt").is(":checked")
				var isMemo = $("#isPrintMemo").is(":checked")
				var isThankYou = $("#isPrintThankYou").is(":checked")
				var format = $("input[name='isPrintFormat']:checked").val()
				
				if (!isReceipt && !isMemo && !isThankYou) {
					displayAttentionDialog('Please select at least one item to print.')
					return
				}
				$(this).dialog('close')
				$("#printDonationSummaryPopup").data('callback')(isReceipt, isMemo, isThankYou, format)
			},
			'Cancel' : function() {
				$(this).dialog('close')
			}
		}
	})
})
function showPrintDonationSummaryDialogWithCallback(callback) {
	$("#printDonationSummaryPopup").data('callback', callback)
	$(".printDonationSummaryItem").prop('checked', false)
	$("#isPrintFormatPDF").prop('checked', true)
	
	$("#printDonationSummaryPopup").dialog('open')
}
function showPrintDonationSummaryDialog(donationSummaryId) {
	showPrintDonationSummaryDialogWithCallback(function(isReceipt, isMemo, isThankYou, format) {
		printIfNeeded(isReceipt ? donationSummaryId : null, isMemo ? donationSummaryId : null,
				isThankYou ? donationSummaryId : null, format)
	})
}

function printIfNeeded(printReceiptId, printMemoId, printThankYouId, printFormat) {
	var reportsToPrint = []
	var skip
	
	var commonParams = {
		Username : "<c:out value="${username}" />",
		UserPasswordHash : "<c:out value="${userPasswordHash}" />",
		PrecinctContextId : "<c:out value="${siteContextId}" />"
	}
	
	skip = (printReceiptId == null) ||
		($.isArray(printReceiptId) && printReceiptId.length == 0) ||
		(!$.isArray(printReceiptId) && printReceiptId <= 0)
	if (!skip)		
		reportsToPrint.push({
			reportName : 'Receipt',
			reportOutputFormat : printFormat,
			reportParams : $.extend({}, commonParams, {
				DonationSummaryID : printReceiptId
			})
		})
	
	skip = (printThankYouId == null) ||
		($.isArray(printThankYouId) && printThankYouId.length == 0) ||
		(!$.isArray(printThankYouId) && printThankYouId <= 0)
	if (!skip)		
		reportsToPrint.push({
			reportName : 'ThankYouLetter',
			reportOutputFormat : printFormat,
			reportParams : $.extend({}, commonParams, {
				DonationSummaryID : printThankYouId
			})
		})

	skip = (printMemoId == null) ||
		($.isArray(printMemoId) && printMemoId.length == 0) ||
		(!$.isArray(printMemoId) && printMemoId <= 0)
	if (!skip)		
		reportsToPrint.push({
			reportName : 'Memorandum',
			reportOutputFormat : printFormat,
			reportParams : $.extend({}, commonParams, {
				DonationSummaryID : printMemoId
			})
		})
	
	if (reportsToPrint.length > 0)
		printReports(reportsToPrint)
}
</script>

<div id="printDonationSummaryPopup" style="display: none"
	title="Print Donation Documents">
	<div align="center">Please select which items to print:</div>
	<p>
	<table>
		<tr>
			<td align="right"><input type="checkbox" class="printDonationSummaryItem"
				id="isPrintReceipt" value="true" /></td>
			<td>Receipt</td>
			<td rowspan="3" width="20">&nbsp;</td>
			<td><input type="radio" name="isPrintFormat"
				id="isPrintFormatPDF" value="PDF" checked="checked" />PDF</td>
		</tr>
		<tr>
			<td align="right"><input type="checkbox" class="printDonationSummaryItem" id="isPrintMemo"
				value="true" /></td>
			<td>Memo</td>
			<td><input type="radio" name="isPrintFormat"
				id="isPrintFormatWord" value="Word" />Word</td>
		</tr>
		<tr>
			<td align="right"><input type="checkbox" class="printDonationSummaryItem"
				id="isPrintThankYou" value="true" /></td>
			<td>Thank You Letter</td>
		</tr>
	</table>
</div>