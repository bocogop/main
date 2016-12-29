<%@ include file="../shared/inc_header.jsp"%>

<c:if test="${FORM_READ_ONLY}">
	<%@ include file="../shared/inc_modifyToView.jsp"%>
</c:if>

<jsp:include page="/WEB-INF/jsp/shared/inc_donorSearchPopup.jsp">
	<jsp:param name="uniqueDonorSearchPopupId" value="eDonation" />
	<jsp:param name="mode" value="addEDonation" />
	<jsp:param name="resultCallbackMethod"
		value="eDonationDonorSelectedCallback" />
	<jsp:param name="disclaimerText"
		value="Please search for an existing donor before adding a new one:" />
	<jsp:param name="addButtonCallbackMethod"
		value="eDonationDonorAddCallback" />
</jsp:include>

<script type="text/javascript">
	var isReadOnly = ${FORM_READ_ONLY}

	<sec:authorize
	access="hasAnyAuthority('${PERMISSION_EDONATION_MANAGE}')"> 
	</sec:authorize>

	$(function() {
		buildDonationLogTable();
		refreshDonationLogTable()
	})
	
	var donationLogMap = new Array();
	
	function buildDonationLogTable() {
		var theDataTable = $('#donationLogList').DataTable({
			buttons: ['excel', {
				extend : 'pdfHtml5',
				orientation : 'landscape'
			}, 'print'],
			"columns" : [ {
		        	"render" : function(row, type, val, meta) {
						var donorNameEscaped = escapeHTML(val.name)
						return donorNameEscaped
		        	}
				}, 
				{
					"render" : function(row, type, val, meta) {
						var addressHtml = val.addressMultilineDisplay ? escapeHTML(val.addressMultilineDisplay) : ""					
						return addressHtml
					}
				}, {
					"render" : function(row, type, val, meta) {
						var phoneHtml = val.phone ? escapeHTML(val.phone) + '<br>' : "";
						var emailHtml = val.email ? escapeHTML(val.email) : "";
						return phoneHtml + emailHtml
					}
				},  {
					"render" : function(row, type, val, meta) {
						return escapeHTML(val.transactionDateOnly)
					} 
				},
				
				{
					"render" : function(row, type, val, meta) {
						return escapeHTML(val.donationAmount ? '$'+ formatAndAddThousandSeparators(val.donationAmount): '');
					} 

				}, {
					"render" : function(row, type, val, meta) {
						return escapeHTML(val.programField? val.programField : '')
					}
				
				}, {
					"render" : function(row, type, val, meta) {
						return escapeHTML(val.additionalInfo? val.additionalInfo : '')
					}
				
				}, {
					"render" : function(row, type, val, meta) {
						return escapeHTML(val.trackingId? val.trackingId : '')
					}
				
				}
				<c:if test="${not FORM_READ_ONLY}">
				, {
					"render" : function(row, type, val, meta) {
						var actions = '<div style="margin:0 auto; text-align:left"><nobr>'

						actions += '<a href="javascript:popupDonorSearch(\'eDonation\', undefined, {' 
							+ 'donationLogId : \'' + val.id   + '\', '	
							+ 'searchNameStr : \'' + val.name + '\', '	
							+ 'searchState : \'' + val.state + '\''
							+ '})">Add New Donation</a>'

						actions += '</nobr></div>'
						return actions
					}
				}
				</c:if>
			],
	    	"dom": '<"top"fBi>rt<"bottom"pl><"clear">',
	    	"lengthMenu" : [ [ 20, 50, -1 ],
	    	 				[ 20, 50, "All" ] ],
			"order": [],
	    	"pageLength": 20,
	    	"pagingType": "full_numbers",
	    	"stripeClasses" : [],
		})
		
	}
	
	function refreshDonationLogTable() {
		donationLogMap = new Object()
		
		$.ajax({
			url : ajaxHomePath + '/donationLogList',
			dataType : 'json',
			data : {
			},
			error : commonAjaxErrorHandler,
			success : function(response) {
				var donationLogs = response
				
				var rArray = new Array()
				
				for (var i = 0; i < donationLogs.length; i++) {
					var donationLog = donationLogs[i]
					donationLogMap[donationLog.id] = donationLog
					rArray[rArray.length] = donationLog
				}

				var table = $('#donationLogList').DataTable()
				table.clear()
				table.rows.add(rArray)
				rebuildTableFilters('donationLogList')
				table.draw()				
		    }
		})
	}
	
	function eDonationDonorSelectedCallback(donorObj, fullObj, donationLogId) {
		var hasDonor = (typeof donorObj !== 'undefined')
		var changedDonorId = hasDonor? donorObj.id : ''
			
		$.ajax({
				url : ajaxHomePath + '/donationLogIdSaveToSession',
				dataType : 'json',
				data : {
					donationLogId : donationLogId,
				},
				error : commonAjaxErrorHandler,
				success : function() {
					document.location.href = homePath + "/donationCreate.htm?donorId=" + changedDonorId
				}
		})
	}


	function eDonationDonorAddCallback(type, donationLogId) {
		$.ajax({
			url : ajaxHomePath + '/donationLogIdSaveToSession',
			dataType : 'json',
			data : {
				donationLogId : donationLogId,
			},
			error : commonAjaxErrorHandler,
			success : function() {
				document.location.href = homePath + '/donorCreate.htm?type=' + type;
			}
		})
	}
</script>

<style>
table#donationLogList tr.serviceRow {
	background-color: #dddddd;
}

table#donationLogList {
	border-collapse: collapse;
}

table#donationLogList td {
	margin: 3px;
}

#donationLogList {
	min-width: 400px;
}
</style>

<div id="donationLogWrapper" class="clearCenter" style="max-width: 75%">
	<fieldset>
		<legend>E-Donations Received</legend>

		<table class="formatTable" id="donationLogList" border="1"
			summary="List of E-Donations Received">
			<thead>
				<tr>
					<td class="noborder" width="25%"></td>
					<td class="noborder" width="50%"></td>
					<td class="noborder" width="7%"></td>
					<td class="noborder" width="7%"</td>
					<td class="noborder" width="7%"></td>
					<td class="noborder" width="7%"></td>
					<td class="noborder" width="7%"></td>
					<td class="noborder" width="7%"></td>
					<c:if test="${not FORM_READ_ONLY}">
						<td class="noborder" width="7%"></td>
					</c:if>
				</tr>
				<tr>
					<th>Donor Name</th>
					<th>Address</th>
					<th>Phone Email</th>
					<th>Date</th>
					<th>Amt</th>
					<th>Program (GPF)</th>
					<th>Additional Information</th>
					<th>Epay Tracking ID</th>
					<c:if test="${not FORM_READ_ONLY}">
						<th>Action</th>
					</c:if>
				</tr>
			</thead>
			<tbody>
			</tbody>
		</table>
	</fieldset>
</div>
