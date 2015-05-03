var helper = {
  afterEachAssertLogsAreEmpty: function () {

    afterEach(function (done) {
      browser.manage().logs().get('browser').then(function (browserLog) {
        expect(browserLog).toEqual([]);
        console.log('log: ' + require('util').inspect(browserLog));
        done();
      }, done);
    });
  }
};

module.exports = helper;