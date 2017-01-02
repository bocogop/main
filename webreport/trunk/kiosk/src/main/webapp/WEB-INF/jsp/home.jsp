<%@ include file="shared/inc_header.jsp"%>

<style>
.eventButton {
	display: inline-block;
	min-width: 250px;
	max-width: 250px;
	min-height: 250px;
	max-height: 250px;
	line-height: 290px;
	border: 3px solid #0033DD;
	border-radius: 15px;
	font-size: 16pt;
	text-align: center;
	margin:10px 20px;
	cursor:pointer;
}
.eventButton a {
	
}
.postTime {
  background: url('${imgHome}/clock.png');
}
.profile {
  background: url('${imgHome}/profile.png');
}
.opportunities {
	background: url('${imgHome}/voter.png');
}
.eventButton span {
	color:black;
	vertical-align: middle;
	text-shadow: white 0.1em 0.1em 0.1em;
}
</style>

<c:if test="${empty facilityContextId}">
	<script type="text/javascript">
		alert('Your working facility configuration is invalid; please contact the national coordinator. You will be logged out until this is corrected.')
		document.location.href = '${home}/logout.htm'
	</script>
</c:if>

<div style="width: 90vw">

	<c:if test="${not empty homepageAnnouncement}">
		<div class="notificationAlert notificationBox"
			style="margin-left: 10%; vertical-align: top; display: inline-block">
			<h1 style="text-align: center">
				<u>Announcements:</u>
				<p />
				${homepageAnnouncement}
			</h1>
		</div>
	</c:if>

</div>

<div class="clearCenter">${homepageContent}</div>

<div class="clearCenter">
	<a href="${home}/postTime.htm"><div class="eventButton postTime">
		<span>Post Time</span>
	</div></a>
	<div class="eventButton profile disabled">
		<span>View Profile</span>
	</div>
	<div class="eventButton opportunities disabled">
		<span>Browse Opportunities</span>
	</div>
</div>
