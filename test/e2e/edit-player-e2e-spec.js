"use strict";
var monk = require("monk");
var config = require("../../config");

var hostName = 'http://localhost:' + config.port;
var database = monk(config.mongoUrl);
var tribeCollection = database.get('tribes');
var playersCollection = database.get('players');

describe('The edit player page', function () {

    var tribe = {_id: 'delete_me', name: 'Change Me'};
    var player = {_id: 'delete_me', tribe: 'delete_me', name: 'Voidman'};
    beforeEach(function (done) {
        browser.get(hostName + '/test-login?username="username"&password="pw"');
        tribeCollection.insert(tribe);
        playersCollection.insert(player, function () {
            done();
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