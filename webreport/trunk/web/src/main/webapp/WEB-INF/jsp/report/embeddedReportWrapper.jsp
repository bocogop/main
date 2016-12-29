<%@ include file="../shared/inc_header.jsp"%>

<tiles:importAttribute name="paramPopupWidth" scope="page" ignore="true" />
<tiles:importAttribute name="paramPopupHeight" scope="page"
	ignore="true" />

<c:if test="${empty paramPopupWidth}">
	<c:set var="paramPopupWidth" value="650" />
</c:if>
<c:if test="${empty paramPopupHeight}">
	<c:set var="paramPopupHeight" value="380" />
</c:if>

<style>
div#ssrsFrameWrapper {
	text-align: center;
	width: 98vw;
	display: block;
}

#ssrsEmbedFrame {
	width: 98vw;
}
</style>


<script type="text/javascript" src="${jsHome}/widget.js"></script>

<%--
	Override the getReportPathExtension method in concrete report class JSPs, if some parameter
	changes the name of the SSRS report generated (e.g. if there are two separate SSRS reports,
	report_visn and report_facility, and a dropdown for the user for "type" with values "visn"
	and "facility", this method would return "_" + the value of that dropdown. CPB
 --%>
<script type="text/javascript">
	var ssrsReportBasename = "<c:out value="${ssrsReportBasename}" />"
	
	/* Default (invalid) implementation for this method to force callers to override this - CPB */
	function getReportPathExtension() {
		alert('Please implement getReportPathExtension() in your report file (return "" if no extension needed)')
	}
	
	/* Map of <widgetId, widget> */
	var allParamWidgets = new Object()
	function getWidget(id) {
		return allParamWidgets[id]
	}
	
	/* Will be redefined by each param widget as they are added to the page */
	var latestReportParameterBaseConfig = {}
	
	function registerWidget(widgetConfig) {
		var widget = new Widget($.extend({}, latestReportParameterBaseConfig, widgetConfig))
		
		if (allParamWidgets[widget.id]) {
			alert('Duplicate widgets detected with ID "' + widget.id)
			return
		}
		
		allParamWidgets[widget.id] = widget
		return widget
	}
	
	/* List of methods to call when all widgets have completed initializing */
	var allWidgetsInitializedCallback = null
	function setWidgetsInitializedCallback(callback) {
		allWidgetsInitializedCallback = callback
	}
	
	var preValidationsCallback = null
	function setPreValidationsCallback(callback) {
		preValidationsCallback = callback
	}
	
	/* A function taking a single parameter representing the final parameters to send to the report */
	var preSubmitCallback = null
	function setPreSubmitCallback(callback) {
		preSubmitCallback = callback
	}
	
	var reportSubmitted = false
	
	$(function() {
		createParamsDialog()
		
		var totalInitialized = 0
		var totalToInitialize = Object.keys(allParamWidgets).length
		
		skipSpinnerOnAjaxStart = true
		
		$.each(allParamWidgets, function(widgetId, widget) {
			var initCallback = function(isDeferred) {
				totalInitialized++
				
				if (widget.visible)
					widget.show()
				
				if (totalInitialized == totalToInitialize) {
					ensureUniqueParametersAcrossWidgets()
					
					$("#loadingParamsDiv").hide()
					skipSpinnerOnAjaxStart = false
					
					if (allWidgetsInitializedCallback != null)
						allWidgetsInitializedCallback()
				}
			}
			
			if (!widget.visible) {
				/* call our own logic now; widget.initialize will be called when the widget is shown */
				initCallback(true)
			} else {
				/* ask the widget to initialize and call our logic when it's done */
				widget.onNextRefresh(initCallback)
				widget.refresh()
			}
			
			widget.registerChangeEventSelectors()
		})
		
		var showParamsDialog = function() {
			$("#reportParametersWrapperDiv").dialog('open')
			return false
		}
		$("#showParamsDialog").click(showParamsDialog)
		
		$("#printButton").click(function() {
			showSpinner('Preparing high resolution report for printing, please wait...')
			setTimeout(hideSpinner, 10000)
			
			submitReport(true)
			return false
		})
		
		hidePageTitle()
		adjustIframeSizingAndLoading()
	})
	
	function createParamsDialog() {
		$("#reportParametersWrapperDiv").dialog({
			autoOpen : true,
			// modal : true,
			width : ${paramPopupWidth},
			height : ${paramPopupHeight},
			closeOnEscape : true,
			draggable : true,
			resizable : true,
			hide: 'fold',
			show: 'fold',
			title: "<c:out value="${pageTitle}" />",
			buttons : {
				'Submit' : function() {
					if (submitReport(false)) {
						$(this).dialog('close')
					}
				},
				'Cancel' : function() {
					$(this).dialog('close')
				}
			}
		})
	}
	
	function adjustIframeSizingAndLoading() {
		$("#ssrsEmbedFrame").load(function() {
			reportSubmitted = true
			hideSpinner()
		})
		
		var delta = ${ssrsEmbedFrameMargin}
		
		setInterval(function() {
			if (!reportSubmitted) return
			
			var iFrame = $('#ssrsEmbedFrame')
			try {
				var theVal = iFrame.contents().find('#ReportViewerControl_fixedTable').find("div[dir='LTR']").height()
				if (!theVal || theVal < 400)
					theVal = 400
				iFrame[0].style.height = (theVal + delta) + 'px';
			} catch (ignored) {}
		}, 1000)
	}
	
	/* Common sense check - CPB */
	function ensureUniqueParametersAcrossWidgets() {
		var allParamsTest = new Object()
		$.each(allParamWidgets, function(widgetId, widget) {
			if (!widget.submitAsReportParam) return
			
			$.each(widget.getParameters(), function(index, item) {
				if (typeof allParamsTest[item.paramName] !== 'undefined') {
					alert('Duplicate report parameter detected: ' + item.paramName + ' in widget ' + widget.id)
				}
				
				allParamsTest[item.paramName] = item.paramValue
			})
		})
	}
	
	function submitReport(isPrint) {
		if (preValidationsCallback) {
			var result = preValidationsCallback()
			if (result != null) {
				displayAttentionDialog(result)
				return false
			}
		}
		
		/* retrieve all widget parameters & add DOM parameter children to the form
		 as hidden elements - CPB */
		var allParams = new Object()
		
		try {
			$.each(allParamWidgets, function(widgetId, widget) {
				var params = widget.getParameters()
				
				/* If the widget's visible, ensure all non-optional parameters have been set by
					the user and pass HTML validation - CPB */
				if (widget.visible) {
					$.each(widget.htmlValidationSelectors, function(index, selector) {
						$(selector).each(function(index1, inputToValidate) {
							if (inputToValidate.checkValidity() == false) {
								if (widget.htmlValidationFailureMessageProvider == null)
									throw 'No htmlValidationFailureMessageProvider provided for widget "' + widget.id + '"'
								
								var msg = widget.htmlValidationFailureMessageProvider(inputToValidate)
								if (!msg)
									throw 'Validation case not handled for input ID ' + inputToValidate.id
								throw msg
							}
						})
					})
					
					$.each(params, function(index, item) {
						if (item.paramValue == null || (!$.isArray(item.paramValue) && $.trim(item.paramValue) == '')) {
							throw 'Please enter a value for parameter "' + item.displayName + '"'
							return false
						}
					})
					
					var error = widget.internalValidator()
					if (error != null) {
						throw error
					}
					
					error = widget.customValidator()
					if (error != null) {
						throw error
					}
				}
				
				if (!widget.submitAsReportParam || widget.bypassAsReportParam) return
			
				$.each(params, function(index, item) {
					allParams[item.paramName] = item.paramValue
				})
			})
		} catch (error) {
			displayAttentionDialog(error)
			return false
		}
		
		if (preSubmitCallback) {
			var result = preSubmitCallback(allParams)
			if (result != null) {
				displayAttentionDialog(result)
				return false
			}
		}
		
		allParams['Username'] = "<c:out value="${username}" />"
		allParams['UserPasswordHash'] = "<c:out value="${userPasswordHash}" />"
		allParams['FacilityContextId'] = "<c:out value="${siteContextId}" />"
		allParams['SkipAuthorization'] = "false"
		
		$("#printButton").show()
		rebuildFormParameters(allParams, isPrint)
		$('#frmRender').submit()
		return true
	}
	
	function rebuildFormParameters(allParams, isPrint) {
		var formAction = '${reportViewURL}' + ssrsReportBasename + getReportPathExtension() + '&rc:Parameters=false'
		var formTarget = 'ssrsEmbedFrame'
		
		if (isPrint) {
			formAction = "${home}/printReport.htm"
			formTarget = 'invisiblePrintFrame'
			allParams["reportName"] = ssrsReportBasename + getReportPathExtension()
			allParams["${_csrf.parameterName}"] = "${_csrf.token}"
			allParams["rc:Parameters"] = "false"
			allParams["rs:Format"] = "PDF"
			allParams["rs:Command"] = "Render"
			allParams["rv:Toolbar"] = "None"
			allParams["rc:Toolbar"] = "false"
			allParams["rv:HeaderArea"] = "None"
		}
		
		$('#frmRender').attr('action', formAction)
		$('#frmRender').attr('target', formTarget)
		
		// remove previously submitted params, if any
		$('#frmRender .ssrsParametersSubmit').remove()
		
		$.each(allParams, function(key, value) {
			$.each($.isArray(value) ? value : [value], function(index, item) {
				// uncomment this line to print parameter values to console
				// console.log('Adding param ' + key + ' = ' + item)  
				$('<input />').attr('type', 'hidden').attr(
						'name', key).attr('value', item).attr(
						'class', 'ssrsParametersSubmit')
						.appendTo('#frmRender')
			}) 
		})
	}
</script>

<div>
	<table align="center" width="90%">
		<tr align="center">
			<td width="40%">&nbsp;</td>
			<td width="20%" nowrap><a class="buttonAnchor" href="#"
				id="showParamsDialog" title="Edit Parameters">Edit Parameters</a> <a
				class="buttonAnchor" id="printButton" style="display: none" href="#"
				title="Print Report">Print</a></td>
			<td width="40%"><c:if test="${not empty disclaimerText}">
					<c:out value="${disclaimerText}" />
				</c:if></td>
		</tr>
	</table>

	<div align="center" id="reportParametersWrapperDiv"
		style="display: none">
		<tiles:insertAttribute name="reportParameters" />
		<form id="frmRender" action="willBeReplaced" method="POST"
			target="ssrsEmbedFrame">
			<input type="hidden" name="rs:Command" value="Render">
		</form>
		<div align="center" id="loadingParamsDiv">
			<img alt="" src="${imgHome}/spinner.gif" /> Loading parameters...
		</div>
	</div>

	<div id="ssrsFrameWrapper">
		<iframe id="ssrsEmbedFrame" frameborder="0" name="ssrsEmbedFrame"></iframe>
	</div>

	<iframe id="invisiblePrintFrame"
		style="visibility: hidden; display: none" frameborder="0"
		name="invisiblePrintFrame"></iframe>
</div>