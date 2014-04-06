var monk = require('monk');
var CouplingDatabaseAdapter = function (mongoUrl, dataIsAvailable) {

    var database = monk(mongoUrl);
    var playersCollection = database.get('players');
    var historyCollection = database.get('history');

    playersCollection.find({}, function (error, playerDocuments) {
        var sortNewestToOldest = {sort: {date: -1}};
        historyCollection.find({}, sortNewestToOldest, function (error, historyDocuments) {
            dataIsAvailable(playerDocuments, historyDocuments);
        });

    });
};

module.exports = CouplingDatabaseAdapter;