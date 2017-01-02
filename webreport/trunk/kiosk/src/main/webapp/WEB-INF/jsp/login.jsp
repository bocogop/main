<%@ include file="shared/inc_header.jsp"%>

<script type="text/javascript">
	function submitLogin() {
		$("#password").val(
				$("#mm").val() + '/' + $("#dd").val() + '/' + $("#yyyy").val())

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
		
		$("#mm").keydown(function(e) {
			var keyCode = e.keyCode || e.which
			if (keyCode == 191 || keyCode == 111) {
				e.preventDefault()
				var v = $("#mm").val()
				if (v.length == 1)
					$("#mm").val('0' + v)
				$("#dd").focus()
			}
		})
		
		$("#dd").keydown(function(e) {
			var keyCode = e.keyCode || e.which
			if (keyCode == 191 || keyCode == 111) {
				e.preventDefault()
				var v = $("#dd").val()
				if (v.length == 1)
					$("#dd").val('0' + v)
				$("#yyyy").focus()
			}
		})
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

<c:if test="${showBookmarkMsg}">
	<div class="clearCenter">
		<span class="redText bookmarkMsg">Please immediately set this page as
			your browser homepage. This is <u>required</u> to enable printing
			functionality.
		</span>
	</div>
</c:if>

<div class="login-form">
	<c:url var="loginUrl" value="/login.htm" />
	<form action="${loginUrl}" method="post" class="form-horizontal"
		onsubmit="return submitLogin();" autocomplete="off">
		<div class="clearCenter" style="max-width: 85%">
			<div class="clearCenter" style="padding: 25px 0px 25px 0px">
				<a name="content"></a>
				<c:out value="${introductoryText}" />
			</div>
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
			<div class="leftHalf"
				style="padding-top: 40px; width: 40%; max-width: 40%">
				<c:out value="${globalIntroText}" />
			</div>
			<div class="rightHalf" style="width: 60%; max-width: 60%">
				<div class="clearCenter">
					<img src="${imgHome}/flag.jpg" alt="Picture of American flag" />
					<table cellpadding="5">
						<tr>
							<td align="right"><label class="loginLabel" for="username"><spring:message code="identifyingCode"/>:</label></td>
							<td align="left"><input type="password" id="username"
								name="username" placeholder="e.g. ABC123" required size="12" /></td>
						</tr>
						<tr>
							<td align="right" class="loginLabel"><spring:message code="dateOfBirth"/>:</td>
							<td align="left"><input id="mm" type="password"
								placeholder="MM" maxlength="2" size="2" required /> / <input
								id="dd" type="password" placeholder="DD" maxlength="2" size="2"
								required /> / <input id="yyyy" type="password"
								placeholder="YYYY" maxlength="4" size="4" required /> <input
								type="password" style="visibility:hidden;display:none" id="password" name="password" /></td>
						</tr>
					</table>
					<div align="center">
						<img src="${imgHome}/spacer.gif" align="absmiddle" height="32"
							width="1" alt="" /> <span id="loginSpinner"
							style="display: none"> <img src="${imgHome}/spinner.gif"
							align="absmiddle" alt="Spinner Icon" /> <spring:message code="loggingIn"/>...
						</span> <img src="${imgHome}/spacer.gif" align="absmiddle" height="32"
							width="1" alt="" />
					</div>
				</div>
				<input type="hidden" name="${_csrf.parameterName}"
					value="${_csrf.token}" />
				<p>
				<div class="form-actions">
					<input type="submit" class="btn btn-block btn-primary btn-default"
						value="Log in" id="loginButton">
				</div>
			</div>
	</form>

</div>

