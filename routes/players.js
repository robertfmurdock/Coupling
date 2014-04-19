"use strict";
var databaseAdapter = require('../lib/CouplingDatabaseAdapter');

module.exports = function (mongoUrl) {
    return function (request, response) {
        databaseAdapter(mongoUrl, function (players) {
            response.send(players);
        }, function (error) {
            response.send(error);
        });
    };
};