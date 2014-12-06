console.log("Starting express init!");
var express = require('express');
var http = require('http');
var path = require('path');

var UserDataService = require('./lib/UserDataService');
var routes = require('./routes/index');
var HistoryRoutes = require('./routes/history');
var PlayerRoutes = require('./routes/players');
var PinRoutes = require('./routes/pins');
var TribeRoutes = require('./routes/tribes');
var spin = require('./routes/spin');
var config = require('./../config');
var DataService = require('./lib/CouplingDataService');
var passport = require('passport');

var userDataService = new UserDataService(config.mongoUrl);

console.log("Finished requires, starting express!");
var app = express();

require('./config/express')(app, userDataService);

console.log("Adding routing!");

app.get('/welcome', routes.welcome);
app.get('/auth/google', passport.authenticate('google'));
app.get('/auth/google/callback', passport.authenticate('google', {
    successRedirect: '/',
    failureRedirect: '/auth/google'
}));

if ('development' == app.get('env')) {
    app.get('/test-login', passport.authenticate('local', {successRedirect: '/', failureRedirect: '/login'}));
}

var dataService = new DataService(config.mongoUrl);
var tempDataService = new DataService(config.tempMongoUrl);
var checkApiAccess = function (request, response, next) {
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

var tribes = new TribeRoutes();
app.get('/', routes.index);
app.all('/api/*', checkApiAccess);
app.route('/api/tribes')
    .get(tribes.list)
    .post(tribes.save);
app.post('/api/:tribeId/spin', spin());

var history = new HistoryRoutes();
app.route('/api/:tribeId/history')
    .get(history.list)
    .post(history.savePairs);
app.delete('/api/:tribeId/history/:id', history.deleteMember);

var players = new PlayerRoutes();
app.route('/api/:tribeId/players')
    .get(players.listTribeMembers)
    .post(players.savePlayer);
app.delete('/api/:tribeId/players/:playerId', players.removePlayer);

var pins = new PinRoutes();
app.route('/api/:tribeId/pins')
    .get(pins.list)
    .post(pins.savePin);
app.delete('/api/:tribeId/pins/:pinId', pins.removePin);

app.get('/partials/:name', routes.partials);
app.get('*', routes.index);

console.log("creating server!");
http.createServer(app).listen(app.get('port'), function () {
    console.log('Express server listening on port ' + app.get('port'));
    console.log('Deployed at: ' + config.buildDate);
    console.log('Git revision: ' + config.gitRev);
});
console.log('Finished Express init!');