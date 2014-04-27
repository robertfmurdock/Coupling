/**
 * Module dependencies.
 */

var express = require('express');
var routes = require('./routes');
var HistoryRoutes = require('./routes/history');
var PlayerRoutes = require('./routes/players');
var tribes = require('./routes/tribes');
var game = require('./routes/game');
var http = require('http');
var path = require('path');
var config = require('./config');

var app = express();

// all environments
app.set('port', process.env.PORT || 3000);
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'jade');
app.use(express.favicon());
app.use(express.logger('dev'));
app.use(express.json());
app.use(express.urlencoded());
app.use(express.methodOverride());
app.use(express.static(path.join(__dirname, 'public')));
app.use(app.router);

// development only
if ('development' == app.get('env')) {
    app.use(express.errorHandler());
}

app.get('/', routes.index);
app.get('/api/tribes', tribes(config.mongoUrl));
app.post('/api/:tribeId/game', game(config.mongoUrl));

var history = new HistoryRoutes(config.mongoUrl);
var historyRoute = '/api/:tribeId/history';
app.get(historyRoute, history.list);
app.post(historyRoute, history.savePairs);

app.delete(historyRoute + '/:id', history.deleteMember);

var players = new PlayerRoutes(config.mongoUrl);
var playersRoute = '/api/:tribeId/players';
app.get(playersRoute, players.listTribeMembers);
app.post(playersRoute, players.savePlayer);

app.get('/partials/:name', routes.partials);
app.get('*', routes.index);

http.createServer(app).listen(app.get('port'), function () {
    console.log('Express server listening on port ' + app.get('port'));
});
