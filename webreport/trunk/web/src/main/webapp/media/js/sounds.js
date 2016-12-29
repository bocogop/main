$(function() {
	$(".notificationBeep").bind('load', function() {
		// console.log('Notification beep sound loaded.')
	})
	$(".notificationBeep").trigger('load')

	$("#soundEnabled").prop('checked', soundsEnabled)
})

function beep() {
	if (soundsEnabled) {
		$(".notificationBeep").trigger('play')
	}
}

function toggleSounds(newEnabled) {
	soundsEnabled = newEnabled

	$.ajax({
		url : ajaxHomePath + "/updatePreferences",
		data : {
			soundsEnabled : newEnabled
		},
		type : "GET",
		dataType : 'json',
		error : commonAjaxErrorHandler,
		success : function() {
			displayAttentionDialog('Your settings were saved.')
		}
	})
}