$(document).ready(function() {
    $(".checkbox_shown_by_default").prop("checked", true);
    $("#checkbox_info").prop("checked", true);
    $("#checkbox_warn").prop("checked", true);
    $("#checkbox_fail").prop("checked", true);
    $("#checkbox_error").prop("checked", true);
    $(".filter_checkbox").on("change", function() {
        $("table").hide();
        if (this.checked) {
            $("." + $(this).val()).show();
        } else {
            $("." + $(this).val()).hide();
        }
        $("table").show();
    }).trigger("change");
});
