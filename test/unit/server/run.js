var Jasmine = require('jasmine');
var reporters = require('jasmine-reporters');

var jasmine = new Jasmine();

jasmine.loadConfig({
  "spec_dir": "test/unit/server",
  "spec_files": [
    "**/*[sS]pec.js"
  ],
  "helpers": [
    "helpers/**/*.js"
  ],
  "stopSpecOnExpectationFailure": false,
  "random": false
});

jasmine.configureDefaultReporter({

});

var junitReporter = new reporters.JUnitXmlReporter({
  savePath: __dirname + '/../../../test-output',
  filePrefix: 'server.unit',
  consolidateAll: true
});

jasmine.addReporter(junitReporter);

jasmine.execute();
