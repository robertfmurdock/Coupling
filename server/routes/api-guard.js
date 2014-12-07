var DataService = require('../lib/CouplingDataService');
var config = require('../../config');
var dataService = new DataService(config.mongoUrl);
var tempDataService = new DataService(config.tempMongoUrl);

module.exports = function (request, response, next) {
    if (!request.isAuthenticated()) {
        response.sendStatus(401);
    } else {
        if (request.user.email.indexOf('._temp') != -1) {
            request.dataService = tempDataService;
        } else {
            request.dataService = dataService;
        }
        next();
    }
};