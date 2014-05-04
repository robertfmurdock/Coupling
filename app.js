/**
 * Module dependencies.
 */

var express = require('express');
var routes = require('./routes');
var HistoryRoutes = require('./routes/history');
var PlayerRoutes = require('./routes/players');
var TribeRoutes = require('./routes/tribes');
var game = require('./routes/game');
var http = require('http');
var path = require('path');
var config = require('./config');
var favicon = require('serve-favicon');
var logger = require('morgan');
var methodOverride = require('method-override');
var errorHandler = require('errorhandler');
var bodyParser = require('body-parser');

var app = express();

app.set('port', process.env.PORT || config.port);
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'jade');
app.use(favicon('public/images/favicon.ico'));
app.use(logger('dev'));
app.use(bodyParser());
app.use(methodOverride());
app.use(express.static(path.join(__dirname, 'public')));

// development only
if ('development' == app.get('env')) {
    app.use(errorHandler());
}

var tribes = new TribeRoutes(config.mongoUrl);

app.get('/', routes.index);
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
