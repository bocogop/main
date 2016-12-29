<%@ include file="../shared/inc_header.jsp"%>

<tiles:importAttribute name="topLeftImage" scope="page" ignore="true" />
<tiles:importAttribute name="extraHeadEntries" scope="page"
	ignore="true" />
<%--Allow these to be set either in the model or in the tiles config - CPB --%>
<c:if test="${empty pageTitle}">
	<tiles:importAttribute name="pageTitle" scope="page" ignore="true" />
</c:if>
<c:if test="${empty pageDescription}">
	<tiles:importAttribute name="pageDescription" scope="page"
		ignore="true" />
</c:if>

<tiles:importAttribute name="showFullHeader" scope="page" />
<tiles:importAttribute name="additionalFooterItem" scope="page" ignore="true" />

<!DOCTYPE html>
<html lang="en-US">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<meta http-equiv="X-UA-Compatible" content="IE=Edge">
<title>WR</title>
<style>
#headerRowCell {
	background-size:100% 100%;
}
</style>
<c:forEach items="${extraHeadEntries}" var="entry">
	<c:out value="${entry}" escapeXml="false" />
</c:forEach>
</head>
<body>
	<%@ include file="javascript/inc_common_staff.jsp"%>
	<div id="skip">
		<a href="#appTitleLink" aria-label="Skip to Page Content" title="Skip to Page Content">Skip to Page Content</a>
	</div>
	<table cellpadding="0" cellspacing="0" width="100%">
		<c:if test="${showFullHeader}">
			<tr valign="middle">
				<td id="headerRowCell" background="${imgHome}/top-banner-middle.gif"><c:if
						test="${not empty pageScope.topLeftImage}">
						<a href="${home}/home.htm"><img
							src="${imgHome}/${pageScope.topLeftImage}" height="99" border="0"
							align="left" alt="Department of Veterans Affairs logo"></a>
					</c:if><img src="${imgHome}/wrlogo_top.png" align="right" height="99"
					alt="Department of Veterans Affairs Voluntary Services System"></td>
			</tr>
		</c:if>
		
		<c:set var="logoutBarHeight" value="40" />
		<c:if test="${showFullHeader}">
			<c:set var="logoutBarHeight" value="20" />
		</c:if>
		
		<tr>
			<td align="center" class="blueBar" height="${logoutBarHeight}" nowrap><table
					cellpadding="0" cellspacing="0" width="100%">
					<tr valign="middle">
						<td width="100%" class="stationHeaderText">
						<c:if test="${showFullHeader}">
						&nbsp;
						</c:if>
						<c:if test="${not showFullHeader}">
						<img src="${imgHome}/wrlogo_oneline.png" height="40"
					alt="Department of Veterans Affairs Voluntary Services System" />
						</c:if></td>
						<c:if test="${not empty facilityContextName}">
							<td align="right" class="stationHeaderText" nowrap><img
								src="${imgHome}/spacer.gif" alt="" height="0"><strong>Facility:</strong>&nbsp;</td>
							<td align="left" class="stationHeaderText" width="0" nowrap
								id="dutyStationId"><div
									style="display: inline-block; background-color: white; padding: 2px; border-radius: 2px">
									<c:if
										test="${multipleStationsAssigned}">
										<a class="changeStationLink" href="${home}/changeStation.htm"><c:out
												value="${facilityContextName}" /><img
											src="${imgHome}/uparrow.png" height="14" hspace="4"
											border="0" align="absmiddle" alt="Change Facility Arrow" /></a>
									</c:if>
									<c:if
										test="${not multipleStationsAssigned}">
										<span class="changeStationText"><c:out
												value="${facilityContextName}" /></span>
									</c:if>
								</div></td>
						</c:if>
						<c:if test="${not empty currentUser}">
							<td align="right" class="stationHeaderText" nowrap><img
								src="${imgHome}/spacer.gif" alt="" height="0" width="20"><strong>User
									Name:</strong>&nbsp;<c:out value="${currentUser.displayName}" /></td>
						</c:if>
						<sec:authorize access="isAuthenticated()">
							<td align="right" nowrap><img src="${imgHome}/spacer.gif"
								alt="" height="0" width="20"><a
								href="javascript:logoutConfirm()" class="logoutLink">Log Out</a><img
								src="${imgHome}/spacer.gif" alt="" height="0" width="10"></td>
						</sec:authorize>
					</tr>
				</table></td>
		</tr>

		<tr>
			<td><tiles:importAttribute name="menu" scope="page"
					ignore="true" /> <c:if test="${not empty pageScope.menu}">
					<div style="width: 100%">
						<tiles:insertAttribute name="menu" />
					</div>
				</c:if></td>
		</tr>

		<tiles:importAttribute name="showPageTitle" scope="page" />
		<c:set var="appTitleStyle" value="" />
		<c:if test="${not showPageTitle}">
			<c:set var="appTitleStyle" value='style="display:none"' />
		</c:if>
		
		<tiles:importAttribute name="showAnnouncements" scope="page" />
		<c:set var="announcementsStyle" value="" />
		<c:if test="${not showAnnouncements}">
			<c:set var="announcementsStyle" value='style="display:none"' />
		</c:if>
		
		<tr>
			<td>
				<table width="100%">
					<tiles:importAttribute name="breadcrumbs" scope="page"
						ignore="true" />
					<c:if test="${not empty pageScope.breadcrumbs}">
						<tr>
							<td colspan="3"><tiles:insertAttribute name="breadcrumbs" /></td>
						</tr>
					</c:if>

					<tr valign="top" id="titleRow">
						<td width="26"><%@ include file="inc_contextualHelp.jsp"%></td>
						<td>
							<table align="center">
								<tr>
									<td>
										<a id="appTitleLink"></a>
										<div id="appTopper" class="roundedRect" ${appTitleStyle}>
											<h1 id="appTitle" tabIndex="0">
												<nobr>
													<span id="pageTitleSpan"><c:out value="${pageTitle}" /></span>
												</nobr>
											</h1>
										</div>
										<span id='pageDescription' style='display: none;'><c:out
													value="${pageDescription}" /></span>
										
										<div id="announcements" ${announcementsStyle}>
											<c:if test="${not empty homepageAnnouncement}">
												<div class="notificationAlert notificationBox"
													style="min-width: 350px;">
													<h1 style="text-align: center">
														<u>Voluntary Service System Announcements:</u>
													</h1>
													<p />
													<div align="center">${homepageAnnouncement}</div>
												</div>
											</c:if>
										</div>
										
									</td>
								</tr>
							</table>
						</td>
						<td width="26">&nbsp;</td>
					</tr>
				</table>
			</td>
		</tr>
	</table>

	<%@ include file="javascript/inc_oneTimeUserNotification.jsp" %>
	
	<c:if test="${not empty systemNotification}">
		<div align="center" class="activeAlert">
			<c:out value="${systemNotification}" />
		</div>
	</c:if>
	<%-- Should match the value of ErrorUtil.COMMAND_OBJ_NAME_ATTRIBUTE --%>
	<c:if test="${empty commandObjectName}">
		<c:set var="commandObjectName" value="command" />
	</c:if>
	<spring:hasBindErrors name="${commandObjectName}">
		<%@ include file="inc_errors.jsp"%>
	</spring:hasBindErrors>
	<div id="appContainerDiv">
		<tiles:insertAttribute name="body" />
	</div>
	<p />
	<table cellpadding="0" cellspacing="0" width="90%" align="center">
		<tr>
			<td align="center">
				<table cellpadding="4" cellspacing="0">
					<tr class="bodyText">
						<td align="left" style="border: 1px solid gray">
							${footerContent}</td>
						<td valign="middle" align="left" width="180"><img
							src="${imgHome}/VA_Excellence_4C.gif" width="180" height="72"
							alt="VA Health Care Defining Excellence in the 21st Century" /></td>
					</tr>
					<tr>
						<td colspan="2">&nbsp;</td>
					</tr>
					<c:if test="${not empty additionalFooterItem}">
						<tr>
							<td colspan="2" align="center"><c:out value="${additionalFooterItem}" /></td>
						</tr>
					</c:if>
					<c:if test="${not empty userTimeZoneName}">
						<tr class="bodyTextSmall">
							<td colspan="2" align="center" nowrap="nowrap"><wr:zonedDateTime value="${currentTime}" zoneId="${userTimeZone}" />
							|
							All times are in <c:out value="${userTimeZoneName}" />. <sec:authorize
									access="isAuthenticated()">
								 |
								<label><input type="checkbox" id="soundEnabled" value="true"
										onchange="javascript:setSounds(this)"> Sounds Enabled</label>
								</sec:authorize>
							</td>
						</tr>
					</c:if>
					<tr class="bodyTextSmall">
						<td colspan="2" align="center" nowrap="nowrap">App Version: <c:out
								value="${appVersionNumber}" />
						</td>
					</tr>
				</table>
			</td>
		</tr>
	</table>
</body>
</html>