"use strict";
var monk = require("monk");
var _ = require('underscore');
var config = require("../../config");

var hostName = 'http://localhost:' + config.port;
var database = monk(config.tempMongoUrl);
var tribeCollection = database.get('tribes');
var playersCollection = database.get('players');
var usersCollection = monk(config.mongoUrl).get('users');

describe('The edit player page', function () {

    var userEmail = 'protractor@test.goo';

    function authorizeUserForTribes(authorizedTribes, callback) {
        usersCollection.update({email: userEmail}, {$set: {tribes: authorizedTribes}}, function (error, updateCount) {
            if (updateCount == 0) {
                usersCollection.insert({email: userEmail, tribes: authorizedTribes}, callback);
            } else {
                callback();
            }
        });
    }

    var tribe = {_id: 'delete_me', name: 'Change Me'};
    var player = {_id: 'delete_me', tribe: 'delete_me', name: 'Voidman'};

    beforeEach(function (done) {
        protractor.getInstance().ignoreSynchronization = true;
        tribeCollection.insert(tribe);
        tribeCollection.find({}, {}, function (error, tribeDocuments) {
            var authorizedTribes = _.pluck(tribeDocuments, '_id');
            authorizeUserForTribes(authorizedTribes, function () {
                browser.get(hostName + '/test-login?username=' + userEmail + '&password="pw"');
                playersCollection.insert(player, function () {
                    done();
                });
            });
        });
    });

    afterEach(function () {
        tribeCollection.remove({_id: tribe._id}, false);
        playersCollection.remove({_id: player._id}, false);
    });

    it('should not alert on leaving when nothing has changed.', function () {
        browser.get(hostName + '/' + tribe._id + '/player/' + player._id);
        element(By.id('spin-button')).click();
        expect(protractor.getInstance().getCurrentUrl()).toBe(hostName + '/' + tribe._id + '/pairAssignments/new/');
    });

    it('should get alert on leaving when name is changed.', function (done) {
        browser.get(hostName + '/' + tribe._id + '/player/' + player._id);
        element(By.id('player-name')).sendKeys('completely different name');
        element(By.id('spin-button')).click();
        protractor.getInstance().switchTo().alert().then(function (alertDialog) {
            alertDialog.dismiss();
            done();
        }, function (error) {
            done(error);
        });
    });

    it('should not get alert on leaving when name is changed after save.', function () {
        browser.get(hostName + '/' + tribe._id + '/player/' + player._id);
        element(By.id('player-name')).sendKeys('completely different name');
        element(By.id('save-player-button')).click();
        element(By.id('spin-button')).click();
        expect(protractor.getInstance().getCurrentUrl()).toBe(hostName + '/' + tribe._id + '/pairAssignments/new/');
    });
});