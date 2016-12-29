<%@ include file="../shared/inc_header.jsp"%>

<div class="clearCenter individualDisplayFields">
			<fieldset>
				<legend>Donor</legend>
				<div class="leftHalf">
					<table>
						<tr>
							<td class='appFieldLabel' nowrap>Donor Type:</td>
							<td><c:out value="${command.donor.donorType.donorType}" /></td>
						</tr>
					<c:if test="${command.donor.donorType.id == '1'}">
						<tr>
							<td class='appFieldLabel' nowrap><span
								style="padding-left: 30px">Name:</span></td>
							<td><a class="appLink"
							href="${home}/donorEdit.htm?id=${command.donor.id}"><c:out
									value="${command.donor.individualName}" /></a></td>
						</tr>
						<tr>
							<td class='appFieldLabel' nowrap>Email:</td>
							<td><c:out value="${command.donor.email}" /> <c:if
									test="${not empty command.donor.email}">
									<a href="mailto:${command.donor.email}"><img
										alt='Click to email donor' src="${imgHome}/envelope.jpg"
										height="14" width="18" border="0" align="absmiddle"
										style="padding-left: 4px; padding-right: 4px" /></a>
								</c:if></td>
						</tr>
						<tr>
							<td class='appFieldLabel' nowrap>Phone:</td>
							<td><c:out value="${command.donor.phone}" /></td>
						</tr>
					</table>
				</div>
				<div class="rightHalf">
					<table>
						<tr>
							<td class='appFieldLabel'>Address:</td>
							<c:if test="${not empty command.donor.addressMultilineDisplay}">
								<td><span><pre><c:out value="${command.donor.addressMultilineDisplay}" /></pre></span></td>
							</c:if>
							<c:if test="${empty command.donor.addressMultilineDisplay}">
								<td>Not on File</td>
							</c:if>
						</tr>
						</c:if>
					</table>
				</div>
				<c:if
					test="${not FORM_READ_ONLY and command.donationSummary.persistent}">
					<div class="clearCenter">
						<td rowspan="2" width="110px">&nbsp;</td> <a
							href="javascript:popupDonorSearch('changeDonor')" id="changeDonorButton"
							class="buttonAnchor">Change Donor</a>
					</div>
				</c:if>
			</fieldset>
		</div>