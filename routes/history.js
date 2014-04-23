"use strict";
var DataService = require('../lib/CouplingDataService');

module.exports = function (mongoUrl) {
    var dataService = new DataService(mongoUrl);
    return function (request, response) {
        dataService.requestHistory(null, function (history) {
            response.send(history);
        }, response.send);
    };
};