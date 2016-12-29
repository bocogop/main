<%@ include file="../../shared/inc_header.jsp"%>

<script type="text/javascript">
	var soundsEnabled = <c:out value="${currentUser.soundsEnabled}" default="false" />
</script>

<script type="text/javascript" src="${jsHome}/sounds.js"></script>

<audio class="notificationBeep" preload="none">
	<source src="${soundHome}/pottery.mp3" type="audio/mpeg" />
</audio>