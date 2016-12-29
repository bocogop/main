<%@ include file="../shared/inc_header.jsp"%>

<c:set var="hasErrors" scope="request" value="${errors.errorCount > 0}" />
<c:set var="hasNonServiceValidationError" value="false" />
<c:catch var="serviceValidationExReferenceThrowable">
	<c:forEach var="error" items="${errors.globalErrors}">
		<c:if test="${not empty error.serviceValidationException}">
			<c:set var="validationFailureException" value="${error.serviceValidationException}" />
			<script type="text/javascript">
			$(function() {
				<%@ include file="../shared/inc_validationFailureDialogDisplay.jsp" %>
			})
			</script>
		</c:if>
		<c:if test="${empty error.serviceValidationException}">
			<c:set var="hasNonServiceValidationError" value="true" />
		</c:if>
	</c:forEach>
</c:catch>
<c:if test="${serviceValidationExReferenceThrowable !=null}">
	<c:set var="hasNonServiceValidationError" value="true" />
</c:if>
<c:if test="${hasNonServiceValidationError or not empty errors.fieldErrors}">
	<div align="center" class="oneTimeUserNotification" tabIndex="0">
	    Please fix the errors below:
	</div>
</c:if>
<c:if test="${hasNonServiceValidationError or not empty errors.fieldErrors}">
	<table align="center" width="90%">
		<%--
		<tr align="center">
			<td class="oneTimeUserNotification"><b>Your changes have not been saved.</b>
			<p>
			<font size="-1"><i>Please correct the ${errors.errorCount}
			error(s) below and resubmit your changes.</i> </font></td>
		</tr>
		 --%>
		<tr>
			<td>
				<table align="center">
					<tr>
						<td class="oneTimeUserNotification">
							<ul>
								<c:forEach var="error" items="${errors.globalErrors}">								    
									<c:if test="${hasNonServiceValidationError}">
										<li><spring:message code="${error.code}"
												text="${error.defaultMessage}" arguments="${error.arguments}" />
										</li>
							 		</c:if>
								</c:forEach>
							</ul>
						</td>
					</tr>
				</table>
			</td>
		</tr>
	</table>
</c:if>
