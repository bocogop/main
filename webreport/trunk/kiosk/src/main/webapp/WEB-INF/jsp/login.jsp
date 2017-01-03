<%@ include file="shared/inc_header.jsp"%>

<script type="text/javascript">
	function submitLogin() {
		var voterId = $("#voterId").val().replace('|', '')
		var firstName = $("#firstName").val().replace('|', '')
		var lastName = $("#lastName").val().replace('|', '')
		var birthYear = $("#birthYear").val().replace('|', '')

		$("#username").val(voterId + '|' + firstName + '|' + lastName)
		$("#password").val(voterId + '|' + birthYear)

		$("#loginSpinner").show()
		doubleClickSafeguard($("#loginButton"), 20000, function() {
			$("#loginSpinner").hide()
		})
		return true
	}
	$(function() {
		setTimeout(function() {
			$("#oneTimeMessages").fadeOut(2000)
		}, 10000)
	})
</script>

<style>
.bookmarkMsg {
	font-size: 14pt;
}

.loginLabel {
	font-weight: bold;
	font-style: italic;
}
</style>

<div class="login-form">
	<c:url var="loginUrl" value="/login.htm" />
	<form action="${loginUrl}" method="post" class="form-horizontal"
		onsubmit="return submitLogin();" autocomplete="${!isProduction}">
		<div class="clearCenter" style="max-width: 85%">
			<c:if test="${not empty introductoryText}">
			<div class="clearCenter" style="padding: 25px 0px 25px 0px">
				<a name="content"></a>
				<c:out value="${introductoryText}" />
			</div>
			</c:if>
			
			<c:if test="${not empty errorMessage}">
				<div class="oneTimeUserNotification clearCenter">
					<c:out value="${errorMessage}" />
				</div>
			</c:if>
			<div id="oneTimeMessages">
				<c:if test="${fn:escapeXml(param.thankYou) != ''}">
					<div align="center" class="oneTimeUserNotification">
						<spring:message code="mealTicket.noneDue" />
					</div>
				</c:if>
				<c:if test="${fn:escapeXml(param.logout) != ''}">
					<div>
						<p>You have been logged out successfully.</p>
					</div>
				</c:if>
			</div>

			<div class="clearCenter">
				<table>
					<tr>
						<td><img src="${imgHome}/profile.png" width="75"
							align="absmiddle" /></td>
						<td align="left">Today's Event: <b><c:out value="${event.name}" /></b>
						<p>
								<c:out value="${globalIntroText}" /></td>
					</tr>
				</table>
			</div>

			<div class="clearCenter">
				<div class="leftHalf"
					style="padding-top: 15px; width: 44%; max-width: 44%">
					<div class="clearCenter">
						<table cellpadding="5">
							<tr>
								<td align="right" nowrap><label class="loginLabel" for="username"><spring:message
											code="identifyingCode" />:</label></td>
								<td align="left"><input type="text" id="voterId"
									 size="12" /> <spring:message code="or" /></td>
							</tr>
							<tr>
								<td align="right" nowrap><label class="loginLabel"
									for="firstName"><spring:message code="name" />:</label></td>
								<td align="left" nowrap><input type="text" id="firstName"
									size="12" placeholder="first" /> <input type="text"
									id="lastName" size="12" placeholder="last" /></td>
							</tr>
							<tr align="right"><td><spring:message code="and" /></td></tr>
							<tr>
								<td align="right" nowrap><label class="loginLabel"
									for="birthYear"><spring:message
										code="yearOfBirth" />:</label></td>
								<td align="left" nowrap><input id="birthYear" type="password"
									placeholder="YYYY" maxlength="4" size="4" /></td>
							</tr>
						</table>
					</div>
				</div>
				<input type="hidden" name="${_csrf.parameterName}"
					value="${_csrf.token}" />
			</div>
			<p>
			<div class="clearCenter" style="padding: 10px 0px 0px 0px">
				<div class="form-actions">
					<input type="submit" class="btn btn-block btn-primary btn-default"
						value="Log in" id="loginButton"> <input type="hidden"
						style="visibility: hidden; display: none" id="username"
						name="username" /> <input type="hidden"
						style="visibility: hidden; display: none" id="password"
						name="password" />
					<div align="center">
						<img src="${imgHome}/spacer.gif" align="absmiddle" height="32"
							width="1" alt="" /> <span id="loginSpinner"
							style="display: none"> <img src="${imgHome}/spinner.gif"
							align="absmiddle" alt="Spinner Icon" /> <spring:message
								code="loggingIn" />...
						</span> <img src="${imgHome}/spacer.gif" align="absmiddle" height="32"
							width="1" alt="" />
					</div>
				</div>
			</div>
	</form>

</div>

