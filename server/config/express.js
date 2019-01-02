"use strict";
const compression = require('compression');
const path = require('path');
const favicon = require('serve-favicon');
const logger = require('morgan');
const methodOverride = require('method-override');
const express = require('express');
const bodyParser = require('body-parser');
const Strategy = require('passport-custom').Strategy;
const {OAuth2Client} = require('google-auth-library');
const errorHandler = require('errorhandler');
const cookieParser = require('cookie-parser');
const session = require('express-session');
const MongoStore = require('connect-mongo')(session);
const passport = require('passport');
const LocalStrategy = require('passport-local').Strategy;
const statsd = require('express-statsd');
const config = require('./config');

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
  if (!process.env['DISABLE_LOGGING']) {
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

  const isInDevelopmentMode = 'development' == app.get('env') || 'test' == app.get('env');
  if (isInDevelopmentMode) {
    app.use(errorHandler());
  }

  passport.serializeUser(userDataService.serializeUser);
  passport.deserializeUser(userDataService.deserializeUser);

  const clientID = config.googleClientID;
  const client = new OAuth2Client(clientID);

  async function verify(token) {
    const ticket = await client.verifyIdToken({
      idToken: token,
      audience: clientID
    });
    return ticket.getPayload();
  }

  passport.use(
    new Strategy(function (request, done) {

      verify(request.body.idToken)
        .then(payload => {
          userDataService.findOrCreate(payload.email, function (user) {
            done(null, user);
          });
        }, err => done(err))

    })
  );

  if (isInDevelopmentMode) {
    passport.use(new LocalStrategy(function (username, password, done) {
      userDataService.findOrCreate(username + "._temp", function (user) {
        done(null, user);
      });
    }));
  }
};