"use strict";
var DataService = require('../lib/CouplingDataService');

module.exports = function (mongoUrl) {
    var dataService = new DataService(mongoUrl);

    this.list = function (request, response) {
        dataService.requestPins(request.params.tribeId).then(function (pins) {
            response.send(pins);
        });
    };

    this.savePin = function (request, response) {
        var pin = request.body;
        dataService.savePin(pin, function () {
            response.send(pin);
        });
    };
};