var stompClient = null;

function setConnected(connected) {
    $("#connect").prop("disabled", connected);
    $("#disconnect").prop("disabled", !connected);
    if (connected) {
        $("#gameResponses").show();
    }
    else {
        $("#gameResponses").hide();
    }
    $("#gameMessages").html("");
}

function connect() {
    var socket = new SockJS('/game-websocket'); // Update the WebSocket URL here
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        setConnected(true);
        console.log('Connected: ' + frame);

        // Subscribe to both rollDice and moveToken topics
        stompClient.subscribe('/topic/game/rollDice', function (response) {
            showGameResponse(JSON.parse(response.body));
        });

        stompClient.subscribe('/topic/game/moveToken', function (response) {
            showGameResponse(JSON.parse(response.body));
        });
    });
}

function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    setConnected(false);
    console.log("Disconnected");
}

function sendRollDice() {
    var message = {
        roomId: $("#roomId").val(),
        playerId: $("#playerId").val()
    };
    stompClient.send("/app/game/rollDice", {}, JSON.stringify(message)); // Update with your WebSocket endpoint
}

function sendMoveToken() {
    var message = {
        roomId: $("#roomId").val(),
        playerId: $("#playerId").val(),
        tokenId: "token1", // Example token ID, replace with actual token ID if needed
        newPosition: 10 // Example new position, replace with actual value
    };
    stompClient.send("/app/game/moveToken", {}, JSON.stringify(message)); // Update with your WebSocket endpoint
}

function showGameResponse(response) {
    var responseRow = "<tr><td>" + response.message + "</td></tr>";
    $("#gameMessages").append(responseRow);
}

$(function () {
    $("form").on('submit', function (e) {
        e.preventDefault();
    });

    $("#connect").click(function() { connect(); });
    $("#disconnect").click(function() { disconnect(); });
    $("#rollDice").click(function() { sendRollDice(); });
    $("#moveToken").click(function() { sendMoveToken(); });
});
