<%@ include file="shared/inc_header.jsp"%>

<c:if test="${not empty errorMessage}">
	<div class="alert alert-danger clearCenter">
		<p>
			<c:out value="${errorMessage}" />
		</p>
	</div>
</c:if>

<div class="alert alert-danger clearCenter">
	Please ensure you have been assigned access to WR and your account is not expired or disabled.
</div>
<p>

<div class="clearCenter">
	<a href="<c:out value="${userErrorPostRedirectUrl}" />">Return to Login</a>
</div>

