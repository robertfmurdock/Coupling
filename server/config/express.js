"use strict";
var compression = require('compression');
var path = require('path');
var favicon = require('serve-favicon');
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
var statsd = require('express-statsd');
var config = require('./../../config');

module.exports = function (app, userDataService) {
  app.use(compression());
  app.use(statsd({host: 'statsd', port: 8125}));

  app.set('port', config.port);
  app.set('views', [
    path.join(__dirname, '../public'),
    path.join(__dirname, '../views')
  ]);
  console.log('dirname: ', __dirname);
  console.log(app.get('views'));
  app.set('view engine', 'pug');
  app.use(favicon('public/images/favicon.ico'));
  app.use(logger('dev'));
  app.use(bodyParser.urlencoded({extended: true}));
  app.use(bodyParser.json());
  app.use(methodOverride());

  app.use(express.static(path.join(__dirname, '../public')));
  app.use(cookieParser());
  app.use(session({
    secret: config.secret,
    resave: true,
    saveUninitialized: true,
    store: new MongoStore({
      url: config.mongoUrl
    })
  }));
  app.use(passport.initialize());
  app.use(passport.session());

  var isInDevelopmentMode = 'development' == app.get('env') || 'test' == app.get('env');
  if (isInDevelopmentMode) {
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
  console.log("App environment is: " + app.get('env'));
  if (isInDevelopmentMode) {
    console.log('Dev Environment: enabling test login');
    passport.use(new LocalStrategy(function (username, password, done) {
      userDataService.findOrCreate(username + "._temp", function (user) {
        done(null, user);
      });
    }));
  }
};