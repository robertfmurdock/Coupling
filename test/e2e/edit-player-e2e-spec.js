"use strict";
var monk = require("monk");
var _ = require('underscore');
var config = require("../../config");

var hostName = 'http://' + config.publicHost + ':' + config.port;
var database = monk(config.tempMongoUrl);
var tribeCollection = database.get('tribes');
var playersCollection = database.get('players');
var usersCollection = monk(config.mongoUrl).get('users');

describe('The edit player page', function () {

    var userEmail = 'protractor@test.goo';

    function authorizeUserForTribes(authorizedTribes) {
        return usersCollection.update({email: userEmail}, {$set: {tribes: authorizedTribes}}).then(function (updateCount) {
            if (updateCount == 0) {
                console.log('INSERTING USER');
                return usersCollection.insert({email: userEmail, tribes: authorizedTribes});
            } else {
                console.log('updating  USER' + updateCount);
            }
        });
    }

    var tribe = {_id: 'delete_me', name: 'Change Me'};
    var player = {_id: 'delete_me', tribe: 'delete_me', name: 'Voidman'};

    beforeEach(function (done) {
        browser.ignoreSynchronization = true;
        tribeCollection.insert(tribe).then(function () {
            return tribeCollection.find({}, {});
        }).then(function (error, tribeDocuments) {
            var authorizedTribes = _.pluck(tribeDocuments, '_id');
            return authorizeUserForTribes(authorizedTribes);
        }).then(function () {
            return browser.get(hostName + '/test-login?username=' + userEmail + '&password="pw"');
        }).then(function () {
            return playersCollection.insert(player);
        }).then(function () {
            done();
        }, done);
    });

    afterEach(function (done) {
        browser.manage().logs().get('browser').then(function (browserLogs) {
            if (browserLogs.length != 0) {
                console.log('LOGS CAPTURED:');
            }
            browserLogs.forEach(function (log) {
                console.log(log.message);
            });
            if (browserLogs.length != 0) {
                console.log('END LOGS');
            }
        });

        tribeCollection.remove({_id: tribe._id}, false)
            .then(function () {
                return playersCollection.remove({_id: player._id}, false);
            }).then(function () {
                done();
            });
    });

    it('should not alert on leaving when nothing has changed.', function (done) {
        browser.get(hostName + '/' + tribe._id + '/player/' + player._id)
            .then(function () {
                expect(browser.getCurrentUrl()).toBe(hostName + '/' + tribe._id + '/player/' + player._id + '/');
                element(By.id('spin-button')).click();
                expect(browser.getCurrentUrl()).toBe(hostName + '/' + tribe._id + '/pairAssignments/new/');
                done();
            });
    });

    it('should get error on leaving when name is changed.', function (done) {
        browser.get(hostName + '/' + tribe._id + '/player/' + player._id)
            .then(function () {
                expect(browser.getCurrentUrl()).toBe(hostName + '/' + tribe._id + '/player/' + player._id + '/');
                return element(By.id('player-name')).sendKeys('completely different name');
            })
            .then(function () {
                element(By.id('spin-button')).click();
                return browser.switchTo().alert()
            })
            .then(function (alertDialog) {
                expect(alertDialog.getText()).toEqual('You have unsaved data. Would you like to save before you leave?');
                alertDialog.dismiss();
                done();
            }, function (error) {
                done(error);
            });
    });

    it('should not get alert on leaving when name is changed after save.', function (done) {
        browser.get(hostName + '/' + tribe._id + '/player/' + player._id)
            .then(function () {
                return element(By.id('player-name')).sendKeys('completely different name');
            }).then(function () {
                element(By.id('save-player-button')).click();
                element(By.id('spin-button')).click();
                expect(browser.getCurrentUrl()).toBe(hostName + '/' + tribe._id + '/pairAssignments/new/');
                done();
            });
    });
});