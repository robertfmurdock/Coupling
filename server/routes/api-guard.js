var CouplingDataService = require('../lib/CouplingDataService');
var config = require('../../config');

module.exports = function (couplingDataService) {
    var tempDataService = new CouplingDataService(config.tempMongoUrl);
    return function (request, response, next) {
        if (!request.isAuthenticated()) {
            response.sendStatus(401);
        } else {
            if (request.user.email.indexOf('._temp') != -1) {
                request.dataService = tempDataService;
            } else {
                request.dataService = couplingDataService;
            }
            next();
        }
    };
};