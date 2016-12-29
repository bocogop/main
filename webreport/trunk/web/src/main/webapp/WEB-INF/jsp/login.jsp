<%@ include file="shared/inc_header.jsp"%>

<script type="text/javascript">
	function submitLogin() {
		$("#loginSpinner").show()
		doubleClickSafeguard($("#loginButton"), 20000, function() {
			$("#loginSpinner").hide()
		})
		return true
	}
</script>

<div class="login-form">
	<c:if test="${not empty errorMessage}">
		<div class="alert alert-danger clearCenter">
			<p>
				<c:out value="${errorMessage}" />
			</p>
		</div>
	</c:if>
	<c:if test="${userLoggedOut}">
		<div>
			<p>You have been logged out successfully.</p>
		</div>
	</c:if>
	<c:url var="loginUrl" value="/login.htm" />
	<form action="${loginUrl}" method="post" class="form-horizontal"
		onsubmit="return submitLogin();">
		<div class="login-input-group">
			<div class="input-group input-sm">
				<label class="input-group-addon" for="username"><i
					class="fa fa-user">VA Username:</i></label> <input type="text"
					class="form-control" id="username" name="username"
					placeholder="Enter Username" required>
			</div>
			<div class="input-group input-sm">
				<label class="input-group-addon" for="password"><i
					class="fa fa-lock">Password:</i></label> <input type="password"
					class="form-control" id="password" name="password"
					placeholder="Enter Password" autocomplete="off" required>
			</div>
		</div>
		<input type="hidden" name="${_csrf.parameterName}"
			value="${_csrf.token}" />
		<p>
		<div class="form-actions">
			<input type="submit" class="btn btn-block btn-primary btn-default"
				value="Log in" id="loginButton">
		</div>
	</form>
	<div align="center">
		<img src="${imgHome}/spacer.gif" align="absmiddle" height="32"
			width="1" alt="" /> <span id="loginSpinner" style="display: none"> <img
			src="${imgHome}/spinner.gif" align="absmiddle" alt="Spinner Icon" /> Logging in, please
			wait...
		</span> <img src="${imgHome}/spacer.gif" align="absmiddle" height="32"
			width="1" alt="" />
	</div>
</div>

