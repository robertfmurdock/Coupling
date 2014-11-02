var express = require('express');
var http = require('http');
var path = require('path');
var passport = require('passport');
var LocalStrategy = require('passport-local').Strategy;
var GoogleStrategy = require('passport-google-oauth').OAuth2Strategy;
var favicon = require('serve-favicon');
var logger = require('morgan');
var methodOverride = require('method-override');
var errorHandler = require('errorhandler');
var bodyParser = require('body-parser');
var cookieParser = require('cookie-parser');
var session = require('express-session');
var MongoStore = require('connect-mongo')(session);

var UserDataService = require('./lib/UserDataService');
var routes = require('./routes');
var HistoryRoutes = require('./routes/history');
var PlayerRoutes = require('./routes/players');
var PinRoutes = require('./routes/pins');
var TribeRoutes = require('./routes/tribes');
var spin = require('./routes/spin');
var config = require('./config');
var userDataService = new UserDataService(config.mongoUrl);

var app = express();

app.set('port', config.port);
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'jade');
app.use(favicon('public/images/favicon.ico'));
app.use(logger('dev'));
app.use(bodyParser.urlencoded({extended: true}));
app.use(bodyParser.json());
app.use(methodOverride());
app.use(express.static(path.join(__dirname, 'public')));
app.use(cookieParser());
app.use(session({
    secret: config.secret,
    resave: true,
    saveUninitialized: true,
    store: new MongoStore({
        url: config.mongoUrl
    }, function () {
        console.log('Finished initializing session storage.');
    })
}));
app.use(passport.initialize());
app.use(passport.session());

// development only
if ('development' == app.get('env')) {
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

app.get('/auth/google', passport.authenticate('google'));
app.get('/auth/google/callback', passport.authenticate('google', { successRedirect: '/', failureRedirect: '/auth/google' }));

if ('development' == app.get('env')) {
    console.log('Dev Environment: enabling test login');
    passport.use(new LocalStrategy(function (username, password, done) {
        userDataService.findOrCreate(username, function (user) {
            done(null, user);
        });
    }));
    app.get('/test-login', passport.authenticate('local', { successRedirect: '/', failureRedirect: '/login' }));
}

var checkApiAccess = function (request, response, next) {
    if (config.requiresAuthentication && !request.isAuthenticated())
        response.send(401);
    else
        next();
};

var tribes = new TribeRoutes(config.mongoUrl);
app.get('/', routes.index);
app.all('/api/*', checkApiAccess);
app.route('/api/tribes')
    .get(tribes.list)
    .post(tribes.save);
app.post('/api/:tribeId/spin', spin(config.mongoUrl));

var history = new HistoryRoutes(config.mongoUrl);
app.route('/api/:tribeId/history')
    .get(history.list)
    .post(history.savePairs);
app.delete('/api/:tribeId/history/:id', history.deleteMember);

var players = new PlayerRoutes(config.mongoUrl);
app.route('/api/:tribeId/players')
    .get(players.listTribeMembers)
    .post(players.savePlayer);
app.delete('/api/:tribeId/players/:playerId', players.removePlayer);

var pins = new PinRoutes(config.mongoUrl);
app.route('/api/:tribeId/pins')
    .get(pins.list)
    .post(pins.savePin);

app.get('/partials/:name', routes.partials);
app.get('*', routes.index);

http.createServer(app).listen(app.get('port'), function () {
    console.log('Express server listening on port ' + app.get('port'));
    console.log('Deployed at: ' + config.buildDate);
    console.log('Git revision: ' + config.gitRev);
});
