<%@ include file="../shared/inc_header.jsp"%>

<script type="text/javascript">
	var isReadOnly = <c:out value="${FORM_READ_ONLY}" />
	var awardProcessedType = <c:out value="${awardCommand.awardsProcessed}" />

	$(function() {

		$('.dateInput').each(function() {
			$(this).enableDatePicker({
				showOn : "button",
				buttonImage : imgHomePath + "/calendar.gif",
				buttonImageOnly : true
			})
			$(this).mask(twoDigitDateMask)
		})

		var exportCols = [ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 ]
		if (!isReadOnly) exportCols.push(11)
		
		var theDataTable = $('#eligibleAwardResultsList').DataTable({
			buttons : [ {
				extend : 'excel',
				exportOptions : {
					columns : exportCols
				}
			}, {
				extend : 'pdfHtml5',
				orientation : 'landscape',
				exportOptions : {
					columns : exportCols
				}
			}, {
				extend : 'print',
				exportOptions : {
					columns : exportCols
				}
			} ],
			"dom" : '<"top"fBi>rt<"bottom"><"clear">',
			"order" : [],
			"paging" : false,			
			"columnDefs" : [{
				targets: isReadOnly ? [10] : [0, 11],
			     orderable: false
		    }] 
				
		})
		
		exportCols = [ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 ]
		
		theDataTable = $('#processedAwardResultsList').DataTable({
			buttons : [ {
				extend : 'excel',
				exportOptions : {
					columns : exportCols
				}
			}, {
				extend : 'pdfHtml5',
				orientation : 'landscape',
				exportOptions : {
					columns : exportCols
				}
			}, {
				extend : 'print',
				exportOptions : {
					columns : exportCols
				}
			} ],
			"dom" : '<"top"fBi>rt<"bottom"><"clear">',
			"order" : [],
			"paging" : false,			
			"columnDefs" : [{
				 targets:  [10],
			     orderable: false
		    }]  
		})


		$('#awardDate').enableDatePicker({
			showOn : "button",
			buttonImage : imgHomePath + "/calendar.gif",
			buttonImageOnly : true
		})
		$('#awardDate').mask("${TWO_DIGIT_DATE_MASK}", {
			autoclear : false
		})

		rebuildTableFilters("processedAwardResultsList")
		rebuildTableFilters("eligibleAwardResultsList")

		toggleDisplays()

		$(".dispositionInput").change(function() {
			awardProcessedType = $(".dispositionInput:checked").val()
			$("#includeAdult").prop('checked', true)
			$("#includeYouth").prop('checked', true)
			$("#includeOther").prop('checked', true)
			$("#includeActive").prop('checked', true)
			$("#includeSeparated").prop('checked',false) 
		
			toggleDisplays()
		})
	})

	function toggleDisplays() {
		$('.awardProcessed').toggle(awardProcessedType == 1);
		$('.awardPotential').toggle(awardProcessedType == 0);
	}
	
	function getDisplayPrefix() {
		return $('.awardPotential').eq(0).is(':visible') ? 'potential' : 'processed'
	}

	function printSelected() {
		var voterIds = new Array()
		$("input[name='" + getDisplayPrefix() + "VolAwardSelect']:checked").each(function(index, item) {
			voterIds.push($(item).val())
		})
		if (voterIds.length == 0) {
			displayAttentionDialog('Please select at least one label to print.')
			return		}
		
		if (voterIds.length > 990) {
			displayAttentionDialog('Please limit number of labels to print to 990 (33 pages worth of labels) at a time.')
			return
		}
		
		printIfNeeded(voterIds)
	}
	
	function printIfNeeded(voterIds) {
		var reportsToPrint = []
				
		var commonParams = {
			Username : "<c:out value="${username}" />",
			UserPasswordHash : "<c:out value="${userPasswordHash}" />",
			PrecinctContextId : "<c:out value="${siteContextId}" />"
		}
		
		reportsToPrint.push({
				reportName : 'Voter_AddressLabels_By_Vol_Id',
				reportOutputFormat : 'PDF',
				reportParams : $.extend({}, commonParams, {
					VolId : voterIds
				})
			})
		
			
		if (reportsToPrint.length > 0)
			printReports(reportsToPrint)
	}

	function setAllCheckboxes(isChecked) {
		$("input[name='" + getDisplayPrefix() + "VolAwardSelect']").prop('checked', isChecked)
	}

	function validateSearchForm() {
		var errors = new Array()
		
		if (!$(".dispositionInput").is(":checked")) {
			 erros.push("Disposition is required.")
		}
		if (awardProcessedType == 1) {
			if ($('#beginDateInput').val() != '' && !validateDate($('#beginDateInput').val())) {
				errors.push("Begin Date is invalid.")
			}
			
			if ($('#endDateInput').val() != '' && !validateDate($('#endDateInput').val())) {
				errors.push("End Date is invalid.")
			}
			
			if ($('#beginDateInput').val() != '' &&  $('#endDateInput').val() != '')  {
				var beginDate = getDateFromMMDDYYYY($('#beginDateInput').val())
				var endDate = getDateFromMMDDYYYY($('#endDateInput').val())
		        if (endDate < beginDate)	
		        	errors.push("End Date must be after Begin Date.")
			}
        	
		}
		
		if (errors.length > 0) {
			displayAttentionDialog("Please correct the following errors: <ul><li>"
					+ errors.join("</li><li>") + "</li></ul>");
		}
		
		return errors.length == 0
	}
	
	function validatePostForm() {
		var errors = new Array()
			
		if ($('#awardDate').val() != ''
				&& !validateDate($('#awardDate').val())) {
			errors.push("Award Date is invalid.")
		}
		
		if ($('#awardDate').val() == '')
			errors.push("Award Date is required.")
			
		var checkedVols = $('.awardVoterId:checked')
		if (checkedVols.length == 0 )
			errors.push("Please select at least 1 voter to receive award.")
		
		if (errors.length > 0) {
			displayAttentionDialog("Please correct the following errors: <ul><li>"
					+ errors.join("</li><li>") + "</li></ul>")
			return false
		}
		
		msg = '<span  class="redText">WARNING: This process will code the awards for the selected voters.' +
			' The process may take some time to post the awards and is an irreversible process.  Do you want to continue?</span>'
		confirmDialog(msg, function() {
			$('.awardVoterId:checked').each(function(index, item) {
				$("#awardForVolId" + $(item).attr('value')).prop('disabled', false)
			})
			
			$("#awardResultForm")[0].submit()
		 })
		 
		return false
	}
</script>

<style>
div.awardSearchFields {
	min-width: 480px;
	max-width: 1000px;
}
</style>

<form:form method="post" modelAttribute="awardCommand"
	action="${home}/awardSearchSubmit.htm" id="awardSearchForm"
	onsubmit="return validateSearchForm();">
	<div class="clearCenter awardSearchFields">
		<table align="center">
			<tr class="clearCenter">
				<td class='appFieldLabel' nowrap>Disposition:<span
					class="invisibleRequiredFor508">*</span></td>
				<td style="text-align: left"><span class='requdIndicator'>*</span></td>
				<td><label><form:radiobutton path="awardsProcessed"
							value="0" class="dispositionInput" />Eligible</label><span
					style="padding-left: 18px"><label><form:radiobutton
								path="awardsProcessed" value="1" class="dispositionInput" />Processed</label></span></td>
			</tr>

			<tr class="clearCenter">
				<td class='appFieldLabel' nowrap>Type:</td>
				<td></td>
				<td><label><form:checkbox path="includeAdult"
							id="includeAdult" />Adult</label> <span style="padding-left: 25px"><label><form:checkbox
								path="includeYouth" id="includeYouth" />Youth</label></span> <span
					class="awardProcessed" style="padding-left: 20px"><label>
							<form:checkbox path="includeOther" id="includeOther" />Other
					</label></span></td>
			</tr>
			<tr class="clearCenter">
				<td class='appFieldLabel' nowrap>Status:</td>
				<td></td>
				<td><label><form:checkbox path="includeActive"
							id="includeActive" />Active</label> <span style="padding-left: 20px"><label><form:checkbox
								path="includeSeparated" id="includeSeparated" />Separated</label></span></td>
			</tr>
			<tr class="awardProcessed" style="display: none">
				<td align="right">Begin Date:</td>
				<td></td>
				<td><app:input size="12" id="beginDateInput" path="startDate"
						cssClass="dateInput" /><br> <app:errors path="startDate"
						cssClass="msg-error" /></td>
			</tr>
			<tr class="awardProcessed" style="display: none">
				<td align="right">End Date:</td>
				<td></td>
				<td><app:input size="12" id="endDateInput" path="endDate"
						cssClass="dateInput" /><br> <app:errors path="endDate"
						cssClass="msg-error" /></td>
			</tr>
		</table>

		<div class="clearCenter" style="padding-top: 15px">
			<input id="submitButton" type="submit" value="Search" /> <span
				style="padding-left: 20px"><a id="cancelOperationBtn"
				class="buttonAnchor" href="${current_breadcrumb}">Cancel</a></span>
		</div>
	</div>

</form:form>

<form method="post" action="${home}/awardPostSubmit.htm"
	id="awardResultForm" onsubmit="return validatePostForm();">
	<c:if test="${not empty awardCommand.eligibleAwardResults }">
		<div class="clearCenter awardPotential" style="display: none">
			<table summary="Format table" align="center" width="100%">
				<tr>
					<td align="center">
						<table id="eligibleAwardResultsList" class="stripe"
							summary="List of Award">
							<thead>
								<tr>
									<c:if test="${not FORM_READ_ONLY}">
										<td class="noborder"></td>
									</c:if>
									<td class="noborder" title="Filter by Award Name"></td>
									<td class="noborder" title="Filter by Type"></td>
									<td class="noborder"></td>
									<td class="noborder" title="Filter by Status"></td>
									<td class="noborder" id="initiatorFilter"
										title="Filter Awards by last award date"></td>
									<td class="noborder"></td>
									<td class="noborder"></td>
									<td class="noborder"></td>
									<td class="noborder"></td>
									<td class="noborder"></td>
									<td class="noborder"></td>
								</tr>
								<tr>
									<c:if test="${not FORM_READ_ONLY}">
										<th>Give Award</th>
									</c:if>
									<th class="select-filter">Award Name</th>
									<th class="select-filter">Type</th>
									<th>Voter Name</th>
									<th class="select-filter">Status</th>
									<th>Last Award Date</th>
									<th>Last Award Hours</th>
									<th>Svc Yrs</th>
									<th>Total Hours</th>
									<th>Avg Hours/Mo</th>
									<th>Date Last Votered</th>
									<th align="center" nowrap class="tableHeaderLinkWrapper">Labels<br>
										<a class="tableHeaderLink"
										href="javascript:setAllCheckboxes(true)">All</a> / <a
										class="tableHeaderLink"
										href="javascript:setAllCheckboxes(false)">None</a></th>
								</tr>
							</thead>
							<tbody>
								<c:forEach var="awardResult"
									items="${awardCommand.eligibleAwardResults}">
									<c:set var="vol"
										value="${awardCommand.votersMap[awardResult.voterId]}" />

									<tr>
										<c:set var="checked" value="checked='checked'" />
										<c:if test="${vol.status.voterInactiveOrTerminated}">
											<c:set var="checked" value="" />
										</c:if>
										<c:if test="${not FORM_READ_ONLY}">
											<td><input type="checkbox" class="awardVoterId"
												name="awardVoterIds" value="${awardResult.voterId}"
												${checked} /> <input type="hidden"
												id="awardForVolId${awardResult.voterId}"
												name="awardForVolId${awardResult.voterId}"
												value="${awardResult.deservedAwardId}" disabled="disabled" /></td>
										</c:if>
										<td><c:out value="${awardResult.deservedAwardName}" /></td>
										<td><c:out value="${awardResult.awardType}" /></td>
										<td align="left"><a
											href="${home}/voterEdit.htm?id=${awardResult.voterId}"><c:out
													value="${vol.displayName}" /></a></td>
										<td><c:out value="${awardResult.volStatus}" /></td>
										<td align="right"><wr:localDate
												date="${awardResult.dateLastAward}"
												pattern="${TWO_DIGIT_DATE_ONLY}" /></td>
										<td align="right"><c:out
												value="${awardResult.hoursLastAward}" /></td>
										<td align="right"><c:out
												value="${awardResult.yearsWorked}" /></td>
										<td align="right"><c:out
												value="${awardResult.actualHours}" /></td>
										<td align="right"><c:out value="${awardResult.aveHours}" /></td>
										<td align="right"><wr:localDate
												date="${awardResult.dateLastVotered}"
												pattern="${TWO_DIGIT_DATE_ONLY}" /></td>
										<td align="center"><input type="checkbox"
											name="potentialVolAwardSelect" value="${vol.id}" /></td>
									</tr>
								</c:forEach>
							</tbody>
						</table>
					</td>
				</tr>
			</table>
			<div>
				<c:if test="${not FORM_READ_ONLY}">
					<table align="center" cellpadding="10">
						<tr class="awardPotential" style="display: none">
							<td class='appFieldLabel' nowrap>Award Date:<span
								class="invisibleRequiredFor508">*</span></td>
							<td style="text-align: left"><span class='requdIndicator'>*</span></td>
							<td><input size="15" id="awardDate" name="awardDate"
								value="${todayDate}" /></td>
						</tr>
					</table>
				</c:if>
				<div align="center" style="margin-top: 10px; margin-left: 15px">
					<c:if test="${not FORM_READ_ONLY}">
						<input id="submitButton" class="submitAnchor awardPotential"
							style="display: none" type="submit" value="Post Awards" />
					</c:if>
					<span style="padding-left: 20px"><a id="cancelPostAwardsBtn"
						class="buttonAnchor" href="${current_breadcrumb}">Cancel</a></span>
				</div>
				<div align="right" style="margin-top: -30px; margin-right: 15px">
					<a id="printSelectedButton" class="buttonAnchor"
						href="javascript:printSelected()">Print Labels</a>
				</div>
			</div>
		</div>
	</c:if>

	<c:if
		test="${awardCommand.awardsProcessed == 0 && empty awardCommand.eligibleAwardResults && awardCommand.eligibleSearched}">
		<table align="center" cellpadding="10">
			<tr>
				<td>Sorry, no awards were found that matched the specified
					criteria.</td>
			</tr>
		</table>
	</c:if>

	<!-- For Processed Awards List -->
	<c:if test="${not empty awardCommand.processedAwardResults}">
		<div class="clearCenter awardProcessed" style="display: none">
			<table summary="Format table" align="center" width="100%">
				<tr>
					<td align="center">
						<table id="processedAwardResultsList" class="stripe"
							summary="List of Award">
							<thead>
								<tr>
									<td class="noborder" title="Filter by Award Name"></td>
									<td class="noborder" title="Filter by Type"></td>
									<td class="noborder"></td>
									<td class="noborder" title="Filter by Status"></td>
									<td class="noborder" id="initiatorFilter"
										title="Filter Awards by last award date"></td>
									<td class="noborder"></td>
									<td class="noborder"></td>
									<td class="noborder"></td>
									<td class="noborder"></td>
									<td class="noborder"></td>
									<td class="noborder"></td>
								</tr>
								<tr>
									<th class="select-filter">Award Name</th>
									<th class="select-filter">Type</th>
									<th>Voter Name</th>
									<th class="select-filter">Status</th>
									<th>Last Award Date</th>
									<th>Last Award Hours</th>
									<th>Svc Yrs</th>
									<th>Total Hours</th>
									<th>Avg Hours/Mo</th>
									<th>Date Last Votered</th>
									<th align="center" nowrap class="tableHeaderLinkWrapper">Labels<br>
										<a class="tableHeaderLink"
										href="javascript:setAllCheckboxes(true)">All</a> / <a
										class="tableHeaderLink"
										href="javascript:setAllCheckboxes(false)">None</a></th>
								</tr>
							</thead>
							<tbody>
								<c:forEach var="awardResult"
									items="${awardCommand.processedAwardResults}">
									<c:set var="vol"
										value="${awardCommand.votersMap[awardResult.voterId]}" />

									<tr>
										<td><c:out value="${awardResult.currentAwardName}" /></td>
										<td><c:out value="${awardResult.awardType}" /></td>
										<td align="left"><a
											href="${home}/voterEdit.htm?id=${awardResult.voterId}"><c:out
													value="${vol.displayName}" /></a></td>
										<td><c:out value="${awardResult.volStatus}" /></td>
										<td align="right"><wr:localDate
												date="${awardResult.dateLastAward}"
												pattern="${TWO_DIGIT_DATE_ONLY}" /></td>
										<td align="right"><c:out
												value="${awardResult.hoursLastAward}" /></td>
										<td align="right"><c:out
												value="${awardResult.yearsWorked}" /></td>
										<td align="right"><c:out
												value="${awardResult.actualHours}" /></td>
										<td align="right"><c:out value="${awardResult.aveHours}" /></td>
										<td align="right"><wr:localDate
												date="${awardResult.dateLastVotered}"
												pattern="${TWO_DIGIT_DATE_ONLY}" /></td>
										<td align="center"><input type="checkbox"
											name="processedVolAwardSelect" value="${vol.id}" /></td>
									</tr>
								</c:forEach>
							</tbody>
						</table>
					</td>
				</tr>
			</table>

			<div align="center" style="margin-top: 10px; margin-left: 15px">
				<a id="cancelPostAwardsBtn" class="buttonAnchor"
					href="${current_breadcrumb}">Cancel</a></span>
			</div>
			<div align="right" style="margin-top: -30px; margin-right: 15px">
				<a id="printSelectedButton" class="buttonAnchor"
					href="javascript:printSelected()">Print Labels</a>
			</div>
		</div>
	</c:if>
	<c:if
		test="${awardCommand.awardsProcessed == 1 && empty awardCommand.processedAwardResults && awardCommand.processedSearched}">
		<table align="center" cellpadding="10">
			<tr>
				<td>Sorry, no awards were found that matched the specified
					criteria.</td>
			</tr>
		</table>
	</c:if>

	<input type="hidden" name="${_csrf.parameterName}"
		value="${_csrf.token}" />
</form>



