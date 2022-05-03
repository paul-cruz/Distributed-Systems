$(document).ready(function () {
    console.log("ready!");

    // find elements on the page
    var banner = $("#banner-message");
    var button = $("#submit_button");
    var searchBox = $("#search_text");
    var resultsTable = $("#results table tbody");
    var resultsWrapper = $("#results");
    var noResultsError = $("#no_results_error");

    // handle search click
    button.on("click", function () {
        banner.addClass("alt");
        // send request to the server
        $.ajax({
            method: "POST",
            contentType: "application/json",
            data: createRequest(),
            url: "procesar_datos",
            dataType: "json",
            success: onHttpResponse
        });
    });

    function createRequest() {
        var searchQuery = searchBox.val();

        // Search request to the server
        var frontEndRequest = {
            search_query: searchQuery,
        };

        return JSON.stringify(frontEndRequest);
    }

    function onHttpResponse(data, status) {
        if (status === "success") {
            console.log(data);
            addResults(data);
        } else {
            alert("Error connecting to the server " + status);
        }
    }

    /*
        Add results from the server to the html or how an error message
     */
    function addResults(data) {
        resultsTable.empty();

        var response = data.rsp;
        //var title = data.title;
        noResultsError.hide();
        resultsWrapper.show();
        //resultsTable.append("<tr><td>Datos recibidos: " + title + "</td><td> Numero de palabras: " + score + "</td></tr>");
        resultsTable.append("<p>" + response + "</p>");
    }
});
