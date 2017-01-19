<%@ page contentType="text/html"%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="sec"
	uri="http://www.springframework.org/security/tags"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles"%>
<%@ taglib prefix="x" uri="http://java.sun.com/jsp/jstl/xml"%>
<%@ taglib prefix="app" uri="http://bocogop.org/app/taglibs"%>
<%@ taglib prefix="wr" tagdir="/WEB-INF/tags"%>

<c:set var="home" value="${pageContext.servletContext.contextPath}" />
<c:set var="ajaxHome" value="${home}${AJAX_CONTEXT_PATH_PREFIX}" />
<c:set var="jsHome" value="${home}/media/js" />
<c:set var="imgHome" value="${home}/media/images" />
<c:set var="cssHome" value="${home}/media/css" />
<c:set var="pkgHome" value="${home}/media/packages" />
