var Jasmine = require('jasmine');
var reporters = require('jasmine-reporters');


var jasmine = new Jasmine();

jasmine.loadConfig({
  "spec_dir": "test/endpoint",
  "spec_files": [
    "**/*[sS]pec.js"
  ],
  "helpers": [
    "helpers/**/*.js"
  ],
  "stopSpecOnExpectationFailure": false,
  "random": false
});

jasmine.configureDefaultReporter({});

var junitReporter = new reporters.JUnitXmlReporter({
  savePath: __dirname + '/../../test-output',
  filePrefix: 'endpoint.xml',
  consolidateAll: true
});

jasmine.addReporter(junitReporter);

process.env.PORT = 3001;
require('../../server/app')
  .start()
  .then(function () {
    jasmine.execute();
  });