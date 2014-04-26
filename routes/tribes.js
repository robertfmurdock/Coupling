"use strict";
var DataService = require('../lib/CouplingDataService');

module.exports = function (mongoUrl) {
    var dataService = new DataService(mongoUrl);
    return function (request, response) {
        dataService.requestTribes(function (tribes) {
            response.send(tribes);
        });
    };
};