/* 
	widget has structure like:
		
	initialize(refreshCompleteCallback) = function to initialize this widget's data
	getReportParameters() - function which returns an Object whose keys/values are parameter
		keys/values we want to pass into the report. items having multiple values
		can be separated with commas or however the report needs to process them.
	getNonReportParameters() - an OPTIONAL function which returns an Object whose keys/values
		can be retrieved and used in report parameter javascript functions, as needed, but
		which are not included when sending the request to generate the report.
 */
function Widget(config) {
	var that = this
	
	if (!config) {
		alert('Missing required config param in widget constructor')
		return
	}
	
	this.id = config.id
	if (!this.id) {
		alert('Missing required config attribute "id" in widget constructor ')
		return
	}
	
	var refreshMethod = config.refresh || function(refreshParams, callback) {
		callback()
	}
	
	var nextRefreshCallback = null
	
	this.onNextRefresh = function(callback) {
		nextRefreshCallback = callback
	}

	/* Useful if our refresh method depends on other param values */
	var refreshParamProvider = function() { return {} }
	this.setRefreshParamProvider = function(provider) {
		refreshParamProvider = provider
	}
	
	var lastRefreshParams = null
	this.refresh = function() {
		if (!that.visible) return
		
		var newRefreshParams = refreshParamProvider()
		if (lastRefreshParams != null && _.isEqual(lastRefreshParams, newRefreshParams))
			return
			
		// console.log('Refreshing widget "' + that.id + '" with params ' + JSON.stringify(newRefreshParams, null, 4))
		
		lastRefreshParams = newRefreshParams
		
		var c = nextRefreshCallback || $.noop
		nextRefreshCallback = null
		refreshMethod(newRefreshParams, c)
	}
	
	this.getParameters = config.getParameters
	if (!this.getParameters || !$.isFunction(this.getParameters)) {
		alert('Missing required config attribute getParameters() in widget with ID "' + this.id + '"')
	}
	
	this.valueUpdatedListeners = []
	this.changeEventSelectors = config.changeEventSelectors || []
	this.htmlValidationSelectors = config.htmlValidationSelectors || []
	this.htmlValidationFailureMessageProvider = config.htmlValidationFailureMessageProvider
	this.submitAsReportParam = config.submitAsReportParam
	this.bypassAsReportParam = config.bypassAsReportParam
	this.visible = config.visible
	this.customValidator = config.customValidator || $.noop
	this.internalValidator = config.internalValidator || $.noop
}

Widget.prototype.show = function() {
	$("#widget_" + this.id + "_div").show()
	this.visible = true
	this.refresh()
}

Widget.prototype.hide = function() {
	$("#widget_" + this.id + "_div").hide()
	this.visible = false
}

Widget.prototype.toggle = function(theVal) {
	$("#widget_" + this.id + "_div").toggle(theVal)
	this.visible = theVal
	if (theVal) this.refresh()
}

Widget.prototype.registerChangeEventSelectors = function() {
	var that = this
	$.each(this.changeEventSelectors, function(index, item) {
		$(item).change(function(evt) {
			that.valueChanged(evt)
		})
	})
}

Widget.prototype.addValueUpdatedListener = function(callback) {
	if (!callback || !$.isFunction(callback)) {
		alert('Callback must be provided and have a type of function')
		return
	}
	
	this.valueUpdatedListeners.push(callback)
}

Widget.prototype.valueChanged = function(evt) {
	var that = this
	$.each(this.valueUpdatedListeners, function(index, callback) {
		callback(evt, that.getParameters(), that)
	})
}

/*
 * Return the value of the report parameter with the specified paramName, or the
 * first parameter value if paramName is unspecified (for convenience) - CPB
 */
Widget.prototype.getParameterValue = function(paramName) {
	var params = this.getParameters()
	for (var i = 0; i < params.length; i++)
		if (!paramName || params[i].paramName == paramName)
			return params[i].paramValue
	return null
}

/*
 * Set the value of the report parameter with the specified paramName to the specified paramValue, or the
 * first parameter value if paramName is unspecified (for convenience) - CPB
 */
Widget.prototype.setParameterValue = function(paramValue, paramName) {
	var params = this.getParameters()
	for (var i = 0; i < params.length; i++)
		if (!paramName || params[i].paramName == paramName) {
			params[i].paramValue = paramValue
			return
		}
}

Widget.prototype.bypassReportParameters = function(theVal) {
	if (typeof theVal != 'undefined') {
		this.bypassAsReportParam = theVal
	} else {
		this.bypassAsReportParam = true
	}
}