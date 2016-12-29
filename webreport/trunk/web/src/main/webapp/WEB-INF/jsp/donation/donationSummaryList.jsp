<%@ include file="../shared/inc_header.jsp"%>

<%@ include file="inc_printDonationSummaryPopup.jsp"%>

<script type="text/javascript">
	$(function() {
		$('select', '#statusFilter').val('Active')
		$('select', '#statusFilter').change()

		$('.dateInput').each(function() {
			$(this).enableDatePicker({
				showOn : "button",
				buttonImage : imgHomePath + "/calendar.gif",
				buttonImageOnly : true
			})
			$(this).mask(twoDigitDateMask)
		})

		var donationIdEditFn = function() {
			$(".nonIdField").prop('disabled', $(this).val() != '')
		}
		$("#donationIdInput").change(donationIdEditFn)
		$("#donationIdInput").keyup(donationIdEditFn)
		$("#donationIdInput").change()
		
		var theDataTable = $('#donationSummarySearchResultsList')
				.DataTable({
					buttons : [{
						extend : 'excel',
						exportOptions: {
							columns : [1,2,3,4,5,6,7,8,9,10,11]
						}
					}, {
						extend : 'pdfHtml5',
						orientation : 'landscape',
						exportOptions: {
							columns : [1,2,3,4,5,6,7,8,9,10,11]
						}
					}, {
						extend : 'print',
						exportOptions: {
							columns : [1,2,3,4,5,6,7,8,9,10,11]
						}
					} ],
						
						"columns" : [
								{
									"orderable" : false,
									"render" : function(data, type, full, meta) {
										if (full[3] == 'Individual' || full[3] == 'Organization' || full[3] == 'Anonymous')
											return '<input type="checkbox" name="donationSummarySelect" value="' + full[0] + '" />'
										return ''
									}
								},
								{
									"type" : "num",
									"render" : function(data, type, full, meta) {
										if (type === 'display') {
											return '<a class="appLink" href="' + homePath + '/donationEdit.htm?id='
													+ full[0] + '">' + getAsMMDDYYYY(data, '/') + '</a>'
										} else if (type === 'sort') {
											return data
										}
									}
								}, {
									"render" : function(data, type, full, meta) {
										return data
									}
								}, {
									"render" : function(data, type, full, meta) {
										return data
									}
								}, {
									"type" : "html",
									"render" : function(data, type, full, meta) {
										return data
									}
								}, {
									"render" : function(data, type, full, meta) {
										return data
									}
								}, {
									"render" : function(data, type, full, meta) {
										return data
									}
								}, {
									"render" : function(data, type, full, meta) {
										return data
									}
								}, {
									"render" : function(data, type, full, meta) {
										return data
									}
								}, {
									"render" : function(data, type, full, meta) {
										if (type === 'display') {
											return getAsMMDDYYYY(data, '/')
										} else if (type === 'sort') {
											return data
										} else if (type === 'filter') {
											return $.trim(data) == '' ? 'No' : 'Yes'
										}
									}
								}, {
									"render" : function(data, type, full, meta) {
										if (type === 'filter') {
											return abbreviate(data)
										} else {
											return data
										}
									}
								}, {
									"render" : function(data, type, full, meta) {
										return data
									}
								} ],
						"dom" : '<"top"fBi>rt<"bottom"><"clear">',
						"order" : [],
						"paging" : false
					})
		rebuildTableFilters('donationSummarySearchResultsList')
		
		<c:if test="${donationListCommand.mode == 'thankyou'}">
			$("select", "#acknowledgementFilter").val('No')
			theDataTable.column(6).visible(false)
			$("select", "#acknowledgementFilter").change()
		</c:if>
		
		<c:if test="${donationListCommand.mode != 'thankyou'}">
			<c:forEach items="${donationListCommand.donorTypes}" var="donorType">
				<c:if test="${donorType.lookupType.legacy}">
					showLegacyDonorTypes()
				</c:if>
			</c:forEach>
		</c:if>
	})
	
	function setAllCheckboxes(isChecked) {
		$("input[name='donationSummarySelect']").prop('checked', isChecked)
	}

	function showLegacyDonorTypes() {
		$("#legacyDonorTypesWrapper").show()
		$("#showLegacyLink").hide()
	}

	function printSelected() {
		var donationSummaryIds = new Array()
		$("input[name='donationSummarySelect']:checked").each(function(index, item) {
			donationSummaryIds.push($(item).val())
		})
		if (donationSummaryIds.length == 0) {
			displayAttentionDialog('Please select at least one donation.')
			return
		}
		showPrintDonationSummaryDialog(donationSummaryIds)		
	}
	
	function validateForm() {
		$("input").prop("disabled", false)
		showSpinner(null, true)
		return true
	}
</script>

<c:if test="${donationListCommand.mode == 'thankyou'}">
<style>
.hiddenThankYouModeFields {
	display: none;
}
</style>
</c:if>
<c:if test="${donationListCommand.mode != 'thankyou'}">
<style>
div.hiddenThankYouModeFields {
	display: inline-block;
}
tr.hiddenThankYouModeFields {
	display: table-row;
}
</style>
</c:if>

<form:form modelAttribute="donationListCommand" method="POST"
	action="${home}/donationList.htm" id="searchForm"
	onsubmit="return validateForm();">
	<div class="clearCenter">
		
		<div style="display: inline-block; vertical-align: top;" >
			<table>
				<tr>
					<td align="right">Begin Date:</td>
					<td><app:input size="12" id="beginDateInput" path="startDate"
							cssClass="dateInput nonIdField" /><br> <app:errors path="startDate"
							cssClass="msg-error" /></td>
				</tr>
				<tr>
					<td align="right">End Date:</td>
					<td><app:input size="12" id="endDateInput" path="endDate"
							cssClass="dateInput nonIdField" /><br> <app:errors path="endDate"
							cssClass="msg-error" /></td>
				</tr>
				<tr class="hiddenThankYouModeFields">
					<td align="right">Donor Name:</td>
					<td><app:input id="donorNameInput" path="donorName" cssClass="nonIdField" /><br>
						<app:errors path="donorName" cssClass="msg-error" /></td>
				</tr>
			</table>
		</div>
		<div style="vertical-align: top; margin-left: 30px;"
			class="hiddenThankYouModeFields">
			<table>
				<tr valign="top">
					<td nowrap>Donor Type(s):<br />
					<form:checkboxes items="${allCurrentDonorTypes}"
							path="donorTypes" itemLabel="donorType" itemValue="id" delimiter="<br>" cssClass="nonIdField" /><br>
						<a id="showLegacyLink" href="javascript:showLegacyDonorTypes()"
						class="appLink">[Show Legacy]</a>
						<div id="legacyDonorTypesWrapper" style="display: none">
							<form:checkboxes items="${allLegacyDonorTypes}" path="donorTypes"
								itemLabel="donorType" itemValue="id" delimiter="<br>" cssClass="nonIdField" />
						</div></td>
				</tr>
			</table>
		</div>
		
		<div style="vertical-align: top; margin-left: 30px;"
			class="hiddenThankYouModeFields">
			<table>
				<tr>
					<td nowrap>Acknowledgement:<br> <form:checkbox
							path="includeAcknowledged" cssClass="nonIdField" />Acknowledged<br> <form:checkbox
							path="includeUnacknowledged" cssClass="nonIdField" />Unacknowledged
					</td>
				</tr>
			</table>
		</div>
		<div style="vertical-align: top; margin-left: 20px; margin-top:4px" class="hiddenThankYouModeFields"><b>Or</b></div>
		<div class="hiddenThankYouModeFields" style="vertical-align: top; margin-left:20px">
			<table>
				<tr>
					<td>Donation ID:</td>
				</tr>
				<tr>
					<td><app:input id="donationIdInput" path="donationId" /><br>
						<app:errors path="donationId" cssClass="msg-error" /></td>
				</tr>
			</table>
		</div>
		
		<div class="clearCenter" style="padding-top: 15px">
			<input id="submitButton" type="submit" value="Submit" /> <a
				id="cancelOperationBtn" class="buttonAnchor"
				href="${current_breadcrumb}">Cancel</a>
		</div>
	</div>
	<c:if
		test="${donationListCommand.searched and not empty donationListCommand.donations}">
		<div align="left" style="margin-top: -30px; margin-left: 15px">
			<a id="printSelectedButton" class="buttonAnchor"
				href="javascript:printSelected()">Print Selected</a>
		</div>
	</c:if>
</form:form>

<style>
.ui-state-default a.tableHeaderLink, .ui-state-default a.tableHeaderLink:link,
	.ui-state-default a.tableHeaderLink:visited {
	text-decoration: underline;
	font-weight: normal;
}

table.dataTable thead th.tableHeaderLinkWrapper {
	font-weight: normal;
}
</style>

<c:if test="${donationListCommand.searched}">
	<c:if test="${maxResultsExceeded}">
		<div class="clearCenter" style="padding-top: 15px">The maximum
			number of search results was met. Please add more restrictive
			criteria and search again.</div>
		<p>
	</c:if>

	<c:if test="${not empty donationListCommand.donations}">
		<table summary="Format table" align="center" width="100%">
			<tr>
				<td align="center">
					<table id="donationSummarySearchResultsList" class="stripe"
						summary="List of Donations">
						<thead>
							<tr id="donationSummarySearchFilterRow">
								<td class="noborder"></td>
								<td class="noborder"></td>
								<td class="noborder" title="Filter by Donation Type"></td>
								<td class="noborder" title="Filter by Donor Type"></td>
								<td class="noborder"></td>
								<td class="noborder"></td>
								<td class="noborder"
									title="Filter by Organization or Other Groups"></td>
								<td class="noborder"></td>
								<td class="noborder"></td>
								<td class="noborder" id="acknowledgementFilter"
									title="Filter by Acknowledgement"></td>
								<td class="noborder" id="letterFilter"
									title="Filter by Letter Type"></td>
								<td class="noborder"></td>
							</tr>
							<tr>
								<th align="center" nowrap class="tableHeaderLinkWrapper">Select<br>
									<a class="tableHeaderLink"
									href="javascript:setAllCheckboxes(true)">All</a> / <a
									class="tableHeaderLink"
									href="javascript:setAllCheckboxes(false)">None</a></th>
								<th>Date</th>
								<th class="select-filter">Type</th>
								<th class="select-filter">Donor Type</th>
								<th>Donor Name</th>
								<th>Affiliation</th>
								<th class="select-filter">Org / Other Groups</th>
								<th>Description</th>
								<th>Value</th>
								<th class="select-filter">Ack Date</th>
								<th class="select-filter">Letter</th>
								<th>Id</th>
							</tr>
						</thead>

						<c:forEach var="donationSummary"
							items="${donationListCommand.donations}">
							<tr>
								<td align="center">${donationSummary.id}</td>
								<td><wr:localDate date="${donationSummary.donationDate}"
										pattern="${MSD_TO_LSD}" /></td>
								<td><c:out
										value="${donationSummary.donationType.donationType}" /></td>
								<td><c:out
										value="${donationSummary.donor.donorType.donorType}" /></td>
								<td><a class="appLink"
									href="${home}/donorEdit.htm?id=${donationSummary.donor.id}"><c:out
											value="${donationSummary.donor.individualName}" /></a></td>
								<td><c:out value="${donationSummary.organization.displayName}" /></td>
								<td><c:if
										test="${empty donationSummary.donor.individualName}">
										<a class="appLink"
											href="${home}/donorEdit.htm?id=${donationSummary.donor.id}">
									</c:if>
									<c:out value="${donationSummary.donor.otherGroupName}" />
									<c:if test="${empty donationSummary.donor.individualName}">
										</a>
									</c:if></td>
								<td><c:out value="${donationSummary.donationDescription}" /></td>
								<td><fmt:formatNumber type="currency" minFractionDigits="2"
										maxFractionDigits="2"
										value="${donationSummary.totalDonationAmount}" /></td>
								<td><wr:localDate
										date="${donationSummary.acknowledgementDate}"
										pattern="${MSD_TO_LSD}" /></td>
								<td><c:out value="${donationSummary.letterType.name}" /></td>
								<td><c:out value="${donationSummary.id}" /></td>
							</tr>
						</c:forEach>
					</table>
				</td>
			</tr>
		</table>
		<div align="left" style="margin-top: 10px; margin-left: 15px">
			<a id="printSelectedButton" class="buttonAnchor"
				href="javascript:printSelected()">Print Selected</a>
		</div>
	</c:if>
	<c:if test="${empty donationListCommand.donations}">
		<table align="center" cellpadding="10">
			<tr>
				<td>Sorry, no donations were found that matched the specified
					criteria.</td>
			</tr>
		</table>
	</c:if>
</c:if>
