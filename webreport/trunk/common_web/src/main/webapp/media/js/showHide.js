(function($) {
	$.fn.showHide = function(options) {

		// default vars for the plugin
		var defaults = {
			speed : 1000,
			easing : '',
			changeText : 0,
			showText : 'Show',
			hideText : 'Hide',
			showCallback : null,
			hideCallback : null
		}
		var options = $.extend(defaults, options)
		var toggleClick = $(this)
		var toggleDiv = toggleClick.attr('rel')
		var isVis = $(toggleDiv).is(":visible")

		$(
				'<img src="'
						+ imgHomePath
						+ '/plus.gif" class="showIcon" border="0" style="display:none" align="absmiddle" alt="'
						+ options.showText + '" />').insertBefore($(this))
		$(
				'<img src="'
						+ imgHomePath
						+ '/minus.gif" class="hideIcon" border="0" style="display:none" align="absmiddle" alt="'
						+ options.hideText + '" />').insertBefore($(this))

		toggleClick.prevAll(isVis ? '.hideIcon:first' : '.showIcon:first')
				.show()
		toggleClick.prevAll(isVis ? '.showIcon:first' : '.hideIcon:first')
				.hide()
		toggleClick.text(isVis ? options.hideText : options.showText)
									
		var clickFunc = function() {
			$(toggleDiv).slideToggle(
					options.speed,
					options.easing,
					function() {
						var isVis = $(toggleDiv).is(":visible")
						toggleClick.prevAll(
								isVis ? '.hideIcon:first' : '.showIcon:first')
								.show()
						toggleClick.prevAll(
								isVis ? '.showIcon:first' : '.hideIcon:first')
								.hide()
						toggleClick.text(isVis ? options.hideText
								: options.showText)
						if (options.showCallback && isVis)
							options.showCallback()
						if (options.hideCallback && !isVis)
							options.hideCallback()
					})
			return false
		}

		toggleClick.prevAll('.showIcon:first').click(clickFunc)
		toggleClick.prevAll('.hideIcon:first').click(clickFunc)
		toggleClick.click(clickFunc)
	}
})(jQuery)
