"use strict";
var databaseAdapter = require('../lib/CouplingDatabaseAdapter');

module.exports = function (mongoUrl) {
    return function (request, response) {
        databaseAdapter(mongoUrl, function (players, history) {
            response.send(history);
        }, response.send);
    };
};