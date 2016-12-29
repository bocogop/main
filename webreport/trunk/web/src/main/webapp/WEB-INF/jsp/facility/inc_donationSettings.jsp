<%@ include file="../shared/inc_header.jsp"%>

<div class="clearCenter" style="margin-bottom: 15px">
	<table>
		<tr>
			<td class='appFieldLabel' nowrap>Voluntary Service Signature:</td>
			<td><app:input
					path="facility.stationParameters.voluntarySignatureLine"
					id="voluntarySignature" maxlength="50" size="50" /> <app:errors
					path="facility.stationParameters.voluntarySignatureLine"
					cssClass="msg-error" /></td>
		
		</tr>
		<tr>
			<td class='appFieldLabel' nowrap>Director's Signature:</td>
			<td><app:input
					path="facility.stationParameters.directorSignatureLine"
					id="directorSignature" maxlength="50" size="50" /> <app:errors
					path="facility.stationParameters.directorSignatureLine"
					cssClass="msg-error" /></td>
		</tr>
		<tr>
			<td class='appFieldLabel' nowrap>Set Value Amount $:</td>
			<td><app:input path="facility.stationParameters.valueAmount"
					id="valueAmount" class="currency" size="13" /> <app:errors
					path="facility.stationParameters.valueAmount" cssClass="msg-error" /></td>
					</tr>

	</table>
</div>
<div style="float: left; margin-bottom: 15px">
	<div id="genPostFundDiv" class="leftHalf">
		<fieldset>
			<legend>General Post Fund</legend>
			<c:if test="${not FORM_READ_ONLY}">
				<div align="center" style="margin-top: 10px">
					<a class="buttonAnchor" id="createGPFButton"
						href="javascript:popupDonGenPostFundEdit()">Add GPF</a>
				</div>
			</c:if>
			<table class="formatTable" id="donGenPostFundList" border="1"
				style="margin-top: 10px" summary="List of Donations">
				<thead>
					<tr>
						<th width="50%">General Post Fund</th>
						<th width="30%" class="select-filter">Status</th>
						<c:if test="${not FORM_READ_ONLY}">
							<th width="20%">Action</th>
						</c:if>
					</tr>
				</thead>
				<tbody>
				</tbody>
			</table>
		</fieldset>
	</div>


	<div id="donReferenceDiv" class="rightHalf" style="margin-left: 10px">
		<fieldset>
			<legend>Reference</legend>
			<c:if test="${not FORM_READ_ONLY}">
				<div align="center" style="margin-top: 10px">
					<a class="buttonAnchor" id="createGPFButton"
						href="javascript:popupDonReferenceEdit()">Add Reference</a>
				</div>
			</c:if>
			<table class="formatTable" id="donationRefList" border="1"
				style="margin-top: 10px" summary="List of References">
				<thead>
					<tr>
						<th width="50%" class="textWrap">Reference</th>
						<th width="30%" class="select-filter">Status</th>
						<c:if test="${not FORM_READ_ONLY}">
							<th width="20%">Action</th>
						</c:if>
					</tr>
				</thead>
				<tbody>
				</tbody>
			</table>
		</fieldset>
	</div>

	<div id="donGenPostFundFieldsWrapper" style="display: none"
		title="General Post Fund Details">
		<div class="clearCenter">
			<table>
				<tr>
					<td class='appFieldLabel' nowrap>General Post Fund:<span
						class="invisibleRequiredFor508">*</span></td>
					<td style="padding: 4px; text-align: center" width="5%"><span
						class='requdIndicator'>*</span></td>
					<td><input type="text" id="donGenPostFundName" maxlength="25" size="30" /></td>
					<td><img alt="" src="${imgHome}/spacer.gif" height="1"
						width="25" /></td>
					<td align="right"><input type="checkbox" id="donGenPostFundActive" /></td>
					<td nowrap>Is Active</td>
				</tr>
			</table>
		</div>
	</div>

	<div id="donReferenceFieldsWrapper" style="display: none"
		title="Reference Details">
		<div class="clearCenter">
			<table>
				<tr>
					<td class='appFieldLabel' nowrap>Reference:<span
						class="invisibleRequiredFor508">*</span></td>
					<td style="padding: 4px; text-align: center" width="5%"><span
						class='requdIndicator'>*</span></td>
					<td><input type="text" id="donReferenceName" maxlength="50" size="30" /></td>
					<td><img alt="" src="${imgHome}/spacer.gif" height="1"
						width="25" /></td>
					<td align="right"><input type="checkbox" id="donReferenceActive" /></td>
					<td nowrap>Is Active</td>
				</tr>
			</table>
		</div>
	</div>