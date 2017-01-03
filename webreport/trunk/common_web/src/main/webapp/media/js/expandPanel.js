(function ($) {
    $.fn.expandPanel = function (options) {

	//default vars for the plugin
        var defaults = {
            speed: 1000,
			easing: '',
			title: 'expand panel',
			expandCollapseIcon: 1,
			open: function()
			{
				return;
			},
			close: function()
			{
				return;
			},
			onFirstOpen: function()
			{
				return;
			}
        };
        var options = $.extend(defaults, options);
		// this var stores which button you've clicked
        var toggleClick = $(this);
        toggleClick.next().css("display", "none");
        toggleClick.html("<div><div class='expandIcon' style='float:left;'></div><div class='expandTitle'></div></div>");
        toggleClick.find(".expandTitle").html(options.title);
        toggleClick.find(".expandIcon").addClass("expand");
        // optionally add the class .toggleDiv to each div you want to automatically close
	     // this reads the rel attribute of the button to determine which div id to toggle
        var toggleDiv = toggleClick.attr('rel');
        var loaded = false;
        
        $(this).click(function () {
		     // here we toggle show/hide the correct div at the right speed and using which easing effect
		     $(toggleDiv).slideToggle(options.speed, options.easing, function() {
		     // this only fires once the animation is completed
			 if(options.expandCollapseIcon==1){
				 var toggleDivId = $(toggleDiv).attr("id");
				 var iconDiv = $('[rel$="'+toggleDivId+'"]').find('.expandIcon');//use $ for ends with since rel will have a "#" preceding the classname
				 $(toggleDiv).is(":visible") ? iconDiv.removeClass("expand").addClass("contract") : iconDiv.removeClass("contract").addClass("expand");
				 $(toggleDiv).is(":visible") ? options.open() : options.close();
				 if (!loaded && $(toggleDiv).is(":visible"))
				 {
		    	 	loaded = true;
		    	 	options.onFirstOpen();
				 }
					 
			 }
          });

		  return false;

        });

    };
})(jQuery);
