exports.players = function (database) {

    var requestHandler = function (request, response) {
        var playersCollection = database.get('players');

        var renderAll = function (error, docs) {
            var content = {
                "players": docs
            };
            response.render('playerRoster', content);
        };
        playersCollection.find({}, {}, renderAll);
    };
    return  requestHandler;
};
