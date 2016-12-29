<%@ include file="../shared/inc_header.jsp"%>

<script type="text/javascript">
$(function() {
	var submitParkingStickerDetails = function() {
		var myParkingStickerId = $("#parkingStickerId").val()
		
		if ($.trim($("#parkingStickerNumber").val()) == '') {
			displayAttentionDialog('Please enter a parking sticker number.')
			return
		}
		
		$.ajax({
			url : ajaxHomePath + '/volunteer/parkingSticker/createOrUpdate',
			method: 'POST',
			dataType : 'json',
			data : {
				volunteerId : volunteerId,
				facilityId : $("#parkingStickerFacilityId").val(),
				parkingStickerId: myParkingStickerId,
				number: $("#parkingStickerNumber").val(),
				state: $("#parkingStickerState").val(),
				licensePlate: $("#parkingStickerLicensePlate").val()
			},
			error : commonAjaxErrorHandler,
			success : function(response) {
				$("#parkingStickerDetailsDialog").dialog('close')
				refreshParkingStickers()
		    }
		})
	}
	
	var dialogEl = $("#parkingStickerDetailsDialog")
	dialogEl.dialog({
		autoOpen : false,
		modal : false,
		width : 700,
		height : 300,
		closeOnEscape : true,
		draggable : true,
		resizable : true,
		buttons : {
			'Submit' : {
				text : 'Submit',
				id : 'parkingStickerSubmit',
				click : function() {
					var facilityId = $("#parkingStickerFacilityId").val()
					var facilityText = $("#parkingStickerFacilityId option:selected").text();
					if (!hasActiveAssignmentAtFacility(facilityId)) {
						confirmDialog('This volunteer does not have an active assignment at ' + facilityText
								+ ', do you wish to continue?', submitParkingStickerDetails)
					} else {
						submitParkingStickerDetails()
					}
				}
			},
			'Cancel' : function() {
				$(this).dialog('close')
			}
		}
	})
	
	var facilityEl = $("#parkingStickerFacilityId")
	facilityEl.multiselect({
		selectedText : function(numChecked, numTotal, checkedItems) {
			return abbreviate($(checkedItems[0]).next().text())
		},
		beforeopen: function(){
			if (dialogEl.data('stationsPopulated')) return
			var curVal = facilityEl.val()
			
			$.ajax({
				url : ajaxHomePath + "/getFacilitiesWithUserPermission",
				type : "POST",
				data : {
					permission : "<c:out value="${PERMISSION_TYPE_VOLUNTEER_CREATE}" />",
					activeStatus: true
				},
				dataType : 'json',
				error : commonAjaxErrorHandler,
				success : function(results) {
					facilityEl.empty()
					var newHtml = []
					$.each(results, function(index, item) {
						if (item.id == centralOfficeId) return
						
						var selectedText = (item.id == curVal) ? ' selected="selected"' : ''
						newHtml.push('<option value="' + item.id + '"' + selectedText + '>' + item.displayName + '</option>')
					})
					facilityEl.html(newHtml.join(''))
					
					facilityEl.val(curVal)
					facilityEl.multiselect("refresh")
					dialogEl.data('stationsPopulated', true)
					
					facilityEl.multiselect("open")
				}
			})
			
			return false
	   },
		multiple : false,
		minWidth : 400
	}).multiselectfilter()
	
	$("#parkingStickerDetailsDialog").show()
})

function showParkingStickerDetailsPopup(parkingStickerId) {
	var isDisabled = <c:out value="${FORM_READ_ONLY}" default="false" />
	
	var parkingStickerObj = parkingStickerId ? parkingStickerData[parkingStickerId] : null
	var hasParkingStickerObj = parkingStickerObj != null
	
	$("#parkingStickerId").val(hasParkingStickerObj ? parkingStickerObj.id : '')
	if ($("#parkingStickerDetailsDialog").data('stationsPopulated')) {
		$("#parkingStickerFacilityId").val(hasParkingStickerObj ? parkingStickerObj.facility.id : "${facilityContextId}")
	} else {
		$("#parkingStickerFacilityId").empty().append(
				$("<option />")
					.prop("selected", true)
					.attr("value", hasParkingStickerObj ? parkingStickerObj.facility.id : "${facilityContextId}")
					.text(hasParkingStickerObj ? parkingStickerObj.facility.displayName : "${facilityContextName}"))
	}
	$("#parkingStickerFacilityId").multiselect("refresh")
	
	$("#parkingStickerNumber").val(hasParkingStickerObj ? parkingStickerObj.stickerNumber : '')
	$("#parkingStickerState").val(hasParkingStickerObj && parkingStickerObj.state ? parkingStickerObj.state.id : '')
	$("#parkingStickerLicensePlate").val(hasParkingStickerObj ? parkingStickerObj.licensePlate : '')
	$(".parkingStickerPopupInputs").prop('disabled', isDisabled)
	$("#parkingStickerDetailsDialog").dialog('open')
}
</script>

<div id="parkingStickerDetailsDialog" style="display: none"
	title="Parking Sticker Details">
	<input type="hidden" id="parkingStickerId" value="" />

	<div class="parkingStickerInputFields">
		<fieldset>
			<legend>Parking Sticker Information</legend>
			<table>
				<tr>
					<td class='appFieldLabel'>Facility:<span
						class="invisibleRequiredFor508">*</span></td>
					<td><span class='requdIndicator'>*</span></td>
					<td><div id="parkingStickerFacilityWrapper">
								<select id="parkingStickerFacilityId"></select>
							</div></td>
				</tr>
				<tr>
					<td class='appFieldLabel'>Sticker Number:<span
						class="invisibleRequiredFor508">*</span></td>
					<td><span class='requdIndicator'>*</span></td>
					<td><input type="text" id="parkingStickerNumber"
						class="parkingStickerPopupInputs" maxlength="13" /></td>
				</tr>
				<tr>
					<td class='appFieldLabel'>State:</td>
					<td></td>
					<td><select id="parkingStickerState"
						class="parkingStickerPopupInputs">
							<option value="">Please select...</option>
							<c:forEach items="${allStates}" var="state">
								<option value="${state.id}"><c:out
										value="${state.displayName}" /></option>
							</c:forEach>
					</select></td>
				</tr>
				<tr>
					<td class='appFieldLabel'>License Plate:</td>
					<td></td>
					<td><input type="text" id="parkingStickerLicensePlate"
						class="parkingStickerPopupInputs" maxlength="12" /></td>
				</tr>
			</table>
		</fieldset>
	</div>
</div>