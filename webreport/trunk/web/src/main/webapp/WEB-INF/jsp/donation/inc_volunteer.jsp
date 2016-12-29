<%@ include file="../shared/inc_header.jsp"%>

<div class="clearCenter volunteerDisplayFields">
	<fieldset>
		<legend>Donor is Volunteer </legend>
		<div class="leftHalf">
			<table>
				<tr>
					<td class='appFieldLabel' nowrap>Donor Type:</td>
					<td><c:out value="${command.donor.donorType.donorType}" /></td>
				</tr>
				<tr>
					<td class='appFieldLabel' nowrap><span
						style="padding-left: 30px">Name:</span></td>
					<td><a class="appLink"
						href="${home}/donorEdit.htm?id=${command.donor.id}"><c:out
								value="${command.donor.volunteer.displayName}" /></a></td>
				</tr>
				<tr>
					<td class='appFieldLabel' nowrap>Email:</td>
					<td><c:out value="${command.donor.volunteer.email}" /> <c:if
							test="${not empty command.donor.volunteer.email}">
							<a href="mailto:${command.donor.volunteer.email}"><img
								alt='Click to email volunteer' src="${imgHome}/envelope.jpg"
								height="14" width="18" border="0" align="absmiddle"
								style="padding-left: 4px; padding-right: 4px" /></a>
						</c:if></td>
				</tr>
				<tr>
					<td class='appFieldLabel' nowrap>Phone:</td>
					<td><c:out value="${command.donor.volunteer.phone}" /></td>
				</tr>
			</table>
		</div>
		<div class="rightHalf">
			<table>
				<tr valign="top">
					<td class='appFieldLabel' nowrap>Address:</td>
					<c:if
						test="${not empty command.donor.volunteer.addressMultilineDisplay}">
						<td><pre>
								<c:out
									value="${command.donor.volunteer.addressMultilineDisplay}" />
							</pre></span></td>
					</c:if>
					<c:if
						test="${empty command.donor.volunteer.addressMultilineDisplay}">
						<td>Not on File</td>
					</c:if>
				</tr>
			</table>
		</div>
		<div style="margin-top: 20px">
			<table width="100%">
				<tr>
					<td class="fixedAckAddressFields"><label
						for='ackAddressOverrideInput' />Acknowledgement Override<a
						href="javascript:editAckAddress()"><img
							alt="Edit Acknowledgement Address"
							src="${imgHome}/edit-small.gif" align="absmiddle" border="0" /></a>
					</td>
				</tr>
				<tr>
					<td class="ackAddressInputs" style="display: none"><label
						for='ackAddressNoneInput' />Hide Acknowledgement Address<a
						href="javascript:hideAckAddress()"><img
							alt="Hide Acknowledgement Address"
							src="${imgHome}/cancel-icon.png" align="absmiddle" border="0" /></a>
					</td>
				</tr>
				<tr>
					<td class="ackAddressInputs" style="display: none">
						<table>
							<tr>
								<td class='appFieldLabel' nowrap>Ack Address 1:</td>
								<td><app:input id="volAckAddress1Input"
										path="donationSummary.ackOverrideAddress1" size="30"
										maxlength="35" /></td>
								<td width="5"><img alt="" src="${imgHome}/spacer.gif"
									width="5" height="1" /></td>
								<td class='appFieldLabel' nowrap>Ack Address 2:</td>
								<td><app:input id="volAckAddress2Input"
										path="donationSummary.ackOverrideAddress2" size="30"
										maxlength="35" /></td>
							</tr>
							<tr>
								<td class='appFieldLabel' nowrap>Ack City:</td>
								<td><app:input id="volAckCityInput"
										path="donationSummary.ackOverrideCity" size="20"
										maxlength="30" /></td>
								<td width="5"><img alt="" src="${imgHome}/spacer.gif"
									width="5" height="1" /></td>
								<td class='appFieldLabel' nowrap><label for='stateSelect'>Ack
										State:</td>
								<td nowrap><app:select id="stateSelect"
										path="donationSummary.ackOverrideState" itemLabel="name"
										itemValue="id">
										<form:option value="-1" label="    " />
										<form:options items="${allStates}" itemLabel="name"
											itemValue="id" />
									</app:select> <app:errors path="donationSummary.ackOverrideState"
										cssClass="msg-error" /></td>
								<td width="2"><img alt="" src="${imgHome}/spacer.gif"
									width="2" height="1" /></td>
							</tr>
							<tr>
								<td class='appFieldLabel' nowrap>Ack Zip:</td>
								<td><app:input id="volAckZipInput"
										path="donationSummary.ackOverrideZip" class="ackAddressInput"
										size="10" maxlength="10" /></td>
							</tr>
						</table>
					</td>
				</tr>
			</table>
		</div>
		<c:if
			test="${not FORM_READ_ONLY and command.donationSummary.persistent}">
			<div class="clearCenter">
				<td rowspan="2" width="110px">&nbsp;</td> <a
					href="javascript:popupDonorSearch('changeDonor')"
					id="changeDonorButton" class="buttonAnchor">Change Donor</a>
			</div>
		</c:if>
	</fieldset>
</div>


