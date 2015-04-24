console.log("Starting express init!");
var express = require('express');
var http = require('http');
var path = require('path');
var UserDataService = require('./lib/UserDataService');
var config = require('./../config');

var userDataService = new UserDataService(config.mongoUrl);

console.log("Finished requires, starting express!");
var app = express();
require('./config/express')(app, userDataService);
console.log("Adding routing!");
require('./routes')(app, userDataService);

console.log("creating server!");
http.createServer(app).listen(app.get('port'), function () {
    console.log('Express server listening on port ' + app.get('port'));
    console.log('Deployed at: ' + config.buildDate);
    console.log('Git revision: ' + config.gitRev);
});
console.log('Finished Express init!');