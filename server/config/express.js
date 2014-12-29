"use strict";
var compression = require('compression');
var minify = require('express-minify');
var config = require('./../../config');
var path = require('path');
var favicon = require('serve-favicon');
var sassMiddleware = require('node-sass-middleware');
var logger = require('morgan');
var methodOverride = require('method-override');
var express = require('express');
var bodyParser = require('body-parser');
var GoogleStrategy = require('passport-google-oauth').OAuth2Strategy;
var errorHandler = require('errorhandler');
var cookieParser = require('cookie-parser');
var session = require('express-session');
var MongoStore = require('connect-mongo')(session);
var passport = require('passport');
var LocalStrategy = require('passport-local').Strategy;

module.exports = function (app, userDataService) {
    app.use(compression());
    app.use(minify({cache: __dirname + '/../../cache'}));
    app.set('port', config.port);
    app.set('views', path.join(__dirname, '../../views'));
    app.set('view engine', 'jade');
    app.use(favicon('public/images/favicon.ico'));
    app.use(sassMiddleware({
        src: __dirname + '/../../public/stylesheets',
        dest: __dirname + '/../../public/stylesheets',
        debug: false,
        outputStyle: 'expanded',
        prefix: '/stylesheets'
    }));
    app.use(logger('dev'));
    app.use(bodyParser.urlencoded({extended: true}));
    app.use(bodyParser.json());
    app.use(methodOverride());

    app.use(express.static(path.join(__dirname, '../../public')));
    app.use(cookieParser());
    app.use(session({
        secret: config.secret,
        resave: true,
        saveUninitialized: true,
        store: new MongoStore({
            url: 'mongodb://' + config.mongoUrl
        }
        //    , function () {
        //    console.log('Finished initializing session storage.');
        //}
        )
    }));
    app.use(passport.initialize());
    app.use(passport.session());

// development only
    if ('development' == app.get('env')) {
        app.use(errorHandler());
    }
    console.log("Adding passport!");

    passport.serializeUser(userDataService.serializeUser);
    passport.deserializeUser(userDataService.deserializeUser);

    passport.use(new GoogleStrategy({
            clientID: config.googleClientID,
            clientSecret: config.googleClientSecret,
            callbackURL: config.publicUrl + '/auth/google/callback',
            scope: 'https://www.googleapis.com/auth/plus.login email'
        },
        function (accessToken, refreshToken, profile, done) {
            userDataService.findOrCreate(profile.emails[0].value, function (user) {
                done(null, user);
            });
        }
    ));
    if ('development' == app.get('env')) {
        console.log('Dev Environment: enabling test login');
        passport.use(new LocalStrategy(function (username, password, done) {
            userDataService.findOrCreate(username + "._temp", function (user) {
                done(null, user);
            });
        }));
    }
};