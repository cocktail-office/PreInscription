    function onclickbefore(btn) {
      var resultat = false;
	  var onclick = btn.getAttribute("onclick"); 
      if (onclick != null && onclick != "void(0);") {
        disableAnchor(btn,true);
        resultat = true;
      }
	  return resultat;
    }
    
	function disableAnchor(obj, disable){ 
		if(disable) { 
			var href = obj.getAttribute("href"); 
			var onclick = obj.getAttribute("onclick"); 
			if(href && href != "" && href != null){ 
			  obj.setAttribute('href_bak', href); 
			} 
			if (onclick != null && onclick != "void(0);") { 
			  obj.setAttribute('onclick_bak', onclick); 
			  obj.setAttribute('onclick', "void(0);"); 
			} 
			obj.removeAttribute('href'); 
			
			obj.style.color="gray"; 
		} else { 
			if(obj.attributes['onclick_bak']!=null) 
			  obj.setAttribute('onclick', obj.attributes['onclick_bak'].nodeValue); 
			if(obj.attributes['href_bak']!=null) 
			  obj.setAttribute('href', obj.attributes['href_bak'].nodeValue); 
			obj.style.color="blue"; 
		} 
	}

  function precedent(modules,btn,times) {
	  $('btn_suivant').addClassName('btn_nav_disabled');
	  disableAnchor($('btn_suivant'),true);
      if (times == null) times=1;
	      var left=$('roll').getStyle('left');
	      var offset=parseFloat($('RollContainer').getStyle('width'));
	      var index = Math.abs(parseInt(left))/offset;
	      var moduleId = modules[index-times];
	      Element.setStyle($(moduleId),{display:'block'});
	      new Effect.Move($('roll'),{x:offset*times,y:0,duration:times*0.5,
        beforeStart:function(effect) {
            // Activation des modules non visibles
            for (var i=index-times+1;i<=index;i++) {
	          var moduleId = modules[i];
	          Element.setStyle($(moduleId),{display:'block'});
	        }
          btn.addClassName('btn_nav_disabled');
        }, 
        afterFinishInternal:function(effect) {
            // Desactivation des modules non visibles
            for (var i=index-times+1;i<=index;i++) {
	          var moduleId = modules[i];
	          Element.setStyle($(moduleId),{display:'none'});
	        }
	        
	        if (parseFloat(left)>=-offset*times) {
	          btn.addClassName('btn_nav_disabled');
            } else {
              btn.removeClassName('btn_nav_disabled');
              disableAnchor(btn,false);
	        }
	        $('btn_suivant').removeClassName('btn_nav_disabled');
            disableAnchor($('btn_suivant'),false);	        
	      }});
  }
  
  function suivant(modules,btn) {

    $('btn_precedent').addClassName('btn_nav_disabled');
    disableAnchor($('btn_precedent'),true);
      
      var left=$('roll').getStyle('left');
      var offset=parseFloat($('RollContainer').getStyle('width'));
      var nbreModules=$('Modules').cells.length;
      var index = Math.abs(parseInt(left))/offset;
      var moduleId = modules[index+1];
      Element.setStyle($(moduleId),{display:'block'});
      new Effect.Move($('roll'),{x:-offset,y:0,duration:0.5,
        beforeStart:function(effect) {
          btn.addClassName('btn_nav_disabled');
        }, 
        afterFinish:function(effect) {
          var moduleId = modules[index];
          Element.setStyle($(moduleId),{display:'none'});
        if (parseFloat(left)<=-(offset*(nbreModules-2))) {
          btn.addClassName('btn_nav_disabled');
        } else {
          btn.removeClassName('btn_nav_disabled');
          disableAnchor(btn,false);         
        }
	    $('btn_precedent').removeClassName('btn_nav_disabled');
        disableAnchor($('btn_precedent'),false);	        
      }});
  }
  
