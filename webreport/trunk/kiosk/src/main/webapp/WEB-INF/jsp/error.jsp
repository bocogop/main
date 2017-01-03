<%@ include file="shared/inc_header.jsp"%>

<div align="center">
	Sorry, but we have experienced an error!
	<p>Please contact Boulder County GOP staff for help entering your attendance, and other event functions.
	<p>
		<textarea style="display:none" rows="20" cols="120" wrap="off"><c:out value="${exceptionStackTrace}" /></textarea>
	</p>
</div>
<p align="center">

	<a class="backLink" href="${home}"> Back to Home</a> <img
		src="${imgHome}/right.gif" alt="Arrow signifying a return to home" border="0" align="absmiddle" />
</p>