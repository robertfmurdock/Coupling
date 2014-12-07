"use strict";

var passport = require('passport');
var routes = require('./routes/index');
var HistoryRoutes = require('./routes/history');
var PlayerRoutes = require('./routes/players');
var PinRoutes = require('./routes/pins');
var apiGuard = require('./routes/api-guard');
var spin = require('./routes/spin');
var config = require('./../config');

module.exports = function (app) {
    app.get('/welcome', routes.welcome);
    app.get('/auth/google', passport.authenticate('google'));
    app.get('/auth/google/callback', passport.authenticate('google', {
        successRedirect: '/',
        failureRedirect: '/auth/google'
    }));
    if ('development' == app.get('env')) {
        app.get('/test-login', passport.authenticate('local', {successRedirect: '/', failureRedirect: '/login'}));
    }

    app.get('/', routes.index);
    app.all('/api/*', apiGuard);
    app.use('/api/tribes', require('./routes/tribes'));
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
};