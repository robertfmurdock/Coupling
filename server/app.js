var http = require('http');
var path = require('path');
var UserDataService = require('./lib/UserDataService').default;
var CouplingDataService = require('./lib/CouplingDataService').default;
var config = require('./../config');
var Promise = require('bluebird');
var express = require('express');

module.exports = {
  start: function () {

    console.log("Starting express init!");
    var app = express();

    var couplingDataService = new CouplingDataService(config.mongoUrl);
    var userDataService = new UserDataService(couplingDataService.database);

    require('./config/express')(app, userDataService);
    console.log("Adding routing!");
    require('./routes')(app, userDataService, couplingDataService);

    console.log("creating server!");

    return new Promise(function (resolve) {
      http.createServer(app)
        .listen(app.get('port'), function () {
          console.log('Express server listening on port ' + app.get('port'));
          console.log('Deployed at: ' + config.buildDate);
          console.log('Git revision: ' + config.gitRev);
          console.log('Finished Express init!');
          resolve();
        });
    });
  }
};