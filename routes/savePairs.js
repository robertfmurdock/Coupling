"use strict";
var monk = require('monk');

module.exports = function (mongoUrl) {
    return function (request, response) {
        var pairs = request.body;
        if (pairs.date && pairs.pairs) {
            var database = monk(mongoUrl);
            var historyCollection = database.get('history');
            pairs.date = new Date(pairs.date);
            historyCollection.insert(pairs, function () {
                response.send(pairs);
            });
        }
        else {
            response.statusCode = 400;
            response.send({error: 'Pairs were not valid.'});
        }
    };
};