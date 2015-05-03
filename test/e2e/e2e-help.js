var util = require('util');

var helper = {
  afterEachAssertLogsAreEmpty: function () {

    afterEach(function (done) {
      browser.waitForAngular();
      browser.manage().logs().get('browser').then(function (browserLog) {
        expect(browserLog).toEqual([]);
        if (browserLog.length > 0) {
          console.log('log: ' + util.inspect(browserLog));
        }
        done();
      }, done);
    });
  }
};

module.exports = helper;