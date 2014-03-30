var monk = require('monk');
var CouplingWheelFactory = function (mongoUrl, wheelIsConstructed) {

    var database = monk(mongoUrl);
    var players = database.get('players');


    players.find({}, function (error, documents) {
        var wheel = {};
        wheel.players = documents;
        wheelIsConstructed(wheel);
    });
};

module.exports = CouplingWheelFactory;
