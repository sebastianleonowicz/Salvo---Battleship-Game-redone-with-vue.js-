//GET ME THE STATUS AND FILL SOT (fillSOT())
var firstGP;
var canPlaceShips;
var allShipsPlaced;
var currentTurnNumber;
var isItGameOver;
function fillSOT(status){
  
    status.forEach(function(gpStatus){
        if(gpStatus.gpId == gamePlayerNumber){
            console.log(gpStatus.status);
            firstGP = gpStatus.status.firstGP;
            console.log(firstGP);
            canPlaceShips = gpStatus.status.canPlaceShips;
            allShipsPlaced = gpStatus.status.allShipsPlaced;
            currentTurnNumber = gpStatus.status.currentTurnNumber;
            isItGameOver = gpStatus.status.isItGameOver;
            console.log("first "+firstGP, "can place ships "+canPlaceShips, "ships placed "+ allShipsPlaced, "turnNo "+currentTurnNumber,"gameOver "+ isItGameOver);
        }
        
    })
}



//        VARIABLES FOR CREATING THE GRID FOR PLAYER
var gamePlayerNumber;
var gamePlayer;
var createdURL;

//    var for end of url
var search = location.search;

//execute function for making the number used to generate json url
paramObj(search);

//function takes location and creates a number to be added to json url
function paramObj(search) {
    var obj = {};
    var reg = /(?:[?&]([^?&#=]+)(?:=([^&#]*))?)(?:#.*)?/g;

    search.replace(reg, function (match, param, val) {
        obj[decodeURIComponent(param)] = val === undefined ? "" : decodeURIComponent(val);
    });

    gamePlayerNumber = obj.gp;

    inputFunction(gamePlayerNumber);
    return obj;
}

//TAKES NUMBER FROM INPUT, STORES IN VAR playerNumber

function inputFunction(gamePlayerNumber) {

    console.log("Nice fresh url for AJAX....");
    console.log("creating URL for GP " + gamePlayerNumber)
    makeUrl();

}
//CREATES URL USED IN LATER AJAX CALL
function makeUrl() {
    createdURL = ('http://localhost:8080/api/game_view/' + gamePlayerNumber);
    console.log(createdURL);

    makeAJAX(createdURL);
    return 'http://localhost:8080/api/game_view/' + gamePlayerNumber;

}
//------AJAX-----------
//EXECUTING AJAX USING GENERATED URL
function makeAJAX(createdURL) {
    console.log("ajax");
    $.ajax({
        dataType: "json",
        url: createdURL,
        cache: true,
        data: JSON,
        success: function (data2) {
            data = data2;
            console.log(data);

            var shipLocations = data2.ships;
            //            console.log(shipLocations);


            //        CREATES GRID ON AJAX SUCCESS

            createGrid(data, $("#demo"));
            createGrid2(data, $("#salvoes"));
            
            var gameStatus = data2.gameStatus;
            fillSOT(gameStatus);
         
            var hitsAndSinks = data2.hitsAndSinks;

            hitsAndSinksTable(hitsAndSinks);
            
        }
    });
}

//CREATES TABLE WITH HITS AND SINKS
function hitsAndSinksTable(data) {
    //    console.log(data);
    for (i = 0; i < data.length; i++) {
        //        console.log(data[i]);
        //        console.log(data[i].turnNumber);
        console.log(data[i].gamePlayers);
        var row = $("<div>");

        row.addClass("test");
        $(".bot").append(row);

        var col1 = $("<div>");
        col1.addClass("test1");
        $(col1).html(data[i].turnNumber);
        var col2 = $("<div>");
        col2.addClass("test2");
        var col3 = $("<div>");
        col3.addClass("test2");
        var col4 = $("<div>");
        col4.addClass("test2");
        var col5 = $("<div>");
        col5.addClass("test2");

        row.append(col1, col2, col3, col4, col5);

        for (j = 0; j < data[i].gamePlayers.length; j++) {
//            console.log(data[i].gamePlayers[j].GPid);
            if (data[i].gamePlayers[j].GPid == gamePlayerNumber) {
//                console.log("it's me");
//                console.log(data[i].gamePlayers[j].shipsLeft);
                $(col3).html(data[i].gamePlayers[j].shipsLeft);
//                console.log(data[i].gamePlayers[j].hitsOnThisGP);
                
                for(k=0; k<data[i].gamePlayers[j].hitsOnThisGP.length; k++){
//                      console.log(data[i].gamePlayers[j].hitsOnThisGP[k]);
                    var shipName = data[i].gamePlayers[j].hitsOnThisGP[k].shipName;
                    var hitTimes = data[i].gamePlayers[j].hitsOnThisGP[k].hitTimes;
                    
                    if(data[i].gamePlayers[j].hitsOnThisGP[k].shipSunk == true){
                    var sunk = "Sunk!";    
                    }else{
                        var sunk ="";
                    }
                    
                    var hitsToAppend = shipName + " hit "+ hitTimes +" times "+ sunk +" ";
                    $(col2).append(hitsToAppend);
                }
                
            }else{
                $(col5).html(data[i].gamePlayers[j].shipsLeft);
                
                for(k=0; k<data[i].gamePlayers[j].hitsOnThisGP.length; k++){
//                      console.log(data[i].gamePlayers[j].hitsOnThisGP[k]);
                    var shipName = data[i].gamePlayers[j].hitsOnThisGP[k].shipName;
                    var hitTimes = data[i].gamePlayers[j].hitsOnThisGP[k].hitTimes;
//                    console.log(data[i].gamePlayers[j].hitsOnThisGP[k].shipSunk);
                    
                    if(data[i].gamePlayers[j].hitsOnThisGP[k].shipSunk == true){
                    var sunk = "Sunk!";    
                        
                    }else{
                        var sunk ="";
                    }
                    
                    var hitsToAppend = shipName + " hit "+ hitTimes +" times "+ sunk +" ";
                    $(col4).append(hitsToAppend);
                }
            }
        }

    }
}

//CREATES GRID FOR THE PLAYER
var letters = ["0", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J"];
var numbers = [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10];


//CREATES GRID FOR GAME PLAYER'S SHIPS
function createGrid(data, location) {
    //      console.log(data);

    numbers.forEach(function (number) {
        //        console.log(number);

        var rowTemplate = $("<div>");
        rowTemplate.addClass("flex-grid-thirds");
        location.append(rowTemplate);
        var rowClass = ("row" + number);
        rowTemplate.addClass("row" + number);

        letters.forEach(function (letter) {
            //            console.log(letter);
            var colTemplate = $("<div>");
            colTemplate.addClass("col");
            var classSquare = (letter + number);
            colTemplate.addClass(classSquare);
            colTemplate.addClass("empty");
            colTemplate.attr("data-location", classSquare);
            rowTemplate.append(colTemplate);

            if (number == 0) {
                var text = letter;
                colTemplate.append(text);
                colTemplate.removeClass("empty");
                colTemplate.addClass("letter");
            }
            if (letter == "0") {
                var text = number;
                colTemplate.append(text);
                colTemplate.removeClass("empty");
                colTemplate.addClass("number");
            }

            for (i = 0; i < data.ships.length; i++) {
                //                  console.log(data.ships[i]);
                for (j = 0; j < data.ships[i].locations.length; j++) {
                    //                      console.log(data.ships[i].locations[j])
                    if (data.ships[i].locations[j] == classSquare) {

                        //                        console.log("SHIP ON " + classSquare);
                        colTemplate.css("cssText", "background-color: yellow");
                        colTemplate.removeClass("empty");
                        colTemplate.addClass("ship");
                        colTemplate.html("ship");


                        for (k = 0; k < data.salvoes.length; k++) {
                            for (l = 0; l < data.salvoes[k].locations.length; l++) {
                                //                                console.log("iterated thru salvoes for every ship field");
                                if (data.salvoes[k].locations[l] == data.ships[i].locations[j] && !(data.salvoes[k].player == gamePlayerNumber)) {
                                    //                                    console.log("got hit" + data.salvoes[k].locations[l]);
                                    colTemplate.css("cssText", "background-color: pink");
                                    colTemplate.removeClass("ship");
                                    colTemplate.addClass("hit");
                                    colTemplate.html(("ship hit on turn " + data.salvoes[k].turn))

                                }

                            }
                        }

                    }
                    if (!(data.ships[i].locations[j] == classSquare) && colTemplate.hasClass("empty")) {
                        //                          colTemplate.html("no ship here")
                        for (k = 0; k < data.salvoes.length; k++) {
                            for (l = 0; l < data.salvoes[k].locations.length; l++) {
                                if (data.salvoes[k].locations[l] == classSquare && !(data.salvoes[k].player == gamePlayerNumber)) {
                                    colTemplate.html("missed ship " + data.salvoes[k].turn);
                                    colTemplate.css("cssText", "background-color: violet");
                                }
                            }
                        }

                    }

                }
            }

        })

    })
}

//
function createGrid2(data, location) {
    console.log("CREATING GRID2");
    numbers.forEach(function (number) {
        //        console.log(number);

        var rowTemplate = $("<div>");
        rowTemplate.addClass("flex-grid-thirds");
        location.append(rowTemplate);
        var rowClass = ("row" + number);
        rowTemplate.addClass("row" + number);

        letters.forEach(function (letter) {
            //            console.log(letter);
            var colTemplate = $("<div>");
            colTemplate.addClass("col");
            var classSquare = (letter + number);
            colTemplate.addClass(classSquare);
            colTemplate.addClass("empty");
            colTemplate.attr("data-location", classSquare);
            rowTemplate.append(colTemplate);

            if (number == 0) {
                var text = letter;
                colTemplate.append(text);
            }
            if (letter == "0") {
                var text = number;
                colTemplate.append(text);
            }
            for (i = 0; i < data.salvoes.length; i++) {
                //                  console.log(data.ships[i]);
                for (j = 0; j < data.salvoes[i].locations.length; j++) {

                    if (data.salvoes[i].locations[j] == classSquare && data.salvoes[i].player == gamePlayerNumber) {


                        colTemplate.css("cssText", "background-color: green");
                        colTemplate.removeClass("empty");
                        colTemplate.addClass("filled");
                        var turnText = (" Salvo TURN " + data.salvoes[i].turn);
                        colTemplate.append(turnText);
                        colTemplate.addClass("ship hit");
                        //                        console.log("SALVO ON " + classSquare);
                    }
                }
            }
        })
    })
}

function whosPlaying() {
    console.log(data);
    for (i = 0; i < data.gamePlayers.length; i++) {
        //          console.log(data.gamePlayers[i].player.name)
        //          console.log(data.gamePlayers[i + 1].player.name)
        var txt1 = data.gamePlayers[i].player.name + " VS ";
        var txt2 = data.gamePlayers[i + 1].player.name;
        var txt3 = "(YOU)"

        if (data.gamePlayers[i].id.toString() == gamePlayerNumber) {
            $("#players").append(txt3);
        }
        $("#players").append(txt1);

        $("#players").append(txt2);
        if (data.gamePlayers[i + 1].id == gamePlayerNumber) {
            $("#players").append(txt3);
        }

        {
            break;
        }
    }
}

$("#logOut").click(function () {
    console.log("log out button works");
    $.post("/api/logout").done(function () {
        console.log("logged out");
        $("#logInForm").css("cssText", "display:block");
        $("#username").html("");

    })
    window.location.href = 'games.html?firstname=&lastname=';
})

//CREATES  JSON AND SENDS IT TO /API
var shipObj_1_Location = [];
var shipObj_1 = new Object();
shipObj_1.shipType = "Carrier";
shipObj_1.shipLocations = shipObj_1_Location;

var shipObj_2_Location = [];
var shipObj_2 = new Object();
shipObj_2.shipType = "Battleship";
shipObj_2.shipLocations = shipObj_2_Location;

var shipObj_3_Location = [];
var shipObj_3 = new Object();
shipObj_3.shipType = "Submarine";
shipObj_3.shipLocations = shipObj_3_Location;

var shipObj_4_Location = [];
var shipObj_4 = new Object();
shipObj_4.shipType = "Destroyer";
shipObj_4.shipLocations = shipObj_4_Location;

var shipObj_5_Location = [];
var shipObj_5 = new Object();
shipObj_5.shipType = "Patrol Boat";
shipObj_5.shipLocations = shipObj_5_Location;

//[{},{},...,{}]
var shipJSON = [];
shipJSON.push(shipObj_1);
shipJSON.push(shipObj_2);
shipJSON.push(shipObj_3);
shipJSON.push(shipObj_4);
shipJSON.push(shipObj_5);
//console.log(shipJSON);

//POSTING SHIPS
$("#postShips").click(function () {
    var url = "/api/games/players/"+gamePlayerNumber+"/ships";
    $.post({
        url: url,
        data: JSON.stringify(shipJSON),
        dataType: "text",
        contentType: "application/json"
    })
})
//RESETTING SHIPS
$("#resetShips").click(function () {
    var shipObj_1_Location = [];
    var shipObj_2_Location = [];
    var shipObj_3_Location = [];
    var shipObj_4_Location = [];
    var shipObj_5_Location = [];
    var carrierClicked = 0;
    var carrierSet = 0;
    var battleshipClicked = 0;
    var battleshipSet = 0;
    var submarineClicked = 0;
    var submarineSet = 0;
    var destroyerClicked = 0;
    var destroyerSet = 0;
    var patrolClicked = 0;
    var patrolSet = 0;
    console.log("Locations emptied, ships unclicked");

})

var carrierClicked = 0;
var carrierSet = 0;
var battleshipClicked = 0;
var battleshipSet = 0;
var submarineClicked = 0;
var submarineSet = 0;
var destroyerClicked = 0;
var destroyerSet = 0;
var patrolClicked = 0;
var patrolSet = 0;
//PICK CARRIER, ON CLICKS PLACE SHIP PARTS
//PICK ONE SHIP. PLACE ITS PARTS ON GRID
$(".carrier").click(function () {
    if (!(shipObj_1_Location.length == 5)) {
        console.log("on click works");
        carrierClicked = 1;
        var size = 5;
        onClicks(size, shipObj_1_Location, carrierClicked);
        $("#actionText").html("You Are Placing Carrier")

    }

})
$(".battleship").click(function () {
    if (!(shipObj_2_Location.length == 4)) {
        console.log("on click works");
        battleshipClicked = 1;
        var size = 4;
        onClicks(size, shipObj_2_Location, battleshipClicked);
        $("#actionText").html("You Are Placing Battleship")
    }

})
$(".submarine").click(function () {
    if (!(shipObj_3_Location.length == 3)) {
        console.log("on click works");
        submarineClicked = 1;
        var size = 3;
        onClicks(size, shipObj_3_Location, submarineClicked);
        $("#actionText").html("You Are Placing Submarine")
    }

})
$(".destroyer").click(function () {
    if (!(shipObj_4_Location.length == 3)) {
        console.log("on click works");
        destroyerClicked = 1;
        var size = 3;
        onClicks(size, shipObj_4_Location, destroyerClicked);
        $("#actionText").html("You Are Placing Destroyer")
    }

})
$(".patrol").click(function () {
    if (!(shipObj_5_Location.length == 2)) {
        console.log("on click works");
        patrolClicked = 1;
        var size = 2;
        onClicks(size, shipObj_5_Location, patrolClicked);
        $("#actionText").html("You Are Placing Patrol Boat")
    }

})

function onClicks(size, locationArray, shipClicked) {
    $(document).ready(function () {

        $(".empty").on("click", function () {
            if (locationArray.length < size && shipClicked == 1) {
                var div = $(this);
                var location = div.attr("data-location");
                locationArray.push(location);
                console.log(locationArray);
                console.log(locationArray.length);
                div.css("background-color", "yellow");
            }
            if (locationArray.length == size && shipClicked == 1) {
                carrierSet = 1;
                shipClicked = 0;
                console.log("ship size " + size + " clicked" + shipClicked)
            }

        })
    })
}


//NOTE TO SELF - SHOULD DISABLE SHOOT WHEN ARR>=5 INTERFERES WITH RESET
var salvoLocations = [];

$("#resetSalvoes").click(function () {
    salvoLocations = [];
    $(".empty").css("background-color", "aqua");
    $(".empty").attr("shot", "no");
    shooting = true;
})

var shooting = false;
$("#chooseSalvoes").click(function () {
    console.log("chooseSalvoes works");
    shooting = true;
    pickSalvoLocations();
})

function pickSalvoLocations() {
    $(document).ready(function () {

        $("#salvoes .empty").on("click", function () {

            if (shooting == true) {
                var salvoLocation = $(this).attr("data-location");
                //                console.log(salvoLocation);
                if ($(this).attr("shot") == "yes") {
                    $(this).css("background-color", "aqua");
                    $(this).attr("shot", "no");
                    salvoLocations = salvoLocations.filter(e => e !== salvoLocation)
                    console.log(salvoLocations);
                } else {
                    $(this).css("background-color", "black");
                    $(this).attr("shot", "yes");

                    salvoLocations.push(salvoLocation);
                    console.log(salvoLocations);
                }
            }

            if (salvoLocations.length == 5) {
                console.log("its 5");
                shooting = false;
            }
        })
    })
}
$("#postSalvoes").click(function () {
    if (salvoLocations.length < 5) {
        console.log("Didnt shoot 5 times");
        $("#actionText").html("You didnt shoot 5 times! Currently you shot only " + salvoLocations.length + "times");
    } else {
        var url = "/api/games/players/"+gamePlayerNumber+"/salvoes";
        $.post({
            url: url,
            data: JSON.stringify({
                turnNumber: "1",
                salvoLocations: salvoLocations
            }),
            dataType: "text",
            contentType: "application/json"
        })
        console.log("salvoes posted");
        $("#actionText").html("Your Salvoes have been succesfully sent!");
    }

})


