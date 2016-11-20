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

var junitReporter = new reporters.JUnitXmlReporter({
  savePath: '../../results/unit.server.xml'
});
jasmine.addReporter(junitReporter);

jasmine.execute();
