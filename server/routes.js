"use strict";

var passport = require('passport');
var routes = require('./routes/index');
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
    app.use('/api/:tribeId/history', require('./routes/history'));
    app.use('/api/:tribeId/players', require('./routes/players'));

    var pins = new PinRoutes();
    app.route('/api/:tribeId/pins')
        .get(pins.list)
        .post(pins.savePin);
    app.delete('/api/:tribeId/pins/:pinId', pins.removePin);

    app.get('/partials/:name', routes.partials);
    app.get('*', routes.index);
};