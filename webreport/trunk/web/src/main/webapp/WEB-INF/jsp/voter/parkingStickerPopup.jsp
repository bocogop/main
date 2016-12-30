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
			url : ajaxHomePath + '/voter/parkingSticker/createOrUpdate',
			method: 'POST',
			dataType : 'json',
			data : {
				voterId : voterId,
				precinctId : $("#parkingStickerPrecinctId").val(),
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
					var precinctId = $("#parkingStickerPrecinctId").val()
					var precinctText = $("#parkingStickerPrecinctId option:selected").text();
					if (!hasActiveAssignmentAtPrecinct(precinctId)) {
						confirmDialog('This voter does not have an active assignment at ' + precinctText
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
	
	var precinctEl = $("#parkingStickerPrecinctId")
	precinctEl.multiselect({
		selectedText : function(numChecked, numTotal, checkedItems) {
			return abbreviate($(checkedItems[0]).next().text())
		},
		beforeopen: function(){
			if (dialogEl.data('stationsPopulated')) return
			var curVal = precinctEl.val()
			
			$.ajax({
				url : ajaxHomePath + "/getPrecinctsWithUserPermission",
				type : "POST",
				data : {
					permission : "<c:out value="${PERMISSION_TYPE_VOTER_EDIT}" />",
					activeStatus: true
				},
				dataType : 'json',
				error : commonAjaxErrorHandler,
				success : function(results) {
					precinctEl.empty()
					var newHtml = []
					$.each(results, function(index, item) {
						if (item.id == centralOfficeId) return
						
						var selectedText = (item.id == curVal) ? ' selected="selected"' : ''
						newHtml.push('<option value="' + item.id + '"' + selectedText + '>' + item.displayName + '</option>')
					})
					precinctEl.html(newHtml.join(''))
					
					precinctEl.val(curVal)
					precinctEl.multiselect("refresh")
					dialogEl.data('stationsPopulated', true)
					
					precinctEl.multiselect("open")
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
		$("#parkingStickerPrecinctId").val(hasParkingStickerObj ? parkingStickerObj.precinct.id : "${precinctContextId}")
	} else {
		$("#parkingStickerPrecinctId").empty().append(
				$("<option />")
					.prop("selected", true)
					.attr("value", hasParkingStickerObj ? parkingStickerObj.precinct.id : "${precinctContextId}")
					.text(hasParkingStickerObj ? parkingStickerObj.precinct.displayName : "${precinctContextName}"))
	}
	$("#parkingStickerPrecinctId").multiselect("refresh")
	
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
					<td class='appFieldLabel'>Precinct:<span
						class="invisibleRequiredFor508">*</span></td>
					<td><span class='requdIndicator'>*</span></td>
					<td><div id="parkingStickerPrecinctWrapper">
								<select id="parkingStickerPrecinctId"></select>
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