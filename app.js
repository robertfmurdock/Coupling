console.log("Starting express init!");
var express = require('express');
var compression = require('compression');
var minify = require('express-minify');
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
var DataService = require('./lib/CouplingDataService');
var sassMiddleware = require('node-sass-middleware');
var userDataService = new UserDataService(config.mongoUrl);

console.log("Finished requires, starting express!");
var app = express();

app.use(compression());
app.use(minify({cache: __dirname + '/cache'}));

app.set('port', config.port);
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'jade');
app.use(favicon('public/images/favicon.ico'));
app.use(sassMiddleware({
    src: __dirname + '/public/stylesheets',
    dest: __dirname + '/public/stylesheets',
    debug: false,
    outputStyle: 'expanded',
    prefix: '/stylesheets'
}));
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

console.log("Adding routing!");

app.get('/welcome', routes.welcome);
app.get('/auth/google', passport.authenticate('google'));
app.get('/auth/google/callback', passport.authenticate('google', {
    successRedirect: '/',
    failureRedirect: '/auth/google'
}));

if ('development' == app.get('env')) {
    console.log('Dev Environment: enabling test login');
    passport.use(new LocalStrategy(function (username, password, done) {
        userDataService.findOrCreate(username + "._temp", function (user) {
            done(null, user);
        });
    }));
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