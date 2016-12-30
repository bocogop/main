<%@ include file="../../shared/inc_header.jsp"%>

<%@ include file="inc_common.jsp" %>

<link type="text/css" rel="Stylesheet"
	href="${cssHome}/commonStaffStyles.css" />

<%-- WR-specific Javascript dependencies --%>
<script type="text/javascript" src="${jsHome}/commonJavascript.js"></script>
<script type="text/javascript" src="${jsHome}/commonStaffJavascript.js"></script>
<script type="text/javascript" src="${jsHome}/voterSearchJavascript.js"></script>
<script type="text/javascript" src="${jsHome}/organizationSearchJavascript.js"></script>
<script type="text/javascript" src="${jsHome}/assignmentSelectJavascript.js"></script>
<script type="text/javascript" src="${jsHome}/donorSearchJavascript.js"></script>
<script type="text/javascript" src="${jsHome}/donationSearchJavascript.js"></script>
<script type="text/javascript" src="${jsHome}/dataTableJavascript.js"></script>

<%@ include file="inc_sounds.jsp"%>

<script type="text/javascript">
	function onIdleCallback() {
		beep()
	}
</script>
<%@ include file="inc_javascriptForCountdownTimer.jsp" %>