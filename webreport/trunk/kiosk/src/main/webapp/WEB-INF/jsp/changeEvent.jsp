<%@ include file="shared/inc_header.jsp"%>

<script type="text/javascript">
	function submitForm(evt) {
		if ($('#eventId').val() == "" ||
				$('#eventId').val() == null) {
			displayAttentionDialog('Please select a event before proceeding!');
			return
		}
		
		if (evt != null)
			doubleClickSafeguard($(evt.currentTarget))
		$('#eventSettingsForm').submit()
	}
	
	$(function() {
		var eventSelect = $('#eventId')[0];
		var myfilter = new filterlist(eventSelect);
		
		$("#submitFormButton").click(submitForm)
		
		$('#selectedEvent').bind('keyup', function(){
			var typedInVal = $(this).val();
			myfilter.set(typedInVal);
		})
		
		$("#eventId").unbind('dblclick')
		$("#eventId").dblclick(function () {
			// $("#list1").find('option:selected').each(function() {
				//put your code here whatever needed on double click
			// })
			submitForm()
		})
	})
</script>

<form:form id="eventSettingsForm" name='eventSettingsForm'
	method="POST" action="${home}/selectEvent.htm">
	
	<table cellpadding="8" align="center">
		<tr valign="top">
			<td align="right" class="appFieldLabel"><label
				for='selectedEvent'>Select Event: <span
					class='requdIndicator'>*</span>
			</label></td>

			<td><input id='selectedEvent' size="50"
				title="Enter some text here to filter the Event List"><br />
				<i>(Enter some text to filter the list below)</i> <label
				for='hiddenCancelSubmit' style='display: none;'>(Enter some
					text to filter the list below)</label><input type='text'
				id='hiddenCancelSubmit' style='display: none;'></input></td>

		</tr>
		<tr align="center">
			<td><label for='eventId' style='display: none;'>Select
					a event</label></td>
			<td align='left'><select name="eventId" id="eventId"
				size='10'
				ondblclick='document.getElementById("eventSettingsForm").submit();'>
					<c:forEach items="${eventList}" var="event">
						<c:choose>
							<c:when
								test="${not empty currentEventId and fn:trim(currentEventId) == fn:trim(event.id)}">
								<option value="${event.id}" selected="selected">
									<wr:localDate date="${event.date}" /> - <c:out value="${event.name}" />
								</option>
							</c:when>
							<c:otherwise>
								<option value="${event.id}">
									<c:out value="${event.date}" /> - <c:out value="${event.name}" />
								</option>
							</c:otherwise>
						</c:choose>
					</c:forEach>
			</select></td>
		</tr>
		<tr align="center">
			<td colspan="2"><a class="buttonAnchor" id="submitFormButton"
				tabIndex="0">Submit</a>  </td>
		</tr>
	</table>
</form:form>