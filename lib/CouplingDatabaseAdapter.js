var monk = require('monk');
var CouplingDatabaseAdapter = function (mongoUrl, dataIsAvailable, errorHandler) {
    var database = monk(mongoUrl);
    var playersCollection = database.get('players');
    var historyCollection = database.get('history');

    playersCollection.find({}, function (error, playerDocuments) {
        if (!error) {
            var sortNewestToOldest = {sort: {date: -1}};
            historyCollection.find({}, sortNewestToOldest, function (error, historyDocuments) {
                if (!error) {
                    dataIsAvailable(playerDocuments, historyDocuments)
                } else {
                    errorHandler({message: 'Could not read from MongoDB.', error: error});
                }
            });
        } else {
            errorHandler({message: 'Could not read from MongoDB.', error: error});
        }
    });
};

module.exports = CouplingDatabaseAdapter;