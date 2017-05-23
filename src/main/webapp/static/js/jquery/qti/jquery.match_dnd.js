(function ($) {
    $.fn.matchInteractionDnd = function(options) {
    	var settings = $.extend({
    		responseIdentifier: null,
    		formDispatchFieldId: null,
    		maxAssociations: 1,
    		responseValue: null,
    		opened: false 
        }, options );

    	try {
    		if(typeof settings.responseValue != "undefined" && settings.responseValue.length > 0) {
    			drawMatch(this, settings);
    		}
    		if(settings.opened) {
    			match(this, settings);
    		}
    	} catch(e) {
    		if(window.console) console.log(e);
    	}
        return this;
    };
    
    function drawMatch($obj, settings) {
    	var containerId = $obj.attr('id');
    	var associationPairs = settings.responseValue.split(',');
    	var associationEls = jQuery('#' + containerId);
    	for(var i=0; i<associationPairs.length; i++) {
    		var associationPair = associationPairs[i].split(' ');
    		var sourceId = associationPair[0];
    		var targetId = associationPair[1];
    		
    		var sourceEl = jQuery("#" + containerId + " .o_match_dnd_sources li[data-qti-id='" + sourceId + "']");
    		if(needToBeAvailable(sourceEl, containerId)) {
    			sourceEl = jQuery(sourceEl).clone();
    		}
    		var targetEl = jQuery("#" + containerId + " .o_match_dnd_targets ul[data-qti-id='" + targetId + "']");

    		jQuery(sourceEl).addClass('oo-choosed');
    		jQuery(targetEl).addClass('oo-choosed');
    		jQuery(targetEl)
    			.addClass('oo-filled')
    			.append(sourceEl);
    	}
    	
    	recalculate(containerId, settings);
    	
    	if(settings.unrestricted && settings.opened) {
			addNewAssociationBoxAndEvents(containerId, settings);
    	}
    };
    
    function match($obj, settings) {
    	var containerId = $obj.attr('id');
    	initializeSourcePanelEvents(containerId, settings);
    	var sources = jQuery("#" + containerId + " .o_match_dnd_source");
    	initializeSourceEvents(sources, containerId, settings);
    	var targets = jQuery("#" + containerId + " .o_match_dnd_target");
    	initializeTargetEvents(targets, containerId, settings);
    };
    
    initializeSourcePanelEvents = function(containerId, settings) {
    	jQuery("#" + containerId + " .o_match_dnd_sources").droppable({
    		over: function(event, ui) {
    			jQuery(this).addClass('oo-accepted');
    		},
    		out: function(event, ui) {
    			jQuery(this).removeClass('oo-accepted');
    		},
    		drop: function(event, ui) {
    			var box = jQuery(this);
    			box.removeClass('oo-accepted');
    			
    			var choiceEl = jQuery(ui.draggable)
    			var choiceQtiId = choiceEl.data('qti-id');
    			var choiceInSources = box.find("li[data-qti-id='" + choiceQtiId + "']");
    			if(choiceInSources.size() > 0) {
    				if(choiceEl.parents(".o_match_dnd_sources").size() == 0) {
    					choiceEl.remove();
    				}
    			} else {
    				choiceEl.appendTo(box);
    			}
    			recalculate(containerId, settings);
    			setFlexiFormDirty(settings.formDispatchFieldId, false);
    		}
    	}).on('click', {formId: settings.formDispatchFieldId}, setFlexiFormDirtyByListener);
    	
    }
    
    initializeSourceEvents = function(jElements, containerId, settings) {
    	jElements.on('click', function(e, el) {
    		var itemEl = jQuery(this);
    		if(!itemEl.hasClass('oo-choosed') && !itemEl.hasClass('oo-selected')) {
    			itemEl.addClass('oo-selected');
    		} else if(itemEl.parents(".o_match_dnd_targets").size() > 0 && !itemEl.hasClass('oo-dropped-mrk')) {
    			removeSourceFromTarget(itemEl, containerId);
    			recalculate(containerId, settings);
    			setFlexiFormDirty(settings.formDispatchFieldId, false);
    		}
    	}).draggable({
    		containment: "#" + containerId,
    		scroll: false,
    		revert: "invalid",
    		start: function(event, ui) {
    			jQuery(ui.helper).removeClass('oo-dropped-mrk');
    		},
    		stop: function(event, ui) {
    			jQuery(this).css({'left': '0px', 'top': '0px' });
    			jQuery(ui.helper).removeClass('oo-drag');
    		},
    		helper: function() {
    			var choiceEl = jQuery(this);
    			var boxed = choiceEl.parent('.o_match_dnd_target').size() > 0;
    	    	if(!boxed && needToBeAvailable(this, containerId)) {
    	    		choiceEl.removeClass('oo-selected');
    	    		var cloned =  choiceEl.clone();// need some click / drag listeners
    	    		jQuery(cloned)
    	    			.attr('id', 'n' + guid())
    	    			.data('qti-cloned','true')
    	    			.addClass('oo-drag')
    	    			.addClass('oo-drag-mrk');
    	    		return cloned;
    			}
    	    	choiceEl
    	    		.addClass('oo-drag')
    				.addClass('oo-drag-mrk');
    			return choiceEl;
    		}
    	}).on('click', {formId: settings.formDispatchFieldId}, setFlexiFormDirtyByListener);
    }
    
    needToBeAvailable = function(selectedEl, containerId) {
    	var choiceEl = jQuery(selectedEl);
    	if(choiceEl.parents(".o_match_dnd_target").size() > 0) {
    		return false;
    	}
    	var matchMax = choiceEl.data("qti-match-max");
    	var gapId = choiceEl.data("qti-id");
    	var currentUsed = jQuery("#" + containerId + " .o_match_dnd_targets li[data-qti-id='" + gapId + "']").size();
    	return (matchMax == 0 || currentUsed + 1 < matchMax);
    }
    
    initializeTargetEvents = function(jElements, containerId, settings) {
    	jElements.on('click', function(e, el) {
    		var box = jQuery(this);
    		var hasItems = jQuery(".o_associate_item", this).size();
    		if(hasItems == 0) {
    			jQuery("#" + containerId + " .o_match_dnd_sources .oo-selected").each(function(index, selectedEl) {
        			var choiceEl = jQuery(selectedEl);
        	    	if(needToBeAvailable(selectedEl, containerId)) {
        	    		choiceEl.removeClass('oo-selected');
        	    		moveSourceToTarget(choiceEl.clone(), box, containerId);
        			} else {
        				moveSourceToTarget(choiceEl, box, containerId);
        			}
        		});
    		}
			recalculate(containerId, settings);
			setFlexiFormDirty(settings.formDispatchFieldId, false);
    	}).droppable({
    		accept: function(el) {
    			var choiceQtiId = jQuery(el).data('qti-id');
    			//check if the source is already in the target
    			var dropAllowed = jQuery(".o_match_dnd_source[data-qti-id='" + choiceQtiId + "']", this).size() == 0;
    			if(dropAllowed) {
    				var targetMatchMax = jQuery(this).data("qti-match-max");
    				if(targetMatchMax > 0) {
    					var filled = jQuery(".o_match_dnd_source", this).size();
    					if(filled >= targetMatchMax) {
    						dropAllowed = false;
    					}
    				}
    			}
    			return dropAllowed;
    		},
    		over: function(event, ui) {
    			jQuery(this).addClass('oo-accepted');
    		},
    		out: function(event, ui) {
    			jQuery(this).removeClass('oo-accepted');
    		},
    		drop: function(event, ui) {
    			var box = jQuery(this);
    			box.removeClass('oo-accepted');
    			
    			var choiceEl= jQuery(ui.draggable);
    			//prevent 2x the same source
    			var choiceQtiId = choiceEl.data('qti-id');
    			var currentItems = jQuery(".o_match_dnd_source[data-qti-id='" + choiceQtiId + "']", this).size();
    			if(currentItems > 0) {
    				return;
    			}
    			
    			if(ui.helper != null && jQuery(ui.helper).data('qti-cloned') == 'true') {
    	    		choiceEl
    	    			.removeClass('oo-selected')
    	    			.removeClass('oo-drag');
    	    		choiceEl = choiceEl.clone()
    	    		initializeSourceEvents(choiceEl, containerId, settings);
    	    		moveSourceToTarget(choiceEl, box, containerId);
    			} else {
    				choiceEl
    	    			.removeClass('oo-selected')
    	    			.removeClass('oo-drag');
    				moveSourceToTarget(choiceEl, box, containerId);
    			}
    			//add (and remove later) drop marker to prevent click event with Firefox
    			choiceEl.addClass('oo-dropped-mrk');
    			setTimeout(function() {
    				choiceEl.removeClass('oo-dropped-mrk');
    			}, 100);
    			
    			recalculate(containerId, settings);
    			setFlexiFormDirty(settings.formDispatchFieldId, false);
    		}
    	}).on('click', {formId: settings.formDispatchFieldId}, setFlexiFormDirtyByListener);
    };
    
    moveSourceToTarget = function(sourceEl, box, containerId) {
    	var container = box.find("ul.o_match_dnd_target_drop_zone");
    	sourceEl
			.removeClass('oo-selected')
			.css({'width' : 'auto', 'left': '0px', 'top': '0px' })
			.addClass('oo-choosed')
			.appendTo(container);
		box.addClass('oo-filled');
    };
    
    removeSourceFromTarget = function(selectedEl, containerId) {
    	var jSelectedEl = jQuery(selectedEl);
    	jSelectedEl
    		.removeClass('oo-choosed');

    	var gapId = jSelectedEl.data('qti-id');
    	var availableSources = jQuery("#" + containerId + " .o_match_dnd_sources li[data-qti-id='" + gapId + "']").size();
    	if(availableSources == 0) {
    		jSelectedEl
    			.css({'width' : 'auto', 'left': '0px', 'top': '0px' })
    			.appendTo(jQuery('#' + containerId +' .o_match_dnd_sources'));
    	} else {
    		jSelectedEl.remove();
    	}
    };

    recalculate = function(containerId, settings) {
    	settings.matchCount = 0;
        settings.matched = {};
        for(var key in settings.leftMap) {
        	settings.leftMap[key].matchCount = 0;
        }
        for(var key in settings.rightMap) {
        	settings.rightMap[key].matchCount = 0;
        }
        
    	var divContainer = jQuery('#' + containerId);
    	divContainer.find("input[type='hidden']").remove();
    	jQuery("#" + containerId + " .o_match_dnd_target_drop_zone").each(function(index, dropBoxEl) {
    		jQuery(dropBoxEl).find('.o_match_dnd_source').each(function(jndex, droppedEl) {
    			var sourceId = jQuery(droppedEl).data('qti-id');
    			var targetId = jQuery(dropBoxEl).data('qti-id');			
    			var inputElement = jQuery('<input type="hidden"/>')
    					.attr('name', 'qtiworks_response_' + settings.responseIdentifier)
    					.attr('value', sourceId + " " + targetId);
    			divContainer.prepend(inputElement);
    		});
    	});
    };

    function checkMatch(settings, inputElement) {
        withCheckbox(settings, inputElement, function(inputElement, directedPair, left, right) {
            if (inputElement.checked){
                var incremented = false;
                if (left.matchMax != 0 && left.matchMax <= left.matchCount) {
                    inputElement.checked = false;
                }
                else {
                    left.matchCount++;
                    settings.matchCount++;
                    incremented = true;
                }

                if (right.matchMax != 0 && right.matchMax <= right.matchCount) {
                    inputElement.checked = false;
                }
                else {
                    right.matchCount++;
                    if (!incremented) {
                        settings.matchCount++;
                    }
                }
            }
            else {
                settings.matchCount--;
                left.matchCount--;
                right.matchCount--;
            }
            updateDisabledStates(settings);
        });
    }
}( jQuery ));