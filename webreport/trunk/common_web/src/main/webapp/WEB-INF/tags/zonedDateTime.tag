<%@ tag body-content="empty" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<%@ attribute name="value" required="true" type="java.time.ZonedDateTime" %>
<%@ attribute name="pattern" required="false" type="java.lang.String" %>
<%@ attribute name="zoneId" required="false" type="java.time.ZoneId" %>
<%@ attribute name="zoneIdName" required="false" type="java.lang.String" %>

<spring:eval var="timeFormatter" expression="T(org.bocogop.shared.util.DateUtil).MILITARY_DATE_TIME_FORMAT"/>
<c:if test="${not empty pattern}">
	<spring:eval var="timeFormatter" expression="T(java.time.format.DateTimeFormatter).ofPattern(pattern)"/>
</c:if>

<c:set var="finalVal" value="${value}" />

<c:if test="${not empty zoneId}">
	<c:set var="finalVal" value="${value.withZoneSameInstant(zoneId)}" />
</c:if>
<c:if test="${empty zoneId and not empty zoneIdName}">
	<spring:eval var="zoneId" expression="T(java.time.ZoneId).of(zoneIdName)"/>
	<c:set var="finalVal" value="${value.withZoneSameInstant(zoneId)}" />
</c:if>

<c:out value="${finalVal.format(timeFormatter)}" />