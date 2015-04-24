console.log("Starting express init!");
var express = require('express');
var http = require('http');
var path = require('path');
var UserDataService = require('./lib/UserDataService');
var CouplingDataService = require('./lib/CouplingDataService');
var config = require('./../config');

console.log("Finished requires, starting express!");
var app = express();

var couplingDataService = new CouplingDataService(config.mongoUrl);
console.log('Connecting to mongo URL: ' + config.mongoUrl);
var userDataService = new UserDataService(couplingDataService.database);

require('./config/express')(app, userDataService);
console.log("Adding routing!");
require('./routes')(app, userDataService, couplingDataService);

console.log("creating server!");
http.createServer(app).listen(app.get('port'), function () {
    console.log('Express server listening on port ' + app.get('port'));
    console.log('Deployed at: ' + config.buildDate);
    console.log('Git revision: ' + config.gitRev);
});
console.log('Finished Express init!');