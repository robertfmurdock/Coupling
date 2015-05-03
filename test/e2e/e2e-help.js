var util = require('util');

var helper = {
  afterEachAssertLogsAreEmpty: function () {

    afterEach(function (done) {
      browser.waitForAngular();
      browser.manage().logs().get('browser').then(function (browserLog) {
        expect(browserLog).toEqual([]);
        console.log('log: ' + util.inspect(browserLog));
        done();
      }, done);
    });
  }
};

module.exports = helper;