function setDutyStation(dsName, parentName) {
	var dutyStationCell = document.getElementById("dutyStationId")
    if (dutyStationCell != null) {
    	/* Always show duty station field, even with an empty value */
    	dutyStationCell.innerHTML = dsName != null && dsName != "" ? dsName : "Not Selected"
    }
	
	var parentStationCell = document.getElementById("parentDutyStationId")
	var parentStationTitleCell = document.getElementById("parentDutyStationTitle")
	
	if (parentStationCell == null || parentStationTitleCell == null) return
	
	if (parentName != null && parentName != "") {
		parentStationCell.innerHTML = parentName;
		parentStationTitleCell.innerHTML = "Parent:"
	} else {
		parentStationCell.innerHTML = ""
		parentStationTitleCell.innerHTML = ""
	}
}

jQuery.fn.reverse = [].reverse

function logoutConfirm() {
	beep()
	confirmDialog("Are you sure you want to logout?", function() {
		document.location.href = homePath + '/logout.htm'
	})
}

function setSounds(theCheckbox) {
	toggleSounds(theCheckbox.checked)
}

function showPageTitle() {
	$("#appTitle").show()
}
function hidePageTitle() {
	$("#appTitle").hide()
}
function setPageTitleText(pageTitle) {
	$("#pageTitleSpan").text(pageTitle)
}
function setPageTitleHtml(pageTitleHtml) {
	$("#pageTitleSpan").html(pageTitleHtml)
}

function printReports(optionArray) {
	var uid = 'a' + new Date().getTime()
	
	var defaultOption = {
		reportName : "OverrideThis",
		reportParams : [], // override this
		reportOutputFormat : "PDF",
		targetType : 'popup', // "invisible" or "popup"
		invisibleLoadCompleteCallback : null
	}
	
	var finalOptionArray = []
	for (var i = 0; i < optionArray.length; i++) {
		finalOptionArray.push($.extend({}, defaultOption, optionArray[i]))
	}
	
	var total = 0
	for (var i = 0; i < finalOptionArray.length; i++)
		if (finalOptionArray[i].targetType == 'popup')
			total++
	
	var totalScreenWidth = screen.availWidth - 2 * 100
	
	var perWindowWidth = Math.min(totalScreenWidth / total, window.innerWidth)
	var perWindowHeight = Math.min(perWindowWidth, screen.availHeight - 2 * 100)
		
	var windowIndex = 0
	
	for (var i = 0; i < finalOptionArray.length; i++) {
		var finalOptions = finalOptionArray[i]
		var isPopup = finalOptions.targetType == 'popup'
		
		var buildForm = function(docContext) {
			var form = $('<form action="' + protocolHostnamePort + homePath + '/printReport.htm" method="POST"></form>', docContext)
			$('<input type="hidden" name="' + csrfParamName + '" value="' + csrfValue + '" />', docContext).appendTo(form)
			$('<input type="hidden" name="rc:Parameters" value="false" />', docContext).appendTo(form)
			$('<input type="hidden" name="rs:Format" value="' + finalOptions.reportOutputFormat + '" />', docContext).appendTo(form)
			$('<input type="hidden" name="rs:Command" value="Render" />', docContext).appendTo(form)
			$('<input type="hidden" name="rv:Toolbar" value="None" />', docContext).appendTo(form)
			$('<input type="hidden" name="rc:Toolbar" value="false" />', docContext).appendTo(form)
			$('<input type="hidden" name="rv:HeaderArea" value="None" />', docContext).appendTo(form)
			
			$('<input type="hidden" name="reportName" value="' + finalOptions.reportName + '" />', docContext).appendTo(form)
			
			var downloadCompleteCookieName = uid + '_' + i
			$('<input type="hidden" name="downloadCompleteCookieId" value="' + downloadCompleteCookieName + '" />', docContext).appendTo(form)
			
			$.each(finalOptions.reportParams, function(key, value) {
				$.each($.isArray(value) ? value : [value], function(index, item) {
					// console.log('adding param ' + key + ' = ' + item)
					$('<input type="hidden" />', docContext).attr('name', key).attr('value', item).appendTo(form)
				})
			})
			return form
		}
		
		if (isPopup) {
			var popupWindow = window.open("", uid + i + "reportPopupWindow",
				"toolbar=no, scrollbars=yes, resizable=yes, top=100, left="
				+ (100 + windowIndex * perWindowWidth)
				+ ", width=" + perWindowWidth + ", height=" + perWindowHeight)
			var form = buildForm(popupWindow.document)
			
			$("body", popupWindow.document).append(form)
			popupWindow.document.close(); // needed for chrome and safari
			$("form", popupWindow.document).submit()
			windowIndex++
		} else {
			var form = buildForm(document)
			
			showSpinner('Report loading, please wait...')
			
			var invisibleReportLoaded = function() {
				hideSpinner()
				if (finalOptions.invisibleLoadCompleteCallback != null)
					finalOptions.invisibleLoadCompleteCallback()
			}
			
			$('<iframe id="' + uid + 'iframe" style="visibility: hidden; display: none" frameborder="0" name="'
					+ uid + 'iframe"></iframe>').appendTo('body')
			var refreshId = setInterval(function() {
			        var downloadComplete = getCookie(downloadCompleteCookieName)
			        if (downloadComplete == 'true') {
			        	clearInterval(refreshId)
			        	invisibleReportLoaded()
			        }
			    }, 1000)
				
			form.attr('target', uid + 'iframe').appendTo('body').submit()
		}
	}
}

function jumpToVolunteer(id) {
	document.location.href = homePath + '/volunteerEdit.htm?id=' + id
}

function menuVolunteerSelectedCallback(volunteerObj) {
	jumpToVolunteer(volunteerObj.id)
}

function jumpToOrganization(id) {
	document.location.href = homePath + '/organizationEdit.htm?id=' + id
}

function menuOrganizationSelectedCallback(organizationObj) {
	jumpToOrganization(organizationObj.id)
}

function menuOrganizationAddSelectedCallback() {
	document.location.href = homePath + "/organizationCreate.htm"
}

function menuVolunteerAddSelectedCallback() {
	document.location.href = homePath + "/volunteerCreate.htm"
}

function menuDonorAddSelectedCallback(type, searchNameStr) {
	document.location.href = homePath + '/donorCreate.htm?type=' + type;
}

function jumpToDonor(id) {
	document.location.href = homePath + '/donorEdit.htm?id=' + id
}

function menuDonorSelectedCallback(donorObj) {
	jumpToDonor(donorObj.id)
}

