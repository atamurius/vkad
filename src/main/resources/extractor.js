window.open('data:application/json;charset=utf-8,'+ encodeURIComponent( JSON.stringify(
    [].map.call(document.getElementsByClassName('play_btn'), function (a) {
        return {
            url: a.getElementsByTagName('input')[0].value,
            artist: a.parentNode.getElementsByTagName('b')[0].textContent.trim(),
            title: a.parentNode.getElementsByClassName('title')[0].textContent.trim()
        }
    })
)));