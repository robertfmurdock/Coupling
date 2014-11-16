var config = require('../config');

exports.index = function (request, response) {
    if (config.requiresAuthentication && !request.isAuthenticated()) {
        response.redirect('/welcome');
    } else {
        response.render('index', { title: 'Coupling', buildDate: config.buildDate, gitRev: config.gitRev });
    }
};

exports.welcome = function(request, response){
    response.render('welcome', { buildDate: config.buildDate, gitRev: config.gitRev });
};

exports.partials = function (req, res) {
    res.render('partials/' + req.params.name);
};