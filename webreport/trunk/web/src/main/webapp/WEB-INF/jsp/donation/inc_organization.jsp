<%@ include file="../shared/inc_header.jsp"%>

<div class="clearCenter organizationDisplayFields">
	<fieldset>
		<legend>Donor is Organization </legend>
		<div class="leftHalf">
			<table width="100%">
				<tr>
					<td class='appFieldLabel' nowrap>Precinct:</td>
					<td style="text-align: left"><c:out
							value="${command.displayedPrecinct}" /></td>
				</tr>
				<tr>
					<td class='appFieldLabel' nowrap><span
						style="padding-left: 30px">Name:</span></td>
					<td><span class="textWrap" style="max-width: 400px"><a class="appLink"
									href="${home}/donorEdit.htm?id=${command.donor.id}"><c:out
								value="${command.donor.organization.displayName}" /></a>
							<c:if test="${command.donor.organization.inactive}">
								<span class="redText" style="font-weight: bold">(INACTIVE)</span>
							</c:if></span></td>
				</tr>
				<tr>
					<td class='appFieldLabel' nowrap><span
						style="padding-left: 30px">Type:</span></td>
					<c:if test="${organizationNotBranch}">
						<td><c:out value="Organization" /></td>
					</c:if>
					<c:if test="${not organizationNotBranch}">
						<td><c:out value="Branch" /></td>
					</c:if>
				</tr>
				<tr>
					<td class='appFieldLabel' nowrap><span
						style="padding-left: 30px">Contact Name:</span></td>
					<td><c:out value="${command.donor.organization.contactName}" /></td>
				</tr>
				<tr>
					<td class='appFieldLabel' nowrap><span
						style="padding-left: 30px">Contact Title:</span></td>
					<td><c:out value="${command.donor.organization.contactTitle}" /></td>
				</tr>
				<tr>
					<td class='appFieldLabel' nowrap>Contact Email:</td>
					<td><c:out value="${command.donor.organization.email}" /> <c:if
							test="${not empty command.donor.organization.email}">
							<a href="mailto:${command.donor.organization.email}"><img
								alt='Click to email contact' src="${imgHome}/envelope.jpg"
								height="14" width="18" border="0" align="absmiddle"
								style="padding-left: 4px; padding-right: 4px" /></a>
						</c:if></td>
				</tr>
				<tr>
					<td class='appFieldLabel' nowrap>Contact Phone:</td>
					<td><c:out value="${command.donor.organization.phone}" /></td>
				</tr>
			</table>
		</div>
		<div class="rightHalf" style="margin-top: 45px">
			<table>
				<tr valign="top">
					<td class='appFieldLabel' nowrap>Address:</td>
					<c:if
						test="${not empty command.donor.organization.addressMultilineDisplay}">
						<td><pre>
								<c:out
									value="${command.donor.organization.addressMultilineDisplay}" />
							</pre></span></td>
					</c:if>
					<c:if
						test="${empty command.donor.organization.addressMultilineDisplay}">
						<td>Not on File</td>
					</c:if>
				</tr>
			</table>
		</div>
		<div style="margin-top: 20px;">
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
								<td class='appFieldLabel' nowrap>Ack Name:</td>
								<td><app:input id="orgAckContactInput"
										path="donationSummary.ackOverrideOrgContactName"
										class="ackAddressInput" size="30" maxlength="35" /></td>
								<td width="5"><img alt="" src="${imgHome}/spacer.gif"
									width="5" height="1" /></td>
								<td class='appFieldLabel' nowrap>Ack Title:</td>
								<td><app:input id="orgAckContactTileInput"
										path="donationSummary.ackOverrideOrgContactTitle"
										class="ackAddressInput" size="30" maxlength="35" /></td>
							</tr>
							<tr>
								<td class='appFieldLabel' nowrap>Ack Address 1:</td>
								<td><app:input id="orgAckAddress1Input"
										path="donationSummary.ackOverrideAddress1"
										class="ackAddressInput" size="30" maxlength="35" /></td>
								<td width="5"><img alt="" src="${imgHome}/spacer.gif"
									width="5" height="1" /></td>
								<td class='appFieldLabel' nowrap>Ack Address 2:</td>
								<td><app:input id="orgAckAddress2Input"
										path="donationSummary.ackOverrideAddress2"
										class="ackAddressInput" size="30" maxlength="35" /></td>
							</tr>
							<tr>
								<td class='appFieldLabel' nowrap>Ack City:</td>
								<td><app:input id="orgAckCityInput"
										path="donationSummary.ackOverrideCity" class="ackAddressInput"
										size="20" maxlength="30" /></td>
								<td width="5"><img alt="" src="${imgHome}/spacer.gif"
									width="5" height="1" /></td>
								<td class='appFieldLabel' nowrap><label for='stateSelect'>Ack
										State:</td>
								<td nowrap><app:select id="stateSelect"
										path="donationSummary.ackOverrideState" itemLabel="name"
										itemValue="id" class="ackAddressInput">
										<form:option value="-1" label="    " />
										<form:options items="${allStates}" itemLabel="name"
											itemValue="id" />
									</app:select> <app:errors path="donationSummary.ackOverrideState"
										cssClass="msg-error" /></td>
								<td width="5"><img alt="" src="${imgHome}/spacer.gif"
									width="5" height="1" /></td>
							</tr>
							<tr>
								<td class='appFieldLabel' nowrap>Ack Zip:</td>
								<td><app:input id="orgAckZipInput"
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
