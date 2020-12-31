
function btnUpHandler() {
  return function(event) {
    if (clickedBtn != null) {
      btnUp(clickedBtn);
    }
  }
}

function btnKeyDownHandler() {
  return function(event) {
    if (event == null) {
      event = window.event;
    }
    var elem;
    if (event.target == null) {
      elem = event.srcElement;
    } else {
      elem = event.target;
    }
    if (elem.nodeName.toLowerCase() == "input") {
      if (elem.type == "image") {
        if (elem.src != null) {
          if (elem.src.indexOf("renderButton") != -1) {
            elem.hideFocus = true;
            if (event.keyCode != null) {
              if (event.keyCode == 32 || event.keyCode == 13) {
                elem.src = elem.src.replace(/(&f)?(&h)?(&p)?$/, "$1$2&p");
                clickedBtn = elem;
              }
            }
          }
        }
      }
    }
  }
}

function btnKeyUpHandler() {
  return function(event) {
    if (event == null) {
      event = window.event;
    }
    if (clickedBtn != null) {
      if (event.keyCode != null) {
        if (event.keyCode == 32 || event.keyCode == 13) {
          clickedBtn.src = clickedBtn.src.replace(/&p/, "");
          clickedBtn = null;
        }
      }
    }
  }
}

function btnHandler(handlerFn) {
  return function(event) {
    if (event == null) {
      event = window.event;
    }
    var elem;
    if (event.target == null) {
      elem = event.srcElement;
    } else {
      elem = event.target;
    }
    if (elem == null) {
      return;
    }
    if (elem.nodeName.toLowerCase() == "input") {
      if (elem.type == "image") {
        if (elem.src != null) {
          if (elem.src.indexOf("renderButton") != -1) {
            handlerFn(elem);
          }
        }
      }
    }
  }
}

function initBtnImgs(btn) {
  btn.onload = null;
  btn._images_ = new Array();
  btn.hideFocus = true;
  if (btn.disabled) {
    return;
  }
  btn.blur();
  var baseSrc = btn.src.replace(/(&f)?(&h)?(&p)?$/, "");
  btn._images_[0] = new Image();
  btn._images_[0].src = baseSrc;
  btn._images_[1] = new Image();
  btn._images_[1].src = baseSrc + "&h";
  btn._images_[2] = new Image();
  btn._images_[2].src = baseSrc + "&f";
  btn._images_[3] = new Image();
  btn._images_[3].src = baseSrc + "&f&h";
  btn._images_[4] = new Image();
  btn._images_[4].src = baseSrc + "&p";
  btn._images_[5] = new Image();
  btn._images_[5].src = baseSrc + "&h&p";
  btn._images_[6] = new Image();
  btn._images_[6].src = baseSrc + "&f&p";
  btn._images_[7] = new Image();
  btn._images_[7].src = baseSrc + "&f&h&p";
}

function btnFocus(btn) {
  if (btn.src.indexOf("&d", 0) == -1) {
    btn.src = btn.src.replace(/(&f)?(&h)?(&p)?$/, "&f$2$3");
  }
}

function btnBlur(btn) {
  if (! btn.disabled) {
    btn.src = btn.src.replace(/&f/, "");
  }
}

function btnDown(btn) {
  if (! btn.disabled) {
    clickedBtn = btn;
    btn.src = btn.src.replace(/(&f)?(&h)?(&p)?$/, "$1$2&p");
  }
}

function btnUp(btn) {
  if (! btn.disabled) {
    clickedBtn = null;
    btn.src = btn.src.replace(/&p/, "");
  }
}

function btnOver(btn) {
  if (! btn.disabled) {
    btn.src = btn.src.replace(/(&f)?(&h)?(&p)?$/, "$1&h$3");
  }
}

function btnOut(btn) {
  if (! btn.disabled) {
    btn.src = btn.src.replace(/&h/, "");
  }
}

function btnKeyDown(event,btn) {
  if (event != null) {
    if (event.keyCode != null) {
      if (event.keyCode == 32 || event.keyCode == 13) {
        if (! btn.disabled) {
          btn.src = btn.src.replace(/(&f)?(&h)?(&p)?$/, "$1$2&p");
        }
      }
    }
  }
}

function btnKeyUp(event,btn) {
  if (event != null) {
    if (event.keyCode != null) {
      if (event.keyCode == 32 || event.keyCode == 13) {
        btn.src = btn.src.replace(/&p/, "");
      }
    }
  }
}

function keypress(event,buttonId) {
  if (event.keyCode != null) {
    if (event.keyCode == 13) {
      if (buttonId != null) {
        var elem = document.getElementById(buttonId);
        if (elem != null) {
          elem.click();
        }
      }
    }
  }
}

function blurInputsRecursive(elem) {
  if (elem.nodeName.toLowerCase() == "input") {
    elem.blur();
    elem.disabled = true;
  } else {
    var kids = elem.childNodes;
    if (kids == null) {
      return;
    }
    var i;
    for (i = 0; i < kids.length; i++) {
      blurInputsRecursive(kids[i]);
    }
  }
}

function searchLoading(obj) {
  if (obj == null) {
    return;
  }
  var prefix = obj.id;
  var end = prefix.lastIndexOf(':searchform:') + 1;
  if (end == 0) {
    prefix = "";
  } else {
    prefix = prefix.substring(0, end);
  }
  var elem;
  elem = document.getElementById(prefix + 'searchform:title');
  if (elem != null) {
    elem.innerHTML = "<img style=\"display:inline;position:relative;top:3px;margin-right:5px\" src=\"images/loading.gif\" alt=\"\"/>Loading...";
  }
  elem = document.getElementById(prefix + 'searchform:applyButton');
  if (elem != null) {
    elem.disabled = true;
  }
  elem = document.getElementById(prefix + 'searchform:nextButton');
  if (elem != null) {
    elem.disabled = true;
  }
  elem = document.getElementById(prefix + 'searchform:prevButton');
  if (elem != null) {
    elem.disabled = true;
  }
  elem = document.getElementById(prefix + 'searchform');
  if (elem != null) {
    elem.className += " busy";
  }
  elem = document.getElementById(prefix + 'searchform:filter:0');
  blurInputsRecursive(elem);
  return true;
}

function addClass(element, className) {
    if (!hasClass(element, className)) {
        if (element.className) element.className += " " + className;
        else element.className = className;
    }
};

function removeClass(element, className) {
    var regexp = new RegExp("(^|\\s)" + className + "(\\s|$)");
    element.className = element.className.replace(regexp, "$2");
};

function hasClass(element, className) {
    var regexp = new RegExp("(^|\\s)" + className + "(\\s|$)");
    return regexp.test(element.className);
};

function choosetab(anchor,groupid,tabid,styleclass,inputid) {
  var group = document.getElementById(groupid);
  var active = styleclass + "_active";
  var inactive = styleclass + "_inactive";
  var tab = document.getElementById(tabid);
  if (hasClass(anchor.parentNode, active)) {
    return false;
  }
  if (group != null && tab != null) {
    var groupchildren = group.childNodes;
    for (i = 0; i < groupchildren.length; i++) {
      var elem = groupchildren[i];
      if (elem.nodeName.toLowerCase() == "ul") {
        var ulchildren = elem.childNodes;
        for (j = 0; j < ulchildren.length; j++) {
          var subelem = ulchildren[j];
          if (subelem.nodeName.toLowerCase() == "li" && hasClass(subelem, active)) {
            removeClass(subelem, active);
            addClass(subelem, inactive);
          }
        }
      } else if (elem.nodeName.toLowerCase() == "div" && hasClass(elem, active)) {
        removeClass(elem, active);
        addClass(elem, inactive);
      }
    }
    removeClass(tab, inactive);
    addClass(tab, active);
    removeClass(anchor.parentNode, inactive);
    addClass(anchor.parentNode, active);
  }
  if (inputid != null) {
    var elem = document.getElementsByName(inputid)[0];
    var idx = tabid.lastIndexOf(":");
    if (idx == -1) {
      elem.value = tabid;
    } else {
      elem.value = tabid.substr(idx + 1);
    }
    if (elem.onchange != null) {
      elem.onchange();
    }
  }
  return false;
}

function forceclick(node,event) {
  var nodechildren = node.childNodes;
  for (i = 0; i < nodechildren.length; i++) {
    var elem = nodechildren[i];
    if (elem.nodeName.toLowerCase() == "a") {
      elem.focus();
      return elem.onclick();
    }
  }
  return false;
}

function hover(object) {
  if (object.className != null) {
    object.className = object.className.replace(/_hoverable_/, "_hover_");
  }
}

function unhover(object) {
  if (object.className != null) {
    object.className = object.className.replace(/_hover_/, "_hoverable_");
  }
}

function hover_activator(item) {
  return function go() { hover(item); };
}

function unhover_activator(item) {
  return function go() { unhover(item); };
}

function activateHover() {
  var elements = getElementsByPartialClass("_hoverable_");
  var len = elements.length;
  for (i in elements) {
    var item = elements[i];
    item.onmouseout = unhover_activator(item);
    item.onmouseover = hover_activator(item);
  }
}

function getElementsByClass(searchClass,node,tag) {
  var classElements = new Array();
  if (node == null)
    node = document;
  if (tag == null)
    tag = '*';
  var els = node.getElementsByTagName(tag);
  var elsLen = els.length;
  var pattern = new RegExp("(^|\\s)"+searchClass+"(\\s|$)");
  for (i = 0, j = 0; i < elsLen; i++) {
    if (pattern.test(els[i].className) ) {
      classElements[j] = els[i];
      j++;
    }
  }
  return classElements;
}

function getElementsByPartialClass(searchClass,node,tag) {
  var classElements = new Array();
  if (node == null)
    node = document;
  if (tag == null)
    tag = '*';
  var els = node.getElementsByTagName(tag);
  var elsLen = els.length;
  for (i = 0, j = 0; i < elsLen; i++) {
    if (els[i].className.match(searchClass)) {
      classElements[j] = els[i];
      j++;
    }
  }
  return classElements;
}

function toggleMsgDetail(buttonId,detailBoxId) {
  var element = document.getElementById(detailBoxId);
  var btn = document.getElementById(buttonId);
  if (element != null && btn != null) {
    var cn = element.className;
    if (cn != null) {
      if (cn == "detail hidden") {
        btn.innerHTML = "Hide details";
        element.className = "detail";
      } else {
        btn.innerHTML = "Show details";
        element.className = "detail hidden";
      }
    }
  }
}