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
        protractor.getInstance().switchTo().alert().then(function (alert) {
            alert.dismiss();
        }, function (thereIsNoAlert) {
        });
        tribeCollection.remove({_id: tribe._id}, false);
        playersCollection.remove({_id: player._id}, false);
    });

    it('should not alert on leaving when nothing has changed.', function (done) {
        browser.get(hostName + '/' + tribe._id + '/player/' + player._id);
        element(By.id('spin-button')).click();
        protractor.getInstance().switchTo().alert().then(function (alert) {
            expect(alert).toBeUndefined();
        }, function () {
            done();
        });
    });

    it('should get alert on leaving when name is changed.', function () {
        browser.get(hostName + '/' + tribe._id + '/player/' + player._id);
        element(By.id('player-name')).sendKeys('completely different name');
        element(By.id('spin-button')).click();
        var alertDialog = protractor.getInstance().switchTo().alert();
        expect(alertDialog).toBeDefined();
        alertDialog.dismiss();
    });
});