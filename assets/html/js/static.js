/*::::::::::::::::::::::::::::::::::::::::::::::::*/
/*::::::: Kirill Miniaev | Miniaev Design ::::::::*/
/*: kirill@miniaevdesign.com | miniaevdesign.com :*/
/*::::::::::::::::::::::::::::::::::::::::::::::::*/
$(document).ready(function() {
    $(document).on('click','.qablock h2', function() {
        $(this).siblings('p').toggle();
    });
});