"use strict";
const compression = require('compression');
const path = require('path');
const favicon = require('serve-favicon');
const logger = require('morgan');
const methodOverride = require('method-override');
const express = require('express');
const bodyParser = require('body-parser');
const GoogleStrategy = require('passport-google-oauth').OAuth2Strategy;
const errorHandler = require('errorhandler');
const cookieParser = require('cookie-parser');
const session = require('express-session');
const MongoStore = require('connect-mongo')(session);
const passport = require('passport');
const LocalStrategy = require('passport-local').Strategy;
const statsd = require('express-statsd');
const config = require('./../../config');

module.exports = function (app, userDataService) {
  app.use(compression());
  app.use(statsd({host: 'statsd', port: 8125}));

  app.set('port', config.port);
  app.set('views', [
    path.join(__dirname, '../public'),
    path.join(__dirname, '../views')
  ]);
  app.set('view engine', 'pug');
  app.use(favicon('public/images/favicon.ico'));
  if(!process.env['DISABLE_LOGGING']) {
    app.use(logger('dev'));
  }

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

  if (isInDevelopmentMode) {
    passport.use(new LocalStrategy(function (username, password, done) {
      userDataService.findOrCreate(username + "._temp", function (user) {
        done(null, user);
      });
    }));
  }
};