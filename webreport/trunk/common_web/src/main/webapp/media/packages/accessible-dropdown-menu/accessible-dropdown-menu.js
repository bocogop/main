/**
 * Adds and removes classes to a list of links to allow keyboard and jaws screen
 * reader accessibility
 * 
 * @param string
 *            menuBarId
 * @param int
 *            mouseOffDelay
 */
function accessibleDropdownMenu(menuBarId, mouseOffDelay) {
	$("li", $(menuBarId)).each(function() {
		var that = $(this)
		that.mouseover(function() {
			that.addClass('hover');
		})
		
		that.mouseout(function() {
			setTimeout(function() {
				that.removeClass('hover')
			}, mouseOffDelay)
			// that.removeClass('hover')
		})

		that.keydown(function(e) {
			var keyCode = e.keyCode || e.which
			var isTopMenu = that.parent().attr('role') == 'menubar'
			
			if (keyCode == $.ui.keyCode.RIGHT || keyCode == $.ui.keyCode.DOWN ||
					keyCode == $.ui.keyCode.LEFT || keyCode == $.ui.keyCode.UP ||
					keyCode == $.ui.keyCode.ESCAPE) {
				e.stopPropagation()
				e.preventDefault()
				tabOff(that)
			}
			
			if (isTopMenu) {
				if (keyCode == $.ui.keyCode.RIGHT) {
					that.next().find('a:first').focus()
				} else if (keyCode == $.ui.keyCode.DOWN) {
					that.find("ul[role='menu']:first").find('a:first').focus()
				} else if (keyCode == $.ui.keyCode.LEFT) {
					that.prev().find('a:first').focus()
				} else if (keyCode == $.ui.keyCode.UP) {
					
				}
			} else {
				if (keyCode == $.ui.keyCode.ESCAPE) {
					that.parentsUntil("[role='menubar']").last().find('a:first').focus()
				} else if (keyCode == $.ui.keyCode.RIGHT) {
					var children = that.find("li")
					if (children.length > 0) {
						children.first().children('a').focus()
					} else {
						that.parentsUntil("[role='menubar']").last().next().find('a:first').focus()
					}
				} else if (keyCode == $.ui.keyCode.DOWN) {
					var nextLi = that.next('li')
					if (nextLi.length > 0) {
						nextLi.children('a').focus()
					} else if (that.siblings().length > 0) {
						that.siblings(":first").children('a').focus()
					} else {
						that.children('a').focus()
					}
				} else if (keyCode == $.ui.keyCode.LEFT) {
					var pathToRootMenu = that.parentsUntil("[role='menubar']")
					if (pathToRootMenu.length > 2) {
						/* I'm in a submenu - need to close and go back to parent menu */
						that.parent().parent().find('a:first').focus()
					} else {
						/* I'm in a main menu - need to jump to previous main menu */
						pathToRootMenu.last().prev().find('a:first').focus()
					}
				} else if (keyCode == $.ui.keyCode.UP) {
					var nextLi = that.prev('li')
					if (nextLi.length > 0) {
						nextLi.children('a').focus()
					} else {
						var pathToRootMenu = that.parentsUntil("[role='menubar']")
						if (pathToRootMenu.length > 2) {
							/* I'm in a submenu - can just loop around to end */
							if (that.siblings().length > 0) {
								that.siblings(":last").children('a').focus()
							} else {
								that.children('a').focus()
							}
						} else {
							/* I'm in the first level menu - need to highlight root item */
							pathToRootMenu.last().find('a:first').focus()
						}
					}
				}
			}
		})
		
		var anchor = $("a:first", that)

		anchor.focus(function() {
			tabOn($(this).parent())
		})
		anchor.blur(function() {
			tabOff($(this).parent())
		})
		anchor.click(function() {
			var el = $(this).parentsUntil("[role='menubar']").not(":last")
			el.removeClass('hover')
			el.hide()
			setTimeout(function() {
				el.show()
			}, 200)
			return true
		})
	})

	addIndexAndIcon(menuBarId)

	function tabOn(el) {
		if (el.is('li')) {
			el.addClass('hover')
			tabOn(el.parent().parent())
		}
	}

	function tabOff(el) {
		if (el.is('li')) {
			el.removeClass('hover')
			tabOff(el.parent().parent())
		}
	}

	/**
	 * Adds z index to the menu popup to make it on top of other images on the
	 * screen. It also appends arrow icons for menus with children
	 * 
	 * @param menuBarId
	 * @createdby jtorreno 10.12.2012
	 */
	function addIndexAndIcon(menuBarId) {
		var downArrowIcon = "<div class='downMenuArrow'></div>";
		var rightArrowIcon = "<div class='rightMenuArrow'></div>";

		$(menuBarId + ' a').each(
				function() {
					var i = 100000

					if ($(this).parent('li').children('ul').size() > 0) {
						i++
						$(this).parent('li').not('ul ul li').children('a')
								.append(downArrowIcon)
						$(this).parent('li ul li').children('a').append(
								rightArrowIcon)
						// modified to be compatible with IE 9 since span had
						// the z-index is necessary to make menu on top of any
						// image on the screen
						$(this).parent('li').children('ul').css('z-index', i)
					}
				})
	}
}