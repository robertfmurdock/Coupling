var express = require('express');
var routes = require('./routes');
var HistoryRoutes = require('./routes/history');
var PlayerRoutes = require('./routes/players');
var TribeRoutes = require('./routes/tribes');
var game = require('./routes/game');
var http = require('http');
var path = require('path');
var config = require('./config');
var passport = require('passport');
var GoogleStrategy = require('passport-google').Strategy;
var favicon = require('serve-favicon');
var logger = require('morgan');
var methodOverride = require('method-override');
var errorHandler = require('errorhandler');
var bodyParser = require('body-parser');
var cookieParser = require('cookie-parser');
var session = require('express-session');

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
    saveUninitialized: true
}));
app.use(passport.initialize());
app.use(passport.session());

// development only
if ('development' == app.get('env')) {
    app.use(errorHandler());
}

passport.serializeUser(function (user, done) {
    done(null, '1');
});

passport.deserializeUser(function (id, done) {
    done(null, id);
});

passport.use(new GoogleStrategy({
        returnURL: 'http://localhost:' + config.port + '/auth/google/return',
        realm: 'http://localhost:' + config.port + '/'
    },
    function (identifier, profile, done) {
        done(null, {id: '1'});
    }
));

app.get('/auth/google', passport.authenticate('google'));
app.get('/auth/google/return',
    passport.authenticate('google', {
        successRedirect: '/',
        failureRedirect: '/auth/google'
    }));

var checkApiAccess = function (req, res, next) {
    if (config.requiresAuthentication && !req.isAuthenticated())
        res.send(401);
    else
        next();
};

var tribes = new TribeRoutes(config.mongoUrl);
app.get('/', routes.index);
app.get('/api/*', checkApiAccess);
app.post('/api/*', checkApiAccess);
app.delete('/api/*', checkApiAccess);

app.get('/api/tribes', tribes.list);
app.post('/api/tribes', tribes.save);
app.post('/api/:tribeId/game', game(config.mongoUrl));

var history = new HistoryRoutes(config.mongoUrl);
var historyRoute = '/api/:tribeId/history';
app.get(historyRoute, history.list);
app.post(historyRoute, history.savePairs);
app.delete(historyRoute + '/:id', history.deleteMember);

var players = new PlayerRoutes(config.mongoUrl);
app.get('/api/:tribeId/players', players.listTribeMembers);
app.post('/api/:tribeId/players', players.savePlayer);
app.delete('/api/:tribeId/players/:playerId', players.removePlayer);

app.get('/partials/:name', routes.partials);
app.get('*', routes.index);

http.createServer(app).listen(app.get('port'), function () {
    console.log('Express server listening on port ' + app.get('port'));
});
