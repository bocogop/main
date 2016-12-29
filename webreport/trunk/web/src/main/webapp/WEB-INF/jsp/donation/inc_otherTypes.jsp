<%@ include file="../shared/inc_header.jsp"%>
<div class="clearCenter otherTypesDisplayFields">
	<fieldset>
		<legend> </legend>
		<div class="leftHalf">
			<table width="100%">
				<tr>
					<td class='appFieldLabel' nowrap>Donor Type:</td>
					<td><c:out value="${command.donor.donorType.donorType}" /></td>
				</tr>
				<c:if test="${command.donor.donorType.id == 2}">
					<tr>
						<td class='appFieldLabel' nowrap><span
							style="padding-left: 30px">Organization Name:</span></td>
						<td><a class="appLink"
							href="${home}/donorEdit.htm?id=${command.donor.id}"><c:out
									value="${command.donor.organization.displayName}" /></a></td>
					</tr>
				</c:if>
				<c:if
					test="${command.donor.donorType.id == 3 || command.donor.donorType.id == 5}">
					<tr>
						<td class='appFieldLabel' nowrap><span
							style="padding-left: 30px">Other Group Name:</span></td>
						<td><a class="appLink"
							href="${home}/donorEdit.htm?id=${command.donor.id}"><c:out
									value="${command.donor.otherGroupName}" /></a></td>
					</tr>
				</c:if>
				<c:if
					test="${command.donor.donorType.id == 2 || command.donor.donorType.id == 3}">
					<tr>
						<td class='appFieldLabel' nowrap><span
							style="padding-left: 30px">Individual Name:</span></td>
						<td><c:out value="${command.donor.individualName}" /></td>
					</tr>
				</c:if>
			</table>
		</div>
		<div class="rightHalf">
			<table>
				<tr>
				<tr valign="top">
					<td class='appFieldLabel' nowrap>Address:</td>
					<td><c:if
							test="${not empty command.donor.organization.addressMultilineDisplay}">
							<td><pre>
									<c:out
										value="${command.donor.organization.addressMultilineDisplay}" />
								</pre></span></td>
						</c:if> <c:if
							test="${empty command.donor.organization.addressMultilineDisplay}">
							<td>Not on File</td>
						</c:if></td>
				</tr>
				</tr>
			</table>
		</div>
	</fieldset>
</div>
