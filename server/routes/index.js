var config = require('../../config');

exports.index = function (request, response) {
  if (!request.isAuthenticated()) {
    response.redirect('/welcome');
  } else {
    response.render('index', {title: 'Coupling', buildDate: config.buildDate, gitRev: config.gitRev});
  }
};

exports.welcome = function (request, response) {
  response.render('welcome', {buildDate: config.buildDate, gitRev: config.gitRev});
};

exports.partials = function (request, response) {
  response.render('partials/' + request.params.name);
};

exports.components = function (request, response) {
  var formatPathForViewEngine = request.path.slice(1, -5);
  response.render(formatPathForViewEngine);
};