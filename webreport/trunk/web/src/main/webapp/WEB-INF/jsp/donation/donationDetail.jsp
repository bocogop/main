<%@ include file="../shared/inc_header.jsp"%>

<script type="text/javascript">
	var donationSummaryId = ${command.donationSummary.persistent ? command.donationSummary.id : 'undefined'}
	var gpfNoneValue = "${GPF_NONE_VALUE}"
</script>
<script type="text/javascript" src="${jsHome}/donationDetail.js"></script>

<div id="donationDetailsDialog" style="display: none"
	title="Donation Detail">
	<input type="hidden" id="donationSummaryId" value="" />
	<div class="donationDetailInputFields donationDetailDisplay">
		<fieldset>
			<legend>Donation Detail</legend>
			<table>
				<tr>
					<td class='appFieldLabel' nowrap>General Post Fund:<span
						class="invisibleRequiredFor508">*</span></td>
					<td><span class='requdIndicator'>*</span></td>
					<td colspan="10"><select id="generalPostFund">
							<option value="">Please select...</option>
							<c:forEach items="${allDonGenPostFunds}" var="genPostFund">
								<option value="${genPostFund.id}"><c:out
										value="${genPostFund.generalPostFund}" /></option>
							</c:forEach>
					</select></td>
				</tr>
				<tr>
					<td class='appFieldLabel' nowrap>Donation Amount:<span
						class="invisibleRequiredFor508">*</span></td>
					<td><span class='requdIndicator'>*</span></td>
					<td><input type="text" id="donationValue" maxlength="13"
						class="currency" /></td>
				</tr>
			</table>
		</fieldset>
	</div>
</div>