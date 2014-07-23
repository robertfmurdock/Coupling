var monk = require('monk');

var UserDataService = function UserDataService(mongoUrl) {
    var database = monk(mongoUrl);
    var usersCollection = database.get('users');

    this.findOrCreate = function findOrCreate(email, callback) {
        var user = {email: email};
        usersCollection.insert(user, function () {
            callback(user);
        });

    }
};

module.exports = UserDataService;