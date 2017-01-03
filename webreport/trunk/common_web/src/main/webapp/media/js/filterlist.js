/*==================================================*
 $Id: filterlist.js,v 1.3 2003/10/08 17:13:49 pat Exp $
 Copyright 2003 Patrick Fitzgerald
 http://www.barelyfitz.com/webdesign/articles/filterlist/

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *==================================================*/

function filterlist(selectobj, opts) {

	var defaults = {
		// Which parts of the select list do you want to match?
		matchText : true,
		matchValue : false,
		matchTitle : false,
		// Flags for regexp matching.
		// "i" = ignore case; "" = do not ignore case
		flags : 'i'
	}

	this.options = $.extend(defaults, opts)
	this.selectobj = selectobj

	// ==================================================
	// METHODS
	// ==================================================

	this.init = function() {
		// This method initializes the object.
		// This method is called automatically when you create the object.
		// You should call this again if you alter the selectobj parameter.

		if (!this.selectobj) {
			console.log('selectobj not defined')
			return
		}
		if (!this.selectobj.options) {
			console.log('selectobj.options not defined')
			return
		}

		// Make a copy of the select list options array
		this.optionscopy = new Array()
		for (var i = 0; i < this.selectobj.options.length; i++) {
			// Create a new Option
			this.optionscopy[i] = new Option()
			this.optionscopy[i].disabled = selectobj.options[i].disabled
			this.optionscopy[i].className = selectobj.options[i].className
			this.optionscopy[i].text = selectobj.options[i].text
			this.optionscopy[i].title = selectobj.options[i].title

			// Set the value for the Option.
			// If the value wasn't set in the original select list,
			// then use the text.
			if (selectobj.options[i].value) {
				this.optionscopy[i].value = selectobj.options[i].value;
			} else {
				this.optionscopy[i].value = selectobj.options[i].text;
			}
		}
	}

	// --------------------------------------------------
	this.reset = function() {
		// This method resets the select list to the original state.
		// It also unselects all of the options.
		this.set('')
	}

	// --------------------------------------------------
	this.set = function(pattern) {
		// This method removes all of the options from the select list,
		// then adds only the options that match the pattern regexp.
		// It also unselects all of the options.

		// Clear the select list so nothing is displayed
		this.selectobj.options.length = 0;

		// Set up the regular expression.
		// If there is an error in the regexp,
		// then return without selecting any items.
		var regexp;
		try {
			// Initialize the regexp
			regexp = new RegExp(pattern, this.options.flags);
		} catch (e) {
			console.log(e)

			return
		}

		var newHtml = []
		// Loop through the entire select list and
		// add the matching items to the select list
		for (var loop = 0; loop < this.optionscopy.length; loop++) {
			var option = this.optionscopy[loop];

			// Check if we have a match
			if ((this.options.matchText && regexp.test(option.text))
					|| (this.options.matchValue && regexp.test(option.value))
					|| (this.options.matchTitle && regexp.test(option.title))) {

				// We have a match, so add this option to the select list
				// and increment the index
				newHtml.push('<option value="'
						+ option.value
						+ '"'
						+ (option.disabled ? ' disabled="disabled"' : '')
						+ (option.className ? ' class="' + option.className
								+ '"' : '')
						+ (option.title ? ' title="' + escapeHTML(option.title)
								+ '"' : '') + '>' + option.text + '</option>')
			}
		}
		this.selectobj.innerHTML = newHtml.join('')
	}

	this.init()

}
