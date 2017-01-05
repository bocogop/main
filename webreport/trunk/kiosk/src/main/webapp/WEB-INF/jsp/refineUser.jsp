<%@ include file="shared/inc_header.jsp"%>

<div class="clearCenter">More than one matching user was found.
	Which address below is familiar?</div>

<div class="clearCenter" style="text-align:center">
	<c:forEach items="${multiMatch.matches}" var="a">
		<div class="blueDiv" style="display: inline-block; text-align: center">
			<pre>
				<c:out value="${a.addressMultilineDisplay}" />
			</pre>
			<br> <a class="buttonAnchor"
				href="<c:url value="/refineUser.htm?id=${a.id}" />">Select</a>
		</div>
	</c:forEach>
	<div class="blueDiv" style="display: inline-block; text-align: center">
		<pre>
				None are familiar!
			</pre>
		<br> <a class="buttonAnchor"
			href="<c:url value="/refineUser.htm?id=-1" />">Select</a>
	</div>
</div>