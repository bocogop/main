function loadCountdownTimer(sessionHeartBeatTimeoutMillis, keepAliveActionURL,
		idleAfterSeconds, pollingIntervalSeconds, failedRequestsCount, onIdleCallback) {
	// session timeout - setup the dialog
	$("#sessionTimeoutDialog").dialog({
		autoOpen : false,
		modal : true,
		width : 400,
		height : 300,
		closeOnEscape : false,
		draggable : false,
		resizable : true,
		zIndex : 999999,
		buttons : {
			'Yes, Keep Working' : {
				text : 'Yes, Keep Working',
				id : 'timeoutDialogKeepWorkingBtn',
				click : function() {
					$(this).dialog('close');
				}
			},
			'No, Logoff' : function() {
				// fire whatever the configured onTimeout callback is.
				// using .call(this) keeps the default behavior of "this"
				// being the warning
				// element (the dialog in this case) inside the callback.
				$.idleTimeout.options.onTimeout.call(this);
			}
		}
	});

	document.getElementById('sessionTimeoutDialog').style.visibility = 'visible'
	document.getElementById('sessionTimeoutDialog').style.display = ''

	// cache a reference to the countdown element so we don't have to query
	// the DOM for it on each ping.
	var $countdown = $("#dialog-countdown");

	// start the idle timer plugin
	$.idleTimeout('#sessionTimeoutDialog', '#timeoutDialogKeepWorkingBtn', {
		AJAXTimeout : sessionHeartBeatTimeoutMillis,
		idleAfter : idleAfterSeconds,
		pollingInterval : pollingIntervalSeconds, // a request to keepalive
													// (below)
		// will be sent to the server every
		// 1 minutes
		failedRequests : failedRequestsCount, // increase number of failed
		// requests to prevent auto-logout
		// stop under bad connection
		keepAliveURL : keepAliveActionURL,
		serverResponseEquals : null,
		onTimeout : function() {
			window.location = homePath + "/logout.htm?timeout=true"
		},
		onIdle : function() {
			$(this).dialog("open")
			if (onIdleCallback) onIdleCallback()
		},
		onCountdown : function(counter) {
			$countdown.html(counter) // update the counter
		},
		onAbort : function(warning) {
			alert("Lost connection to the application, you will be logged off!")
			window.location = homePath + "/logout.htm?timeout=true"
		}
	})
}