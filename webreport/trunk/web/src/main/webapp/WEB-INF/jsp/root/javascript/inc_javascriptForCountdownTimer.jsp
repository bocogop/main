<%@ include file="../../shared/inc_header.jsp"%>

<tiles:importAttribute name="activateCountdownTimer" scope="page"
	ignore="true" />

<c:if test="${activateCountdownTimer == 'true'}">
	<script type="text/javascript" src="${jsHome}/jquery.idletimer.js"></script>
	<script type="text/javascript" src="${jsHome}/jquery.idletimeout.js"></script>
	<script type="text/javascript" src="${jsHome}/countdownTimer.js"></script>
	<script type="text/javascript">
	$(function() {
		loadCountdownTimer(${sessionScope.sessionHeartBeatTimeoutMillis},
				"${ajaxHome}/keepAlive",
				${sessionScope.sessionIdleAfterSeconds},
				${sessionScope.sessionPollingIntervalSeconds},
				${sessionScope.sessionFailedRequestsCount}, onIdleCallback)
	})
</script>

	<!-- dialog window markup -->
	<div id="sessionTimeoutDialog"
		style="visibility: hidden; display: none"
		title="Your session is about to expire!">
		<p>
			<span class="ui-icon ui-icon-alert"
				style="float: left; margin: 0 7px 50px 0;"></span> You will be
			logged off in <span id="dialog-countdown" style="font-weight: bold"></span>
			seconds.
		</p>

		<p>Do you want to continue your session?</p>
	</div>
</c:if>