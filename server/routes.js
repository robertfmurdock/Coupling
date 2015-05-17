"use strict";

var passport = require('passport');
var routes = require('./routes/index');
var config = require('./../config');

module.exports = function (app, userDataService, couplingDataService) {
    app.get('/welcome', routes.welcome);
    app.get('/auth/google', passport.authenticate('google'));
    app.get('/auth/google/callback', passport.authenticate('google', {
        successRedirect: '/',
        failureRedirect: '/auth/google'
    }));
    if ('development' == app.get('env') || 'test' == app.get('env')) {
        app.get('/test-login', passport.authenticate('local', {successRedirect: '/', failureRedirect: '/login'}));
    }

    app.get('/', routes.index);
    app.all('/api/*', require('./routes/api-guard')(couplingDataService));
    app.use('/api/tribes', require('./routes/tribeList'));
    app.use('/api/:tribeId', require('./routes/tribe'));
    app.get('/partials/:name', routes.partials);
    app.get('*', routes.index);
};