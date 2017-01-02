<%@ include file="shared/inc_header.jsp"%>

<%@ include file="root/javascript/inc_common.jsp" %>

<link type="text/css" rel="Stylesheet"
	href="${cssHome}/commonEventStyles.css" />

<%-- VSS-specific Javascript dependencies --%>
<script type="text/javascript" src="${jsHome}/commonJavascript.js"></script>
<script type="text/javascript" src="${jsHome}/commonEventJavascript.js"></script>

<script type="text/javascript">
	function onIdleCallback() {
	}
</script>
<%@ include file="root/javascript/inc_javascriptForCountdownTimer.jsp" %>