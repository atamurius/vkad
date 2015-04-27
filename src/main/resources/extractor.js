javascript:void(
    window.open().document.write(
        '<ol>'+ [].map.call($$('.play_btn'), function (a) {
            return '<li><a href="'+ $('input',a).value +'"><span>'+
                $('b', a.parentNode).innerText +"</span> - <span>"+ $('.title', a.parentNode).innerText +"</span></a>"
        }).join("\n")));