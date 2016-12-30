<%@ include file="shared/inc_header.jsp"%>

<script type="text/javascript">
	function submitForm(evt) {
		if ($('#stationCode').val() == -99 || $('#stationCode').val() == "" ||
				$('#stationCode').val() == null) {
			displayAttentionDialog('Please select a precinct before proceeding!');
			return
		}
		
		if (evt != null)
			doubleClickSafeguard($(evt.currentTarget))
		$('#stationSettingsForm').submit()
	}
	
	$(function() {
		var stationSelect = $('#stationCode')[0];
		var myfilter = new filterlist(stationSelect);
		
		$("#submitFormButton").click(submitForm)
		
		$('#selectedStation').bind('keyup', function(){
			var typedInVal = $(this).val();
			myfilter.set(typedInVal);
		})
		
		$("#stationCode").unbind('dblclick')
		$("#stationCode").dblclick(function () {
			// $("#list1").find('option:selected').each(function() {
				//put your code here whatever needed on double click
			// })
			submitForm()
		})
	})
</script>

<form:form id="stationSettingsForm" name='stationSettingsForm'
	method="POST" action="${home}/selectStation.htm">
	
	<table cellpadding="8" align="center">
		<tr valign="top">
			<td align="right" class="appFieldLabel">Last Visited Precinct:</td>
			<td>
				<table cellpadding="3">
					<tr>
						<td align="right"><i>Name:</i></td>
						<td align="left"><c:out value="${currentStationName}" /></td>
					</tr>
					<c:if test="${not empty currentStationParentName}">
						<tr>
							<td align="right"><i>Parent:</i></td>
							<td align="left"><c:out value="${currentStationParentName}" /></td>
						</tr>
					</c:if>
					<c:if test="${not empty currentStationVisnName}">
						<tr>
							<td align="right"><i>Visn:</i></td>
							<td align="left"><c:out value="${currentStationVisnName}" /></td>
						</tr>
					</c:if>
				</table>
			</td>
		</tr>
		<tr valign="top">
			<td align="right" class="appFieldLabel"><label
				for='selectedStation'>Select Precinct: <span
					class='requdIndicator'>*</span>
			</label></td>

			<td><input id='selectedStation' size="50"
				title="Enter some text here to filter the Precinct List"><br />
				<i>(Enter some text to filter the list below)</i> <label
				for='hiddenCancelSubmit' style='display: none;'>(Enter some
					text to filter the list below)</label><input type='text'
				id='hiddenCancelSubmit' style='display: none;'></input></td>

		</tr>
		<tr align="center">
			<td><label for='stationCode' style='display: none;'>Select
					a precinct</label></td>
			<td align='left'><select name="stationCode" id="stationCode"
				size='10'
				ondblclick='document.getElementById("stationSettingsForm").submit();'>
					<option value="-99">Select a Precinct..</option>
					<c:forEach items="${precinctList}" var="precinct">
						<c:choose>
							<c:when
								test="${not empty currentStationId and fn:trim(currentStationId) == fn:trim(precinct.id)}">
								<option value="${precinct.id}" selected="selected">
									<c:out value="${precinct.displayName}" />
								</option>
							</c:when>
							<c:otherwise>
								<option value="${precinct.id}">
									<c:out value="${precinct.displayName}" />
								</option>
							</c:otherwise>
						</c:choose>
					</c:forEach>
			</select><br> <app:errors path="institution"
					cssClass="msg-error" /></td>
		</tr>
		<tr align="center">
			<td colspan="2"><a class="buttonAnchor" id="submitFormButton"
				tabIndex="0">Submit</a> <c:if test="${cancelAllowed}">
					<a class="buttonAnchor" id="changeStationCancelButton"
						href="${siteMapUrl}" tabIndex="0">Cancel</a>
				</c:if> </td>
		</tr>
	</table>
</form:form>