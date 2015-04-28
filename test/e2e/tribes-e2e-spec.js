"use strict";
var monk = require("monk");
var config = require("../../config");
var _ = require('underscore');
var RSVP = require('rsvp');
var hostName = 'http://' + config.publicHost + ':' + config.port;
var database = monk(config.tempMongoUrl);
var tribeCollection = database.get('tribes');
var usersCollection = monk(config.mongoUrl).get('users');

var userEmail = 'protractor@test.goo';

function authorizeUserForTribes(authorizedTribes) {
    var tempUserEmail = userEmail + "._temp";
    return usersCollection.update({
        email: tempUserEmail
    }, {
        $set: {
            tribes: authorizedTribes
        }
    }).then(function(updateCount) {
        if (updateCount == 0) {
            console.log('INSERTING USER');
            return usersCollection.insert({
                email: tempUserEmail,
                tribes: authorizedTribes
            });
        } else {
            console.log('updating  USER ' + updateCount);
        }
    });
}

function authorizeAllTribes() {
    return tribeCollection.find({}, {})
        .then(function(tribeDocuments) {
            var authorizedTribes = _.pluck(tribeDocuments, '_id');
            return authorizeUserForTribes(authorizedTribes);
        });
}

describe('The default tribes page', function() {

    var tribeDocuments;

    beforeEach(function(done) {
        browser.driver.manage().deleteAllCookies();
        tribeCollection.drop()
            .then(function() {
                return tribeCollection.insert(
                    [{
                        _id: 'e2e1',
                        name: 'E2E Example Tribe 1'
                    }, {
                        _id: 'e2e2',
                        name: 'E2E Example Tribe 2'
                    }]);
            }).then(function() {
                return authorizeAllTribes();
            }).then(function() {
                return tribeCollection.find({}, {})
            }).then(function(result) {
                tribeDocuments = result;

                browser.get(hostName + '/test-login?username=' + userEmail + '&password="pw"');
                browser.get(hostName);
                browser.refresh();
                element(By.tagName('body')).allowAnimations(false);
                element(By.css('.view-frame')).allowAnimations(false);
                done();
            }, done);
    });

    it('should have a section for each tribe', function() {
        browser.wait(function() {
            return browser.driver.isElementPresent(By.css('.tribe-listing'));
        }, 5000);
        expect(browser.getCurrentUrl()).toEqual(hostName + '/tribes/');
        var tribeElements = element.all(By.repeater('tribe in tribes'));
        expect(tribeElements.getText()).toEqual(_.pluck(tribeDocuments, 'name'));
    });

    it('can navigate to the a specific tribe page', function() {
        expect(browser.getCurrentUrl()).toEqual(hostName + '/tribes/');
        browser.wait(function() {
            return browser.driver.isElementPresent(By.css('.ng-animate'))
                .then(function(result) {
                    return !result;
                });
        }, 5000);

        var tribeElements = element.all(By.repeater('tribe in tribes'));
        tribeElements.first().element(By.css('.tribe-name')).click();
        expect(browser.getCurrentUrl()).toEqual(hostName + '/' + tribeDocuments[0]._id + '/');
    });

    it('can navigate to the new tribe page', function() {
        element(By.id('new-tribe-button')).click();
        expect(browser.getCurrentUrl()).toBe(hostName + '/new-tribe/');
    });

    describe('when a tribe exists, on the tribe page', function() {

        var expectedTribe;
        beforeEach(function() {
            expectedTribe = tribeDocuments[0];
            browser.get(hostName + '/' + expectedTribe._id + '/');
            browser.refresh();
            element(By.tagName('body')).allowAnimations(false);
        });

        afterEach(function(done) {
            browser.manage().logs().get('browser').then(function(browserLogs) {
                if (browserLogs.length != 0) {
                    console.log('LOGS CAPTURED:');
                }
                browserLogs.forEach(function(log) {
                    console.log(log.message);
                });
                if (browserLogs.length != 0) {
                    console.log('END LOGS');
                }
                done();
            });
        });

        it('the tribe name is shown', function() {
            element(By.css('.view-frame')).allowAnimations(false);
            expect(browser.getCurrentUrl()).toEqual(hostName + '/' + expectedTribe._id + '/');
            var tribeNameElement = element.all(By.id('tribe-name')).first();
            expect(tribeNameElement.getAttribute('value')).toEqual(expectedTribe.name);
        });

        it('the tribe image url is shown', function() {
            element(By.css('.view-frame')).allowAnimations(false);
            expect(browser.getCurrentUrl()).toEqual(hostName + '/' + expectedTribe._id + '/');
            var tribeNameElement = element.all(By.id('tribe-img-url')).first()
            var expectedValue = expectedTribe.imgURL || '';
            expect(tribeNameElement.getAttribute('value')).toEqual(expectedValue);
        });

        it('the tribe email is shown', function() {
            element(By.css('.view-frame')).allowAnimations(false);
            expect(browser.getCurrentUrl()).toEqual(hostName + '/' + expectedTribe._id + '/');
            var tribeNameElement = element.all(By.id('tribe-email')).first()
            var expectedValue = expectedTribe.email || '';
            expect(tribeNameElement.getAttribute('value')).toEqual(expectedValue);
        });
    });

    describe('on the new tribe page', function() {

        it('the id field shows and does not disappear when text is added', function() {
            browser.get(hostName + '/new-tribe/');
            var tribeIdElement = element(By.id('tribe-id'))
            tribeIdElement.sendKeys('oopsie');
            expect(tribeIdElement.isDisplayed()).toBe(true);
        });
    });
});

xdescribe('The edit tribe page', function() {
    var tribe = {
        _id: 'delete_me',
        name: 'Change Me'
    };
    beforeEach(function(done) {
        browser.ignoreSynchronization = true;
        tribeCollection.insert(tribe);
        authorizeAllTribes(done);
    });

    afterEach(function() {
        tribeCollection.remove({
            _id: tribe._id
        }, false);
    });

    it('can save edits to a tribe correctly', function() {
        browser.get(hostName + '/' + tribe._id);

        var expectedNewName = 'Different name';
        element(By.id('tribe-name')).clear();
        element(By.id('tribe-name')).sendKeys(expectedNewName);
        element(By.id('save-tribe-button')).click();

        browser.get(hostName + '/' + tribe._id);

        expect(element(By.id('tribe-name')).getAttribute('value')).toEqual(expectedNewName);
    });
});