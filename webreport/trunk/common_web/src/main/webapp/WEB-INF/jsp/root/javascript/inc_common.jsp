<%@ include file="../../shared/inc_header.jsp"%>

<script type="text/javascript">
	var protocolHostnamePort = '${protocolHostnamePort}'
	var homePath = '${home}'
	var imgHomePath = '${imgHome}'
	var ajaxHomePath = '${ajaxHome}'
	var cssHomePath = '${cssHome}'
	var jsHomePath = '${jsHome}'
	
	var csrfParamName = '${_csrf.parameterName}'
	var csrfValue = '${_csrf.token}'

	var twoDigitDateMask = '${TWO_DIGIT_DATE_MASK}'
	var twoDigitMonthYearMask = '${TWO_DIGIT_MONTH_YEAR_MASK}'
	var twoDigitDateTimeMask = '${TWO_DIGIT_DATE_TIME_MASK}'
	var useMinifiedDependencies = <c:out value="${useMinifiedDependencies}" default="false" />
	
	var nationalAdminRoleId = <c:out value="${ROLE_TYPE_NATIONAL_ADMIN.id}" default="-1" />
	var formReadOnly = <c:out value="${FORM_READ_ONLY}" default="false" />
</script>

<c:set var="minifiedSuffix" value="" />
<c:if test="${useMinifiedDependencies}">
	<c:set var="minifiedSuffix" value=".min" />
</c:if>

<%-- ========================== JQuery DataTables --%>

<script type="text/javascript"
	src="${jsHome}/jquery-2.1.4${minifiedSuffix}.js"></script>
<link type="text/css" rel="stylesheet"
	href="${pkgHome}/datatables-1.10.9/datatables${minifiedSuffix}.css" />
<script type="text/javascript"
	src="${pkgHome}/datatables-1.10.9/datatables${minifiedSuffix}.js"></script>

<%-- ========================== JQuery UI --%>

<link type="text/css" rel="Stylesheet"
	href="${pkgHome}/jquery-ui-1.11.4/jquery-ui${minifiedSuffix}.css" />
<link type="text/css" rel="Stylesheet"
	href="${pkgHome}/jquery-ui-1.11.4/jquery-ui.structure${minifiedSuffix}.css" />
<link type="text/css" rel="Stylesheet"
	href="${pkgHome}/jquery-ui-1.11.4/jquery-ui.theme${minifiedSuffix}.css" />
<script type="text/javascript"
	src="${pkgHome}/jquery-ui-1.11.4/jquery-ui${minifiedSuffix}.js"></script>

<%-- ========================== JQuery Dropdown --%>

<link type="text/css" rel="Stylesheet"
	href="${pkgHome}/jquery-dropdown/jquery.dropdown${minifiedSuffix}.css" />
<script type="text/javascript"
	src="${pkgHome}/jquery-dropdown/jquery.dropdown${minifiedSuffix}.js"></script>
	
<%-- I adapted the above for 508 compatibility and also for click-specific positioning
	(since their design only works relative to some target element, it doesn't work in
	scrollable divs - CPB --%>
<link type="text/css" rel="Stylesheet"
	href="${pkgHome}/jquery-wr-dropdown/jquery.wr.dropdown.css" />
<script type="text/javascript"
	src="${pkgHome}/jquery-wr-dropdown/jquery.wr.dropdown.js"></script>

<%-- ========================== JQuery Misc --%>

<script type="text/javascript" src="${jsHome}/jquery.alphanumeric.js"></script>
<script type="text/javascript" src="${jsHome}/jquery.bgiframe.js"></script>
<script type="text/javascript"
	src="${jsHome}/jquery.easy-confirm-dialog.js"></script>
<script type="text/javascript" src="${jsHome}/jquery.formrestrict.js"></script>
<script type="text/javascript" src="${jsHome}/jquery.form.js"></script>
<script type="text/javascript" src="${jsHome}/jquery.hotkeys.js"></script>
<script type="text/javascript" src="${jsHome}/jquery.hotKeyMap-1.0.js"></script>
<script type="text/javascript"
	src="${jsHome}/jquery.maskedinput-1.4.1.js"></script>
<script type="text/javascript" src="${jsHome}/jquery.mousewheel.js"></script>
<script type="text/javascript"
	src="${jsHome}/jquery.scrollTo${minifiedSuffix}.js"></script>
<script type="text/javascript" src="${jsHome}/jquery.topzindex.js"></script>

<%-- ========================== Other Misc --%>

<script type="text/javascript" src="${jsHome}/showHide.js"></script>
<script type="text/javascript" src="${jsHome}/expandPanel.js"></script>
<script type="text/javascript" src="${jsHome}/xdate.js"></script>
<script type="text/javascript" src="${jsHome}/filterlist.js"></script>
<script type="text/javascript" src="${jsHome}/sorted-array.js"></script>
<script type="text/javascript"
	src="${jsHome}/lodash.core.min.js"></script>
	
<%-- ========================== Date & Time Picker --%>

<link type="text/css" rel="Stylesheet"
	href="${pkgHome}/timepicker/timepicker.css" />
<script type="text/javascript" src="${pkgHome}/timepicker/timepicker.js"></script>

<%--
<link type="text/css" rel="Stylesheet"
	href="${pkgHome}/accessible-datepicker/datepicker-accessible.css" />
<script type="text/javascript" src="${pkgHome}/accessible-datepicker/datepicker-accessible.js"></script>
 --%>
 
<%-- ========================== MultiSelect --%>

<%-- Multi-select from http://loudev.com/#demos --%>
<link type="text/css" rel="Stylesheet"
	href="${pkgHome}/multiselect/jquery.multiselect.filter.css" />
<script type="text/javascript"
	src="${pkgHome}/multiselect/jquery.multiselect.filter.js"></script>
<link type="text/css" rel="Stylesheet"
	href="${pkgHome}/multiselect/jquery.multiselect.css" />
<script type="text/javascript"
	src="${pkgHome}/multiselect/jquery.multiselect.js"></script>

<link type="text/css" rel="Stylesheet"
	href="${cssHome}/commonStyles.css" />

<div id="spinner" class="spinner" style="display: none">
	<img id="img-spinner" src="${imgHome}/spinner.gif" alt="Loading" />
</div>
<div id="spinnerMessage" class="spinnerMessage" style="display:none"><p></p></div>

<%@ include file="inc_attentionDialog.jsp"%>
<%@ include file="inc_confirmationDialog.jsp"%>