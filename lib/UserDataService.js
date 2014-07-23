var monk = require('monk');

var UserDataService = function UserDataService(mongoUrl) {
    var database = monk(mongoUrl);
    var usersCollection = database.get('users');

    this.findOrCreate = function findOrCreate(email, callback) {
        usersCollection.find({email: email}, function (error, foundUsers) {
            if (foundUsers.length > 0) {
                callback(foundUsers[0]);
            } else {
                var user = {email: email};
                usersCollection.insert(user, function () {
                    callback(user);
                });
            }
        });
    }
};

module.exports = UserDataService;