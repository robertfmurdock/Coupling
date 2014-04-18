exports.index = function (req, res) {
    res.render('index', { title: 'Coupling' });
};

exports.partials = function (req, res) {
    res.render('partials/' + req.params.name);
};