<%@ include file="shared/inc_header.jsp"%>

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

<tiles:importAttribute name="headerRowHeight" scope="page" />

<!DOCTYPE html>
<html lang="en-US">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<meta http-equiv="X-UA-Compatible" content="IE=Edge">
<title>VSS</title>
<style>
.noticeOfNonProdEnvironment {
	top: <c:out value="${headerRowHeight + 5}"/>px;
}

#headerRowCell {
	background-size: 100% 100%;
}

#appTitle {
	font-size: 15pt;
}
</style>
<c:forEach items="${extraHeadEntries}" var="entry">
	<c:out value="${entry}" escapeXml="false" />
</c:forEach>
</head>
<body>
	<%@ include file="inc_common_event.jsp"%>
	<span class="noticeOfNonProdEnvironment"><spring:message
			code="testEnvironmentWarning" /></span>
	<div id="skip">
		<a href="#content" on
			onclick="javascript:document.getElementById('content').focus()">Skip
			to Page Content</a>
	</div>
	<table cellpadding="0" cellspacing="0" width="100%">

		<tr valign="middle">
			<td id="headerRowCell" background="${imgHome}/top-banner-bg-red.gif"><c:if
					test="${not empty pageScope.topLeftImage}">
					<a href="${home}/home.htm"><img
						src="${imgHome}/${pageScope.topLeftImage}"
						height="${headerRowHeight}" border="0" align="left"
						alt="Department of Veterans Affairs logo"></a>
				</c:if><img src="${imgHome}/vsslogo_top.png" align="right"
				height="${headerRowHeight}"
				alt="Department of Veterans Affairs Voluntary Services System"></td>
		</tr>
		<tr>
			<td align="center" class="blueBar" height="28" nowrap><table
					cellpadding="0" cellspacing="0" width="100%">
					<tr valign="middle">
						<td width="100%" class="stationHeaderText">&nbsp;</td>
						<c:if test="${not empty facilityContextName}">
							<td align="right" class="stationHeaderText" nowrap><img
								src="${imgHome}/spacer.gif" alt="" height="0"><strong><spring:message
										code="facility" />:</strong>&nbsp;</td>
							<td align="left" class="stationHeaderText" width="0" nowrap
								id="dutyStationId"><div
									style="display: inline-block; background-color: white; padding: 2px; border-radius: 2px">
									<c:if test="${multipleStationsAssigned}">
										<a class="changeStationLink" href="${home}/changeStation.htm"><c:out
												value="${facilityContextName}" /><img
											src="${imgHome}/uparrow.png" height="14" hspace="4"
											border="0" align="absmiddle" alt="Change Station Arrow" /></a>
									</c:if>
									<c:if test="${not multipleStationsAssigned}">
										<span class="changeStationText"><c:out
												value="${facilityContextName}" /></span>
									</c:if>
								</div></td>
						</c:if>
						<c:if test="${not empty currentUser}">
							<td align="right" class="stationHeaderText" nowrap><img
								src="${imgHome}/spacer.gif" alt="" height="0" width="20"><strong><spring:message
										code="username" />:</strong>&nbsp;<c:out
									value="${currentUser.displayName}" /><img src="${imgHome}/spacer.gif"
								alt="" height="0" width="20"></td>
						</c:if>
					</tr>
				</table></td>
		</tr>

		<tr>
			<td><%@ include
					file="root/javascript/inc_oneTimeUserNotification.jsp"%>

				<table width="100%">
					<tr valign="top" id="titleRow">
						<td width="150"><tiles:importAttribute name="showHelp" scope="page" />
						<c:if test="${showHelp}">
						<a id='helpLink' href="help.htm"><img
							style="border: 0px; margin-bottom: 15px" alt="Access Help for this Page"
							src="${imgHome}/Helpicon-blue.png"></a>
						</c:if></td>
						<td>
							<table align="center">
								<tr>
									<td>
										<div id="appTopper">
											<h1 id="appTitle" tabIndex="0">
												<nobr>
													<span id="pageTitleSpan"><spring:message
															code="${pageTitle}" /></span>
												</nobr>
											</h1>
											<span id='pageDescription' style='display: none;'><c:out
													value="${pageDescription}" /></span>
										</div>
									</td>
								</tr>
							</table> <a id="content" href="#skipToPageContent"></a> <!--for 508 compliance "Skip to Page Content" -->
						</td>
						<td width="150" align="right" nowrap><sec:authorize access="isAuthenticated()">
							<a style="margin-top:5px;margin-bottom:5px;border:2px solid blue;" class="buttonAnchor" href="${home}/logout.htm"
								class="logoutLink"><b><spring:message code="logout" /></b></a>
						</sec:authorize></td>
					</tr>
				</table></td>
		</tr>
	</table>

	<c:if test="${not empty systemNotification}">
		<div align="center" class="activeNotification">
			<c:out value="${systemNotification}" />
		</div>
	</c:if>
	<%-- Should match the value of ErrorUtil.COMMAND_OBJ_NAME_ATTRIBUTE --%>
	<c:if test="${empty commandObjectName}">
		<c:set var="commandObjectName" value="command" />
	</c:if>
	<%-- <spring:hasBindErrors name="${commandObjectName}">
		<%@ include file="inc_errors.jsp"%>
	</spring:hasBindErrors> --%>
	<div id="appContainerDiv">
		<tiles:insertAttribute name="body" />
	</div>
	<p />
	<table cellpadding="0" cellspacing="0" width="90%" align="center">
		<tr>
			<td align="center">
				<table cellpadding="4" cellspacing="0">
					<tr class="bodyText">
						<td align="left">
							<ul
								style="padding-top: 5px; padding-bottom: 5px; margin-top: 0px; margin-bottom: 0px">
								<li><spring:message code="footerContact" /></li>
							</ul>
						</td>
						<td valign="middle" align="left" width="180"><img
							src="${imgHome}/VA_Excellence_4C.gif" width="180" height="72"
							alt="VA Health Care Defining Excellence in the 21st Century" /></td>
					</tr>
					<tr class="bodyTextSmall">
						<td colspan="2" align="center" nowrap="nowrap"><input
							type="radio" name="localeRadio"
							onclick="document.location.href='?locale=en'"
							${locale == 'en' ? ' checked' : ''}>English/Ingles <input
							type="radio" name="localeRadio"
							onclick="document.location.href='?locale=es'"
							${locale == 'es' ? ' checked' : ''}>Spanish/Espa�ol |
							<vss:zonedDateTime value="${currentTime}" /> |
							<spring:message code="printerStatus" />: <c:out
								value="${eventPrinterStatusHealthy ? 'Online' : 'Offline'}" />
						</td>
					</tr>
				</table>
			</td>
		</tr>
	</table>
</body>
</html>