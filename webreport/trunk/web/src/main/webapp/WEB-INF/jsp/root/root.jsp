<%@ include file="../shared/inc_header.jsp"%>

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

<tiles:importAttribute name="additionalFooterItem" scope="page" ignore="true" />

<!DOCTYPE html>
<html lang="en-US">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<meta http-equiv="X-UA-Compatible" content="IE=Edge">
<title>Boulder County GOP Staff</title>
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
	<%@ include file="javascript/inc_common_web.jsp"%>
	<table cellpadding="0" cellspacing="0" width="100%">
			<tr valign="middle">
				<td id="headerRowCell" background="${imgHome}/top-banner-middle.gif">
						<a href="${home}/home.htm"><img
							src="${imgHome}/elephant.png" height="99" border="0"
							align="left"></a>
					<img src="${imgHome}/flatirons.png" align="right" height="99"></td>
			</tr>
		
			<c:set var="logoutBarHeight" value="20" />
		<tr>
			<td align="center" class="blueBar" height="${logoutBarHeight}" nowrap><table
					cellpadding="0" cellspacing="0" width="100%">
					<tr valign="middle">
						<td width="100%" class="stationHeaderText">
						&nbsp;
						</td>
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
														<u>Announcements:</u>
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
					</tr>
					<tr>
						<td>&nbsp;</td>
					</tr>
					<c:if test="${not empty additionalFooterItem}">
						<tr>
							<td align="center"><c:out value="${additionalFooterItem}" /></td>
						</tr>
					</c:if>
					<c:if test="${not empty userTimeZoneName}">
						<tr class="bodyTextSmall">
							<td align="center" nowrap="nowrap"><wr:zonedDateTime value="${currentTime}" zoneId="${userTimeZone}" />
							|
							All times are in <c:out value="${userTimeZoneName}" />.
							</td>
						</tr>
					</c:if>
					<tr class="bodyTextSmall">
						<td align="center" nowrap="nowrap">App Version: <c:out
								value="${appVersionNumber}" />
						</td>
					</tr>
				</table>
			</td>
		</tr>
	</table>
</body>
</html>