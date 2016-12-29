/*
 * jQuery Dropdown: A simple dropdown plugin that positions relative to the mouse click
 *
 * Based on: https://github.com/claviska/jquery-dropdown
 */
if (jQuery)
	(function($) {
		$.extend($.fn, {
			wrDropdown : function(method, data) {
				switch (method) {
				case 'show':
					show(null, $(this));
					return $(this);
				case 'hide':
					hide();
					return $(this);
				case 'attach':
					return $(this).attr('data-wr-dropdown', data);
				case 'detach':
					hide();
					return $(this).removeAttr('data-wr-dropdown');
				case 'disable':
					return $(this).addClass('wr-dropdown-disabled');
				case 'enable':
					hide();
					return $(this).removeClass('wr-dropdown-disabled');
				}
			}
		});

		function show(event, object) {

			var trigger = event ? $(this) : object, wrDropdown = $(trigger.attr('data-wr-dropdown')), isOpen = trigger
					.hasClass('wr-dropdown-open');

			// In some cases we don't want to show it
			if (event) {
				if ($(event.target).hasClass('wr-dropdown-ignore'))
					return;

				event.preventDefault();
				event.stopPropagation();
			} else {
				if (trigger !== object.target && $(object.target).hasClass('wr-dropdown-ignore'))
					return;
			}
			hide();

			if (isOpen || trigger.hasClass('wr-dropdown-disabled'))
				return;

			// Show it
			trigger.addClass('wr-dropdown-open');
			wrDropdown.data('wr-dropdown-trigger', trigger).show();

			// Position it
			position(event, object);

			// Trigger the show callback
			wrDropdown.trigger('show', {
				wrDropdown : wrDropdown,
				trigger : trigger
			});

		}

		function hide(event) {

			// In some cases we don't hide them
			var targetGroup = event ? $(event.target).parents().addBack() : null;

			// Are we clicking anywhere in a wr-dropdown?
			if (targetGroup && targetGroup.is('.wr-dropdown')) {
				// Is it a wr-dropdown menu?
				if (targetGroup.is('.wr-dropdown-menu')) {
					// Did we click on an option? If so close it.
					if (!targetGroup.is('A'))
						return;
				} else {
					// Nope, it's a panel. Leave it open.
					return;
				}
			}

			// Hide any wr-dropdown that may be showing
			$(document).find('.wr-dropdown:visible').each(function() {
				var wrDropdown = $(this);
				wrDropdown.hide().removeData('wr-dropdown-trigger').trigger('hide', {
					wrDropdown : wrDropdown
				});
			});

			// Remove all wr-dropdown-open classes
			$(document).find('.wr-dropdown-open').removeClass('wr-dropdown-open');
		}

		function hideAndResize(event) {
			// IE-11 patch for weird artifacts that it leaves behind
			// calls hide via listener
			$(window).resize()
		}
		
		function position(event, object) {
			var pageX = event ? event.pageX : object ? object.offset().left : -1
			var pageY = event ? event.pageY : object ? object.offset().top : -1
			var scrollLeft = $(document).scrollLeft()
			var scrollTop = $(document).scrollTop()

			var wrDropdown = $('.wr-dropdown:visible').eq(0), trigger = wrDropdown.data('wr-dropdown-trigger'), hOffset = trigger ? parseInt(
					trigger.attr('data-horizontal-offset') || 0, 10)
					: null, vOffset = trigger ? parseInt(trigger.attr('data-vertical-offset') || 0, 10) : null;

			if (wrDropdown.length === 0 || !trigger)
				return;

			wrDropdown.css({
				left : wrDropdown.hasClass('wr-dropdown-anchor-right') ? pageX - scrollLeft
						- (wrDropdown.outerWidth(true) - trigger.outerWidth(true))
						- parseInt(trigger.css('margin-right'), 10) + hOffset : pageX - scrollLeft
						+ parseInt(trigger.css('margin-left'), 10) + hOffset,
				top : pageY - scrollTop + trigger.outerHeight(true) - parseInt(trigger.css('margin-top'), 10) + vOffset
			})
		}

		function keydown(event) {
			if (event.which == 13) {
				event.preventDefault();
				event.stopPropagation();
				show(null, $(event.currentTarget))
				var firstLink = $(document).find('.wr-dropdown:visible').find("a").eq(0)
				firstLink.focus().addClass('active')
			}
		}

		function keydownLink(event) {
			var $a = $(event.currentTarget)
			var $ul = $a.closest('ul')
			var $allLinks = $ul.find('a')
			var linkIndex = $allLinks.index($a)
			
			switch (event.which) {
			case $.ui.keyCode.UP:
				event.preventDefault()
				event.stopPropagation()
				$ul.find('a.active').removeClass('active')
				linkIndex--
				if (linkIndex < 0) linkIndex = $allLinks.length - 1
				$allLinks.eq(linkIndex).focus().addClass('active')
				break
			case $.ui.keyCode.DOWN:
				event.preventDefault()
				event.stopPropagation()
				$ul.find('a.active').removeClass('active')
				linkIndex = (linkIndex + 1) % $allLinks.length
				$allLinks.eq(linkIndex).focus().addClass('active')
				break
			case $.ui.keyCode.ESCAPE:
				event.preventDefault()
				event.stopPropagation()
				hide(event)
				break
			case $.ui.keyCode.TAB:
				hide(event)
				break;
			}
		}

		function mouseoverLink(event) {
			$(event.currentTarget).closest('.wr-dropdown-menu').find('a.active').removeClass('active')
		}
		
		$(document).on('click.wr-dropdown', '[data-wr-dropdown]', show);
		$(document).on('keydown.wr-dropdown', '[data-wr-dropdown]', keydown);
		$(document).on('click.wr-dropdown', hideAndResize);

		$(document).on('mouseover.wr-dropdown', '.wr-dropdown-menu a', mouseoverLink);
		$(document).on('keydown.wr-dropdown', '.wr-dropdown-menu a', keydownLink);

		document.addEventListener('scroll', hide, true);
		$(window).on('resize', hide);

	})(jQuery);